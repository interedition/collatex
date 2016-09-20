import unittest
from collatex import *


class Test(unittest.TestCase):
    def test_export_alignment_table_as_xml(self):
        output_expected = """<root><app><rdg wit="#A">The </rdg><rdg wit="#B">The </rdg></app><app><rdg wit="#A">quick </rdg></app><app><rdg wit="#A">brown fox jumps over the </rdg><rdg wit="#B">brown fox jumps over the </rdg></app><app><rdg wit="#B">lazy </rdg></app><app><rdg wit="#A">dog.</rdg><rdg wit="#B">dog.</rdg></app></root>"""
        collation = Collation()
        collation.add_plain_witness("A", "The quick brown fox jumps over the dog.")
        collation.add_plain_witness("B", "The brown fox jumps over the lazy dog.")
        output = collate(collation, output="xml")
        self.assertEquals(output_expected, output)

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testSuffix']
    unittest.main()
