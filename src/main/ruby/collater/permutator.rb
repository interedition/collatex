#class Set
#	def delete_subsets
#		new_set = self
#		self.each do |a|
#			a_is_subset = false
#		  self.each do |b|
#				next if a.eql? b || a.size >= b.size
#				a_in_b = true
#				a.each { |e| a_in_b &= b.include? e }
#				a_is_subset |= a_in_b 
#			end
#			new_set = self.delete(a) if a_is_subset
#		end
#		return new_set
#	end
#end

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
	
#	def group(tuples)
#		groups = Set.new
#		_group = []
#		group_id = nil
#		tuples.sort{|a,b| "#{a[0]}.#{a[1]}" <=> "#{b[0]}.#{b[1]}"}.each do |t|
#      if (t[0] != group_id)
#      	group_id = t[0]
#        groups << _group unless _group.empty?
#        _group = []
#      end
#      _group << t
#		end
#    groups << _group unless _group.empty?
#
#		tuples.sort{|a,b| "#{a[1]}.#{a[0]}" <=> "#{b[1]}.#{b[0]}"}.each do |t|
#      if (t[1] != group_id)
#      	group_id = t[1]
#        groups << _group unless _group.empty?
#        _group = []
#      end
#      _group << t
#		end
#    groups << _group unless _group.empty?
#		return groups.delete_subsets.to_a.sort
#	end

	def group(tuples)
		groups=[]
		t0=[] # the values of t[0] that've been done
		t1=[] # the values of t[1] that've been done
    tuples.each do |tuple|
    	group0=[]
    	group1=[]
      if (!t0.include? tuple[0])
        # group on t[0]
        group0 = tuples.select{ |t| t[0] == tuple[0]}
        t0 << tuple[0]
      end
      if (!t1.include? tuple[1])
        group1 = tuples.select{ |t| t[1] == tuple[1]}
        t1 << tuple[1]
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
	
	def permutate(tuplegroups,start_group,permutations)
		if (valid_permutation?(tuplegroups))
  	  permutations << degroup(tuplegroups)
  	else
  		i = start_group
  		while (tuplegroups[i].size==1 && i<tuplegroups.size-1)
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
		  	new_groups << tg.select{ |t| t[1] != tuple[1] && t[0] != tuple[0]}
		  end
		end
		return new_groups.delete_if{ |g| g.empty? }.sort.uniq
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
