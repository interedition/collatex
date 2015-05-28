import unittest
from collatex import collate, Collation
from tests import unit_disabled

__author__ = 'ronalddekker'

class TestTranspositionDetection(unittest.TestCase):

    def testThisMorningExample(self):
        collation = Collation()
        collation.add_plain_witness("A", "This morning the cat observed little birds in the trees.")
        collation.add_plain_witness("B", "The cat was observing birds in the little trees this morning, it observed birds for two hours.")
        alignment_table = collate(collation, detect_transpositions=True)
