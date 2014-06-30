.. :changelog:

History
-------

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
