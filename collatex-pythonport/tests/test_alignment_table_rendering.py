'''
Created on Jun 11, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from collatex.collatex_dekker_algorithm import Collation, collate


class Test(unittest.TestCase):

    def testPlainTableRendering(self):
        collation = Collation()
        collation.add_witness("A", "This very quick very quick brown wombat")
        collation.add_witness("B", "That very quick brown koala")
        collation.add_witness("C", "That very quick brown kangaroo")
        expected_output = """+---+-----------------+------------------+----------+
| A | This very quick | very quick brown | wombat   |
| B | That            | very quick brown | koala    |
| C | That            | very quick brown | kangaroo |
+---+-----------------+------------------+----------+"""
        plain_text_output = collate(collation).get_string()
        self.assertEquals(expected_output, plain_text_output)

    def testPlainTableRenderingNoParallelSegmentation(self):
        collation = Collation()
        collation.add_witness("A", "This very quick very quick brown wombat")
        collation.add_witness("B", "That very quick brown koala")
        collation.add_witness("C", "That very quick brown kangaroo")
        expected_output = """+---+------+------+-------+------+-------+-------+----------+
| A | This | very | quick | very | quick | brown | wombat   |
| B | That | -    | -     | very | quick | brown | koala    |
| C | That | -    | -     | very | quick | brown | kangaroo |
+---+------+------+-------+------+-------+-------+----------+"""
        plain_text_output = collate(collation, segmentation=False).get_string()
        self.assertEquals(expected_output, plain_text_output)

    def testJSONAlignmentTableRendering(self):
        collation = Collation()
        collation.add_witness("A", "This very quick very quick brown wombat")
        collation.add_witness("B", "That very quick brown koala")
        collation.add_witness("C", "That very quick brown kangaroo")
        expected_output = '{\n  "witnesses": [\n    "A", \n    "B", \n    "C"\n  ], \n  "table": [\n    [\n      [\n        "This very quick"\n      ], \n      [\n        "very quick brown"\n      ], \n      [\n        "wombat"\n      ]\n    ], \n    [\n      [\n        "That"\n      ], \n      [\n        "very quick brown"\n      ], \n      [\n        "koala"\n      ]\n    ], \n    [\n      [\n        "That"\n      ], \n      [\n        "very quick brown"\n      ], \n      [\n        "kangaroo"\n      ]\n    ]\n  ]\n}'
        json = collate(collation, output="json")
        self.assertEquals(expected_output, json)

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()