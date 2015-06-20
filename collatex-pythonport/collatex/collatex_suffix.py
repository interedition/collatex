'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
from ClusterShell.RangeSet import RangeSet

class Stack(list):
    def push(self, item):
        self.append(item)
        
    def peek(self):
        return self[-1]
    
class PartialOverlapException(Exception):
    pass

class ExtendedSuffixArray(object):
    
    def __init__(self, tokens, sa_array, lcp_array, collation):
        self.tokens = tokens
        self.SA = sa_array
        self.LCP = lcp_array
        self.collation = collation

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
                    closed_intervals.append(LCPInterval(self.tokens, self.SA, self.LCP, start, idx-1, length, 0, self.collation))
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
            closed_intervals.append(LCPInterval(self.tokens, self.SA, self.LCP, start, len(self.LCP)-1, length, 0, self.collation))
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
    
    def __init__(self, tokens, SA, LCP, start, end, length, number_of_siblings, collation):
        self.tokens = tokens
        self.SA = SA
        self.LCP = LCP
        self.start = start
        self.end = end
        self.length = length
        self.number_of_siblings = number_of_siblings
        self.collation = collation
        
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

    def _as_range(self):
        # convert interval into range
        range = RangeSet()
        for occurrence in self.block_occurrences():
            range.add_range(occurrence, occurrence + self.minimum_block_length)
        return range

    @property
    def number_of_witnesses(self):
        range = self._as_range()
        number_of_witnesses = 0
        for witness_range in self.collation.witness_ranges.values():
            if witness_range.intersection(range):
                number_of_witnesses += 1
        return number_of_witnesses

    def calculate_non_overlapping_range_with(self, occupied):
        potential_block_range = self._as_range()
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
        first_range = next(real_block_range.contiguous())
        if first_range[-1]-first_range[0]+1>self.minimum_block_length:
            raise PartialOverlapException()
        return real_block_range
    
    def show_lcp_array(self):
        return self.LCP[self.start:self.end+1]
        
    def __str__(self):
        part1= "<"+" ".join(self.tokens[self.SA[self.start]:self.SA[self.start]+min(10, self.minimum_block_length)])
        return part1+"> with "+str(self.number_of_witnesses)+":"+str(self.number_of_occurrences)+" witnesses/occurrences and length: "+str(self.minimum_block_length)+" and number of siblings: "+str(self.number_of_siblings)
 
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


    




