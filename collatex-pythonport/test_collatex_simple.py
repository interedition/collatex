import unittest

from collatex_core import Tokenizer
from collatex_suffix import Suffix


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