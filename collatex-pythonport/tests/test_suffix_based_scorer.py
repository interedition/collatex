import unittest
from ClusterShell.RangeSet import RangeSet
from collatex import Collation
from collatex.extended_suffix_array import Block
from collatex.core_functions import collate
from collatex.suffix_based_scorer import Scorer
from collatex.tokenindex import TokenIndex

__author__ = 'ronalddekker'


class Test(unittest.TestCase):

    def test_non_overlapping_blocks_Hermans(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_plain_witness("W2", "a b c d F g h i ! q r s t")
        algorithm = Scorer(TokenIndex.create_token_index(collation))
        blocks = algorithm._get_non_overlapping_repeating_blocks()
        self.assertIn(Block(RangeSet("0-8, 16-24")), blocks) # a b c d F g h i !
        self.assertIn(Block(RangeSet("11-14, 25-28")), blocks) # q r s t

    def test_blocks_Hermans_case_three_witnesses(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_plain_witness("W2", "a b c d F g h i ! q r s t")
        collation.add_plain_witness("W3", "a b c d E g h i ! q r s t")
        algorithm = Scorer(TokenIndex.create_token_index(collation))
        blocks = algorithm._get_non_overlapping_repeating_blocks()
        self.assertIn(Block(RangeSet("0-3, 16-19, 30-33")), blocks) # a b c d
        self.assertIn(Block(RangeSet("5-7, 21-23, 35-37")), blocks) # g h i
        self.assertIn(Block(RangeSet("10-14, 24-28, 38-42")), blocks) # ! q r s t
        self.assertIn(Block(RangeSet("4, 20")), blocks) # F

    def test_non_overlapping_blocks_overlap_case(self):
        collation = Collation()
        collation.add_plain_witness("W1", "in the in the bleach")
        collation.add_plain_witness("W2", "in the in the bleach in the")
        algorithm = Scorer(TokenIndex.create_token_index(collation))
        blocks = algorithm._get_non_overlapping_repeating_blocks()
        self.assertIn(Block(RangeSet("0-4, 6-10")), blocks) # in the in the bleach

    def test_2(self):
        collation = Collation()
        collation.add_plain_witness("W1", "in the in the bleach")
        collation.add_plain_witness("W2", "in the in the bleach in the")
        collation.add_plain_witness("W3", "in the in the bleach in the")
        algorithm = Scorer(TokenIndex.create_token_index(collation))
        blocks = algorithm._get_non_overlapping_repeating_blocks()
        self.assertIn(Block(RangeSet("0-4, 6-10, 14-18")), blocks) # in the in the bleach
        self.assertIn(Block(RangeSet("11-12, 19-20")), blocks) # in the


    def match_properties(self, token1_data, token2_data):
        return token1_data == token2_data

    def test_scoring_with_properties_filter(self):
        json_in = {
          "witnesses" : [
            {
              "id" : "A",
              "tokens" : [
                  { "t" : "filler1" },
                  { "t" : "token" },
              ]
            },
            {
              "id" : "B",
              "tokens" : [
                  { "t" : "token", "rend" : "b" },
                  { "t" : "filler2" },
              ]
            }
          ]
        }

        expected_output = """+---+---------+-------+---------+
| A | filler1 | token | -       |
| B | -       | token | filler2 |
+---+---------+-------+---------+"""
        alignment_table = collate(json_in, segmentation=False)
        self.assertEqual(expected_output, str(alignment_table))

        expected_output = """+---+---------+---------+
| A | filler1 | token   |
| B | token   | filler2 |
+---+---------+---------+"""
        alignment_table = collate(json_in, properties_filter=self.match_properties, segmentation=False)
        self.assertEqual(expected_output, str(alignment_table))

