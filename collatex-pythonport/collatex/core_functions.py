"""
Created on May 3, 2014

@author: Ronald Haentjens Dekker
"""
import re
from xml.etree import ElementTree as etree
from xml.dom.minidom import Document
from collections import defaultdict
from collatex.core_classes import Collation, VariantGraph, join, AlignmentTable, VariantGraphRanking
from collatex.exceptions import SegmentationError
from collatex.experimental_astar_aligner import ExperimentalAstarAligner
import json
from collatex.edit_graph_aligner import EditGraphAligner
from collatex.display_module import display_alignment_table_as_html, visualize_table_vertically_with_colors
from collatex.display_module import display_variant_graph_as_svg
from collatex.display_module import display_alignment_table_as_csv
from collatex.near_matching import perform_near_match


# Valid options for output are:
# "table" for the alignment table (default)
# "graph" for the variant graph
# "json" for the alignment table exported as JSON
# "csv", "tsv" for CSV and TSV output
# "xml" for the alignment table as pseudo-TEI XML
#   All columns are output as <app> elements, regardless of whether they have variation
#   Each witness is in a separate <rdg> element with the siglum in a @wit attribute
#       (i.e, witnesses with identical readings are nonetheless in separate <rdg> elements)
# "tei" for the alignment table as TEI XML parallel segmentation (but in no namespace)
#   Wrapper element is always <cx:apparatus> in the CollateX namespace
#   indent=True pretty-prints the output
#       (for proofreading convenience only; does not observe proper white-space behavior)
def collate(collation, output="table", layout="horizontal", segmentation=True, near_match=False, astar=False,
            detect_transpositions=False, debug_scores=False, properties_filter=None, indent=False):
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
    algorithm.collate(graph)
    ranking = VariantGraphRanking.of(graph)
    if near_match:
        # Segmentation not supported for near matching; raise exception if necessary
        # There is already a graph ('graph', without near-match edges) and ranking ('ranking')
        if segmentation:
            raise SegmentationError('segmentation must be set to False for near matching')
        ranking = perform_near_match(graph, ranking)

    # join parallel segments
    if segmentation:
        join(graph)
        ranking = VariantGraphRanking.of(graph)
    # check which output format is requested: graph or table
    if output == "svg" or output == "svg_simple":
        return display_variant_graph_as_svg(graph, output)
    if output == "graph":
        return graph
    # create alignment table
    table = AlignmentTable(collation, graph, layout, ranking)
    if output == "json":
        return export_alignment_table_as_json(table)
    if output == "html":
        return display_alignment_table_as_html(table)
    if output == "html2":
        return visualize_table_vertically_with_colors(table, collation)
    if output == "table":
        return table
    if output == "xml":
        return export_alignment_table_as_xml(table)
    if output == "tei":
        return export_alignment_table_as_tei(table, indent)
    if output == "csv" or output == "tsv":
        return display_alignment_table_as_csv(table, output)
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
    d = Document()
    root = d.createElementNS("http://interedition.eu/collatex/ns/1.0", "cx:apparatus") # fake namespace declarations
    root.setAttribute("xmlns:cx","http://interedition.eu/collatex/ns/1.0")
    root.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0")
    d.appendChild(root)
    for column in table.columns:
        value_dict = defaultdict(list)
        for key, value in sorted(column.tokens_per_witness.items()):
            # key is reading, value is list of witnesses
            value_dict["".join(str(item.token_data["t"]) for item in value)].append(key)

        # REVIEW [RHD]: Isn't there a method on table that can be used instead of this len(next(iter() etc?
        # otherwise I think there should be. Not sure what len(next(iter(etc))) represents.
        #
        # See https://stackoverflow.com/questions/4002874/non-destructive-version-of-pop-for-a-dictionary
        # It returns the number of witnesses that attest the one reading in the dictionary, that is, it peeks
        #   nondestructively at the value of the single dictionary item, which is a list, and counts the members
        #   of the list
        if len(value_dict) == 1 and len(next(iter(value_dict.values()))) == len(table.rows):
            # len(table.rows) is total number of witnesses; guards against nulls, which aren't in table
            key, value = value_dict.popitem() # there's just one item
            text_node = d.createTextNode(key)
            root.appendChild(text_node)
        else:
            # variation is either more than one reading, or one reading plus nulls
            ws_flag = False # add space after <app> if any <rdg> ends in whitespace
            app = d.createElementNS("http://www.tei-c.org/ns/1.0", "app")
            root.appendChild(app)
            for key,value in value_dict.items():
                # key is reading, value is list of witnesses
                rdg = d.createElementNS("http://www.tei-c.org/ns/1.0", "rdg")
                rdg.setAttribute("wit", " ".join(["#" + item for item in value_dict[key]]))
                if ws_flag == False and key.endswith((" ", r"\u0009", r"\u000a")): # space, tab, linefeed
                    ws_flag = True
                text_node = d.createTextNode(key.strip())
                rdg.appendChild(text_node)
                app.appendChild(rdg)
            if ws_flag:
                text_node = d.createTextNode(" ")
                root.appendChild(text_node)
    if indent:
        result = d.toprettyxml()
    else:
        result = d.toxml()
    return result
