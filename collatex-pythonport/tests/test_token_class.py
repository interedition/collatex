'''
Created on March 24, 2015

@author: Elisa Nury
'''

import unittest
from collatex.core_classes import Token
from collatex.exceptions import TokenError


class TestToken(unittest.TestCase):

    def test_creation_token_t(self):
        data = {'t': 'fox', 'id': 123 }
        t = Token(data)
        self.assertEqual(t.token_string, 'fox')
        self.assertEqual(t.token_data, data)
        
    def test_creation_token_n(self):
        data = {'t': 'kitten', 'n': 'cat'}
        t = Token(data)
        self.assertEqual(t.token_string, 'cat')
        self.assertEqual(t.token_data, data)
    
    def test_creation_token_none(self):
        t = Token(None)
        self.assertEqual(t.token_string, '')
        self.assertIsNone(t.token_data)
        
    def test_invalid_token_raises_exception(self):
        with self.assertRaises(TokenError):
            #data = {'x': 'abc'}
            data = {}
            Token(data)

if __name__ == '__main__':
    unittest.main()
    
