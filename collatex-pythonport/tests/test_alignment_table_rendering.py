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
        self.assertEquals(expected_output, plain_text_output)

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
        self.assertEquals(expected_output, plain_text_output)

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
        self.assertEquals(expected_output, plain_text_output)

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
        self.assertEquals(expected_output, plain_text_output)

    def testJSONAlignmentTableRendering(self):
        collation = Collation()
        collation.add_plain_witness("A", "This very quick very quick brown wombat")
        collation.add_plain_witness("B", "That very quick brown koala")
        collation.add_plain_witness("C", "That very quick brown kangaroo")
        expected_output = {"table": [[[{"n": "This", "t": "This "}, {"n": "very", "t": "very "}, {"n": "quick", "t": "quick "}], [{"n": "very", "t": "very "}, {"n": "quick", "t": "quick "}, {"n": "brown", "t": "brown "}], [{"n": "wombat", "t": "wombat"}]], [[{"n": "That", "t": "That "}], [{"n": "very", "t": "very "}, {"n": "quick", "t": "quick "}, {"n": "brown", "t": "brown "}], [{"n": "koala", "t": "koala"}]], [[{"n": "That", "t": "That "}], [{"n": "very", "t": "very "}, {"n": "quick", "t": "quick "}, {"n": "brown", "t": "brown "}], [{"n": "kangaroo", "t": "kangaroo"}]]], "witnesses": ["A", "B", "C"]}
        json_out = collate(collation, output="json")
        self.assertEquals(expected_output, json.loads(json_out))

    def testJSONAlignmentTableRenderingNoSegmentation(self):
        collation = Collation()
        collation.add_plain_witness("A", "This very quick very quick brown wombat")
        collation.add_plain_witness("B", "That very quick brown koala")
        collation.add_plain_witness("C", "That very quick brown kangaroo")
        expected_output = {"table": [[[{"n": "This", "t": "This "}], [{"n": "very", "t": "very "}], [{"n": "quick", "t": "quick "}], [{"n": "very", "t": "very "}], [{"n": "quick", "t": "quick "}], [{"n": "brown", "t": "brown "}], [{"n": "wombat", "t": "wombat"}]], [[{"n": "That", "t": "That "}], None, None, [{"n": "very", "t": "very "}], [{"n": "quick", "t": "quick "}], [{"n": "brown", "t": "brown "}], [{"n": "koala", "t": "koala"}]], [[{"n": "That", "t": "That "}], None, None, [{"n": "very", "t": "very "}], [{"n": "quick", "t": "quick "}], [{"n": "brown", "t": "brown "}], [{"n": "kangaroo", "t": "kangaroo"}]]], "witnesses": ["A", "B", "C"]}
        json_out = collate(collation, output="json", segmentation=False)
        self.assertEquals(expected_output, json.loads(json_out))

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
