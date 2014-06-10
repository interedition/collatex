'''
Created on Apr 20, 2014

Darwin Integration test

@author: Ronald Haentjens Dekker
'''
import json
from collatex import Collation, collate

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

    print(collate(collation))
    
#     write_dot(graph.graph, "rawoutput") 
