'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
#using RangeSet from ClusterShell project (install it first with pip)
from ClusterShell.RangeSet import RangeSet
from operator import attrgetter
from collatex.collatex_core import Witness, VariantGraph, Tokenizer
from collatex.linsuffarr import SuffixArray

class Stack(list):
    def push(self, item):
        self.append(item)
        
    def peek(self):
        return self[-1]
    
class PartialOverlapException(Exception):
    pass

class ExtendedSuffixArray(object):
    
    def __init__(self, tokens, sa_array, lcp_array):
        self.tokens = tokens
        self.SA = sa_array
        self.LCP = lcp_array

    # NOTE: LCP intervals can 1) ascend or 2) descend or 3) first ascend and then descend. 4) descend, ascend
    def split_lcp_array_into_intervals(self):
        closed_intervals = []
        previous_lcp_value = 0
        open_intervals = Stack()
        for idx in range(0, len(self.LCP)):
            lcp_value = self.LCP[idx]
#             print(lcp_value)
            if lcp_value > previous_lcp_value:
                open_intervals.push((idx-1, lcp_value))
                previous_lcp_value = lcp_value
            if lcp_value < previous_lcp_value:
                # close open intervals that are larger than current lcp_value
                while open_intervals and open_intervals.peek()[1] > lcp_value:
#                     print("Peek: "+str(open_intervals.peek()))
                    (start, length) = open_intervals.pop()
                    #TODO: FIX NUMBER OF SIBLINGS!
                    closed_intervals.append(LCPInterval(self.tokens, self.SA, self.LCP, start, idx-1, length, 0))
#                     print("new: "+repr(closed_intervals[-1]))
                # then: open a new interval starting with start filter open intervals.
                if lcp_value > 0:
                    start = closed_intervals[-1].start
                    open_intervals.push((start, lcp_value))
                previous_lcp_value = lcp_value
        # add all the open intervals to the result
#         print("Closing remaining:")
        for start, length in open_intervals:
            #TODO: FIX NUMBER OF SIBLINGS!
            closed_intervals.append(LCPInterval(self.tokens, self.SA, self.LCP, start, len(self.LCP)-1, length, 0))
#             print("new: "+repr(closed_intervals[-1]))
        return closed_intervals

    def list_prefixes(self):
        for idx in range(0, len(self.LCP)):
            if self.LCP[idx] > 0:
                prefix = " ".join(self.tokens[self.SA[idx]:self.SA[idx]+self.LCP[idx]])
                print(prefix)
            else:
                print("--------")

# parts of the LCP array become potential blocks.
# minimum block_length: the number of tokens a single occurrence of this block spans
# block_occurrences: the ranges within the suffix array that this block spans
class LCPInterval(object):
    
    def __init__(self, tokens, SA, LCP, start, end, length, number_of_siblings):
        self.tokens = tokens
        self.SA = SA
        self.LCP = LCP
        self.start = start
        self.end = end
        self.length = length
        self.number_of_siblings = number_of_siblings
        
    @property
    def minimum_block_length(self):
        return self.length
    
    @property
    def number_of_occurrences(self):
        return self.end - self.start + 1
    
    def block_occurrences(self):
        block_occurrences = []
        for idx in range(self.start, self.end + 1):
            block_occurrences.append(self.SA[idx])
        return block_occurrences

    def info(self):
        return "looking at: "+str(self)
    
    @property            
    def token_start_position(self):
        return min(self.block_occurrences())
    
    def calculate_non_overlapping_range_with(self, occupied):
        # convert block occurrences into ranges
        potential_block_range = RangeSet()
        for occurrence in self.block_occurrences():
            potential_block_range.add_range(occurrence, occurrence + self.minimum_block_length)
        #check the intersection with the already occupied ranges
        block_intersection = potential_block_range.intersection(occupied)
        if not block_intersection:
            # no overlap, return complete block_range
            return potential_block_range
        # There is overlap with occupied range
        # we need to deal with it
        real_block_range = RangeSet()
        for lower in potential_block_range.contiguous():
            # TODO: what I really want here is a find first over a generator
            upper = [x for x in block_intersection.contiguous() if x[0] >= lower[0]]
            if upper:
                lower = lower[0]
                upper = upper[0][0]
                if lower != upper:
                    real_block_range.add_range(lower, upper)
        if not real_block_range:
            # There is complete overlap, so return None
            return None
        # Assert: check that the first slice is not larger than potential block length!
        first_range = real_block_range.contiguous().next()
        if first_range[-1]-first_range[0]+1>self.minimum_block_length:
            raise PartialOverlapException()
        return real_block_range
    
    def show_lcp_array(self):
        return self.LCP[self.start:self.end+1]
        
    def __str__(self):
        part1= "<"+" ".join(self.tokens[self.SA[self.start]:self.SA[self.start]+min(10, self.minimum_block_length)])
        return part1+"> with "+str(self.number_of_occurrences)+" occurrences and length: "+str(self.minimum_block_length)+" and number of siblings: "+str(self.number_of_siblings)
 
    def __repr__(self):
        return "LCPivl: "+str(self.token_start_position)+","+str(self.minimum_block_length)+","+str(self.number_of_occurrences)
            
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
    
  

'''
Suffix specific implementation of Collation object
'''
class Collation(object):
    
    def __init__(self):
        self.witnesses = []
        self.counter = 0
        self.witness_ranges = {}
        self.combined_string = ""
        self.blocks = None
    
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
        #TODO: make this lazy!
        return SuffixArray(self.combined_string)

    def get_suffix_array(self):
        sa = self.get_sa()
        return sa.SA

    def get_lcp_array(self):
        sa = self.get_sa()
        return sa._LCP_values
    
    # filter out all the blocks that have more than one occurrence within a witness
    def filter_potential_blocks(self, potential_blocks):
        for potential_block in potential_blocks[:]:
            for witness in self.witnesses:
                witness_sigil = witness.sigil
                witness_range = self.get_range_for_witness(witness_sigil)
                inter = witness_range.intersection(potential_block.block_occurrences())
                if potential_block.number_of_occurrences > len(self.witnesses) or len(inter)> potential_block.minimum_block_length:
                    print("Removing block: "+str(potential_block))
                    potential_blocks.remove(potential_block)
#                     print("Before:")
#                     potential_block.list_prefixes()
#                     print("After:")
#                     split = potential_block.split_into_smaller_intervals()
#                     for interval in split:
#                         print(str(interval))
                    break

    def to_extended_suffix_array(self):
        return ExtendedSuffixArray(self.tokens, self.get_suffix_array(), self.get_lcp_array())

    def get_non_overlapping_repeating_blocks(self):
        extended_suffix_array = self.to_extended_suffix_array()
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
                    print("Selecting: "+str(potential_block))
                    occupied.union_update(non_overlapping_range)
                    real_blocks.append(Block(non_overlapping_range))
            except PartialOverlapException:          
                print("Skip due to conflict: "+str(potential_block))
                while potential_block.minimum_block_length > 1:
                    # retry with a different length: one less
                    for idx in range(potential_block.start+1, potential_block.end+1):
                        potential_block.LCP[idx] -= 1
                    potential_block.length -= 1
                    try:
                        non_overlapping_range = potential_block.calculate_non_overlapping_range_with(occupied)
                        if non_overlapping_range:
                            print("Retried and selecting: "+str(potential_block))
                            occupied.union_update(non_overlapping_range)
                            real_blocks.append(Block(non_overlapping_range))
                            break
                    except PartialOverlapException:          
                        print("Retried and failed again")
        return real_blocks

    def get_block_witness(self, witness):
        sigil_witness = witness.sigil
        range_witness = self.get_range_for_witness(sigil_witness)
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
        block_witness = BlockWitness(sorted_o, self.tokens)
        return block_witness

    @property
    def tokens(self):
        #TODO: complete set of witnesses is retokenized here!
        tokenizer = Tokenizer()
        tokens = tokenizer.tokenize(self.get_combined_string())
        return tokens
    
    




