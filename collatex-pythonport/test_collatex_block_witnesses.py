'''
Created on Apr 27, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from collatex_suffix import Collation


class Test(unittest.TestCase):

    def test_Hermans_case_block_witnesses(self):
        collation = Collation()
        collation.add_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_witness("W2", "a b c d F g h i ! q r s t")
        collation.add_witness("W3", "a b c d E g h i ! q r s t")
        block_witness = collation.get_first_block_witness()
        self.assertEquals(["a b c d", "F", "g h i", "!", "!", "q r s t"], block_witness.debug())
        #self.ass

 

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()