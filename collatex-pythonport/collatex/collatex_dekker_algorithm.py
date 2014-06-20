'''
Created on May 3, 2014

@author: Ronald Haentjens Dekker
'''
from collatex.collatex_core import CollationAlgorithm, VariantGraphRanking,\
    VariantGraph, Tokenizer, Witness, join, AlignmentTable, Row
from operator import attrgetter
from collatex.collatex_suffix import PartialOverlapException,\
    ExtendedSuffixArray
from collatex.linsuffarr import SuffixArray
from ClusterShell.RangeSet import RangeSet
from prettytable import PrettyTable
from textwrap import fill
import json

# optionally load the IPython dependencies
try:
    from IPython.display import HTML
    from IPython.display import SVG
    from IPython.core.display import display
except:
    pass
    
def in_ipython():
    try:
        get_ipython().config  # @UndefinedVariable
#         print('Called by IPython.')
        return True
    except:
        return False

#TODO: this only works with a table output at the moment
#TODO: store the tokens on the graph instead
def collate_pretokenized_json(json, output="table", layout="horizontal"):
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
    at = collate(collation, output="novisualization")
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
        return display_alignment_table_as_json(tokenized_at)
    if output=="table":
        # transform JSON objects to "t" form.
        for row in tokenized_at.rows:
            row.cells = [cell["t"]  for cell in row.cells]
        # create visualization of alignment table
        if layout == "vertical":    
            prettytable = visualizeTableVertically(tokenized_at)
        else:
            prettytable = visualizeTableHorizontal(tokenized_at)
        if in_ipython():
            html = prettytable.get_html_string(formatting=True)
            return display(HTML(html))
        return prettytable
    
# Valid options for output are "table" (default)
# "graph" for the variant graph rendered as SVG
# "json" for the alignment table rendered as JSON
# "novisualization" to get the plain AlignmentTable object without any rendering         
def collate(collation, output="table", layout="horizontal", segmentation=True):
    algorithm = DekkerSuffixAlgorithm(collation)
    # build graph
    graph = VariantGraph()
    algorithm.build_variant_graph_from_blocks(graph, collation)
    # join parallel segments
    if segmentation:
        join(graph)
    # check which output format is requested: graph or table
    if output=="graph" and in_ipython:
        # visualize the variant graph into SVG format
        from networkx.drawing.nx_agraph import to_agraph
        agraph = to_agraph(graph.graph)
        svg = agraph.draw(format="svg", prog="dot", args="-Grankdir=LR -Gid=VariantGraph")
        return display(SVG(svg)) 
    # create alignment table
    table = AlignmentTable(collation, graph)
    if output == "json":
        return display_alignment_table_as_json(table)
    if output == "novisualization":
        return table
    # create visualization of alignment table
    if layout == "vertical":    
        prettytable = visualizeTableVertically(table)
    else:
        prettytable = visualizeTableHorizontal(table)
    if in_ipython():
        html = prettytable.get_html_string(formatting=True)
        return display(HTML(html))
    return prettytable

def display_alignment_table_as_json(table):
    json = alignmentTableToJSON(table)
#     if in_ipython():
#         return display(JSON(json))
    if in_ipython():
        print(json)
        return
    return json    
        
def visualizeTableHorizontal(table):
    # print the table horizontal
    x = PrettyTable()
    x.header=False
    for row in table.rows:
        cells = [row.header]
        cells.extend(row.cells)
        x.add_row(cells)
    # alignment can only be set after the field names are known.
    # since add_row sets the field names, it has to be set after x.add_row(cells)
    x.align="l"
    return x

def visualizeTableVertically(table):
    # print the table vertically
    x = PrettyTable()
    x.hrules = 1
    for row in table.rows:
        x.add_column(row.header, [fill(cell, 20) for cell in row.cells])
    return x

def alignmentTableToJSON(table):
    json_output = {}
    json_output["table"]=[]
    sigli = []
    for row in table.rows:
        sigli.append(row.header)
        json_output["table"].append([[cell] for cell in row.cells])
    json_output["witnesses"]=sigli
    return json.dumps(json_output, indent=2)

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
        self.counter += len(witness.tokens()) +1 
        self.witness_ranges[sigil] = witness_range
        if not self.combined_string == "":
            self.combined_string += " $"+str(len(self.witnesses)-1)+ " "
        self.combined_string += content
        
    def get_range_for_witness(self, witness_sigil):
        if not self.witness_ranges.has_key(witness_sigil):
            raise Exception("Witness "+witness_sigil+" is not added to the collation!")
        return self.witness_ranges[witness_sigil]
    
    def get_combined_string(self):
        return self.combined_string

    def get_sa(self):
        #NOTE: implemented in a lazy manner, since calculation of the Suffix Array and LCP Array takes time
        if not self.cached_suffix_array:
            self.cached_suffix_array = SuffixArray(self.combined_string)
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
        #TODO: complete set of witnesses is retokenized here!
        tokenizer = Tokenizer()
        tokens = tokenizer.tokenize(self.get_combined_string())
        return tokens
    

class Block(object):
    
    def __init__(self, ranges):
        """
        :type ranges: RangeSet
        """
        self.ranges = ranges
        
    def __hash__(self):
        return hash(self.ranges.__str__())
    
    def __eq__(self, other):
        if type(other) is type(self):
            return self.__dict__ == other.__dict__
        return False
    
    def __str__(self):
        return "Block with occurrences "+str(self.ranges)
    
    def __repr__(self):
        return "Block: "+str(self.ranges)
    
# Class represents a range within one witness that is associated with a block
class Occurrence(object):

    def __init__(self, token_range, block):
        self.token_range = token_range
        self.block = block
    
    def __repr__(self):
        return str(self.token_range)
    
    @property
    def lower_end(self):
        return self.token_range[0]
    
    def is_in_range(self, position):
        return position in self.token_range
    
# Class represents a witness which consists of occurrences of blocks            
class BlockWitness(object):
    
    def __init__(self, occurrences, tokens):
        self.occurrences = occurrences
        self.tokens = tokens
        
    def debug(self):
        result = []
        for occurrence in self.occurrences:
            result.append(' '.join(self.tokens[occurrence.token_range.slices().next()]))
        return result
    

class DekkerSuffixAlgorithm(CollationAlgorithm):
    def __init__(self, collation):
        self.blocks = None
        self.collation = collation
    
    def get_block_witness(self, witness):
        sigil_witness = witness.sigil
        range_witness = self.collation.get_range_for_witness(sigil_witness)
        #NOTE: to prevent recalculation of blocks
        if not self.blocks:
            self.blocks = self.get_non_overlapping_repeating_blocks() 
        blocks = self.blocks 
        # make a selection of blocks and occurrences of these blocks in the selected witness
        occurrences = []
        for block in blocks:
            block_ranges_in_witness = block.ranges & range_witness
            # note this are multiple ranges
            # we need to iterate over every single one
            for block_range in block_ranges_in_witness.contiguous():
                occurrence = Occurrence(block_range, block)
                occurrences.append(occurrence) 
        # sort occurrences on position
        sorted_o = sorted(occurrences, key=attrgetter('lower_end'))
        block_witness = BlockWitness(sorted_o, self.collation.tokens)
        return block_witness

    def get_non_overlapping_repeating_blocks(self):
        extended_suffix_array = self.collation.to_extended_suffix_array()
        potential_blocks = extended_suffix_array.split_lcp_array_into_intervals() 
        self.filter_potential_blocks(potential_blocks)
        # step 3: sort the blocks based on depth (number of repetitions) first,
        # second length of LCP interval,
        # third sort on parent LCP interval occurrences.
        sorted_blocks_on_priority = sorted(potential_blocks, key=attrgetter("number_of_occurrences", "minimum_block_length", "number_of_siblings"), reverse=True)
        # step 4: select the definitive blocks
        occupied = RangeSet()
        real_blocks = []
        for potential_block in sorted_blocks_on_priority:
#           print(potential_block.info())
            try:
                non_overlapping_range = potential_block.calculate_non_overlapping_range_with(occupied)
                if non_overlapping_range:
#                     print("Selecting: "+str(potential_block))
                    occupied.union_update(non_overlapping_range)
                    real_blocks.append(Block(non_overlapping_range))
            except PartialOverlapException:          
#                 print("Skip due to conflict: "+str(potential_block))
                while potential_block.minimum_block_length > 1:
                    # retry with a different length: one less
                    for idx in range(potential_block.start+1, potential_block.end+1):
                        potential_block.LCP[idx] -= 1
                    potential_block.length -= 1
                    try:
                        non_overlapping_range = potential_block.calculate_non_overlapping_range_with(occupied)
                        if non_overlapping_range:
#                             print("Retried and selecting: "+str(potential_block))
                            occupied.union_update(non_overlapping_range)
                            real_blocks.append(Block(non_overlapping_range))
                            break
                    except PartialOverlapException:          
#                         print("Retried and failed again")
                        pass
        return real_blocks


        # filter out all the blocks that have more than one occurrence within a witness
    def filter_potential_blocks(self, potential_blocks):
        for potential_block in potential_blocks[:]:
            for witness in self.collation.witnesses:
                witness_sigil = witness.sigil
                witness_range = self.collation.get_range_for_witness(witness_sigil)
                inter = witness_range.intersection(potential_block.block_occurrences())
                if potential_block.number_of_occurrences > len(self.collation.witnesses) or len(inter)> potential_block.minimum_block_length:
#                     print("Removing block: "+str(potential_block))
                    potential_blocks.remove(potential_block)
                    break

    def build_variant_graph_from_blocks(self, graph, collation):
        '''
        :type graph: VariantGraph
        :type collation: Collation
        '''
        # step 1: Build the variant graph for the first witness
        # this is easy: generate a vertex for every token
        first_witness = collation.witnesses[0]
        tokens = first_witness.tokens()
        token_to_vertex = self.merge(graph, first_witness.sigil, tokens)
        # step 2: Build the initial occurrence to list vertex map 
        graph_occurrence_to_vertices = {}
        self._build_occurrences_to_vertices(collation, first_witness, token_to_vertex, [], graph_occurrence_to_vertices) 
        
        # align witness 2 - n
        for x in range(1, len(collation.witnesses)):
            # step 3: Build the occurrence to tokens map for the next witness
            next_witness = collation.witnesses[x]
            block_witness = self.get_block_witness(next_witness)
            witness_occurrence_to_tokens = self._build_occurrences_to_tokens(collation, next_witness, block_witness)
            # step 4: align and merge next witness
            alignment = self._align(graph_occurrence_to_vertices, witness_occurrence_to_tokens, block_witness)
            # determine phrase matches
            phrase_match_detector = PhraseMatchDetector()
            phrasematches = phrase_match_detector.detect(alignment, graph, next_witness.tokens())
            #print(phrasematches)
            # transposition detector
            transposition_detector = TranspositionDetector()
            transpositions = transposition_detector.detect(phrasematches, graph)
#             if transpositions:
#                 print(transpositions)
            # transposed tokens can not be aligned
            transposed_tokens = []
            for transposition in transpositions:
                for (_, token) in transposition:
                    del alignment[token]
                    transposed_tokens.append(token)
            # merge
            token_to_vertex = self.merge(graph, next_witness.sigil, next_witness.tokens(), alignment)
            # step 5: update the occurrences to vertex map with the new vertices created for the second witness
            self._build_occurrences_to_vertices(collation, next_witness, token_to_vertex, transposed_tokens, graph_occurrence_to_vertices)    
        
    #===========================================================================
    # graph block to occurrences: every block that is present in the graph mapped to
    # its occurrences
    # graph occurrence to vertices: maps every graph occurrence of a block to a list
    # of vertices 
    # block_witness: a witness represented as a list of occurrences of blocks
    #===========================================================================
    def _align(self, graph_occurrence_to_vertices, witness_occurrence_to_tokens, block_witness):
        # map graph occurrences to their block
        graph_block_to_occurrences = {}
        for graph_occurrence in graph_occurrence_to_vertices:
            block = graph_occurrence.block
            graph_block_to_occurrences.setdefault(block, []).append(graph_occurrence)
        # generate the witness block to occurrence map to check whether some blocks occur multiple times
        # in the witness
        witness_block_to_occurrence={}
        for witness_occurrence in block_witness.occurrences:
            witness_block = witness_occurrence.block
            witness_block_to_occurrence.setdefault(witness_block, []).append(witness_occurrence)
        # Generate token to vertex alignment map for second 
        # witness, based on block to vertices map
        alignment = {}
        for witness_occurrence in block_witness.occurrences:
            witness_block = witness_occurrence.block
            #NOTE: the witness_block could also not be present
            if not witness_block in graph_block_to_occurrences:
#                 print(str(witness_block)+" missing in graph!")
                continue
            # check number of occurrences of block in witness
            # if larger than 1 we have to make a decision
            witness_occurrences = witness_block_to_occurrence[witness_block]
            if len(witness_occurrences)>1:
#                 print(str(witness_block)+" occurring multiple times in witness!")
                #TODO: we have to make a decision here!
                continue        
            # check number of occurrences of block in graph 
            # if larger than 1 we have to make a decision
            graph_occurrences = graph_block_to_occurrences[witness_block]
            if len(graph_occurrences)>1:
#                 print(str(witness_block)+" occurring multiple times in graph!")
                #TODO: we have to make a decision here!
                continue        
            graph_occurrence = graph_occurrences[0]
            tokens = witness_occurrence_to_tokens[witness_occurrence]
            vertices = graph_occurrence_to_vertices[graph_occurrence]
            for token, vertex in zip(tokens, vertices):
                alignment[token]=vertex
        return alignment
        
    def _build_occurrences_to_vertices(self, collation, witness, token_to_vertex, transposed_tokens, occurrence_to_vertices):
        witness_range = collation.get_range_for_witness(witness.sigil)
        token_counter = witness_range[0]
        block_witness = self.get_block_witness(witness)
        # note: this can be done faster by focusing on the occurrences
        # instead of the tokens
        for token in witness.tokens():
            for occurrence in block_witness.occurrences:
                if occurrence.is_in_range(token_counter) and not token in transposed_tokens and token in token_to_vertex:
                    vertex = token_to_vertex[token]
                    occurrence_to_vertices.setdefault(occurrence, []).append(vertex)
            token_counter += 1
        return occurrence_to_vertices

    def _build_occurrences_to_tokens(self, collation, witness, block_witness):
        occurrence_to_tokens = {}
        witness_range = collation.get_range_for_witness(witness.sigil)
        token_counter = witness_range[0]
        # note: this can be done faster by focusing on the occurrences
        # instead of the tokens
        for token in witness.tokens():
            for occurrence in block_witness.occurrences:
                if occurrence.is_in_range(token_counter):
                    occurrence_to_tokens.setdefault(occurrence, []).append(token)
            token_counter += 1
        return occurrence_to_tokens

    
#===========================================================================
# Direct port from Java code
#===========================================================================
class PhraseMatchDetector(object):
    def _add_new_phrase_match_and_clear_buffer(self, phrase_matches, base_phrase, witness_phrase):
        if base_phrase:
            phrase_matches.append(zip(base_phrase, witness_phrase)) 
            del base_phrase[:]
            del witness_phrase[:]

    def detect(self, linked_tokens, base, tokens):
        phrase_matches = []
        base_phrase = []
        witness_phrase = []
        previous = base.start
        
        for token in tokens:
            if not token in linked_tokens:
                self._add_new_phrase_match_and_clear_buffer(phrase_matches, base_phrase, witness_phrase)
                continue
            base_vertex = linked_tokens[token]
            # requirements:
            # - see comments in java class
            same_transpositions = True #TODO
            same_witnesses = True #TODO
            directed_edge = base.edge_between(previous, base_vertex)
            is_near = same_transpositions and same_witnesses and directed_edge and len(base.out_edges(previous))==1 and len(base.in_edges(base_vertex))==1
            if not is_near:
                self._add_new_phrase_match_and_clear_buffer(phrase_matches, base_phrase, witness_phrase)
            base_phrase.append(base_vertex)
            witness_phrase.append(token)
            previous = base_vertex
        if base_phrase:
            phrase_matches.append(zip(base_phrase, witness_phrase)) 
        return phrase_matches

#=================================================
# Almost fully direct port from Java code
#=================================================
class TranspositionDetector(object):
    def detect(self, phrasematches, base):
        if not phrasematches:
            return []
        
        ranking = self._rank_the_graph(phrasematches, base)
        
        def compare_phrasematches(pm1, pm2):
            (vertex1, _) = pm1[0]
            (vertex2, _) = pm2[0]
            rank1 = ranking.apply(vertex1)
            rank2 = ranking.apply(vertex2)
            difference = rank1 - rank2
            
            if difference != 0:
                return difference
            index1 = phrasematches.index(pm1)
            index2 = phrasematches.index(pm2)
            return index1 - index2
        
        phrasematches_graph_order = sorted(phrasematches, cmp=compare_phrasematches)
        
        # map 1
        self.phrasematch_to_index = {}
        for idx, val in enumerate(phrasematches_graph_order):
            self.phrasematch_to_index[val[0]]=idx
        
        # We calculate the index for all the phrase matches
        # First in witness order, then in graph order
        phrasematches_graph_index = range(0, len(phrasematches))
        
        phrasematches_witness_index = []
        for phrasematch in phrasematches:
            phrasematches_witness_index.append(self.phrasematch_to_index[phrasematch[0]])
        
        # initialize result variables
        non_transposed_phrasematches = list(phrasematches)
        transpositions = []
        
        # loop here until the maximum distance == 0
        while(True):
            # map 2
            phrasematch_to_distance = {}
            for i, phrasematch in enumerate(non_transposed_phrasematches):
                graph_index = phrasematches_graph_index[i]
                witness_index = phrasematches_witness_index[i]
                distance = abs(graph_index - witness_index)
                phrasematch_to_distance[phrasematch[0]]=distance
        
            distance_list = list(phrasematch_to_distance.values())
            
            if not distance_list or max(distance_list) == 0:
                break
            
            def comp2(pm1, pm2):
                # first order by distance
                distance1 = phrasematch_to_distance[pm1[0]]
                distance2 = phrasematch_to_distance[pm2[0]]
                difference = distance2 - distance1
                if difference != 0:
                    return difference
                
                # second order by size
                #TODO: this does not work for Greek texts with lots of small words!
                #TODO: it should determine which block this phrasematch is part of and
                #TODO: the number of occurrences for that block
                return len(pm1) - len(pm2)
                
            sorted_phrasematches = sorted(non_transposed_phrasematches, cmp = comp2) 
            transposedphrase = sorted_phrasematches[0]
            
            transposed_index = self.phrasematch_to_index[transposedphrase[0]]
            graph_index = phrasematches_graph_index.index(transposed_index)
            transposed_with_index = phrasematches_witness_index[graph_index]
            linked_transposed_phrase = phrasematches_graph_order[transposed_with_index]
            
            self._add_transposition(phrasematches_witness_index, phrasematches_graph_index, non_transposed_phrasematches, transpositions, transposedphrase)
            
            distance = phrasematch_to_distance[transposedphrase[0]]
            if distance == phrasematch_to_distance[linked_transposed_phrase[0]] and distance > 1:
                self._add_transposition(phrasematches_witness_index, phrasematches_graph_index, non_transposed_phrasematches, transpositions, linked_transposed_phrase)
    
        return transpositions
    
    def _add_transposition(self, phrasematches_witness_index, phrasematches_graph_index, non_transposed_phrasematches, transpositions, transposed_phrase):
        index_to_remove = self.phrasematch_to_index[transposed_phrase[0]]
        non_transposed_phrasematches.remove(transposed_phrase)
        transpositions.append(transposed_phrase)
        phrasematches_graph_index.remove(index_to_remove)
        phrasematches_witness_index.remove(index_to_remove)

    def _rank_the_graph(self, phrase_matches, base):
        #TODO: rank the graph based on only the first vertex of each of the phrasematches!
        return VariantGraphRanking.of(base)
