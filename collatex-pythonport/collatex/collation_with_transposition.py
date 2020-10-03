"""
Created on: 3 October 2020
@author: Ronald Haentjens Dekker

"""
from collatex.tokenindex import TokenIndex


def collate_with_transposition(collation):
    token_index = TokenIndex(collation.witnesses)
    token_index.prepare()
    # print(token_index)
    return token_index
    pass
