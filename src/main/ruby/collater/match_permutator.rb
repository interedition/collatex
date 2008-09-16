class MatchPermutator
	attr_reader :grouped_matches, :permutations
	
	def initialize(match_array)
		@matches = match_array
		@grouped_matches = []
		@permutations = []
	end
	
	def process
		@grouped_matches = group(@matches)
		@grouped_matches.each{|mg| puts "group:"; mg.each{|m| p ["#{m.word1.original} (#{m.word1.position})","#{m.word2.original} (#{m.word2.position})"]}}
		permutate(@grouped_matches,0,@permutations)
	end
	
	def group(matches)
#		groups = []
#		_group = []
#		group_id = nil
#		matches.sort{|a,b| "#{a.word1.position}.#{a.word2.position}" <=> "#{b.word1.position}.#{b.word2.position}"}.each do |m|
#      if (m.word1 != group_id)
#      	group_id = m.word1
#        groups << _group unless _group.empty?
#        _group = []
#      end
#      _group << m
#		end
#    groups << _group unless _group.empty?
#		return groups
		groups=[]
		t0=[] # the values of t[0] that've been done
		t1=[] # the values of t[1] that've been done
    matches.each do |match|
    	group0=[]
    	group1=[]
    	pos0=match.word1.position
    	pos1=match.word2.position
      if (!t0.include? pos0)
        # group on t[0]
        group0 = matches.select{ |m| m.word1.position == pos0}
        t0 << pos0
      end
      if (!t1.include? pos1)
        group1 = matches.select{ |m| m.word2.position == pos1}
        t1 << pos1
      end
		  set0 = Set.new(group0)
		  set1 = Set.new(group1)
		  if (set0.superset?(set1))
		  	groups << group0 unless group0.empty?
		  elsif (set1.superset?(set0))
		  	groups << group1.to_a unless group1.empty?
		  else
		  	groups << group0 unless group0.empty?
		  	groups << group1 unless group1.empty?
		  end
	  end
		return groups
	end
	
	def permutate(matchgroups,start_group,permutations)
		if (valid_permutation?(matchgroups))
  	  permutations << degroup(matchgroups)
  	else
  		i = start_group
  		while (!matchgroups[i].nil? && matchgroups[i].size==1 && i<matchgroups.size-1)
  		  i+=1
  	  end
  	  if (i<matchgroups.size)
				matchgroups[i].each do |match|
					new_matchgroups = fix_cell(matchgroups, i, match)
					permutate(new_matchgroups, i+1, permutations)
				end
		  end
	  end
	end
	
	def fix_cell(_matchgroups, index, match)
		new_groups = []
		_matchgroups.each_with_index do |mg,i|
		  if (i == index)
		  	new_groups << [match]
		  else
		  	new_groups << mg.select{ |m| m.word1 != match.word1 && m.word2 != match.word2 }
		  end
		end
		return new_groups.delete_if{ |g| g.empty? }
	end
	
	def valid_permutation?(_matchgroups)
		single_matchgroups = _matchgroups.all?{ |mg| mg.size==1 }
		all_positions1 = _matchgroups.collect{ |mg| mg.collect{|m| m.word1.position}}
		all_positions2 = _matchgroups.collect{ |mg| mg.collect{|m| m.word2.position}}
		unique_word1 = all_positions1.uniq.size == all_positions1.size
		unique_word2 = all_positions2.uniq.size == all_positions2.size
		return single_matchgroups && unique_word1 && unique_word2
	end
	
	def degroup(matchgroups)
		matchgroups.collect{ |mg| mg[0] }
	end
	
end
