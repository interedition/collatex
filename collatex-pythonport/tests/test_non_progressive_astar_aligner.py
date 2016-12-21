import unittest
from collections import defaultdict
from operator import attrgetter
from typing import List, Mapping

from collatex.core_classes import Collation, Witness
from collatex.extended_suffix_array import Occurrence

from collatex.tokenindex import TokenIndex, LCPInterval


class ExperimentalAligner(object):
    def __init__(self, witnesses: List[Witness], witness_to_intervals: Mapping[str, List[LCPInterval]], token_index: TokenIndex):
        self.witnesses = witnesses
        self.witness_to_intervals = witness_to_intervals
        self.token_index = token_index

    def align(self):
        print("Take the second witness and show all the block occurrences!")
        # witness1 = self.witnesses[0]
        witness2 = self.witnesses[1]
        print(witness2)
        occurrences = self._internal_method_to_get_all_block_occurrences(witness2)
        print(occurrences)
        self._group_block_occurrences_by_start_token_position(occurrences)
        pass

    def _internal_method_to_get_all_block_occurrences(self, witness: Witness):
        blocks = self.witness_to_intervals[witness.sigil]
        range_witness = self.token_index.get_range_for_witness(witness.sigil)
        # make a selection of blocks and occurrences of these blocks in the selected witness
        occurrences = []
        for block in blocks:
            block_ranges_in_witness = block._as_range() & range_witness
            # note this are multiple ranges
            # we need to iterate over every single one
            for block_range in block_ranges_in_witness.contiguous():
                occurrence = Occurrence(block_range, block)
                occurrences.append(occurrence)
        # sort occurrences on position
        sorted_o = sorted(occurrences, key=attrgetter('lower_end'))
        return sorted_o

    # There are at most two block occurrences per token start position
    def _group_block_occurrences_by_start_token_position(self, occurrences: List[Occurrence]):
        occurrences_per_position = defaultdict(list)
        for o in occurrences:
            occurrences_per_position[o.token_range[0]].append(o)
        print(occurrences_per_position)
        pass



class Test(unittest.TestCase):
    def test_astar_experiment(self):

        w1 = "a b c d F g h i ! K ! q r s t"
        w2 = "a b c d F g h i ! q r s t"
        w3 = "a b c d E g h i ! q r s t"

        # create collation object
        collation = Collation()
        collation.add_plain_witness("W1", w1)
        collation.add_plain_witness("W2", w2)
        collation.add_plain_witness("W3", w3)

        # we start with calculating the token index
        # with three witnesses
        token_index = TokenIndex(collation.witnesses)
        token_index.prepare()

        intervals = token_index.split_lcp_array_into_intervals()
        # We need to code to go from ALL intervals to intervals for a superwitness and a next witness!
        # print(intervals)

        # We want to map all the occurrences of an interval (= block) to a witness
        # this method is the same as the "constructWitnessToBlockInstancesMap" method.
        # in the python version tokens don't know their witness
        # so we create a witness to block dictionary instead
        witness_to_intervals = defaultdict(list)
        for interval in intervals:
            # sigli only!
            witnesses = interval.witnesses
            for witness in witnesses:
                witness_to_intervals[witness].append(interval)

        aligner = ExperimentalAligner(collation.witnesses, witness_to_intervals, token_index)
        aligner.align()
        self.fail()

        pass
    


    
    pass