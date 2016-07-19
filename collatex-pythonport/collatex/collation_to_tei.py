from collatex import *
from lxml import etree

collation = Collation()
collation.add_plain_witness("A", "The quick brown wombat jumps over the industrious brown dog.")
collation.add_plain_witness("B", "The brown koala jumps over the lazy yellow dog.")
collation.add_plain_witness("C", "The brown koala jumps over the industrious yellow dog.")
table = collate(collation)

# TODO: Starts and ends each <app> with a new line, which will break on an <app> within a word

p = etree.Element('p')
app = None
for column in table.columns:
    if not(column.variant):
        text_node = " ".join(item.token_data["t"] for item in next(iter(column.tokens_per_witness.values())))
        if not(len(p)):
            p.text = text_node + "\n"
        else:
            app.tail = "\n" + text_node + "\n"
    else:
        app = etree.Element('app')
        app.text = "\n"
        value_dict = {} # keys are readings, values are an unsorted lists of sigla
        for key, value in column.tokens_per_witness.items():
            group = value_dict.setdefault(" ".join([item.token_data["t"] for item in value]),[])
            group.append(key)
            rdg_dict = {} # keys are sorted lists of sigla, values are readings
        for key, value in value_dict.items():
            rdg_dict[" ".join("#" + item for item in sorted(value))] = key
        for key, value in sorted(rdg_dict.items()): # sort <rdg> elements by @wit values
            child = etree.Element('rdg')
            child.attrib['wit'] = key
            child.text = value
            app.append(child)
            child.tail = "\n"
            # Without the encoding specification, outputs bytes instead of a string
        p.append(app)
result = etree.tostring(p, encoding="unicode")
print(result)
