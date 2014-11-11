'''
Created on Aug 4, 2014

@author: Ronald Haentjens Dekker
'''

from collatex.astar import AStarNode, AStar

#The second parameter could also be the tree
class DecisionTreeNode(AStarNode):
    def __init__(self, aligner):
        self.pointer_a = 0
        self.pointer_b = 0
        self.aligner = aligner
        #call super
        super(DecisionTreeNode, self).__init__()
        
    def is_end_node(self):
        #TODO: tokens recalculated 
        # better to do this with iteration
        # This can then be written without if statements
        len_wit_a = len(self.aligner.witness_a.tokens())
        if self.pointer_a == len_wit_a:
            return True
        len_wit_b = len(self.aligner.witness_b.tokens())
        if self.pointer_b == len_wit_b:
            return True
        return False
        
class DecisionTree(AStar):
    def __init__(self, aligner):
        self.aligner = aligner
        
    def create_childnodes(self):
#         # check whether a token is a match
#         token_a = tokens_a[self.state.pointer_a]
#         token_b = tokens_b[self.state.pointer_b]
#         match = token_a.eql(token_b)
# 
#         #move this to above!
#         if not match:
#             # we try to handle mismatch as an omission
#             self.states
#             
        pass

    

class Aligner(object):
    '''
    Decision Tree based aligner
    This is a prototype: 
    There are limitations
    At first it only works with just two witnesses
    It works with two pointers.
    The pointers start at the beginning (first token) of each of the witnesses.
    If this prototype works, one of the witnesses has to be replaced by the graph
    Then if that works, the enhanced suffix array based optimalization has to be integrated.
    Also we start by tracking one decision state.. we should keep track of more state
    to reach the right conclusion. The A* algorithm works very well for that.
    '''


    def __init__(self, witness_a, witness_b):
        '''
        Constructor
        '''
        self.witness_a = witness_a
        self.witness_b = witness_b
        self.tree = DecisionTree(self)
        
    def align(self):
        '''
        Every step we have 3 choices:
        1) Move pointer witness a --> omission
        2) Move pointer witness b --> addition
        3) Move pointer of both witness a/b  --> match
        Note: a replacement is omission followed by an addition or the other way around
        
        Choice 1 and 2 are only possible if token a and b are not a match OR when tokens are repeated.
        For now I ignore token repetition..
        '''
        # extract tokens from witness (note that this can be done in a streaming manner if desired)
        tokens_a = self.witness_a.tokens()
        tokens_b = self.witness_b.tokens()
        # create virtual decision tree (nodes are created on demand)
        # see above
        # create start node
        start = DecisionTreeNode(self)
        
        # search the decision tree
        result = self.tree.search(start)
        print(result)
        
        
        pass
    