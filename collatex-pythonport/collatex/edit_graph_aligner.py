'''
Created on Aug 5, 2014

@author: Ronald Haentjens Dekker
'''

class EditGraphAligner(object):
    '''
    Aligner based on an edit graph.
    It needs a score function and a definition of a match.
    Since every node of the graph has three children the graph is represented as a table internally.
    THIS CLASS IS ONLY HERE TO SHOW HOW THE SCORING WORKS AND THAT EVERY POSSIBLE COMBINATION IS CONSIDERED!
    THE A* BASED ALIGNER WILL BE MUCH FASTER!
    '''
    def __init__(self, witness_a, witness_b):
        '''
        Constructor
        '''
        self.tokens_witness_a = witness_a.tokens()
        self.tokens_witness_b = witness_b.tokens()
        self.length_witness_a = len(self.tokens_witness_a)
        self.length_witness_b = len(self.tokens_witness_b)
        self.table = [[0 for _ in range(self.length_witness_a)] for _ in range(self.length_witness_b)]
        
    # This function traverses the table diagonally and scores each cell.
    # Original function from Mark Byers; translated from C into Python.
    def traverse_diagonally(self):
        m = self.length_witness_b
        n = self.length_witness_a
        for _slice in range(0, m + n - 1, 1):
            z1 = 0 if _slice < n else _slice - n + 1;
            z2 = 0 if _slice < m else _slice - m + 1;
            j = _slice - z2
            while j >= z1:
                self.table[j][_slice-j]=self.score(j, _slice - j)
                j -= 1
    
    def align(self):
        # initialize root node score to zero
        self.table[0][0] = 0
        # per diagonal calculate the score (taking into account the three surrounding nodes)
        self.traverse_diagonally()
        
            
    def score(self, y, x):
        #TODO: implement seriously
        return x+y