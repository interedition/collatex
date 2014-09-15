'''
Created on Sep 12, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from collatex.collatex_dekker_algorithm import Collation, collate


class Test(unittest.TestCase):

    def test_hermans_witness_order_independence_case_two_witnesses(self):
        collation = Collation()
        collation.add_witness("A", "a b c d F g h i ! K ! q r s t")
        collation.add_witness("B", "a b c d F g h i ! q r s t")
        alignment_table = collate(collation, output="novisualization")
        self.assertEquals(["a b c d F g h i!", "K!", "q r s t"], alignment_table.rows[0].to_list())
        self.assertEquals(["a b c d F g h i!", "-", "q r s t"], alignment_table.rows[1].to_list())



if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()