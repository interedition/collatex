'''
Created on Nov 20, 2014

@author: Tara L Andrews
'''

import unittest
from collatex import Collation, collate


class Test(unittest.TestCase):
    def testPlainWitness(self):
        plain_witness = {'id': 'A', 'content': 'The quick brown fox jumped over the lazy dogs.'}
        c = Collation()
        c.add_witness(plain_witness)
        self.assertEqual(len(c.witnesses[0].tokens()), 10)

    def testPretokenizedWitnessAdd(self):
        pt_witness = {
                    "id": "A",
                    "tokens": [
                        {"t": "A", "ref": 123},
                        {"t": "black and blue", "adj": True},
                        {"t": "cat", "id": "xyz"},
                        {"t": "bird", "id": "abc"}
                    ]
                }
        c = Collation()
        c.add_witness(pt_witness)
        self.assertEqual(len(c.witnesses[0].tokens()), 4)

    def testPretokenizedWitness(self):
        pretokenized_witness = {
            "witnesses": [
                {
                    "id": "A",
                    "tokens": [
                        {"t": "A", "ref": 123},
                        {"t": "black", "adj": True},
                        {"t": "cat", "id": "xyz"},
                        {"t": "bird", "id": "abc"}
                    ]
                },
                {
                    "id": "B",
                    "tokens": [
                        {"t": "A"},
                        {"t": "white", "adj": True},
                        {"t": "mousedog bird", "adj": False}
                    ]
                }
            ]
        }
        result = collate(pretokenized_witness, segmentation=False)
        self.assertEqual(len(result.rows[0].to_list()), 4)
        self.assertEqual(len(result.rows[1].to_list()), 4)
        # The second witness should have a token that reads 'mousedog bird'.
        self.assertIn("mousedog bird", str(result.rows[1].to_list()))


if __name__ == '__main__':
    unittest.main()
