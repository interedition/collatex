'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
#using RangeSet from ClusterShell project (install it first with pip)
from ClusterShell.RangeSet import RangeSet
from operator import itemgetter, methodcaller
from collatex.collatex_core import Witness, VariantGraph, Tokenizer
from collatex.linsuffarr import SuffixArray


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
                parent_lcp_intervals.append((start_position, index-1))
                # create new interval
                start_position = index 
            previous_prefix = prefix
        # add the final interval
        #NOTE: this one can be empty?
        parent_lcp_intervals.append((start_position, len(lcp)-1))    
        return parent_lcp_intervals

    def calculate_sub_lcp_intervals(self, lcp, parent_lcp_intervals):
        sub_lcp_intervals = []
        for start_position, end_position in parent_lcp_intervals:
            child_lcp_intervals = []
            child_interval_start = None
            previous_prefix = 0
            for index in range(start_position, end_position+1):
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
            child_lcp_intervals.append((child_interval_start, end_position))
            
            # add all the child_lcp_intervals to the sub_lcp_intervals list
            # with as third parameter the number of parent prefix occurrences
            for start, end in child_lcp_intervals:
                #NOTE: LCP intervals can be ascending or descending.
                child_lcp_interval_length = min(lcp[start+1], lcp[end])
                sub_lcp_intervals.append((start, end, len(child_lcp_intervals), child_lcp_interval_length, lcp[start_position:end_position+1]))
        return sub_lcp_intervals


    def calculate_potential_blocks(self):
        SA = self.get_sa().SA
        lcp = self.get_lcp_array() # step 1: calculate the sub LCP intervals
        lcp_intervals = self.get_lcp_intervals()
        sub_lcp_intervals = self.calculate_sub_lcp_intervals(lcp, lcp_intervals) # step 2: process the LCP sub intervals
    # and generate potential blocks
        potential_blocks = []
        for start, end, parent_prefix_occurrences, block_length, parent_lcp in sub_lcp_intervals:
            number_of_occurrences = end - start + 1
            block_occurrences = []
            for idx in range(start, end + 1):
                block_occurrences.append(SA[idx])
            
            potential_blocks.append((number_of_occurrences, block_length, parent_prefix_occurrences, block_occurrences, start, lcp[start:end + 1], parent_lcp))
        return potential_blocks

    # filter out all the blocks that have more than one occurrence within a witness
    def filter_potential_blocks(self, potential_blocks):
        for (number_of_occurrences, block_length, parent_prefix_occurrences, block_occurrences, start, lcp_interval, parent_lcp) in potential_blocks:
            for witness in self.witnesses:
                witness_sigil = witness.sigil
                witness_range = self.get_range_for_witness(witness_sigil)
                inter = witness_range.intersection(block_occurrences)
                print(inter)
                if len(inter)> block_length:
                    print("Say hi!")
                    potential_blocks.remove((number_of_occurrences, block_length, parent_prefix_occurrences, block_occurrences, start, lcp_interval, parent_lcp))
                    break
    
    def get_non_overlapping_repeating_blocks(self):
        potential_blocks = self.calculate_potential_blocks() 
        self.filter_potential_blocks(potential_blocks)
        # step 3: sort the blocks based on depth (number of repetitions) first,
        # second length of LCP interval,
        # third sort on parent LCP interval occurrences.
        sorted_blocks_on_priority = sorted(potential_blocks, key=itemgetter(0, 1, 2), reverse=True)
        # step 4: select the definitive blocks
        # NOTE: Tokenizer is for debug reasons only!
        tokenizer = Tokenizer()
        tokens = tokenizer.tokenize(self.get_combined_string()) 
        #Note: SA is for debug reasons only
        SA = self.get_sa().SA
        occupied = RangeSet()
        real_blocks = []
        for number_of_occurrences, block_length, parent_prefix_occurrences, block_occurrences, start, lcp_interval, parent_lcp in sorted_blocks_on_priority:
#             if number_of_occurrences > len(self.witnesses):
#                 print("Skipped one!")
#                 continue
            print("looking at: <"+" ".join(tokens[SA[start]:SA[start]+min(10, block_length)])+"> with "+str(number_of_occurrences)+" occurrences and length: "+str(block_length)+" and parent prefix occurrences: "+str(parent_prefix_occurrences)+" lcp: "+str(lcp_interval))
            if SA[start]=="cause":
                print(" parent LCP: "+str(parent_lcp))
                for SA_index in range(start, start+len(parent_lcp)):
                    print(" ".join(tokens[SA[SA_index]:SA[SA_index]+10]))
            self._add_potential_block_to_real_blocks(block_length, block_occurrences, occupied, real_blocks)
             
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

    def _add_potential_block_to_real_blocks(self, block_length, block_occurrences, occupied, real_blocks):
        # convert block occurrences into ranges
        potential_block_range = RangeSet()
        for occurrence in block_occurrences:
            potential_block_range.add_range(occurrence, occurrence + block_length)
        #check the intersection with the already occupied ranges
        block_intersection = potential_block_range.intersection(occupied)
        if block_intersection:
#             print("was: "+str(potential_block_range))
#             print("occupied: "+str(occupied))
#             print("intersection: "+str(block_intersection))
            real_block_range = RangeSet()
            for lower in potential_block_range.contiguous():
                # TODO: what I really want here is a find first over a generator
                upper = [x for x in block_intersection.contiguous() if x[0] >= lower[0]]
                if upper:
                    lower = lower[0]
                    upper = upper[0][0]
                    if lower != upper:
                        real_block_range.add_range(lower, upper)
            if real_block_range:
                # Assert: check that the first slice is not larger than potential block length!
                first_range = real_block_range.contiguous().next()
                if first_range[-1]-first_range[0]+1>block_length:
                    print("was: "+str(potential_block_range))
                    print("occupied: "+str(occupied))
                    print("intersection: "+str(block_intersection))
                    print("First range: "+str(first_range)+" "+str(first_range[0])+" "+str(first_range[-1]))
                    assert(first_range[-1]-first_range[0]+1<=block_length)
                
#                 print("real: "+str(real_block_range))
                occupied.union_update(real_block_range)
                real_blocks.append((Block(real_block_range), block_length, block_occurrences))
        else:
#             print("real: "+str(potential_block_range))
            occupied.union_update(potential_block_range)
            real_blocks.append((Block(potential_block_range), block_length, block_occurrences))

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
    




