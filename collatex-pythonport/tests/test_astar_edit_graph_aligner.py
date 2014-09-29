'''
Created on Sep 29, 2014

@author: ronald
'''
import unittest
from collatex.collatex_dekker_algorithm import Collation, collate
from collatex.edit_graph_aligner import EditGraphAligner
from collatex.collatex_core import VariantGraph


class Test(unittest.TestCase):

    def test_longer_example(self):
        collation = Collation()
        collation.add_witness("A", "The quick brown fox jumps over the dog.")
        collation.add_witness("B", "The brown fox jumps over the lazy dog.")
        collate(collation)

#     # we need to introduce a gap here
#     def testOmission(self):
#         collation = Collation()
#         collation.add_witness("A", "a b c")
#         collation.add_witness("B", "b c")
#         aligner = EditGraphAligner(collation)
#         graph = VariantGraph()
#         aligner.collate(graph, collation)
#         table = aligner.table
#         aligner._debug_edit_graph_table(table)
#         self.assertEqual(0, table[0][0].g)
#         self.assertEqual(-1, table[0][1].g)
#         self.assertEqual(-2, table[0][2].g)
#         self.assertEqual(-3, table[0][3].g)
#         self.assertEqual(-1, table[1][0].g)
#         self.assertEqual(-2, table[1][1].g)
#         self.assertEqual(-1, table[1][2].g)
#         self.assertEqual(-2, table[1][3].g)
#         self.assertEqual(-2, table[2][0].g)
#         self.assertEqual(-3, table[2][1].g)
#         self.assertEqual(-2, table[2][2].g)
#         self.assertEqual(-1, table[2][3].g)


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()