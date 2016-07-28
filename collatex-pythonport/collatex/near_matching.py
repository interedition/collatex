"""Functions and objects for near matching
    Called by: collate() (in core_functions.py) with near_match=True, segmentation=False
"""
from Levenshtein import distance

debug = 0 # set to 1 for debug output about near matching


class Scheduler(object):
    def __init__(self):
        self.tasks = []

    def create_and_execute_task(self, description, funct, *args):
        task = Task(description, funct, args)
        self.tasks.append(task)
        return task.execute()

    def __getitem__(self, item):
        return self.tasks[item]

    def __len__(self):
        return len(self.tasks)

    def debug_tasks(self):
        for task in self.tasks:
            print(task)


class Task(object):
    def __init__(self, name, func, args):
        self.name = name
        self.func = func
        self.args = args

    def execute(self):
        return self.func(*self.args)

    def __repr__(self):
        return "Task: "+self.name+", "+str(self.args)


def process_rank(scheduler, rank, collation, ranking, witness_count):
    nodes_at_rank = ranking.byRank[rank]
    witnesses_at_rank = []
    for this_node in nodes_at_rank:
        for key in this_node.tokens:
            witnesses_at_rank.append(str(key))
    witnesses_at_rank_count = sum([len(thisNode.tokens) for thisNode in nodes_at_rank])
    if witnesses_at_rank_count == witness_count:
        print('\nno variation in witnesses at rank ' + str(rank)) if debug else None
        pass
    else:
        print('\nvariation found at rank ' + str(rank)) if debug else None
        missing_witnesses = set([witness.sigil for witness in collation.witnesses]) - set(witnesses_at_rank)
        # print('missing witnesses: ' + str(missing_witnesses))
        witnesses_weve_seen = set()
        filtered_missing_witnesses = filter(lambda x: not(x in witnesses_weve_seen), sorted(missing_witnesses))
        prior_ranks_and_nodes = map(lambda x: find_prior_node(x, rank, ranking), filtered_missing_witnesses)
        for (prior_rank, prior_node) in filter(lambda x: x[0] is not None, prior_ranks_and_nodes): # alphabetize witnesses for testing consistency
            print('prior node is ' + str(prior_node) + ' at rank ' + str(prior_rank)) if debug else None
            print('current node has witnesses: ' + str(witnesses_at_rank)) if debug else None
            print('prior_node has witnesses: ' + str([key for key in prior_node.tokens.keys()])) if debug else None
            build_candidate_rank_dict = {candidate_rank : scheduler.create_and_execute_task("build column for rank", create_near_match_table, prior_node, candidate_rank, ranking) for candidate_rank in range(prior_rank, rank + 1)}
            new_rank = min(build_candidate_rank_dict, key=build_candidate_rank_dict.get) # returns key (rank number) of min (closest) prior node
            print('new rank = ' + str(new_rank)) if debug else None
            # Need to move only if there's a better match elsewhere
            need_to_move = prior_rank != new_rank
            print('need to move? ' + str(need_to_move)) if debug else None
            # If prior_node and witnesses_at_rank already share a witness, don't move
            can_move = not (set(witnesses_at_rank) & set(prior_node.tokens.keys()))
            print('node can be moved? ' + str(can_move)) if debug else None
            if need_to_move and can_move:
                print('moving node ' + str(prior_node) + ' from rank ' + str(prior_rank) + ' to rank ' + str(new_rank)) if debug else None
                scheduler.create_and_execute_task("move node from prior rank to rank with best match", move_node_from_prior_rank_to_rank, prior_node, prior_rank, new_rank, ranking)
    return rank


def create_near_match_table(prior_node, prior_rank, ranking):
    return NearMatchTable(ranking, prior_rank, prior_node).return_values


def move_node_from_prior_rank_to_rank(prior_node, prior_rank, rank, ranking):
    # if (right_min, -right_max_count) < (left_min, -left_max_count):
    # move the entire node from prior_rank to (current) rank
    ranking.byRank[prior_rank].remove(prior_node)
    ranking.byRank[rank].append(prior_node)
    ranking.byVertex[prior_node] = rank


def find_prior_node(witness, current_rank, ranking):
    # filter nodes according to whether they have an entry in their token dictionary with a key equal to witness
    # returns default of None on StopIteration
    next_matching_node = next((filter(lambda x: witness in x.tokens, get_nodes_in_reverse_rank_order(current_rank, ranking))), None)
    # return default of None for ranking.byVertex[next_matching_node] if there is no next matching node
    rank = ranking.byVertex.get(next_matching_node, None)
    print('returning witness ' + witness + ' at rank ' + str(rank)) if debug else None
    return rank, next_matching_node


def get_nodes_in_reverse_rank_order(current_rank, ranking):
    for rank in range(current_rank - 1, 0, -1):
        for this_node in ranking.byRank[rank]: # variant graph vertex
            yield this_node

class NearMatchTable(object):
    def __init__(self, ranking, rank, prior_node):
        self.table = self._construct_table(ranking, rank, prior_node)

    def _construct_table(self, ranking, rank, prior_node):
        table = {current_node.label: (distance(current_node.label, prior_node.label), len(current_node.tokens))\
            for current_node in ranking.byRank[rank] if current_node != prior_node}
        return table

    def __str__(self):
        return str(self.table.items())

    @property
    def return_values(self):
        min_distance = min((value[0] for value in self.table.values())) if self.table else 0
        # TODO: Replace the arbitrarily high value with witness count + 1
        max_witness_count = max((value[1] for value in self.table.values())) if self.table else 100
        # If distances are the same, break the tie according to number of witnesses with that reading
        return min_distance, -max_witness_count
