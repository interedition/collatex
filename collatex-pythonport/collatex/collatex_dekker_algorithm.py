'''
Created on May 3, 2014

@author: Ronald Haentjens Dekker
'''
from collatex.collatex_core import CollationAlgorithm

class DekkerSuffixAlgorithm(CollationAlgorithm):
    
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
        self._build_occurrences_to_vertices(collation, first_witness, token_to_vertex, graph_occurrence_to_vertices) 
        # step 3: Build the occurrence to tokens map for the second witness
        second_witness = collation.witnesses[1]
        block_witness = collation.get_block_witness(second_witness)
        witness_occurrence_to_tokens = self._build_occurrences_to_tokens(collation, second_witness, block_witness)
        # step 4: align and merge second witness
        alignment = self._align(graph_occurrence_to_vertices, witness_occurrence_to_tokens, block_witness)
        token_to_vertex = self.merge(graph, second_witness.sigil, second_witness.tokens(), alignment)
        # step 5: update the occurrences to vertex map with the new vertices created for the second witness
        self._build_occurrences_to_vertices(collation, second_witness, token_to_vertex, graph_occurrence_to_vertices)    
        # step 6: add third witness
        # NOTE: third witness might not have to be there!
        if len(collation.witnesses)>2:
            third_witness = collation.witnesses[2]
            block_witness = collation.get_block_witness(third_witness)
            witness_occurrence_to_tokens = self._build_occurrences_to_tokens(collation, third_witness, block_witness)
            # step 6: align and merge third witness
            alignment = self._align(graph_occurrence_to_vertices, witness_occurrence_to_tokens, block_witness)
            token_to_vertex = self.merge(graph, third_witness.sigil, third_witness.tokens(), alignment)
            self._build_occurrences_to_vertices(collation, third_witness, token_to_vertex, graph_occurrence_to_vertices)    
        # step 7: add fourth witness
        if len(collation.witnesses)>3:
            fourth_witness = collation.witnesses[3]
            block_witness = collation.get_block_witness(fourth_witness)
            witness_occurrence_to_tokens = self._build_occurrences_to_tokens(collation, fourth_witness, block_witness)
            # step 8: align and merge witness
            alignment = self._align(graph_occurrence_to_vertices, witness_occurrence_to_tokens, block_witness)
            token_to_vertex = self.merge(graph, fourth_witness.sigil, fourth_witness.tokens(), alignment)
            self._build_occurrences_to_vertices(collation, fourth_witness, token_to_vertex, graph_occurrence_to_vertices)    
        # step 9: add fifth witness
        if len(collation.witnesses)>4:
            fifth_witness = collation.witnesses[4]
            block_witness = collation.get_block_witness(fifth_witness)
            witness_occurrence_to_tokens = self._build_occurrences_to_tokens(collation, fifth_witness, block_witness)
            # step 10: align and merge witness
            alignment = self._align(graph_occurrence_to_vertices, witness_occurrence_to_tokens, block_witness)
            token_to_vertex = self.merge(graph, fifth_witness.sigil, fifth_witness.tokens(), alignment)
            self._build_occurrences_to_vertices(collation, fifth_witness, token_to_vertex, graph_occurrence_to_vertices)    
        # step: add sixth witness
        if len(collation.witnesses)>5:
            sixth_witness = collation.witnesses[5]
            block_witness = collation.get_block_witness(sixth_witness)
            witness_occurrence_to_tokens = self._build_occurrences_to_tokens(collation, sixth_witness, block_witness)
            # step: align and merge witness
            alignment = self._align(graph_occurrence_to_vertices, witness_occurrence_to_tokens, block_witness)
            token_to_vertex = self.merge(graph, sixth_witness.sigil, sixth_witness.tokens(), alignment)
            # self._build_occurrences_to_vertices(collation, fifth_witness, token_to_vertex, graph_occurrence_to_vertices)    
        
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
        
    def _build_occurrences_to_vertices(self, collation, witness, token_to_vertex, occurrence_to_vertices):
        witness_range = collation.get_range_for_witness(witness.sigil)
        token_counter = witness_range[0]
        block_witness = collation.get_block_witness(witness)
        # note: this can be done faster by focusing on the occurrences
        # instead of the tokens
        for token in witness.tokens():
            for occurrence in block_witness.occurrences:
                if occurrence.is_in_range(token_counter) and token in token_to_vertex:
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

    
    #===========================================================================
    # Direct port from Java code
    #===========================================================================
class PhraseMatchDetector(object):
    def _add_new_phrase_match_and_clear_buffer(self, phrase_matches, base_phrase, witness_phrase):
        if base_phrase:
            phrase_matches.append(zip(base_phrase, witness_phrase)) 
            del base_phrase[:]
            del witness_phrase[:]

    def detect(self, linked_tokens, base, tokens):
        phrase_matches = []
        base_phrase = []
        witness_phrase = []
        previous = base.get_start()
        
        for token in tokens:
            if not token in linked_tokens:
                self._add_new_phrase_match_and_clear_buffer(phrase_matches, base_phrase, witness_phrase)
                continue
            base_vertex = linked_tokens[token]
            # requirements:
            # - see comments in java class
            same_transpositions = True #TODO
            same_witnesses = True #TODO
            directed_edge = base.edge_between(previous, base_vertex)
            is_near = same_transpositions and same_witnesses and directed_edge and len(base.out_edges(previous))==1 and len(base.in_edges(base_vertex))==1
            if not is_near:
                self._add_new_phrase_match_and_clear_buffer(phrase_matches, base_phrase, witness_phrase)
            base_phrase.append(base_vertex)
            witness_phrase.append(token)
            previous = base_vertex
        if base_phrase:
            phrase_matches.append(zip(base_phrase, witness_phrase)) 

