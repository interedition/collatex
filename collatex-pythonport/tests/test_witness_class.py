'''
Created on March 24, 2015

@author: Elisa Nury
'''

import unittest
from collatex.core_classes import Witness, Token, Tokenizer
from collatex.exceptions import UnsupportedError, TokenError

class TestWitness(unittest.TestCase):

    def test_creation_witness_plain(self):
        data = {'id': 'A', 'content': 'The quick brown fox jumped over the lazy dogs.'}
        w = Witness(data)
        self.assertEqual(w.sigil, 'A')
        self.assertEqual(len(w.tokens()), 10)
        self.assertEqual(w.tokens()[3].token_string, 'fox')
        
    def test_creation_witness_pretokenized(self):
        data = {    'id': 'B',
                    'tokens': [
                        {'t': 'A', 'ref': 123},
                        {'t': 'black and blue', 'adj': True},
                        {'t': 'cat', 'id': 'xyz'},
                        {'t': 'bird.', 'id': 'abc'}
                    ]
                }
        w = Witness(data)
        self.assertEqual(w.sigil, 'B')
        self.assertEqual(len(w.tokens()), 4)
    
    def test_invalid_witness_missing_id(self):
        data = {'name': 'A', 'content': 'The quick brown fox jumped over the lazy dogs.'}
        self.assertRaises(UnsupportedError, Witness, data)
        
    def test_invalid_witness_missing_content_tokens(self):
        data = {'id': 'A'}
        self.assertRaises(UnsupportedError, Witness, data)
   
    def test_invalid_witness_content_is_pretokenized(self):
        #'content' is pretokenized instead of plain text
        data = {'id': 'A', 'content': [{'t':'the'}, {'t':'fox'}]}
        self.assertRaises(TypeError, Witness, data)  
    
    def test_invalid_witness_tokens_is_plain(self):
        #'tokens' is plain text instead of pretokenized        
        data = {'id': 'A', 'tokens': 'The quick brown fox jumped over the lazy dogs.'}    
        self.assertRaises(TokenError, Witness, data) 


if __name__ == '__main__':
    unittest.main()
    
