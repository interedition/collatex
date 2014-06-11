'''
Created on Jun 8, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from collatex import Collation
from collatex.collatex_dekker_algorithm import collate


class Test(unittest.TestCase):


    def testDoubleTransposition1(self):
        collation = Collation()
        collation.add_witness("A", "the cat is black")
        collation.add_witness("B", "black is the cat")
        alignment_table = collate(collation, output="novisualization")
        self.assertEquals(["the cat", "is", "black"], alignment_table.rows[0].to_list())
        self.assertEquals(["black", "is", "the cat"], alignment_table.rows[1].to_list())

#   @Test
#   public void doubleTransposition1() {
#     final SimpleWitness[] w = createWitnesses("the cat is black", "black is the cat");
#     final RowSortedTable<Integer, Witness, Set<Token>> t = table(collate(w));
#     assertEquals("|the|cat|is|black| |", toString(t, w[0]));
#     assertEquals("|black| |is|the|cat|", toString(t, w[1]));
#   }
# 
#   @Test
#   public void doubleTransposition2() {
#     final SimpleWitness[] w = createWitnesses("a b", "b a");
#     final RowSortedTable<Integer, Witness, Set<Token>> t = table(collate(w));
#     assertEquals("| |a|b|", toString(t, w[0]));
#     assertEquals("|b|a| |", toString(t, w[1]));
#   }
# 
#   @Test
#   public void doubleTransposition3() {
#     final SimpleWitness[] w = createWitnesses("a b c", "b a c");
#     final RowSortedTable<Integer, Witness, Set<Token>> t = table(collate(w));
#     assertEquals("| |a|b|c|", toString(t, w[0]));
#     assertEquals("|b|a| |c|", toString(t, w[1]));
#   }


# 
# if __name__ == "__main__":
#     #import sys;sys.argv = ['', 'Test.testName']
#     unittest.main()