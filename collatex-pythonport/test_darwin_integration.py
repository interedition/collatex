'''
Created on Apr 20, 2014

Darwin Integration test

@author: Ronald Haentjens Dekker
'''
import json
from collatex_suffix import Collation
from collatex_core import VariantGraph, join
from collatex_dekker_algorithm import DekkerSuffixAlgorithm
from networkx.drawing.nx_agraph import write_dot

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