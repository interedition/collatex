# author: by Ronald Haentjens Dekker (created 4 August 2014)

class AStar(object):
        
    def heuristic(self, node):
        raise NotImplementedError
      
    def create_childnodes(self, node):
        raise NotImplementedError
      
    def search(self, start, control_table):
        # TODO: should become priority queue to store nodes
        heap = [] 
        
        # Set to store previously visited nodes
        visited = set()

        # put the initial node on the queue
        heap.append(start)
 
        while heap:
            heap.sort(key=lambda x:x.g+x.h)
#             print(heap)
            node = heap.pop(0)
#             print("selected (lowest): "+str(node.y)+" "+str(node.x))
            if node.is_end_node():
                return self.reconstruct_path(node)
            visited.add(node)
            children = self.create_childnodes(node)
            for child in children:
                if child in visited:
                    continue
                old_g = child.g
                tentative_g_score = node.g + node.move_cost(child)
                child.g = old_g
                if not child in heap or tentative_g_score < child.g:
                    child.parent = node
                    child.g = tentative_g_score
                    child.h = self.heuristic(child)
                    if not child in heap:
                        heap.append(child)
#                     else:
#                         print("Child is already in heap, will be resorted!")
        # entire tree searched, no goal state found
        return None

    def reconstruct_path(self, current):
        path = []
        while current.parent:
            path.append(current)
            current = current.parent
        return path[::-1]

 
class AStarNode(object):
    def __init__(self):
        self.g = 0
        self.h = 0
        self.parent = None
    
    def move_cost(self, other):
        raise NotImplementedError
    
    def is_end_node(self):
        raise NotImplementedError
    