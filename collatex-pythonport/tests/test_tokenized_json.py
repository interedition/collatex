'''
Created on Jun 11, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from collatex.collatex_dekker_algorithm import collate_pretokenized_json, alignmentTableToJSON
    

class Test(unittest.TestCase):


    def testTokenizedJSON(self):
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
        expected_json = '{"witnesses": ["A", "B"], "table": [[[{"ref": 123, "t": "A"}], [{"adj": true, "t": "black"}], [{"t": "cat", "id": "xyz"}]], [[{"t": "A"}], [{"adj": true, "t": "white"}], [{"t": "kitten.", "n": "cat"}]]]}'
        tokenized_at = collate_pretokenized_json(json_in)
        json_out = alignmentTableToJSON(tokenized_at)
        self.assertEqual(expected_json, json_out)

    def test_empty_cells_in_output(self):
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
        expected_json = '{"witnesses": ["A", "B"], "table": [[[{"ref": 123, "t": "A"}], [{"adj": true, "t": "black"}], [{"t": "cat", "id": "xyz"}]], [[{"t": "A"}], [{"t": "-"}], [{"t": "kitten.", "n": "cat"}]]]}'
        tokenized_at = collate_pretokenized_json(json_in)
        #print(visualizeTableHorizontal(tokenized_at))
        json_out = alignmentTableToJSON(tokenized_at)
        #print(json_out)
        self.assertEqual(expected_json, json_out)
