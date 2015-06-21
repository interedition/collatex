import unittest
from collatex import Collation, collate

__author__ = 'ronalddekker'


class Test(unittest.TestCase):

    def test_storage_of_tokens_on_variant_graph(self):
        collation = Collation()
        collation.add_plain_witness("A", "a b c")
        collation.add_plain_witness("B", "a d c")
        variant_graph = collate(collation, output="graph")
        self.assertEqual("{}", str(variant_graph.vertex_attributes(0)["tokens"]))
        self.assertEqual("{}", str(variant_graph.vertex_attributes(1)["tokens"]))
        # TODO: testing node 2 is difficult because of random order of tokens
        self.assertEqual("{'A': b}", str(variant_graph.vertex_attributes(3)["tokens"]))
        # TODO: testing node 4 is difficult because of random order of tokens
        self.assertEqual("{'B': d}", str(variant_graph.vertex_attributes(5)["tokens"]))


