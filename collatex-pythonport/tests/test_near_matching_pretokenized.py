'''
Created on Sep 12, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from tests import unit_disabled
from collatex.core_functions import *


class Test(unittest.TestCase):
    def setUp(self):
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
        self.c = Collation.create_from_dict(json_in)

    def test_exact_matching(self):
        result = collate(self.c, segmentation=False)
        self.assertEqual(["I", "bought", "this", "glass", ",", "because", "it", "matches", "those", "dinner", "plates", "."],
                          result.rows[0].to_list())
        self.assertEqual(["I", "bought", "-", "-", "-", "-", "-", "-", "those", "glasses", "-", "."], result.rows[1].to_list())

    def test_near_matching(self):
        result = collate(self.c, segmentation=False, near_match=True)
        self.assertEqual(["I", "bought", "this", "glass", ",", "because", "it", "matches", "those", "dinner", "plates", "."],
                          result.rows[0].to_list())
        self.assertEqual(["I", "bought", "those", "glasses", "-", "-", "-", "-", "-", "-", "-", "."], result.rows[1].to_list())

    # Re-enable this one if segmented output is ever supported on tokenized collation
    @unit_disabled
    def test_near_matching_segmented(self):
        result = collate(self.c, near_match=True, segmentation=True)
        self.assertEqual(["I bought", "this glass, because it matches those dinner plates."],
                          result.rows[0].to_list())
        self.assertEqual(["I bought", "those glasses."], result.rows[1].to_list())


if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testOmission']
    unittest.main()
