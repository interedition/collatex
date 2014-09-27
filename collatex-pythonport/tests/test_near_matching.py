'''
Created on Sep 12, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from collatex.collatex_dekker_algorithm import Collation, collate


class Test(unittest.TestCase):

	def test_near_matching(self):
		collation = Collation()
		collation.add_witness("A", "I bought this glass, because it matches those dinner plates")
		collation.add_witness("B", "I bought those glasses")
		alignment_table = collate(collation, near_match=True, output="novisualization")
		self.assertEquals(["I bought", "this glass, because it matches those dinner plates"], alignment_table.rows[0].to_list())
		self.assertEquals(["I bought", "those glasses"], alignment_table.rows[1].to_list())
		
if __name__ == "__main__":
	#import sys;sys.argv = ['', 'Test.testOmission']
	unittest.main()
		