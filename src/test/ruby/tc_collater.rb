require 'collater/collate_r'

class TC_CollateR < Test::Unit::TestCase
	
	def test_each_pair
		pairs=[]
		[1,2,3,4].each_pair {|p| pairs << p }
		expected = [[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]] 
		assert_equal(expected, pairs)
	end
end