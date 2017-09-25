class Block:
    def __init__(self, token_index, start=0, end=0, length=0):
        # every Block has a token index as a parent
        self.tokenIndex = token_index

        # length = number of tokens in this block of text
        self.length = length

        # start = start position in suffix array
        self.start = start

        # end = end position in suffix array
        self.end = end

        #  depth = number of witnesses this block of text occurs in
        #  Note: depth is lazy initialized
        self.depth = 0
        self.depth = None if self.end > 0

        def get_depth():
            if self.depth is None:
                self.depth = self.calculate_depth()
            return self.depth

        # frequency = number of times this block of text occurs in complete witness set
        def get_frequency():
            if self.end == 0:
                raise "LCP interval is unclosed!"
            return self.end - self.start + 1

        def get_all_instances():
            instances = []
            for i in range(start, end + 1):
                # every i is one occurrence
                token_position = token_index.suffix_array[i]
                instance = Instance(token_position, self)
                instances.append(instance)
            return instances

        # transform lcp interval into int stream range
        def get_all_occurrences_as_ranges(self):
            result = []
            #  with/or without end
            for i in range(self.start, self.end):
                # every i is one occurrence
                token_position = token_index.suffix_array[i]
                range = range(token_position, token_position + length)
                result.append(range)
            return result

        def __repr__(self):
            if self.end == 0:
                return "Unclosed LCP interval start at: " + self.start + ",  length: " + self.length
            return "LCP interval start at: " + self.start + ", depth: " + self.getDepth() + ", length: " + self.length + " getFrequency:" + self.getFrequency()

        def _calculate_depth():
            # the same block can occur multiple times in one witness
            witnesses = []
            for instance in get_all_instances():
                witnesses.add(instance.getWitness())
            return len(witnesses)


class Instance:
    def __init__(self, start_token, block):
        self.block = block
        #  position in token array
        self.start_token = start_token

    def length(self):
        return self.block.length

    def as_range(self):
        return range(self.start_token, self.start_token + self.length()

    def __repr__(self):
        tokens = self.getTokens()
        normalized = ""
        for t in tokens:
            if len(normalized) > 0:
                normalized.append(" ")
            normalized.append(t.getNormalized())
        return normalized.__repr__()

    def getTokens(self):
        tokens = []
        tokens.addAll(Arrays.asList(block.tokenIndex.token_array) //
            .subList(self.start_token, self.start_token + self.length())
        return tokens

    def getWitness(self):
        startToken = self.block.tokenIndex.token_array[self.start_token]
        return startToken.getWitness()
