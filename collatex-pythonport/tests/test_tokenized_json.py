'''
Created on Jun 11, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from collatex.collatex_dekker_algorithm import collate_pretokenized_json
    

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
        expected_json = '{\n  "witnesses": [\n    "A", \n    "B"\n  ], \n  "table": [\n    [\n      [\n        {\n          "ref": 123, \n          "t": "A"\n        }\n      ], \n      [\n        {\n          "adj": true, \n          "t": "black"\n        }\n      ], \n      [\n        {\n          "t": "cat", \n          "id": "xyz"\n        }\n      ]\n    ], \n    [\n      [\n        {\n          "t": "A"\n        }\n      ], \n      [\n        {\n          "adj": true, \n          "t": "white"\n        }\n      ], \n      [\n        {\n          "t": "kitten.", \n          "n": "cat"\n        }\n      ]\n    ]\n  ]\n}'
        json_out = collate_pretokenized_json(json_in, output="json")
        self.assertEqual(expected_json, json_out)

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
        expected_json = '{\n  "witnesses": [\n    "A", \n    "B"\n  ], \n  "table": [\n    [\n      [\n        {\n          "ref": 123, \n          "t": "A"\n        }\n      ], \n      [\n        {\n          "adj": true, \n          "t": "black"\n        }\n      ], \n      [\n        {\n          "t": "cat", \n          "id": "xyz"\n        }\n      ]\n    ], \n    [\n      [\n        {\n          "t": "A"\n        }\n      ], \n      [\n        {\n          "t": "-"\n        }\n      ], \n      [\n        {\n          "t": "kitten.", \n          "n": "cat"\n        }\n      ]\n    ]\n  ]\n}'
        json_out = collate_pretokenized_json(json_in, output="json")
        self.assertEqual(expected_json, json_out)

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
        expected_plain_table = """+---+---+-------+---------+
| A | A | black | cat     |
| B | A | white | kitten. |
+---+---+-------+---------+"""
        plain_table = collate_pretokenized_json(json_in, output="table").get_string()
        self.assertEqual(expected_plain_table, plain_table)
