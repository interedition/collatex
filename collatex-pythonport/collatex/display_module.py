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
        # # create new Digraph
        # dot = Digraph(format="svg", graph_attr={'rankdir': 'LR'})
        # # add nodes
        # counter = 0
        # mapping = {}
        # for n in graph.graph.nodes():
        #     counter += 1
        #     mapping[n] = str(counter)
        #     # dot.node(str(n), nodedata["label"])
        #     readings = ["<TR><TD ALIGN='LEFT'><B>" + n.label + "</B></TD><TD ALIGN='LEFT'><B>Sigla</B></TD></TR>"]
        #     reverseDict = defaultdict(list)
        #     for key,value in n.tokens.items():
        #         reverseDict["".join(item.token_data["t"] for item in value)].append(key)
        #     for key,value in sorted(reverseDict.items()):
        #         reading = ("<TR><TD ALIGN='LEFT'><FONT FACE='Bukyvede'>{}</FONT></TD><TD ALIGN='LEFT'>{}</TD></TR>").format(key,', '.join(value))
        #         readings.append(reading)
        #     dot.node(mapping[n], '<<TABLE CELLSPACING="0">' + "".join(readings) + '</TABLE>>',{'shape': 'box'})
        # # add edges
        # for u,v,edgedata in graph.graph.edges_iter(data=True):
        #     dot.edge(str(mapping[u]), str(mapping[v]), edgedata["label"])
        # # render the dot graph to SVG
        # # Note: this creates a file
        # if svg_output:
        #     svg = dot.render(svg_output,'svg_output')
        # else:
        #     svg = dot.render()

        # a = nx.drawing.nx_agraph.to_agraph(graph.graph)
        # a.graph_attr['rankdir'] = 'LR'
        # # Create labels for nodes
        # for n in graph.graph.nodes():
        #     for key, value in n.tokens.items():
        #         print(key, value)
        #         print([item.token_data['t'] for item in value])

        a = pygraphviz.AGraph(directed=True, rankdir='LR')
        counter = 0
        mapping = {}
        ranking = VariantGraphRanking.of(graph)
        for n in graph.graph.nodes():
            counter += 1
            mapping[n] = counter
            rank = ranking.byVertex[n]
            readings = ["<TR><TD ALIGN='LEFT'><B>" + n.label + "</B></TD><TD ALIGN='LEFT'>exact: " + str(rank) + "</TD></TR>"]
            reverseDict = defaultdict(list)
            for key,value in n.tokens.items():
                reverseDict["".join(re.sub(r'>',r'&gt;',re.sub(r'<',r'&lt;',item.token_data["t"]))  for item in value)].append(key)
            for key,value in sorted(reverseDict.items()):
                reading = ("<TR><TD ALIGN='LEFT'><FONT FACE='Bukyvede'>{}</FONT></TD><TD ALIGN='LEFT'>{}</TD></TR>").format(key,', '.join(value))
                readings.append(reading)
            a.add_node(mapping[n], label='<<TABLE CELLSPACING="0">' + "".join(readings) + '</TABLE>>')
        # add edges
        for u,v,edgedata in graph.graph.edges_iter(data=True):
            a.add_edge(str(mapping[u]), str(mapping[v]), edgedata["label"])
        for key, value in ranking.byRank.items():
            a.add_subgraph([mapping[item] for item in value], rank='same')
        svg = a.draw(prog='dot', format='svg')
        # display using the IPython SVG module
        return display(SVG(svg))
