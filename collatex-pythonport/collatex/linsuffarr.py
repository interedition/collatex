#! /usr/bin/python
# -*- coding: utf-8 -*-
"""
Linsuffarr: Suffix arrays for natural language processing

In its simplest use as a command line, this Python module performs the linear
construction of the suffix array for the text given on the standard input
(Kärkkäinen and Sanders, Linear work suffix array construction,
Journal of the ACM, vol. 56, pp. 918-936, 2006)

In addition to the construction of suffix array, this module provides
facilities to attach user-defined features to suffixes:
see SuffixArray.addFeature and SuffixArray.addFeatureSA for more details.
This module provides a simple mechanism to load and save suffix arrays along
with the features already defined.


To get more information: 
 - about the module API, type
     $ pydoc linsuffarr
     
 - about the command line usage, type
     $ python linsuffarr.py --help

Ported to Python 3 by Ronald Haentjens Dekker
     
"""

from array    import array  as _array
from pickle  import HIGHEST_PROTOCOL as _HIGHEST_PROTOCOL
from pickle  import dumps  as _dumps
from pickle  import loads  as _loads
from gzip     import GzipFile
from inspect  import getargspec
from optparse import OptionParser
from os.path  import getsize
from sys      import argv   as _argv
from sys      import stderr as _stderr
from sys      import stdin  as _stdin
from sys      import stdout as _stdout
from time     import time   as _time


__version__  = "0.3"
__author__   = "Julien Gosme <Julien.Gosme@unicaen.fr>"


UNIT_BYTE      = 0
UNIT_CHARACTER = 1
UNIT_WORD      = 2

DEFAULT_UNIT_STR = "word"
DEFAULT_UNIT     = UNIT_WORD
DEFAULT_ENCODING = "utf-8"

COMPRESSION_LEVEL = 1

EXIT_BAD_OPTION  = 1
EXIT_ERROR_FILE  = 2


_trace=False


def _open(filename, mode="r"):
    """
    Universal open file facility.
    With normal files, this function behaves as the open builtin.
    With gzip-ed files, it decompress or compress according to the specified mode.
    In addition, when filename is '-', it opens the standard input or output according to
    the specified mode.
    Mode are expected to be either 'r' or 'w'.
    """
    if filename.endswith(".gz"):
        return GzipFile(filename, mode, COMPRESSION_LEVEL)
    elif filename == "-":
        if mode=="r":
            return _stdin
        elif mode=="w":
            return _stdout
    else:
        #TODO: set encoding to UTF-8?
        return open(filename, mode=mode)





def _radixPass(a, b, r, n, K):
    """
    Stable sort of the sequence a according to the keys given in r.
    
    >>> a=range(5)
    >>> b=[0]*5
    >>> r=[2,1,3,0,4]
    
    >>> _radixPass(a, b, r, 5, 5)
    >>> b
    [3, 1, 0, 2, 4]

    When n is less than the length of a, the end of b must be left unaltered.
    >>> b=[5]*5
    >>> _radixPass(a, b, r, 2, 2)
    >>> b
    [1, 0, 5, 5, 5]
    
    >>> _a=a=[1, 0]
    >>> b= [0]*2
    >>> r=[0, 1]
    >>> _radixPass(a, b, r, 2, 2)
    >>> a=_a 
    >>> b
    [0, 1]

    >>> a=[1, 1]
    >>> _radixPass(a, b, r, 2, 2)
    >>> b
    [1, 1]
    
    >>> a=[0, 1, 1, 0]
    >>> b= [0]*4
    >>> r=[0, 1]
    >>> _radixPass(a, b, r, 4, 2)
    >>> a=_a 
    >>> b
    [0, 0, 1, 1]
    """
    c = _array("i", [0]*(K+1))                # counter array
  
    for i in range(n):                      # count occurrences
        c[r[a[i]]]+=1

    sum=0

    for i in range(K+1):                    # exclusive prefix sums
        t = c[i]
        c[i] = sum
        sum += t
  
    for a_i in a[:n]:                        # sort
        b[c[r[a_i]]] = a_i
        c[r[a_i]]+=1





def _nbOperations(n):
    """
    Exact number of atomic operations in _radixPass.
    
    """
    if n<2:
        return 0
    else:
        n0=(n+2)//3
        n02=n0+n//3
        return 3*(n02)+n0+_nbOperations(n02)

def _traceSuffixArray(operations, totalOperations):
    if totalOperations==0:
        percentage=100.
    else:
        percentage=float((operations*100)/totalOperations)
    print >> _stderr, "Construction %.2f%% (%i/%i)\r"%(percentage,operations, totalOperations),
    _stderr.flush()

def _suffixArrayWithTrace(s, SA, n, K, operations, totalOperations):
    """
    This function is a rewrite in Python of the C implementation proposed in Kärkkäinen and Sanders paper.

    Find the suffix array SA of s[0..n-1] in {1..K}^n
    Require s[n]=s[n+1]=s[n+2]=0, n>=2
    """
    if _trace:
        _traceSuffixArray(operations, totalOperations)

    n0  = (n+2)//3
    n1  = (n+1)//3
    n2  = n//3
    n02 = n0+n2
    
    
    SA12 = _array("i", [0]*(n02+3))
    SA0  = _array("i", [0]*n0)
    s0   = _array("i", [0]*n0)
    
    # s12 : positions of mod 1 and mod 2 suffixes
    s12 = _array("i", [i for i in range(n+(n0-n1)) if i%3])# <- writing i%3 is more efficient than i%3!=0
    s12.extend([0]*3)
  
    # lsb radix sort the mod 1 and mod 2 triples
    _radixPass(s12, SA12, s[2:], n02, K)
    if _trace:
        operations+=n02
        _traceSuffixArray(operations, totalOperations)
    
    _radixPass(SA12, s12, s[1:], n02, K)
    if _trace:
        operations+=n02
        _traceSuffixArray(operations, totalOperations)
    
    _radixPass(s12, SA12, s, n02, K)
    if _trace:
        operations+=n02
        _traceSuffixArray(operations, totalOperations)
    
    # find lexicographic names of triples
    name = 0
    c= _array("i",[-1]*3)
    for i in range(n02) :
        cSA12=s[SA12[i]:SA12[i]+3]
        if cSA12!=c:
            name+=1
            c=cSA12

        if SA12[i] % 3 == 1 :
            s12[SA12[i]//3]        = name  # left half
        else :
            s12[(SA12[i]//3) + n0] = name  # right half

    if name < n02 : # recurse if names are not yet unique
        operations=_suffixArrayWithTrace(s12, SA12,n02,name+1,operations, totalOperations)
        if _trace:
            _traceSuffixArray(operations, totalOperations)
        
        # store unique names in s12 using the suffix array
        for i,SA12_i in enumerate(SA12[:n02]):
            s12[SA12_i] = i + 1
    else: #generate the suffix array of s12 directly
        if _trace:
            operations+=_nbOperations(n02)
            _traceSuffixArray(operations, totalOperations)
    
        for i,s12_i in enumerate(s12[:n02]):
            SA12[s12_i - 1] = i

    # stably sort the mod 0 suffixes from SA12 by their first character
    j=0
    for SA12_i in SA12[:n02]:
        if (SA12_i < n0):
            s0[j] = 3*SA12_i
            j+=1

    _radixPass(s0,SA0,s,n0,K)
    if _trace:
        operations+=n0
        _traceSuffixArray(operations, totalOperations)
    
    
    # merge sorted SA0 suffixes and sorted SA12 suffixes
    p = j = k = 0
    t = n0 - n1
    while k < n :
        if SA12[t] < n0 :# pos of current offset 12 suffix
            i = SA12[t] * 3 + 1
        else :
            i = (SA12[t] - n0 ) * 3 + 2

        j = SA0[p]#pos of current offset 0 suffix
 
        if SA12[t] < n0 :
            bool = (s[i], s12[SA12[t]+n0])           <= (s[j], s12[int(j/3)])
        else :
            bool = (s[i], s[i+1], s12[SA12[t]-n0+1]) <= ( s[j], s[j+1], s12[int(j/3)+n0])  

        if(bool) :
            SA[k] = i
            t += 1
            if t == n02 : # done --- only SA0 suffixes left
                k += 1
                while p < n0 :
                    SA[k] = SA0[p]
                    p += 1
                    k += 1
            
        else : 
            SA[k] = j
            p += 1
            if p == n0 :#done --- only SA12 suffixes left
                k += 1
                while t < n02 :
                    if SA12[t] < n0 :# pos of current offset 12 suffix
                        SA[k] = (SA12[t] * 3) + 1
                    else :
                        SA[k] = ((SA12[t] - n0) * 3) + 2
                    t += 1
                    k += 1
        k += 1
    return operations

def _suffixArray(s, SA, n, K):
    if(_trace):
        totalOperations=_nbOperations(n)
        operations=0
    else:
        totalOperations=0
        operations=0
         
    _suffixArrayWithTrace(s, SA, n, K, operations, totalOperations)
    if(_trace):
        print >> _stderr, ""


def _longestCommonPrefix(seq1, seq2, start1=0, start2=0):
    """
    Returns the length of the longest common prefix of seq1
    starting at offset start1 and seq2 starting at offset start2.
        
    >>> _longestCommonPrefix("abcdef", "abcghj")
    3
    
    >>> _longestCommonPrefix("abcghj", "abcdef")
    3
    
    >>> _longestCommonPrefix("miss", "")
    0

    >>> _longestCommonPrefix("", "mr")
    0
    
    >>> _longestCommonPrefix(range(128), range(128))
    128

    >>> _longestCommonPrefix("abcabcabc", "abcdefabcdef", 0, 6)
    3

    >>> _longestCommonPrefix("abcdefabcdef", "abcabcabc", 6, 0)
    3

    >>> _longestCommonPrefix("abc", "abcabc", 1, 4)
    2

    >>> _longestCommonPrefix("abcabc", "abc", 4, 1)
    2
    """
    
    
    len1=len(seq1)-start1
    len2=len(seq2)-start2
    
    
    # We set seq2 as the shortest sequence
    if len1 < len2:
        seq1, seq2     = seq2, seq1
        start1, start2 = start2, start1
        len1,len2      = len2, len1
    
    # if seq2 is empty returns 0
    if len2==0:
        return 0

    i=0
    pos2=start2
    for i in range(min(len1, len2)):
        #print seq1, seq2, start1, start2
        if seq1[start1+i] != seq2[start2+i]:
            return i
    
    # we have reached the end of seq2 (need to increment i) 
    return i+1

def LCP(SA):
    """
    Compute the longest common prefix for every adjacent suffixes.
    The result is a list of same size as SA.
    Given two suffixes at positions i and i+1,
    their LCP is stored at position i+1.
    A zero is stored at position 0 of the output.
    
    >>> SA=SuffixArray("abba", unit=UNIT_BYTE)
    >>> SA._LCP_values
    array('i', [0, 1, 0, 1])
    
    >>> SA=SuffixArray("", unit=UNIT_BYTE)
    >>> SA._LCP_values
    array('i')

    >>> SA=SuffixArray("", unit=UNIT_CHARACTER)
    >>> SA._LCP_values
    array('i')

    >>> SA=SuffixArray("", unit=UNIT_WORD)
    >>> SA._LCP_values
    array('i')

    >>> SA=SuffixArray("abab", unit=UNIT_BYTE)
    >>> SA._LCP_values
    array('i', [0, 2, 0, 1])
    """
    string=SA.string
    length=SA.length
    lcps=_array("i", [0]*length)
    SA=SA.SA
    
    if _trace:
        delta=max(length//100,1)
        for i, pos in enumerate(SA):
            if i%delta==0:
                percent=float((i+1)*100)/length
                print >> _stderr, "Compute_LCP %.2f%% (%i/%i)\r"%(percent, i+1, length),
            lcps[i]=_longestCommonPrefix(string, string, SA[i-1], pos)
    else:
        for i, pos in enumerate(SA):
            lcps[i]=_longestCommonPrefix(string, string, SA[i-1], pos)
    
    if _trace:
        print >> _stderr, "Compute_LCP %.2f%% (%i/%i)\r"%(100.0, length, length)
        
    if lcps:   # Correct the case where string[0] == string[-1]
        lcps[0] = 0
    return lcps
        
class SuffixArray(object):
    """
    Constructs the suffix array of the string using the processing unit specified.
    """
    def __init__(self, string, unit=DEFAULT_UNIT, encoding=DEFAULT_ENCODING, noLCPs=False):
        if unit==UNIT_WORD:
            self.tokSep=" "
        elif unit in (UNIT_CHARACTER, UNIT_BYTE):
            self.tokSep=""
        else:
            raise Exception("Unknown unit type identifier:", unit)
   
        start=_time()

        self.unit  = unit
        self.encoding = encoding
        
        if _trace: print >> _stderr, "Tokenization ...\r",
        string      = self.tokenize(string)
        if _trace: print >> _stderr, "Tokenization done"
        
        if _trace: print >> _stderr, "Renaming tokens ...\r",
        self.voc    = [None]+sorted(set(string))
        self.tokId = dict((char, iChar) for iChar,char in enumerate(self.voc))
        self.string = [self.tokId[c] for c in string]
        if _trace: print >> _stderr, "Renaming tokens done"

        self.vocSize= len(self.voc)
        self.length = len(string)
        self.SA     = _array("i", [0]*(self.length+3))
        self.string = _array("i", self.string+[0]*3)
        
        _suffixArray(self.string, self.SA, self.length, self.vocSize)
        del self.SA[self.length:]
        del self.string[self.length:]
        

        self.nbSentences = self.string.count(self.tokId.get("\n", 0))
        
        self.length = len(string)
        self.vocSize= len(self.voc) - 1 # decrement because of the None token
        if "\n" in self.tokId:
            self.vocSize-=1             # decrement because of the EOL token

        self.features=[]

        if not noLCPs:
            self.addFeatureSA(LCP)
        self.constructionTime=_time()-start
        
        if _trace: print >> _stderr, "construction time %.3fs"%self.constructionTime
        
    
    def addFeatureSA(self, callback, default=None, name=None):
        """
        Add a feature to the suffix array.
        The callback must return a sequence such that
        the feature at position i is attached to the suffix referenced by
        self.SA[i].
        
        It is called with one argument: the instance of SuffixArray self.
        The callback may traverse self.SA in any fashion.
        
        The default behavior is to name the new feature after the callback name.
        To give another name, set the argument name accordingly.
        
        When the feature of an unknown substring of the text is requested,
        the value of the default argument is used.
        
        If the feature attached to a suffix is independent of the other suffix
        features, then the method addFeature gives a better alternative.
        
        You may use addFeatureSA as a decorator as in the following example.
        
        Example: feature named bigram which attach the frequencies of the
        leading bigram to each suffix. 
        
        >>> SA=SuffixArray("mississippi", unit=UNIT_BYTE)
        
        >>> def bigram(SA):
        ...     res=[0]*SA.length
        ...     end=0
        ...     while end <= SA.length:
        ...     
        ...         begin=end-1
        ...         while end < SA.length and  SA._LCP_values[end]>=2:
        ...             if SA.SA[end]+2<=SA.length: #end of string
        ...                 end+=1
        ...         
        ...         nbBigram=end-begin
        ...         for i in xrange(begin, end):
        ...             if SA.SA[i]+2<=SA.length:
        ...                 res[i]=nbBigram
        ...     
        ...         end+=1
        ...     return res
        
        >>> SA.addFeatureSA(bigram, 0)
        
        >>> SA._bigram_values
        [0, 1, 2, 2, 1, 1, 1, 2, 2, 2, 2]
    
        >>> print str(SA).expandtabs(14) #doctest: +SKIP
        ...     10        'i'           LCP=0 ,       bigram=0 
        ...      7        'ippi'        LCP=1 ,       bigram=1 
        ...      4        'issippi'     LCP=1 ,       bigram=2 
        ...      1        'ississippi'  LCP=4 ,       bigram=2 
        ...      0        'mississipp'  LCP=0 ,       bigram=1 
        ...      9        'pi'          LCP=0 ,       bigram=1 
        ...      8        'ppi'         LCP=1 ,       bigram=1 
        ...      6        'sippi'       LCP=0 ,       bigram=2 
        ...      3        'sissippi'    LCP=2 ,       bigram=2 
        ...      5        'ssippi'      LCP=1 ,       bigram=2 
        ...      2        'ssissippi'   LCP=3 ,       bigram=2
        
        >>> SA.bigram('ip')
        1

        >>> SA.bigram('si')
        2

        >>> SA.bigram('zw')
        0

        """
        if name is None:
            featureName = callback.__name__
        else:
            featureName = name
            
        featureValues=callback(self)
        setattr(self, "_%s_values"%featureName, featureValues)
        setattr(self, "%s_default"%featureName, default)
        self.features.append(featureName)
        
        
        def findFeature(substring):
            res=self._findOne(substring,)
            if res is not False:
                return featureValues[res]
            else:
                return default
                
        setattr(self, featureName, findFeature)
  
    def addFeature(self, callback, default=None, name=None, arguments=None):
        """
        Add a feature to the suffix array.
        The callback must return the feature corresponding to the suffix at
        position self.SA[i].
        
        The callback must be callable (a function or lambda).
        The argument names of the callback are used to determine the data
        needed. If an argument is the name of feature already defined, then
        this argument will be the value of that feature for the current suffix.
        In addition the argument pos is the position of the current suffix
        and iSA is the index of pos in SA.
        Other attributes of the SuffixArray instance may be use as argument
        names.
        
        If the feature attached to a suffix depends on other suffix features,
        then the method addFeatureSA is the only choice.
        
        """
        if name is None:
            featureName=callback.__name__
        else:
            featureName=name
       
        if arguments is None:
            signature=getargspec(callback)[0]
        else:
            signature=arguments
        
       
        featureValues=[default]*(self.length)
        args=[getattr(self, "_%s_values"%featName) for featName in  signature]
        #print args
        for i, pos in enumerate(self.SA):
            arg=[j[i] for j in args]
            #print arg
            featureValues[i]=callback(*arg)
        #end alternative    
            
        setattr(self, "_%s_values"%featureName, featureValues)
        setattr(self, "%s_default"%featureName, default)
        self.features.append(featureName)
        
        
        def findFeature(substring):
            res=self._findOne(substring)
            if res:
                return featureValues[res]
            else:
                return default
                
        setattr(self, featureName, findFeature)
        
    def tokenize(self, string):
        """
        Tokenizer utility.
        When processing byte, outputs the string unaltered.
        The character unit type is used for unicode data, the string is
        decoded according to the encoding provided.
        In the case of word unit, EOL characters are detached from the
        preceding word, and outputs the list of words, i.e. the list of non-space strings
        separated by space strings.
        
        
        >>> SA=SuffixArray('abecedaire', UNIT_BYTE)
        
        >>> SA.tokenize('abecedaire')=='abecedaire'
        True
        >>> len(SA.tokenize('abecedaire'))
        10

        >>> SA=SuffixArray('abecedaire', UNIT_BYTE, "utf-8")
        
        >>> SA.tokenize('abecedaire')==u'abecedaire'
        True
        >>> len(SA.tokenize('abecedaire'))
        10

        >>> SA=SuffixArray('mississippi', UNIT_WORD)

        >>> SA.tokenize('miss issi ppi')
        ['miss', 'issi', 'ppi']
        
        >>> SA.tokenize('miss issi\\nppi')
        ['miss', 'issi', '\\n', 'ppi']
        
        """
        if self.unit   == UNIT_WORD:
            # the EOL character is treated as a word, hence a substitution
            # before split 
            
            return [token for token in  string.replace("\n", " \n ").split(self.tokSep) if token!=""]
        elif self.unit == UNIT_CHARACTER:
            return string.decode(self.encoding)
        else:
            return string
            
    def reprString(self, string, length):
        """
        Output a string of length tokens in the original form.
        If string is an integer, it is considered as an offset in the text.
        Otherwise string is considered as a sequence of ids (see voc and
        tokId).
        
        >>> SA=SuffixArray('mississippi', UNIT_BYTE)
        >>> SA.reprString(0, 3)
        'mis'

        >>> SA=SuffixArray('mississippi', UNIT_BYTE)
        >>> SA.reprString([1, 4, 1, 3, 3, 2], 5)
        'isipp'

        >>> SA=SuffixArray('missi ssi ppi', UNIT_WORD)
        >>> SA.reprString(0, 3)
        'missi ssi ppi'

        >>> SA=SuffixArray('missi ssi ppi', UNIT_WORD)
        >>> SA.reprString([1, 3, 2], 3)
        'missi ssi ppi'
        """
        if isinstance(string, int):
            length=min(length, self.length-string)
            string=self.string[string:string+length]
            
        
        voc=self.voc
        res= self.tokSep.join((voc[id] for id in string[:length]))
        if self.unit==UNIT_WORD:
            res=res.replace(" \n", "\n")
            res=res.replace("\n ", "\n")
        
        if self.unit==UNIT_CHARACTER:
            res=res.encode(self.encoding)

        return res
        
        
    def __str__(self, start=0, end=-1, maxSuffixLength=10):
        """
        Human readable string representation of the suffix array.
        
        """
        string=self.string
        SA=self.SA
        voc=self.voc
        tokSep=self.tokSep
        features=self.features
        
        res=[]
        if end==-1:
            end=self.length
            
        for i, pos in enumerate(SA[start:end]):
            
            suffix=self.reprString(pos, maxSuffixLength)[:maxSuffixLength]
            suffix=repr(suffix)
            suffix=suffix.ljust(maxSuffixLength+2)
            
            pos=str(pos).rjust(6)
            
            
            feat=",\t".join(["%s=%s "%(f,repr(getattr(self, "_%s_values"%f)[i])) for f in features])
            res.append("%s\t%s\t%s"%(pos, suffix, feat))
        return '\n'.join(res)
    

    def toFile(self, filename):
        """
        Save the suffix array instance including all features attached in
        filename. Accept any filename following the _open conventions,
        for example if it ends with .gz the file created will be a compressed
        GZip file.
        """
        start=_time()
        fd=_open(filename, "w")
        
        savedData=[self.string, self.unit,  self.voc, self.vocSize, self.SA, self.features]
        
        
        for featureName in self.features:
            featureValues = getattr(self, "_%s_values"%featureName)
            featureDefault = getattr(self, "%s_default"%featureName)
            
            savedData.append((featureValues,featureDefault))
        
        fd.write(_dumps(savedData, _HIGHEST_PROTOCOL))
        fd.flush()
        try:
            self.sizeOfSavedFile=getsize(fd.name)
        except OSError:#if stdout is used
            self.sizeOfSavedFile="-1"
        self.toFileTime=_time()-start
        if _trace: print >> _stderr, "toFileTime %.2fs"%self.toFileTime
        
            
        if _trace: print >> _stderr, "sizeOfSavedFile %sb"%self.sizeOfSavedFile
        fd.close()
    
    @classmethod
    def fromFile(cls, filename):
        """
        Load a suffix array instance from filename, a file created by
        toFile.
        Accept any filename following the _open conventions.
        """
        self = cls.__new__(cls) #new instance which does not call __init__  
        
        start=_time()
        
        savedData=_loads(_open(filename, "r").read())
        
        # load common attributes
        self.string, self.unit, self.voc, self.vocSize, self.SA, features = savedData[:6]
        self.length=len(self.SA)
        
        # determine token delimiter
        if self.unit==UNIT_WORD:
            self.tokSep=" "
        elif self.unit in (UNIT_CHARACTER, UNIT_BYTE):
            self.tokSep=""
        else:
            raise Exception("Unknown unit type identifier:", self.unit)
        
        # recompute tokId based on voc
        self.tokId=dict((char, iChar) for iChar,char in enumerate(self.voc))
        self.nbSentences = self.string.count(self.tokId.get("\n", 0))
        
        
        # Load features
        self.features=[]        
        for featureName, (featureValues, featureDefault) in zip(features, savedData[6:]):
            self.addFeatureSA((lambda _: featureValues), name=featureName, default=featureDefault)
        
        self.fromFileTime=_time()-start
        if _trace: print >> _stderr, "fromFileTime %.2fs"%self.fromFileTime
        return self
        
        
        
        
    def _findOne(self, subString):
        """
        >>> SA=SuffixArray("mississippi", unit=UNIT_BYTE)
        >>> SA._findOne("ippi")
        1
        
        >>> SA._findOne("missi")
        4
        """
        SA=self.SA
        LCPs=self._LCP_values
        string=self.string
        
        try:
            subString=_array("i", [self.tokId[c] for c in self.tokenize(subString)])
        except KeyError:
            # if a token of the subString is not in the vocabulary
            # the substring can't be in the string
            return False
        lenSubString=len(subString)
        
        
        #################################
        # Dichotomy search of subString #
        #################################
        lower=0
        upper=self.length
        success=False

        while upper-lower >0:
            middle=(lower+upper)//2
            
            middleSubString=string[SA[middle]:min(SA[middle]+lenSubString,self.length)]
            
            #NOTE: the cmp function is removed in Python 3
            #Strictly speaking we are doing one comparison more now
            if subString < middleSubString:
                upper=middle
            elif subString > middleSubString:
                lower=middle+1
            else:
                success=True
                break        
        

        if not success:
            return False
        else:
            return middle
            
            
    def find(self, subString, features=[]):
        """
        Dichotomy search of subString in the suffix array.
        As soon as a suffix which starts with subString is found, 
        it uses the LCPs in order to find the other matching suffixes.
        
        The outputs consists in a list of tuple (pos, feature0, feature1, ...)
        where feature0, feature1, ... are the features attached to the suffix
        at position pos.
        Features are listed in the same order as requested in the input list of
        features [featureName0, featureName1, ...]
        
        >>> SA=SuffixArray('mississippi', UNIT_BYTE)
        >>> SA.find("ssi")
        array('i', [5, 2])
        
        >>> SA.find("mi")
        array('i', [0])

        >>> SA=SuffixArray('miss A and miss B', UNIT_WORD)
        >>> SA.find("miss")
        array('i', [0, 3])

        >>> SA=SuffixArray('mississippi', UNIT_BYTE)
        >>> SA.find("iss", ['LCP'])
        [(4, 1), (1, 4)]

        >>> SA=SuffixArray('mississippi', UNIT_BYTE)
        >>> SA.find("A")
        array('i')
        
        >>> SA=SuffixArray('mississippi', UNIT_BYTE)
        >>> SA.find("pp")
        array('i', [8])
        
        >>> SA=SuffixArray('mississippi', UNIT_BYTE)
        >>> SA.find("ppp")
        array('i')


        >>> SA=SuffixArray('mississippi', UNIT_BYTE)
        >>> SA.find("im")
        array('i')
        """
        SA=self.SA
        LCPs=self._LCP_values
        string=self.string
       
        
        middle=self._findOne(subString)
        if middle is False:
            return _array('i')
            
        subString=_array("i", [self.tokId[c] for c in self.tokenize(subString)])
        lenSubString=len(subString)        

        ###########################################
        # Use LCPS to retrieve the other suffixes #
        ###########################################
        lower=middle
        upper=middle+1
        middleLCP=LCPs[middle]
        while lower>0 and LCPs[lower]>=lenSubString:
            lower-=1
        
        while upper<self.length and LCPs[upper]>=lenSubString:
            upper+=1
        
        ###############################################
        # When features is empty, outputs a flat list #
        ###############################################
        res=SA[lower:upper]
        if len(features)==0:
            return res

        ##############################################
        # When features is non empty, outputs a list #
        # of tuples (pos, feature_1, feature_2, ...) #
        ##############################################
        else:
            features=[getattr(self, "_%s_values"%featureName) for featureName in features]
            features=[featureValues[lower:upper] for featureValues in features]
            
            return zip(res, *features)



    


def parseArgv():
    """
    Command line option parser. 
    """
    parser = OptionParser()
    parser.usage=r""" cat <TEXT> | %prog [--unit <UNIT>] [--output <SA_FILE>]

Create the suffix array of TEXT with the processing UNIT and optionally store it in SA_FILE for subsequent use.
UNIT may be set to 'byte', 'character' (given an encoding with the --encoding option) or 'word', which is the default.
"""


    parser.add_option("-i", "--input",
                      action="store", type="string", dest="input",
                      default=False,
                      help="Path of the file containing the input text. When '-' is given, read the standard input (default). If the path ends with '.gz', reads the decompressed file.")
  
    parser.add_option("-o", "--output",
                      action="store", type="string", dest="output",
                      default=False,
                      help="Store the suffix array of the input to the file OUTPUT. When '-' is given, writes to the standard output. If the filename ends with '.gz', the suffix array will be stored  compressed.")

    parser.add_option("", "--load",
                      action="store", type="string", dest="SAFile",
                      default=False,
                      help="Load a suffix array from SAFILE, this option and --input are mutually exclusive.")

    parser.add_option("-u", "--unit",
                      action="store", type="string", dest="unit",
                      default=DEFAULT_UNIT_STR,
                      help="Processing unit used for the creation of the suffix array."+\
                      'Possible values are "byte", "character" and "word". Default is "%s".'%DEFAULT_UNIT_STR+\
                      "This option is ignored when the suffix array is loaded from SAFILE."+\
                      'For characters, the input is decoded according to the encoding set via the option --encoding.')

    parser.add_option("-e", "--encoding",
                      action="store", type="string", dest="encoding",
                      default=DEFAULT_ENCODING,
                      help="Encoding of the input. This information is required only when processing characters. Default is '%s'."%DEFAULT_ENCODING)
                       
    parser.add_option("-p", "--print",
                      action="store_true", dest="printSA",
                      default=False,
                      help="Prints the suffix array in a human readable format to the standard error output.")
  

    parser.add_option("", "--verbose",
                      action="store_true", dest="verbose",
                      default=False,
                      help="Prints more information.")

    parser.add_option("", "--no-lcps",
                      action="store_true", dest="noLCPs",
                      default=False,
                      help="Switch off the computation of LCPs. By doing so, the find functions are unusable.")
                    
    (options, args) = parser.parse_args(_argv)
    strings=args[1:]
    return (options, strings)
      
          
def main():
    """
    Entry point for the standalone script.
    
    """
    (options, strings)=parseArgv()
    global _suffixArray, _trace
    
    #############
    # Verbosity #
    #############
    _trace=options.verbose 
    
    ###################
    # Processing unit #
    ###################
    if options.unit == "byte":
        options.unit = UNIT_BYTE
    elif options.unit == "character":
        options.unit = UNIT_CHARACTER
    elif options.unit == "word":
        options.unit = UNIT_WORD
    else:
        print >> _stderr, "Please specify a valid unit type."
        exit(EXIT_BAD_OPTION)
    
    ######################
    # Build suffix array #
    ######################
    if not options.SAFile: # Build the suffix array from INPUT
        if not options.input:#default is standard input
            options.input="-"
        try:
            string=_open(options.input, "r").read()
        except IOError:
            print >> _stderr, "File %s does not exist."%options.input
            exit(EXIT_ERROR_FILE)
        
        SA=SuffixArray(string, options.unit, options.encoding, options.noLCPs)
    ########################
    # Or load suffix array #
    ########################
    elif not options.input and options.SAFile: #Load suffix array from SA_FILE
        try:
            SA=SuffixArray.fromFile(options.SAFile)
        except IOError:
            print >> _stderr, "SA_FILE %s does not exist."%options.SAFile
            exit(EXIT_ERROR_FILE)
    else:
            print >> _stderr, "Please set only one option amongst --input and --load.\n"+\
            "Type %s --help for more details."%_argv[0]
            exit(EXIT_BAD_OPTION)
            
    ######################
    # Print suffix array #
    ######################
    if options.printSA:
        #Buffered ouptut
        deltaLength=1000
        start=0
        while start<SA.length:
            print >> _stderr, SA.__str__(start, start+deltaLength)
            start+=deltaLength
        
    ####################################
    # Look for every string in strings #
    ####################################
    for string in strings:
        print >> _stderr, ""
        print >> _stderr, "Positions of %s:"%string
        print >> _stderr, "  %s"%list(SA.find(string))
    
    #########################
    # Save SAFILE if needed #
    #########################
    if options.output:
        SA.toFile(options.output)
        
    if _trace: print >> _stderr, "Done\r\n"

if __name__ == "__main__":
    if len(_argv)==2 and _argv[1]=="--test":
        from doctest import testmod
        testmod()
    else:
        main()
