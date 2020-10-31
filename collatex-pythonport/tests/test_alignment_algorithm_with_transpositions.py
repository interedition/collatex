"""
Created on Oct 3, 2020

@author: Ronald Haentjens Dekker
"""
import unittest


from collatex import Collation
from collatex.collation_with_transposition import collate_with_transposition, \
    potential_instance_to_instance_matches, find_most_unique_blocks


class Test(unittest.TestCase):
    def test1(self):
        # w1: the red and the black cat
        # w2: the black and the red cat
        collation = Collation()
        collation.add_plain_witness("w1", "the red and the black cat")
        collation.add_plain_witness("w2", "the black and the red cat")
        token_index = collate_with_transposition(collation)
        # print(token_index.blocks)

        # we need to convert the intervals into matches
        # Then do the transposition detection
        # hmm I need the block index in that dataframe.
        # We need to get all the instances for a witness.
        # Then we filter so that we only take them if they are part of the important blocks.

        # We ask for all the potential matches (warning this is a lot)
        x = potential_instance_to_instance_matches(token_index, collation.witnesses[1])
        print(x)

        most_unique_blocks = find_most_unique_blocks(token_index)
        print(most_unique_blocks)
        # filter the potential matches on the most unique blocks.
        matches_to_look_at = [item for item in x if item.block_instance1.block in most_unique_blocks]
        print(matches_to_look_at)

        # We need to see whether any of the potential matches to consider are transposed.
        # sort on token position instance 1, if tie, than sort on token position Instance 2
        a = sorted(matches_to_look_at,key=lambda item: (item.block_instance1.start_token, item.block_instance2.start_token))
        b = sorted(matches_to_look_at,key=lambda item: (item.block_instance2.start_token, item.block_instance1.start_token))
        print(a)
        print(b)
        pass



if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testName']
    unittest.main()

    # NOTES:
    # I already have code in the Java and the Python version to convert a variant graph, a token index
    # and a given witness into a set of matches.
    # That code is in the Match Cube class in the Edit_graph_aligner file.
    # A match is currently defined as a (vertex, token) combination.
    # the vertex array is a token array -> vertex mapping for easy access into the variant graph.
    # the vertex ranking is a number assigned to each vertex. This can change after an addition to the graph.

    # after that we have to sort the matches twice, once by witness a order, tie -> witness b order.
    # and once by witness b order, tie -> witness a order.

    # This calls the traditional Needleman-Wunsch based alignment.
    # alignment_table = collate(collation)
    # print(alignment_table)

    # There is this annoying cross between going witness by witness and working with the most unique blocks first.
    # workaround
    # We need to convert the blocks to instances in the second witness
    # We take the most prominent blocks
    # and take the witness 2 instances
    # We also need to create the token to vertex array
    # By creating a VG and merging the first witness in.
