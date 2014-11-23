'''
Created on Jun 11, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from collatex import Collation


class Test(unittest.TestCase):

    def testCachingOfSuffixArrayAndLCPArray(self):
        collation = Collation()
        collation.add_plain_witness("A", "content")
        collation.add_plain_witness("B", "content")
        sa1 = collation.get_sa()
        sa2 = collation.get_sa()
        self.assertEquals(sa1,sa2)
        collation.add_plain_witness("C", "content")
        sa3 = collation.get_sa()
        self.assertNotEquals(sa2, sa3)


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()