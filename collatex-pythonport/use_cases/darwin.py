'''
Created on Apr 20, 2014

Darwin Integration test

@author: Ronald Haentjens Dekker
'''
import json
from collatex import Collation, collate

if __name__ == '__main__':
    # read source JSON data into dictionary 
    json_data=open('darwin_chapter1_para1.json')
    data = json.load(json_data)
    json_data.close()
    #pprint(data)
    
    # generate collation object from dictionary    
    collation = Collation.create_from_dict(data)

    print(collate(collation))
    
#     write_dot(graph.graph, "rawoutput") 
