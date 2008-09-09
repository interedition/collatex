require 'text'

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
	attr_reader :matches, :matchpermutations, :matchgroups, :transpositions
	
  def initialize(witness1,witness2)
    @matches = calculate_matches(witness1,witness2)
    @matchpermutations = calculate_permutations(@matches)
    @matchgroups = calculate_matchgroups(@matches)
    @transpositions = calculate_transpositions(@matchgroups)
  end
  
  def calculate_permutations(matches)
  	permutations=[]
  	
  	all_matching_basewords = matches.collect{|m| m.word1 }.uniq
  	matches_per_baseword = all_matching_basewords.collect do |w|
  	  [w, matches.find_all{|m| m.word1.eql? w}.collect{|m| [ m.word2, m.levenshtein_distance]} ]
  	end
  	matches_per_baseword.each{|m| p m }
    permutations = []
    permutate(matches_per_baseword, permutations)
  	return permutations
  end
  
  def permutate(match_array, permutations)
  	match_array.each{|m| p m }
  	match_array.each_with_index do |e,i|
  		matches_in_witness = e[1]
  	  if (matches_in_witness.size > 1)
  	  	matches_in_witness.each do |mw|
  	  		new_array = Marshal.load(Marshal.dump(match_array)) # deep clone
  	  		new_array[i]=[e[0],[mw]]
  	  		puts "permutating..."
  	  		permutate(new_array, permutations)
  	  	end
  	  end
  	end
  	permutation_matches = match_array.collect{|m| Match.new(m[0],m[1][0][0], m[1][0][1])}
  	matchgroups = calculate_matchgroups(permutation_matches)
  	permutations << {
  	  :matches => permutation_matches,
  	  :matchgroups => matchgroups,
  	  :transpositions => calculate_transpositions(matchgroups)
  	}
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
  
  def calculate_matchgroups(matches)
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
 
   def calculate_transpositions(matchgroups)
   	 transpositions=[]
   	 matchgroups_in_base = matchgroups.collect{ |mg| mg.collect{ |m| m.word1 }}
   	 matchgroups_in_witness = matchgroups.sort{|a,b| a[0].word2.position <=> b[0].word2.position }.collect{ |mg| mg.collect{ |m| m.word2 }}
   	 i=0; matchbase = matchgroups_in_base.collect{|g| i+=1; Word.new(g.collect{|w| w.original }.join(' '),i)}
   	 i=0;matchwitness = matchgroups_in_witness.collect{|g| i+=1; Word.new(g.collect{|w| w.original }.join(' '),i)}
   	 matchbase.each_with_index do |w1,i1|
   	   w1_in_witness = matchwitness.find{|w| w.matches_with?(w1) }
   	   position1_in_witness = matchwitness.index(w1_in_witness)
   	   next if position1_in_witness.eql? i1

   	   w2 = matchbase[position1_in_witness]
   	   w2_in_witness = matchwitness.find{|w| w.matches_with?(w2) }
   	   position2_in_witness = matchwitness.index(w2_in_witness)
   	   transpositions << [w1,w2] if position2_in_witness.eql? i1
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
	    puts "Matchgroups:"
	    wp.matchgroups.each {|mg| p mg.collect{|m| m.word1.original}.join(' ')}
	    puts "Transpositions:"
	    wp.transpositions.each{|t| p t}
	    puts
  	end
  end
end

class Main
	def initialize
		usecases = []
		usecases << [ 'the black block', 'the black cat' ]
#		usecases << [ 'a cat or dog', 'a cat and dog and' ]
#		usecases << [ 'Auch hier hab ich wieder ein Pl�tzchen', 'Ich hab auch hier wieder ein Pl�zchen', 'Ich hab auch hier wieder ein Pl�zchen' ]
#		usecases << [ 'He was agast, so', 'He was agast', 'So he was agast', 'He was so agast', 'He was agast and feerd', 'So was he agast' ]
#		usecases << [ 'Hebban olla vogala nestas hagunnan hinase hic enda thu wat unbidan we nu', 'Alle vogels zijn al aan het nestelen, behalve jij en ik; waar wachten we nog op?' ]
#		usecases << [ 'the big bug had a big head', 'the bug big had a big head', 'the bug had a small head' ]
#		usecases << [ 'the black cat and the black mat', 'the black dog and the black mat' ]
#		usecases << [ 'the black cat on the table', 'the black saw the black cat on the table' ]
#		usecases << [ 'the black cat sat on the mat', 'the cat sat on the black mat' ]
#		usecases << [ 'The black cat', 'The black and white cat', 'The black and green cat', 'The black very special cat', 'The black not very special cat' ]
#		usecases << [ 'the black cat', 'THE BLACK CAT', 'The black cat', 'The, black cat' ]
#		usecases << [ 'the bug big had a big head', 'the bug had a small head' ]
#		usecases << [ 'the drought of march hath perced to the root and is this the right', 'the first march of drought pierced to the root and this is the ', 'the first march of drought hath perced to the root' ]
#		usecases << [ 'the drought of march hath perced to the root', 'the march of the drought hath perced to the root', 'the march of drought hath perced to the root' ]
#		usecases << [ 'the very first march of drought hath', 'the drought of march hath', 'the drought of march hath' ]
#		usecases << [ 'the white and black cat', 'The black cat', 'the black and white cat', 'the black and green cat' ]
#		usecases << [ 'This Carpenter hadde wedded newe a wyf', 'This Carpenter hadde wedded a newe wyf', 'This Carpenter hadde newe wedded a wyf', 'This Carpenter hadde wedded newly a wyf', 'This Carpenter hadde E wedded newe a wyf', 'This Carpenter hadde newli wedded a wyf', 'This Carpenter hadde wedded a wyf' ]
#		usecases << [ 'When April with his showers sweet with fruit The drought of March has pierced unto the root', 'When showers sweet with April fruit The March of drought has pierced to the root', 'When showers sweet with April fruit The drought of March has pierced the rood' ]
		
		usecases.each { |u| CollateR.new(u) }		
	end
end

Main.new
