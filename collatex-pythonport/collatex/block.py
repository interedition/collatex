class Block:
    def __init__(self, token_index, start=0, end=0, length=0):
        # every Block has a token index as a parent
        self.token_index = token_index

        # length = number of tokens in this block of text
        self.length = length

        # start = start position in suffix array
        self.start = start

        # end = end position in suffix array
        self.end = end

        #  depth = number of witnesses this block of text occurs in
        #  Note: depth is lazy initialized
        self._depth = None if self.end > 0 else 0

    def __repr__(self):
        if self.end == 0:
            return str.format("Unclosed LCP interval start at: {},  length: {}", self.start, self.length)
        return str.format("LCP interval (start,end): ({},{}), length: {}, depth: {}, getFrequency: {}",
                          self.start, self.end, self.length, self.get_depth(), self.get_frequency())

    def __lt__(self, other):
        return (self.start < other.start) and (self.end < other.end)

    def get_depth(self):
        if self._depth is None:
            self._depth = self._calculate_depth()
        return self._depth

    # frequency = number of times this block of text occurs in complete witness set
    def get_frequency(self):
        if self.end == 0:
            raise Exception("LCP interval is unclosed!")
        return self.end - self.start + 1

    def get_all_instances(self):
        instances = []
        for i in range(self.start, self.end + 1):
            # every i is one occurrence
            token_position = self.token_index.suffix_array[i]
            instance = Instance(token_position, self)
            instances.append(instance)
        return instances

    # transform lcp interval into int stream range
    def get_all_occurrences_as_ranges(self):
        result = []
        #  with/or without end
        for i in range(self.start, self.end):
            # every i is one occurrence
            token_position = self.token_index.suffix_array[i]
            token_range = range(token_position, token_position + self.length)
            result.append(token_range)
        return result

    def _calculate_depth(self):
        # the same block can occur multiple times in one witness
        witness_sigils = set()
        for instance in self.get_all_instances():
            witness_sigils.add(instance.get_witness_sigil())
        return len(witness_sigils)

    def _as_range(self):
        return range(self.start, self.start + self.length)


class Instance:
    def __init__(self, start_token, block):
        self.block = block
        #  position in token array
        self.start_token = start_token

    def __repr__(self):
        tokens = self.get_tokens()
        normalized = ""
        for t in tokens:
            if len(normalized) > 0:
                normalized += " "
            normalized += t.__repr__()
        return normalized.__repr__()

    def length(self):
        return self.block.length

    def _as_range(self):
        return range(self.start_token, self.start_token + self.length())

    def get_tokens(self):
        tokens = [self.block.token_index.token_array[self.start_token:self.start_token + self.length()]]
        return tokens

    def get_witness_sigil(self):
        if self.block.token_index.token_array:
            start_token = self.block.token_index.token_array[self.start_token]
            return start_token.token_data['_sigil']
        return None
