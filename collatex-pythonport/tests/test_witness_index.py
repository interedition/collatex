# Witness index
# 10-08-2018
# @author: Ronald Haentjens Dekker
# We are going to create an index for each witness.
# We use n-grams and skip grams
# The n-grams are going to be 2 or 3 tokens wide
# Gaps in the skipgrams are going to be 1 to 3 tokens wide.

# Witness are going to be arrays of strings for now
# In reality witnesses and tokens are more complex
import unittest


class Test(unittest.TestCase):

    def test(self):
        # we add the start and end symbol for now
        w1 = ["#", "a", "b", "c", "d", "e", "#"]
        # create the bigrams first..
        # we walk over each of the items, and remember the previous one
        # we then combine the current one with the previous one...
        # So this is without gaps...
        previous = ""
        result = []
        for w in w1:
            bigram = (previous, w)
            result.append(bigram)
            print(bigram)
            previous = w

