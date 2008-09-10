require 'collater/collate_r'

class TC_CollateR < Test::Unit::TestCase
	
	def test_each_pair
		pairs=[]
		[1,2,3,4].each_pair {|p| pairs << p }
		expected = [[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]] 
		assert_equal(expected, pairs)
	end
	
  def test_transposition1
  	wp = WitnessPair.new(Witness.new("a b c d e"), Witness.new("a c d b e"))
  	mp = wp.matchpermutations.first
  	matchsequences = wp.calculate_matchsequences(mp)
  	transpositions = wp.calculate_transpositions(matchsequences)
  	assert_equal(1,transpositions.size)
  	assert_equal("Transposition: 'b' <=> 'c d'", transpositions.to_a.first.to_s)
  end

  def test_transposition2
  	wp = WitnessPair.new(Witness.new("a b x c d "), Witness.new("a c d x b"))
  	mp = wp.matchpermutations.first
  	matchsequences = wp.calculate_matchsequences(mp)
  	transpositions = wp.calculate_transpositions(matchsequences)
  	assert_equal(1,transpositions.size)
  	assert_equal("Transposition: 'b' <=> 'c d'", transpositions.to_a.first.to_s)
  end

  def test_transposition3
  	wp = WitnessPair.new(Witness.new("a b x c d "), Witness.new("c d x a b"))
  	mp = wp.matchpermutations.first
  	matchsequences = wp.calculate_matchsequences(mp)
  	transpositions = wp.calculate_transpositions(matchsequences)
  	assert_equal(1,transpositions.size)
  	assert_equal("Transposition: 'a b' <=> 'c d'", transpositions.to_a.first.to_s)
  end

end