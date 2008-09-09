class MatchPermutator
	attr_reader :grouped_matches, :permutations
	
	def initialize(match_array)
		@matches = match_array
		@grouped_matches = []
		@permutations = []
	end
	
	def process
		@grouped_matches = group(@matches)
#		@grouped_matches.each{|mg| mg.each{|m| p [m.word1.original,m.word2.original]}}
		permutate(@grouped_matches,0,@permutations)
	end
	
	def group(matches)
		groups = []
		_group = []
		group_id = nil
		matches.sort{|a,b| "#{a.word1.position}.#{a.word2.position}" <=> "#{b.word1.position}.#{b.word2.position}"}.each do |m|
      if (m.word1 != group_id)
      	group_id = m.word1
        groups << _group unless _group.empty?
        _group = []
      end
      _group << m
		end
    groups << _group unless _group.empty?
		return groups
	end
	
	def permutate(matchgroups,start_group,permutations)
#		matchgroups.each{|mg| p mg}
		if (valid_permutation?(matchgroups))
  	  permutations << degroup(matchgroups)
  	else
  		i = start_group
  		while (matchgroups[i].size==1 && i<matchgroups.size-1)
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
		  	new_groups << mg.select{ |m| m.word2 != match.word2 }
		  end
		end
		return new_groups.delete_if{ |g| g.empty? }
	end
	
	def valid_permutation?(_matchgroups)
		single_matchgroups = _matchgroups.all?{ |mg| mg.size==1 }
		unique_word2 = _matchgroups.collect{ |mg| mg.collect{|m| m.word2}}.uniq.size == _matchgroups.size
		return single_matchgroups && unique_word2
	end
	
	def degroup(matchgroups)
		matchgroups.collect{ |mg| mg[0] }
	end
	
end
