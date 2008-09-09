require 'collater/permutator'

class TC_Permutator < Test::Unit::TestCase
	
	def test_group
		permutator = Permutator.new([])
		raw_tuples = [[1,2],[1,3],[2,1],[2,3],[3,4]]
		expected = [[[1,2],[1,3]],[[2,1],[2,3]],[[3,4]]]
		assert_equal(expected, permutator.group(raw_tuples))
	end

	def test_degroup
		permutator = Permutator.new([])
		grouped_tuples = [[[1,2]],[[2,1]],[[3,4]]]
		expected = [[1,2],[2,1],[3,4]]
		assert_equal(expected, permutator.degroup(grouped_tuples))
	end
	
	def test_fix_cell1
		permutator = Permutator.new([])
		grouped_tuples = [[[1,2],[1,3]],[[2,1],[2,3]],[[3,4]]]
		expected=[[[1,3]],[[2,1]],[[3,4]]]
		assert_equal(expected, permutator.fix_cell(grouped_tuples, 0, [1,3]))
	end

	def test_fix_cell2
		permutator = Permutator.new([])
		grouped_tuples = [[[1,2],[1,3]],[[2,1],[2,3]],[[4,3]]]
		expected=[[[1,3]],[[2,1]]]
		assert_equal(expected, permutator.fix_cell(grouped_tuples, 0, [1,3]))
	end

  def test_valid_permutation
		permutator = Permutator.new([])

		valid_permutation = [[[1,2]],[[2,1]],[[3,4]]]
  	assert(permutator.valid_permutation?(valid_permutation))
  	
		invalid_permutation1 = [[[1,2]],[[2,1],[2,4]],[[3,4]]] # invalid because [[2,1],[2,4]] is not singular
  	assert(!permutator.valid_permutation?(invalid_permutation1))
  	
		invalid_permutation2 = [[[1,2]],[[2,1]],[[3,1]]] # invalid because [2,1] and [3,1] both map to 1
  	assert(!permutator.valid_permutation?(invalid_permutation2))
  end
 
  def test_permutate1
  	permutator = Permutator.new([[1,2],[1,3],[2,1],[2,3],[3,4]])
  	permutator.process
  	expected_permutations = [
  	  [[1,2],[2,1],[3,4]],
  	  [[1,2],[2,3],[3,4]],
  	  [[1,3],[2,1],[3,4]]
  	]
  	assert_equal(expected_permutations.sort, permutator.permutations.sort)
  end
	
  def test_permutate2
  	permutator = Permutator.new([
	  	[1,2], [1,3],
	  	[2,1], [2,3],
	  	[3,1], [3,4],
	  	[4,3]
  	])
  	permutator.process
  	expected_permutations = [
  	  [[1,2],[2,1],[3,4],[4,3]],
  	  [[1,2],[2,3],[3,1]],
  	  [[1,2],[2,3],[3,4]],
  	  [[1,3],[2,1],[3,4]],
  	  [[1,3],[3,1]]
  	]
  	assert_equal(expected_permutations, permutator.permutations.sort)
  end

  def test_permutate3
  	permutator = Permutator.new([[1,2],[1,3],[2,1],[2,4],[3,4]])
  	permutator.process
  	expected_permutations = [
  	  [[1,2],[2,1],[3,4]],
  	  [[1,2],[2,4]],
  	  [[1,3],[2,1],[3,4]],
  	  [[1,3],[2,4]]
  	]
  	assert_equal(expected_permutations, permutator.permutations.sort)
  end

  def test_permutate4
  	permutator = Permutator.new([[1,1],[2,2],[3,2]])
  	permutator.process
  	expected_permutations = [
  	  [[1,1],[2,2]],
  	  [[1,1],[3,2]]
  	]
  	assert_equal(expected_permutations, permutator.permutations.sort)
  end

  def test_permutate5
  	permutator = Permutator.new([[1,1],[2,2],[2,3]])
  	permutator.process
  	expected_permutations = [
  	  [[1,1],[2,2]],
  	  [[1,1],[2,3]]
  	]
  	assert_equal(expected_permutations, permutator.permutations.sort)
  end
end