{% include header.ext %}

# CollateX Python documentation main page

## Overview

This page documents _input formats_, _output formats_, and the _API_ for CollateX Python 2.1.3rc2. 

Information about the _Gothenburg model of textual variation_, the _variant graph_ data model, and _alignment algorithms_ is available at the main CollateX site at <https://collatex.net>.

Tutorial information about using CollateX Python is available at <https://github.com/DiXiT-eu/collatex-tutorial>. These materials were written for an earlier release of CollateX Python, and may be superseded in part by the present page.

## Installation

Basic installation instructions for CollateX Python are available at <https://github.com/DiXiT-eu/collatex-tutorial/blob/master/unit1/Installation.ipynb>. 

In order render variant graphs in the Jupyter Notebook interface (which is optional), you must intall both the Graphviz stand-alone program and the graphviz Python package. Graphviz (the stand-alone program) installation has been simplified since the time the basic installation instructions were written; use the newer method at <https://graphviz.gitlab.io/download/>. Install the Python package as described above, with `pip install graphviz`.

## Getting started

Import and use the CollateX Python package as follows:

```python
from collatex import *
collation = Collation()
collation.add_plain_witness("A", "The quick brown fox jumps over the dog.")
collation.add_plain_witness("B", "The brown fox jumps over the lazy dog.")
alignment_table = collate(collation)
print(alignment_table)
```

The preceding reads plain-text data, applies default tokenization and normalization, aligns the witnesses, and outputs an ASCII alignment table. As described at <https://github.com/DiXiT-eu/collatex-tutorial>, you can replace the default tokenization and normalization with methods customized to fit the shape of your data; those procedures are not described further in this document. Multiple input and output formats are described below.

## Input 

CollateX Python accepts input as either _plain text_ or _pretokenized JSON_.

### Plain text input

Plain text input is illustrated above. The witnesses are added to the `Collation` object, which can then be passed as the first argument to the `collate()` function. Plain text input uses default tokenization (split on white space, treat punctuation as separate tokens) and normalization (strip trailing white space). If you want to perform custom tokenization or normalization, you must create pretokenized JSON input.

### Pretokenized JSON input

In the following example, a JSON object has been assigned to the variable `stuff`, which can then be passed directly as the first argument to the `collate()` function. Python does not tolerate white space for human legibility, so the JSON object in this example is written (awkwardly) entirely on one line. The structure CollateX requires for JSON input is described and illustrated at <https://collatex.net/doc/>.

```python
from collatex import *
stuff = {"witnesses" : [ {"id": "A", "tokens" : [{"t": "The ", "n": "The"}, {"t": "brown ", "n": "brown"}, {"t": "fox ", "n": "fox"}, {"t": "jumps ", "n": "jumps"}, {"t": "over ", "n": "over"}, {"t": "the ", "n": "the"}, {"t": "dog", "n": "dog"}, {"t": ".", "n": "."}]},  {"id" : "B", "tokens" : [{"t": "The ", "n": "The"}, {"t": "quick ", "n" :"quick"}, {"t": "brown ", "n": "brown"}, {"t": "fox ", "n": "fox"}, {"t": "jumps ", "n" :"jumps"}, {"t": "over ", "n": "over"}, {"t": "the ", "n": "the"}, {"t": "lazy ", "n": "lazy"}, {"t": "dog", "n": "dog"}, {"t": ".", "n": "."}]}]}
print(collate(stuff))
```

## Alignment parameters

The first argument to the `collate()` is the name of the `Collation` object. The `output`, `layout`, and `indent` parameters control the shape of the output, and are discussed below. There are also two parameters that control the way the alignment is performed: `near_match` and `segmentation`. Both take Boolean values (`True` or `False`).

### Segmentation

`Segmentation` determines whether each token is output separately (`False`) or whether adjacent tokens that agree in whether they include variation or not are merged into the same output cell (`True`). The default is `True`, so `collate(collation)` produces output like:

```
+---+-----+-------+--------------------------+------+------+
| A | The | -     | brown fox jumps over the | -    | dog. |
| B | The | quick | brown fox jumps over the | lazy | dog. |
+---+-----+-------+--------------------------+------+------+
```

while `collate(collation, segmentation=False)` produces:

```
+---+-----+-------+-------+-----+-------+------+-----+------+-----+---+
| A | The | -     | brown | fox | jumps | over | the | -    | dog | . |
| B | The | quick | brown | fox | jumps | over | the | lazy | dog | . |
+---+-----+-------+-------+-----+-------+------+-----+------+-----+---+
```

## Output

### Overview

CollateX Python supports the following output formats: ASCII table, HTML table (plain and colorized, only in Jupyter Notebook interface), SVG variant graph (only in Jupyter Notebook interface, requires Graphviz executable and Python `graphviz` package), generic XML, and TEI-XML. Output support is planned for CSV, TSV, and GraphML; support is also planned for saving HTML and SVG output for reuse outside the Jupyter Notebook interface).

### Output formats

The output format is specified with the `output` parameter to the `collate()` functions, e.g., `collate(collation, output="svg")`. The default is the ASCII table. In the following examples, the variable `collation` is a `Collation` object.

#### ASCII table

`collate(collation)`, without any `output` value, creates a horizontal ASCII table, along the lines of:

```
+---+-----+-------+--------------------------+------+------+
| A | The | -     | brown fox jumps over the | -    | dog. |
| B | The | quick | brown fox jumps over the | lazy | dog. |
+---+-----+-------+--------------------------+------+------+
```

The ASCII table output is not printed by default. The typical way to use it inside the Jupyter Notebook interface is:

```python
alignment_table = collate(collation)
print(alignment_table)
```

You can create a vertical table (most useful when there are many witnesses) with `collate(stuff, layout="vertical")`. The output looks like:

```
+----------------------+----------------------+
|          A           |          B           |
+----------------------+----------------------+
|         The          |         The          |
+----------------------+----------------------+
|          -           |        quick         |
+----------------------+----------------------+
| brown fox jumps over | brown fox jumps over |
|         the          |         the          |
+----------------------+----------------------+
|          -           |         lazy         |
+----------------------+----------------------+
|         dog.         |         dog.         |
+----------------------+----------------------+
```

#### HTML table

Two types

#### SVG variant graph

Two types

#### Generic XML

#### TEI-XML

## API