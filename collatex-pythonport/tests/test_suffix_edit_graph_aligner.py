'''
Created on Sep 12, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from collatex import Collation, collate


class Test(unittest.TestCase):

    def test_hermans_witness_order_independence_case_two_witnesses(self):
        collation = Collation()
        collation.add_plain_witness("A", "a b c d F g h i ! K ! q r s t")
        collation.add_plain_witness("B", "a b c d F g h i ! q r s t")
        alignment_table = collate(collation)
        self.assertEquals(["a b c d F g h i ! ", "K ! ", "q r s t"], alignment_table.rows[0].to_list_of_strings())
        self.assertEquals(["a b c d F g h i ! ", None, "q r s t"], alignment_table.rows[1].to_list_of_strings())

    def test_hermans_witness_order_independence_case_three_witnesses(self):
        collation = Collation()
        collation.add_plain_witness("A", "a b c d F g h i ! K ! q r s t")
        collation.add_plain_witness("B", "a b c d F g h i ! q r s t")
        collation.add_plain_witness("C", "a b c d E g h i ! q r s t")
        alignment_table = collate(collation)
        self.assertEquals(["a b c d ", "F ", "g h i ", "! K ", "! q r s t"], alignment_table.rows[0].to_list_of_strings())
        self.assertEquals(["a b c d ", "F ", "g h i ", None, "! q r s t"], alignment_table.rows[1].to_list_of_strings())
        self.assertEquals(["a b c d ", "E ", "g h i ", None, "! q r s t"], alignment_table.rows[2].to_list_of_strings())

    def test_witness_order(self):
        collation = Collation()
        collation.add_plain_witness("A", "x a y")
        collation.add_plain_witness("B", "x b y")
        collation.add_plain_witness("C", "x a b y")
        alignment_table = collate(collation)
        self.assertEquals(["x ", "a ", None, "y"], alignment_table.rows[0].to_list_of_strings())
        self.assertEquals(["x ", None, "b ", "y"], alignment_table.rows[1].to_list_of_strings())
        self.assertEquals(["x ", "a ", "b ", "y"], alignment_table.rows[2].to_list_of_strings())

    # TODO: test with x b a y
    
if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()