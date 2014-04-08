'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
#using rangeset (0.0.5) from http://axiak.github.com/py-rangeset
from rangeset import RangeSet

class Block(object):
    
    def __init__(self, ranges):
        self.ranges = ranges
    
    def __str__(self):
        return "Block with occurrences "+self.ranges.__str__()
    
class SuperMaximumRe(object):
    

    def find_max_prefix(self, lcp):
        max_prefix = 0
        max_position = 0
        for index, prefix in enumerate(lcp):
            if (prefix > max_prefix):
                max_prefix = prefix
                max_position = index
        
        print(max_prefix, max_position)
        return max_position, max_prefix

    def find_blocks(self, sa):
        lcp = sa._LCP_values
        blocks = []
        
        max_prefix = -1
        while(max_prefix!=0):
            max_position, max_prefix = self.find_max_prefix(lcp)
            if (max_prefix!=0):
                piece1 = sa.SA[max_position-1]
                piece2 = sa.SA[max_position]
                p1 = RangeSet(piece1, piece1+max_prefix-1)
                p2 = RangeSet(piece2, piece2+max_prefix-1)
                blocks.append(Block(RangeSet.mutual_union(p1, p2)))
                # reset the lcp value to zero
                # it is not nice to change the lcp value
                lcp[max_position]=0
        return blocks

# not used
# Tokenizer inside suffix array library is used
class Tokenizer(object):
    
    #by default the tokenizer splits on space characters    
    def tokenize(self, contents):
        return contents.split()

# not used
# suffix library is used    
class Suffix(object):
    
    #generate suffixes from a list of tokens
    def gather_suffices(self, tokens):
        i = 0
        suffixes = []
        for t in tokens:
            suffixes.append(tokens[i:])
            i=i+1
        return suffixes
    



