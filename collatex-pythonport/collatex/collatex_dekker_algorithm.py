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
        token_to_vertex = self.merge(graph, tokens)
        # step 2: Build the initial occurrence to list vertex map 
        graph_occurrence_to_vertices = self._build_occurrences_to_vertices(collation, first_witness, token_to_vertex)    
        # step 3: Build the occurrence to tokens map for the second witness
        second_witness = collation.witnesses[1]
        block_witness = collation.get_block_witness(second_witness)
        witness_occurrence_to_tokens = self._build_occurrences_to_tokens(collation, second_witness, block_witness)
        
#         print(graph_occurrence_to_vertices)
#         print(witness_occurrence_to_tokens)
        
        # map graph occurrences to their block
        graph_block_to_occurrences = {}
        for graph_occurrence in graph_occurrence_to_vertices:
            block = graph_occurrence.block
            graph_block_to_occurrences.setdefault(block, []).append(graph_occurrence)
        
        alignment = self._align(graph_block_to_occurrences, graph_occurrence_to_vertices, witness_occurrence_to_tokens, block_witness)
        self.merge(graph, second_witness.tokens(), alignment)
        pass

    #===========================================================================
    # graph block to occurrences: every block that is present in the graph mapped to
    # its occurrences
    # graph occurrence to vertices: maps every graph occurrence of a block to a list
    # of vertices 
    # block_witness: a witness represented as a list of occurrences of blocks
    #===========================================================================
    def _align(self, graph_block_to_occurrences, graph_occurrence_to_vertices, witness_occurrence_to_tokens, block_witness):
        # step 4: Generate token to vertex alignment map for second 
        # witness, based on block to vertices map
        alignment = {}
        for witness_occurrence in block_witness.occurrences:
            witness_block = witness_occurrence.block
            if not witness_block in graph_block_to_occurrences:
                continue
            graph_occurrences = graph_block_to_occurrences[witness_block]
            #TODO: the witness_block could also not be present!
            if len(graph_occurrences)>1:
                print(witness_block)
                #TODO: we have to make a decision here!
                continue        
            graph_occurrence = graph_occurrences[0]
            tokens = witness_occurrence_to_tokens[witness_occurrence]
            vertices = graph_occurrence_to_vertices[graph_occurrence]
    
            for token, vertex in zip(tokens, vertices):
                alignment[token]=vertex
#         print(alignment)
        return alignment
        
    def _build_occurrences_to_vertices(self, collation, witness, token_to_vertex):
        occurrence_to_vertices = {}
        #TODO: for any witness outside of the first witness the token counter needs to start at lower end of the witness range!
        token_counter = 0 
        block_witness = collation.get_block_witness(witness)
        # note: this can be done faster by focusing on the occurrences
        # instead of the tokens
        for token in witness.tokens():
            for occurrence in block_witness.occurrences:
                if occurrence.is_in_range(token_counter):
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

