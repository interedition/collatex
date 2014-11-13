'''
Created on Nov 13, 2014

@author: ronald
'''

# optionally load the IPython dependencies
# try:
#     from IPython.display import HTML
#     from IPython.display import SVG
#     from IPython.core.display import display
# except:
#     pass

# def in_ipython():
#     try:
#         get_ipython().config  # @UndefinedVariable
# #         print('Called by IPython.')
#         return True
#     except:
#         return False

# TODO: move HTML display to  display function!
#     if in_ipython():
#         html = prettytable.get_html_string(formatting=True)
#         return display(HTML(html))




# display alignment table 
#     if in_ipython():
#         return display(JSON(json))
#     if in_ipython():
#         print(json)
#         return

# create visualization of alignment table
#         if layout == "vertical":    
#             prettytable = visualizeTableVertically(tokenized_at)
#         else:
#             prettytable = visualizeTableHorizontal(tokenized_at)
#         if in_ipython():
#             html = prettytable.get_html_string(formatting=True)
#             return display(HTML(html))
#         return prettytable

# DISPLAY PART OF THE VARIANT_GRAPH IN SVG!
#     and in_ipython:
#         # visualize the variant graph into SVG format
#         from networkx.drawing.nx_agraph import to_agraph
#         agraph = to_agraph(graph.graph)
#         svg = agraph.draw(format="svg", prog="dot", args="-Grankdir=LR -Gid=VariantGraph")
#         return display(SVG(svg)) 
