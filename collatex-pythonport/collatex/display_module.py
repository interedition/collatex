'''
Created on Nov 13, 2014

@author: ronald
'''
from textwrap import fill
from collatex.HTML import Table, TableRow, TableCell
from collatex.core_classes import create_table_visualization

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
            cells.append(TableCell(text=fill(cell, 20) if cell else "-", bgcolor="FF0000" if column.variant else "00FF00"))
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
def display_variant_graph_as_SVG(graph):
        # create new Digraph
        dot = Digraph(format="svg", graph_attr={'rankdir': 'LR'})
        # add nodes
        for n,nodedata in graph.graph.nodes(data=True):
            dot.node(str(n), nodedata["label"])
        # add edges
        for u,v,edgedata in graph.graph.edges_iter(data=True):
            dot.edge(str(u), str(v), edgedata["label"])
        # render the dot graph to SVG
        # Note: this creates a file
        svg = dot.render()
        # display using the IPython SVG module
        return display(SVG(svg))


# def in_ipython():
#     try:
#         get_ipython().config  # @UndefinedVariable
# #         print('Called by IPython.')
#         return True
#     except:
#         return False





# display alignment table 
#     if in_ipython():
#         return display(JSON(json))
#     if in_ipython():
#         print(json)
#         return


# DISPLAY PART OF THE VARIANT_GRAPH IN SVG!
#     and in_ipython:
#         # visualize the variant graph into SVG format
#         from networkx.drawing.nx_agraph import to_agraph
#         agraph = to_agraph(graph.graph)
#         svg = agraph.draw(format="svg", prog="dot", args="-Grankdir=LR -Gid=VariantGraph")
#         return display(SVG(svg)) 
