'''
Created on March 24, 2015

@author: Elisa Nury
'''

import unittest
from collatex.core_functions import *
from collatex.exceptions import UnsupportedError
from testfixtures import TempDirectory
import os
import json

class TestCollationMethods(unittest.TestCase):
    
    def test_collation_method_create_from_json_file(self):
        with TempDirectory() as d:
            #create a temporary file in a temporary directory
            d.write('testfile.json', b'{"witnesses" : [{"id" : "A", "content" : "The fox."}, {"id" : "B", "content": "The dog"}]}')
            c = Collation.create_from_json_file(os.path.join(d.path, 'testfile.json'))
            self.assertEqual(len(c.witnesses), 2)
    
    def test_collation_create_from_dict(self):
        data = {"witnesses" : [{"id" : "A", "content" : "The fox."}, {"id" : "B", "content": "The dog"}]}
        c = Collation.create_from_dict(data)
        self.assertEqual(len(c.witnesses), 2)
  

class TestCollationFunctions(unittest.TestCase):
    def setUp(self):
        data = {
            'witnesses' : [
                {
                    'id' : 'A',
                    'content' : 'The cat'
                },
                {
                    'id' : 'B',
                    'tokens' : [
                        { 't' : 'The'},
                        { 't' : 'kitten'}
                    ]
                }
            ]
        }
        self.c = Collation.create_from_dict(data)
    
    def test_collation_function_add_plain_witness(self):
        self.c.add_plain_witness('C', 'A cat')
        self.assertEqual(len(self.c.witnesses), 3)
    
    def test_collation_function_add_witness(self):
        witnessdata = {'id': 'C', 'tokens': [{ 't' : 'A'},{ 't' : 'cat'}]}
        self.c.add_witness(witnessdata)
        self.assertEqual(len(self.c.witnesses), 3)
    
    @unittest.expectedFailure
    def test_collation_function_add_witnesses_with_same_id(self):
        witnessdata1 = {'id': 'C', 'tokens': [{ 't' : 'The'},{ 't': 'fox'}]}
        witnessdata2 = {'id': 'C', 'tokens': [{ 't' : 'The'},{ 't': 'dog'}]}
        self.c.add_witness(witnessdata1)
        self.c.add_witness(witnessdata2)
        self.assertEqual(len(self.c.witnesses), 4)
        
        #error in the collation result => there should be an exception raised...
        #json_result = json.loads(collate(self.c, output='json'))
        #self.assertEqual(json_result['table'][2][1], 'fox')
        #self.assertEqual(json_result['table'][3][1], 'dog')
        self.fail("It should not be possible to add 2 witnesses with the same id")
    
    def test_collation_function_get_range_for_witness(self):
        expected_range_B = RangeSet()
        expected_range_B.add_range(4, 6)
        self.assertEqual(self.c.get_range_for_witness('B'), expected_range_B)
        self.assertRaises(Exception, self.c.get_range_for_witness, 'W')
    
    #test other functions?
    #get suffix array
    #get sa
    #get lcp array
    #to extended suffix array


if __name__ == '__main__':
    unittest.main()
