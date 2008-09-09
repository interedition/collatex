class Permutator
	attr_reader :grouped_tuples, :permutations
	
	def initialize(tuple_array)
		@tuples = tuple_array
		@grouped_tuples = []
		@permutations = []
	end
	
	def process
		@grouped_tuples = group(@tuples)
		permutate(@grouped_tuples,0,@permutations)
	end
	
	def group(tuples)
		groups = []
		_group = []
		group_id = nil
		tuples.sort{|a,b| "#{a[0]}.#{a[1]}" <=> "#{b[0]}.#{b[1]}"}.each do |t|
      if (t[0] != group_id)
      	group_id = t[0]
        groups << _group unless _group.empty?
        _group = []
      end
      _group << t
		end
    groups << _group unless _group.empty?
		return groups
	end
	
	def permutate(tuplegroups,start_group,permutations)
		if (valid_permutation?(tuplegroups))
  	  permutations << degroup(tuplegroups)
  	else
  		i = start_group
  		while (tuplegroups[i].size==1 && i<tuplegroups.size)
  		  i+=1
  	  end
  	  if (i<tuplegroups.size)
				tuplegroups[i].each do |t|
					new_tuplegroups = fix_cell(tuplegroups, i, t)
					permutate(new_tuplegroups, i+1, permutations)
				end
		  end
	  end
	end
	
	def fix_cell(_tuplegroups, index, tuple)
		new_groups = []
		_tuplegroups.each_with_index do |tg,i|
		  if (i == index)
		  	new_groups << [tuple]
		  else
		  	new_groups << tg.select{ |t| t[1] != tuple[1] }
		  end
		end
		return new_groups.delete_if{ |g| g.empty? }
	end
	
	def valid_permutation?(_tuplegroups)
		single_tuplegroups = _tuplegroups.all?{ |tg| tg.size==1 }
		unique_t2 = _tuplegroups.collect{ |tg| tg.collect{|t| t[1]}}.uniq.size == _tuplegroups.size
		return single_tuplegroups && unique_t2
	end
	
	def degroup(tuplegroups)
		tuplegroups.collect{ |tg| tg[0] }
	end
	
end

#permutator = Permutator.new([[1,2],[5,6],[1,3],[3,3],[2,1],[2,4],[3,4]])
#permutator.grouped_tuples.each{|g| p g}
#p permutator.permutations
