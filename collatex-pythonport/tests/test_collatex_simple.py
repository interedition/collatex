import unittest
from collatex.core_classes import WordPunctuationTokenizer


class Test(unittest.TestCase):

    def test_tokenize(self):
        contents = "a b c"
        tokenizer = WordPunctuationTokenizer()
        #print contents
        self.assertEquals(["a ","b ","c"], tokenizer.tokenize(contents))
    
if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testSuffix']
    unittest.main()