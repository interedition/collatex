'''
Created on Aug 5, 2014

@author: Ronald Haentjens Dekker
'''

from collatex.collatex_core import CollationAlgorithm
from sets import Set
from collatex.collatex_suffix import Occurrence, BlockWitness, Block,\
    PartialOverlapException
from operator import attrgetter
from ClusterShell.RangeSet import RangeSet
from prettytable import PrettyTable

class EditGraphNode(object):
    def __init__(self):
        self.g = 0 # global score 
        self.match = False # this node represents a match or not
        
    def __repr__(self):
        return repr(self.g)

    '''
    Aligner based on an edit graph.
    It needs a g function and a definition of a match.
    Since every node of the graph has three children the graph is represented as a table internally.
    Note: this implementation can be further optimised by using a* search algorithm.
    '''
class DekkerSuffixEditGraphAligner(CollationAlgorithm):
    def __init__(self, collation):
        self.blocks = None
        self.collation = collation


    def _debug_edit_graph_table(self):
        # print the table horizontal
        x = PrettyTable()
        x.header=False
        for y in xrange(0, len(self.table)):
            cells = self.table[y]
            x.add_row(cells)
        # alignment can only be set after the field names are known.
        # since add_row sets the field names, it has to be set after x.add_row(cells)
        x.align="l"
        print(x)
        return x
    
    
    def collate(self, graph, collation):
        '''
        :type graph: VariantGraph
        :type collation: Collation
        '''
        # Build the variant graph for the first witness
        # this is easy: generate a vertex for every token
        first_witness = collation.witnesses[0]
        tokens = first_witness.tokens()
        token_to_vertex = self.merge(graph, first_witness.sigil, tokens)
 
        first_witness_as_blocks = self._get_block_witness(first_witness)
#         print(first_witness_as_blocks.debug())

        # construct superbase
        # The superbase needs to consists of Blocks
        # the superbase is a lineair representation of the variant graph
        # the superbase consists of a list of occurrences of blocks
        superbase = first_witness_as_blocks.occurrences
        
        # the graph occurrence to vertices dictionary translates between the occurrences of the
        # superbase to the variant graph vertices
        # Build the initial occurrence to list vertex map 
        graph_occurrence_to_vertices = {}
        self._build_occurrences_to_vertices(collation, first_witness, first_witness_as_blocks, token_to_vertex, [], graph_occurrence_to_vertices) 
        
#         print(graph_occurrence_to_vertices)
        
        # align witness 2 - n
        for x in range(1, len(collation.witnesses)):
            next_witness = collation.witnesses[x]
            # create block witness out of next_witness
            next_witness_as_blocks = self._get_block_witness(next_witness)
            witness_occurrence_to_tokens = self._build_occurrences_to_tokens(collation, next_witness, next_witness_as_blocks)
            # debug
#             print(next_witness_as_blocks.debug())
            self._create_edit_graph_table(superbase, next_witness_as_blocks)
            # debug
#             self._debug_edit_graph_table()
            occ_alignment = self._traverse_edit_graph_table_to_find_optimal_alignment()
            # debug
#             print(occ_alignment)
            
            alignment = {}
            
            for witness_occurrence, graph_occurrence in occ_alignment.iteritems():
                # convert the occurrences back to token -> vertex
                tokens = witness_occurrence_to_tokens[witness_occurrence]
                vertices = graph_occurrence_to_vertices[graph_occurrence]
                for token, vertex in zip(tokens, vertices):
                    alignment[token]=vertex

            # merge
            token_to_vertex = self.merge(graph, next_witness.sigil, next_witness.tokens(), alignment)
 
            # change superbase
            superbase = self.new_superbase
            self._build_occurrences_to_vertices(collation, next_witness, next_witness_as_blocks, token_to_vertex, [], graph_occurrence_to_vertices)    

        
            
            
    # superbase is a list of occurrences
    # witness is a block witness (which contains a list of occurrences)
    def _create_edit_graph_table(self, superbase, witness):
        self.occ_witness_a = superbase
        self.occ_witness_b = witness.occurrences
        self.length_witness_a = len(self.occ_witness_a)
        self.length_witness_b = len(self.occ_witness_b)
        self.table = [[EditGraphNode() for _ in range(self.length_witness_a+1)] for _ in range(self.length_witness_b+1)]

        # per diagonal calculate the score (taking into account the three surrounding nodes)
        self.traverse_diagonally()

    # the alignment return is in the form of dict(occ_witness --> occ_base)
    def _traverse_edit_graph_table_to_find_optimal_alignment(self):
        alignment = {}

        # segment stuff
        # note we traverse from right to left!
        self.last_x = self.length_witness_a
        self.last_y = self.length_witness_b
        self.new_superbase=[]
        
        # start lower right cell
        x = self.length_witness_a
        y = self.length_witness_b
        # work our way to the upper left
        while x > 0 and y > 0:
            self._process_cell(self.occ_witness_a, self.occ_witness_b, alignment, x, y)
            # examine neighbor nodes
            nodes_to_examine = Set()
            nodes_to_examine.add(self.table[y][x-1])
            nodes_to_examine.add(self.table[y-1][x])
            nodes_to_examine.add(self.table[y-1][x-1])
            # calculate the maximum scoring parent node
            parent_node = max(nodes_to_examine, key=lambda x: x.g)
            # move position
            if self.table[y-1][x-1] == parent_node:
                # another match or replacement
                y = y -1
                x = x -1
            else:
                if self.table[y-1][x] == parent_node:
                    #omission?
                    y = y -1
                else:
                    if self.table[y][x-1] == parent_node:
                        #addition?
                        x = x -1
        # process additions/omissions in the begin of the superbase/witness
        self._add_to_superbase(self.occ_witness_a, self.occ_witness_b, 0, 0)
        return alignment

        
    def _process_cell(self, witness_a, witness_b, alignment, x, y):
        cell = self.table[y][x]
        # process segments
        if cell.match == True:
            self._add_to_superbase(witness_a, witness_b, x, y)
            self.last_x = x
            self.last_y = y
        # process alignment
        if cell.match == True:
            occ_base = witness_a[x-1]
            occ_witness = witness_b[y-1]
            alignment[occ_witness] = occ_base
            self.new_superbase.insert(0, occ_base)
        return cell


    def _add_to_superbase(self, witness_a, witness_b, x, y):
#         print self.last_x - x - 1, self.last_y - y - 1
        if self.last_x - x - 1 > 0 or self.last_y - y - 1 > 0:
#             print x, self.last_x, y, self.last_y 
            # create new segment
            omitted_base = witness_a[x:self.last_x - 1]
#             print omitted_base
            added_witness = witness_b[y:self.last_y - 1]
#             print added_witness
            self.new_superbase = added_witness + self.new_superbase
            self.new_superbase = omitted_base + self.new_superbase

    # This function traverses the table diagonally and scores each cell.
    # Original function from Mark Byers; translated from C into Python.
    def traverse_diagonally(self):
        m = self.length_witness_b+1
        n = self.length_witness_a+1
        for _slice in range(0, m + n - 1, 1):
            z1 = 0 if _slice < n else _slice - n + 1;
            z2 = 0 if _slice < m else _slice - m + 1;
            j = _slice - z2
            while j >= z1:
                self.table[j][_slice-j].g=self.score(j, _slice - j)
                j -= 1


    # TODO: Count and score segments
    def score(self, y, x):
        # initialize root node score to zero (no edit operations have
        # been performed)
        if y == 0 and x == 0:
            return 0 
        # examine neighbor nodes
        nodes_to_examine = Set()
        # fetch existing score from the left node if possible
        if x > 0:
            nodes_to_examine.add(self.table[y][x-1])
        if y > 0:
            nodes_to_examine.add(self.table[y-1][x])
        if x > 0 and y > 0:
            nodes_to_examine.add(self.table[y-1][x-1])
        # calculate the maximum scoring parent node
        parent_node = max(nodes_to_examine, key=lambda x: x.g)
        # no matching possible in this case (always treated as a gap)
        # it is either an add or a delete
        if x == 0 or y == 0:
            return parent_node.g - 1
         
        # it is either an add/delete or replacement (so an add and a delete)
        # it is a replacement
        if parent_node == self.table[y-1][x-1]:
            # now we need to determine whether this node represents a match
            # NOTE: it would be nicer if matching was separate function!
            occ_a = self.occ_witness_a[x-1]
            occ_b = self.occ_witness_b[y-1]
            match = occ_a.block == occ_b.block
            # based on match or not and parent_node calculate new score
            if match:
                # mark the fact that this node is match
                self.table[y][x].match = True
                # do not change score for now 
                score = parent_node.g
            else:
                score = parent_node.g - 2
        # it is an add/delete
        else:
            score = parent_node.g - 1
        return score

    '''
    Internal method to transform a Witness into a Block Witness.
    '''
    def _get_block_witness(self, witness):
#         print("Generating block witness for: "+witness.sigil)
        sigil_witness = witness.sigil
        range_witness = self.collation.get_range_for_witness(sigil_witness)
        #NOTE: to prevent recalculation of blocks
        if not self.blocks:
            self.blocks = self._get_non_overlapping_repeating_blocks() 
        blocks = self.blocks 
        # make a selection of blocks and occurrences of these blocks in the selected witness
        occurrences = []
        for block in blocks:
            block_ranges_in_witness = block.ranges & range_witness
            # note this are multiple ranges
            # we need to iterate over every single one
            for block_range in block_ranges_in_witness.contiguous():
                occurrence = Occurrence(block_range, block)
                occurrences.append(occurrence) 
        # sort occurrences on position
        sorted_o = sorted(occurrences, key=attrgetter('lower_end'))
        block_witness = BlockWitness(sorted_o, self.collation.tokens)
        return block_witness

    '''
    Find all the non overlapping repeating blocks in the witness set of this collation.
    '''
    def _get_non_overlapping_repeating_blocks(self):
        extended_suffix_array = self.collation.to_extended_suffix_array()
        potential_blocks = extended_suffix_array.split_lcp_array_into_intervals() 
        self.filter_potential_blocks(potential_blocks)
        # step 3: sort the blocks based on depth (number of repetitions) first,
        # second length of LCP interval,
        # third sort on parent LCP interval occurrences.
        sorted_blocks_on_priority = sorted(potential_blocks, key=attrgetter("number_of_occurrences", "minimum_block_length", "number_of_siblings"), reverse=True)
        # step 4: select the definitive blocks
        occupied = RangeSet()
        real_blocks = []
        for potential_block in sorted_blocks_on_priority:
#           print(potential_block.info())
            try:
                non_overlapping_range = potential_block.calculate_non_overlapping_range_with(occupied)
                if non_overlapping_range:
#                     print("Selecting: "+str(potential_block))
                    occupied.union_update(non_overlapping_range)
                    real_blocks.append(Block(non_overlapping_range))
            except PartialOverlapException:          
#                 print("Skip due to conflict: "+str(potential_block))
                while potential_block.minimum_block_length > 1:
                    # retry with a different length: one less
                    for idx in range(potential_block.start+1, potential_block.end+1):
                        potential_block.LCP[idx] -= 1
                    potential_block.length -= 1
                    try:
                        non_overlapping_range = potential_block.calculate_non_overlapping_range_with(occupied)
                        if non_overlapping_range:
#                             print("Retried and selecting: "+str(potential_block))
                            occupied.union_update(non_overlapping_range)
                            real_blocks.append(Block(non_overlapping_range))
                            break
                    except PartialOverlapException:          
#                         print("Retried and failed again")
                        pass
        return real_blocks


    # filter out all the blocks that have more than one occurrence within a witness
    def filter_potential_blocks(self, potential_blocks):
        for potential_block in potential_blocks[:]:
            for witness in self.collation.witnesses:
                witness_sigil = witness.sigil
                witness_range = self.collation.get_range_for_witness(witness_sigil)
                inter = witness_range.intersection(potential_block.block_occurrences())
                if potential_block.number_of_occurrences > len(self.collation.witnesses) or len(inter)> potential_block.minimum_block_length:
#                     print("Removing block: "+str(potential_block))
                    potential_blocks.remove(potential_block)
                    break

    def _build_occurrences_to_vertices(self, collation, witness, block_witness, token_to_vertex, transposed_tokens, occurrence_to_vertices):
        witness_range = collation.get_range_for_witness(witness.sigil)
        token_counter = witness_range[0]
        # note: this can be done faster by focusing on the occurrences
        # instead of the tokens
        for token in witness.tokens():
            for occurrence in block_witness.occurrences:
                if occurrence.is_in_range(token_counter) and not token in transposed_tokens and token in token_to_vertex:
                    vertex = token_to_vertex[token]
                    occurrence_to_vertices.setdefault(occurrence, []).append(vertex)
            token_counter += 1
        return occurrence_to_vertices

    #TODO: it should be possible to do this simpler, faster
    # An occurrence should know its tokens, since it knows its token range
    def _build_occurrences_to_tokens(self, collation, witness, block_witness):
        occurrence_to_tokens = {}
        witness_range = collation.get_range_for_witness(witness.sigil)
        token_counter = witness_range[0]
        # note: this can be done faster by focusing on the occurrences
        # instead of the tokens
        for token in witness.tokens():
            for occurrence in block_witness.occurrences:
                if occurrence.is_in_range(token_counter):
                    occurrence_to_tokens.setdefault(occurrence, []).append(token)
            token_counter += 1
        return occurrence_to_tokens
