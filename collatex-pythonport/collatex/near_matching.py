"""Functions and objects for near matching
    Called by: collate() (in core_functions.py) with near_match=True, segmentation=False
"""
from Levenshtein import distance


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
        # print('no variation in witnesses at rank ' + str(rank))
        pass
    else:
        # print('variation found at rank ' + str(rank))
        missing_witnesses = set([witness.sigil for witness in collation.witnesses]) - set(witnesses_at_rank)
        # print('missing witnesses: ' + str(missing_witnesses))
        witnesses_weve_seen = set()
        for missingWitness in sorted(missing_witnesses): # alphabetize witnesses for testing consistency
            if missingWitness not in witnesses_weve_seen:
                (prior_rank, prior_node) = find_prior_node(missingWitness, rank, ranking)
                # print('prior node: ' + str(prior_node) + ' with rank ' + str(prior_rank))
                if prior_rank:
                    candidate_ranks = {} # keys are ranks, values are distances
                    for candidate_rank in range(prior_rank, rank + 1):
                        candidate_ranks[candidate_rank] = scheduler.create_and_execute_task("build column for rank", create_near_match_table, prior_node, candidate_rank, ranking)
                    new_rank = min(candidate_ranks, key=candidate_ranks.get)  # returns key (rank number) of min (closest) prior node
                    need_to_move = prior_rank != new_rank
                    if need_to_move:
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
    for rank in range(current_rank - 1, 0, -1):
        nodes_at_rank = ranking.byRank[rank]
        for this_node in nodes_at_rank:
            for key in this_node.tokens:
                # print('looking for key ' + key + ' in prior node')
                if witness == key:  # Worst case: will be found at start if not on a real node
                    # print('find_prior_node returns: ' + str(this_node))
                    return rank, this_node
    # The start node has no witnesses, so return a special value to indicate nothing found
    return None, None


class NearMatchTable(object):
    def __init__(self, ranking, rank, prior_node):
        self.table = {}
        self._construct_table(ranking, rank, prior_node)

    def _construct_table(self, ranking, rank, prior_node):
        for current_node in ranking.byRank[rank]:
            if current_node != prior_node:
                self.table[current_node.label] = distance(current_node.label, prior_node.label), len(
                    current_node.tokens)

    def __str__(self):
        return str(self.table.items())

    @property
    def return_values(self):
        min_distance = min((value[0] for value in self.table.values())) if self.table else 0
        # TODO: Replace the arbitrarily high value with witness count + 1
        max_witness_count = max((value[1] for value in self.table.values())) if self.table else 100
        # If distances are the same, break the tie according to number of witnesses with that reading
        return min_distance, -max_witness_count
