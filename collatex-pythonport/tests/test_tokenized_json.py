'''
Created on Jun 11, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from collatex.collatex_core import Witness, AlignmentTable, Row
from collatex.collatex_dekker_algorithm import Collation, collate,\
    visualizeTableHorizontal, collate_pretokenized_json


class Test(unittest.TestCase):


    def testTokenizedJSON(self):
        json = {
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
        tokenized_at = collate_pretokenized_json(json)
        prettytable = visualizeTableHorizontal(tokenized_at)
        print(prettytable)
        pass
                