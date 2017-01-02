'''
Created on Nov 13, 2014

@author: ronald
'''
from collections import defaultdict
from textwrap import fill
from collatex.HTML import Table, TableRow, TableCell
from collatex.core_classes import create_table_visualization, VariantGraphRanking
import pygraphviz
import re
import networkx as nx

# optionally load the IPython dependencies
try:
    from IPython.display import HTML
    from IPython.display import SVG
    from IPython.core.display import display
    # import graphviz python library
    from graphviz import Digraph
except:
    pass


def visualizeTableVerticallyWithColors(table, collation):
    # print the table vertically
    # switch columns and rows
    rows = []
    for column in table.columns:
        cells = []
        for witness in collation.witnesses:
            cell = column.tokens_per_witness.get(witness.sigil)
            cells.append(TableCell(text=fill("".join(item.token_data["t"] for item in cell) if cell else "-", 20), bgcolor="FF0000" if column.variant else "00FF00"))
        rows.append(TableRow(cells=cells))
    sigli = []
    for witness in collation.witnesses:
        sigli.append(witness.sigil)
    x = Table(header_row=sigli, rows=rows)
    return display(HTML(str(x)))


# create visualization of alignment table
def display_alignment_table_as_HTML(at):
    prettytable = create_table_visualization(at)
    html = prettytable.get_html_string(formatting=True)
    return display(HTML(html))

# visualize the variant graph into SVG format
# from networkx.drawing.nx_agraph import to_agraph
def display_variant_graph_as_SVG(graph,svg_output):
    a = pygraphviz.AGraph(directed=True, rankdir='LR')
    counter = 0
    mapping = {}
    ranking = VariantGraphRanking.of(graph)
    for n in graph.graph.nodes():
        counter += 1
        mapping[n] = counter
        rank = ranking.byVertex[n]
        readings = ["<TR><TD ALIGN='LEFT'><B>" + n.label + "</B></TD><TD ALIGN='LEFT'>rank: " + str(rank) + "</TD></TR>"]
        reverseDict = defaultdict(list)
        for key,value in n.tokens.items():
            reverseDict["".join(re.sub(r'>',r'&gt;',re.sub(r'<',r'&lt;',item.token_data["t"]))  for item in value)].append(key)
        for key,value in sorted(reverseDict.items()):
            reading = ("<TR><TD ALIGN='LEFT'><FONT FACE='Bukyvede'>{}</FONT></TD><TD ALIGN='LEFT'>{}</TD></TR>").format(key,', '.join(value))
            readings.append(reading)
        a.add_node(mapping[n], label='<<TABLE CELLSPACING="0">' + "".join(readings) + '</TABLE>>')
    # add regular (token sequence) edges
    for u,v,edgedata in graph.graph.edges_iter(data=True):
        # print('regular edges ', u, v, edgedata)
        label = edgedata['label']
        a.add_edge(mapping[u], mapping[v], label=label)
    # add near-match edges
    # TODO: Show all near edges (currently), or just the top one?
    for u,v,edgedata in graph.near_graph.edges_iter(data=True):
        # print('near-match edges ', u, v, edgedata)
        label = str('{:3.2f}'.format(edgedata['weight']))
        a.add_edge(mapping[u], mapping[v], style='dashed', label=label)
    # Add rank='same' information
    for key, value in ranking.byRank.items():
        # print(key, value)
        # print(key, value, len(value))
        # print(key, set(value), len(set(value)))
        a.add_subgraph([mapping[item] for item in value], rank='same')
    # diagnostic, not for production
    # dot = a.draw(prog='dot')
    # print(dot.decode(encoding='utf-8'))
    # # display using the IPython SVG module
    svg = a.draw(prog='dot', format='svg')
    return display(SVG(svg))
