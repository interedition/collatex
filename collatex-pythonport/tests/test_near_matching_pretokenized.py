'''
Created on Sep 12, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from tests import unit_disabled
from collatex.core_functions import collate


class Test(unittest.TestCase):
    json_in = {
      "witnesses" : [
        {
          "id" : "A",
          "tokens" : [
              { "t" : "I", "ref" : 123 },
              { "t" : "bought" , "adj" : True },
              { "t" : "this", "id" : "x3" },
              { "t" : "glass", "id" : "x4" },
              { "t" : ",", "type" : "punct" },
              { "t" : "because", "id" : "x5" },
              { "t" : "it", "id" : "x6" },
              { "t" : "matches" },
              { "t" : "those", "id" : "x7" },
              { "t" : "dinner", "id" : "x8" },
              { "t" : "plates", "id" : "x9" },
              { "t" : ".", "type" : "punct" }
          ]
        },
        {
          "id" : "B",
          "tokens" : [
              { "t" : "I" },
              { "t" : "bought" , "adj" : True },
              { "t" : "those", "id" : "abc" },
              { "t" : "glasses", "id" : "xyz" },
              { "t" : ".", "type" : "punct" }
          ]
        }
      ]
    }

    def test_exact_matching(self):
        result = collate(self.json_in, segmentation=False)
        self.assertEquals(["I", "bought", "this", "glass", ",", "because", "it", "matches", "those", "dinner", "plates", "."],
                          result.rows[0].to_list_of_strings())
        self.assertEquals(["I", "bought", None, None, None, None, None, None, "those", "glasses", None, "."], result.rows[1].to_list_of_strings())

    def test_near_matching(self):
        result = collate(self.json_in, near_match=True, segmentation=False)
        self.assertEquals(["I", "bought", "this", "glass", ",", "because", "it", "matches", "those", "dinner", "plates", "."],
                          result.rows[0].to_list_of_strings())
        self.assertEquals(["I", "bought", "those", "glasses", None, None, None, None, None, None, None, "."], result.rows[1].to_list_of_strings())

    # Re-enable this one if segmented output is ever supported on tokenized collation
    @unit_disabled
    def test_near_matching_segmented(self):
        result = collate(self.json_in, near_match=True, segmentation=True)
        self.assertEquals(["I bought", "this glass, because it matches those dinner plates."],
                          result.rows[0].to_list_of_strings())
        self.assertEquals(["I bought", "those glasses."], result.rows[1].to_list_of_strings())


if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testOmission']
    unittest.main()
