'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
from ClusterShell.RangeSet import RangeSet


class PartialOverlapException(Exception):
    pass


class ExtendedSuffixArray(object):
    
    def __init__(self, tokens, sa_array, lcp_array, collation):
        self.tokens = tokens
        self.SA = sa_array
        self.LCP = lcp_array
        self.collation = collation

    def list_prefixes(self):
        for idx in range(0, len(self.LCP)):
            if self.LCP[idx] > 0:
                prefix = " ".join(self.tokens[self.SA[idx]:self.SA[idx]+self.LCP[idx]])
                print(prefix)
            else:
                print("--------")


# This class is a simplified version of an LCP interval

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
            result.append(' '.join(self.tokens[next(occurrence.token_range.slices())]))
        return result


    




