'''
    Created on May 3, 2014
        
    @author: Ronald Haentjens Dekker
'''
from collatex.core_classes import VariantGraphRanking

# New transposition detection implementation, still beta quality
# This implementation works with the new collation algorithm (LCP intervals and edit graph)
class TranspositionDetection(object):

    def __init__(self, aligner):
        self.aligner = aligner

    def detect(self):
        # analyse additions and omissions to detect transpositions
        # We fetch all the occurrences of the added tokens
        # Using the scorer (which has the blocks and occurrences of these blocks)
        added_occurrences = set()
        for token in self.aligner.additions:
            # get occurrences from scorer
            occurrence = self.aligner.scorer.global_tokens_to_occurrences[token]
            # Note: not every token is an occurrence of a block
            if occurrence:
                added_occurrences.add(occurrence)
        # for every occurrences we have to detect the associated block
        added_blocks = set()
        added_blocks_dict = {}
        for occurrence in added_occurrences:
            added_blocks.add(occurrence.block)
            added_blocks_dict[occurrence.block]=occurrence
        print("Added blocks: "+str(added_blocks))
        # Fetch all omitted block
        omitted_occurrences = set()
        for token in self.aligner.omissions:
            # get occurrences from scorer
            occurrence = self.aligner.scorer.global_tokens_to_occurrences[token]
            if occurrence:
                omitted_occurrences.add(occurrence)
        # for every occurrences we have to detect the associated block
        omitted_blocks = set()
        omitted_blocks_dict = {}
        for occurrence in omitted_occurrences:
            omitted_blocks.add(occurrence.block)
            omitted_blocks_dict[occurrence.block]=occurrence
        print("omitted blocks: "+str(omitted_blocks))

        # calculate transpositions by taking the intersection of the two sets
        transposed_blocks = omitted_blocks.intersection(added_blocks)
        print ("transposed blocks: "+str(transposed_blocks))


        # for now assume that there is only one occurrence for every block
        # otherwise we skip
        for block in transposed_blocks:
            occurrence1 = added_blocks_dict[block]
            occurrence2 = omitted_blocks_dict[block]
            # we need to go from the occurrences to the tokens
            token_positions = zip(occurrence1.token_range, occurrence2.token_range)
            for (token_position_base, token_position_witness) in token_positions:
                token_base = self.aligner.collation.tokens[token_position_base]
                token_witness = self.aligner.collation.tokens[token_position_witness]
                print(token_base, token_witness)








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
        previous = base.start

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
        return phrase_matches

#=================================================
# Almost fully direct port from Java code
#=================================================
class TranspositionDetector(object):
    def detect(self, phrasematches, base):
        if not phrasematches:
            return []

        ranking = self._rank_the_graph(phrasematches, base)

        def compare_phrasematches(pm1, pm2):
            (vertex1, _) = pm1[0]
            (vertex2, _) = pm2[0]
            rank1 = ranking.apply(vertex1)
            rank2 = ranking.apply(vertex2)
            difference = rank1 - rank2

            if difference != 0:
                return difference
            index1 = phrasematches.index(pm1)
            index2 = phrasematches.index(pm2)
            return index1 - index2

        phrasematches_graph_order = sorted(phrasematches, cmp=compare_phrasematches)

        # map 1
        self.phrasematch_to_index = {}
        for idx, val in enumerate(phrasematches_graph_order):
            self.phrasematch_to_index[val[0]]=idx

        # We calculate the index for all the phrase matches
        # First in witness order, then in graph order
        phrasematches_graph_index = range(0, len(phrasematches))

        phrasematches_witness_index = []
        for phrasematch in phrasematches:
            phrasematches_witness_index.append(self.phrasematch_to_index[phrasematch[0]])

        # initialize result variables
        non_transposed_phrasematches = list(phrasematches)
        transpositions = []

        # loop here until the maximum distance == 0
        while(True):
            # map 2
            phrasematch_to_distance = {}
            for i, phrasematch in enumerate(non_transposed_phrasematches):
                graph_index = phrasematches_graph_index[i]
                witness_index = phrasematches_witness_index[i]
                distance = abs(graph_index - witness_index)
                phrasematch_to_distance[phrasematch[0]]=distance

            distance_list = list(phrasematch_to_distance.values())

            if not distance_list or max(distance_list) == 0:
                break

            def comp2(pm1, pm2):
                # first order by distance
                distance1 = phrasematch_to_distance[pm1[0]]
                distance2 = phrasematch_to_distance[pm2[0]]
                difference = distance2 - distance1
                if difference != 0:
                    return difference

                # second order by size
                #TODO: this does not work for Greek texts with lots of small words!
                #TODO: it should determine which block this phrasematch is part of and
                #TODO: the number of occurrences for that block
                return len(pm1) - len(pm2)

            sorted_phrasematches = sorted(non_transposed_phrasematches, cmp = comp2) 
            transposedphrase = sorted_phrasematches[0]

            transposed_index = self.phrasematch_to_index[transposedphrase[0]]
            graph_index = phrasematches_graph_index.index(transposed_index)
            transposed_with_index = phrasematches_witness_index[graph_index]
            linked_transposed_phrase = phrasematches_graph_order[transposed_with_index]

            self._add_transposition(phrasematches_witness_index, phrasematches_graph_index, non_transposed_phrasematches, transpositions, transposedphrase)

            distance = phrasematch_to_distance[transposedphrase[0]]
            if distance == phrasematch_to_distance[linked_transposed_phrase[0]] and distance > 1:
                self._add_transposition(phrasematches_witness_index, phrasematches_graph_index, non_transposed_phrasematches, transpositions, linked_transposed_phrase)

        return transpositions

    def _add_transposition(self, phrasematches_witness_index, phrasematches_graph_index, non_transposed_phrasematches, transpositions, transposed_phrase):
        index_to_remove = self.phrasematch_to_index[transposed_phrase[0]]
        non_transposed_phrasematches.remove(transposed_phrase)
        transpositions.append(transposed_phrase)
        phrasematches_graph_index.remove(index_to_remove)
        phrasematches_witness_index.remove(index_to_remove)

    def _rank_the_graph(self, phrase_matches, base):
        #TODO: rank the graph based on only the first vertex of each of the phrasematches!
        return VariantGraphRanking.of(base)