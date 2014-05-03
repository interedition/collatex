'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
#using RangeSet from ClusterShell project (install it first with pip)
from ClusterShell.RangeSet import RangeSet
from collatex_core import Witness, VariantGraph, Tokenizer
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
    
# Class represents a range within one witness that is associated with a block
class Occurrence(object):

    def __init__(self, token_range, block):
        self.token_range = token_range
        self.block = block
    
    def __repr__(self):
        return str(self.token_range)
    
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
        parent_lcp_intervals = []
        # first detect the intervals based on zero's
        start_position = 0
        previous_prefix = 0
        for index, prefix in enumerate(lcp):
            if prefix == 0 and previous_prefix == 0:
                start_position = index
            if prefix == 0 and not previous_prefix == 0:
                # first end last interval
                parent_lcp_intervals.append((start_position, index-1))
                # create new interval
                start_position = index 
            previous_prefix = prefix
        # add the final interval
        #TODO: this one can be empty!
        parent_lcp_intervals.append((start_position, len(lcp)-1))    
        # step 2
        child_lcp_intervals = {} 
        for start_position, end_position in parent_lcp_intervals:
            parent_start_position = start_position
            previous_prefix = 0
            created_new = False
            for index in range(start_position, end_position):
                prefix = lcp[index]
                if prefix < previous_prefix:
                    # first end last interval
                    child_lcp_intervals.setdefault(parent_start_position, []).append((start_position, index-1))
                    # design decision --> sub intervals are also present in parent list!
                    parent_lcp_intervals.append((start_position, index-1))
                    # create new interval
                    start_position = index
                    created_new=True 
                previous_prefix = prefix
            # add the final interval
            #TODO: this one can be empty!
            if created_new:
                #TODO: add test for test (= only integration tested at the moment)
                child_lcp_intervals[parent_start_position].append((start_position, end_position))
                # design decision --> sub intervals are also present in parent list!
                parent_lcp_intervals.append((start_position, end_position))        
        return parent_lcp_intervals, child_lcp_intervals

    def add_potential_block_to_real_blocks(self, block_length, block_occurrences, occupied, real_blocks):
        # convert block occurrences into ranges
        potential_block_range = RangeSet()
        for occurrence in block_occurrences:
            potential_block_range.add_range(occurrence, occurrence + block_length)
        
        # calculate the difference with the already occupied ranges
        block_range = potential_block_range.difference(occupied)
        if block_range:
            occupied.union_update(block_range)
            real_blocks.append((Block(block_range), block_length, block_occurrences))

    def get_non_overlapping_repeating_blocks(self):
        SA = self.get_sa().SA
        lcp = self.get_lcp_array()
        lcp_intervals, lcp_sub_intervals = self.get_lcp_intervals()
        
        # step 1: process LCP array
        potential_blocks = []
        for start, end in lcp_intervals:
            number_of_occurrences = end - start +1
            block_length = lcp[start+1]
            block_occurrences = []
            for idx in range(start, end+1):
                block_occurrences.append(SA[idx])
            potential_blocks.append((number_of_occurrences, block_length, block_occurrences, start, end)) 
        # step 2: sort the blocks based on depth (number of repetitions) first,
        # second length of LCP interval
        sorted_blocks_on_priority = sorted(potential_blocks, key=itemgetter(0, 1), reverse=True)
        # step 3: select the definitive blocks
        occupied = RangeSet()
        real_blocks = []
        for number_of_occurrences, block_length, block_occurrences, start, end in sorted_blocks_on_priority:
#             print("looking at", number_of_occurrences, block_length, block_occurrences)
#             print(lcp[start: end+1])
            sub_intervals = lcp_sub_intervals.get(start, None)
            if sub_intervals:
#                 print(sub_intervals)
                for start, end in sub_intervals:
                    number_of_occurrences = end - start +1
                    block_length = lcp[start+1]
                    block_occurrences = []
                    for idx in range(start, end+1):
                        block_occurrences.append(SA[idx])
                    self.add_potential_block_to_real_blocks(block_length, block_occurrences, occupied, real_blocks)
            else:
                self.add_potential_block_to_real_blocks(block_length, block_occurrences, occupied, real_blocks)
             
#         # debug: list final blocks (move to string method on Block class)
#         tokenizer = Tokenizer()
#         tokens = tokenizer.tokenize(self.get_combined_string()) 
#         #TODO: block_length is calculated wrong here!                
#         print("Final blocks!")
#         for block, block_length, block_occurrences in real_blocks:
#             print("block found", tokens[block_occurrences[0]:block_occurrences[0]+block_length])
#             for block in block_occurrences:
#                 print(block, block+block_length-1)
        
        result = []
        for block, block_length, block_occurrences in real_blocks:
            result.append(block)
        return result

    def get_block_witness(self, witness):
        sigil_witness = witness.sigil
        range_witness = self.get_range_for_witness(sigil_witness)
        #TODO: block calculation is repeated here!
        blocks = self.get_non_overlapping_repeating_blocks() 
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
        sorted_o = sorted(occurrences, key=methodcaller('lower_end'))
        #TODO: complete set of witnesses is retokenized here!
        tokenizer = Tokenizer()
        tokens = tokenizer.tokenize(self.get_combined_string())
        block_witness = BlockWitness(sorted_o, tokens)
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
    




