"""
Created on May 3, 2014

@author: Ronald Haentjens Dekker
"""
from collatex.core_classes import VariantGraph, Witness, join, AlignmentTable, Row, WordPunctuationTokenizer, \
    VariantGraphRanking
from collatex.extended_suffix_array import ExtendedSuffixArray
from collatex.exceptions import UnsupportedError
from collatex.experimental_astar_aligner import ExperimentalAstarAligner
from collatex.linsuffarr import SuffixArray, UNIT_BYTE
from ClusterShell.RangeSet import RangeSet
import json
from collatex.edit_graph_aligner import EditGraphAligner
from collatex.display_module import display_alignment_table_as_HTML, visualizeTableVerticallyWithColors
from collatex.display_module import display_variant_graph_as_SVG
from Levenshtein import ratio,distance

# Valid options for output are:
# "table" for the alignment table (default)
# "graph" for the variant graph
# "json" for the alignment table exported as JSON
def collate(collation, output="table", layout="horizontal", segmentation=True, near_match=False, astar=False, detect_transpositions=False, debug_scores=False, properties_filter=None, svg_output=None):
    if not astar:
        algorithm = EditGraphAligner(collation, near_match=near_match, detect_transpositions=detect_transpositions, debug_scores=debug_scores, properties_filter=properties_filter)
    else:
        algorithm = ExperimentalAstarAligner(collation, near_match=near_match, debug_scores=debug_scores)

    # build graph
    graph = VariantGraph()
    algorithm.collate(graph, collation)
    # join parallel segments
    if segmentation:
        join(graph)
    # check which output format is requested: graph or table
    if output == "svg":
        return display_variant_graph_as_SVG(graph,svg_output)
    if output=="graph": 
        return graph
    # create alignment table
    table = AlignmentTable(collation, graph, layout)
    if output == "json":
        return export_alignment_table_as_json(table)
    if output == "html":
        return display_alignment_table_as_HTML(table)
    if output == "html2":
        return visualizeTableVerticallyWithColors(table, collation)
    if output == "table":
        return table
    else:
        raise Exception("Unknown output type: "+output)
    
def collate_nearMatch(collation, output="table", detect_transpositions=False, layout=None, segmentation=False, debug_scores=False, properties_filter=None, svg_output=None):
    algorithm = EditGraphAligner(collation, detect_transpositions=detect_transpositions, debug_scores=debug_scores, properties_filter=properties_filter)

    # build graph
    graph = VariantGraph()
    algorithm.collate(graph, collation)
    ranking = VariantGraphRanking.of(graph)
    highestRank = ranking.byVertex[graph.end]
    witnessCount = len(collation.witnesses)

    # do-while loop to avoid looping through ranking while modifying it
    rank = highestRank - 1
    condition = True
    while condition:
        rank = processRank(rank, collation, graph, ranking, witnessCount)
        rank -= 1
        condition = rank > 1
    # check which output format is requested: graph or table

    # # Verify that nodes have been moved
    # print("\nLabels at each rank at end of processing: ")
    # for rank in ranking.byRank:
    #     print("\nRank: " + str(rank))
    #     print([node.label for node in ranking.byRank[rank]])
    # if output == "svg":
    #     return display_variant_graph_as_SVG(graph,svg_output)
    # else:
    #     raise Exception("Unknown output type for near-match collation: "+output)

def processRank(rank, collation, graph, ranking, witnessCount):
    nodesAtRank = ranking.byRank[rank]
    witnessesAtRank = []
    for thisNode in nodesAtRank:
        for key in thisNode.tokens:
            witnessesAtRank.append(str(key))
    witnessesAtRankCount = sum([len(thisNode.tokens) for thisNode in nodesAtRank])
    if witnessesAtRankCount == witnessCount:
        pass
    else:
        print("\nBefore adjustment, rank " + str(rank) + ' has ' + str(witnessesAtRankCount) + ' witnesses (out of ' + str(witnessCount) + ') on ' + str(len(nodesAtRank)) + ' nodes' )
        missingWitnesses = set([witness.sigil for witness in collation.witnesses]) - set(witnessesAtRank)
        print('Missing witnesses: ' + ' '.join(missingWitnesses))
        witnessesWeveSeen = set()
        for missingWitness in missingWitnesses:
            if missingWitness not in witnessesWeveSeen:
                print('Looking for ' + missingWitness)
                currentLabels = [node.label for node in ranking.byRank[rank]]
                # print('Labels at current location ' + str(rank) + ': ' + str(currentLabels))
                (priorRank, priorNode) = findPriorNode(missingWitness,rank,ranking)
                if not(priorRank == None):
                    priorLabel = priorNode.label
                    priorNodeWitnesses = priorNode.tokens.keys()
                    witnessesWeveSeen = witnessesWeveSeen.union(priorNodeWitnesses)
                    # print("We've seen witnesses: " + str(witnessesWeveSeen))
                    # print('Prior label is ' + priorLabel + ' at ' + str(priorRank))
                    priorLabels = [node.label for node in ranking.byRank[priorRank]]
                    # print('Labels at prior location ' + str(priorRank) + ': ' + str(priorLabels))
                    priorDistances = [distance(priorLabel,label) for label in priorLabels]
                    # print('Prior distances = ' + str(priorDistances))
                    currentDistances = [distance(priorLabel,label) for label in currentLabels]
                    # print('Current distances = ' + str(currentDistances))

                    leftTable = {}
                    for currentNode in ranking.byRank[priorRank]:
                        if currentNode.label != priorLabel:
                            leftTable[currentNode.label] = [distance(currentNode.label,priorLabel),len(currentNode.tokens)]
                    # print('Left table:')
                    # print(leftTable)
                    leftMin = min((value[0] for value in leftTable.values()))
                    leftMaxCount = max((value[1] for value in leftTable.values()))
                    # print('Minimum distance on left is ' + str(leftMin) + ' and max witness count on left is ' + str(leftMaxCount))

                    rightTable = {}
                    for currentNode in ranking.byRank[rank]:
                        rightTable[currentNode.label] = [distance(currentNode.label,priorLabel),len(currentNode.tokens)]
                    rightMin = min((value[0] for value in rightTable.values()))
                    rightMaxCount = max((value[1] for value in rightTable.values()))

                    if (rightMin,-rightMaxCount) < (leftMin,-leftMaxCount):
                        #move the entire node from priorRank to (current) rank
                        # print('Before: ranking.byRank[priorRank] = ' + str(ranking.byRank[priorRank]))
                        # print('Before: ranking.byRank[rank] = ' + str(ranking.byRank[rank]))
                        # print('Before: ranking.byVertex[priorNode] = ' + str(ranking.byVertex[priorNode]))
                        ranking.byRank[priorRank].remove(priorNode)
                        ranking.byRank[rank].append(priorNode)
                        ranking.byVertex[priorNode] = rank
                        # print('After: ranking.byRank[priorRank] = ' + str(ranking.byRank[priorRank]))
                        # print('After: ranking.byRank[rank] = ' + str(ranking.byRank[rank]))
                        # print('After: ranking.byVertex[priorNode] = ' + str(ranking.byVertex[priorNode]))
    return rank

def findPriorNode(witness,currentRank,ranking):
    for rank in range(currentRank - 1, 1, -1):
        nodesAtRank = ranking.byRank[rank]
        for thisNode in nodesAtRank:
            for key in thisNode.tokens:
                if witness == key: # Worst case: will be found at start if not on a real node
                    return (rank,thisNode)
    # The start node has no witnesses, so return a special value to indicate nothing found
    return (None, None)

def collate_pretokenized_json(json, output='table', layout='horizontal', **kwargs):
    # Takes the same arguments as collate() above
    if output not in ['json', 'table', 'html2', 'html', 'svg']:
        raise UnsupportedError("Output type " + output + " not supported for pretokenized collation")

    collation = Collation()
    for witness in json["witnesses"]:
        collation.add_witness(witness)
    return collate(collation,output=output,layout=layout,**kwargs)

def collate_pretokenized_json_nearMatch(json, output='table', layout='horizontal', **kwargs):
    # Takes the same arguments as collate() above
    if output not in ['json', 'table', 'html2', 'html', 'svg']:
        raise UnsupportedError("Output type " + output + " not supported for pretokenized collation")

    collation = Collation()
    for witness in json["witnesses"]:
        collation.add_witness(witness)
    return collate_nearMatch(collation,output=output,layout=layout,**kwargs)

def export_alignment_table_as_json(table, indent=None, status=False):
    json_output = {}
    json_output["table"]=[]
    sigli = []
    for row in table.rows:
        sigli.append(row.header)
        json_output["table"].append([[listItem.token_data for listItem in cell] if cell else None for cell in row.cells])
    json_output["witnesses"] = sigli
    if status:
        variant_status = []
        for column in table.columns:
            variant_status.append(column.variant)
        json_output["status"] = variant_status
    return json.dumps(json_output, sort_keys=True, indent=indent,ensure_ascii=False)

class Collation(object):

    @classmethod
    def create_from_dict(cls, data, limit=None):
        witnesses = data["witnesses"]
        collation = Collation()
        for witness in witnesses[:limit]:
            # generate collation object from json_data
            collation.add_witness(witness)
        return collation

    @classmethod
    # json_data can be a string or a file
    def create_from_json(cls, json_data):
        data = json.load(json_data)
        collation = cls.create_from_dict(data)
        return collation

    def __init__(self):
        self.witnesses = []

    def add_witness(self, witnessdata):
        witness = Witness(witnessdata)
        self.witnesses.append(witness)

    def add_plain_witness(self, sigil, content):
        return self.add_witness({'id':sigil, 'content':content})



