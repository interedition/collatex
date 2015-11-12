from ClusterShell.RangeSet import RangeSet
from collatex.core_classes import Token
from collatex.extended_suffix_array import LCPInterval
from collatex.linsuffarr import SuffixArray
from collatex.linsuffarr import UNIT_BYTE




# TokenIndex
class TokenIndex(object):


    # public TokenIndex(Comparator<Token> comparator, List<? extends Iterable<Token>> w) {
    #     this.w = w;
    #     this.comparator = new MarkerTokenComparatorWrapper(comparator);
    # }
    def __init__(self, witnesses):
        self.witnesses = witnesses
        self.counter = 0
        self.witness_ranges = {}
        # this is a poor man's version of a token array
        self.combined_string = ""
        self.token_array = []

    # // 1. prepare token array
    # // 2. derive the suffix array
    # // 3. derive LCP array
    # // 4. derive LCP intervals
    # // TODO: we do not have to store w!
    # public void prepare() {
    #     this.token_array = this.prepareTokenArray();
    #     SuffixData suffixData = SuffixArrays.createWithLCP(token_array, new SAIS(), comparator);
    #     this.suffix_array = suffixData.getSuffixArray();
    #     this.LCP_array = suffixData.getLCP();
    #     this.blocks = splitLCP_ArrayIntoIntervals();
    #     constructWitnessToBlockInstancesMap();
    # }




    def prepare(self):
        self._prepare_token_array()
        # call third party library here
        self.suffix_array = self.get_suffix_array()
        self.lcp_array = self.get_lcp_array()

    def _prepare_token_array(self):
        #TODO: the lazy init should move to somewhere else
        # clear the suffix array and LCP array cache
        self.cached_suffix_array = None
        for witness in self.witnesses:
            witness_range = RangeSet()
            witness_range.add_range(self.counter, self.counter+len(witness.tokens()))
            # the extra one is for the marker token
            self.counter += len(witness.tokens()) + 1
            self.witness_ranges[witness.sigil] = witness_range
            if self.token_array:
                # add marker token
                self.token_array.append(Token({"n":"$"+str(len(self.witnesses)-1)}))
            # remember get tokens twice
            self.token_array.extend(witness.tokens())

    def get_sa(self):
        # NOTE: implemented in a lazy manner, since calculation of the Suffix Array and LCP Array takes time
        if not self.cached_suffix_array:
            string_array = [token.token_string for token in self.token_array]
            # Unit byte is done to skip tokenization in third party library
            self.cached_suffix_array = SuffixArray(string_array, unit=UNIT_BYTE)
        return self.cached_suffix_array

    def get_suffix_array(self):
        sa = self.get_sa()
        return sa.SA

    def get_lcp_array(self):
        sa = self.get_sa()
        return sa._LCP_values

    # NOTE: LCP intervals can 1) ascend or 2) descend or 3) first ascend and then descend. 4) descend, ascend
    def split_lcp_array_into_intervals(self):
        closed_intervals = []
        previous_lcp_value = 0
        open_intervals = Stack()
        for idx in range(0, len(self.LCP)):
            lcp_value = self.LCP[idx]
#             print(lcp_value)
            if lcp_value > previous_lcp_value:
                open_intervals.push((idx-1, lcp_value))
                previous_lcp_value = lcp_value
            if lcp_value < previous_lcp_value:
                # close open intervals that are larger than current lcp_value
                while open_intervals and open_intervals.peek()[1] > lcp_value:
#                     print("Peek: "+str(open_intervals.peek()))
                    (start, length) = open_intervals.pop()
                    #TODO: FIX NUMBER OF SIBLINGS!
                    closed_intervals.append(LCPInterval(self.tokens, self.SA, self.LCP, start, idx-1, length, 0, self.collation))
#                     print("new: "+repr(closed_intervals[-1]))
                # then: open a new interval starting with start filter open intervals.
                if lcp_value > 0:
                    start = closed_intervals[-1].start
                    open_intervals.push((start, lcp_value))
                previous_lcp_value = lcp_value
        # add all the open intervals to the result
#         print("Closing remaining:")
        for start, length in open_intervals:
            #TODO: FIX NUMBER OF SIBLINGS!
            closed_intervals.append(LCPInterval(self.tokens, self.SA, self.LCP, start, len(self.LCP)-1, length, 0, self.collation))
#             print("new: "+repr(closed_intervals[-1]))
        return closed_intervals

