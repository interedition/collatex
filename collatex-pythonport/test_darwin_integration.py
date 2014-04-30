'''
Created on Apr 20, 2014

Darwin Integration test

@author: Ronald Haentjens Dekker
'''
import json
from collatex_suffix import Collation, DekkerSuffixAlgorithmn
from collatex_core import VariantGraph
from networkx.drawing.nx_pydot import to_pydot
from linsuffarr import LCP
from pprint import pprint

if __name__ == '__main__':
    # read source data
    json_data=open('darwin_chapter1_para1.json')
    data = json.load(json_data)
    json_data.close()
    #pprint(data)
    
    first_witness = data["witnesses"][0]
    second_witness = data["witnesses"][1]

    # generate collation object from json_data    
    collation = Collation()
    collation.add_witness(first_witness["id"], first_witness["content"])
    collation.add_witness(second_witness["id"], second_witness["content"])
    
#     print(collation.get_lcp_array())
    lcp_intervals = collation.get_lcp_intervals()
#     print(lcp_intervals)

    blocks = collation.get_non_overlapping_repeating_blocks()
     
    #pprint(blocks)
    
    block_witness = collation.get_first_block_witness()
    
    print(block_witness.debug())
    
    
# #     
#     graph = VariantGraph()
#     collationAlgorithm = DekkerSuffixAlgorithmn()
#     collationAlgorithm.buildVariantGraphFromBlocks(graph, collation)
#     
#     #THIS DOES NOT WORK FOR SOME REASON!
#     #install pygraphviz first
#     #view_pygraphviz(graph.graph)
#     
#     #trying pydot
#     dot = to_pydot(graph.graph)
#     dot.write("rawoutput")
#     
#     #dot command
#     #-Grankdir=LR -Gid=VariantGraph -Tsvg
#     
    pass