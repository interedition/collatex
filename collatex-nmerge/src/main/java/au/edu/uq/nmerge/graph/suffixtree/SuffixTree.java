/*
 * NMerge is Copyright 2009-2011 Desmond Schmidt
 *
 * This file is part of NMerge. NMerge is a Java library for merging
 * multiple versions into multi-version documents (MVDs), and for
 * reading, searching and comparing them.
 *
 * NMerge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package au.edu.uq.nmerge.graph.suffixtree;
import au.edu.uq.nmerge.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a Java translation of Shlomo Yona's open source 
 * C implementation of Esko Ukkonen's Suffix Tree algorithm
 * http://mila.cs.technion.ac.il/~yona/suffix_tree/
 * @author Desmond Schmidt 25/10/08
 */
public class SuffixTree 
{
  private static final Logger LOG = LoggerFactory.getLogger(SuffixTree.class);

	/** Used in function traceString for skipping (Ukkonen's Skip Trick). */
	enum SkipType
	{
		skip, 
		noSkip
	}
	/** Used in method applyRule2 - two types of rule 2 - see function 
	 * for more details.*/
	enum Rule2Type  
	{
		newSon,
		split
	}
	/** Signals whether last matching position is the last one of the 
	 * current edge */
	enum LastPosType 
	{
		lastCharInEdge, 
		otherChar
	}
	/** error value calculated in tree init phase */
	int stError;
	/** Used to mark the node that has no suffix link yet. According to 
	 * Ukkonen, it will have one by the end of the current phase. */
	Node suffixless;
	/** The virtual end of all leaves */
	int e;
    /** The one and only real source string of the tree. All edge-labels
      contain only indices to this string and do not contain the characters
      themselves */
	byte[] treeString;
	/** The node that is the head of all others. It has no siblings nor a 
	 * father */
	public Node root;

    /** length of treeString minus the '$' */
    int length;
	/** needed to store VAR parameters in traceSingleEdge */
	class TraceReturnValue
	{
		/** Last matching position in edge */
		int edgePos; 
        /** Last matching position in tree source string */
        int charsFound;   
        /** true if search is done, false if not */
        boolean searchDone;
	}
	/** required by SPA */
	class ExtensionParam
	{
        /** The last extension performed in the previous phase */
 		int extension;         
        /** 1 if the last rule applied is 3 */
        char repeatedExtension;
	}
	class Path 
	{
		int begin;
		int end;
	}
	/**
	 * Starts Ukkonen's construction algorithm by calling SPA n times, where 
	 * n is the length of the source string.
	 * @param str the source string is a sequence of unsigned characters 
	 * (maximum of 256 different symbols). In the original algorithm '$' 
	 * was a special character. However,http://www.abc.net.au/news/ in Yona's 
	 * version it is appended at the end of the input string and then never used.
	 */
	public SuffixTree( byte[] str )
	{
	   int phase;
	   // added to make 1-character suffix trees work
	   this.e = 1;
	   this.length = str.length+1;
	   stError = length+10;
	   this.treeString = new byte[length+1];
	   for ( int i=0;i<str.length;i++ )
		   treeString[i+1] = str[i];
	   // the '$' is never examined but assumed to be there
	   treeString[length] = '$';
	   root = new Node( null, 0, 0, 0 );
	   root.suffixLink = null;
	   // initializing algorithm parameters
	   ExtensionParam ep = new ExtensionParam();
	   ep.extension = 2;
	   ep.repeatedExtension = 0;
	   phase = 2;
	   // allocating first node, son of the root (phase 0), the longest 
	   // path node
	   root.sons = new Node( root, 1, length, 1 );
	   suffixless = null;
	   Pos pos = new Pos( root, 0 );
	   // Ukkonen's algorithm begins here
	   for (; phase<length; phase++ )
	   {
	      // perform Single Phase Algorithm
	      SPA( pos, phase, ep );
	   }
	   verify( str );
	}
	/**
	 * Find the son of a node that starts with a certain character. 
	 * @param node the node to start searching from
	 * @param character the character to be searched for in the sons
	 * @return the son found, or null if no such son.
	*/
	Node findSon( Node node, byte character )
	{
	   // point to the first son.
	   node = node.sons;
	   // scan all sons (all right siblings of the first son) for their first
	   // character (it must match the char given as input to this function).
	   while ( node != null && treeString[node.edgeLabelStart] != character )
	   {
	      node = node.rightSibling;
	   }
	   return node;
	}
	/**
	 * Returns the end index of the incoming edge to that node. This function is 
	 * needed because for leaves the end index is not relevant, instead we must 
	 * look at the variable "e" (the global virtual end of all leaves). Never 
	 * refer directly to a leaf's end-index.
	 * @param node the node whose end index we require
	 * @return the end index of that node (meaning the end index of the node's 
	 * incoming edge).
	 */
	private int getNodeLabelEnd( Node node )
	{
	   // if it's a leaf - return e
	   if ( node.sons == null )
	      return e;
	   // if it's not a leaf - return its real end
	   return node.edgeLabelEnd;
	}
	/**
	 * Returns the length of the incoming edge to that node. Uses getNodeLabelEnd 
	 * (see below).
	 * @param node the node whose length we need
	 * @return the length of that node.
	 */
	int getNodeLabelLength( Node node )
	{
	   // calculate and return the length of the node
	   return getNodeLabelEnd(node) - node.edgeLabelStart + 1;
	}
	/** 
	 * Is edgePos the last position in node's incoming edge?
	 * @param node the node to be checked 
	 * @param edgePos the position in its incoming edge.
	 * @return true if edgePos is the last position in node's incoming edge
	 */
	boolean isLastCharInEdge( Node node, int edgePos )
	{
	   return edgePos == getNodeLabelLength(node)-1;
	}
	/**
	 * Connect rightSib as the right sibling of leftSib and vice versa.
	 * @param leftSib one of two nodes to be connected
	 * @param rightSib one of two nodes to be connected
	 */
	void connectSiblings( Node leftSib, Node rightSib )
	{
	   // connect the right node as the right sibling of the left node
	   if ( leftSib != null )
	      leftSib.rightSibling = rightSib;
	   // connect the left node as the left sibling of the right node 
	   if ( rightSib != null )
	      rightSib.leftSibling = leftSib;
	}
	/**
	 * Apply "extension rule 2" in 2 cases:
	 * 1. A new son (leaf 4) is added to a node that already has sons:
	 *              (1)	           (1)
	 *             /   \	 ->   / | \
	 *            (2)  (3)      (2)(3)(4)
	 * 2. An edge is split and a new leaf (2) and an internal node (3) are added:
	 *            | 	  |
	 *            | 	 (3)
	 *            |     ->   / \
	 *           (1)       (1) (2)
	 * @param node node 1 (see drawings)
	 * @param edgeLabelBegin start index of node 2's incoming edge
	 * @param edgeLabelEnd end index of node 2's incoming edge
	 * @param pathPos path start index of node 2
	 * @param edgePos position in node 1's incoming edge where split is to be 
	 * performed
	 * @return a newly created leaf (newSon case) or internal node (split case).
	 */
	Node applyExtensionRule2( Node node, int edgeLabelBegin, int edgeLabelEnd, 
			int pathPos, int edgePos, Rule2Type type )
	{
	   Node newLeaf,newInternal,son;
	   // newSon
	   if ( type == Rule2Type.newSon )                                       
	   {
         if (LOG.isTraceEnabled()) {
           LOG.trace("rule 2: new leaf ({},{})", edgeLabelBegin, edgeLabelEnd );
         }
	      // create a new leaf (4) with the characters of the extension
	      newLeaf = new Node( node, edgeLabelBegin, edgeLabelEnd, pathPos );
	      // connect newLeaf (4) as the new son of node (1) 
	      son = node.sons;
	      while ( son.rightSibling != null )
	         son = son.rightSibling;
	      connectSiblings(son, newLeaf);
	      // return (4)
	      return newLeaf;
	   }
	   // split
      if (LOG.isTraceEnabled()) {
        LOG.trace( "rule 2: split ({}, {})", edgeLabelBegin, edgeLabelEnd);
      }
	   // create a new internal node (3) at the split point
	   newInternal = new Node( node.father, node.edgeLabelStart,
	       node.edgeLabelStart+edgePos, node.pathPosition );
	   // update the node (1) incoming edge starting index (it now starts 
	   // where node (3) incoming edge ends)
	   node.edgeLabelStart += edgePos+1;
	   // create a new leaf (2) with the characters of the extension 
	   newLeaf = new Node( newInternal, edgeLabelBegin, edgeLabelEnd, pathPos );
	   // connect newInternal (3) where node (1) was 
	   // connect (3) with (1)'s left sibling 
	   connectSiblings( node.leftSibling, newInternal );   
	   // connect (3) with (1)'s right sibling
	   connectSiblings( newInternal, node.rightSibling );
	   node.leftSibling = null;
	   // connect (3) with (1)'s father 
	   if ( newInternal.father.sons == node )            
	      newInternal.father.sons = newInternal;
	   // connect newLeaf (2) and node (1) as sons of newInternal (3)
	   newInternal.sons = node;
	   node.father = newInternal;
	   connectSiblings( node, newLeaf );
	   // return (3) 
	   return newInternal;
	}
	/**
	 * Traces for a string in a given node's OUTcoming edge. It searches 
	 * only in the given edge and not other ones. Search stops when either 
	 * whole string was found in the given edge, a part of the string was 
	 * found but the edge ended (and the next edge must be searched too 
	 * - performed by function traceString) or one non-matching character 
	 * was found.
	 * @param node node to start from
	 * @param str string to trace
	 * @param type skip or noSkip
	 * @return the node where tracing has stopped
	 */
	private Node traceSingleEdge( Node node, Path str, TraceReturnValue trv, 
		SkipType type )   
	{
	   Node contNode;
	   int length,strLen;

	   // set default return values
	   trv.searchDone = true;
	   trv.edgePos    = 0;

	   // search for the first character of the string in the outgoing 
	   // edge of node 
	   contNode = findSon( node, treeString[str.begin] );
	   if ( contNode == null )
	   {
	      // Search is done, string not found
	      trv.edgePos = getNodeLabelLength( node )-1;
	      trv.charsFound = 0;
	      return node;
	   }
	   // found first character - prepare for continuing the search
	   node    = contNode;
	   length  = getNodeLabelLength( node );
	   strLen = str.end - str.begin + 1;

	   // compare edge length and string length. 
	   // if edge is shorter then the string being searched and skipping 
	   // is enabled - skip edge 
	   if ( type == SkipType.skip )
	   {
	      if ( length <= strLen )
	      {
	         trv.charsFound = length;
	         trv.edgePos = length-1;
	         if ( length < strLen )
	            trv.searchDone  = false;
	      }
	      else
	      {
	         trv.charsFound = strLen;
	         trv.edgePos = strLen-1;
	      }
	      return node;
	   }
	   else
	   {
	      // find minimum out of edge length and string length, and scan it
	      if ( strLen < length)
	         length = strLen;

	      for ( trv.edgePos=1, trv.charsFound=1; trv.edgePos<length; 
	      	trv.charsFound++,trv.edgePos++ )
	      {
	         // compare current characters of the string and the edge. 
	    	 // if equal - continue
	         if ( treeString[node.edgeLabelStart+trv.edgePos] != 
	        	 treeString[str.begin+trv.edgePos])
	         {
	            trv.edgePos--;
	            return node;
	         }
	      }
	   }
	   // the loop has advanced edgePos one too much 
	   trv.edgePos--;
	   if ( (trv.charsFound) < strLen )
	      // search is not done yet
	      trv.searchDone = false;
	   return node;
	}
	/**
	 * <p>Traces for a string in the tree. This function is used in 
	 * construction process only, and not for after-construction search 
	 * of substrings. It is tailored to enable skipping (when we know a 
	 * suffix is in the tree (when following a suffix link) we can avoid 
	 * comparing all symbols of the edge by skipping its length immediately 
	 * and thus save atomic operations - see Ukkonen's algorithm, skip 
	 * trick).</p>
	 * <p>This function, in contradiction to the function traceSingleEdge, 
	 * 'sees' the whole picture, meaning it searches a string in the whole 
	 * tree and not just in a specific edge.</p>
	 * @param node node to start from
	 * @param str string to trace
	 * @param trv return values to fill in
	 * @param type skip or noSkip
	 * @return the node where tracing has stopped
	 */
	private Node traceString( Node node, Path str, TraceReturnValue trv, 
		SkipType type )        
	{
		Path localStr = new Path();
		localStr.begin = str.begin;
		localStr.end = str.end;
		trv.charsFound = 0;
		TraceReturnValue localTrv = new TraceReturnValue();
		localTrv.searchDone = false;
		while ( localTrv.searchDone == false )
		{
			trv.edgePos = localTrv.edgePos = 0;
			localTrv.charsFound = 0;
			node = traceSingleEdge( node, localStr, localTrv, type );
			localStr.begin += localTrv.charsFound;
			trv.charsFound += localTrv.charsFound;
			trv.edgePos = localTrv.edgePos;
		}
		return node;
	}
	/**
	 * Get the Pos from the root that corresponds to the initial byte b
	 * @param b the first byte from the root whose Pos is desired
	 * @return the relevant Pos, null if not present
	 */
	public Pos getStartPos( byte b )
	{
		Node node = findSon( root, b );
		if ( node != null )
		{
			return new Pos( node, node.edgeLabelStart );
		}
		else
			return null;
	}
	/**
	 * Advance a Pos in the suffix tree by one byte if possible.
	 * On entry the pos is matched with the byte it points to. We 
	 * try to match the NEXT byte. If we succeed, we update pos. 
	 * Otherwise we do nothing to pos.
	 * @param b the byte to advance from pos
	 * @param pos the position in the tree where we were last time
	 * @return true if the advance was successful, false otherwise
	 */
	public boolean advance( Pos pos, byte b )
	{
		if ( pos.node == null )
		{
			pos.node = findSon( root, b );
			if ( pos.node != null )
			{
				pos.edgePos = pos.node.edgeLabelStart;
				return true;
			}
			else
				return false;
		}
		else
		{
			int nodeLabelEnd = getNodeLabelEnd( pos.node );
			// already matched that byte ...
			if ( pos.edgePos == nodeLabelEnd )
		    {
				Node localNode = findSon( pos.node, b );
		    	if ( localNode != null )
		    	{
		    		pos.edgePos = localNode.edgeLabelStart;
		    		pos.node = localNode;
		    		return true;
		    	}
		    	else
		    		return false;
		    }
			else 
			{
				boolean success = treeString[pos.edgePos+1] == b;
				if ( success )
					pos.edgePos++;
				return success;
		    }
		}
	}
	/**
	 * Find the length of a match starting from a pos that represents 
	 * the first mismatch AFTER some matching string.
	 * @param pos the end-pos of the string
	 * @return the length of the matching string
	 */
	int getMatchLength( Pos pos )
	{
		if ( pos.node == null )
			return 0;
		else
		{
			Node temp = pos.node;
			int length = pos.edgePos - temp.edgeLabelStart;
			temp = temp.father;
			while ( temp != root )
			{
				length += getNodeLabelLength( temp );
				temp = temp.father;
			}
			return length;
		}
	}
	/**
	 * Traces for a string in the tree. This function is used for 
	 * substring search after tree construction is done. It simply 
	 * traverses down the tree starting from the root until either 
	 * the searched string is fully found or one non-matching character 
	 * is found. In this function skipping is not enabled because we 
	 * don't know whether the string is in the tree or not (see function 
	 * traceString above).
	 * @param W the substring to find
	 * @return the index of the starting position of the substring in 
	 * the tree source string. If the substring is not found - returns 
	 * stError
	 */
	public int findSubstring( byte[] W )         
	{
		// starts with the root's son that has the first character of W 
		// as its incoming edge first character
		Node node = findSon( root, W[0] );
		int k,j = 0, nodeLabelEnd;

		// scan nodes down from the root until a leaf is reached or the 
		// substring is found 
		while ( node != null )
		{
		   k = node.edgeLabelStart;
		   nodeLabelEnd = getNodeLabelEnd( node );
	      // Scan a single edge - compare each character with the searched 
		  // string
	      while ( j < W.length && k <= nodeLabelEnd && treeString[k] 
	           == W[j] )
	      {
	         j++;
	         k++;
	      }
	      // checking which of the stopping conditions are true 
	      if ( j == W.length )
	      {
	    	  // W was found - it is a substring. Return its path starting 
	    	  // index
	    	  return node.pathPosition;
	      }
	      else if ( k > nodeLabelEnd )
	    	  // current edge is found to match, continue to next edge
	    	  node = findSon( node, W[j] );
	      else
	      {
	    	  // one non-matching symbols is found - W is not a substring
	    	  return stError;
	      }
	   }
	   return stError;
	}
	/**
	 * Follows the suffix link of the source node according to Ukkonen's 
	 * rules. 
	 * @param pos a combination of the source node and the position in 
	 * its incoming edge where suffix ends
	 * @return The destination node that represents the longest suffix 
	 * of node's path. Example: if node represents the path "abcde" then 
	 * it returns the node that represents "bcde"
 	 */
	Pos followSuffixLink( Pos pos )
	{
	   // gama is the string between node and its father, in case node 
	   // doesn't have a suffix link
	   Path gama = new Path();   
	   // dummy argument for trace_string function
	   int charsFound = 0;   
	   
	   if ( pos.node == root )
	      return pos;
	   // if node has no suffix link yet or in the middle of an edge - remember the 
	   // doesn't have edge between the node and its father (gama) and follow its 
	   // father's suffix link (it must have one by Ukkonen's lemma). After 
	   // following, trace down gama - it must exist in the tree (and thus can use 
	   // the skip trick - see traceString function description)
	   if ( pos.node.suffixLink == null || !isLastCharInEdge(pos.node,pos.edgePos) )
	   {
		   // if the node's father is the root, than no use following it's link (it 
		   // is linked to itself). Tracing from the root (like in the naive 
		   // algorithm) is required and is done by the calling function SEA upon 
		   // receiving a return value of tree->root from this function 
		   if ( pos.node.father == root )
		   {
	    	  pos.node = root;
	    	  return pos;
		   }
		   // store gama - the indices of node's incoming edge
		   gama.begin = pos.node.edgeLabelStart;
		   gama.end = pos.node.edgeLabelStart + pos.edgePos;
		   // follow father's suffix link
		   pos.node = pos.node.father.suffixLink;
		   // down-walk gama back to suffixLink's son
		   TraceReturnValue trv = new TraceReturnValue();
		   trv.edgePos = pos.edgePos;
		   trv.charsFound = charsFound;
		   pos.node = traceString( pos.node, gama, trv, SkipType.skip );
		   pos.edgePos = trv.edgePos;
	   }
	   else
	   {
		   // if a suffix link exists - just follow it
		   pos.node = pos.node.suffixLink;
		   pos.edgePos = getNodeLabelLength( pos.node )-1;
	   }
	   return pos;
	}
	/**
	 * Creates a suffix link between node and the node 'link' which represents its
	 * largest suffix. The function could be avoided but is needed to monitor the
	 * creation of suffix links when debugging or changing the tree.
	 * @param node the node to link from
	 * @param link the node to link to
	 */
	private void createSuffixLink( Node node, Node link )
	{
	   node.suffixLink = link;
	}
	/**
	 * Single-Extension-Algorithm (see Ukkonen's algorithm). Ensure that a certain 
	 * extension is in the tree.
	 * <ol><li>Follows the current node's suffix link.</li>
	 * <li>Check whether the rest of the extension is in the tree.</li>
	 * <li>If it is - reports the calling function SPA of rule 3 (= current phase is
	 * done).</li>
	 * <li>If it's not - inserts it by applying rule 2.</li></ol>
	 * @param pos the node and position in its incoming edge where extension begins
	 * @param str the starting and ending indices of the extension
	 * @param afterRule3 a flag indicating whether the last phase ended by rule 3 
	 * (last extension of the last phase already existed in the tree - and 
	 * if so, the current phase starts at not following the suffix link of 
	 * the first extension) 
	 * @param ruleApplied last rule applied
	 * @return The rule that was applied to that extension. Can be 3 (phase is done) 
	 * or 2 (a new leaf was created).
	 */
	int SEA( Pos pos, Path str, char afterRule3, int ruleApplied )
	{
		int charsFound = 0, pathPos = str.begin;
		Path origStr = new Path();
		origStr.begin = str.begin;
		origStr.end = str.end;
		Node tmp;

      if (LOG.isTraceEnabled()) {
        LOG.trace("\n{}extension: {} phase+1: {} -- {} ({},{} | {})", new Object[] {
                printTree(),
                str.begin, str.end,
                (afterRule3 == 0 ? "followed from" : "starting at"),
                pos.node.edgeLabelStart, getNodeLabelEnd(pos.node), pos.edgePos
        });
      }
	   // follow suffix link only if it's not the first extension after rule 3 was applied
	   if ( afterRule3 == 0 )
	      followSuffixLink( pos );
	   // if node is root - trace whole string starting from the root, else - 
	   // trace last character only 
	   if (pos.node == root )
	   {
		   TraceReturnValue trv = new TraceReturnValue();
		   trv.edgePos = pos.edgePos;
		   trv.charsFound = charsFound;
		   pos.node = traceString( root, str, trv, SkipType.noSkip );
		   pos.edgePos = trv.edgePos;
		   charsFound = trv.charsFound;
	   }
	   else
	   {
		   str.begin = str.end;
		   charsFound = 0;

	      // consider 2 cases:
	      // 1. last character matched is the last of its edge 
	      if ( isLastCharInEdge( pos.node, pos.edgePos) )
	      {
	         // trace only last symbol of str, search in the  NEXT edge (node)
	         tmp = findSon( pos.node, treeString[str.end] );
	         if ( tmp != null )
	         {
	        	 pos.node = tmp;
	        	 pos.edgePos = 0;
	        	 charsFound = 1;
	         }
	      }
	      // 2. last character matched is NOT the last of its edge
	      else
	      {
	         // Trace only last symbol of str, search in the CURRENT edge (node)
	         if ( treeString[pos.node.edgeLabelStart+pos.edgePos+1] == treeString[str.end] )
	         {
	            pos.edgePos++;
	            charsFound = 1;
	         }
	      }
	   }
	   // if whole string was found - rule 3 applies 
	   if ( charsFound == str.end - str.begin + 1 )
	   {
	      ruleApplied = 3;
	      // if there is an internal node that has no suffix link yet (only one may 
	      // exist) - create a suffix link from it to the father-node of the 
	      // current position in the tree (pos)
	      if ( suffixless != null )
	      {
	         createSuffixLink( suffixless, pos.node.father );
	         // marks that no internal node with no suffix link exists 
	         suffixless = null;
	      }

         if (LOG.isTraceEnabled()) {
           LOG.trace("rule 3 ({},{})", str.begin, str.end);
         }
	      return ruleApplied;
	   }
	   
	   // if last char found is the last char of an edge - add a character at the 
	   // next edge 
	   if ( isLastCharInEdge(pos.node,pos.edgePos) || pos.node == root)
	   {
	      // decide whether to apply rule 2 (newSon) or rule 1 
	      if ( pos.node.sons != null )
	      {
	         // apply extension rule 2 new son - a new leaf is created and returned 
	         // by applyExtensionRule2 
	         applyExtensionRule2( pos.node, str.begin+charsFound, str.end, pathPos, 0, 
	        		Rule2Type.newSon );
	         ruleApplied = 2;
	         // if there is an internal node that has no suffix link yet (only one 
	         // may exist) - create a suffix link from it to the father-node of the 
	         // current position in the tree (pos)
	         if ( suffixless != null )
	         {
	            createSuffixLink( suffixless, pos.node );
	            // Marks that no internal node with no suffix link exists
	            suffixless = null;
	         }
	      }
	   }
	   else
	   {
	      // apply extension rule 2 split - a new node is created and returned by 
	      // applyExtensionRule2
	      tmp = applyExtensionRule2( pos.node, str.begin+charsFound, str.end, pathPos, 
	    		 pos.edgePos, Rule2Type.split);
	      if ( suffixless != null )
	    	  createSuffixLink( suffixless, tmp );
	      // link root's sons with a single character to the root
	      if ( getNodeLabelLength(tmp) == 1 && tmp.father == root)
	      {
	         tmp.suffixLink = root;
	         // marks that no internal node with no suffix link exists
	         suffixless = null;
	      }
	      else
	         // mark tmp as waiting for a link
	         suffixless = tmp;
	      
	      // prepare pos for the next extension
	      pos.node = tmp;
	      ruleApplied = 2;
	   }
	   return ruleApplied;
	}
	/**
	 * Performs all insertion of a single phase by calling function SEA starting 
	 * from the first extension that does not already exist in the tree and ending 
	 * at the first extension that already exists in the tree. 
	 * @param pos the node and position in its incoming edge where extension begins
	 * @param phase current phase number - offset into text
	 * @param ep the first extension number of that phase, a flag signaling whether 
	 * the extension is the first of this phase, after the last phase ended with 
	 * rule 3. If so - extension will be executed again in this phase, and thus 
	 * its suffix link would not be followed. Updated: The extension number that 
	 * was last executed on this phase. Next phase will start from it and not from 1
	 */
	void SPA( Pos pos, int phase, ExtensionParam ep )
	{
	   // no such rule (0). Used for entering the loop
	   int ruleApplied = 0;   
	   Path str = new Path();
	   // leafs Trick: apply implicit extensions 1 through prevPhase 
	   e = phase+1;
	   // apply explicit extensions until last extension of this phase is reached 
	   // or extension rule 3 is applied once 
	   while ( ep.extension <= phase+1 )            
	   {
		   str.begin = ep.extension;
		   str.end = phase+1;
		   // Call Single-Extension-Algorithm
		   ruleApplied = SEA( pos, str, ep.repeatedExtension, ruleApplied );
	      
	      // check if rule 3 was applied for the current extension 
	      if ( ruleApplied == 3 )
	      {
	         // Signaling that the next phase's first extension will not follow a 
	         // suffix link because same extension is repeated 
	         ep.repeatedExtension = 1;
	         break;
	      }
	      ep.repeatedExtension = 0;
	      ep.extension++;
	   }
	}
	/**
	 * This function prints the tree. It simply starts the recursive function 
	 * printNode with depth 0 (the root).
	 */
	String printTree()
	{
      final StringBuilder tree = new StringBuilder("\nroot\n");
      printNode( root, 0, tree );
      return tree.toString();
	}
	/**
	 * Deletes a subtree that is under node. It recursively calls itself for all of 
	 * node's right sons and then deletes node.
	 * @param node the node that is the root of the subtree to be deleted.
	 */
	void deleteSubTree( Node node )
	{
	   // recursion stopping condition
	   if ( node != null )
	   {
		   // recursive call for right sibling
		   if ( node.rightSibling != null )
		      deleteSubTree( node.rightSibling );
		   // recursive call for first son
		   if ( node.sons != null )
		      deleteSubTree( node.sons );
		   // encourage garbage collection
		   node = null;
	   }
	}
	/**
	 * Deletes a whole suffix tree by starting a recursive call to deleteSubTree
	 * from the root. After all of the nodes have been deleted, the function deletes 
	 * the structure that represents the tree.
	 */
	void deleteTree()
	{
	   deleteSubTree( root );
	}
	/**
	 * Prints a subtree under a node of a certain tree-depth.
	 * @param node1 the node that is the root of the subtree
	 * @param depth the depth of that node. This is used for printing the branches 
	 * that are coming from higher nodes and only then the node itself is printed.
	 * This gives the effect of a tree on screen. In each recursive call, the depth 
	 * is increased.
	 */
	void printNode( Node node1, int depth, StringBuilder tree )
	{
	   Node node2 = node1.sons;
	   int d = depth;
	   int start = node1.edgeLabelStart;
	   int end;
	   end = getNodeLabelEnd( node1 );
	   if ( depth > 0 )
	   {
	      // print the branches coming from higher nodes
	      while ( d > 1 )
	      {
            tree.append("|");
	         d--;
	      }
         tree.append("+");
	      // print the node itself
	      while ( start <= end )
	      {
            tree.append((char)treeString[start]);
	         start++;
	      }
         tree.append(" (").append(node1.edgeLabelStart).append(",").append(end).append(" | ").append(node1.pathPosition).append(")\n");
	   }
	   // recursive call for all node1's sons
	   while ( node2 != null )
	   {
	      printNode( node2, depth+1, tree );
	      node2 = node2.rightSibling;
	   }
	}
	/**
	 * This function prints the full path of a node, starting from the root. It 
	 * calls itself recursively and than prints the last edge.
	 * @param node the node to be printed
	 */
	void printFullNode( Node node )
	{
	   int start, end;
	   if ( node != null )
	   {
		   // calculating the beginning and ending of the last edge
		   start = node.edgeLabelStart;
		   end = getNodeLabelEnd( node );
		   
		   // stopping condition - the root
		   if ( node.father != root )
		      printFullNode( node.father );
		   // print the last edge
		   while ( start <= end )
		   {
		      System.out.print(treeString[start]);
		      start++;
		   }
	   }
	}
	/** verify that all the words stored in str are findable */
	void verify( byte[] str )
	{
		int start=-1;
		for ( int i=0;i<str.length;i++ )
		{
			if ( str[i] != 0 && start == -1 )
				start = i;
			else if ( str[i] == 0 && start != -1 )
			{
				reportWord( str, start, i );
				start = -1;
			}
		}
		if ( start != -1 )
			reportWord( str, start, str.length );
	}
	/**
	 * Report a word as found or not
	 * @param str the byte array the words are coming from
	 * @param start the start offset of the word to report-
	 * @param end an index one beyond the end of the word
	 */
	void reportWord( byte[] str, int start, int end )
	{
		byte[] word = new byte[end-start];
		for ( int j=start,i=0;i<word.length;i++,j++ )
			word[i] = str[j];
		String temp = new String(word);
		if ( findSubstring(word) == stError )
		//	System.out.println("Found word "+temp);
		//else
			System.out.println("Couldn't find word "+temp );
	}
}
