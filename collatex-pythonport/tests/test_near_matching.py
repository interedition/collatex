'''
Created on Sep 12, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from collatex import Collation, collate


class Test(unittest.TestCase):

    def test_exact_matching(self):
        collation = Collation()
        collation.add_plain_witness("A", "I bought this glass , because it matches those dinner plates")
        collation.add_plain_witness("B", "I bought those glasses")
        alignment_table = collate(collation)
        self.assertEquals(["I bought ", "this glass , because it matches ", "those ", "dinner plates"],
                          alignment_table.rows[0].to_list_of_strings())
        self.assertEquals(["I bought ", None, "those ", "glasses"], alignment_table.rows[1].to_list_of_strings())

    def test_near_matching(self):
        collation = Collation()
        collation.add_plain_witness("A", "I bought this glass , because it matches those dinner plates")
        collation.add_plain_witness("B", "I bought those glasses")
        alignment_table = collate(collation, near_match=True)
        self.assertEquals(["I bought ", "this glass , because it matches those dinner plates"],
                          alignment_table.rows[0].to_list_of_strings())
        self.assertEquals(["I bought ", "those glasses"], alignment_table.rows[1].to_list_of_strings())


if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testOmission']
    unittest.main()
