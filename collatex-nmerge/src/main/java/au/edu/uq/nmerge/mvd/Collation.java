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
package au.edu.uq.nmerge.mvd;

import au.edu.uq.nmerge.exception.MVDException;
import au.edu.uq.nmerge.graph.*;
import au.edu.uq.nmerge.graph.suffixtree.SuffixTree;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import java.util.*;

/**
 * Represent a multi-version document.
 * @author Desmond Schmidt &copy; 2009
 */
public class Collation
{
  public static String UNTITLED_NAME = "untitled";

  Mask mask;
  // new options
	boolean directAlignOnly;
	Vector<Witness> witnesses;	// id = position in table+1
	Vector<Match> matches;
	String description;
	int headerSize,groupTableSize,versionTableSize,pairsTableSize,
	dataTableSize,versionSetSize;
	int bestScore;
	// used for checking
	HashSet<Match> parents;
	BitSet partialVersions;
	String encoding;

	public Collation()
	{
      this.description = "";
      this.witnesses = new Vector<Witness>();
      this.matches = new Vector<Match>();
      this.mask = Mask.NONE;
      this.encoding = "UTF-8";
	}
	public Collation(String description)
	{
		this();
		this.description = description;
	}
	/**
	 * Create an empty MVD - add versions and data later
	 * @param description about this MVD
	 * @param encoding the encoding of the data in this MVD
	 */
	public Collation(String description, String encoding)
	{
		this();
		this.description = description;
		this.encoding = encoding;
	}

	/**
	 * Set the encoding, which defaults to UTF-8
	 * @param encoding the new encoding
	 */
	public void setEncoding( String encoding )
	{
		this.encoding = encoding;
	}
	/**
	 * Set the version set size for all version sets
	 * @param setSize the size of the version set in bytes
	 */
	void setVersionSetSize( int setSize )
	{
		versionSetSize = setSize;
	}
	/**
	 * Get the description defined for this MVD
	 * @return the description as a String
	 */
	public String getDescription()
	{
		return description;
	}
	/**
	 * Get the encoding of the data in this MVD
	 * @return the encoding as a String
	 */
	public String getEncoding()
	{
		return encoding;
	}
	/** 
	 * Add a version after we've built the MVD. 
	 * @param version the version to add
	 * @throws MVDException
	 */
	public void addVersion( int version ) throws MVDException
	{
		if ( version == witnesses.size()+1 )
		{
			Witness v = new Witness(  "Z", UNTITLED_NAME );
			witnesses.insertElementAt( v, version-1 );
		}
		else
			throw new MVDException("Invalid version "+version+" ignored");
	}

	/**
	 * Get the number of versions
	 * @return the number of elements in the versions array
	 */
	public int numVersions()
	{
		return witnesses.size();
	}


	/**
	 * Get the data mask
	 * @return the kind of mask being applied to all data in the MVD
	 */
	public Mask getMask()
	{
		return mask;
	}
	/**
	 * Get the pairs list for converting to a Graph
	 * @return the pairs - read only!
	 */
	public Vector<Match> getMatches()
	{
		return matches;
	}
	/**
	 * Get a pair from the MVD
	 * @param pairIndex the index of the pair
	 */
	Match getPair( int pairIndex ) throws Exception
	{
		return matches.get( pairIndex );
	}

	/**
	 * Compare two versions u and v. If it is in u but not in v 
	 * then turn that pair and any subsequent pairs with the 
	 * same characteristic into a match. We also generate merged 
	 * Matches for the gaps between the state matches (added or 
	 * deleted). This way we can link up the merged matches in 
	 * the GUI.
	 * @param u the first version to compare
	 * @param v the second version to compare
	 * @param state the state of text belonging only to u
	 * @return an array of chunks for special display
	 */
	public Chunk[] compare( short u, short v, ChunkState state ) 
		throws MVDException
	{
		Vector<Chunk> chunks = new Vector<Chunk>();
		Chunk current = new Chunk( encoding );
		current.setVersion( u );
		TransposeState oldTS = null;
		TransposeState ts = new TransposeState();
		ChunkStateSet cs = new ChunkStateSet();
		ChunkStateSet oldCS = null;
		Match p = null;
		Chunk.chunkId = 0;
		TransposeState.transposeId = Integer.MAX_VALUE;
		int i = next( 0, u );
		while ( i < matches.size() )
		{
			p = matches.get( i );
			oldTS = ts;
			oldCS = cs;
			ts = ts.next( p, u, v );
			// transposed is not deleted, inserted or merged
			if ( !ts.isTransposed() )
				cs = cs.next( p, state, v );
			if ( ts != oldTS || cs != oldCS )
			{
				// then we have to write out current
				ChunkStateSet cs1 = current.getStates();
				if ( current.getLength()>0 )
				{
					if ( cs1.isMerged() )
						current.setId( ++Chunk.chunkId );
					chunks.add( current );
				}
				// set up a new current chunk
				ChunkState[] newStates;
				if ( ts.getId() != 0 )
				{
					newStates = new ChunkState[1];
					newStates[0] = ts.getChunkState();
				}
				else
					newStates = cs.getStates();
				current = new Chunk( encoding, ts.getId(), newStates, p.getData());
				current.setVersion( u );
			}
			else
				current.addData( p.getData() );
			if ( i < matches.size()-1 )
				i = next( i+1, u );
			else
				break;
		}
		// add any lingering chunks
		if ( current.getStates().isMerged() )
			current.setId( ++Chunk.chunkId );
		if ( chunks.size()==0 || current != chunks.get(chunks.size()-1) )
			chunks.add( current );
		Chunk[] result = new Chunk[chunks.size()];
		chunks.toArray( result );
		return result;
	}

	/**
	 * Get the index of the next pair intersecting with a version
	 * @param pairIndex the index to start looking from
	 * @param u the version to look for
	 * @return the index of the next pair or Integer.MAX_VALUE if not found
	 */
	int next( int pairIndex, short u )
	{
		int i=pairIndex;
		while ( i < matches.size() )
		{
			Match p = matches.get( i );
			if ( p.contains(u) )
				return i;
			else
				i++;
		}
		return Integer.MAX_VALUE;
	}
	/**
	 * Get the index of the previous pair intersecting with a 
	 * version
	 * @param pairIndex the index to start looking from
	 * @param u the version to look for
	 * @return the index of the previous pair or -1 if not found
	 */
	int previous( int pairIndex, short u )
	{
		int i=pairIndex-1;
		while ( i > 0 )
		{
			Match p = matches.get( i );
			if ( p.contains(u) )
				return i;
			else
				i--;
		}
		return -1;
	}
	/**
	 * Search for a pattern. Return multiple matches if requested 
	 * as an array of Match objects
	 * @param pattern the pattern to search for
	 * @param bs the set of versions to search through
	 * @param multiple if true return all hits; otherwise only the first 
	 * @return an array of matches
	 */
	public Hit[] search( byte[] pattern, BitSet bs, boolean multiple )
		throws Exception
	{
		KMPSearchState inactive = null;
		KMPSearchState active = null;
		Hit[] hits = new Hit[0];
		if ( !witnesses.isEmpty() )
		{
			inactive = new KMPSearchState( pattern, bs );
			for ( int i=0;i< matches.size();i++ )
			{
				Match temp = matches.get( i );
				// move all elements from active to inactive
				if ( inactive == null )
					inactive = active;
				else
					inactive.append( active );
				active = null;
				// move matching SearchStates into active
				KMPSearchState s = inactive;
				while ( s != null )
				{
					KMPSearchState sequential = s.following;
					if ( s.v.intersects(temp.versions) )
					{
						KMPSearchState child = s.split(temp.versions);
						if ( active == null )
							active = child;
						else
							active.append( child );
						if ( s.v.isEmpty() )
							inactive = inactive.remove( s );
					}
					s = sequential;
				}
				// now process each byte of the pair
				if ( active != null )
				{
					byte[] data = temp.getData();
					for ( int j=0;j<data.length;j++ )
					{
						KMPSearchState ss = active;
						while ( ss != null )
						{
							if ( ss.update(data[j]) )
							{
								Hit[] m = Hit.createHits(
                                        pattern.length, ss.v,
                                        this, i, j, multiple, ChunkState.FOUND);
								if ( hits == null )
									hits = m;
								else
									hits = Hit.merge(hits, m);
								if ( !multiple )
									break;
							}
							ss = ss.following;
						}
						// now prune the active list
						KMPSearchState s1 = active;
						if ( s1.next != null )
						{
							while ( s1 != null )
							{
								KMPSearchState s2 = s1.following;
								while ( s2 != null )
								{
									KMPSearchState sequential = s2.following;
									if ( s1.equals(s2) )
									{
										s1.merge( s2 );
										active.remove( s2 );
									}
									s2 = sequential;
								}
								s1 = s1.following;
							}
						}
					}
				}
			}
		}
		return hits;
	}
	/**
	 * Create a new empty version.
	 * @return the id of the new version
	 */
	public int newVersion( String shortName, String longName)
	{
		witnesses.add( new Witness(shortName, longName) );
		return witnesses.size();
	}

	/**
	 * Get the current index of the given version + 1
	 * @param v the version to search for
	 * @return the index +1 of the version in the versions vector or 0
	 */
	public int getVersionId( Witness v )
	{
		return witnesses.indexOf(v) + 1;
	}
	/**
	 * Get the id of a version (version index+1) given its short name
	 * @param shortName the shortName
	 * @return -1 if not found or the correct id
	 */
	public int getVersionId( String shortName )
	{
		for ( int i=0;i< witnesses.size();i++ )
			if ( witnesses.get(i).shortName.equals(shortName) )
				return i + 1;
		return -1;
	}
	/**
	 * Change the description of this MVD
	 * @param description
	 */
	public void setDescription( String description )
	{
		this.description = description;
	}

	/**
	 * Set the short name of a given version
	 * @param versionId the id of the affected version
	 * @param shortName the new short name
	 */
	public void setVersionShortName( int versionId, String shortName )
	{
		Witness v = witnesses.get( versionId-1 );
		v.shortName = shortName;
	}
	/**
	 * Set the long name of a given version
	 * @param versionId the id of the affected version
	 * @param longName the new long name
	 */
	public void setVersionLongName( int versionId, String longName )
	{
		Witness v = witnesses.get( versionId-1 );
		v.longName = longName;
	}

	/**
	 * Set the data mask
	 * @param mask the kind of mask to apply to all data in the MVD
	 */
	public void setMask( Mask mask )
	{
		this.mask = mask;
	}

	/**
	 * Update an existing version or add a new one.
	 * @param version the id of the version to add. 
	 * @param data the data to merge
	 * @return percentage of the new version that was unique, or 0 
	 * if this was the first version
	 */
	public float update( short version, byte[] data ) throws Exception
	{
		// to do: if version already exists, remove it first
		Converter con = new Converter();
		VariantGraph original = con.create(matches, witnesses.size() );
		original.removeVersion( version );
		VariantGraph g = original;
		VariantGraphSpecialArc special;
		if ( mask != Mask.NONE )
		{
			byte[] byteMask = XMLMasker.getMask(data, mask==Mask.XML);
			special = g.addSpecialArc( data, byteMask, version, 0 );
		}
		else
			special = g.addSpecialArc( data, version, 0 );
		if ( g.getStart().cardinality() > 1 )
		{
			SuffixTree<Byte> st = makeSuffixTree( special );
			MaximalUniqueMatch bestMUM = MaximalUniqueMatch.findDirectMUM(special, st, g);
			TreeMap<VariantGraphSpecialArc,VariantGraph> specials =
				new TreeMap<VariantGraphSpecialArc,VariantGraph>();
			while ( bestMUM != null )
			{
				if ( bestMUM.verify() )
				{
					bestMUM.merge();
					SimpleQueue<VariantGraphSpecialArc> leftSpecials =
						bestMUM.getLeftSpecialArcs();
					SimpleQueue<VariantGraphSpecialArc> rightSpecials =
						bestMUM.getRightSpecialArcs();
					while ( leftSpecials != null && !leftSpecials.isEmpty() )
						installSpecial( specials, leftSpecials.poll(), 
							bestMUM.getLeftSubgraph(), true );
					while ( rightSpecials != null && !rightSpecials.isEmpty() )
						installSpecial( specials, rightSpecials.poll(), 
							bestMUM.getRightSubgraph(), false );
				}
				else // try again
				{
					bestMUM = recomputeMUM( bestMUM );
					if ( bestMUM != null )
						specials.put( bestMUM.getArc(), bestMUM.getGraph() );
				}
				/*// debug
				Set<SpecialArc> keys = specials.keySet();
				Iterator<SpecialArc> iter = keys.iterator();
				while ( iter.hasNext() )
				{
					SpecialArc s = iter.next();
					if ( s.getBest().isTransposition() )
						System.out.print("Transposed: ");
					System.out.println( s.getBest().getMatch() );
				}*/
				// POP topmost entry, if possible
				bestMUM = null;
				if ( specials.size() > 0 )
				{
					VariantGraphSpecialArc key = specials.firstKey();
					//assert key.from != null && key.to != null;
					//System.out.println(key.toString());
					if ( key != null )
					{
						g = specials.remove( key );
						bestMUM = key.getBest();
						assert !specials.containsKey(key);
					}
				}
			}
		}
		original.adopt( version );
		matches = con.serialise();
		if ( encoding.toUpperCase().equals("UTF-8") )
			removeUTF8Splits();
		if ( numVersions()==1 )
			return 0.0f;
		else
			return getPercentUnique( version );
	}
	/**
	 * Remove split multi-byte UTF-8 characters. A UTF-8 character split 
	 * between two pairs will give rise to invalid characters if the mvd 
	 * is written out. Correct this by moving any orphaned character-
	 * sequence starts to the start of the following pair. We don't change
	 * the graph structure just its content a little bit.
	 */
	private void removeUTF8Splits()
	{
		for ( int i=0;i< matches.size();i++ )
		{
			Match p = matches.get(i);
			if ( p.dataSize() > 0 )
			{
				byte[] data = matches.get(i).getData();
				int len;
				/*
				 * A split utf-8 character's length varies according to 
				 * the bit combination at the start. Since we only take 
				 * incomplete characters over to the next pair we 
				 * need only examine bytes starting a sequence that cannot  
				 * finish before the end of the pair's data.
				 */
				if ( /*data.length>0&&*/(data[data.length-1]&0xE0)==0xC0 )
					len = 1;
				else if ( data.length>1 && (data[data.length-2]&0xF0)==0xE0 )
					len = 2;
				else if ( data.length>2 && (data[data.length-3]&0xF8)==0xF0 )
					len = 3;
				else
					len = 0;
				int j = data.length-len;
				if ( len > 0 && i < matches.size()-1 )
				{
					// preserve the data that stays put
					byte[] newData = new byte[data.length-len];
					byte[] prefix = new byte[len];
					for ( int k=0;k<j;k++ )
						newData[k] = data[k];
					// extract the portion to move
					for ( int k=0;k<len;k++ )
						prefix[k] = data[j+k];
					// find all the subsequent pairs 
					// that share a version with this pair
					// and add the prefix onto them
					BitSet seekSet = new BitSet();
					seekSet.or( p.versions );
					int m = i+1;
					while ( !seekSet.isEmpty() && m < matches.size() )
					{
						Match q = matches.get( m );
						if ( !q.isHint() && q.versions.intersects(seekSet) )
						{
							// This relies on the closing bytes of all 
							// preceding arcs ending in the same way.
							// We don't update children because they
							// don't own any data. Their parents will 
							// get updated in exactly the right way.
							if ( !q.isChild() )
							{
								byte[] qData = q.getData();
								byte[] newQData = new byte[qData.length+len];
								for ( int k=0;k<len;k++ )
									newQData[k] = prefix[k];
								for ( int k=0;k<qData.length;k++ )
									newQData[k+len] = qData[k];
								q.setData( newQData );
							}
							seekSet.andNot( q.versions );
						}
						m++;
					}
					if ( !p.isChild() )
						p.setData( newData );
				}
			}
		}
	}
	/**
	 * Get the percentage of the given version that is unique
	 * @param version the version to compute uniqueness for
	 * @return the percent as a float
	 */
	public float getUniquePercentage( short version )
	{
		int totalLen = 0;
		int uniqueLen = 0;
		if ( numVersions()==1 )
			return 0.0f;
		else
		{
			for ( int i=0;i< matches.size();i++ )
			{
				Match p = matches.get( i );
				if ( p.versions.nextSetBit(version)==version )
				{
					if ( p.versions.size()==1 )
						uniqueLen+= p.length();
					totalLen += p.length();
				}
			}
			return (float)uniqueLen/(float)totalLen;
		}
	}
	/**
	 * Get the percentage of the given version that is unique
	 * @param version the version to test
	 * @return float fraction of version that is unique
	 */
	private float getPercentUnique( short version )
	{
		float unique=0.0f,shared=0.0f;
		for ( int i=0;i< matches.size();i++ )
		{
			Match p = matches.get( i );
			if ( p.versions.nextSetBit(version)==version )
			{
				if ( p.versions.size()==1 )
					unique += p.length();
				else
					shared += p.length();
			}
		}
		return unique/shared;
	}
	/**
	 * The MUM is invalid. We have to find a valid one.
	 * @param old the old invalid MUM
	 * @return a new valid MUM or null
	 */
	MaximalUniqueMatch recomputeMUM( MaximalUniqueMatch old ) throws MVDException
	{
		VariantGraph g = old.getGraph();
		VariantGraphSpecialArc special = old.getArc();
		return computeBestMUM( g, special );
	}
	/**
	 * Compute the best MUM
	 * @param g a graph
	 * @param special a special arc aligned with g
	 * @return the new MUM or null
	 * @throws MVDException
	 */
	private MaximalUniqueMatch computeBestMUM( VariantGraph g, VariantGraphSpecialArc special )
		throws MVDException
	{
		SuffixTree<Byte> st = makeSuffixTree(special);
		MaximalUniqueMatch directMUM = MaximalUniqueMatch.findDirectMUM(special, st, g);
		MaximalUniqueMatch best = directMUM;
		if ( !directAlignOnly )
		{
			MaximalUniqueMatch leftTransposeMUM = MaximalUniqueMatch.findLeftTransposeMUM(
                    special, st, g);
			MaximalUniqueMatch rightTransposeMUM = MaximalUniqueMatch.findRightTransposeMUM(
                    special, st, g);
			best = getBest( directMUM, leftTransposeMUM, 
				rightTransposeMUM );
		}
		if ( best != null )
			special.setBest( best );
		return best;
	}
	/**
	 * Create a new suffix tree based on the data in the special arc. 
	 * Mask out bytes that are not to be considered.
	 * @param special the special arc
	 * @return the suffix tree
	 * @throws MVDException
	 */
	private SuffixTree<Byte> makeSuffixTree( VariantGraphSpecialArc special )
		throws MVDException
	{
      List<Byte> treeSource = Lists.newArrayListWithExpectedSize(special.getData().length);
      byte[] data = (special.hasMask() ? XMLMasker.maskOut(special.getData(), special.getMask() ) : special.getData());
      for (byte b : data) {
        treeSource.add(b);
      }

	  return new SuffixTree<Byte>( treeSource, Ordering.<Byte>natural(), (byte) '$');
	}
	/**
	 * Install a subarc into specials
	 * @param specials the specials TreeMap (red-black tree)
	 * @param special the special subarc to add
	 * @param subGraph the directly opposite subgraph
	 * @param left true if we are doing the left subarc, otherwise the 
	 * right
	 */
	private void installSpecial( TreeMap<VariantGraphSpecialArc,VariantGraph> specials,
		VariantGraphSpecialArc special, VariantGraph subGraph, boolean left ) throws MVDException
	{
		assert special.getFrom() != null && special.to != null;
		// this is necessary BEFORE you recalculate the MUM
		// because it will invalidate the special's location 
		// in the treemap and make it unfindable
		if ( specials.containsKey(special) )
			specials.remove( special );
		MaximalUniqueMatch best = computeBestMUM( subGraph, special );
		if ( best != null )
			specials.put( special, subGraph );
	}
	/**
	 * Find the better of three MUMs or null if they are all null.
	 * @param direct a direct align MUM possibly null
	 * @param leftTransposed the left transpose MUM possibly null
	 * @param rightTransposed the right transpose MUM possibly null
	 * @return null or the best MUM
	 */
	private MaximalUniqueMatch getBest( MaximalUniqueMatch direct, MaximalUniqueMatch leftTransposed,
		MaximalUniqueMatch rightTransposed )
	{
		MaximalUniqueMatch best = null;
		// decide which transpose MUM to use
		MaximalUniqueMatch transposed;
		if ( leftTransposed == null )
			transposed = rightTransposed;
		else if ( rightTransposed == null )
			transposed = leftTransposed;
		else if ( leftTransposed.compareTo(rightTransposed) > 0 )
			transposed = leftTransposed;
		else
			transposed = rightTransposed;
		// decide between direct and transpose MUM
		if ( direct != null && transposed != null )
		{
			int result = direct.compareTo( transposed );
			// remember, we nobbled the compareTo method
			// to produce reverse ordering in the specials
			// treemap, so "less than" is actually longer
			if ( result == 0 || result < 0 )
				best = direct;
			else
				best = transposed;
		}
		else if ( direct == null )
			best = transposed;
		else 
			best = direct;
		return best;
	}
	/**
	 * The only way to remove a version from an MVD is to construct 
	 * a graph and then delete the version from it. Then we serialise 
	 * it out into pairs again and call the other removeVersion method 
	 * on EACH and EVERY pair.
	 * @param version the version to be removed
	 */
	public void removeVersion( int version ) throws Exception
	{
		Converter con = new Converter();
		VariantGraph original = con.create(matches, witnesses.size() );
		original.removeVersion( version );
		original.verify();
		witnesses.remove( version-1 );
		matches = con.serialise();
		for ( int i=0;i< matches.size();i++ )
		{
			Match p = matches.get( i );
			p.versions = removeVersion( p.versions, version );
		}
	}
	/**
	 * Remove a version from a BitSet and shift all subsequent 
	 * versions down by 1
	 * @param versions the bitset containing the versions
	 * @param version the version id to remove
	 * @erturn a modified bitset 
	 */
	private BitSet removeVersion( BitSet versions, int version )
	{
		BitSet bs = new BitSet();
		for ( int i=versions.nextSetBit(0);i>=0;
			i=versions.nextSetBit(i+1) ) 
		{
			if ( i < version )
				bs.set( i );
			else if ( i > version )
				bs.set( i-1 );
			// and if equal we of course skip it
		}
		return bs;
	}

	/**
	 * Get the long name for the given version
	 * @param versionId the id of the version
	 * @return its long name
	 */
	public String getLongNameForVersion( int versionId )
	{
		Witness v = witnesses.get( versionId-1 );
		if ( v != null )
			return v.longName;
		else
			return "";
	}
	/**
	 * Get the version contents by its short name
	 * @param shortName the shortName identifying the version
	 * @return the id of that version
	 */
	int getVersionByShortName( String shortName )
	{
		int version = -1;
		for ( int i=0;i< witnesses.size();i++ )
		{
			Witness v = witnesses.get( i );
			if ( v.shortName.equals(shortName) )
			{
				version = i;
				break;
			}
		}
		return version+1;
	}

	/**
	 * Get the id of the highest version in the MVD
	 * @return a version ID
	 */
	int getHighestVersion()
	{
		return witnesses.size();
	}

	/**
	 * Return a printout of all the versions in the MVD.
	 * @return the descriptions as a String array
	 */
	public String[] getVersionDescriptions()
	{
		String[] descriptions = new String[witnesses.size()];
		for ( int id=1,i=0;i< witnesses.size();i++,id++ )
		{
			Witness v = witnesses.get(i);
			descriptions[i] = v.toString()+";id:"+id;
		}
		return descriptions;
	}

	/**
	 * Retrieve a version, copying it from the MVD
	 * @param version the version to retrieve
	 * @return a byte array containing all the data of that version
	 */
	public byte[] getVersion( int version )
	{
		int length = 0;
		// measure the length
		for ( int i=0;i< matches.size();i++ )
		{
			Match p = matches.get( i );
			if ( p.versions.nextSetBit(version)==version )
			{
				length += p.length();
			}
		}
		byte[] result = new byte[length];
		// now copy it
		int k,i;
		for ( k=0,i=0;i< matches.size();i++ )
		{
			Match p = matches.get( i );
			if ( p.versions.nextSetBit(version)==version )
			{
				for ( int j=0;j<p.length();j++ )
					result[k++] = p.getData()[j];
			}
		}
		return result;
	}
	/*
	 * Find out the version id from the version's long name
	 * @param longName the long name of the desired version
	 * @return the version id or -1
	 */
	public short getVersionByLongName( String longName )
	{
		for ( int i=0;i< witnesses.size();i++ )
		{
			Witness vi = witnesses.get( i );
			if ( longName.equals(vi.longName) )
				return (short)(i+1);
		}
		return -1;
	}
	/**
	 * Get a version's long name
	 * @param id the id of the version
	 * @return the long name of the version
	 */
	public String getVersionLongName( int id )
	{
		Witness v = witnesses.get( id-1 );
		return v.longName;
	}
	/**
	 * Get a version's short name
	 * @param id the id of the version
	 * @return the short name of the version
	 */
	public String getVersionShortName( int id )
	{
		Witness v = witnesses.get( id-1 );
		return v.shortName;
	}
	/**
	 * For each version in the MVD calculate its length in bytes
	 * @return an array of lengths where each index represents 
	 * one version id-1 and the values are the lengths of that 
	 * version
	 */
	int[] getVersionLengths()
	{
		int[] lengths = new int[witnesses.size()];
		for ( int i=0;i< matches.size();i++ )
		{
			Match p = matches.get( i );
			BitSet bs = p.versions;
			for ( int j=bs.nextSetBit(1);j>=0;j=bs.nextSetBit(j+1) ) 
			{
				lengths[j-1] += p.length();
			}
		}
		return lengths;
	}




	/**
	 * Get the variants as in an apparatus. The technique is to reconstruct 
	 * just enough of the variant graph for a given range of pairs to determine 
	 * what the variants of a given base text are.
	 * @param base the base version
	 * @param offset the starting offset in that version
	 * @param len the length of the range to compute variants for
	 * @return an array of Variants
	 * @throws MVDException
	 */
	public Variant[] getApparatus( short base, int offset, int len ) 
		throws MVDException
	{
		int first = getPairIndex( base, offset );
		int last = getPairIndex( base, offset+len );
		/// list of unattached-as-outgoing pairs on the right
		LinkedList<WrappedPair> right = new LinkedList<WrappedPair>();
		LinkedList<CompactNode> nodes = buildBasicNodes( first, last, 
			right, true );
		// find the nodes to which any remaining pairs belong
		// there may still be some ambiguous pairs that are outgoing 
		// from nodes within the range
		if ( !right.isEmpty() )
			buildBasicNodes( 0, first-1, right, false );
		return buildVariants( nodes, base );
	}
	/**
	 * Build a list of all nodes within the range
	 * @param first first index in the pairs array
	 * @param last last index in the pairs array
	 * @param right list of unattached pairs
	 * @param pushRight if true push unattached pairs onto the right list
	 * @return a list of nodes
	 */
	LinkedList<CompactNode> buildBasicNodes( int first, int last, 
		LinkedList<WrappedPair> right, boolean pushRight )
	{
		LinkedList<CompactNode> nodes = new LinkedList<CompactNode>();
		for ( int i=last;i>=first;i-- )
		{
			// if not saving unattached pairs
			if ( !pushRight && right.isEmpty() )
				break;
			Match p = matches.get( i );
			if ( pushRight && p.isHint() )
				right.push( new WrappedPair(p) );
			else if ( !right.isEmpty() && right.peek().getMatch().isHint() )
			{
				CompactNode cn = new CompactNode( i );
				// add hint discretely
				cn.addOutgoing( right.pop().getMatch() );
				nodes.push( cn );
				addOutgoing( cn, right.pop(), right );
				setDefaultNode( cn, right );
				if ( pushRight )
					right.push( new WrappedPair(p) );
			}
			else if ( !right.isEmpty()
				&& right.peek().getMatch().versions.intersects(p.versions) )
			{
				CompactNode cn = new CompactNode( i );
				addOutgoing( cn, right.pop(), right );
				nodes.push( cn );
				setDefaultNode( cn, right );
				if ( pushRight )
					right.push( new WrappedPair(p) );
			}
			else if ( pushRight )
				right.push( new WrappedPair(p) );
		}
		return nodes;
	}
	/**
	 * Turn the raw list of nodes and their assigned versions into 
	 * an array of unique Variants
	 * @param nodes the list of variant nodes computed earlier
	 * @param base the base version of the variants
	 * @return and array of Variants
	 */
	Variant[] buildVariants( LinkedList<CompactNode> nodes, short base )
		throws MVDException
	{
		TreeSet<Variant> variants = new TreeSet<Variant>();
		LinkedList<CompactNode> departing = new LinkedList<CompactNode>();
		LinkedList<CompactNode> delenda = new LinkedList<CompactNode>();
		Iterator<CompactNode> iter1 = nodes.iterator();
		BitSet basePath = new BitSet();
		basePath.set( base );
		while ( iter1.hasNext() )
		{
			CompactNode node = iter1.next();
			if ( node.getIncoming().nextSetBit(base)==base )
			{
				// clear expended nodes from departing
				if ( delenda.size() > 0 )
				{
					Iterator<CompactNode> iter3 = delenda.iterator();
					while ( iter3.hasNext() )
						departing.remove( iter3.next() );
					delenda.clear();
				}
				Iterator<CompactNode> iter2 = departing.iterator();
				while ( iter2.hasNext() )
				{
					CompactNode upNode = iter2.next();
					if ( upNode.getOutgoing().intersects(node.getIncoming()) )
					{
						// compute intersection
						BitSet bs = new BitSet();
						bs.or( upNode.getOutgoing() );
						bs.and( node.getIncoming() );
						if ( !bs.isEmpty() )
						{
							BitSet[] paths = getUniquePaths( upNode, node, bs, base );
							// precompute base variant for later
							Variant[] w = getWordVariants(
								upNode.getIndex(), node.getIndex(),
								basePath );
							// create variants
							for ( int i=0;i<paths.length;i++ )
							{
								Variant[] v = getWordVariants( 
									upNode.getIndex(), node.getIndex(), 
									paths[i] );
								for ( int j=0;j<v.length;j++ )
								{
									// prune those equal to base content
									if ( w.length == 0 || !w[0].equalsContent(v[j]) )
									{
								    	if ( variants.size()>0 )
										{
								    		// omit variant if it is within an existing one
								    		Variant delendum = null;
								    		Iterator<Variant> iter3 = variants.descendingIterator();
								    		while ( iter3.hasNext() )
								    		{
								    			Variant x = iter3.next();
								    			if ( x.endIndex < v[j].startIndex )
								    			{
								    				variants.add( v[j] );
								    				break;
								    			}
								    			else if ( v[j].isWithin(x) )
													break;
												else if ( x.isWithin(v[j]) )
												{
													delendum = x;
													variants.add( v[j] );
													break;
												}
								    		}
								    		if ( delendum != null )
								    			variants.remove( delendum );
										}
										else
											variants.add( v[j] );
									}
								}
							}
							// clear that path so we won't follow it again
							upNode.getOutgoing().andNot( bs );
							if ( upNode.getOutgoing().isEmpty() )
								delenda.add( upNode );
							node.getIncoming().andNot( bs );
						}
					}
				}
				departing.push( node );
			}
		}
		Variant[] array = new Variant[variants.size()];
		return variants.toArray( array );
	}
	/**
	 * Get an array of variants (usually 1) corresponding to the 
	 * path between two nodes, extended to the next word-boundaries.
	 * The method is pretty simple: just follow the path for each separate 
	 * version of the variant. Generate one variant for each such path. 
	 * Then test if they are equal. If they are, merge them. Then return 
	 * an array of the remaining variants.
	 * @param start index of the start node
	 * @param end index of the end-node
	 * @param versions the set of versions to follow through the 
	 * variant(s)
	 * @return an array of Variants
	 */
	Variant[] getWordVariants( int start, int end, BitSet versions )
	{
		Vector<Variant> variants = new Vector<Variant>();
		int offset,length;
		int startIndex=start,origStart,endIndex;
		for ( int i=versions.nextSetBit(0);i>=0;i=versions.nextSetBit(i+1) ) 
		{
	     	offset = -1;
	     	length = 0;
	     	// get the first outgoing arc containing i
	     	startIndex = origStart = next( startIndex+1, (short)i );
	     	Match p = matches.get( startIndex );
	     	// start HERE: first outgoing arc, offset 0
	     	int lastStartIndex = startIndex;
	     	int lastOffset = 0;
	     	// move start index backwards, computing offset
		    while ( startIndex >= 0 )
		    {
		    	if ( offset < 0 )
		    	{
		    		startIndex = previous( startIndex, (short)i );
		    		if ( startIndex == -1 )
		    			break;
		    		p = matches.get( startIndex );
		    		if ( p.length()==0 )
		    			offset = -1;
		    		else
		    			offset = p.length()-1;
		    	}
		    	else if ( p.getData()[offset]!=' ' )
		    	{
		    		lastStartIndex = startIndex;
		    		lastOffset = offset;
		    		offset--;
		    		length++;
		    	}
		    	else
		    	{
		    		startIndex = lastStartIndex;
		    		offset = lastOffset;
		    		break;
		    	}
		    }
		    // we may shoot off the start
		    if ( startIndex == -1 )
		    {
		    	offset = 0;
		    	startIndex = next( 0, (short)i );
		    }
		    // now advance to end, extending length
		    endIndex = origStart;
		    while ( endIndex <= end )
		    {
		    	p = matches.get( endIndex );
			    length += p.length();
		    	endIndex = next( endIndex+1, (short)i );
		    }
		    // extend to next space after end
		    p = matches.get( endIndex );
	    	int endOffset = 0;
		    while ( endIndex < matches.size() )
		    {
		    	if ( endOffset==p.length() )
		    	{
		    		endIndex = next( endIndex+1, (short)i );
		    		if ( endIndex == Integer.MAX_VALUE )
		    			break;
		    		p = matches.get( endIndex );
		    		endOffset = 0;
		    	}
		    	else if ( p.getData()[endOffset]!=' ' )
		    	{
		    		endOffset++;
		    		length++;
		    	}
		    	else 
		    		break;
		    }
		    // in case we shot off the end
		    if ( endIndex == Integer.MAX_VALUE )
		    	endIndex = previous( matches.size()-1,(short)i );
		    // now build variant
		    BitSet bs = new BitSet();
		    bs.set( i );
		    Variant temp = new Variant( offset, startIndex, endIndex, 
		    	length, bs, this );
		    int k;
		    for ( k=0;k<variants.size();k++ )
		    {
		    	if ( temp.equalsContent(variants.get(k)) )
		    	{
		    		variants.get(k).merge( temp );
		    		break;
		    	}
		    }
		    if ( k == variants.size() )
				variants.add( temp );
		}
		Variant[] array = new Variant[variants.size()];
		return variants.toArray( array );
	}
	/**
	 * Set the default node of the wrapped pairs on the right list 
	 * if not already set. Stop once you meet one that IS set.
	 * @param cn the Compact Node to act as default parent
	 * @param right the list
	 */
	void setDefaultNode( CompactNode cn, LinkedList<WrappedPair> right )
	{
		Iterator<WrappedPair> iter = right.iterator();
		while ( iter.hasNext() )
		{
			WrappedPair wp = iter.next();
			if ( wp.getDefaultNode() == null )
				wp.setDefaultNode( cn );
			else
				break;
		}
	}
	/**
	 * Add an outgoing arc to a node and look for incoming 
	 * pairs to the left of the node.
	 */
	void addOutgoing( CompactNode cn, WrappedPair p, LinkedList<WrappedPair> right )
	{
		cn.addOutgoing( p.getMatch() );
		BitSet wi = cn.getWantsIncoming();
		while ( !wi.isEmpty() )
		{
			int index = cn.getIndex();
			for ( int i=index;i>=0;i-- )
			{
				Match q = matches.get( i );
				if ( q.versions.intersects(wi) )
				{
					addIncoming( cn, new WrappedPair(q), right );
					wi = cn.getWantsIncoming();
					break;
				}
			}
		}
		// look through right for arcs that intersect 
		// with p and must be attached to their default nodes
		Iterator<WrappedPair> iter = right.iterator();
		WrappedPair q = null;
		while ( iter.hasNext() )
		{
			q = iter.next();
			if ( q.getMatch().versions.intersects(p.getMatch().versions) )
				break;
			else
				q = null;
		}
		if ( q != null )
		{
			right.remove( q );
			CompactNode c = q.getDefaultNode();
			addIncoming( c, p, right );
			addOutgoing( c, q, right );
		}
	}
	/**
	 * Add an incoming arc to a node and look for intersecting 
	 * pairs in the right list. 
	 */
	void addIncoming( CompactNode cn, WrappedPair p, LinkedList<WrappedPair> right )
	{
		cn.addIncoming( p.getMatch() );
		BitSet wo = cn.getWantsOutgoing();
		while ( !wo.isEmpty() )
		{
			Iterator<WrappedPair> iter = right.iterator();
			WrappedPair q = null;
			while ( iter.hasNext() )		
			{
				q = iter.next();
				if ( q.getMatch().versions.intersects(wo) )
					break;
				else
					q = null;
			}
			if ( q != null )
			{
				right.remove( q );
				addOutgoing( cn, q, right );
				wo = cn.getWantsOutgoing();
			}
			else
				break;
		}
	}
	/**
	 * Compute an array of unique paths between two nodes in the graph 
	 * that don't include the base version
	 * @param from the node we are travelling from
	 * @param to the node we are travelling to
	 * @param pathV the set of versions to try and follow 
	 * @param base the base version
	 * @return an array of paths unique to that walk
	 */
	BitSet[] getUniquePaths( CompactNode from, CompactNode to, 
		BitSet pathV, short base )
	{
		Vector<BitSet> paths = new Vector<BitSet>();
		// for each version in pathV follow the path from-to
		// if any such path contains even one pair that doesn't 
		// contain the base version, add it to the paths set.
		for ( int i=from.getIndex()+1;i<=to.getIndex();i++ )
		{
			Match p = matches.get( i );
			if ( p.versions.intersects(pathV) )
			{
				// get intersection
				BitSet bs = new BitSet();
				bs.or( p.versions );
				bs.and( pathV );
				if ( !bs.isEmpty() )
				{
					LinkedList<BitSet> queue = new LinkedList<BitSet>();
					queue.push( bs );
					while ( !queue.isEmpty() )
					{
						BitSet b = queue.pop();
						int j = 0;
						int remove = -1;
						for ( ;j<paths.size();j++ )
						{
							BitSet c = paths.get( j );
							if ( b.equals(c) )
								break;
							else if ( b.intersects(c) )
							{
								// compute intersection and difference
								BitSet d = new BitSet();
								BitSet e = new BitSet();
								d.or( b );
								e.or( b );
								d.and( c );
								e.andNot( c );
								// push them both and start again
								queue.push( d );
								queue.push( e );
								remove = j;
								break;
							}
						}
						if ( remove != -1 )
							paths.remove( remove );
						else if ( j == paths.size() )
						{
							paths.add( b );
						}
					}
				}
			}
		}
		int k = paths.size()-1;
		// variants containing the base aren't variants 
		while ( k >= 0 )
		{
			BitSet b = paths.get( k );
			if ( b.nextSetBit(base)==base )
				paths.remove( k );
			k--;
		}
		BitSet[] array = new BitSet[paths.size()];
		return paths.toArray( array );
	}
	/**
	 * Get the index of the pair containing the given offset in the 
	 * given version
	 * @param version the version to get the pair index for
	 * @param offset the byte-offset within the version
	 * @return the relevant pair index of -1 if not found
	 */
	int getPairIndex( short version, int offset )
	{
		int pos = 0;
		int found = -1;
		for ( int i=0;i< matches.size();i++ )
		{
			Match p = matches.get( i );
			if ( p.versions.nextSetBit(version)==version )
			{
				if ( offset < pos+p.length() )
				{
					found = i;
					break;
				}
				else 
					pos += p.length();
			}
		}
		return found;
	}
	/**
	 * Debug tool. Turn all instance vars to a string except content
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( "; directAlignOnly="+directAlignOnly);
		sb.append( "; versions.size()="+ witnesses.size() );
		sb.append( "; pairs.size()="+ matches.size() );
		sb.append( "; description="+description );
		sb.append( "; headerSize="+headerSize );
		sb.append( "; groupTableSize="+groupTableSize );
		sb.append( "; versionTableSize="+versionTableSize );
		sb.append( "; pairsTableSize="+pairsTableSize );
		sb.append( "; dataTableSize="+dataTableSize );
		sb.append( "; versionSetSize="+versionSetSize );
		sb.append( "; bestScore="+bestScore );
		sb.append( "; parents.size()="+parents.size() );
		sb.append( "; partialVersions="+partialVersions );
		sb.append( "; encoding="+encoding );
		return sb.toString();
	}
	/**
	 * Compute a difference matrix, suitable for inputting into 
	 * fitch, kitsch or neighbor programs in Phylip. Compute a simple
	 * sum of squares between all possible pairs of versions in the MVD
	 * such that equal characters are scored as 0, variants as 1 for 
	 * each character of the longest of the two variants, 1 for each 
	 * entire transposition, thus scaling them for length (a 10-char 
	 * transposition costs half that of a 5-char one). Having calculated 
	 * that in a matrix of nVersions x nVersions, divide by the length
	 * of the longest version in each case - 1.
	 * @return a 2-D matrix of differences.
	 */
	public double[][] computeDiffMatrix( )
	{
		// ignore 0th element to simplify indexing
		int s = witnesses.size()+1;
		// keep track of the length of each version
		int[] lengths = new int[s];
		// the length of j last time j and k were joined
		int[][] lastJoinJ = new int[s][s];
		// the length of k last time j and k were joined
		int[][] lastJoinK = new int[s][s];
		// the cost is the longest distance between any two 
		// versions since they were last joined
		int[][] costs = new int[s][s];
		for ( int i=0;i< matches.size();i++ )
		{
			Match p = matches.get( i );
			// consider each combination of j and k, including j=k
			for ( int j=p.versions.nextSetBit(1);j>=1;j=p.versions.nextSetBit(j+1) )
			{
				for ( int k=p.versions.nextSetBit(j);k>=1;k=p.versions.nextSetBit(k+1) )
				{
					costs[j][k] += Math.max(
						lengths[j]-lastJoinJ[j][k],
						lengths[k]-lastJoinK[j][k]);
					costs[k][j] = costs[j][k];
					lastJoinJ[j][k] = lengths[j] + p.length();
					lastJoinK[j][k] = lengths[k] + p.length();
				}
				lengths[j] += p.length();
			}
		}
		double[][] diffs = new double[s-1][s-1];
		for ( int i=1;i<s;i++ )
		{
			for ( int j=1;j<s;j++ )
			{
				// normalise by the longer of the two lengths -1
				double denominator = Math.max(lengths[i],lengths[j])-1;
				diffs[i-1][j-1] = ((double)costs[i][j]) / denominator;
			}
		}
		return diffs;
	}
}
