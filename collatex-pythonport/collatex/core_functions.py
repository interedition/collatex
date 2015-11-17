"""
Created on May 3, 2014

@author: Ronald Haentjens Dekker
"""
from collatex.core_classes import VariantGraph, Witness, join, AlignmentTable, Row, WordPunctuationTokenizer
from collatex.extended_suffix_array import ExtendedSuffixArray
from collatex.exceptions import UnsupportedError
from collatex.experimental_astar_aligner import ExperimentalAstarAligner
from collatex.linsuffarr import SuffixArray, UNIT_BYTE
from ClusterShell.RangeSet import RangeSet
import json
from collatex.edit_graph_aligner import EditGraphAligner
from collatex.display_module import display_alignment_table_as_HTML, visualizeTableVerticallyWithColors
from collatex.display_module import display_variant_graph_as_SVG

# Valid options for output are:
# "table" for the alignment table (default)
# "graph" for the variant graph
# "json" for the alignment table exported as JSON
def collate(collation, output="table", layout="horizontal", segmentation=True, near_match=False, astar=False, detect_transpositions=False, debug_scores=False, properties_filter=None):
    if not astar:
        algorithm = EditGraphAligner(collation, near_match=near_match, detect_transpositions=detect_transpositions, debug_scores=debug_scores, properties_filter=properties_filter)
    else:
        algorithm = ExperimentalAstarAligner(collation, near_match=near_match, debug_scores=debug_scores)

    # build graph
    graph = VariantGraph()
    algorithm.collate(graph, collation)
    # join parallel segments
    if segmentation:
        join(graph)
    # check which output format is requested: graph or table
    if output == "svg":
        return display_variant_graph_as_SVG(graph)
    if output=="graph": 
        return graph
    # create alignment table
    table = AlignmentTable(collation, graph, layout)
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
    
# TODO: this only works with a table output at the moment
# TODO: store the tokens on the graph instead
def collate_pretokenized_json(json, output='table', layout='horizontal', **kwargs):
    # Takes more or less the same arguments as collate() above, but with some restrictions.
    # Only output types 'json' and 'table' are supported.
    if output not in ['json', 'table', 'html2']:
        raise UnsupportedError("Output type " + output + " not supported for pretokenized collation")
    if 'segmentation' in kwargs and kwargs['segmentation']:
        raise UnsupportedError("Segmented output not supported for pretokenized collation")
    kwargs['segmentation'] = False

    # For each witness given, make a 'shadow' witness based on the normalization tokens
    # that will actually be collated.
    tokenized_witnesses = []
    collation = Collation()
    for witness in json["witnesses"]:
        collation.add_witness(witness)
        tokenized_witnesses.append(witness["tokens"])
    at = collate(collation, output="table", **kwargs)
    if output == "html2":
        return visualizeTableVerticallyWithColors(at, collation)

    # record whether there is variation in each of the columns (horizontal) or rows (vertical layout)
    has_variation_array = []
    for column in at.columns:
        has_variation_array.append(column.variant)
    tokenized_at = AlignmentTable(collation, layout=layout)
    for row, tokenized_witness in zip(at.rows, tokenized_witnesses):
        new_row = Row(row.header)
        tokenized_at.rows.append(new_row)
        token_counter = 0
        for cell in row.cells:
            new_row.cells.append(tokenized_witness[token_counter] if cell else None)
            if cell:
                token_counter += 1
    # In order to have the same information as in the non pretokenized alignment table we
    # add variation information to the pretokenized alignment table.
    tokenized_at.has_rank_variation = has_variation_array
    if output == "json":
        return export_alignment_table_as_json(tokenized_at)
    if output == "table":
        # transform JSON objects to "t" form.
        for row in tokenized_at.rows:
            row.cells = [cell["t"] if cell else None for cell in row.cells]
        return tokenized_at

def export_alignment_table_as_json(table, indent=None, status=False):
    json_output = {}
    json_output["table"]=[]
    sigli = []
    for row in table.rows:
        sigli.append(row.header)
        json_output["table"].append([[cell] for cell in row.cells])
    json_output["witnesses"] = sigli
    if status:
        variant_status = []
        for column in table.columns:
            variant_status.append(column.variant)
        json_output["status"] = variant_status
    return json.dumps(json_output, sort_keys=True, indent=indent)

class Collation(object):

    @classmethod
    def create_from_dict(cls, data, limit=None):
        witnesses = data["witnesses"]
        collation = Collation()
        for witness in witnesses[:limit]:
            # generate collation object from json_data
            collation.add_witness(witness)
        return collation

    @classmethod
    # json_data can be a string or a file
    def create_from_json(cls, json_data):
        data = json.load(json_data)
        collation = cls.create_from_dict(data)
        return collation

    def __init__(self):
        self.witnesses = []

    def add_witness(self, witnessdata):
        witness = Witness(witnessdata)
        self.witnesses.append(witness)

    def add_plain_witness(self, sigil, content):
        return self.add_witness({'id':sigil, 'content':content})



