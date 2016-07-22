'''
Created on Sep 12, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from collatex import Collation, collate
from collatex.exceptions import SegmentationError
from collatex.near_matching import Scheduler


class Test(unittest.TestCase):

    def assertTask(self, expected_description, expected_args, actual):
        # compare description (as string)
        self.assertEqual(expected_description, actual.name)
        # compare arguments (as strings; does not test same number of args!)
        for counter, expected_arg in enumerate(expected_args):
            actual_arg = str(actual.args[counter])
            self.assertEqual(expected_arg, actual_arg)

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
        # Arguments to collate() must be passed as arguments to assertRaises()
        self.assertRaises(SegmentationError, collate, collation, near_match=True)

    def test_near_matching_accidentally_correct_short(self):
        collation = Collation()
        collation.add_plain_witness("A", "over this dog")
        collation.add_plain_witness("B", "over that there dog")
        alignment_table = str(collate(collation, near_match=True, segmentation=False))
        expected = """\
+---+------+------+-------+-----+
| A | over | this | -     | dog |
| B | over | that | there | dog |
+---+------+------+-------+-----+"""
        self.assertEquals(expected, alignment_table)

    def test_near_matching_accidentally_incorrect_short(self):
        collation = Collation()
        collation.add_plain_witness("A", "over this dog")
        collation.add_plain_witness("B", "over there that dog")
        alignment_table = str(collate(collation, near_match=True, segmentation=False))
        expected = """\
+---+------+-------+------+-----+
| A | over | -     | this | dog |
| B | over | there | that | dog |
+---+------+-------+------+-----+"""
        self.assertEquals(expected, alignment_table)

    def test_near_matching_accidentally_correct_long(self):
        collation = Collation()
        collation.add_plain_witness("A", "The brown fox jumps over this dog.")
        collation.add_plain_witness("B", "The brown fox jumps over that there dog.")
        alignment_table = str(collate(collation, near_match=True, segmentation=False))
        expected = """\
+---+-----+-------+-----+-------+------+------+-------+-----+---+
| A | The | brown | fox | jumps | over | this | -     | dog | . |
| B | The | brown | fox | jumps | over | that | there | dog | . |
+---+-----+-------+-----+-------+------+------+-------+-----+---+"""
        self.assertEquals(expected, alignment_table)

    def test_near_matching_accidentally_incorrect_long(self):
        self.maxDiff = None
        scheduler = Scheduler()
        collation = Collation()
        collation.add_plain_witness("A", "The brown fox jumps over this dog.")
        collation.add_plain_witness("B", "The brown fox jumps over there that dog.")
        alignment_table = str(collate(collation, near_match=True, segmentation=False, scheduler=scheduler))
        self.assertTask("determine whether node should be moved", ["this"], scheduler[0])
        self.assertTask("move node from prior rank to rank", ["this", "6", "7"], scheduler[1])
        self.assertTask("determine whether node should be moved", ["over"], scheduler[2])
        self.assertEquals(3, len(scheduler))
        expected = """\
+---+-----+-------+-----+-------+------+-------+------+-----+---+
| A | The | brown | fox | jumps | over | -     | this | dog | . |
| B | The | brown | fox | jumps | over | there | that | dog | . |
+---+-----+-------+-----+-------+------+-------+------+-----+---+"""
        self.assertEquals(expected, alignment_table)

    def test_near_matching_rank_0(self):
        # find_prior_node() should check ranks back through 0, not 1
        collation = Collation()
        collation.add_plain_witness("A", "this")
        collation.add_plain_witness("B", "there thin")
        output = str(collate(collation, near_match=True, segmentation=False))
        expected = """\
+---+-------+------+
| A | -     | this |
| B | there | thin |
+---+-------+------+"""
        self.assertEqual(expected, output)

if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testOmission']
    unittest.main()
