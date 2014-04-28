'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
#using RangeSet from ClusterShell project (install it first with pip)
from ClusterShell.RangeSet import RangeSet
from collatex_core import Witness, VariantGraph, CollationAlgorithm, Tokenizer
from linsuffarr import SuffixArray
from operator import itemgetter, methodcaller


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
        return "wowie a block: "+str(self.ranges)
    
    def is_in_range(self, position):
        return position in self.ranges
    
# Class represents a range within one witness that is associated with a block
class Occurrence(object):

    def __init__(self, token_range, block):
        self.token_range = token_range
        self.block = block
    
    def __repr__(self):
        return str(self.token_range)
    
    def lower_end(self):
        return self.token_range[0]
    
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
    
  

'''
Suffix specific implementation of Collation object
'''
class Collation(object):
    witnesses = []
    counter = 0
    witness_ranges = {}
    combined_string = ""
    
    # the tokenization process happens multiple times
    # and by different tokenizers. This should be fixed
    def add_witness(self, sigil, content):
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
        
    def collate(self):
        self.graph = VariantGraph() 
        return self.graph

    def get_range_for_witness(self, witness_sigil):
        if not self.witness_ranges.has_key(witness_sigil):
            raise Exception("Witness "+witness_sigil+" is not added to the collation!")
        return self.witness_ranges[witness_sigil]
    
    def get_combined_string(self):
        return self.combined_string

    def get_sa(self):
        return SuffixArray(self.combined_string)

    def get_lcp_array(self):
        sa = self.get_sa()
        return sa._LCP_values
    
    # Note: LCP intervals can overlap.. for now we solve this with a two pass algorithm
    def get_lcp_intervals(self):
        lcp = self.get_lcp_array()
        temp_lcp_intervals = []
        # first detect the intervals based on zero's
        start_position = 0
        previous_prefix = 0
        for index, prefix in enumerate(lcp):
            if prefix == 0 and previous_prefix == 0:
                start_position = index
            if prefix == 0 and not previous_prefix == 0:
                # first end last interval
                temp_lcp_intervals.append((start_position, index-1))
                # create new interval
                start_position = index 
            previous_prefix = prefix
        # add the final interval
        #TODO: this one can be empty!
        temp_lcp_intervals.append((start_position, len(lcp)-1))    
        # step 2
        lcp_intervals = list(temp_lcp_intervals) 
        for start_position, end_position in temp_lcp_intervals:
            previous_prefix = 0
            created_new = False
            for index in range(start_position, end_position):
                prefix = lcp[index]
                if prefix < previous_prefix:
                    # first end last interval
                    lcp_intervals.append((start_position, index-1))
                    # create new interval
                    start_position = index
                    created_new=True 
                previous_prefix = prefix
            # add the final interval
            #TODO: this one can be empty!
            if created_new:
                lcp_intervals.append((start_position, len(lcp)-1))        
        return lcp_intervals

    #TODO: use lcp_intervals here
    def get_non_overlapping_repeating_blocks(self):
        SA = self.get_sa().SA
        LCP = self.get_lcp_array()
        tokenizer = Tokenizer()
        tokens = tokenizer.tokenize(self.get_combined_string()) 
#         print(SA)
#         print(LCP)
        # step one find potential blocks
        potential_blocks = []
        prev_lcp_value = 0
        for idx, lcp in enumerate(LCP):
            if lcp > 0 and prev_lcp_value == 0:
                block_occurrences = [SA[idx-1], SA[idx]]
                block_length = lcp
                #this check is not nice! (could be move to after for)
                if idx == len(LCP)-1:
                    potential_blocks.append((block_length, block_occurrences, len(block_occurrences)))
            if not lcp == 0 and not prev_lcp_value == 0:
                block_occurrences.append(SA[idx])    
            if lcp == 0 and not prev_lcp_value == 0:
                potential_blocks.append((block_length, block_occurrences, len(block_occurrences)))
            prev_lcp_value = lcp
        # step two.. sort on depth (length of occurrences), length
        pbs = sorted(potential_blocks, key=itemgetter(2,0), reverse=True)
        # step three: select the definitive blocks
        occupied = RangeSet()
        real_blocks = []
#         # debug: list potential blocks (move to string method on Block class)                
#         for block_length, block_occurrences, depth in pbs:
#             print("block found", tokens[block_occurrences[0]:block_occurrences[0]+block_length])
#             for block in block_occurrences:
#                 print(block, block+block_length-1)
        # process tuples, depth first
        for block_length, block_occurrences, depth in pbs:
            # convert block occurrences into ranges
            potential_block_range=RangeSet()
            for occurrence in block_occurrences:
                potential_block_range.add_range(occurrence, occurrence+block_length)
            
            # calculate the difference with the already occupied ranges
            block_range=potential_block_range.difference(occupied)
            if block_range:
                occupied = occupied.union(block_range)
                real_blocks.append((Block(block_range), block_length, block_occurrences))
            
#         # debug: list final blocks (move to string method on Block class)                
#         print("Final blocks!")
#         for block, block_length, block_occurrences in real_blocks:
#             print("block found", tokens[block_occurrences[0]:block_occurrences[0]+block_length])
#             for block in block_occurrences:
#                 print(block, block+block_length-1)
        
        result = []
        for block, block_length, block_occurrences in real_blocks:
            result.append(block)
        return result

    
    def get_first_block_witness(self):
        # prepare the witnesses -> convert witnesses into block witnesses
        sigil_first_witness = self.witnesses[0].sigil
        range_first_witness = self.get_range_for_witness(sigil_first_witness)
        blocks = self.get_non_overlapping_repeating_blocks()
        # make a selection of blocks and occurrences of these blocks in the first witness
        occurrences = []
        for block in blocks:
            block_ranges_in_witness = block.ranges & range_first_witness
            # note this are multiple ranges
            # we need to iterate over every single one
            for block_range in block_ranges_in_witness.contiguous():
                occurrence = Occurrence(block_range, block)
                occurrences.append(occurrence)
        # sort occurrences on position
        sorted_o = sorted(occurrences, key=methodcaller('lower_end'))
        block_witness = BlockWitness(sorted_o, self.witnesses[0].tokens())
        return block_witness
    
    

# not used
# external suffix library is used    
class Suffix(object):
    
    #generate suffixes from a list of tokens
    def gather_suffices(self, tokens):
        i = 0
        suffixes = []
        for t in tokens:
            suffixes.append(tokens[i:])
            i=i+1
        return suffixes
    

#TODO: check spelling!
class DekkerSuffixAlgorithmn(CollationAlgorithm):
    

    def build_block_to_vertices(self, collation, tokens, token_to_vertex):
        block_to_vertices = {}
        token_counter = 0
        blocks = collation.get_non_overlapping_repeating_blocks()
        # note: this can be done faster by focusing on the blocks
        # instead of the tokens
        for token in tokens:
            for block in blocks:
                if block.is_in_range(token_counter):
                    vertex = token_to_vertex[token]
                    if block_to_vertices.has_key(block):
                        existing_vertices = block_to_vertices[block]
                        existing_vertices.append(vertex)
                    else:
                        block_to_vertices[block] = [vertex]
            
            token_counter += 1
        #print(block_to_vertices)
        return block_to_vertices
        
    def build_block_to_tokens(self, collation, tokens):
        block_to_tokens = {}
        #TODO: witness hardcoded!
        witness_range = collation.get_range_for_witness(collation.witnesses[1].sigil)
        token_counter = witness_range[0]
        blocks = collation.get_non_overlapping_repeating_blocks()
        # note: this can be done faster by focusing on the blocks
        # instead of the tokens
        for token in tokens:
            for block in blocks:
                if block.is_in_range(token_counter):
                    if block_to_tokens.has_key(block):
                        existing_tokens = block_to_tokens[block]
                        existing_tokens.append(token)
                    else:
                        block_to_tokens[block] = [token]
            
            token_counter += 1
        #print(block_to_tokens)
        return block_to_tokens


    def get_alignment(self, block_to_vertices, block_to_tokens):
        alignment = {}
        for block in block_to_tokens:
            tokens = block_to_tokens[block]
            vertices = block_to_vertices[block]
            for token, vertex in zip(tokens, vertices):
                alignment[token]=vertex
        return alignment        
    
    
    def buildVariantGraphFromBlocks(self, graph, collation):
        '''
        :type graph: VariantGraph
        :type collation: Collation
        '''
        # step 1: Build the variant graph for the first witness
        # this is easy: generate a vertex for every token
        first_witness = collation.witnesses[0]
        tokens = first_witness.tokens()
        token_to_vertex = self.merge(graph, tokens)
        # step 2: Build the initial block to list vertex map 
        block_to_vertices = self.build_block_to_vertices(collation, tokens, token_to_vertex)    
        # step 3: Build the block to tokens map for the second witness
        second_witness = collation.witnesses[1]
        tokens = second_witness.tokens()
        block_to_tokens = self.build_block_to_tokens(collation, tokens)
        
        # step 4: Generate token to vertex alignment map for second 
        # witness, based on block to vertices map
        alignment = self.get_alignment(block_to_vertices, block_to_tokens)
        #print(alignment)
        self.merge(graph, tokens, alignment)

        pass



