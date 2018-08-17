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
from collections import Counter


class Test(unittest.TestCase):

    def test_create_bigrams(self):
        # we add the start and end symbol for now
        w1 = ["#", "a", "b", "c", "d", "e", "#"]
        w2 = ["#", "a", "e", "c", "d", "#"]
        w3 = ["#", "a", "d", "b", "#"]

        # We import NLTK to calculate the biskipgrams for us.
        from nltk.util import skipgrams
        a = list(skipgrams(w1, 2, 3))
        b = list(skipgrams(w2, 2, 3))
        c = list(skipgrams(w3, 2, 3))
        print(a)
        print(b)
        print(c)
        #   print(type(a[0]))

        ca = Counter(a)
        ca.update(b)
        ca.update(c)
        print(ca)

        # cc = Counter()
        # #a, b, c)
        # cc.fromkeys(a)

        # # Only show what is in all three!
        # z = list(set(a).intersection(b, c))
        # print(sorted(z))
        #
        # # deze intersectie aanpak werkt niet voor drie witnesses, daar moet je eerst optellen..
        # # en dan pas aan het einde dingen weghalen...




    # this is the previous function, that only created ordinarely
    def create_bigrams_by_hand(self, w1):
        # create the bigrams first..
        # we walk over each of the items, and remember the previous one
        # we then combine the current one with the previous one...
        # So this is without gaps...
        previous = ""
        result = []
        for w in w1:
            bigram = (previous, w)
            result.append(bigram)
            previous = w
        result = result[1:]
        print(result)
        return result


