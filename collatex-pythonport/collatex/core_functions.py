'''
Created on May 3, 2014

@author: Ronald Haentjens Dekker
'''
from collatex.core_classes import VariantGraph, Witness, join, AlignmentTable, Row, WordPunctuationTokenizer
from collatex.collatex_suffix import ExtendedSuffixArray
from collatex.linsuffarr import SuffixArray, UNIT_BYTE
from ClusterShell.RangeSet import RangeSet
import json
from collatex.edit_graph_aligner import EditGraphAligner

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
    table = AlignmentTable(collation, graph)
    if output == "json":
        return export_alignment_table_as_json(table)
    if output == "table":
        return table
    else:
        raise Exception("Unknown output type: "+output)
    


#TODO: this only works with a table output at the moment
#TODO: store the tokens on the graph instead
def collate_pretokenized_json(json, output="table", layout="horizontal", segmentation=False):
    witnesses = json["witnesses"]
    normalized_witnesses = []
    tokenized_witnesses = []
    for witness in witnesses:
        normalized_tokens = []
        tokenized_witness = []
        sigil = witness["id"]
        for token in witness["tokens"]:
            tokenized_witness.append(token)
            if "n" in token:
                normalized_tokens.append(token["n"])
            else:
                normalized_tokens.append(token["t"])
            pass
        normalized_witnesses.append(Witness(sigil, " ".join(normalized_tokens)))
        tokenized_witnesses.append(tokenized_witness)
    collation = Collation()
    for normalized_witness in normalized_witnesses:
        collation.add_witness(normalized_witness.sigil, normalized_witness.content)
    #TODO: change!
    at = collate(collation, output="novisualization", segmentation=segmentation)
    tokenized_at = AlignmentTable(collation)
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
        return tokenized_at
#         # transform JSON objects to "t" form.
#        TODO: REENABLE!
#         for row in tokenized_at.rows:
#             row.cells = [cell["t"]  for cell in row.cells]
#         # create visualization of alignment table
#         if layout == "vertical":    
#             prettytable = visualizeTableVertically(tokenized_at)
#         else:
#             prettytable = visualizeTableHorizontal(tokenized_at)
#         if in_ipython():
#             html = prettytable.get_html_string(formatting=True)
#             return display(HTML(html))
#         return prettytable

def export_alignment_table_as_json(table, indent=None):
    json_output = {}
    json_output["table"]=[]
    sigli = []
    variant_status = []
    for column in table.columns:
        variant_status.append(column.variant)
    for row in table.rows:
        sigli.append(row.header)
        json_output["table"].append([[cell] for cell in row.cells])
    json_output["witnesses"]=sigli
    json_output["status"]=variant_status
    return json.dumps(json_output, indent=indent)

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
            collation.add_witness(witness["id"], witness["content"])
        return collation

    @classmethod
    # json_data can be a string or a file
    def create_from_json(cls, json_data):
        data = json.load(json_data)
        collation = cls.create_from_dict(data)
        return collation

    def __init__(self):
        self.witnesses = []
        self.counter = 0
        self.witness_ranges = {}
        self.combined_string = ""
        self.cached_suffix_array = None

    # the tokenization process happens multiple times
    # and by different tokenizers. This should be fixed
    def add_witness(self, sigil, content):
        # clear the suffix array and LCP array cache
        self.cached_suffix_array = None
        witness = Witness(sigil, content)
        self.witnesses.append(witness)
        witness_range = RangeSet()
        witness_range.add_range(self.counter, self.counter+len(witness.tokens()))
        # the extra one is for the marker token
        self.counter += len(witness.tokens()) +2 # $ + number 
        self.witness_ranges[sigil] = witness_range
        if not self.combined_string == "":
            self.combined_string += " $"+str(len(self.witnesses)-1)+ " "
        self.combined_string += content

    def get_range_for_witness(self, witness_sigil):
        if not witness_sigil in self.witness_ranges:
            raise Exception("Witness "+witness_sigil+" is not added to the collation!")
        return self.witness_ranges[witness_sigil]

    def get_combined_string(self):
        return self.combined_string

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
        #print("COLLATION TOKENIZE IS CALLED!")
        #TODO: complete set of witnesses is retokenized here!
        tokenizer = WordPunctuationTokenizer()
        tokens = tokenizer.tokenize(self.get_combined_string())
        return tokens



