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

package au.edu.uq.nmerge;

import java.io.File;

import au.edu.uq.nmerge.exception.MVDException;
import au.edu.uq.nmerge.mvd.*;
import au.edu.uq.nmerge.Utilities;

import java.util.BitSet;

/**
 * This is the public interface to the nmerge package. 
 * This is what the web application sees.
 * Pass in file paths to MVDs and get back MVD data.
 * @author desmond 21/8/07
 */
public class PublicInterface 
{
	Cache cache;
	private static String DEFAULT_ENCODING = "UTF-8";
	/**
	 * Initialise the cache.
	 * @param dbConn database connection file
	 */
	public PublicInterface( String dbConn )
	{
		cache = new Cache( dbConn );
	}
	/**
	 * Second invocation uses a pre-created cache stored in the 
	 * session object.
	 * @param cache a previously stored cache
	 */
	public PublicInterface( Cache cache )
	{
		this.cache = cache;
	}
	/**
	 * Create a new MVD and save it
	 * @param mvdFile the actual file to create
	 * @param description the MVD description string
	 * @param encoding the encoding for data of the MVD
	 * @param mask the mask value NONE, TEXT or XML)
	 * @throws MVDException
	 */
	public void createNewMvd( File mvdFile, String description, 
		String encoding, int folderId, String mask ) throws MVDException
	{
		try
		{
			if ( mvdFile.exists() && !mvdFile.delete() )
				throw new Exception( 
					"Couldn't delete existing file "+mvdFile);
			else
			{
				Mask mvdMask = Mask.valueOf(mask.toUpperCase());
				MVD mvd = (description==null||description.length()==0)
					?new MVD():new MVD(description);
				mvd.setMask( mvdMask );
				mvd.setEncoding( encoding );
			}
		}
		catch ( Exception e )
		{
			throw new MVDException( e );
		}
	}
	/**
	 * Retrieve the cache so it can be attached to a session object, 
	 * so it will hang around between invocations.
	 * @return the cache
	 */
	public Cache getCache()
	{
		return cache;
	}
	/**
	 * Get the name of the default group of an mvd
	 * @param fileName the path to the mvd
	 * @return the name of the default group 0
	 */
	public String getDefaultGroup( String fileName ) throws Exception
	{
		MVD mvd = getFromCache( fileName );
		return mvd.getDefaultGroup();
	}
	/**
	 * Get a description from an MVD
	 * @param fileName the name of the file on the file system
	 * @return the description string from the MVD
	 */
	public String getDescription( String fileName ) throws Exception
	{
		MVD mvd = getFromCache( fileName );
		return mvd.getDescription();
	}
	/**
	 * Get an encoding from an MVD
	 * @param fileName the name of the file on the file system
	 * @return the encoding string from the MVD
	 */
	public String getEncoding( String fileName )
	{
		try
		{
			MVD mvd = getFromCache( fileName );
			return mvd.getEncoding();
		}
		catch ( Exception e)
		{
			return DEFAULT_ENCODING;
		}
	}
	/**
	 * Set the short name of a version
	 * @param mvdPath the path to the mvd
	 * @param versionId the id of the version affected
	 * @param shortName the desired new short name
	 */
	public void setVersionShortName( String mvdPath, int versionId, 
		String shortName ) throws Exception
	{
		MVD mvd = getFromCache( mvdPath );
		mvd.setVersionShortName( versionId, shortName );
	}
	/**
	 * Set the long name of a version
	 * @param mvdPath the path to the mvd
	 * @param versionId the id of the version affected
	 * @param longName the desired new long name
	 */
	public void setVersionLongName( String mvdPath, int versionId, 
		String longName ) throws Exception
	{
		MVD mvd = getFromCache( mvdPath );
		mvd.setVersionLongName( versionId, longName );
	}
	/**
	 * Set the backup version (or NO_BACKUP) for a given version
	 * @param mvdPath the path to the mvd
	 * @param versionId the id of the version affected
	 * @param backup the new backup version or NO_BACKUP
	 */
	public void setVersionBackup( String mvdPath, int versionId, 
		short backup ) throws Exception
	{
		MVD mvd = getFromCache( mvdPath );
		mvd.setVersionBackup( versionId, backup );
	}
	/**
	 * Set the group for a given version
	 * @param mvdPath the path to the mvd
	 * @param versionId the id of the version affected
	 * @param groupId the new group the version is to belong to
	 */
	public void setVersionGroup( String mvdPath, int versionId, 
		short groupId ) throws Exception
	{
		MVD mvd = getFromCache( mvdPath );
		mvd.setVersionGroup( versionId, groupId );
	}
	/**
	 * Set a description of an MVD
	 * @param fileName the name of the file on the file system
	 * @param description the new description for the MVD
	 */
	public void setDescription( String fileName, String description ) 
		throws Exception
	{
		MVD mvd = getFromCache( fileName );
		mvd.setDescription( description );
	}
	/**
	 * Get a group name given its id
	 * @param mvdPath the path to the relevant mvd
	 * @param groupId its groupid
	 * @param groupName the new name for the group
	 */
	public void setGroupName( String mvdPath, short groupId, 
		String groupName ) throws Exception
	{
		MVD mvd = getFromCache( mvdPath );
		mvd.setGroupName( groupId, groupName );
	}
	/**
	 * Get a the group's parent id
	 * @param mvdPath the path to the relevant mvd
	 * @param groupId its groupid
	 * @param parentId the new parent of the group
	 */
	public void setGroupParent( String mvdPath, short groupId, 
		short parentId ) throws Exception
	{
		MVD mvd = getFromCache( mvdPath );
		mvd.setGroupParent( groupId, parentId );
	}
	/**
	 * Set the open status of an MVD's group
	 * @param mvdPath the name of the file on the file system
	 * @param groupId the group to open
	 * @param open the new open status of the group
	 */
	public void setOpen( String mvdPath, short groupId, boolean open ) 
		throws Exception
	{
		MVD mvd = getFromCache( mvdPath );
		mvd.setOpen( groupId, open );
	}
	/**
	 * Get an array of version descriptions from an MVD
	 * @param fileName the name of the file on the file system
	 * @return an array of version descriptions
	 */
	public String[] getVersionDescriptions( String fileName ) 
		throws Exception
	{
		MVD mvd = getFromCache( fileName );
		return mvd.getVersionDescriptions();
	}
	/**
	 * Get an array of version ids for a given group
	 * @param fileName the name of the file on the file system
	 * @return an array of version descriptions
	 */
	public int[] getGroupVersions( String fileName, short group ) 
		throws Exception
	{
		MVD mvd = getFromCache( fileName );
		return mvd.getVersionsForGroup( group );
	}

	/**
	 * Get group descriptions from an MVD
	 * @param fileName the name of the file on the file system
	 * @return an array of group descriptions
	 */
	public String[] getGroupDescriptions( String fileName ) throws Exception
	{
		MVD mvd = getFromCache( fileName );
		return mvd.getGroupDescriptions();
	}
	/**
	 * Get a group name given its id
	 * @param mvdPath the path to the relevant mvd
	 * @param groupId its groupid
	 * @return the current name of the group
	 */
	public String getGroupName( String mvdPath, short groupId ) 
		throws Exception
	{
		MVD mvd = getFromCache( mvdPath );
		return mvd.getGroupName( groupId );
	}
	/**
	 * Get the long name for a version
	 * @param fileName the file name of the MVD
	 * @param versionId its version number
	 * @return its long name
	 */
	public String getLongName( String fileName, short versionId )
		throws Exception
	{
		MVD mvd = getFromCache( fileName );
		return mvd.getLongNameForVersion( versionId );
	}
	/**
	 * Search an MVD for a pattern. Since we can't return more than 
	 * one match over a web-service we will cache them for later 
	 * retrieval if another request for the same pattern is later 
	 * received.  
	 * @param fileName the name of the file on the file system
	 * @param pattern the pattern to search for
	 * @param versions an array of version ids
	 * @param defaultVersion return the text of this version if not 
	 * found
	 * @param multiple true if multiple matches are desired
	 * @return an array of 3 or 1 blocks with various characteristics 
	 * representing ONE or ZERO matches
	 */
	public Chunk[] search( String fileName, byte[] pattern, 
		short[] versions, short defaultVersion, boolean multiple ) 
		throws Exception
	{
		MVD mvd = getFromCache( fileName );
		updateSavedMatches( versions );
		Match[] matches = executeSearch( mvd, pattern, versions, 
			multiple, defaultVersion );
		return matchesToChunks( fileName, matches, defaultVersion );
	}
	/**
	 * Convert an array of matches (or an empty array) to an array of 
	 * Chunks that cover an entire version, namely that version in which 
	 * the match occurs.
	 * @param fileName the mvd file to get the chunks from
	 * @param matches convert the first match in the array, if present
	 * @param defaultVersion the default version to use if no matches 
	 * present. In this case retrieve the entire default version and 
	 * convert that into a chunk.
	 * @return an array of 1-3 chunks
	 */
	private Chunk[] matchesToChunks( String fileName, Match[] matches, 
		short defaultVersion ) throws Exception
	{
		// simple case first
		if ( matches.length == 0 )
			return getVersion( fileName, defaultVersion );
		else
		{
			Chunk[] chunks = getVersion( fileName, matches[0].getVersion() );
			return Chunk.overlay( matches[0], chunks );
		}
	}
	/**
	 * If the user has switched from multi to single version search 
	 * or vice versa, clear the cache of search hits.
	 * @param versions an array of versions to search NOW
	 */
	private void updateSavedMatches( short[] versions )
	{
		boolean multiVersion = cache.getMultiVersion();
		if ( (multiVersion && versions.length == 1) 
			|| (!multiVersion && versions.length > 1) )
		{
			cache.clearMatches();
			cache.setMultiVersion( versions.length > 1 );
		}
	}
	/**
	 * Perform a search and compose the result as an array of chunks, 
	 * containing at most one match
	 * @param mvd the mvd to search
	 * @param pattern the pattern to look for
	 * @param versions an array of version ids
	 * @param multiple true if multiple matches are desired
	 * @param defaultVersion move the hit for this version into the first position
	 * @return an array of size 1 or 0 of matches
	 * @throws Exception if an in/out error occurred
	 */
	private Match[] executeSearch( MVD mvd, byte[] pattern, 
		short[] versions, boolean multiple, short defaultVersion ) 
		throws Exception
	{
		BitSet bs = new BitSet();
		for ( int i=0;i<versions.length;i++ )
			bs.set( versions[i] );
		Match[] matches = cache.getNextMatch( pattern );
		if ( matches.length == 0 )
		{
			matches = mvd.search(pattern,bs,multiple);
			// move default version's match if available to the front
			for ( int j=0,i=0;i<matches.length;i++ )
			{
				if ( matches[i].getVersion() == defaultVersion )
				{
					Match m = matches[j];
					matches[j++] = matches[i];
					matches[i] = m;
				}
			}
			cache.saveMatches( matches, pattern );
			matches = cache.getNextMatch( pattern );
		}
		return matches;
	}
	/**
	 * Compare the two versions of an MVD. Return an array of Chunks 
	 * in which sections of text that are present in u but not in v 
	 * are marked as indicated.
	 * @param fileName the name of the file on the file system
	 * @param u the main version 
	 * @param v the other version to compare u with
	 * @param markAsDeleted mark differences as DELETED, else as INSERTED
	 * @return an array of chunks marked as DELETED or INSERTED
	 */
	public Chunk[] compare( String fileName, short u, short v, 
		boolean markAsDeleted ) throws Exception
	{
		MVD mvd = getFromCache( fileName );
		return mvd.compare( u, v, 
			(markAsDeleted)?ChunkState.deleted:ChunkState.added );
	}
	/**
	 * Search and compare at the same time. This is so we can 
	 * calculate one set of overlapping matches and merge them 
	 * at the same time into an array of chunks. 
	 * @param filePath the name of the file on the file system
	 * @param pattern the pattern to search for
	 * @param versions an array of version ids
	 * @param u the main version to search
	 * @param v the other version to compare u with
	 * @param multiple true if multiple matches are desired
	 * @param markAsDeleted mark differences as DELETED, else as INSERTED
	 * @return an array of blocks with various characteristics 
	 * representing one match and some compare results
	 * @throws Exception if an in/out error occurred
	 */
	public Chunk[] searchAndCompare( String filePath, byte[] pattern, 
		short[] versions, short u, short v, boolean multiple, 
		boolean markAsDeleted ) throws Exception
	{
		MVD mvd = getFromCache( filePath );
		updateSavedMatches( versions );
		Match[] sMatches = executeSearch( mvd, pattern, 
			versions, multiple, u );
		// sMatches is of length 1 or 0.
		// we have to reset the u version here (v is unchanged)
		// because it may have changed during the search
		if ( sMatches.length == 1 )
			u = sMatches[0].getVersion();
		Chunk[] chunks = mvd.compare( u, v, 
			(markAsDeleted)?ChunkState.deleted:ChunkState.added );
		if ( sMatches.length == 1 )
			chunks = Chunk.overlay( sMatches[0], chunks );
		return chunks;
	}
	/**
	 * Get the variants of a particular MVD file
	 * @param fileName path to the MVD
	 * @param base the base version from which to calculate 
	 * the variants
	 * @param offset the starting offset within base where the 
	 * variants 
	 * are to be calculated
	 * @param len the length of the part of base to get the 
	 * variants of
	 * @return an array of variants and their versions. The 
	 * 0th variant is the base version
	 */
	String[] getVariantsOf( String fileName, short base, int offset, 
		int len ) throws Exception
	{
		MVD mvd = getFromCache( fileName );
		Variant[] vars = mvd.getApparatus( base, offset, len );
		String[] array = new String[vars.length];
		for ( int i=0;i<vars.length;i++ )
			array[i] = vars[i].toString();
		return array;
	}
	/**
	 * Retrieve a version from the cached MVD
	 * @param fileName the path to the MVD
	 * @param versionId the id of the version to retrieve
	 * @return the data of the version as a Chunk array (just one 
	 * chunk)
	 */
	public Chunk[] getVersion( String fileName, short versionId ) 
		throws Exception
	{
		// to redo
		MVD mvd = getFromCache( fileName );
		Chunk[] chunks = new Chunk[1];
		ChunkState[] cs = new ChunkState[1];
		cs[0] = ChunkState.none;
		chunks[0] = new Chunk( mvd.getEncoding(), 
			0, cs, mvd.getVersion(versionId), (short)0 );
		chunks[0].setVersion( versionId );
		return chunks;
	}
	/**
	 * Get the shortName of a version from its id
	 * @param mvdPath the path to the mvd
	 * @param id the desired id
	 * @return the short name of the version
	 */
	public String getVersionShortName( String mvdPath, int id ) 
		throws Exception
	{
		MVD mvd = getFromCache( mvdPath );
		return mvd.getVersionShortName( id );
	}
	/**
	 * Merge the given data into the specified MVD
	 * @param fileName the path to the MVD
	 * @param versionId the version to update
	 * @param data the data to use for the merge
	 * @param id of folder to store it in (default 1)
	 * @return percentage of new version that was unique, or 0 
	 * if this was the first version
	 */
	public float update( String fileName, short versionId, 
		int folderId, byte[] data ) throws Exception
	{
		MVD mvd = getFromCache( fileName );
		return mvd.update( versionId, data );
	}
	/**
	 * Save the MVD in the cache. If its not in the cache there's 
	 * nothing to do.
	 * @param fileName the relative path to the file
	 * @param id of folder to store it in (default 1)
	 */
	public void save( String fileName, int folderId ) throws Exception
	{
		if ( cache.containsKey(fileName) )
		{
			MVD mvd = cache.get( fileName );
		}
	}
	/**
	 * Add a new version to the MVD. 
	 * @param fileName the relative path to the MVD file
	 * @param gId the group id
	 * @throws Exception if there was an error
	 */
	public void addVersion( String fileName, short gId ) 
		throws MVDException
	{
		try
		{
			MVD mvd = getFromCache( fileName );
			mvd.addVersion( mvd.numVersions()+1, gId );
		}
		catch ( Exception e )
		{
			throw new MVDException( e );
		}
	}
	/**
	 * Add a new group to the MVD.
	 * @param fileName the relative path to the MVD
	 * @param parentId the id of the parent of gId or 0
	 */
	public void addGroup( String fileName, short parentId ) 
		throws Exception
	{
		try
		{
			MVD mvd = getFromCache( fileName );
			mvd.addGroup( (short)(mvd.numGroups()+1), parentId );
		}
		catch ( Exception e )
		{
			throw new MVDException( e );
		}
	}
	/**
	 * Remove an entire group and its subversions
	 * @param gId the id of the group to remove
	 */
	public void removeGroup( String fileName, short gId ) throws Exception
	{
		MVD mvd = getFromCache( fileName );
		mvd.removeGroup( gId );
	}
	/**
	 * Delete the version from the MVD but don't save it
	 * @param fileName the relative path to the MVD file
	 * @param vId the id of the version to delete
	 * @throws Exception if there was an error
	 */
	public void deleteVersion( String fileName, int vId ) throws Exception
	{
		MVD mvd = getFromCache( fileName );
		mvd.removeVersion( vId );
	}
	/**
	 * Delete an entire MVD
	 * @param fileName the relative path to the MVD file
	 * @throws MVDException if there was an error
	 */
	public void deleteMVD( String fileName ) throws Exception
	{
		cache.remove( fileName );
		File mvdFile = new File( fileName );
		if ( mvdFile.exists() )
			if ( !mvdFile.delete() )
				throw new Exception( "Couldn't delete "+fileName );
	}
	/**
	 * Delete the version from the MVD but don't save it
	 * @param fileName the relative path to the MVD file
	 * @param gId the id of the group to delete
	 * @throws MVDException if there was an error
	 */
	public void deleteGroup( String fileName, short gId ) 
		throws Exception
	{
		try
		{
			MVD mvd = getFromCache( fileName );
			mvd.removeGroup( gId );
		}
		catch ( Exception e )
		{
			throw new MVDException( e );
		}
	}
	/**
	 * Retrieve an MVD from the cache or put it there if it isn't already
	 * @param fileName path to the MVD file
	 * @return the MVD corresponding to the file or a freshly loaded one
	 * @throws an MVDException if the loading failed
	 */
	private MVD getFromCache( String fileName ) throws Exception
	{
		MVD mvd=null;
		try
		{
	    mvd = cache.get( fileName );
		}
		catch ( Exception e )
		{
			throw new MVDException( e );
		}
		return mvd;
	}
	/**
	 * Reread the mvd from its source
	 * @param fileName path to the MVD file
	 * @throws an Exception if the loading failed
	 */
	public void revert( String fileName ) throws Exception
	{
	}
	/**
	 * Find all the groups that belong under the given group
	 * @param fileName the relative path to the mvd
	 * @param groupId the group's current id
	 * @return an array of sub-groups as an array of Strings
	 * @throws Exception if there was an I/O error
	 */
	public String[] getSubGroups( String fileName, short groupId ) 
		throws MVDException
	{
		try
		{
			MVD mvd = getFromCache( fileName );
			return mvd.getSubGroups( groupId );
		}
		catch ( Exception e )
		{
			throw new MVDException( e );
		}
	}
	/** 
	 * Find the overall number of versions
	 * @param mvdPath the path to the MVD
	 * @return the number of versions
	 * @throws Exception if an I/O error occurred
	 */
	public int numVersions( String mvdPath ) throws Exception
	{
		MVD mvd = getFromCache( mvdPath );
		return mvd.numVersions();
	}
	/** 
	 * Find the number of groups currently defined
	 * @param mvdPath the path to the MVD
	 * @return the number of groups
	 * @throws Exception if an I/O error occurred
	 */
	public int numGroups( String mvdPath ) throws Exception
	{
		MVD mvd = getFromCache( mvdPath );
		return mvd.numGroups();
	}
	/**
	 * Get the id of a group previously fetched from the mvd
	 * @param fileName
	 * @param g the group to look for
	 * @return the group's id as a Short or 0 if not found
	 * @throws Exception if an I/O error occurred
	 */
	public Short getGroupId( String fileName, Group g ) 
		throws MVDException
	{
		try
		{
			MVD mvd = getFromCache( fileName );
			return new Short( mvd.getGroupId(g) );
		}
		catch ( Exception e )
		{
			throw new MVDException( e );
		}
	}
	/**
	 * Get an array of versions that belong as direct children 
	 * of a given group
	 * @param fileName the relative path to the mvd
	 * @param groupId the group's current id (index+1)
	 * @return the array of versions under groupId
	 * @throws Exception if an I/O error occurred
	 */
	public String[] getSubVersions( String fileName, short groupId ) 
		throws Exception
	{
		try
		{
			MVD mvd = getFromCache( fileName );
			return mvd.getSubVersions( groupId );
		}
		catch ( Exception e )
		{
			throw new MVDException( e );
		}
	}
	/**
	 * Get the id of the given version previously fetched from 
	 * the mvd
	 * @param v the version to look for
	 * @return the id of the version (its index+1) as an Integer
	 * @throws Exception if an I/O error occurred
	 */
	public Integer getVersionId( String fileName, Version v ) 
		throws Exception
	{
		try
		{
			MVD mvd = getFromCache( fileName );
			return new Integer( mvd.getVersionId(v) );
		}
		catch ( Exception e )
		{
			throw new MVDException( e );
		}
	}
}
