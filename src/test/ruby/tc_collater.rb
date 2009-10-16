require 'test/unit'
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

#  def test_complex
#  	wp = WitnessPair.new(Witness.new("The black dog chases a red cat."), Witness.new("A red cat chases the yellow dog"))
#  	mp = wp.matchpermutations.first
#  	matchsequences = wp.calculate_matchsequences(mp)
#  	transpositions = wp.calculate_transpositions(matchsequences)
#  	assert_equal(0,transpositions.size)
#  end

	def test_collater1
	  CollateR.new([ 'the black block', 'the black cat' ])
	end
	
	def test_collater2
	  CollateR.new([ 'a cat or dog', 'a cat and dog and' ])
	end
	
	def test_collater3
	  CollateR.new([ 'Auch hier hab ich wieder ein Plätzchen', 'Ich hab auch hier wieder ein Pläzchen', 'Ich hab auch hier wieder ein Pläzchen' ])
	end
	
	def test_collater4
	  CollateR.new([ 'He was agast, so', 'He was agast', 'So he was agast', 'He was so agast', 'He was agast and feerd', 'So was he agast' ])
	end
	
	def test_collater5
	  CollateR.new([ 'Hebban olla vogala nestas hagunnan hinase hic enda thu wat unbidan we nu', 'Alle vogels zijn al aan het nestelen, behalve jij en ik; waar wachten we nog op?' ])
	end
	
	def test_collater6
	  CollateR.new([ 'the big bug had a big head', 'the bug big had a big head', 'the bug had a small head' ])
	end
	
	def test_collater7
	  CollateR.new([ 'the black cat and the black mat', 'the black dog and the black mat' ])
	end
	
	def test_collater8
	  CollateR.new([ 'the black cat on the table', 'the black saw the black cat on the table' ])
	end
	
	def test_collater9
	  CollateR.new([ 'the black cat sat on the mat', 'the cat sat on the black mat' ])
	end
	
	def test_collater10
	  CollateR.new([ 'The black cat', 'The black and white cat', 'The black and green cat', 'The black very special cat', 'The black not very special cat' ])
	end
	
	def test_collater11
	  CollateR.new([ 'the black cat', 'THE BLACK CAT', 'The black cat', 'The, black cat' ])
	end
	
	def test_collater12
	  CollateR.new([ 'the bug big had a big head', 'the bug had a small head' ])
	end
	
	def test_collater13
	  CollateR.new([ 'the drought of march hath perced to the root and is this the right', 'the first march of drought pierced to the root and this is the ', 'the first march of drought hath perced to the root' ])
	end
	
	def test_collater14
	  CollateR.new([ 'the drought of march hath perced to the root', 'the march of the drought hath perced to the root', 'the march of drought hath perced to the root' ])
	end
	
	def test_collater15
	  CollateR.new([ 'the very first march of drought hath', 'the drought of march hath', 'the drought of march hath' ])
	end
	
	def test_collater16
	  CollateR.new([ 'the white and black cat', 'The black cat', 'the black and white cat', 'the black and green cat' ])
	end
	
	def test_collater17
	  CollateR.new([ 'This Carpenter hadde wedded newe a wyf', 'This Carpenter hadde wedded a newe wyf', 'This Carpenter hadde newe wedded a wyf', 'This Carpenter hadde wedded newly a wyf', 'This Carpenter hadde E wedded newe a wyf', 'This Carpenter hadde newli wedded a wyf', 'This Carpenter hadde wedded a wyf' ])
	end
	
	def test_collater18
	  CollateR.new([ 'When April with his showers sweet with fruit The drought of March has pierced unto the root', 'When showers sweet with April fruit The March of drought has pierced to the root', 'When showers sweet with April fruit The drought of March has pierced the rood' ])
	end

end
