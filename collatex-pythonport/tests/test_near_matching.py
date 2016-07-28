"""
Created on Sep 12, 2014

@author: Ronald Haentjens Dekker
"""
import unittest
from collatex import Collation, collate
from collatex.core_classes import VariantGraphRanking
from collatex.exceptions import SegmentationError
from collatex.near_matching import Scheduler, get_nodes_in_reverse_rank_order, find_prior_node


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
        self.assertTask("build column for rank", ["this", "6"], scheduler[0])
        self.assertTask("build column for rank", ["this", "7"], scheduler[1])
        self.assertTask("move node from prior rank to rank with best match", ["this", "6", "7"], scheduler[2])
        self.assertTask("build column for rank", ["over", "5"], scheduler[3])
        self.assertTask("build column for rank", ["over", "6"], scheduler[4])
        self.assertEquals(5, len(scheduler))
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

    def test_near_matching_middle(self):
        # Three candidates, closest is middle, match rank 2 0 1 (0 is closest)
        # Should go to the middle; incorrectly goes right
        self.maxDiff = None
        scheduler = Scheduler()
        collation = Collation()
        collation.add_plain_witness("A", "abcd 0123 efgh")
        collation.add_plain_witness("B", "abcd 0xxx 012x 01xx efgh")
        alignment_table = str(collate(collation, near_match=True, segmentation=False, scheduler=scheduler))
        # Find the rightmost rank with a gap (rank 4); this is activeRank
        # Find the first witness with a gap at that rank (A)
        # Find first token to the left of the gap for the first gappy witness ("0123" in A at rank 2)
        #   and check whether to move it
        # Calculate strength of match for all columns from the token's current rank (2) through activeRank (4)
        #   parameters are token string and rank to check
        self.assertTask("build column for rank", ["0123", "2"], scheduler[0])
        self.assertTask("build column for rank", ["0123", "3"], scheduler[1])
        self.assertTask("build column for rank", ["0123", "4"], scheduler[2])
        # The best (max()) fit of "0123" among all ranks between current rank 2 and activeRank 4
        #   is at rank 3, so move "0123" from current rank 2 to rank 3
        self.assertTask("move node from prior rank to rank with best match", ["0123", "2", "3"], scheduler[3])
        # No more gaps at activeRank 4, no gaps at rank 3, so move to next rank with a gap
        #   (rank 2, gap in A), with "abcd" at rank 1
        self.assertTask("build column for rank", ["abcd", "1"], scheduler[4])
        self.assertTask("build column for rank", ["abcd", "2"], scheduler[5])
        # Don't move it because it's closer to current location
        # No more gaps at rank 2, non gaps at rank 1, no more ranks
        self.assertEquals(6, len(scheduler))
        expected = """\
+---+------+------+------+------+------+
| A | abcd | -    | 0123 | -    | efgh |
| B | abcd | 0xxx | 012x | 01xx | efgh |
+---+------+------+------+------+------+"""
        self.assertEqual(expected, alignment_table)

    def test_near_matching_three_witnesses(self):
        self.maxDiff = None
        scheduler = Scheduler()
        collation = Collation()
        collation.add_plain_witness("A", "abcd 012345 efgh")
        collation.add_plain_witness("B", "abcd 0xxxxx 01xxxx 01234x 012xxx 0123xx efgh")
        collation.add_plain_witness("C", "abcd 01xxxx zz23xx efgh")
        alignment_table = str(collate(collation, near_match=True, segmentation=False, scheduler=scheduler))
        # Find the rightmost rank with a gap (rank 6); this is activeRank
        # Find the first witness (alphabetically by siglum) with a gap at that rank (A)
        # Get the first token to the left of the gap for the first gappy witness ("012345" in A at rank 2)
        #   and check whether to move it
        # Calculate strength of match for all columns from current rank (2) through activeRank (6), inclusive
        self.assertTask("build column for rank", ["012345", "2"], scheduler[0])
        self.assertTask("build column for rank", ["012345", "3"], scheduler[1])
        self.assertTask("build column for rank", ["012345", "4"], scheduler[2])
        self.assertTask("build column for rank", ["012345", "5"], scheduler[3])
        self.assertTask("build column for rank", ["012345", "6"], scheduler[4])
        # The best (max()) fit of "012345" among all ranks between current rank 2 and activeRank 6
        #   is at rank 4, so move "012345" from current rank 2 to rank 4
        self.assertTask("move node from prior rank to rank with best match", ["012345", "2", "4"], scheduler[5])
        # Find next (alphabetically) witness with a gap at activeRank (still 6), which is witness C
        # Get the first token to the left of the gap ("zz23xx" in C at rank 4)
        #   and check whether to move it
        # Calculate strength of match for all columns from current rank (4) through activeRank (6), inclusive
        self.assertTask("build column for rank", ["zz23xx", "4"], scheduler[6])
        self.assertTask("build column for rank", ["zz23xx", "5"], scheduler[7])
        self.assertTask("build column for rank", ["zz23xx", "6"], scheduler[8])
        # The best (max()) fit of "zz23xx" among all ranks between current rank 4 and activeRank 6
        #   is at rank 6, so move "zz23xx" from current rank 4 to rank 6
        self.assertTask("move node from prior rank to rank with best match", ["zz23xx", "4", "6"], scheduler[9])
        # No more gaps at rank 6, so advance to rank 5, which has gaps in witnesses A and C
        # First gap (alphabetically by siglum) at rank 5 is in witness A, where left node is "012345" at rank 4
        self.assertTask("build column for rank", ["012345", "4"], scheduler[10])
        self.assertTask("build column for rank", ["012345", "5"], scheduler[11])
        # Match is closest at current rank 4, so don't move the node
        # Next gap at rank 5 is in witness C, where left node is "01xxxx" at rank 3
        self.assertTask("build column for rank", ["01xxxx", "3"], scheduler[12])
        self.assertTask("build column for rank", ["01xxxx", "4"], scheduler[13])
        self.assertTask("build column for rank", ["01xxxx", "5"], scheduler[14])
        # Exact match at current rank 3, so don't move it
        # No more gaps at rank 5, so advance to rank 4, which has a gap in witness C,
        #   where left node is "01xxxx" at rank 3
        self.assertTask("build column for rank", ["01xxxx", "3"], scheduler[15])
        self.assertTask("build column for rank", ["01xxxx", "4"], scheduler[16])
        # Exact match at rank 3, so don't move it
        # No more gaps at rank 4, so advance to rank 3, where only gap is in witness A, with "abcd" at rank 1
        self.assertTask("build column for rank", ["abcd", "1"], scheduler[17])
        self.assertTask("build column for rank", ["abcd", "2"], scheduler[18])
        self.assertTask("build column for rank", ["abcd", "3"], scheduler[19])
        # Exact match at rank 1, so don't move it
        # No more gaps at rank 3, so advance to rank 2, with gaps in witnesses A and C and "abcd" at rank 1
        # Check witness A first
        self.assertTask("build column for rank", ["abcd", "1"], scheduler[20])
        self.assertTask("build column for rank", ["abcd", "2"], scheduler[21])
        # Exact match at rank 1, so don't move it
        # Check witness C
        self.assertTask("build column for rank", ["abcd", "1"], scheduler[22])
        self.assertTask("build column for rank", ["abcd", "2"], scheduler[23])
        # Exact match at rank 1, so don't move it
        # No more gaps at rank 2, no gaps at rank 1
        self.assertEquals(24, len(scheduler))
        expected = """\
+---+------+--------+--------+--------+--------+--------+------+
| A | abcd | -      | -      | 012345 | -      | -      | efgh |
| B | abcd | 0xxxxx | 01xxxx | 01234x | 012xxx | 0123xx | efgh |
| C | abcd | -      | 01xxxx | -      | -      | zz23xx | efgh |
+---+------+--------+--------+--------+--------+--------+------+"""
        self.assertEqual(expected, alignment_table)

    def test_near_matching_repetition(self):
        self.maxDiff = None
        scheduler = Scheduler()
        collation = Collation()
        collation.add_plain_witness("A", "abcd 01234x 0123xx efgh")
        collation.add_plain_witness("B", "abcd 01xxxx 01234x 01234x 012xxx 0123xx efgh")
        collation.add_plain_witness("C", "abcd 012345 efgh")
        alignment_table = str(collate(collation, near_match=True, segmentation=False, scheduler=scheduler))
        scheduler.debug_tasks()
        # Without near matching, output is:
        # +---+------+--------+--------+--------+--------+--------+------+
        # | A | abcd | -      | 01234x | -      | -      | 0123xx | efgh |
        # | B | abcd | 01xxxx | 01234x | 01234x | 012xxx | 0123xx | efgh |
        # | C | abcd | 012345 | -      | -      | -      | -      | efgh |
        # +---+------+--------+--------+--------+--------+--------+------+
        # Gap at rank 6 for witness C; try to move "012345" from rank 2 for witness C
        self.assertTask("build column for rank", ["012345", "2"], scheduler[0])
        self.assertTask("build column for rank", ["012345", "3"], scheduler[1])
        self.assertTask("build column for rank", ["012345", "4"], scheduler[2])
        self.assertTask("build column for rank", ["012345", "5"], scheduler[3])
        self.assertTask("build column for rank", ["012345", "6"], scheduler[4])
        # Closest match is rank 3, so move "012345" from rank 2 to rank 3
        self.assertTask("move node from prior rank to rank with best match", ["012345", "2", "3"], scheduler[5])
        # Structure is now
        # +---+------+--------+--------+--------+--------+--------+------+
        # | A | abcd | -      | 01234x | -      | -      | 0123xx | efgh |
        # | B | abcd | 01xxxx | 01234x | 01234x | 012xxx | 0123xx | efgh |
        # | C | abcd | -      | 012345 | -      | -      | -      | efgh |
        # +---+------+--------+--------+--------+--------+--------+------+
        # No more gaps in rank 6; advance to rank 5
        # Try to move "01234x" from rank 3 for witness A
        self.assertTask("build column for rank", ["01234x", "3"], scheduler[6])
        self.assertTask("build column for rank", ["01234x", "4"], scheduler[7])
        self.assertTask("build column for rank", ["01234x", "5"], scheduler[8])
        # Can't move node with "01234x" from rank 3 to rank 5 because the rank 3 reading is shared
        #   with witness B, which is already present at rank 5
        # Try to move "012345" from rank 3 for witness C
        self.assertTask("build column for rank", ["012345", "3"], scheduler[9])
        self.assertTask("build column for rank", ["012345", "4"], scheduler[10])
        self.assertTask("build column for rank", ["012345", "5"], scheduler[11])
        # Strongest match for "012345" in witness C is current location (rank 3), so don't move
        # No more gaps at rank 5, advance to rank 4
        # Try to move "01234x" from rank 3 for witness A
        self.assertTask("build column for rank", ["01234x", "3"], scheduler[12])
        self.assertTask("build column for rank", ["01234x", "4"], scheduler[13])
        # Can't move node with "01234x" from rank 3 to rank 4 because the rank 3 reading is shared
        #   with witness B, which is already present at rank 4
        # Try to move "012345" from rank 3 for witness C
        self.assertTask("build column for rank", ["012345", "3"], scheduler[14])
        self.assertTask("build column for rank", ["012345", "4"], scheduler[15])
        # Tie for current rank 3 and rank 4; don't move
        # TODO: verify behavior in case of ties
        # No more gaps at rank 4, advance to rank 3
        # No gaps at rank 3, advance to rank 2
        # Try to move "abcd" from rank 1 for witness A
        self.assertTask("build column for rank", ["abcd", "1"], scheduler[16])
        self.assertTask("build column for rank", ["abcd", "2"], scheduler[17])
        # Closer match at current location; don't move
        # Try to move "abcd" from rank 1 for witness C
        self.assertTask("build column for rank", ["abcd", "1"], scheduler[18])
        self.assertTask("build column for rank", ["abcd", "2"], scheduler[19])
        # Closer match at current location; don't move
        # No more gaps at rank 2, advance to rank 1; no gaps at rank 1
        self.assertEquals(20, len(scheduler))
        expected = """\
+---+------+--------+--------+--------+--------+--------+------+
| A | abcd | -      | 01234x | -      | -      | 0123xx | efgh |
| B | abcd | 01xxxx | 01234x | 01234x | 012xxx | 0123xx | efgh |
| C | abcd | -      | 012345 | -      | -      | -      | efgh |
+---+------+--------+--------+--------+--------+--------+------+"""
        self.assertEqual(expected, alignment_table)

    def test_get_nodes_in_reverse_rank_order(self):
        collation = Collation()
        collation.add_plain_witness("A", "The koala jumped over the lazy wombat")
        collation.add_plain_witness("B", "The koala jumped over the industrious wombat")
        graph = collate(collation, output="graph", segmentation=False)
        ranking = VariantGraphRanking.of(graph)  # 9 ranks (0–8), 10 verticies
        generator = get_nodes_in_reverse_rank_order(9, ranking)  # starts on rank  9 - 1 = 8 = end node
        self.assertRaises(Exception, next(generator).label)  # no label on end vertex
        expected7 = 'wombat'
        self.assertEqual(expected7, next(generator).label)
        expected6 = {'industrious', 'lazy'}
        self.assertIn(next(generator).label, expected6)
        self.assertIn(next(generator).label, expected6)
        expected5 = 'the'
        self.assertEqual(expected5, next(generator).label)
        expected4 = 'over'
        self.assertEqual(expected4, next(generator).label)
        expected3 = 'jumped'
        self.assertEqual(expected3, next(generator).label)
        expected2 = 'koala'
        self.assertEqual(expected2, next(generator).label)
        expected1 = 'The'
        self.assertEqual(expected1, next(generator).label)
        self.assertRaises(StopIteration, lambda: next(generator))  # no label on start vertex

    def test_next_matching_node_filter(self):
        collation = Collation()
        collation.add_plain_witness("A", "The koala jumped over the red pink grey blue wombat")
        collation.add_plain_witness("B", "The koala jumped over the gray wombat")
        graph = collate(collation, output="graph", segmentation=False)
        ranking = VariantGraphRanking.of(graph)  # 9 ranks (0–8, not counting end), 10 verticies
        test_rank = 9
        expected_rank = 6
        expected_label = "gray"
        (returned_rank, returned_node) = find_prior_node('B', test_rank, ranking)
        self.assertEqual(expected_rank, returned_rank)
        self.assertEqual(expected_label, returned_node.label)

    def test_near_matching_node_filter_start_node(self):
        collation = Collation()
        collation.add_plain_witness("A", "koala and the bettong jumped over wombat")
        collation.add_plain_witness("B", "bettong jumped over wombat")
        graph = collate(collation, output="graph", segmentation=False)
        ranking = VariantGraphRanking.of(graph)  # 9 ranks (0–8, not counting end), 10 verticies
        test_rank = 3
        (returned_rank, returned_node) = find_prior_node('B', test_rank, ranking)
        self.assertIsNone(returned_rank)
        self.assertIsNone(returned_node)

    def test_near_matching_dict_comprehension_ranking_table(self):
        collation = Collation()
        collation.add_plain_witness("A", "blue no green gray yellow purple koala and the 0123 0120 0100 0000 wombat")
        collation.add_plain_witness("B", "numerous grey koala and the 010x wombat")
        graph = collate(collation, output="graph", segmentation=False)
        ranking = VariantGraphRanking.of(graph)  # 15 ranks (0–14, not counting end), 19 verticies
        # test_rank is the location of the gap
        # expected_rank is where the node to be moved is found originally
        test_rank_1 = 13
        expected_rank_1 = 10
        expected_label_1 = "010x"
        (returned_rank_1, returned_node_1) = find_prior_node('B', test_rank_1, ranking)
        self.assertEqual(expected_rank_1, returned_rank_1)
        self.assertEqual(expected_label_1, returned_node_1.label)
        test_rank_2 = 6
        expected_rank_2 = 2
        expected_label_2 = "grey"
        (returned_rank_2, returned_node_2) = find_prior_node('B', test_rank_2, ranking)
        self.assertEqual(expected_rank_2, returned_rank_2)
        self.assertEqual(expected_label_2, returned_node_2.label)
        test_rank_3 = 2
        expected_rank_3 = 1
        expected_label_3 = "numerous"
        (returned_rank_3, returned_node_3) = find_prior_node('B', test_rank_3, ranking)
        self.assertEqual(expected_rank_3, returned_rank_3)
        self.assertEqual(expected_label_3, returned_node_3.label)
        test_rank_4 = 1
        (returned_rank, returned_node) = find_prior_node('B', test_rank_4, ranking)
        self.assertIsNone(returned_rank)
        self.assertIsNone(returned_node)


if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testOmission']
    unittest.main()
