.. :changelog:

History
-------

2.0.0 (2016-10-15)
++++++++++++++++++
* First official release for the Dixit code and collation workshop in Amsterdam
* Added XML as an output format
* Added TEI parallel segmentation as an output format
* Tokenizer: retain whitespace in the t-property of preceding token
* Witness: added normalization: strips whitespace

2.0.0rc20 (2016-07-18)
++++++++++++++++++++++
* Merged old collate_pretokenized_json() function into collate() function
* JSON output contains full JSON representation of the tokens
* Enabled segmentation support for all input formats, and for SVG output
* Enhanced SVG output to include "n" value and all "t" values of JSON input
* JSON output is raw Unicode, instead of escaped characters
* Test suite updated

2.0.0rc19 (2015-12-10)
++++++++++++++++++++++
* Rename of TokenIndex.py was not in effect in the uploaded files. Fixed now.

2.0.0rc18 (2015-11-25)
++++++++++++++++++++++
* Renamed TokenIndex.py module to tokenindex.py to follow conventions.

2.0.0rc17 (2015-11-25)
+++++++++++++++++++++++

* Moved all the block and suffix, LCP interval code to new class TokenIndex.

2.0.0rc16 (2015-06-28)
+++++++++++++++++++++++

* Added output option 'html2' for colored alignment table rendering.

2.0.0rc15 (2015-06-28)
+++++++++++++++++++++++

* Fix a bug that was caused by the fact that a dash was stored in empty cells of the AlignmentTable. Now None is stored (this resolved a TODO). Plain text and HTML rendering of the table render a dash for empty cells. JSON output now returns null for empty cells. Fixes bug when a token with a dash in the content was screwing the rendering of the alignment table (caused of by one errors).

2.0.0rc14 (2015-06-27)
+++++++++++++++++++++++

* Further improved blockification of witnesses.

2.0.0rc13 (2015-06-21)
+++++++++++++++++++++++

* Added properties_filter option to enable users to influence matching based on properties of tokens.
* Improved blockification of witnesses.

2.0.0pre12 (2015-05-12)
+++++++++++++++++++++++

* Added SVG output option to the collate function. For this functionality to work the graphviz python library needs to be installed.

2.0.0pre11 (2014-12-02)
+++++++++++++++++++++++

* Bug-fix: collate_pretokenize_json function should not re-tokenized the content. Thanks to Tara L. Andrews.
* Allow near-matching for plain as well as for pre-tokenized content. Thanks to Tara L. Andrews.
* Added HTML option to collate function for the output as an alignment table represented as HTML.


2.0.0pre10 (2014-11-13)
+++++++++++++++++++++++

* Added support for Unicode character encoding
* Ported codebase from Python 2 to Python 3
* Separated IPython display logic from functional logic. No longer will the collate function try to determine whether you are running an environment that is capable of display HTML or SVG. 

2.0.0pre9 (2014-10-02)
++++++++++++++++++++++

* Added near matching option to collate function.
* Added variant or invariant status to columns in alignment table object and JSON output.
* Added experimental A* decision graph search optimization.  

2.0.0pre8 (2014-09-18)
++++++++++++++++++++++

* Added WordPunctuationTokenizer (treats punctuation as separate tokens).
* Combined suffix array and edit graph aligner approaches into one collation algorithm.

2.0.0pre7 (2014-07-14)
++++++++++++++++++++++

* Fixed handling of segmentation parameter in pretokenized JSON function.

2.0.0pre6 (2014-06-30)
++++++++++++++++++++++

* Added Windows support. Thanks to David J. Birnbaum.
* Fixed handling of IPython imports.

2.0.0pre5 (2014-06-11)
++++++++++++++++++++++

* Added JSON output to collate method.
* Added option to collate method to enable or disable parallel segmentation.
* Added table output to collate_pretokenized_json method, next to the already existing JSON output.
* Cached the suffix and LCP arrays to prevent unnecessary recalculation
* Fixed handling of empty cells in JSON output of pretokenized JSON.
* Fixed compatibility issue when rendering HTML or SVG with IPython 2.1 instead of IPython 0.13.
* Corrected RST syntax in package info description. 

2.0.0pre4 (2014-06-11)
++++++++++++++++++++++

* Added pretokenized JSON support.
* Added JSON visualization for the alignment table.

2.0.0pre3 (2014-06-10)
++++++++++++++++++++++

* Fixed imports in init.py, "from collatex import \*" now works correctly.
* Added IPython HTML support for alignment table.
* Added IPython SVG support for variant graph.
* Added convenience constructors on Collation object. 
* Added horizontal layout for the alignment table visualization, next to vertical one.

2.0.0pre2 (2014-06-09)
++++++++++++++++++++++

* Removed max 6 witness limit in aligner, now n number of witnesses can be aligned. 
* Added transposition detection.
* Added alignment table plus plain text visualization.
* Added collate convenience function.

2.0.0pre1 (2014-06-02)
++++++++++++++++++++++

* First release on PyPI.
* First pure Python development release of CollateX.
* New collation algorithm, which does non progressive multiple witness alignment.
