'''
Created on Aug 5, 2014

@author: Ronald Haentjens Dekker
'''
from collatex.extended_suffix_array import Occurrence, BlockWitness, Block,\
    PartialOverlapException
from operator import attrgetter
from ClusterShell.RangeSet import RangeSet
from queue import PriorityQueue
#TODO: different in Python 2?
# optionally load the Levenshtein dependency for near match functionality.
# Consists of C code, needs to be compiled which is problematic on Windows.
# There are pre-compiled binaries however, but this requires an extra step
# during installation.
try:
    from Levenshtein import ratio
except:
    pass


'''
 This class scores cells in the edit graph table based on the largest non overlapping blocks
 that are found in the witness set.
'''

class Scorer(object):
    def __init__(self, collation, near_match=False, properties_filter=None):
        self.collation = collation
        self.blocks = []
        self.global_tokens_to_occurrences = {}
        self.properties_filter=properties_filter
        if near_match:
            self.match_function = self.near_match
        else:
            self.match_function = self.match
        
    # edit operation:
    #    0 == match/replacement
    #    1 == addition/omission
    def score_cell(self, table_node, parent_node, token_a, token_b, y, x, edit_operation):
        # no matching possible in this case (always treated as a gap)
        # it is either an add or a delete
        if x == 0 or y == 0:
            table_node.g = parent_node.g - 1
            return

        # it is either an add/delete or replacement (so an add and a delete)
        # it is a replacement
        if edit_operation == 0:
            match = self.match_function(token_a, token_b)
#             print("testing "+token_a.token_string+" and "+token_b.token_string+" "+str(match))
            # match = token_a.token_string == token_b.token_string
            # based on match or not and parent_node calculate new score
            if match==0:
                # mark the fact that this node is match
                table_node.match = True
                # do not change score for now
                table_node.g = parent_node.g
                # count segments
                if parent_node.match == False:
                    table_node.segments = parent_node.segments + 1
                return
            if match==1:
                table_node.g = parent_node.g - 0.5 #TODO: TEST TEST TEST
                pass
            else:
                table_node.g = parent_node.g - 2
                return
        # it is an add/delete
        else:
            table_node.g = parent_node.g - 1
            return

    def heuristic_score(self, node, length_witness_a, length_witness_b):
        heuristic_gap_penalty = 0

        # step 1: check blocks for all the base tokens
        #TODO: implement!
        #TODO: isn't the correct heuristic to count all the tokens that do not have a block associated with them?
        #TODO: for the base as the witness?


        # step 2: add costs to get to the end to the heuristic gap penalty
        distance_to_end_node_horizontal = (length_witness_a - node.x)
        distance_to_end_node_vertical = (length_witness_b - node.y)
        gap_penalty = abs(distance_to_end_node_horizontal - distance_to_end_node_vertical)
        #         print("heuristic penalty: "+str(node.y)+" "+str(node.x)+" penalty: "+str(-gap_penalty))
        return gap_penalty


    def prepare_witness(self, witness):
        # this code can probably done more efficient, for now the main goal is to make it work
        block_witness = self._get_block_witness(witness)
        tokens_to_occurrences = self._build_tokens_to_occurrences(self.collation, witness, block_witness)
        #NOTE: we do not have to store all tokens from the witness
        #NOTE: if we split the dict into two: one for next witness and one for superbase
        #NOTE: only the ones that are going to be aligned have to be stored in superbase dict.
        self.global_tokens_to_occurrences.update(tokens_to_occurrences)
        
    # return values:
    # 0 = FULL_MATCH
    # -1 = NO MATCH
    # 1 = PARTIAL MATCH
    def match(self, token_a, token_b):
        # now we need to determine whether this node represents a match
        # determine this based on whether token a and token b are part of the same block
        occur_a = self.global_tokens_to_occurrences.setdefault(token_a, None)
        occur_b = self.global_tokens_to_occurrences.setdefault(token_b, None)
        if occur_a and occur_b:
            match = occur_a.block == occur_b.block
        else:
            match = False
        if match:
            # Check whether the user has supplied a properties filter
            if not self.properties_filter:
                return 0
            match = self.properties_filter(token_a.token_data, token_b.token_data)
            if match:
                return 0
            else:
                return -1
        else:
            return -1

    def near_match(self, token_a, token_b):
        result = self.match(token_a, token_b)
        if result==0:
            return 0
        r = ratio(token_a.token_string, token_b.token_string)
        print(str(token_a)+" "+str(token_b)+" "+str(r))
        if r > 0.6: 
            return 1
        else:
            return -1
        pass
    
    #TODO: it should be possible to do this simpler, faster
    # An occurrence should know its tokens, since it knows its token range
    def _build_tokens_to_occurrences(self, collation, witness, block_witness):
        tokens_to_occurrence = {}
        witness_range = collation.get_range_for_witness(witness.sigil)
        token_counter = witness_range[0]
        # note: this can be done faster by focusing on the occurrences
        # instead of the tokens
        for token in witness.tokens():
            for occurrence in block_witness.occurrences:
                if occurrence.is_in_range(token_counter):
                    tokens_to_occurrence[token]=occurrence
            token_counter += 1
        return tokens_to_occurrence

    

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

        # The LCP intervals that are calculated from the extend suffix array are all potential blocks.
        # However some potential blocks overlap. To decide the definitive blocks we sort the potential blocks on the
        # amount of witnesses they occur in.
        potential_blocks = extended_suffix_array.split_lcp_array_into_intervals()
        print(potential_blocks)
        # we add all the intervals to a priority queue based on 1) number of witnesses 2) block length
        queue = PriorityQueue()
        for interval in potential_blocks:
            queue.put(interval)

        occupied = RangeSet()
        real_blocks = []

        while not queue.empty():
            item = queue.get()
            print(item)
            # test intersection with occupied
            potential_block_range = item._as_range()
            # check the intersection with the already occupied ranges
            block_intersection = potential_block_range.intersection(occupied)
            if not block_intersection:
                print("Selected!")
                occupied.union_update(potential_block_range)
                real_blocks.append(Block(potential_block_range))
                continue

            # check complete overlap or partial
            if block_intersection == potential_block_range:
                print("complete overlap; skip")
                continue

            # partial overlap
            # we construct new pieces
            # by walking over all the occurrences of the block
            # a single occurrence can either be completely overlapped or partially overlapped
            block_ranges_of_new_lcp_interval = []
            # NOTE: it is not as simple as this!
            for occurrence in item.block_occurrences():
                occurrence_range = RangeSet()
                occurrence_range.add_range(occurrence, occurrence + item.minimum_block_length)
                print(occurrence_range)
                print(block_intersection)
                occurrence_difference = occurrence_range.difference(block_intersection)
                print(occurrence_difference)

                # complete overlap
                if not occurrence_difference:
                    continue

                # no overlap
                if occurrence_difference == occurrence_range:
                    print("Remaining: "+str(occurrence_difference))

                    # create new block TODO: and group by length
                    block_ranges_of_new_lcp_interval.append(occurrence_difference)

            # check whether block_ranges is empty or not (must have to continuous blocks)
            if (len(block_ranges_of_new_lcp_interval)) < 2:
                print("Skipped remaining")
            else:
                # we select them for now... not sure if that is the best way to deal with it!
                # otherwise we need to be able to add blocks to priority queue and extended
                #  the block class functionality to be more like LCP interval
                print("Selected remaining!")
                range_set = RangeSet()
                for block_range in block_ranges_of_new_lcp_interval:
                    range_set.union_update(block_range)

                occupied.union_update(range_set)
                real_blocks.append(Block(range_set))







        return real_blocks

        # # Sort the blocks based on number of witnesses first,
        # # second length of LCP interval,
        # # third sort on parent LCP interval occurrences.
        # sorted_blocks_on_priority = sorted(potential_blocks, key=attrgetter("number_of_witnesses", "minimum_block_length", "number_of_siblings"), reverse=True)
        # # Select the definitive blocks
        # occupied = RangeSet()
        # real_blocks = []
        # for potential_block in sorted_blocks_on_priority:
        #     # print(potential_block.info())
        #     try:
        #         non_overlapping_range = potential_block.calculate_non_overlapping_range_with(occupied)
        #         if non_overlapping_range:
        #             #                     print("Selecting: "+str(potential_block))
        #             occupied.union_update(non_overlapping_range)
        #             real_blocks.append(Block(non_overlapping_range))
        #     except PartialOverlapException:
        #         print("Skip due to conflict: "+str(potential_block))
        #         while potential_block.minimum_block_length > 1:
        #             # retry with a different length: one less
        #             for idx in range(potential_block.start+1, potential_block.end+1):
        #                 potential_block.LCP[idx] -= 1
        #             potential_block.length -= 1
        #             try:
        #                 non_overlapping_range = potential_block.calculate_non_overlapping_range_with(occupied)
        #                 if non_overlapping_range:
        #                     #                             print("Retried and selecting: "+str(potential_block))
        #                     occupied.union_update(non_overlapping_range)
        #                     real_blocks.append(Block(non_overlapping_range))
        #                     break
        #             except PartialOverlapException:
        #                 #                         print("Retried and failed again")
        #                 pass
        # return real_blocks
