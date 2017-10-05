'''
Created on Jun 11, 2014

@author: Ronald Haentjens Dekker
'''
import json
import unittest

from collatex import collate
from collatex.exceptions import *


class Test(unittest.TestCase):

    def testJSONOutputPretokenizedJSON(self):
        json_in = {
      "witnesses" : [
        {
          "id" : "A",
          "tokens" : [
              { "t" : "A", "ref" : 123 },
              { "t" : "black" , "adj" : True },
              { "t" : "cat", "id" : "xyz" }
          ]
        },
        {
          "id" : "B",
          "tokens" : [
              { "t" : "A" },
              { "t" : "white" , "adj" : True },
              { "t" : "kitten.", "n" : "cat" }
          ]
        }
      ]
    }

        # expected_json = {"table": [[[{"ref": 123, "t": "A"}], [{"adj": True, "t": "black"}], [{"id": "xyz", "t": "cat"}]], [[{"t": "A"}], [{"adj": True, "t": "white"}], [{"n": "cat", "t": "kitten."}]]], "witnesses": ["A", "B"]}
        expected_json = {"table": [[[{"ref": 123, "_sigil": "A", "t": "A", "_token_array_position": 0}], [{"adj": True, "_sigil": "A", "t": "black", "_token_array_position": 1}], [{"id": "xyz", "_sigil": "A", "t": "cat", "_token_array_position": 2}]], [[{"_sigil": "B", "t": "A", "_token_array_position": 4}], [{"adj": True, "_sigil": "B", "t": "white", "_token_array_position": 5}], [{"n": "cat", "_sigil": "B", "t": "kitten.", "_token_array_position": 6}]]], "witnesses": ["A", "B"]}
        json_out = collate(json_in, output="json")
        self.assertEqual(expected_json, json.loads(json_out))

    def testJSONOutput_empty_cells_in_output(self):
        json_in = {
      "witnesses" : [
        {
          "id" : "A",
          "tokens" : [
              { "t" : "A", "ref" : 123 },
              { "t" : "black" , "adj" : True },
              { "t" : "cat", "id" : "xyz" }
          ]
        },
        {
          "id" : "B",
          "tokens" : [
              { "t" : "A" },
              { "t" : "kitten.", "n" : "cat" }
          ]
        }
      ]
    }
        # expected_json = {"table": [[[{"ref": 123, "t": "A"}], [{"adj": True, "t": "black"}], [{"id": "xyz", "t": "cat"}]], [[{"t": "A"}], None, [{"n": "cat", "t": "kitten."}]]], "witnesses": ["A", "B"]}
        expected_json = {"table": [[[{"ref": 123, "_sigil": "A", "t": "A", "_token_array_position": 0}], [{"adj": True, "_sigil": "A", "t": "black", "_token_array_position": 1}], [{"id": "xyz", "_sigil": "A", "t": "cat", "_token_array_position": 2}]], [[{"_sigil": "B", "t": "A", "_token_array_position": 4}], None, [{"n": "cat", "_sigil": "B", "t": "kitten.", "_token_array_position": 5}]]], "witnesses": ["A", "B"]}
        json_out = collate(json_in, output="json")
        self.assertEqual(expected_json, json.loads(json_out))

    def testHTMLOutputPretokenizedJSON(self):
        json_in = {
      "witnesses" : [
        {
          "id" : "A",
          "tokens" : [
              { "t" : "A", "ref" : 123 },
              { "t" : "black" , "adj" : True },
              { "t" : "cat", "id" : "xyz" }
          ]
        },
        {
          "id" : "B",
          "tokens" : [
              { "t" : "A" },
              { "t" : "white" , "adj" : True },
              { "t" : "kitten.", "n" : "cat" }
          ]
        }
      ]
    }
        expected_plain_table = """\
+---+---+-------+---------+
| A | A | black | cat     |
| B | A | white | kitten. |
+---+---+-------+---------+"""
        plain_table = str(collate(json_in, output="table"))
        self.assertEqual(expected_plain_table, plain_table)

    def testHTMLOutputVerticalLayoutPretokenizedJSON(self):
        json_in = {
      "witnesses" : [
        {
          "id" : "A",
          "tokens" : [
              { "t" : "A", "ref" : 123 },
              { "t" : "black" , "adj" : True },
              { "t" : "cat", "id" : "xyz" }
          ]
        },
        {
          "id" : "B",
          "tokens" : [
              { "t" : "A" },
              { "t" : "white" , "adj" : True },
              { "t" : "kitten.", "n" : "cat" }
          ]
        }
      ]
    }
        expected_output = """\
+-------+---------+
|   A   |    B    |
+-------+---------+
|   A   |    A    |
+-------+---------+
| black |  white  |
+-------+---------+
|  cat  | kitten. |
+-------+---------+"""
        plain_text_output = str(collate(json_in, layout="vertical"))
        self.assertEqual(expected_output, plain_text_output)

    def testSegmentationPretokenizedJSON(self):
        json_in = {
      "witnesses" : [
        {
          "id" : "A",
          "tokens" : [
              { "t" : "A", "ref" : 123 },
              { "t" : "black" , "adj" : True },
              { "t" : "cat", "id" : "xyz" }
          ]
        },
        {
          "id" : "B",
          "tokens" : [
              { "t" : "A" },
              { "t" : "white" , "adj" : True },
              { "t" : "stripy", "adj" : True },
              { "t" : "kitten.", "n" : "cat" }
          ]
        }
      ]
    }
        # json_expected = {"table": [[[{"ref": 123, "t": "A"}], [{"adj": True, "t": "black"}], [{"id": "xyz", "t": "cat"}]], [[{"t": "A"}], [{"adj": True, "t": "white"}, {"adj": True, "t": "stripy"}], [{"n": "cat", "t": "kitten."}]]], "witnesses": ["A", "B"]}
        json_expected = {"table": [[[{"ref": 123, "_sigil": "A", "t": "A", "_token_array_position": 0}], [{"adj": True, "_sigil": "A", "t": "black", "_token_array_position": 1}], [{"id": "xyz", "_sigil": "A", "t": "cat", "_token_array_position": 2}]], [[{"_sigil": "B", "t": "A", "_token_array_position": 4}], [{"adj": True, "_sigil": "B", "t": "white", "_token_array_position": 5}, {"adj": True, "_sigil": "B", "t": "stripy", "_token_array_position": 6}], [{"n": "cat", "_sigil": "B", "t": "kitten.", "_token_array_position": 7}]]], "witnesses": ["A", "B"]}
        json_out = collate(json_in, output="json", segmentation=True)
        self.assertEqual(json_expected, json.loads(json_out))

if __name__ == '__main__':
    unittest.main()
