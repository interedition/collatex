import unittest
from collatex import Collation, collate

__author__ = 'ronalddekker'


class Test(unittest.TestCase):

    def test_storage_of_tokens_on_variant_graph(self):
        collation = Collation()
        collation.add_plain_witness("A", "a b c")
        collation.add_plain_witness("B", "a d c")
        variant_graph = collate(collation, output="graph")
        self.assertEqual("{}", str(variant_graph.start.tokens))
        self.assertEqual("{}", str(variant_graph.end.tokens))
        # 'b' and 'd' are in one witness each
        self.assertEqual("{'A': [b]}", str(variant_graph.vertexWith('b').tokens))
        self.assertEqual("{'B': [d]}", str(variant_graph.vertexWith('d').tokens))
        # 'a' and 'c' are in both witnesses
        self.assertEqual("[a]",str(variant_graph.vertexWith('a').tokens['A']))
        self.assertEqual("[a]", str(variant_graph.vertexWith('a').tokens['B']))
        self.assertEqual("[c]", str(variant_graph.vertexWith('c').tokens['A']))
        self.assertEqual("[c]", str(variant_graph.vertexWith('c').tokens['B']))


