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
import java.util.HashMap;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Arrays;
import java.util.Set;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import java.util.ListIterator;
import java.io.Serializable;

import au.edu.uq.nmerge.graph.Graph;
import au.edu.uq.nmerge.graph.MUM;
import au.edu.uq.nmerge.graph.SimpleQueue;
import au.edu.uq.nmerge.graph.SpecialArc;
import au.edu.uq.nmerge.graph.SpecialComparator;
import au.edu.uq.nmerge.graph.XMLMasker;
import au.edu.uq.nmerge.graph.suffixtree.SuffixTree;
import au.edu.uq.nmerge.graph.Converter;
import au.edu.uq.nmerge.exception.MVDException;

/**
 * Represent a multi-version document.
 * @author Desmond Schmidt &copy; 2009
 */
public class MVD extends Serialiser implements Serializable
{
	Mask mask;
	// new options
	boolean timing;
	boolean directAlignOnly;
	public static final long serialVersionUID = 1;
	static final int DUFF_PID = -1;
	static final int NULL_PID = 0;
	public static String UNTITLED_NAME = "untitled";
	Vector<Group> groups;		// id = position in table+1
	Vector<Version> versions;	// id = position in table+1
	Vector<Pair> pairs;
	String description;
	int headerSize,groupTableSize,versionTableSize,pairsTableSize,
	dataTableSize,versionSetSize;
	int bestScore;
	long startTime;
	// used for checking
	HashSet<Pair> parents;
	BitSet partialVersions;
	String encoding;
	public MVD()
	{
		setDefaults();
	}
	public MVD( String description )
	{
		setDefaults();
		this.description = description;
	}
	/**
	 * Create an empty MVD - add versions and data later
	 * @param description about this MVD
	 * @param encoding the encoding of the data in this MVD
	 */
	public MVD( String description, String encoding )
	{
		setDefaults();
		this.description = description;
		this.encoding = encoding;
	}
	/**
	 * Set default values for the MVD
	 */
	private void setDefaults()
	{
		this.description = "";
		this.groups = new Vector<Group>();
		this.versions = new Vector<Version>();
		this.pairs = new Vector<Pair>();
		this.mask = Mask.NONE;
		this.encoding = "UTF-8";
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
	 * @param group its group id
	 * @throws MVDException
	 */
	public void addVersion( int version, short group ) throws MVDException
	{
		if ( version == versions.size()+1 )
		{
			Version v = new Version( group, Version.NO_BACKUP, "Z", 
				UNTITLED_NAME );
			versions.insertElementAt( v, version-1 );
		}
		else
			throw new MVDException("Invalid version "+version+" ignored");
	}
	/**
	 * Add a version to the MVD. 
	 * @param v the version definition to add
	 */
	void addVersion( Version v ) throws MVDException
	{
		if ( versions == null )
			versions = new Vector<Version>();
		if ( v.group > groups.size() )
			throw new MVDException( "invalid group id="+v.group );
		versions.add( v );
	}
	/**
	 * Get the number of versions
	 * @return the number of elements in the versions array
	 */
	public int numVersions()
	{
		return versions.size();
	}
	/**
	 * Get the number of groups
	 * @return the number of elements in the groups array
	 */
	public int numGroups()
	{
		return groups.size();
	}
	/**
	 * Add an anonymous group to a finished MVD by shifting higher 
	 * group ids up.
	 * @param groupId the id of the desired group
	 * @param parent the id of the parent group or 0 if top level
	 */
	public void addGroup( short groupId, short parent )
	{
		Group g = new Group( parent, UNTITLED_NAME );
		groups.insertElementAt( g, groupId-1 );
		// adjust groups referenced by versions
		for ( int i=0;i<versions.size();i++ )
		{
			Version v = versions.get( i );
			if ( v.group >= groupId )
				v.group++;
		}
	}
	/**
	 * Add a group to the MVD
	 * @param group the group to add
	 */
	void addGroup( Group group )
	{
		if ( groups == null )
			groups = new Vector<Group>();
		groups.add( group );
	}
	/**
	 * Get any sub-groups of the specified group
	 * @return an array of sub-groups or an empty array if none
	 */
	public String[] getSubGroups( short groupId )
	{
		Vector<String> subGroups = new Vector<String>();
		for ( int id=1,i=0;i<groups.size();i++,id++ )
		{
			Group g = groups.get( i );
			if ( g.getParent() == groupId )
				subGroups.add( g.toString()+";id:"+id );
		}
		String[] array = new String[subGroups.size()];
		return subGroups.toArray( array );
	}
	/**
	 * Get any immediate (not nested) sub-versions of the specified group
	 * @return an array of sub-versions or an empty array if none
	 */
	public String[] getSubVersions( short groupId )
	{
		Vector<String> subVersions = new Vector<String>();
		for ( int id=1,i=0;i<versions.size();i++,id++ )
		{
			Version v = versions.get( i );
			if ( v.group == groupId )
				subVersions.add( v.toString()+";id:"+id );
		}
		String[] array = new String[subVersions.size()];
		return subVersions.toArray( array );
	}
	/**
	 * Add a pair to the MVD
	 * @param pair the pair to add
	 */
	void addPair( Pair pair ) throws Exception
	{
		pairs.add( pair );
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
	public Vector<Pair> getPairs()
	{
		return pairs;
	}
	/**
	 * Get a pair from the MVD
	 * @param pairIndex the index of the pair
	 */
	Pair getPair( int pairIndex ) throws Exception
	{
		return pairs.get( pairIndex );
	}
	/**
	 * Make a bitset of all the partial versions for quick lookup
	 */
	void initPartialVersions()
	{
		partialVersions = new BitSet();
		for ( int i=1;i<=versions.size();i++ ) 
		{ 
			Version v = versions.get( i-1 );
			if ( v.isPartial() )
				partialVersions.set( i );
		}
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
		short backup = versions.get(u-1).getBackup();
		Chunk current = new Chunk( encoding, backup );
		current.setVersion( u );
		TransposeState oldTS = null;
		TransposeState ts = new TransposeState();
		ChunkStateSet cs = new ChunkStateSet( backup );
		ChunkStateSet oldCS = null;
		Pair p = null;
		Chunk.chunkId = 0;
		TransposeState.transposeId = Integer.MAX_VALUE;
		int i = next( 0, u );
		while ( i < pairs.size() )
		{
			p = pairs.get( i );
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
				current = new Chunk( encoding, ts.getId(), 
					newStates, p.getData(), backup );
				current.setVersion( u );
			}
			else
				current.addData( p.getData() );
			if ( i < pairs.size()-1 )
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
	 * Update the chunk's state list given a new pair and the version 
	 * we are following through
	 * @param chunk the chunk to update
	 * @param p the new pair
	 * @param version
	 */
	public void nextChunkState( Chunk chunk, Pair p, short version )
	{
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
		while ( i < pairs.size() )
		{
			Pair p = pairs.get( i );
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
			Pair p = pairs.get( i );
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
	public Match[] search( byte[] pattern, BitSet bs, boolean multiple ) 
		throws Exception
	{
		KMPSearchState inactive = null;
		KMPSearchState active = null;
		Match[] matches = new Match[0];
		if ( !versions.isEmpty() )
		{
			inactive = new KMPSearchState( pattern, bs );
			for ( int i=0;i<pairs.size();i++ )
			{
				Pair temp = pairs.get( i );
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
								Match[] m = Match.makeMatches( 
									pattern.length,ss.v,
									this,i,j,multiple,ChunkState.found);
								if ( matches == null )
									matches = m;
								else
									matches = Match.merge( matches, m );
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
		return matches;
	}
	/**
	 * Create a new empty version.
	 * @return the id of the new version
	 */
	public int newVersion( String shortName, String longName, String group, 
		short backup, boolean partial ) 
	{
		short gId = findGroup( group );
		if ( gId == 0 )
		{
			Group g = new Group( (short)0, group );
			groups.add( g );
			gId = (short)groups.size();
		}
		versions.add( new Version(gId, (partial)?backup:Version.NO_BACKUP, 
			shortName, longName) );
		int vId = versions.size();
		// now go through the graph, looking for any pair 
		// containing the backup version and adding to it 
		// the new version. Q: does that apply also to hints?
		if ( partial )
		{
			for ( int i=0;i<pairs.size();i++ )
			{
				Pair p = pairs.get( i );
				if ( p.versions.nextSetBit(backup)==backup )
					p.versions.set(vId);
			}
		}
		return vId;
	}
	/**
	 * Get the group id corresponding to the name
	 * @param groupName
	 * @return the group id
	 */
	private short findGroup( String groupName ) 
	{
		short id = 0;
		for ( int i=0;i<groups.size();i++ )
		{
			Group g = groups.get( i );
			if ( g.name.equals(groupName) )
			{
				id = (short) (i + 1);
				break;
			}
		}
		return id;
	}
	/**
	 * Get the group parent id
	 * @param groupId the group id
	 * @return the corresponding group name
	 */
	public short getGroupParent( short groupId )
	{
		Group g = groups.get( groupId-1 );
		return g.parent;
	}
	/**
	 * Get the group name given its id
	 * @param groupId the group id
	 * @return the corresponding group name
	 */
	public String getGroupName( short groupId )
	{
		Group g = groups.get( groupId-1 );
		return g.name;
	}
	/**
	 * Get the backup for the given version
	 * @param vId the version to get the backup of
	 * @return the backup version or 0 for NO_BACKUP
	 */
	public short getBackupForVersion( int vId )
	{
		Version v = versions.get(vId-1);
		return v.backup;
	}
	/**
	 * Get the group id for the given version
	 * @param vId the version to get the group of
	 * @return the group id
	 */
	public short getGroupForVersion( int vId )
	{
		Version v = versions.get(vId-1);
		return v.group;
	}
	/**
	 * Get the current index of the given group + 1
	 * @param g the group to search for
	 * @return the index +1 of the group in the groups vector or 0
	 */
	public short getGroupId( Group g )
	{
		return (short) (groups.indexOf(g) + 1);
	}
	/**
	 * Get the current index of the given version + 1
	 * @param v the version to search for
	 * @return the index +1 of the version in the versions vector or 0
	 */
	public int getVersionId( Version v )
	{
		return versions.indexOf(v) + 1;
	}
	/**
	 * Get the id of a version (version index+1) given its short name
	 * @param shortName the shortName
	 * @return -1 if not found or the correct id
	 */
	public int getVersionId( String shortName )
	{
		for ( int i=0;i<versions.size();i++ )
			if ( versions.get(i).shortName.equals(shortName) )
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
	 * Rename a group. If the groupId == 0 do nothing
	 * @param groupId the id of the group to rename
	 * @param groupName the new name for the group
	 */
	public void setGroupName( short groupId, String groupName )
	{
		if ( groupId > 0 )
		{
			Group g = groups.get( groupId-1 );
			g.setName( groupName);
		}
	}
	/**
	 * Set the parent of the given group
	 * @param groupId the id of the group to change
	 * @param parentId the new parent
	 */
	public void setGroupParent( short groupId, short parentId )
	{
		if ( groupId > 0 )
		{
			Group g = groups.get( groupId-1 );
			g.setParent( parentId );
		}
	}
	/**
	 * Set the short name of a given version
	 * @param versionId the id of the affected version
	 * @param shortName the new short name
	 */
	public void setVersionShortName( int versionId, String shortName )
	{
		Version v = versions.get( versionId-1 );
		v.shortName = shortName;
	}
	/**
	 * Set the long name of a given version
	 * @param versionId the id of the affected version
	 * @param longName the new long name
	 */
	public void setVersionLongName( int versionId, String longName )
	{
		Version v = versions.get( versionId-1 );
		v.longName = longName;
	}
	/**
	 * Set the backup version of a given version
	 * @param versionId the id of the affected version
	 * @param backup the new backup or NO_BACKUP
	 */
	public void setVersionBackup( int versionId, short backup )
	{
		Version v = versions.get( versionId-1 );
		v.backup = backup;
	}
	/**
	 * Set the group membership of a version
	 * @param versionId id of the affected version 
	 * @param groupId the new groupId
	 */
	public void setVersionGroup( int versionId, short groupId )
	{
		Version v = versions.get( versionId-1 );
		v.group = groupId;
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
	 * Set a group's open status (a transient property)
	 * @param groupId the group id that is affected by the change
	 * @param open the new open value
	 */
	public void setOpen( short groupId, boolean open )
	{
		Group g = groups.get( groupId-1 );
		g.setOpen( open );
	}
	/**
	 * Get the default group for this MVD
	 * @return null if no versions or groups defined, otherwise the 
	 * first group in the list
	 */
	public String getDefaultGroup()
	{
		String groupName = null;
		if ( groups.size() > 0 )
		{
			Group g = groups.get( 0 );
			groupName = g.name;
		}
		return groupName;
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
		Graph original = con.create( pairs, versions.size() );
		original.removeVersion( version );
		Graph g = original;
		SpecialArc special;
		if ( mask != Mask.NONE )
		{
			byte[] byteMask = XMLMasker.getMask(data, mask==Mask.XML);
			special = g.addSpecialArc( data, byteMask, version, 0 );
		}
		else
			special = g.addSpecialArc( data, version, 0 );
		if ( timing )
			startTime = System.currentTimeMillis();
		if ( g.getStart().cardinality() > 1 )
		{
			SuffixTree st = makeSuffixTree( special );
			MUM bestMUM = MUM.findDirectMUM( special, st, g );
			TreeMap<SpecialArc,Graph> specials = 
				new TreeMap<SpecialArc,Graph>(new SpecialComparator());
			while ( bestMUM != null )
			{
				if ( bestMUM.verify() )
				{
					bestMUM.merge();
					SimpleQueue<SpecialArc> leftSpecials = 
						bestMUM.getLeftSpecialArcs();
					SimpleQueue<SpecialArc> rightSpecials = 
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
					SpecialArc key = specials.firstKey();
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
		pairs = con.serialise();
		if ( encoding.toUpperCase().equals("UTF-8") )
			removeUTF8Splits();
		if ( timing )
		{
			String finishTime = new Long(System.currentTimeMillis()
				-startTime).toString();
			System.out.println( "Time taken to merge version "
				+version+"="+finishTime );
		}
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
		for ( int i=0;i<pairs.size();i++ )
		{
			Pair p = pairs.get(i);
			if ( p.dataSize() > 0 )
			{
				byte[] data = pairs.get(i).getData();
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
				if ( len > 0 && i < pairs.size()-1 )
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
					while ( !seekSet.isEmpty() && m < pairs.size() )
					{
						Pair q = pairs.get( m );
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
			for ( int i=0;i<pairs.size();i++ )
			{
				Pair p = pairs.get( i );
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
		for ( int i=0;i<pairs.size();i++ )
		{
			Pair p = pairs.get( i );
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
	MUM recomputeMUM( MUM old ) throws MVDException
	{
		Graph g = old.getGraph();
		SpecialArc special = old.getArc();
		return computeBestMUM( g, special );
	}
	/**
	 * Compute the best MUM
	 * @param g a graph
	 * @param special a special arc aligned with g
	 * @return the new MUM or null
	 * @throws an MVDException
	 */
	private MUM computeBestMUM( Graph g, SpecialArc special ) 
		throws MVDException
	{
		SuffixTree st = makeSuffixTree( special );
		MUM directMUM = MUM.findDirectMUM( special, st, g );
		MUM best = directMUM;
		if ( !directAlignOnly )
		{
			MUM leftTransposeMUM = MUM.findLeftTransposeMUM( 
				special, st, g );
			MUM rightTransposeMUM = MUM.findRightTransposeMUM( 
				special, st, g );
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
	private SuffixTree makeSuffixTree( SpecialArc special ) 
		throws MVDException
	{
		byte[] specialData;
		if ( special.hasMask() )
			specialData = XMLMasker.maskOut(special.getData(), 
				special.getMask() );
		else
			specialData = special.getData();
		return new SuffixTree( specialData );
	}
	/**
	 * Install a subarc into specials
	 * @param specials the specials TreeMap (red-black tree)
	 * @param special the special subarc to add
	 * @param subGraph the directly opposite subgraph
	 * @param left true if we are doing the left subarc, otherwise the 
	 * right
	 */
	private void installSpecial( TreeMap<SpecialArc,Graph> specials, 
		SpecialArc special, Graph subGraph, boolean left ) throws MVDException
	{
		assert special.getFrom() != null && special.to != null;
		// this is necessary BEFORE you recalculate the MUM
		// because it will invalidate the special's location 
		// in the treemap and make it unfindable
		if ( specials.containsKey(special) )
			specials.remove( special );
		MUM best = computeBestMUM( subGraph, special );
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
	private MUM getBest( MUM direct, MUM leftTransposed, 
		MUM rightTransposed )
	{
		MUM best = null;
		// decide which transpose MUM to use
		MUM transposed;
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
		Graph original = con.create( pairs, versions.size() );
		original.removeVersion( version );
		original.verify();
		versions.remove( version-1 );
		pairs = con.serialise();
		for ( int i=0;i<pairs.size();i++ )
		{
			Pair p = pairs.get( i );
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
	 * Remove a group from the group table. Check that the parent 
	 * group now has at least one member. If not, remove it also. 
	 * Update all the group ids in all the versions too. 
	 * @param group the group id to remove
	 */
	public void removeGroup( short group ) throws Exception
	{
		// remove the actual group
		groups.remove( (short)(group-1) );
		// update all the versions to reflect the change
		HashSet<Integer> delenda = new HashSet<Integer>();
		for ( int i=0;i<versions.size();i++ )
		{
			Version v = versions.get( i );
			if ( v.group > group )
				v.group--;
			else if ( v.group == group )
				delenda.add( new Integer(i+1) );
		}
		// now remove any child versions of the group 
		if ( delenda.size() > 0 )
		{
			// delete the versions in reverse order
			Integer[] array = new Integer[delenda.size()];
			delenda.toArray( array );
			Arrays.sort( array );
			for ( int i=array.length-1;i>=0;i-- )
				removeVersion( array[i].intValue() );
		}
	}
	/**
	 * Get the long name for the given version
	 * @param versionId the id of the version
	 * @return its long name
	 */
	public String getLongNameForVersion( int versionId )
	{
		Version v = versions.get( versionId-1 );
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
		for ( int i=0;i<versions.size();i++ )
		{
			Version v = versions.get( i );
			if ( v.shortName.equals(shortName) )
			{
				version = i;
				break;
			}
		}
		return version+1;
	}
	/**
	 * Get an array of Version ids of a given group. If request is 
	 * for a group that contains other groups, get the versions for 
	 * that group recursively.
	 * @param group the group or TOP_LEVEL - get all the versions of 
	 * this group and its descendants
	 * @return an array of version ids
	 */
	public int[] getVersionsForGroup( short group )
	{
		HashSet<Short> descendants = new HashSet<Short>();
		if ( group != Group.TOP_LEVEL )
			descendants.add( group );
		getDescendantsOfGroup( group, descendants );
		Vector<Integer> chosen = new Vector<Integer>();
		for ( int i=0;i<versions.size();i++ )
		{
			Version v = versions.get( i );
			Short vGroup = new Short( v.group );
			if ( descendants.contains(vGroup) )
				chosen.add( i+1 );
		}
		int[] selectedVersions = new int[chosen.size()];
		for ( int i=0;i<chosen.size();i++ )
			selectedVersions[i] = chosen.get(i).intValue();
		return selectedVersions;
	}
	/**
	 * Get all the direct descendants of a group 
	 * @param group the parent group to check for descendants
	 * @param descendants a set containing the ids of the descendants 
	 * to be filled in
	 */
	private void getDescendantsOfGroup( short group, 
		HashSet<Short> descendants )
	{
		for ( int i=0;i<groups.size();i++ )
		{
			Group g = groups.get( i );
			if ( group == g.parent )
			{
				short localGroup = (short)(i+1);
				descendants.add( localGroup );
				getDescendantsOfGroup( localGroup, descendants );
			}
		}
	}
	/**
	 * Get the id of the highest version in the MVD
	 * @return a version ID
	 */
	int getHighestVersion()
	{
		return versions.size();
	}
	/**
	 * Return a readable printout of all the versions in the MVD.
	 * @param indent the amount to indent the outermost group
	 * @param gId the id of the group whose contents are desired
	 * @return the contents of the group in XML
	 * @throws MVDException if the group was not found
	 */
	public String getContentsForGroup( int indent, short gId ) 
		throws MVDException
	{
		StringBuffer sb = new StringBuffer();
		// write group start tag
		for ( int i=0;i<indent;i++ )
			sb.append( " " );
		Group g1 = (gId != 0)?groups.get(gId-1):new Group((short)-1, 
			"top level" );
		if ( g1 == null )
			throw new MVDException("group id "+gId+" not found!");
		sb.append("<group name=\""+g1.name+"\" id=\""+gId+"\"");
		if ( gId != 0 )
			sb.append(" parent=\""+g1.parent+"\"");
		sb.append( ">\n" );
		// check for sub-groups
		for ( short i=0;i<groups.size();i++ )
		{
			Group g = groups.get( i );
			if ( g.parent == gId )
				sb.append( getContentsForGroup(indent+2,
					(short)(i+1)) );
		}
		// get sub-versions
		for ( short i=0;i<versions.size();i++ )
		{
			Version v = versions.get( i );
			if ( v.group == gId )
				sb.append( v.toXML(indent+2,i+1) );
		}
		// write group end tag
		for ( int i=0;i<indent;i++ )
			sb.append( " " );
		sb.append("</group>");
		sb.append("\n");
		return sb.toString();
	}
	/**
	 * Return a printout of all the versions in the MVD.
	 * @return the descriptions as a String array
	 */
	public String[] getVersionDescriptions()
	{
		String[] descriptions = new String[versions.size()];
		for ( int id=1,i=0;i<versions.size();i++,id++ )
		{
			Version v = versions.get(i);
			descriptions[i] = v.toString()+";id:"+id;
		}
		return descriptions;
	}
	/**
	 * Return a printout of all the groups in the MVD.
	 * @return the descriptions as a String array
	 */
	public String[] getGroupDescriptions()
	{
		String[] descriptions = new String[groups.size()];
		for ( int id=1,i=0;i<groups.size();i++,id++ )
		{
			Group g = groups.get(i);
			descriptions[i] = g.toString()+";id:"+id;
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
		for ( int i=0;i<pairs.size();i++ )
		{
			Pair p = pairs.get( i );
			if ( p.versions.nextSetBit(version)==version )
			{
				length += p.length();
			}
		}
		byte[] result = new byte[length];
		// now copy it
		int k,i;
		for ( k=0,i=0;i<pairs.size();i++ )
		{
			Pair p = pairs.get( i );
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
		for ( int i=0;i<versions.size();i++ )
		{
			Version vi = versions.get( i );
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
		Version v = versions.get( id-1 );
		return v.longName;
	}
	/**
	 * Get a version's short name
	 * @param id the id of the version
	 * @return the short name of the version
	 */
	public String getVersionShortName( int id )
	{
		Version v = versions.get( id-1 );
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
		int[] lengths = new int[versions.size()];
		for ( int i=0;i<pairs.size();i++ )
		{
			Pair p = pairs.get( i );
			BitSet bs = p.versions;
			for ( int j=bs.nextSetBit(1);j>=0;j=bs.nextSetBit(j+1) ) 
			{
				lengths[j-1] += p.length();
			}
		}
		return lengths;
	}
	/**
	 * Get the size of the data required in bytes to store this MVD
	 * @return the byte-size of the serialised mvd
	 * @throws UnsupportedEncodingException
	 */
	int dataSize() throws UnsupportedEncodingException
	{
		headerSize = groupTableSize = versionTableSize = 
			pairsTableSize = dataTableSize = 0;
		// header
		headerSize = MVDFile.MVD_MAGIC.length; // magic
		headerSize += 5 * 4; // table offsets etc
		/*try
		{
			MVDError.log( this.toString() );
		}
		catch ( Exception e )
		{
		}*/
		headerSize += measureUtf8String( description );
		headerSize += measureUtf8String( encoding );
		groupTableSize = 2; // number of groups
		for ( int i=0;i<groups.size();i++ )
		{
			Group g = groups.get( i );
			groupTableSize += g.dataSize();
		}
		versionTableSize = 2 + 2; // number of versions + setSize
		for ( int i=0;i<versions.size();i++ )
		{
			Version v = versions.get( i );
			versionTableSize += v.dataSize();
		}
		pairsTableSize = 4;	// number of pairs
		versionSetSize = (versions.size()+8)/8;
		for ( int i=0;i<pairs.size();i++ )
		{
			Pair p = pairs.get( i );
			pairsTableSize += p.pairSize(versionSetSize);
			dataTableSize += p.dataSize();
		}
		return headerSize + groupTableSize + versionTableSize 
			+ pairsTableSize + dataTableSize;
	}
	/**
	 * Serialise the entire mvd into the given byte array. Must 
	 * be preceded by a call to dataSize (otherwise no way to 
	 * calculate size of data).
	 * @param data a byte array of exactly the right size
	 * @return the number of serialised bytes
	 * @throws an Exception if data was the wrong size
	 */
	int serialise( byte[] data ) throws Exception
	{
		int nBytes = serialiseHeader( data );
		int p = headerSize;
		nBytes += serialiseGroups( data, p );
		p += groupTableSize;
		nBytes += serialiseVersions( data, p );
		p += versionTableSize;
		nBytes += serialisePairs( data, p, p+pairsTableSize );
		return nBytes;
	}
	/**
	 * Serialise the header starting at offset 0 in the data byte 
	 * array
	 * @param data the byte array to write to
	 * @return the number of serialised bytes
	 */
	private int serialiseHeader( byte[] data ) throws Exception
	{
		int nBytes = 0;
		if ( data.length >= headerSize )
		{
			for ( int i=0;i<MVDFile.MVD_MAGIC.length;i++ )
				data[i] = MVDFile.MVD_MAGIC[i];
			int p = 0;
			nBytes = MVDFile.MVD_MAGIC.length;
			// mask type
			writeInt( data, p+nBytes, mask.ordinal() );
			nBytes += 4;
			// groupTableOffset
			writeInt( data, p+nBytes, headerSize );
			nBytes += 4;
			// versionTableOffset
			writeInt( data, p+nBytes, headerSize+groupTableSize );
			nBytes += 4;
			// pairsTableOffset
			writeInt( data, p+nBytes, headerSize
				+groupTableSize+versionTableSize );
			nBytes += 4;
			// dataTableOffset
			writeInt( data, p+nBytes, headerSize
				+groupTableSize+versionTableSize
				+pairsTableSize );
			nBytes += 4;
			nBytes += writeUtf8String( data, p+nBytes, description );
			nBytes += writeUtf8String( data, p+nBytes, encoding );
		}
		else
			throw new MVDException("No room for MVD header");
		return nBytes;
	}
	/**
	 * Serialise the pairs table starting at offset p in the data byte 
	 * array. Don't serialise the data they refer to yet. Since parents 
	 * and children may come in any order we have to keep track of orphaned 
	 * children or parents without children, and then join them up when we 
	 * can.
	 * @param data the byte array to write to
	 * @param p the offset within data to start writing
	 * @param dataTableOffset the offset to the start of the dataTable 
	 * within data
	 * @return the number of serialised bytes
	 */
	private int serialisePairs( byte[] data, int p, int dataTableOffset ) 
		throws Exception
	{
		int nBytes = 0;
		HashMap<Pair,Integer> ancestors = new HashMap<Pair,Integer>();
		HashMap<Pair,Integer> orphans = new HashMap<Pair,Integer>();
		if ( p + pairsTableSize <= data.length )
		{
			writeInt( data, p, pairs.size() );
			p += 4;
			nBytes += 4;
			// where we're writing the actual data
			int dataOffset = 0;
			int parentDataOffset = 0;
			int parentId = 1;
			for ( int i=0;i<pairs.size();i++ )
			{
				// this is set if known
				int tempPId = NULL_PID;
				Pair t = pairs.get( i );
				if ( t.isChild() )
				{
					// Do we have a registered parent?
					Integer value = ancestors.get( t.parent );
					// value is the parent's data offset
					if ( value != null )
					{
						parentDataOffset = value.intValue();
						tempPId = t.parent.id;
					}
					else
					{
						// the value in orphans is the offset 
						// pointing to the orphan pair entry
						orphans.put( t, new Integer(p) );
						// clearly duff value: fill this in later
						tempPId = DUFF_PID;
					}
				}
				else if ( t.isParent() )
				{
					// first assign this parent an id
					tempPId = t.id = parentId++;
					// then put ourselves in the ancestors list
					ancestors.put( t, dataOffset );
					// now check if we have any registered orphans
					ListIterator<Pair> iter = t.getChildIterator();
					while ( iter.hasNext() )
					{
						Pair child = iter.next();
						Integer value = orphans.get( child );
						if ( value != null )
						{
							// copy the parent's data offset 
							// into that of the child
							Pair.fixDataOffset( data, value.intValue(), 
								dataOffset, versionSetSize, t.id );
							// remove the child from the orphan list
							orphans.remove( child );
						}
					}
				}
				// if we set the parent data offset use that
				// otherwise use the current pair's data offset
				nBytes += t.serialisePair( data, p, versionSetSize, 
					(parentDataOffset!=0)?parentDataOffset:dataOffset, 
					dataTableOffset, tempPId );
				p += t.pairSize( versionSetSize );
				dataOffset += t.dataSize();
				parentDataOffset = 0;
			}
			if ( orphans.size() != 0 )
			{
				Set<Pair> keys = orphans.keySet();
				Iterator<Pair> iter = keys.iterator();
				while ( iter.hasNext() )
				{
					Pair q = iter.next();
					if ( !ancestors.containsKey(q) )
						System.out.println("No matching key for pair");
				}
				throw new MVDException("Unmatched orphans after serialisation");
			}
		}
		else
			throw new MVDException( "No room for pairs table" );
		return nBytes;
	}
	/**
	 * Serialise the groups table starting at offset p in the data byte 
	 * array
	 * @param data the byte array to write to
	 * @param p the offset within data to start writing
	 * @return the number of serialised bytes
	 */
	private int serialiseGroups( byte[] data, int p ) throws Exception
	{
		int oldP = p;
		if ( p + groupTableSize < data.length )
		{
			writeShort( data, p, (short)groups.size() );
			p += 2;
			for ( int i=0;i<groups.size();i++ )
			{
				Group g = groups.get( i );
				g.serialise( data, p );
				p += g.dataSize();
			}
		}
		else
			throw new MVDException( "No room for group table" );
		return p - oldP;
	}
	/**
	 * Serialise the versions table starting at offset p in the data 
	 * byte array
	 * @param data the byte array to write to
	 * @param p the offset within data to start writing
	 * @return the number of serialised bytes
	 */
	private int serialiseVersions( byte[] data, int p ) throws Exception
	{
		int oldP = p;
		if ( p + versionTableSize < data.length )
		{
			if ( versions.size() < 0 )
				throw new MVDException( "at least one version needed" );
			writeShort( data, p, (short)versions.size() );
			p += 2;
			writeShort( data, p, (short)versionSetSize );
			p += 2;
			for ( int i=0;i<versions.size();i++ )
			{
				Version v = versions.get( i );
				v.serialise( data, p );
				p += v.dataSize();
			}
		}
		else
			throw new MVDException( "No room for group table" );
		return p - oldP;
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
			Pair p = pairs.get( i );
			if ( pushRight && p.isHint() )
				right.push( new WrappedPair(p) );
			else if ( !right.isEmpty() && right.peek().getPair().isHint() )
			{
				CompactNode cn = new CompactNode( i );
				// add hint discretely
				cn.addOutgoing( right.pop().getPair() );
				nodes.push( cn );
				addOutgoing( cn, right.pop(), right );
				setDefaultNode( cn, right );
				if ( pushRight )
					right.push( new WrappedPair(p) );
			}
			else if ( !right.isEmpty()
				&& right.peek().getPair().versions.intersects(p.versions) )
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
	     	Pair p = pairs.get( startIndex );
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
		    		p = pairs.get( startIndex );
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
		    	p = pairs.get( endIndex );
			    length += p.length();
		    	endIndex = next( endIndex+1, (short)i );
		    }
		    // extend to next space after end
		    p = pairs.get( endIndex );
	    	int endOffset = 0;
		    while ( endIndex < pairs.size() )
		    {
		    	if ( endOffset==p.length() )
		    	{
		    		endIndex = next( endIndex+1, (short)i );
		    		if ( endIndex == Integer.MAX_VALUE )
		    			break;
		    		p = pairs.get( endIndex );
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
		    	endIndex = previous( pairs.size()-1,(short)i );
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
		cn.addOutgoing( p.getPair() );
		BitSet wi = cn.getWantsIncoming();
		while ( !wi.isEmpty() )
		{
			int index = cn.getIndex();
			for ( int i=index;i>=0;i-- )
			{
				Pair q = pairs.get( i );
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
			if ( q.getPair().versions.intersects(p.getPair().versions) )
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
		cn.addIncoming( p.getPair() );
		BitSet wo = cn.getWantsOutgoing();
		while ( !wo.isEmpty() )
		{
			Iterator<WrappedPair> iter = right.iterator();
			WrappedPair q = null;
			while ( iter.hasNext() )		
			{
				q = iter.next();
				if ( q.getPair().versions.intersects(wo) )
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
			Pair p = pairs.get( i );
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
		for ( int i=0;i<pairs.size();i++ )
		{
			Pair p = pairs.get( i );
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
		sb.append( "timing="+timing );
		sb.append( "; directAlignOnly="+directAlignOnly);
		sb.append( "; groups.size()="+groups.size() );
		sb.append( "; versions.size()="+versions.size() );
		sb.append( "; pairs.size()="+pairs.size() );
		sb.append( "; description="+description );
		sb.append( "; headerSize="+headerSize );
		sb.append( "; groupTableSize="+groupTableSize );
		sb.append( "; versionTableSize="+versionTableSize );
		sb.append( "; pairsTableSize="+pairsTableSize );
		sb.append( "; dataTableSize="+dataTableSize );
		sb.append( "; versionSetSize="+versionSetSize );
		sb.append( "; bestScore="+bestScore );
		sb.append( "; startTime="+startTime );
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
		int s = versions.size()+1;
		// keep track of the length of each version
		int[] lengths = new int[s];
		// the length of j last time j and k were joined
		int[][] lastJoinJ = new int[s][s];
		// the length of k last time j and k were joined
		int[][] lastJoinK = new int[s][s];
		// the cost is the longest distance between any two 
		// versions since they were last joined
		int[][] costs = new int[s][s];
		for ( int i=0;i<pairs.size();i++ )
		{
			Pair p = pairs.get( i );
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
