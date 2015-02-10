'''
Created on May 3, 2014

@author: Ronald Haentjens Dekker
'''
from collatex.core_classes import VariantGraph, Witness, join, AlignmentTable, Row, WordPunctuationTokenizer
from collatex.collatex_suffix import ExtendedSuffixArray
from collatex.exceptions import UnsupportedError
from collatex.linsuffarr import SuffixArray, UNIT_BYTE
from ClusterShell.RangeSet import RangeSet
import json
from collatex.edit_graph_aligner import EditGraphAligner
from collatex.display_module import display_alignment_table_as_HTML

# Valid options for output are:
# "table" for the alignment table (default)
# "graph" for the variant graph
# "json" for the alignment table exported as JSON
def collate(collation, output="table", layout="horizontal", segmentation=True, near_match=False, astar=False, debug_scores=False):
    algorithm = EditGraphAligner(collation, near_match=near_match, astar=astar, debug_scores=debug_scores)
    # build graph
    graph = VariantGraph()
    algorithm.collate(graph, collation)
    # join parallel segments
    if segmentation:
        join(graph)
    # check which output format is requested: graph or table
    if output=="graph": 
        return graph
    # create alignment table
    table = AlignmentTable(collation, graph, layout)
    if output == "json":
        return export_alignment_table_as_json(table)
    if output == "html":
        return display_alignment_table_as_HTML(table)
    if output == "table":
        return table
    else:
        raise Exception("Unknown output type: "+output)
    


#TODO: this only works with a table output at the moment
#TODO: store the tokens on the graph instead
def collate_pretokenized_json(json, output='table', layout='horizontal', **kwargs):
    # Takes more or less the same arguments as collate() above, but with some restrictions.
    # Only output types 'json' and 'table' are supported.
    if output not in ['json', 'table']:
        raise UnsupportedError("Output type" + kwargs['output'] + "not supported for pretokenized collation")
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
    tokenized_at = AlignmentTable(collation, layout=layout)
    for row, tokenized_witness in zip(at.rows, tokenized_witnesses):
        new_row = Row(row.header)
        tokenized_at.rows.append(new_row)
        token_counter = 0
        for cell in row.cells:
            if cell != "-":
                new_row.cells.append(tokenized_witness[token_counter])
                token_counter+=1
            else:
                #TODO: should probably be null or None instead, but that would break the rendering at the moment 
                new_row.cells.append({"t":"-"})
    if output=="json":
        return export_alignment_table_as_json(tokenized_at)
    if output=="table":
        # transform JSON objects to "t" form.
        for row in tokenized_at.rows:
            row.cells = [cell["t"]  for cell in row.cells]
        return tokenized_at

def export_alignment_table_as_json(table, indent=None, status=False):
    json_output = {}
    json_output["table"]=[]
    sigli = []
    for row in table.rows:
        sigli.append(row.header)
        json_output["table"].append([[cell] for cell in row.cells])
    json_output["witnesses"]=sigli
    if status:
        variant_status = []
        for column in table.columns:
            variant_status.append(column.variant)
        json_output["status"]=variant_status
    return json.dumps(json_output, sort_keys=True, indent=indent)

'''
Suffix specific implementation of Collation object
'''
class Collation(object):

    @classmethod
    def create_from_dict(cls, data, limit=None):
        witnesses = data["witnesses"]
        collation = Collation()
        for witness in witnesses[:limit]:
            # generate collation object from json_data
            collation.add_witness(witness)
        return collation

    # json input can be a string or a file
    @classmethod
    def create_from_json_string(cls, json_string):
        data = json.loads(json_string)
        collation = cls.create_from_dict(data)
        return collation
    
    @classmethod
    def create_from_json_file(cls, json_path):
        with open(json_path, 'r') as json_file:
            data = json.load(json_file)
        collation = cls.create_from_dict(data)
        return collation

    def __init__(self):
        self.witnesses = []
        self.counter = 0
        self.witness_ranges = {}
        self.cached_suffix_array = None

    def add_witness(self, witnessdata):
        # clear the suffix array and LCP array cache
        self.cached_suffix_array = None
        witness = Witness(witnessdata)
        self.witnesses.append(witness)
        witness_range = RangeSet()
        witness_range.add_range(self.counter, self.counter+len(witness.tokens()))
        # the extra one is for the marker token
        self.counter += len(witness.tokens()) +2 # $ + number 
        self.witness_ranges[witness.sigil] = witness_range

    def add_plain_witness(self, sigil, content):
        return self.add_witness({'id':sigil, 'content':content})

    def get_range_for_witness(self, witness_sigil):
        if not witness_sigil in self.witness_ranges:
            raise Exception("Witness "+witness_sigil+" is not added to the collation!")
        return self.witness_ranges[witness_sigil]

    def get_sa(self):
        #NOTE: implemented in a lazy manner, since calculation of the Suffix Array and LCP Array takes time
        if not self.cached_suffix_array:
            # Unit byte is done to skip tokenization in third party library
            self.cached_suffix_array = SuffixArray(self.tokens, unit=UNIT_BYTE)
        return self.cached_suffix_array

    def get_suffix_array(self):
        sa = self.get_sa()
        return sa.SA

    def get_lcp_array(self):
        sa = self.get_sa()
        return sa._LCP_values

    def to_extended_suffix_array(self):
        return ExtendedSuffixArray(self.tokens, self.get_suffix_array(), self.get_lcp_array())

    @property
    def tokens(self):
        tokens = []
        for i, witness in enumerate(self.witnesses):
            if i > 0 :
                tokens.append('$')
                tokens.append(str(i))
            for tk in witness.tokens():
                tokens.append(tk.token_string)
        return tokens



