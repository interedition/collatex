'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
#using RangeSet from ClusterShell project (install it first with pip)
from ClusterShell.RangeSet import RangeSet
from operator import methodcaller, attrgetter
from collatex.collatex_core import Witness, VariantGraph, Tokenizer
from collatex.linsuffarr import SuffixArray

class LCPInterval(object):
    
    def __init__(self, LCP, begin, end):
        self.LCP = LCP
        self.start_position = begin
        self.end_position = end


class PartialOverlapException(Exception):
    pass


# parts of the LCP array become potential blocks.
# block_length: the number of tokens a single occurrence of this block spans
# block_occurrences: the ranges within the suffix array that this block spans
class LCPSubinterval(object):
    
    def __init__(self, tokens, SA, LCP, start, end, number_of_siblings, parent_lcp_interval):
        self.tokens = tokens
        self.SA = SA
        self.LCP = LCP
        self.start = start
        self.end = end
        self.number_of_siblings = number_of_siblings
        self.parent_lcp_interval = parent_lcp_interval
        
    @property
    def minimum_block_length(self):
        #NOTE: LCP intervals can be ascending or descending.
        return min(self.LCP[self.start+1], self.LCP[self.end])
    
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
    
    def list_prefixes(self):
        for idx in range(self.start, self.end + 1):
            if self.LCP[idx] > 0:
                prefix = " ".join(self.tokens[self.SA[idx]:self.SA[idx]+self.LCP[idx]])
                print(prefix)
    
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
        return "<"+" ".join(self.tokens[self.SA[self.start]:self.SA[self.start]+min(10, self.minimum_block_length)])+"> with "+str(self.number_of_occurrences)+" occurrences and length: "+str(self.minimum_block_length)+" and number of siblings: "+str(self.number_of_siblings)
 
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
        return SuffixArray(self.combined_string)

    def get_lcp_array(self):
        sa = self.get_sa()
        return sa._LCP_values
    

    # Note: LCP intervals can overlap.. for now we solve this with a two pass algorithm
    def get_lcp_intervals(self, lcp = None):
        lcp = lcp if lcp else self.get_lcp_array()
        parent_lcp_intervals = []
        # first detect the intervals based on zero's
        start_position = 0
        previous_prefix = 0
        for index, prefix in enumerate(lcp):
            if prefix == 0 and previous_prefix == 0:
                start_position = index
            if prefix == 0 and not previous_prefix == 0:
                # first end last interval
                parent_lcp_intervals.append(LCPInterval(lcp, start_position, index-1))
                # create new interval
                start_position = index 
            previous_prefix = prefix
        # add the final interval
        #NOTE: this one can be empty?
        parent_lcp_intervals.append(LCPInterval(lcp, start_position, len(lcp)-1))    
        return parent_lcp_intervals

    def calculate_sub_lcp_intervals(self, lcp, parent_lcp_intervals):
        # NOTE: Tokenizer is for debug reasons only!
        tokenizer = Tokenizer()
        tokens = tokenizer.tokenize(self.get_combined_string()) 
        SA = self.get_sa().SA
        sub_lcp_intervals = []
        for lcp_interval in parent_lcp_intervals:
            child_lcp_intervals = []
            child_interval_start = None
            previous_prefix = 0
            for index in range(lcp_interval.start_position, lcp_interval.end_position+1):
                prefix = lcp[index]
                if prefix > previous_prefix and lcp[index-2] >= previous_prefix:
                    # first end last interval
                    if child_interval_start:
                        child_lcp_intervals.append((child_interval_start, index - 2))
                    # create new interval
                    child_interval_start = index - 1
                previous_prefix = prefix 
            # add the final interval
            #NOTE: this one can be empty?
            child_lcp_intervals.append((child_interval_start, lcp_interval.end_position))
            
            # add all the child_lcp_intervals to the sub_lcp_intervals list
            # with as third parameter the number of parent prefix occurrences
            for start, end in child_lcp_intervals:
                sub_lcp_intervals.append(LCPSubinterval(tokens, SA, lcp, start, end, len(child_lcp_intervals), lcp_interval))
        return sub_lcp_intervals


    def split_lcp_intervals(self):
        lcp_intervals = self.calculate_potential_blocks()
        result = []
        for lcp_interval in lcp_intervals:
            array = lcp_interval.show_lcp_array()
            print("LCP array is "+str(array))
            # NOTE: LCP intervals can 1) ascend or 2) descend or 3) first ascend and then descend.
            # NOTE:The case of descending and then ascending is already handled in the sub_intervals method
            # Make sure that the LCP array is ascending!
            if lcp_interval.LCP[lcp_interval.start+1] > lcp_interval.LCP[lcp_interval.end]:
                print("LCP array is descending: ")
                lcp_interval.list_prefixes()
                result.append(lcp_interval)
                continue
            # now we need to build up a stack of open intervals
            previous_lcp_value = 0
            open_intervals = []
            for idx in range(lcp_interval.start+1, lcp_interval.end+1):
                lcp_value = lcp_interval.LCP[idx]
                if lcp_value > previous_lcp_value:
                    open_intervals.append(idx-1)
                    previous_lcp_value = lcp_value
            for o_i in open_intervals:
                result.append(LCPSubinterval(lcp_interval.tokens, lcp_interval.SA, lcp_interval.LCP, o_i, lcp_interval.end, lcp_interval.number_of_siblings, lcp_interval.parent_lcp_interval))
                print("new: "+str(result[-1]))
        return result
        
        
    def calculate_potential_blocks(self):
        # step 1: calculate the sub LCP intervals
        lcp = self.get_lcp_array()
        lcp_intervals = self.get_lcp_intervals()
        sub_lcp_intervals = self.calculate_sub_lcp_intervals(lcp, lcp_intervals) 
        # step 2: process the LCP sub intervals
        # and generate more potential blocks
        # TODO: more blocks can be generated here
        return sub_lcp_intervals

    # filter out all the blocks that have more than one occurrence within a witness
    def filter_potential_blocks(self, potential_blocks):
        for potential_block in potential_blocks:
            for witness in self.witnesses:
                witness_sigil = witness.sigil
                witness_range = self.get_range_for_witness(witness_sigil)
                inter = witness_range.intersection(potential_block.block_occurrences())
                if len(inter)> potential_block.minimum_block_length:
                    print("Removing block: "+str(potential_block))
                    potential_blocks.remove(potential_block)
                    break
    
    def get_non_overlapping_repeating_blocks(self):
        potential_blocks = self.calculate_potential_blocks()
        # TODO: activate!    
        # potential_blocks = self.split_lcp_intervals() 
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
        for _ in tokens:
            suffixes.append(tokens[i:])
            i=i+1
        return suffixes
    




