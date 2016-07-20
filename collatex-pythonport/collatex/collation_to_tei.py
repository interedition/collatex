from collatex import *
from lxml import etree

collation = Collation()
collation.add_plain_witness("A", "The quick brown wombat jumps over the industrious brown dog.")
collation.add_plain_witness("B", "The brown koala jumps over the lazy yellow dog.")
collation.add_plain_witness("C", "The brown koala jumps over the industrious yellow dog.")
table = collate(collation)

# TODO: Heavy-handed pretty printing starts and ends each <app> with a new line,
# TODO:   which will break on an <app> that is supposed to be adjacent to or within a word
# TODO: Root <p> element created although the text is not guaranteed to be a paragraph and
# TODO:   may contain other paragraphs

p = etree.Element('p')
app = None
for column in table.columns:
    if not column.variant:
        text_node = " ".join(item.token_data["t"] for item in next(iter(column.tokens_per_witness.values())))
        if not (len(p)):
            p.text = text_node + "\n"
        else:
            app.tail = "\n" + text_node + "\n"
    else:
        app = etree.Element('app')
        preceding = None  # If preceding is None, we're processing the first <rdg> child
        app.text = "\n  "  # Indent first <rdg>
        value_dict = {}  # keys are readings, values are an unsorted lists of sigla
        for key, value in column.tokens_per_witness.items():
            group = value_dict.setdefault(" ".join([item.token_data["t"] for item in value]), [])
            group.append(key)
        rdg_dict = {}  # keys are sorted lists of sigla, with "#" prepended; values are readings
        for key, value in value_dict.items():
            rdg_dict[" ".join("#" + item for item in sorted(value))] = key
        for key, value in sorted(rdg_dict.items()):  # sort <rdg> elements by @wit values
            if preceding is not None:  # Change tail of preceding <rdg> to indent current one
                preceding.tail = "\n  "
            child = etree.Element('rdg')
            child.attrib['wit'] = key
            child.text = value
            app.append(child)
            child.tail = "\n"
            # If preceding is not None on an iteration, use its tail indent non-initial current <rdg>
            preceding = child
        p.append(app)
# Without the encoding specification, outputs bytes instead of a string
result = etree.tostring(p, encoding="unicode")
print(result)
