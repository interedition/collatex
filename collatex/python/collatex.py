#!/usr/bin/env python
"""Using CollateX in Python.

Collatex is a Java library for collating textual sources,
for example, to produce an apparatus. You don't need to know
anything about Java at all to use the bindings (that is the point,
of course).

Collatex was developed by Ronald Haentjens Dekker and Gregor Middell
amongst other people.

These Python bindings were by me, Zeth, and any errors or omissions are
mine alone.

The main interface is the Collation class.

Use it as follows:

>>> collation = Collation()
>>> collation.add_witness('one', 'The cat in the hat')
>>> collation.add_witness('two', 'The hat in the cat')
>>> apparatus = collation.get_apparatus()

For all the available classes and methods see the documentation
provided in the Collation class.

The general approach is to provide something easy to use within Python,
with special emphasis to make it easy for people using template engines,
such as the one provided by Django or standalone engines such as Mako,
Genshi, Jinja2 and so on.

Therefore where it makes sense to do so, I have provided human readable
properties rather than method names, (thus following the MVC pattern of
isolating application logic from presentation, i.e. so an HTML-aware
non-developer could edit an HTML template without being scared
or breaking anything).

Whether this has an effect on performance remains to be seen (a little more
work is performed up front rather than lazily), but sooner or later most of the
apparatus will be in RAM either way, and generating apparati tends to be
a batch process anyway, rather than in real time. So who cares about
performance right? If you cared about performance you would be doing
this directly in Collatex anyway :-) Only kidding, I will make sure it is
performant as possible by using it and tweaking it as I go.

I hope to add some easy premade default output formats for inclusion in
templates, so the Python bindings can at least do whatever the Java API
can.

To build Collatex I use commands such as:

bzr branch lp:collatex
export PATH=$PATH:/home/zeth/Sandbox/apache-maven-2.2.1/bin
mvn clean package

"""

# Fill out the following line with where the collatex jar file is located
# Eventually this should be replaced by something more intelligent
# when CollateX is packaged.
JAR_PATH = "interedition/trunk/collatex/collatex-nodeps/target/"

# The following is to manually specify the location of the JVM library
# You only need this if we fail to find your JVM automatically.
JVM_LOCATION = ""
import sys
import os

try:
    import jpype
except ImportError:
    print "You need to install jpype."""
    print """Visit http://sourceforge.net/projects/jpype/files/ """
    sys.exit()


def get_jvm():
    """Get the JVM location.

    We start by trying to find the default JVM path,
    this is in most cases the environment variable JAVA_HOME
    which is normally set by SUN's JDK.

    For Linux users, this might not be set. If so we try to use
    the location used by the openjdk package, one location for 64bit,
    one location for 32bit.

    """

    if JVM_LOCATION:
        return JVM_LOCATION

    try:
        jvm_location = jpype.getDefaultJVMPath()
    except TypeError:
        pass
    else:
        return jvm_location

    if os.name == 'posix':
        if os.uname()[4] == 'x86_64':
            return "/usr/lib/jvm/java-6-openjdk/jre/lib/amd64/server/libjvm.so"
        else:
            return \
      "/usr/lib/debug/usr/lib/jvm/java-6-openjdk/jre/lib/i386/server/libjvm.so"

    if os.name == 'mac':
        return \
    "/System/Library/Frameworks/JavaVM.framework/Libraries/libjvm_compat.dylib"

    # No JVM has been found automatically
    print "You need to manually specify the location of your JVM library."
    print "Please set the JVM_LOCATION configuration within collatex.py."
    sys.exit()

# You can override these manually if the defaults are not working.
JVM = get_jvm()
HOME_PATH = os.path.expanduser('~')
COLLATE_JAR_PATH = os.path.join(HOME_PATH, JAR_PATH)

# Start the Java Virtual Machine
jpype.startJVM(JVM, "-Djava.ext.dirs=%s" % COLLATE_JAR_PATH)

# Java Classes
_ENGINE = jpype.JClass(\
            'eu.interedition.collatex2.implementation.CollateXEngine')
_ALIGNMENT_TABLE = jpype.JClass(\
    'eu.interedition.collatex2.interfaces.IAlignmentTable')
_COLUMN = jpype.JClass('eu.interedition.collatex2.interfaces.IColumn')
_ROW = jpype.JClass('eu.interedition.collatex2.interfaces.IRow')
_CELL = jpype.JClass('eu.interedition.collatex2.interfaces.ICell')
_VARIANT = jpype.JClass(\
    'eu.interedition.collatex2.interfaces.INormalizedToken')
_WITNESS = jpype.JClass('eu.interedition.collatex2.interfaces.IWitness')
_PARALLEL_SEGMENTATION_APPARATUS = \
    jpype.JClass(\
    'eu.interedition.collatex2.output.ParallelSegmentationApparatus')
_APPARATUS_ENTRY = \
    jpype.JClass('eu.interedition.collatex2.output.ApparatusEntry')
_PHRASE = jpype.JClass('eu.interedition.collatex2.interfaces.IPhrase')


DEFAULT_TOKENIZER = jpype.JClass(\
'eu.interedition.collatex2.implementation.tokenization.WhitespaceTokenizer')
DEFAULT_NORMALIZER = jpype.JClass(\
'eu.interedition.collatex2.implementation.tokenization.DefaultTokenNormalizer')


class Collation(object):
    """A collation.

    The collation object has the following methods:

    collation.add_witness - add a witness into the witness list
    collation.get_alignment_table - align the witnesses together

    There are also the following more advanced configuration methods:

    collation.set_tokenizer - allows you to use your own tokenizer
    collation.set_normalizer - allows you to use your own normaliser

    """
    witnesses = []

    def __init__(self):
        self.engine = _ENGINE()

    def _get_raw_witnesses(self):
        """Get the raw witness objects."""
        return [witness.get_raw_witness() for witness in self.witnesses]

    def add_witness(self, sigil, content):
        """Add a witness into the witness list.

        This method requires the following arguments:

        `sigil` is the name of the witness.
        `content` is the textual content that you which to collate

        >>> collation = Collation()
        >>> collation.witnesses = [] # Reset because of docttest quirk
        >>> collation.add_witness('one', 'The cat in the hat')
        >>> collation.add_witness('two', 'The hat in the cat')
        >>> len(collation.witnesses)
        2
        >>> for witness in collation.witnesses:
        ...     print witness.sigil
        one
        two

        """
        witness = Witness(self.engine.createWitness(sigil, content))
        self.witnesses.append(witness)

    def get_alignment_table(self):
        """Align the witnesses together into a table.

        See the AlignmentTable class for more details.

        >>> collation = Collation()
        >>> collation.add_witness('one', 'The cat in the hat')
        >>> collation.add_witness('two', 'The hat in the cat')
        >>> table = collation.get_alignment_table()
        >>> len(table.columns)
        6

        """
        java_alignment_object = self.engine.align(self._get_raw_witnesses())
        table = AlignmentTable(java_alignment_object)
        return table

    def get_apparatus(self, table = None):
        """Get an apparatus object, see the Apparatus class for more details.

        >>> collation = Collation()
        >>> collation.add_witness('one', 'The cat in the hat')
        >>> collation.add_witness('two', 'The hat in the cat')
        >>> apparatus = collation.get_apparatus()

        """
        alignment_table = table if table else self.get_alignment_table()
        apparatus = self.engine.createApparatus(\
            alignment_table.get_raw_table())
        return Apparatus(apparatus)

    def set_tokenizer(self, tokenizer):
        """Specify your own tokenizer to use instead of the default.

        >>> collation = Collation()
        >>> tokenizer = DEFAULT_TOKENIZER()
        >>> collation.set_tokenizer(tokenizer)
        >>> collation.add_witness('one', 'The cat in the hat')
        >>> print collation.witnesses[0].tokens[0]
        The

        """
        self.engine.setTokenizer(tokenizer)

    def set_normalizer(self, normalizer):
        """Specify your own normalizer to use instead of the default.
        >>> collation = Collation()
        >>> normaliser = DEFAULT_NORMALIZER()
        >>> collation.set_normalizer(normaliser)
        >>> collation.add_witness('one', 'The cat in the hat')
        >>> print collation.witnesses[0].tokens[0].normalised
        the

        """
        self.engine.setTokenNormalizer(normalizer)


class AlignmentTable(object):
    """Variants stored in rows and columns.


    When using any position numbers to get out objects from lists,
    remember that position numbers count from 1, while Python counts from 0.

    An alignment table object has the following properties:

    alignment_table.columns - a list of the columns in the table
    alignment_table.rows - a list of the rows in the table
    alignment_table.repeating_tokens - tokens that are repeated

    An alignment table object also has the following methods:

    alignment_table.to_html - provides an HTML representation of the table

    """

    def __init__(self, table = None):
        self._table = table if table else _ALIGNMENT_TABLE()
        self.columns = [Column(column) for column in self._table.getColumns()]
        self.repeating_tokens = [token \
                                     for token in \
                                     self._table.findRepeatingTokens()]

        self.rows = [Row(row) for row in self._table.getRows()]

    def to_html(self):
        """Provides an HTML representation of the alignment table.
        >>> collation = Collation()
        >>> collation.add_witness('one', 'The cat in the hat')
        >>> collation.add_witness('two', 'The hat in the cat')
        >>> table = collation.get_alignment_table()
        >>> print table.to_html().splitlines()[0]
        <div id="alignment-table"><h4>Alignment Table:</h4>

        """
        return self._table.alignmentTableToHTML()

    def get_raw_table(self):
        """Get the unbound table, not recommended as
        the underlying API may change."""
        return self._table


class Row(object):
    """A row of aligned words.

    A row object will have the following properties:

    row.cells - a list of the child cells of the row
    row.sigil - the witness represented by the row

    >>> collation = Collation()
    >>> collation.add_witness('one', 'The cat in the hat')
    >>> collation.add_witness('two', 'The hat in the cat')
    >>> table = collation.get_alignment_table()
    >>> row = table.rows[0]
    >>> print row.sigil
    one
    >>> len(row.cells)
    6

    """

    def __init__(self, row = None):
        self._row = row if row else _ROW()
        self.cells = [Cell(cell) for cell in self._row.cells]
        self.sigil = self._row.getSigil()


class Cell(object):
    """A single cell in the alignment table.

    A cell object will have the following properties:

    cell.empty - whether the cell is empty or not
    cell.token - the token object (if there is one), i.e. the variant reading
    cell.column - the column object within which the cell resides
    cell.position - the position in the row
    cell.sigil - the witness represented by the cell

    >>> collation = Collation()
    >>> collation.add_witness('one', 'The cat in the hat')
    >>> collation.add_witness('two', 'The hat in the cat')
    >>> table = collation.get_alignment_table()
    >>> row = table.rows[0]
    >>> cell = row.cells[1]
    >>> print cell.empty
    False
    >>> print cell.token
    The
    >>> print cell.column
    The,hat
    >>> print cell.position
    2
    >>> print cell.sigil
    one

    """

    def __init__(self, cell = None):
        self._cell = cell if cell else _CELL()
        self.empty = True if self._cell.isEmpty() else False
        self.token = Variant(self._cell.getToken()) if not self.empty else None
        self.column = Column(self._cell.column)
        self.position = self._cell.getPosition()
        self.sigil = self._cell.sigil


class Column(object):
    """A column of aligned words.

    A column object will have the following properties:

    column.variants - the list of variants in the column
    column.position - the position of the column in the alignment table
    column.sigli - the source documents of the variants within the column
    column.state - the state (i.e. type) of the variation

    A column object will also have the following methods:

    column.get_variant_by_sigi - get a particular witness' reading.
    column.contains_witness - See if the column contains a witness.

    >>> collation = Collation()
    >>> collation.add_witness('one', 'The cat in the hat')
    >>> collation.add_witness('two', 'The hat in the cat')
    >>> table = collation.get_alignment_table()
    >>> column = table.columns[0]
    >>> print column.variants[0].content
    The
    >>> print column.position
    1
    >>> print column.sigli[0]
    two
    >>> print column.state
    MATCH

    """

    def __init__(self, column = None):
        self._column = column if column else _COLUMN()
        self.variants = [Variant(variant) \
                             for variant in self._column.getVariants()]
        self.position = self._column.getPosition()
        self.sigli = [sigli for sigli in self._column.getSigli()]
        self.state = str(self._column.getState())

    def get_variant_by_sigil(self, sigil):
        """Get a particular witness' reading.

        >>> collation = Collation()
        >>> collation.add_witness('one', 'The cat in the hat')
        >>> collation.add_witness('two', 'The hat in the cat')
        >>> table = collation.get_alignment_table()
        >>> column = table.columns[0]
        >>> variant = column.get_variant_by_sigil('two')
        >>> print variant.content
        The

        """

        return self._column.getToken(sigil)

    def contains_witness(self, sigil):
        """See if the column contains a witness.

        >>> collation = Collation()
        >>> collation.add_witness('one', 'The cat in the hat')
        >>> collation.add_witness('two', 'The hat in the cat')
        >>> table = collation.get_alignment_table()
        >>> column = table.columns[0]
        >>> column.contains_witness('one')
        False
        >>> column.contains_witness('two')
        True

        """

        if self._column.containsWitness(sigil):
            return True
        else:
            return False

    def __str__(self):
        return self._column.__str__()


class Witness(object):
    """A reading of a textual work.
    A reading contains tokens (i.e. words)."""

    def __init__(self, witness = None):
        self._witness = witness if witness else _WITNESS()
        self.sigil = self._witness.getSigil()
        self.tokens = [Variant(token) for token in self._witness.getTokens()]

    def _get_token_objects(self):
        """Return the raw token objects."""
        return self._witness.getTokens()

    def get_raw_witness(self):
        """Return the unbound witness object,
        not recommended as underlying API may change."""
        return self._witness

    def __str__(self):
        return self.sigil


class Variant(object):
    """An aligned token, e.g. a word.

    A variant object will have the following properties:

    variant.content - the input form of the variant
    variant.normalised - the normalised form of the variant
    variant.position - the position in the alignment table
    variant.sigil - the source document of the variant

    >>> collation = Collation()
    >>> collation.add_witness('one', 'The cat in the hat')
    >>> collation.add_witness('two', 'The hat in the cat')
    >>> table = collation.get_alignment_table()
    >>> column = table.columns[0]
    >>> variant = column.variants[0]
    >>> print variant
    The
    >>> print variant.content
    The
    >>> print variant.normalised
    the
    >>> print variant.position
    1
    >>> print variant.sigil
    two

    """

    def __init__(self, variant = None):
        self._variant = variant if variant else _VARIANT()
        self.content = self._variant.getContent()
        self.normalised = self._variant.getNormalized()
        self.position = self._variant.getPosition()
        self.sigil = self._variant.getSigil()

    def __str__(self):
        return self.content


class Apparatus(object):
    """A Parallel Segmentation Apparatus

    >>> collation = Collation()
    >>> collation.add_witness('one', 'The cat in the hat')
    >>> collation.add_witness('two', 'The hat in the cat')
    >>> apparatus = collation.get_apparatus()
    >>> print apparatus.sigli
    [u'one', u'two']
    >>> print apparatus.entries[0].get_phrase('two')
    The

    """

    def __init__(self, apparatus = None):
        self._apparatus = apparatus if apparatus \
            else _PARALLEL_SEGMENTATION_APPARATUS()
        self.entries = [ApparatusEntry(entry) for \
                            entry in self._apparatus.getEntries()]
        self.sigli = [sigil for sigil in self._apparatus.getSigli()]


class ApparatusEntry(object):
    """An entry in the Parallel Segmentation Apparatus.

    >>> collation = Collation()
    >>> collation.add_witness('one', 'The cat in the hat')
    >>> collation.add_witness('two', 'The hat in the cat')
    >>> apparatus = collation.get_apparatus()
    >>> entry = apparatus.entries[0]

    """

    def __init__(self, entry = None):
        self._entry = entry if entry else _APPARATUS_ENTRY()
        self.sigli = [sigil for sigil in self._entry.getSigli()]

    def get_phrase(self, sigil):
        """Get a phrase by sigil."""
        return Phrase(self._entry.getPhrase(sigil))


class Phrase(object):
    """A sequence of tokens.

    >>> collation = Collation()
    >>> collation.add_witness('one', 'The cat in the hat')
    >>> collation.add_witness('two', 'The hat in the cat')
    >>> apparatus = collation.get_apparatus()
    >>> entry = apparatus.entries[0]
    >>> print entry.get_phrase('two')
    The

    """

    def __init__(self, phrase = None):
        self._phrase = phrase if phrase else _PHRASE()
        self.normalised = self._phrase.getNormalized()
        self.sigil = self._phrase.getSigil()
        self.content = self._phrase.getContent()
        self.begin_position = self._phrase.getBeginPosition()
        self.end_position = self._phrase.getEndPosition()

    def get_first_token(self):
        """Get the first token in the phrase."""
        return Variant(self._phrase.getFirstToken())

    def get_last_token(self):
        """Get the last token in the phrase."""
        return Variant(self._phrase.getLastToken())

    def get_all_tokens(self):
        """Get the whole phrase as a list of tokens."""
        return [Variant(token) for token in self._phrase.getTokens()]

    def __str__(self):
        return self.content


if __name__ == "__main__":
    import doctest
    doctest.testmod()
