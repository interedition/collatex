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

class PTuple
  attr_reader :tuple
  
  def initialize(tuple)
    @tuple = tuple
    @fixed = false
  end
  
  def [](i)
    @tuple[i]
  end
  
  def fixed?
    @fixed    
  end
  
  def fix!
    @fixed = true
  end  
end

class Permutator
	attr_reader :grouped_tuples, :permutations
	
	def initialize(tuple_array)
		@tuples = tuple_array
		@permutations = []
	end
	
	def process
	  ptuples = @tuples.collect{|t| new PTuple(t) }
		permutate(ptuples,@permutations)
	end
	
	def permutate(ptuples,permutations)
	  # find the first non-fixed ptuple
	  ptuple = ptuples.find{|pt| !pt.fixed? }
		if (ptuple.nil?)
		  permutations << ptuples.collect{|pt| pt.tuple}
  	else
  		alternatives = find_alternatives(ptuples, ptuple)
  		alternatives.each do |a|
  		  new_ptuples = fix_cell(ptuples, a)
  		  permutate(new_ptuples, permutations)
  		end
	  end
	end
	
	def find_alternatives(array, element)
  	return array.select{|e| e[0] == element[0] || e[1] == element[1]}
	end
	
	def fix_cell(ptuples, ptuple)
	  ptuple.fix!
		ptuples.select { |pt| pt.fixed? || (pt[0] != ptuple[0] && pt[1] != ptuple[1])}
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
