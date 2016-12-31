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
import Levenshtein
from networkx.algorithms.dag import topological_sort


# Flatten a list of lists (goes only one level down)
def flatten(in_list):
    return [item for sublist in in_list for item in sublist]


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
## From core_functions.py
def collate(collation, output="table", layout="horizontal", segmentation=True, near_match=False, astar=False,
            detect_transpositions=False, debug_scores=False, properties_filter=None, svg_output=None, indent=False):
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
        # There is already a graph ('graph', without near-match edges) and ranking ('ranking')
        if segmentation:
            raise SegmentationError('segmentation must be set to False for near matching')

        # Walk ranking table in reverse order and add near-match edges to graph
        reverse_topological_sorted_vertices = topological_sort(graph.graph, reverse=True)
        for v in reverse_topological_sorted_vertices:
            target_rank = ranking.byVertex[v]
            in_edges = graph.in_edges(v)
            if len(in_edges) > 1:
                move_candidates = [in_edge[0] for in_edge in in_edges \
                                   if target_rank > ranking.byVertex[in_edge[0]] + 1]
                for move_candidate in move_candidates:
                    min_rank = ranking.byVertex[move_candidate]
                    max_rank = target_rank - 1
                    vertices_to_compare = flatten([ranking.byRank[r] for r in range(min_rank, max_rank + 1)])
                    vertices_to_compare.remove(move_candidate)
                    for vertex_to_compare in vertices_to_compare:
                        ratio = Levenshtein.ratio(str(move_candidate), str(vertex_to_compare))
                        graph.connect_near(vertex_to_compare, move_candidate, ratio)
        # Create new ranking table (passed along to creation of alignment table)
        ranking = VariantGraphRanking.of(graph)

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
