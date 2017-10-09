"""
Created on Jun 11, 2014

@author: Ronald Haentjens Dekker
"""
import json
import unittest
from collatex.core_functions import Collation, collate


class Test(unittest.TestCase):

    def testPlainTableRendering(self):
        collation = Collation()
        collation.add_plain_witness("A", "This very quick very quick brown wombat")
        collation.add_plain_witness("B", "That very quick brown koala")
        collation.add_plain_witness("C", "That very quick brown kangaroo")
        expected_output = """\
+---+-----------------+------------------+----------+
| A | This very quick | very quick brown | wombat   |
| B | That            | very quick brown | koala    |
| C | That            | very quick brown | kangaroo |
+---+-----------------+------------------+----------+"""
        plain_text_output = str(collate(collation))
        self.assertEqual(expected_output, plain_text_output)

    def testPlainTableRenderingNoParallelSegmentation(self):
        collation = Collation()
        collation.add_plain_witness("A", "This very quick very quick brown wombat")
        collation.add_plain_witness("B", "That very quick brown koala")
        collation.add_plain_witness("C", "That very quick brown kangaroo")
        expected_output = """\
+---+------+------+-------+------+-------+-------+----------+
| A | This | very | quick | very | quick | brown | wombat   |
| B | That | -    | -     | very | quick | brown | koala    |
| C | That | -    | -     | very | quick | brown | kangaroo |
+---+------+------+-------+------+-------+-------+----------+"""
        plain_text_output = str(collate(collation, segmentation=False))
        self.assertEqual(expected_output, plain_text_output)

    def testPlainTableRenderingVertical(self):
        collation = Collation()
        collation.add_plain_witness("A", "This very quick very quick brown wombat")
        collation.add_plain_witness("B", "That very quick brown koala")
        collation.add_plain_witness("C", "That very quick brown kangaroo")
        expected_output = """\
+------------------+------------------+------------------+
|        A         |        B         |        C         |
+------------------+------------------+------------------+
| This very quick  |       That       |       That       |
+------------------+------------------+------------------+
| very quick brown | very quick brown | very quick brown |
+------------------+------------------+------------------+
|      wombat      |      koala       |     kangaroo     |
+------------------+------------------+------------------+"""
        plain_text_output = str(collate(collation, layout="vertical"))
        self.assertEqual(expected_output, plain_text_output)

    def testPlainTableRenderingVerticalNoSegmentation(self):
        collation = Collation()
        collation.add_plain_witness("A", "This very quick very quick brown wombat")
        collation.add_plain_witness("B", "That very quick brown koala")
        collation.add_plain_witness("C", "That very quick brown kangaroo")
        expected_output = """\
+--------+-------+----------+
|   A    |   B   |    C     |
+--------+-------+----------+
|  This  |  That |   That   |
+--------+-------+----------+
|  very  |   -   |    -     |
+--------+-------+----------+
| quick  |   -   |    -     |
+--------+-------+----------+
|  very  |  very |   very   |
+--------+-------+----------+
| quick  | quick |  quick   |
+--------+-------+----------+
| brown  | brown |  brown   |
+--------+-------+----------+
| wombat | koala | kangaroo |
+--------+-------+----------+"""
        plain_text_output = str(collate(collation, layout="vertical", segmentation=None))
        self.assertEqual(expected_output, plain_text_output)

    def testJSONAlignmentTableRendering(self):
        collation = Collation()
        collation.add_plain_witness("A", "This very quick very quick brown wombat")
        collation.add_plain_witness("B", "That very quick brown koala")
        collation.add_plain_witness("C", "That very quick brown kangaroo")
        expected_output = {"table": [[[{"n": "This", "_sigil": "A", "t": "This ", "_token_array_position": 0},
                                       {"n": "very", "_sigil": "A", "t": "very ", "_token_array_position": 1},
                                       {"n": "quick", "_sigil": "A", "t": "quick ", "_token_array_position": 2}],
                                      [{"n": "very", "_sigil": "A", "t": "very ", "_token_array_position": 3},
                                       {"n": "quick", "_sigil": "A", "t": "quick ", "_token_array_position": 4},
                                       {"n": "brown", "_sigil": "A", "t": "brown ", "_token_array_position": 5}],
                                      [{"n": "wombat", "_sigil": "A", "t": "wombat", "_token_array_position": 6}]],

                                     [[{"n": "That", "_sigil": "B", "t": "That ", "_token_array_position": 8}],
                                      [{"n": "very", "_sigil": "B", "t": "very ", "_token_array_position": 9},
                                       {"n": "quick", "_sigil": "B", "t": "quick ", "_token_array_position": 10},
                                       {"n": "brown", "_sigil": "B", "t": "brown ", "_token_array_position": 11}],
                                      [{"n": "koala", "_sigil": "B", "t": "koala", "_token_array_position": 12}]],

                                     [[{"n": "That", "_sigil": "C", "t": "That ", "_token_array_position": 14}],
                                      [{"n": "very", "_sigil": "C", "t": "very ", "_token_array_position": 15},
                                       {"n": "quick", "_sigil": "C", "t": "quick ", "_token_array_position": 16},
                                       {"n": "brown", "_sigil": "C", "t": "brown ", "_token_array_position": 17}],
                                      [{"n": "kangaroo", "_sigil": "C", "t": "kangaroo", "_token_array_position": 18}]]],
                           "witnesses": ["A", "B", "C"]}
        json_out = collate(collation, output="json")
        print(json_out)
        self.assertEqual(expected_output, json.loads(json_out))

    def testJSONAlignmentTableRenderingNoSegmentation(self):
        collation = Collation()
        collation.add_plain_witness("A", "This very quick very quick brown wombat")
        collation.add_plain_witness("B", "That very quick brown koala")
        collation.add_plain_witness("C", "That very quick brown kangaroo")
        expected_output = {"table": [[[{"_sigil": "A", "_token_array_position": 0, "n": "This", "t": "This "}],
                                      [{"_sigil": "A", "_token_array_position": 1, "n": "very", "t": "very "}],
                                      [{"_sigil": "A", "_token_array_position": 2, "n": "quick", "t": "quick "}],
                                      [{"_sigil": "A", "_token_array_position": 3, "n": "very", "t": "very "}],
                                      [{"_sigil": "A", "_token_array_position": 4, "n": "quick", "t": "quick "}],
                                      [{"_sigil": "A", "_token_array_position": 5, "n": "brown", "t": "brown "}],
                                      [{"_sigil": "A", "_token_array_position": 6, "n": "wombat", "t": "wombat"}]],

                                     [[{"_sigil": "B", "_token_array_position": 8, "n": "That", "t": "That "}],
                                      None,
                                      None,
                                      [{"_sigil": "B", "_token_array_position": 9, "n": "very", "t": "very "}],
                                      [{"_sigil": "B", "_token_array_position": 10, "n": "quick", "t": "quick "}],
                                      [{"_sigil": "B", "_token_array_position": 11, "n": "brown", "t": "brown "}],
                                      [{"_sigil": "B", "_token_array_position": 12, "n": "koala", "t": "koala"}]],

                                     [[{"_sigil": "C", "_token_array_position": 14, "n": "That", "t": "That "}],
                                      None,
                                      None,
                                      [{"_sigil": "C", "_token_array_position": 15, "n": "very", "t": "very "}],
                                      [{"_sigil": "C", "_token_array_position": 16, "n": "quick", "t": "quick "}],
                                      [{"_sigil": "C", "_token_array_position": 17, "n": "brown", "t": "brown "}],
                                      [{"_sigil": "C", "_token_array_position": 18, "n": "kangaroo", "t": "kangaroo"}]]],
                           "witnesses": ["A", "B", "C"]}
        json_out = collate(collation, output="json", segmentation=False)
        self.assertEqual(expected_output, json.loads(json_out))

    def testColumnStatusInAlignmentTable(self):
        collation = Collation()
        collation.add_plain_witness("A", "The quick brown fox jumps over the dog.")
        collation.add_plain_witness("B", "The brown fox jumps over the lazy dog.")
        alignment_table = collate(collation)
        status_array = []
        for column in alignment_table.columns:
            status_array.append(column.variant)
        self.assertEqual([False, True, False, True, False], status_array)
        collation.add_plain_witness("C", "The brown fox walks around the lazy dog.")
        collate(collation)
        alignment_table = collate(collation)
        status_array = []
        for column in alignment_table.columns:
            status_array.append(column.variant)
        self.assertEqual([False, True, False, True, False, True, False], status_array)

if __name__ == "__main__":
    unittest.main()
