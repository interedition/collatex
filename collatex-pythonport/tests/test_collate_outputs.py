'''
Created on March 24, 2015

@author: Elisa Nury
'''

import unittest
from collatex.core_functions import *
from collatex.exceptions import UnsupportedError

class TestCollate(unittest.TestCase):
    def test_collate_with_invalid_output(self):
        data = {"witnesses" :
            [
            {"id" : "A", "tokens" :
                [
                {"t": "A", "id": 1},
                {"t": "small"},
                {"t": "black"},
                {"t": "cat"}
                ]
            },
            {"id" : "B", "tokens" :
                [
                {"t": "A"},
                {"t": "small"},
                {"t": "white"},
                {"t": "kitten.", "n": "cat"}
                ]
            }
            ]
        }
        c = Collation.create_from_dict(data)
        with self.assertRaises(Exception):
            collate(c, output="xyz")
    
    def test_collate_with_empty_collation(self):
        c = Collation()
        with self.assertRaises(IndexError):
            collate(c)
  

class TestTokenizedJsonOutput(unittest.TestCase):
    def setUp(self):
        self.data = {"witnesses" :
            [
            {"id" : "A", "tokens" :
                [
                {"t": "A", "id": 1},
                {"t": "small"},
                {"t": "black"},
                {"t": "cat"}
                ]
            },
            {"id" : "B", "tokens" :
                [
                {"t": "A"},
                {"t": "small"},
                {"t": "white"},
                {"t": "kitten.", "n": "cat"}
                ]
            }
            ]
        }
        self.c = Collation.create_from_dict(self.data)
        self.maxDiff = None
    
    #--------------------------------------------------
    #JSON output
    def test_tokenized_output_json_segmentationFalse_layoutHorizontal(self):
        expected = '{"table": [[[{"id": 1, "t": "A"}], [{"t": "small"}], [{"t": "black"}], [{"t": "cat"}]], [[{"t": "A"}], [{"t": "small"}], [{"t": "white"}], [{"n": "cat", "t": "kitten."}]]], "witnesses": ["A", "B"]}'
        output = collate(self.c, output="json", segmentation=False, layout="horizontal")
        self.assertEqual(output, expected)
    
    def test_tokenized_output_json_segmentationFalse_layoutVertical(self):
        expected = '{"table": [[[{"id": 1, "t": "A"}], [{"t": "A"}]], [[{"t": "small"}], [{"t": "small"}]], [[{"t": "black"}], [{"t": "white"}]], [[{"t": "cat"}], [{"n": "cat", "t": "kitten."}]]], "witnesses": ["A", "B"]}'
        output = collate(self.c, output="json", segmentation=False, layout="vertical")
        self.assertEqual(output, expected)
    
    def test_tokenized_output_json_segmentationTrue_layoutHorizontal(self):
        expected = '{"table": [[["A small"], ["black"], ["cat"]], [["A small"], ["white"], ["cat"]]], "witnesses": ["A", "B"]}'
        output = collate(self.c, output="json", segmentation=True, layout="horizontal")
        self.assertEqual(output, expected)
    
    def test_tokenized_output_json_segmentationTrue_layoutVertical(self):
        expected = '{"table": [[["A small"], ["A small"]], [["black"], ["white"]], [["cat"], ["cat"]]], "witnesses": ["A", "B"]}'
        output = collate(self.c, output="json", segmentation=True, layout="vertical")
        self.assertEqual(output, expected)
    
    #--------------------------------------------------
    #TABLE output

    def test_tokenized_output_table_segmentationFalse_layoutHorizontal(self):
        expected = """\
+---+---+-------+-------+---------+
| A | A | small | black | cat     |
| B | A | small | white | kitten. |
+---+---+-------+-------+---------+"""
        output = str(collate(self.c, output="table", segmentation=False, layout="horizontal"))
        self.assertEqual(output, expected)
    
    def test_tokenized_output_table_segmentationFalse_layoutVertical(self):
        expected = '''\
+-------+---------+
|   A   |    B    |
+-------+---------+
|   A   |    A    |
+-------+---------+
| small |  small  |
+-------+---------+
| black |  white  |
+-------+---------+
|  cat  | kitten. |
+-------+---------+'''
        output = str(collate(self.c, output="table", segmentation=False, layout="vertical"))
        self.assertEqual(output, expected)
    
    def test_tokenized_output_table_segmentationTrue_layoutHorizontal(self):
        expected = """\
+---+---------+-------+-----+
| A | A small | black | cat |
| B | A small | white | cat |
+---+---------+-------+-----+"""
        output = str(collate(self.c, output="table", segmentation=True, layout="horizontal"))
        self.assertEqual(output, expected)
    
    def test_tokenized_output_table_segmentationTrue_layoutVertical(self):
        expected = '''\
+---------+---------+
|    A    |    B    |
+---------+---------+
| A small | A small |
+---------+---------+
|  black  |  white  |
+---------+---------+
|   cat   |   cat   |
+---------+---------+'''
        output = str(collate(self.c, output="table", segmentation=True, layout="vertical"))
        self.assertEqual(output, expected)
   
    #--------------------------------------------------
    #HTML output

    def test_tokenized_output_html_segmentationFalse_layoutHorizontal(self):
        expected = '''\
<table>
    <tr>
        <td>A</td>
        <td>A</td>
        <td>small</td>
        <td>black</td>
        <td>cat</td>
    </tr>
    <tr>
        <td>B</td>
        <td>A</td>
        <td>small</td>
        <td>white</td>
        <td>kitten.</td>
    </tr>
</table>'''
        output = collate(self.c, output="html", segmentation=False, layout="horizontal")
        self.assertEqual(output, expected)
    
    def test_tokenized_output_html_segmentationFalse_layoutVertical(self):
        expected = '''\
<table>
    <tr>
        <th>A</th>
        <th>B</th>
    </tr>
    <tr>
        <td>A</td>
        <td>A</td>
    </tr>
    <tr>
        <td>small</td>
        <td>small</td>
    </tr>
    <tr>
        <td>black</td>
        <td>white</td>
    </tr>
    <tr>
        <td>cat</td>
        <td>kitten.</td>
    </tr>
</table>'''
        output = collate(self.c, output="html", segmentation=False, layout="vertical")
        self.assertEqual(output, expected)
    
    def test_tokenized_output_html_segmentationTrue_layoutHorizontal(self):
        expected = '''\
<table>
    <tr>
        <td>A</td>
        <td>A small</td>
        <td>black</td>
        <td>cat</td>
    </tr>
    <tr>
        <td>B</td>
        <td>A small</td>
        <td>white</td>
        <td>cat</td>
    </tr>
</table>'''
        output = collate(self.c, output="html", segmentation=True, layout="horizontal")
        self.assertEqual(output, expected)
    
    def test_tokenized_output_html_segmentationTrue_layoutVertical(self):
        expected = '''\
<table>
    <tr>
        <th>A</th>
        <th>B</th>
    </tr>
    <tr>
        <td>A small</td>
        <td>A small</td>
    </tr>
    <tr>
        <td>black</td>
        <td>white</td>
    </tr>
    <tr>
        <td>cat</td>
        <td>cat</td>
    </tr>
</table>'''
        output = collate(self.c, output="html", segmentation=True, layout="vertical")
        self.assertEqual(output, expected)

    
    
    
#--------------------------------------------------
#Empty cells output

class TestOutputEmptyCells(unittest.TestCase):
    def setUp(self):
        data = {
      "witnesses" : [
        {
          "id" : "A",
          "tokens" : [
              { "t" : "A"},
              { "t" : "black"},
              { "t" : "cat"}
          ]
        },
        {
          "id" : "B",
          "tokens" : [
              { "t": "A" },
              { "t": "kitten.", "n": "cat" }
          ]
        }
    ]
    }
        self.c = Collation.create_from_dict(data)
    
    def test_json_segmentationTrue_output_with_empty_cells(self):
        expected = '{"table": [[["A"], ["black"], ["cat"]], [["A"], ["-"], ["cat"]]], "witnesses": ["A", "B"]}'
        output = collate(self.c, output="json")
        self.assertEqual(output, expected)
    
    def test_json_segmentationFalse_output_with_empty_cells(self):
        expected = '{"table": [[[{"t": "A"}], [{"t": "black"}], [{"t": "cat"}]], [[{"t": "A"}], [{"t": "-"}], [{"n": "cat", "t": "kitten."}]]], "witnesses": ["A", "B"]}'
        output = collate(self.c, output="json", segmentation=False)
        self.assertEqual(output, expected)


if __name__ == '__main__':
    unittest.main()
