'''
Created on Nov 13, 2014

@author: ronald
'''
from collections import defaultdict
from textwrap import fill
from collatex.HTML import Table, TableRow, TableCell
from collatex.core_classes import create_table_visualization, VariantGraphRanking
import re

# optionally load the IPython dependencies
try:
    from IPython.display import HTML
    from IPython.display import SVG
    from IPython.core.display import display
    # import graphviz python bindings
    import pygraphviz
except:
    pass


def visualize_table_vertically_with_colors(table, collation):
    # print the table vertically
    # switch columns and rows
    rows = []
    for column in table.columns:
        cells = []
        for witness in collation.witnesses:
            cell = column.tokens_per_witness.get(witness.sigil)
            cells.append(TableCell(text=fill("".join(item.token_data["t"] for item in cell) if cell else "-", 20), bgcolor="FF0000" if column.variant else "00FFFF"))
        rows.append(TableRow(cells=cells))
    sigli = []
    for witness in collation.witnesses:
        sigli.append(witness.sigil)
    x = Table(header_row=sigli, rows=rows)
    return display(HTML(str(x)))


# create visualization of alignment table
def display_alignment_table_as_html(at):
    pretty_table = create_table_visualization(at)
    html = pretty_table.get_html_string(formatting=True)
    return display(HTML(html))


# visualize the variant graph into SVG format
# from networkx.drawing.nx_agraph import to_agraph
def display_variant_graph_as_svg(graph, output):
    a = pygraphviz.AGraph(directed=True, rankdir='LR')
    counter = 0
    mapping = {}
    ranking = VariantGraphRanking.of(graph)
    # add nodes
    for n in graph.graph.nodes():
        counter += 1
        mapping[n] = counter
        if output == "svg_simple":
            label = n.label
            if label == '':
                label = '#'
            a.add_node(mapping[n], label=label)
        else:
            rank = ranking.byVertex[n]
            readings = ["<TR><TD ALIGN='LEFT'><B>" + n.label + "</B></TD><TD ALIGN='LEFT'>exact: " + str(
                rank) + "</TD></TR>"]
            reverse_dict = defaultdict(list)
            for key, value in n.tokens.items():
                reverse_dict["".join(
                    re.sub(r'>', r'&gt;', re.sub(r'<', r'&lt;', item.token_data["t"])) for item in value)].append(
                    key)
            for key, value in sorted(reverse_dict.items()):
                reading = (
                    "<TR><TD ALIGN='LEFT'><FONT FACE='Bukyvede'>{}</FONT></TD><TD ALIGN='LEFT'>{}</TD></TR>").format(
                    key, ', '.join(value))
                readings.append(reading)
            a.add_node(mapping[n], label='<<TABLE CELLSPACING="0">' + "".join(readings) + '</TABLE>>')

    # add edges
    for u,v,edge_data in graph.graph.edges_iter(data=True):
        a.add_edge(mapping[u], mapping[v], edge_data["label"])
    for key, value in ranking.byRank.items():
        a.add_subgraph([mapping[item] for item in value], rank='same')
    svg = a.draw(prog='dot', format='svg')
    # diagnostic, not for production
    # dot = a.draw(prog='dot')
    # print(dot.decode(encoding='utf-8'))
    # display using the IPython SVG module
    return display(SVG(svg))
