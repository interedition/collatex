'''
Created on Sep 29, 2014

@author: ronald
'''
import unittest
from collatex import Collation, collate
from collatex.edit_graph_aligner import EditGraphAligner
from collatex.core_classes import VariantGraph
from collatex.experimental_astar_aligner import ExperimentalAstarAligner


class Test(unittest.TestCase):

    def test_heuristic_function_everything_equals(self):
        collation = Collation()
        collation.add_plain_witness("A", "everything equal")
        collation.add_plain_witness("B", "everything equal")
        aligner = ExperimentalAstarAligner(collation)
        aligner._create_heuristic_table(collation.witnesses[0].tokens(), collation.witnesses[1])
        self.assertEqual([0, 1, 2], aligner.heuristic_table[0])
        self.assertEqual([1, 0, 1], aligner.heuristic_table[1])
        self.assertEqual([2, 1, 0], aligner.heuristic_table[2])
        pass

    # #NOTE: The heuristic function may be more optimistic (I.e. results may be worse; cost may be higher than expected; however
    # #NOTE: cost may not be lower than expected.
    # def test_heuristic_function_we_must_take_one_gap(self):
    #     collation = Collation()
    #     collation.add_plain_witness("A", "one large gap")
    #     collation.add_plain_witness("B", "one small gap")
    #     aligner = ExperimentalAstarAligner(collation)
    #     aligner._create_heuristic_table(collation.witnesses[0].tokens(), collation.witnesses[1])
    #
    #
    #
    #     #
    #     #
    #     print(aligner.heuristic_table[0])
    #     print(aligner.heuristic_table[1])
    #     print(aligner.heuristic_table[2])
















#     def test_longer_example(self):
#         collation = Collation()
#         collation.add_plain_witness("A", "The quick brown fox jumps over the dog.")
#         collation.add_plain_witness("B", "The brown fox jumps over the lazy dog.")
#         collate(collation)
#
# #     # we need to introduce a gap here
#     def testOmission(self):
#         collation = Collation()
#         collation.add_plain_witness("A", "a b c")
#         collation.add_plain_witness("B", "b c")
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