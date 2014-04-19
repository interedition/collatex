'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
#using RangeSet from ClusterShell project (install it first with pip)
from ClusterShell.RangeSet import RangeSet
from collatex_core import Witness, VariantGraph, CollationAlgorithm
from linsuffarr import SuffixArray

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
        witness_range = range(self.counter, self.counter+len(witness.tokens()))
        # the extra one is for the marker token
        self.counter += len(witness.tokens()) +1 
        self.witness_ranges[sigil] = witness_range
        if not self.combined_string == "":
            self.combined_string += " $"+str(len(self.witnesses)-1)+ " "
        self.combined_string += content
        
    def get_blocks(self):
        sa = self.get_sa()
        smr = SuperMaximumRe()
        blocks = smr.find_blocks(sa)
        return blocks
    
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

class Block(object):
    
    def __init__(self, ranges):
        self.ranges = ranges
    
    def __eq__(self, other):
        if type(other) is type(self):
            return self.__dict__ == other.__dict__
        return False
    
    def __str__(self):
        return "Block with occurrences "+self.ranges.__str__()
    
    def __repr__(self):
        return "wowie a block: "+self.ranges.__str__()
    
class SuperMaximumRe(object):
    
    def find_blocks(self, sa):
        lcp = sa._LCP_values
        blocks = []
        # TODO: instead of using an occupied range set it might be better
        # to loop over the blocks and delegate this responsibility to them.
        occupied = RangeSet()
        max_prefix = -1
        while(max_prefix!=0):
            max_position, max_prefix = self.find_max_prefix(lcp)
            if (max_prefix!=0):
                piece1 = sa.SA[max_position-1]
                piece2 = sa.SA[max_position]
                blockRanges = RangeSet()
                blockRanges.add_range(piece1, piece1+max_prefix)
                blockRanges.add_range(piece2, piece2+max_prefix)
                if not (occupied.intersection(blockRanges)):
                    block = Block(blockRanges)
                    blocks.append(block)
                    occupied = occupied.union(blockRanges)
                # reset the lcp value to zero
                # TODO: it is not nice to change the lcp value
                lcp[max_position]=0
        return blocks

    def find_max_prefix(self, lcp):
        max_prefix = 0
        max_position = 0
        for index, prefix in enumerate(lcp):
            if (prefix > max_prefix):
                max_prefix = prefix
                max_position = index
        
        #print(max_prefix, max_position)
        return max_position, max_prefix

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
    
    def buildVariantGraphFromBlocks(self, graph, collation):
        '''
        :type graph: VariantGraph
        :type collation: Collation
        '''
        first_witness = collation.witnesses[0]
        tokens = first_witness.tokens()
        self.merge(graph, tokens)
        
        
#         for token in tokens:
#             graph.
#         #graph.
        pass

#  /*
# * take the vectors from the vectorspace.
# * they represent the alignment
# * build a Vector -> List<vertex> representation
# * 1)Building the graph for the first witness is
# * 1) easy --> a vertex for every token
# * 2)then build the initial vector to vertex map
# * find all the vector that have a coordinate in the
# * first dimension (that is the dimension related to
# * the first witness)
# * 3) Merge in witness b
# */
