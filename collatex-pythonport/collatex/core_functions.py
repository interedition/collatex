"""
Created on May 3, 2014

@author: Ronald Haentjens Dekker
"""
import re
from xml.etree import ElementTree as etree
from collatex.core_classes import Collation, VariantGraph, join, AlignmentTable, VariantGraphRanking
from collatex.exceptions import SegmentationError
from collatex.experimental_astar_aligner import ExperimentalAstarAligner
import json
from collatex.edit_graph_aligner import EditGraphAligner
from collatex.display_module import display_alignment_table_as_HTML, visualizeTableVerticallyWithColors
from collatex.display_module import display_variant_graph_as_SVG
from collatex.near_matching import process_rank, Scheduler


# Valid options for output are:
# "table" for the alignment table (default)
# "graph" for the variant graph
# "json" for the alignment table exported as JSON
# "xml" for the alignment table as pseudo-TEI XML
#   All columns are output as <app> elements, regardless of whether they have variation
#   Each witness is in a separate <rdg> element with the siglum in a @wit attribute
#       (i.e, witnesses with identical readings are nonetheless in separate <rdg> elements)
# "tei" for the alignment table as TEI XML parallel segmentation (but in no namespace)
#   Wrapper element is always <p>
#   indent=True pretty-prints the output
#       (for proofreading convenience only; does not observe proper white-space behavior)
def collate(collation, output="table", layout="horizontal", segmentation=True, near_match=False, astar=False,
            detect_transpositions=False, debug_scores=False, properties_filter=None, svg_output=None, indent=False, scheduler=Scheduler()):
    # collation may be collation or json; if it's the latter, use it to build a real collation
    if isinstance(collation, dict):
        json_collation = Collation()
        for witness in collation["witnesses"]:
            json_collation.add_witness(witness)
        collation = json_collation

    # assume collation is collation (by now); no error trapping
    if not astar:
        algorithm = EditGraphAligner(collation, near_match=False, detect_transpositions=detect_transpositions, debug_scores=debug_scores, properties_filter=properties_filter)
    else:
        algorithm = ExperimentalAstarAligner(collation, near_match=False, debug_scores=debug_scores)

    # build graph
    graph = VariantGraph()
    algorithm.collate(graph, collation)
    ranking = VariantGraphRanking.of(graph)
    if near_match:
        # Segmentation not supported for near matching; raise exception if necessary
        if segmentation:
            raise SegmentationError('segmentation must be set to False for near matching')

        highestRank = ranking.byVertex[graph.end]
        witnessCount = len(collation.witnesses)

        # do-while loop to avoid looping through ranking while modifying it
        rank = highestRank - 1
        condition = True
        while condition:
            rank = process_rank(scheduler, rank, collation, ranking, witnessCount)
            rank -= 1
            condition = rank > 0

        # # Verify that nodes have been moved
        # print("\nLabels at each rank at end of processing: ")
        # for rank in ranking.byRank:
        #     print("\nRank: " + str(rank))
        #     print([node.label for node in ranking.byRank[rank]])

    # join parallel segments
    if segmentation:
        join(graph)
        ranking = VariantGraphRanking.of(graph)
    # check which output format is requested: graph or table
    if output == "svg":
        return display_variant_graph_as_SVG(graph, svg_output)
    if output == "graph":
        return graph
    # create alignment table
    table = AlignmentTable(collation, graph, layout, ranking)
    if output == "json":
        return export_alignment_table_as_json(table)
    if output == "html":
        return display_alignment_table_as_HTML(table)
    if output == "html2":
        return visualizeTableVerticallyWithColors(table, collation)
    if output == "table":
        return table
    if output == "xml":
        return export_alignment_table_as_xml(table)
    if output == "tei":
        return export_alignment_table_as_tei(table, indent)
    else:
        raise Exception("Unknown output type: " + output)


def export_alignment_table_as_json(table, indent=None, status=False):
    json_output = {"table": []}
    sigli = []
    for row in table.rows:
        sigli.append(row.header)
        json_output["table"].append(
            [[listItem.token_data for listItem in cell] if cell else None for cell in row.cells])
    json_output["witnesses"] = sigli
    if status:
        variant_status = []
        for column in table.columns:
            variant_status.append(column.variant)
        json_output["status"] = variant_status
    return json.dumps(json_output, sort_keys=True, indent=indent, ensure_ascii=False)


def export_alignment_table_as_xml(table):
    readings = []
    for column in table.columns:
        app = etree.Element('app')
        for key, value in sorted(column.tokens_per_witness.items()):
            child = etree.Element('rdg')
            child.attrib['wit'] = "#" + key
            child.text = "".join(str(item.token_data["t"]) for item in value)
            app.append(child)
        # Without the encoding specification, outputs bytes instead of a string
        result = etree.tostring(app, encoding="unicode")
        readings.append(result)
    return "<root>" + "".join(readings) + "</root>"


def export_alignment_table_as_tei(table, indent=None):
    # TODO: Pretty printing makes fragile (= likely to be incorrect) assumptions about white space
    # TODO: To fix pretty printing indirectly, fix tokenization
    p = etree.Element('p')
    app = None
    for column in table.columns:
        if not column.variant:  # no variation
            text_node = "".join(item.token_data["t"] for item in next(iter(column.tokens_per_witness.values())))
            if not (len(p)):  # Result starts with non-varying reading
                p.text = re.sub('\s+$','',text_node) + "\n" if indent else text_node
            else:  # Non-varying reading after some <app>
                app.tail = "\n" + re.sub('\s+$','',text_node) + "\n" if indent else text_node
        else:
            app = etree.Element('app')
            preceding = None  # If preceding is None, we're processing the first <rdg> child
            app.text = "\n  " if indent else None  # Indent first <rdg> if pretty-printing
            value_dict = {}  # keys are readings, values are an unsorted lists of sigla
            for key, value in column.tokens_per_witness.items():
                group = value_dict.setdefault("".join([item.token_data["t"] for item in value]), [])
                group.append(key)
            rdg_dict = {}  # keys are sorted lists of sigla, with "#" prepended; values are readings
            for key, value in value_dict.items():
                rdg_dict[" ".join("#" + item for item in sorted(value))] = key
            for key, value in sorted(rdg_dict.items()):  # sort <rdg> elements by @wit values
                if preceding is not None and indent:  # Change tail of preceding <rdg> to indent current one
                    preceding.tail = "\n  "
                child = etree.Element('rdg')
                child.attrib['wit'] = key
                child.text = value
                app.append(child)
                child.tail = "\n" if indent else None
                # If preceding is not None on an iteration, use its tail indent non-initial current <rdg>
                preceding = child
            p.append(app)
            app.tail = "\n" if indent else None
    # Without the encoding specification, outputs bytes instead of a string
    result = etree.tostring(p, encoding="unicode")
    return result
