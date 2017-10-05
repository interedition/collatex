'''
Created on Aug 4, 2014

@author: Ronald
'''
import unittest
from tests import unit_disabled
from collatex.core_functions import VariantGraph, collate
from collatex.edit_graph_aligner import EditGraphAligner
from collatex import Collation


class Test(unittest.TestCase):
    # global score
    def assertRow(self, expected, cell_data):
        actual = []
        for cell in cell_data:
            actual.append(cell.g)
        self.assertEqual(expected, actual)

    def assertSuperbaseEquals(self, expected, superbase):
        actual = ""
        for token in superbase:
            if actual:
                actual += " "
            actual += str(token)
        self.assertEqual(expected, actual)

    def debugRowSegments(self, cell_data):
        actual = []
        for cell in cell_data:
            actual.append(cell.segments)
        print(actual)

    def debug_table(self, aligner, table):
        for y in range(aligner.length_witness_b + 1):
            for x in range(aligner.length_witness_a + 1):
                print(y, x), table[y][x]

    def test_superbase_generation_multiple_short_witnesses(self):
        collation = Collation()
        collation.add_plain_witness("A", "a")
        collation.add_plain_witness("B", "b")
        collation.add_plain_witness("C", "c")
        aligner = EditGraphAligner(collation)
        graph = VariantGraph()
        aligner.collate(graph)
        # superbase = aligner.new_superbase
        # self.assertSuperbaseEquals("a b c", superbase)

    # we need to introduce a gap here
    @unit_disabled  # no aligner.table implemented yet
    def testOmission(self):
        collation = Collation()
        collation.add_plain_witness("A", "a b c")
        collation.add_plain_witness("B", "b c")
        aligner = EditGraphAligner(collation)
        graph = VariantGraph()
        aligner.collate(graph)
        table = aligner.table
        #         self.debug_table(aligner, table)
        self.assertEqual(0, table[0][0].g)
        self.assertEqual(-1, table[0][1].g)
        self.assertEqual(-2, table[0][2].g)
        self.assertEqual(-3, table[0][3].g)
        self.assertEqual(-1, table[1][0].g)
        self.assertEqual(-2, table[1][1].g)
        self.assertEqual(-1, table[1][2].g)
        self.assertEqual(-2, table[1][3].g)
        self.assertEqual(-2, table[2][0].g)
        self.assertEqual(-3, table[2][1].g)
        self.assertEqual(-2, table[2][2].g)
        self.assertEqual(-1, table[2][3].g)

    # note: the scoring table in this test is only correct when block detection is OFF
    @unit_disabled
    def testOmission2GlobalScore(self):
        collation = Collation()
        collation.add_plain_witness("A", "a a b c")
        collation.add_plain_witness("B", "a b c")
        aligner = EditGraphAligner(collation)
        graph = VariantGraph()
        aligner.collate(graph)
        table = aligner.table

        self.assertRow([0, -1, -2, -3, -4], table[0])
        self.assertRow([-1, 0, -1, -2, -3], table[1])
        self.assertRow([-2, -1, -2, -1, -2], table[2])
        self.assertRow([-3, -2, -3, -2, -1], table[3])

    # def test_superbase(self):
    #     collation = Collation()
    #     collation.add_plain_witness("A", "X a b c d e f X g h i Y Z j k")
    #     collation.add_plain_witness("B", "a b c Y d e f Y Z g h i X j k")
    #     aligner = EditGraphAligner(collation)
    #     graph = VariantGraph()
    #     aligner.collate(graph)
    #     # superbase = aligner.new_superbase
    #     # self.assertSuperbaseEquals("X a b c Y d e f X Y Z g h i Y Z X j k", superbase)

    # TODO: add Y to the witness B (to check end modification
    def test_duplicated_tokens_in_witness(self):
        collation = Collation()
        collation.add_plain_witness("A", "a")
        collation.add_plain_witness("B", "b")
        collation.add_plain_witness("C", "c")
        collation.add_plain_witness("D", "a a")

        alignment_table = collate(collation)
        print("alignment_table=\n", alignment_table)
        self.assertEqual([None, 'a'], alignment_table.rows[0].to_list_of_strings())
        self.assertEqual(['b', None], alignment_table.rows[1].to_list_of_strings())
        self.assertEqual(['c', None], alignment_table.rows[2].to_list_of_strings())
        self.assertEqual(['a ', 'a'], alignment_table.rows[3].to_list_of_strings())

    def test_duplicated_tokens_in_witness2(self):
        collation = Collation()
        collation.add_plain_witness("A", "a")
        collation.add_plain_witness("B", "b")
        collation.add_plain_witness("C", "c")
        collation.add_plain_witness("D", "a b c a b c")

        # alignment_table = collate(collation)
        # self.assertEqual(['a', None, None, None], alignment_table.rows[0].to_list_of_strings())
        # self.assertEqual([None, 'b', None, None], alignment_table.rows[1].to_list_of_strings())
        # self.assertEqual([None, None, 'c', None], alignment_table.rows[2].to_list_of_strings())
        # self.assertEqual(['a ', 'b ', 'c ', 'a b c'], alignment_table.rows[3].to_list_of_strings())

        expected_tei = """<p><app>
  <rdg wit="#D">a b c </rdg>
</app>
<app>
  <rdg wit="#A">a</rdg>
  <rdg wit="#D">a </rdg>
</app>
<app>
  <rdg wit="#B">b</rdg>
  <rdg wit="#D">b </rdg>
</app>
<app>
  <rdg wit="#C #D">c</rdg>
</app>
</p>"""

        # alignment_table = collate(collation)
        # print("alignment_table=\n",alignment_table)

        output_tei = collate(collation, output="tei", indent=True)
        self.assertEqual(expected_tei, output_tei)

    def test_1(self):
        collation = Collation()
        collation.add_plain_witness("A", "a")
        collation.add_plain_witness("B", "b")
        collation.add_plain_witness("C", "a b")

        alignment_table = collate(collation)
        print("alignment_table=\n", alignment_table)
        self.assertEqual(['a', None], alignment_table.rows[0].to_list_of_strings())
        self.assertEqual([None, 'b'], alignment_table.rows[1].to_list_of_strings())
        self.assertEqual(['a ', 'b'], alignment_table.rows[2].to_list_of_strings())

    def test_2(self):
        collation = Collation()
        collation.add_plain_witness("W1", "in the in the bleach")
        collation.add_plain_witness("W2", "in the in the bleach in the")
        collation.add_plain_witness("W3", "in the in the bleach in the")

        alignment_table = collate(collation)
        print("alignment_table=\n", alignment_table)
        self.assertEqual(['in the in the bleach', None], alignment_table.rows[0].to_list_of_strings())
        self.assertEqual(['in the in the bleach ', 'in the'], alignment_table.rows[1].to_list_of_strings())
        self.assertEqual(['in the in the bleach ', 'in the'], alignment_table.rows[2].to_list_of_strings())

    def test_rank_adjustment(self):
        collation = Collation()
        collation.add_plain_witness('A', 'aa bb cc dd ee ff')
        collation.add_plain_witness('B', 'aa bb ex ff')
        collation.add_plain_witness('C', 'aa bb cc ee ff')
        collation.add_plain_witness('D', 'aa bb ex dd ff')
        collation.add_plain_witness('E', 'aaa aaa aaa aaa aaa')

        alignment_table = collate(collation)
        print("alignment_table=\n", alignment_table)
        self.assertEqual(['aa bb ', 'cc ', 'dd ', 'ee ', 'ff'], alignment_table.rows[0].to_list_of_strings())
        self.assertEqual(['aa bb ', 'ex ', None, None, 'ff'], alignment_table.rows[1].to_list_of_strings())
        self.assertEqual(['aa bb ', 'cc ', None, 'ee ', 'ff'], alignment_table.rows[2].to_list_of_strings())
        self.assertEqual(['aa bb ', 'ex ', 'dd ', None, 'ff'], alignment_table.rows[3].to_list_of_strings())
        self.assertEqual(['aaa aaa aaa aaa aaa', None, None, None, None], alignment_table.rows[4].to_list_of_strings())

    @unit_disabled  # like in the java version, this test currently fails
    def test_align_with_longest_match(self):
        collation = Collation()
        collation.add_plain_witness("A", "a g a g c t a g t")
        collation.add_plain_witness("B", "a g c t")

        alignment_table = collate(collation)
        print("alignment_table=\n", alignment_table)
        self.assertEqual(['a g ', 'a g c t ', 'a g t'], alignment_table.rows[0].to_list_of_strings())
        self.assertEqual([None, 'a g c t', None], alignment_table.rows[1].to_list_of_strings())

    def test_non_overlapping_blocks_Hermans(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_plain_witness("W2", "a b c d F g h i ! q r s t")

        alignment_table = collate(collation)
        print("alignment_table=\n", alignment_table)
        self.assertEqual(['a b c d F g h i ', '! K ', '! q r s t'], alignment_table.rows[0].to_list_of_strings())
        self.assertEqual(['a b c d F g h i ', None, '! q r s t'], alignment_table.rows[1].to_list_of_strings())

    def test_blocks_Hermans_case_three_witnesses(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_plain_witness("W2", "a b c d F g h i ! q r s t")
        collation.add_plain_witness("W3", "a b c d E g h i ! q r s t")

        alignment_table = collate(collation)
        print("alignment_table=\n", alignment_table)
        self.assertEqual(['a b c d ', 'F ', 'g h i ', '! K ', '! q r s t'],
                         alignment_table.rows[0].to_list_of_strings())
        self.assertEqual(['a b c d ', 'F ', 'g h i ', None, '! q r s t'], alignment_table.rows[1].to_list_of_strings())
        self.assertEqual(['a b c d ', 'E ', 'g h i ', None, '! q r s t'], alignment_table.rows[2].to_list_of_strings())


# def test_path(self):
#         a = Witness("A", "a b c")
#         b = Witness("B", "a b c")
#         aligner = EditGraphAligner(a, b)
#         aligner.align()
#         segments = aligner.get_segments()
#         self.assertSegments(["a b c"], segments)


#         path = aligner.get_path()
#         self.assertPath([(0,0),(1,1),(2,2),(3,3)], path)

#     def testOmission2SegmentsScore(self):
#         a = Witness("A", "a a b c")
#         b = Witness("B", "a b c")
#         aligner = EditGraphAligner(a, b)
#         aligner.align()
#         table = aligner.table
#
#         self.debugRowSegments(table[0])
#         self.debugRowSegments(table[1])

#      TODO: add test for segments
#         self.assertEqual(1, table[3][4].segments)



if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testOmission']
    unittest.main()
