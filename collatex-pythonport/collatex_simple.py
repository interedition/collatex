'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
import unittest


class Tokenizer(object):
    
    #by default the tokenizer splits on space characters    
    def tokenize(self, contents):
        return contents.split()
    
class Suffix(object):
    
    #generate suffixes from a list of tokens
    def gather_suffices(self, tokens):
        i = 0
        suffixes = []
        for t in tokens:
            suffixes.append(tokens[i:])
            i=i+1
        return suffixes
    



class Test(unittest.TestCase):

    def test_tokenize(self):
        contents = "a b c"
        tokenizer = Tokenizer()
        #print contents
        self.assertEquals(["a","b","c"], tokenizer.tokenize(contents))
    
    
    def testSuffix(self):
        tokenizer = Tokenizer()
        w1 = tokenizer.tokenize("a b c")
        suffix = Suffix()
        suffixes = suffix.gather_suffices(w1);
        self.assertEquals([["a", "b", "c"], ["b", "c"], ["c"]], suffixes)


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testSuffix']
    unittest.main()