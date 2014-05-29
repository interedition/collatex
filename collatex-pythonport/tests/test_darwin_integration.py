'''
Created on Apr 20, 2014

Darwin Integration test

@author: Ronald Haentjens Dekker
'''
import json
from networkx.drawing.nx_agraph import write_dot
from collatex.collatex_suffix import Collation
from collatex.collatex_core import VariantGraph, join
from collatex.collatex_dekker_algorithm import DekkerSuffixAlgorithm

if __name__ == '__main__':
    # read source data
    json_data=open('darwin_chapter1_para1.json')
    data = json.load(json_data)
    json_data.close()
    #pprint(data)
    
    first_witness = data["witnesses"][0]
    second_witness = data["witnesses"][1]
    third_witness = data["witnesses"][2]
    fourth_witness = data["witnesses"][3]
    fifth_witness = data["witnesses"][4]
    sixth_witness = data["witnesses"][5]

    # generate collation object from json_data    
    collation = Collation()
    collation.add_witness(first_witness["id"], first_witness["content"])
    collation.add_witness(second_witness["id"], second_witness["content"])
    collation.add_witness(third_witness["id"], third_witness["content"])
    collation.add_witness(fourth_witness["id"], fourth_witness["content"])
    collation.add_witness(fifth_witness["id"], fifth_witness["content"])
    collation.add_witness(sixth_witness["id"], sixth_witness["content"])

#     
    #print(collation.get_lcp_array())
    #    print(collation.get_non_overlapping_repeating_blocks())

    print(collation.get_block_witness(collation.witnesses[0]).debug())
    print(collation.get_block_witness(collation.witnesses[1]).debug())
    print(collation.get_block_witness(collation.witnesses[2]).debug())
    print(collation.get_block_witness(collation.witnesses[3]).debug())
    print(collation.get_block_witness(collation.witnesses[4]).debug())
    print(collation.get_block_witness(collation.witnesses[5]).debug())

    
    graph = VariantGraph()
    collationAlgorithm = DekkerSuffixAlgorithm()
    collationAlgorithm.build_variant_graph_from_blocks(graph, collation)
      
    join(graph)
      
    #install pygraphviz first
    #view_pygraphviz(graph.graph)
    write_dot(graph.graph, "rawoutput") 
     
#     #trying pydot
#     dot = to_pydot(graph.graph)
#     dot.write("rawoutput")
#     
#     #dot command
#     #-Grankdir=LR -Gid=VariantGraph -Tsvg
#     
    pass