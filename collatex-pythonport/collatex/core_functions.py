"""
Created on May 3, 2014

@author: Ronald Haentjens Dekker
"""
from collatex.core_classes import VariantGraph, Witness, join, AlignmentTable, Row, WordPunctuationTokenizer, \
    VariantGraphRanking
from collatex.extended_suffix_array import ExtendedSuffixArray
from collatex.exceptions import UnsupportedError
from collatex.experimental_astar_aligner import ExperimentalAstarAligner
from collatex.linsuffarr import SuffixArray, UNIT_BYTE
from ClusterShell.RangeSet import RangeSet
import json
from collatex.edit_graph_aligner import EditGraphAligner
from collatex.display_module import display_alignment_table_as_HTML, visualizeTableVerticallyWithColors
from collatex.display_module import display_variant_graph_as_SVG
from collatex.near_matching import process_rank


# Valid options for output are:
# "table" for the alignment table (default)
# "graph" for the variant graph
# "json" for the alignment table exported as JSON
def collate(collation, output="table", layout="horizontal", segmentation=True, near_match=False, astar=False, detect_transpositions=False, debug_scores=False, properties_filter=None, svg_output=None):
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
            raise Exception("segmentation=True not supported for near matching; must be set to False")

        highestRank = ranking.byVertex[graph.end]
        witnessCount = len(collation.witnesses)

        # do-while loop to avoid looping through ranking while modifying it
        rank = highestRank - 1
        condition = True
        while condition:
            rank = process_rank(rank, collation, ranking, witnessCount)
            rank -= 1
            condition = rank > 1

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
        return display_variant_graph_as_SVG(graph,svg_output)
    if output=="graph": 
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
    else:
        raise Exception("Unknown output type: "+output)
    
def export_alignment_table_as_json(table, indent=None, status=False):
    json_output = {}
    json_output["table"]=[]
    sigli = []
    for row in table.rows:
        sigli.append(row.header)
        json_output["table"].append([[listItem.token_data for listItem in cell] if cell else None for cell in row.cells])
    json_output["witnesses"] = sigli
    if status:
        variant_status = []
        for column in table.columns:
            variant_status.append(column.variant)
        json_output["status"] = variant_status
    return json.dumps(json_output, sort_keys=True, indent=indent,ensure_ascii=False)
