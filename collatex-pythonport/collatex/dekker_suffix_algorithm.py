'''
Created on May 3, 2014

@author: Ronald Haentjens Dekker
'''
from collatex.collatex_core import CollationAlgorithm
from operator import attrgetter
from collatex.collatex_suffix import PartialOverlapException, Occurrence,\
    BlockWitness, Block
from ClusterShell.RangeSet import RangeSet
from collatex.collatex_dekker_algorithm import PhraseMatchDetector,\
    TranspositionDetector

class DekkerSuffixAlgorithm(CollationAlgorithm):
    def __init__(self, collation):
        self.blocks = None
        self.collation = collation

    def get_block_witness(self, witness):
        sigil_witness = witness.sigil
        range_witness = self.collation.get_range_for_witness(sigil_witness)
        #NOTE: to prevent recalculation of blocks
        if not self.blocks:
            self.blocks = self.get_non_overlapping_repeating_blocks() 
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

    def get_non_overlapping_repeating_blocks(self):
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

    def build_variant_graph_from_blocks(self, graph, collation):
        '''
        :type graph: VariantGraph
        :type collation: Collation
        '''
        # step 1: Build the variant graph for the first witness
        # this is easy: generate a vertex for every token
        first_witness = collation.witnesses[0]
        tokens = first_witness.tokens()
        token_to_vertex = self.merge(graph, first_witness.sigil, tokens)
        # step 2: Build the initial occurrence to list vertex map 
        graph_occurrence_to_vertices = {}
        self._build_occurrences_to_vertices(collation, first_witness, token_to_vertex, [], graph_occurrence_to_vertices) 

        # align witness 2 - n
        for x in range(1, len(collation.witnesses)):
            # step 3: Build the occurrence to tokens map for the next witness
            next_witness = collation.witnesses[x]
            block_witness = self.get_block_witness(next_witness)
            witness_occurrence_to_tokens = self._build_occurrences_to_tokens(collation, next_witness, block_witness)
            # step 4: align and merge next witness
            alignment = self._align(graph_occurrence_to_vertices, witness_occurrence_to_tokens, block_witness)
            # determine phrase matches
            phrase_match_detector = PhraseMatchDetector()
            phrasematches = phrase_match_detector.detect(alignment, graph, next_witness.tokens())
            #print(phrasematches)
            # transposition detector
            transposition_detector = TranspositionDetector()
            transpositions = transposition_detector.detect(phrasematches, graph)
#             if transpositions:
#                 print(transpositions)
            # transposed tokens can not be aligned
            transposed_tokens = []
            for transposition in transpositions:
                for (_, token) in transposition:
                    del alignment[token]
                    transposed_tokens.append(token)
            # merge
            token_to_vertex = self.merge(graph, next_witness.sigil, next_witness.tokens(), alignment)
            # step 5: update the occurrences to vertex map with the new vertices created for the second witness
            self._build_occurrences_to_vertices(collation, next_witness, token_to_vertex, transposed_tokens, graph_occurrence_to_vertices)    

    #===========================================================================
    # graph block to occurrences: every block that is present in the graph mapped to
    # its occurrences
    # graph occurrence to vertices: maps every graph occurrence of a block to a list
    # of vertices 
    # block_witness: a witness represented as a list of occurrences of blocks
    #===========================================================================
    def _align(self, graph_occurrence_to_vertices, witness_occurrence_to_tokens, block_witness):
        # map graph occurrences to their block
        graph_block_to_occurrences = {}
        for graph_occurrence in graph_occurrence_to_vertices:
            block = graph_occurrence.block
            graph_block_to_occurrences.setdefault(block, []).append(graph_occurrence)
        # generate the witness block to occurrence map to check whether some blocks occur multiple times
        # in the witness
        witness_block_to_occurrence={}
        for witness_occurrence in block_witness.occurrences:
            witness_block = witness_occurrence.block
            witness_block_to_occurrence.setdefault(witness_block, []).append(witness_occurrence)
        # Generate token to vertex alignment map for second 
        # witness, based on block to vertices map
        alignment = {}
        for witness_occurrence in block_witness.occurrences:
            witness_block = witness_occurrence.block
            #NOTE: the witness_block could also not be present
            if not witness_block in graph_block_to_occurrences:
#                 print(str(witness_block)+" missing in graph!")
                continue
            # check number of occurrences of block in witness
            # if larger than 1 we have to make a decision
            witness_occurrences = witness_block_to_occurrence[witness_block]
            if len(witness_occurrences)>1:
#                 print(str(witness_block)+" occurring multiple times in witness!")
                #TODO: we have to make a decision here!
                continue        
            # check number of occurrences of block in graph 
            # if larger than 1 we have to make a decision
            graph_occurrences = graph_block_to_occurrences[witness_block]
            if len(graph_occurrences)>1:
#                 print(str(witness_block)+" occurring multiple times in graph!")
                #TODO: we have to make a decision here!
                continue        
            graph_occurrence = graph_occurrences[0]
            tokens = witness_occurrence_to_tokens[witness_occurrence]
            vertices = graph_occurrence_to_vertices[graph_occurrence]
            for token, vertex in zip(tokens, vertices):
                alignment[token]=vertex
        return alignment

    def _build_occurrences_to_vertices(self, collation, witness, token_to_vertex, transposed_tokens, occurrence_to_vertices):
        witness_range = collation.get_range_for_witness(witness.sigil)
        token_counter = witness_range[0]
        block_witness = self.get_block_witness(witness)
        # note: this can be done faster by focusing on the occurrences
        # instead of the tokens
        for token in witness.tokens():
            for occurrence in block_witness.occurrences:
                if occurrence.is_in_range(token_counter) and not token in transposed_tokens and token in token_to_vertex:
                    vertex = token_to_vertex[token]
                    occurrence_to_vertices.setdefault(occurrence, []).append(vertex)
            token_counter += 1
        return occurrence_to_vertices

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
