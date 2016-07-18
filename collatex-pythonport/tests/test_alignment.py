'''
Created on Jun 8, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from tests import unit_disabled
from collatex import Collation
from collatex import collate


class Test(unittest.TestCase):

    @unit_disabled
    # TODO This aligns 'the cat' instead of 'is'!
    def testDoubleTransposition1(self):
        collation = Collation()
        collation.add_plain_witness("A", "the cat is black")
        collation.add_plain_witness("B", "black is the cat")
        alignment_table = collate(collation)
        self.assertEquals(["the cat", "is", "black"], alignment_table.rows[0].to_list())
        self.assertEquals(["black", "is", "the cat"], alignment_table.rows[1].to_list())

    @unit_disabled
    def testDoubleTransposition2(self):
        # Either the 'a' can align or the 'b' can. See also #3 below.
        collation = Collation()
        collation.add_plain_witness("A", "a b")
        collation.add_plain_witness("B", "b a")
        alignment_table = collate(collation)
        witness_a_list = alignment_table.rows[0].to_list()
        self.assertEquals(len(witness_a_list), 3)
        witness_b_list = alignment_table.rows[1].to_list()
        self.assertEquals(len(witness_b_list), 3)
        matching_tokens = []
        for idx in range(3):
            if witness_a_list[idx] == witness_b_list[idx]:
                matching_tokens.append(witness_a_list[idx])
        self.assertEquals(len(matching_tokens), 1)

    @unit_disabled
    def testDoubleTransposition3(self):
        # Tricky. Aligning a and c can work; so can aligning b and c. Both
        # are equally valid, and both can crop up.
        # Let's test that each row has four values, and that two of the
        # columns have identical values, and that c is one of those columns.
        collation = Collation()
        collation.add_plain_witness("A", "a b c")
        collation.add_plain_witness("B", "b a c")
        alignment_table = collate(collation)
        witness_a_list = alignment_table.rows[0].to_list()
        self.assertEquals(len(witness_a_list), 4)
        witness_b_list = alignment_table.rows[1].to_list()
        self.assertEquals(len(witness_b_list), 4)
        matching_tokens = []
        for idx in range(4):
            if witness_a_list[idx] == witness_b_list[idx]:
                matching_tokens.append(witness_a_list[idx])
        self.assertEquals(len(matching_tokens), 2)
        self.assertIn("c", matching_tokens)

    @unit_disabled
    def testThisMorningExample(self):
        collation = Collation()
        collation.add_plain_witness("A", "This morning the cat observed little birds in the trees.")
        collation.add_plain_witness("B",
                                    "The cat was observing birds in the little trees this morning, it observed birds for two hours.")
        alignment_table = collate(collation, detect_transpositions=True)


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()