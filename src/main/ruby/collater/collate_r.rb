require 'rubygems'
require 'text'
require 'set'
require 'collater/match_permutator'


class Array
	def each_pair
		(0..self.size-2).each do |i|
			(i+1..self.size-1).each do |j|
				yield [self[i],self[j]]
			end
		end
	end
end

class Match
	attr_reader :word1, :word2, :levenshtein_distance

  def initialize(word1,word2,levdistance=0)
    @word1=word1
    @word2=word2
    @levenshtein_distance = levdistance
  end
  
  def to_s
  	"Match: #{word1} #{word2} #{levenshtein_distance}"
  end
	
	def eql?(other)
		(self.word1.eql?(other.word1) || self.word1.eql?(other.word2)) &&
		(self.word2.eql?(other.word1) || self.word2.eql?(other.word2))
  end

	def hash
		word1.normalized.hash + word2.normalized.hash
	end
	
end

class Transposition < Match 
	def to_s
		"Transposition: '#{word1.original}' <=> '#{word2.original}'"
	end
end

class Word
  attr_reader :original, :normalized, :position
  alias :word :original
  
  def initialize(text,position)
    @original = text
    @normalized = text.downcase.gsub(/\W/,'')
    @position = position
  end
  
  def distance_to(other_word)
    w1 = @normalized
    w2 = other_word.normalized
    lev = Text::Levenshtein.distance(w1, w2)
    normalized_lev = (2*lev.to_f) / (w1.size + w2.size)
    return normalized_lev 
  end
  
  def matches_with?(other_word)
    distance_to(other_word) < 0.5
  end
  
  def to_s
  	"Word: #{original} at #{position}; normalized = #{normalized}"
  end
end

class Witness
  attr_reader :words
  
  def initialize(string)
    i=0
    @words = string.split(' ').collect{ |w| i+=1; Word.new(w,i) }    
  end
  
  def [](index)
    @words[index-1]
  end
end

class WitnessPair
	attr_reader :matches, :matchpermutations
	
  def initialize(witness1,witness2)
    @matches = calculate_matches(witness1,witness2)

  	permutator = MatchPermutator.new(@matches)
  	permutator.process
    @matchpermutations = permutator.permutations

#    @matchpermutations.each do |mp|
#      matchsequences = calculate_matchsequences(mp)
#      transpositions = calculate_transpositions(matchsequences)
#    end
  end
  
  def calculate_matches(w1,w2)
    matches=[]
    w1.words.each do |word1|
      w2.words.each do |word2|
       if (word1.normalized.eql? word2.normalized)
         matches << Match.new(word1,word2)
       else
         lev_distance = word1.distance_to(word2)
         matches << Match.new(word1,word2,lev_distance) if (lev_distance<0.5)
       end 
      end      
    end
    return matches
  end
  
  def calculate_matchsequences(matches)
  	groups = []
  	group = []
  	prev_index1=0
  	prev_index2=0
  	matches.each do |m|
  		index1 = m.word1.position 
  		index2 = m.word2.position
  		if ((index1-prev_index1) != (index2-prev_index2))
  			groups << group unless group.empty?
  	 	  group=[]
  		end
  		group << m
  		prev_index1=index1
  		prev_index2=index2
  	end
		groups << group unless group.empty?
  	return groups
   end
 
   def calculate_transpositions(matchsequences)
   	 transpositions=Set.new
   	 matchsequences_in_base = matchsequences.collect{ |mg| mg.collect{ |m| m.word1 } }
   	 matchsequences_in_witness = matchsequences.sort{ |a,b| a[0].word2.position <=> b[0].word2.position }.collect{ |mg| mg.collect{ |m| m.word2 }}
   	 i=0; matchbase = matchsequences_in_base.collect{ |g| i+=1; Word.new(g.collect{|w| w.original }.join(' '),i) }
   	 i=0; matchwitness = matchsequences_in_witness.collect{ |g| i+=1; Word.new(g.collect{|w| w.original }.join(' '),i) }
   	 matchbase.each_with_index do |w1,i1|
   	   w1_in_witness = matchwitness.find{|w| w.matches_with?(w1) }
   	   position1_in_witness = matchwitness.index(w1_in_witness)
   	   next if position1_in_witness.eql? i1

   	   w2 = matchbase[position1_in_witness]
   	   w2_in_witness = matchwitness.find{|w| w.matches_with?(w2) }
   	   position2_in_witness = matchwitness.index(w2_in_witness)
   	   transpositions << Transposition.new(w1,w2) if (position2_in_witness.eql? i1) # && !(transpositions.include? Transposition.new(w2,w1)))
   	 end
   	 return transpositions
   end
 
end

class CollateR
  def initialize(usecase)
  	usecase.each_pair do |pair|
	    w1=Witness.new(pair[0])
	    w2=Witness.new(pair[1])
	    puts "Witnesses:\n  '#{pair[0]}'\n  '#{pair[1]}'\n:"
	    wp = WitnessPair.new(w1,w2)
	    puts wp
	    wp.matches.each{|m| p [m.word1.original,m.word2.original]}
	    puts "permutations:"
	    wp.matchpermutations.each{|mp| p mp.sort{|a,b| a.word1.position <=> b.word1.position }.collect{|m| ["#{m.word1.original}(#{m.word1.position})","#{m.word2.original}(#{m.word2.position})"]}}
#	    puts "matchsequences:"
#	    wp.matchsequences.each {|mg| p mg.collect{|m| m.word1.original}.join(' ')}
#	    puts "Transpositions:"
#	    wp.transpositions.each{|t| p t}
#	    puts
  	end
  end
end

class Main
	def initialize
		usecases = []
		usecases << [ 'the black block', 'the black cat' ]
		usecases << [ 'a cat or dog', 'a cat and dog and' ]
		usecases << [ 'Auch hier hab ich wieder ein Pl�tzchen', 'Ich hab auch hier wieder ein Pl�zchen', 'Ich hab auch hier wieder ein Pl�zchen' ]
		usecases << [ 'He was agast, so', 'He was agast', 'So he was agast', 'He was so agast', 'He was agast and feerd', 'So was he agast' ]
		usecases << [ 'Hebban olla vogala nestas hagunnan hinase hic enda thu wat unbidan we nu', 'Alle vogels zijn al aan het nestelen, behalve jij en ik; waar wachten we nog op?' ]
		usecases << [ 'the big bug had a big head', 'the bug big had a big head', 'the bug had a small head' ]
		usecases << [ 'the black cat and the black mat', 'the black dog and the black mat' ]
		usecases << [ 'the black cat on the table', 'the black saw the black cat on the table' ]
		usecases << [ 'the black cat sat on the mat', 'the cat sat on the black mat' ]
		usecases << [ 'The black cat', 'The black and white cat', 'The black and green cat', 'The black very special cat', 'The black not very special cat' ]
		usecases << [ 'the black cat', 'THE BLACK CAT', 'The black cat', 'The, black cat' ]
		usecases << [ 'the bug big had a big head', 'the bug had a small head' ]
		usecases << [ 'the drought of march hath perced to the root and is this the right', 'the first march of drought pierced to the root and this is the ', 'the first march of drought hath perced to the root' ]
		usecases << [ 'the drought of march hath perced to the root', 'the march of the drought hath perced to the root', 'the march of drought hath perced to the root' ]
		usecases << [ 'the very first march of drought hath', 'the drought of march hath', 'the drought of march hath' ]
		usecases << [ 'the white and black cat', 'The black cat', 'the black and white cat', 'the black and green cat' ]
		usecases << [ 'This Carpenter hadde wedded newe a wyf', 'This Carpenter hadde wedded a newe wyf', 'This Carpenter hadde newe wedded a wyf', 'This Carpenter hadde wedded newly a wyf', 'This Carpenter hadde E wedded newe a wyf', 'This Carpenter hadde newli wedded a wyf', 'This Carpenter hadde wedded a wyf' ]
		usecases << [ 'When April with his showers sweet with fruit The drought of March has pierced unto the root', 'When showers sweet with April fruit The March of drought has pierced to the root', 'When showers sweet with April fruit The drought of March has pierced the rood' ]
		
		usecases.each { |u| CollateR.new(u) }		
	end
end
