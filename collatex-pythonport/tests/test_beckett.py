'''
Created on Jun 6, 2014

@author: ronald
'''
import unittest
from collatex.collatex_suffix import Collation


class Test(unittest.TestCase):

    #TODO: last one only works when transposition detection is added.
    def testBeckett(self):
        collation = Collation()
        collation.add_witness("1", "The same clock as when for example Magee once died.")
        collation.add_witness("2", "The same as when for example Magee once died.")
        table = collation.get_alignment_table()
        self.assertEquals(["The same", "clock", "as when for example Magee once died."], table.rows[0].to_list())
        self.assertEquals(["The same", "-", "as when for example Magee once died."], table.rows[1].to_list())
        
#         "The same as when for example McKee once died .",//
#         "The same as when among others Darly once died & left him.",//
#       #  "The same as when Darly among others once died and left him.");
        pass

# 
# if __name__ == "__main__":
#     #import sys;sys.argv = ['', 'Test.testName']
#     unittest.main()