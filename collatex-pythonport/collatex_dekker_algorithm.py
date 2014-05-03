'''
Created on May 3, 2014

@author: Ronald Haentjens Dekker
'''
from collatex_core import CollationAlgorithm

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
        #TODO: tokens parameter can be removed here!
        graph_occurrence_to_vertices = self._build_occurrences_to_vertices(collation, first_witness, token_to_vertex)    
        # step 3: Build the occurrence to tokens map for the second witness
        second_witness = collation.witnesses[1]
        witness_occurrence_to_tokens = self._build_occurrences_to_tokens(collation, second_witness)
        
        print(graph_occurrence_to_vertices)
        print(witness_occurrence_to_tokens)
        
        
        
        
        
        
        
        # UNFINISHED BUSINESS!
#         # step 4: Generate token to vertex alignment map for second 
#         # witness, based on block to vertices map
#         alignment = self.get_alignment(graph_occurrence_to_vertices, witness_occurrence_to_tokens)
#         #print(alignment)
#         self.merge(graph, tokens, alignment)

        pass

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
        

    def _build_occurrences_to_tokens(self, collation, witness):
        occurrence_to_tokens = {}
        witness_range = collation.get_range_for_witness(witness.sigil)
        token_counter = witness_range[0]
        block_witness = collation.get_block_witness(witness)
        # note: this can be done faster by focusing on the occurrences
        # instead of the tokens
        for token in witness.tokens():
            for occurrence in block_witness.occurrences:
                if occurrence.is_in_range(token_counter):
                    occurrence_to_tokens.setdefault(occurrence, []).append(token)
            token_counter += 1
        return occurrence_to_tokens

    def get_alignment(self, block_to_vertices, block_to_tokens):
        alignment = {}
        for block in block_to_tokens:
            tokens = block_to_tokens[block]
            vertices = block_to_vertices[block]
            for token, vertex in zip(tokens, vertices):
                alignment[token]=vertex
        return alignment        
    
    
