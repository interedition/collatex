"""
Created on: 3 October 2020
@author: Ronald Haentjens Dekker

"""


# for 3 or more witnesses+++
class VertexToTokenPositionMatch(object):
    def __init__(self, vertex, token):
        self.vertex = vertex
        self.token = token

    def __repr__(self):
        return str.format("Match(vertex={},token={}", self.vertex, self.token)


# returns matches as (vertex -> index in the token array mapping)
def potential_vertex_token_matches(token_index, witness, token_to_vertex_array):
    instances = token_index.block_instances_for_witness(witness)
    # print("> token_index.witness_to_block_instances", token_index.witness_to_block_instances)
    # print("> instances", instances)
    start_token_position_for_witness = token_index.start_token_position_for_witness(witness)
    # print("> start_token_position_for_witness=", start_token_position_for_witness)
    return potential_vertex_token_matches_for_specific_instances(instances, start_token_position_for_witness,
                                                                 token_to_vertex_array)
    pass


# This version is for multiple witnesses.
# For just two witnesses we can do something simpler.
# NOTE: This version could just extend the 2 witnesses version, that might be simpler.
def potential_vertex_token_matches_for_specific_instances(instances, start_token_position_for_witness,
                                                          token_to_vertex_array):
    matches = []
    # print("> vertex_array =", vertex_array)
    for witness_instance in instances:
        # print("> witness_instance=", witness_instance)
        block = witness_instance.block
        all_instances = block.get_all_instances()
        instances_in_graph = [i for i in all_instances if i.start_token < start_token_position_for_witness]
        for instance_in_graph in instances_in_graph:
            graph_start_token = instance_in_graph.start_token
            for i in range(0, block.length):
                # print("> graph_start_token + i =", (graph_start_token + i))
                v = token_to_vertex_array[graph_start_token + i]
                if v is None:
                    raise Exception(
                        str.format('Vertex is null for token {} {} that is supposed to be mapped to a vertex in'
                                   ' the graph!', graph_start_token, i))
                witness_start_token = witness_instance.start_token + i
                matches.append(VertexToTokenPositionMatch(v, witness_start_token))
    return matches
