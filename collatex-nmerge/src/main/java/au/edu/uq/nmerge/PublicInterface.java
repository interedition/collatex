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
				Collation collation = (description==null||description.length()==0)
					?new Collation():new Collation(description);
				collation.setMask(mvdMask);
				collation.setEncoding(encoding);
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
	 * Get a description from an MVD
	 * @param fileName the name of the file on the file system
	 * @return the description string from the MVD
	 */
	public String getDescription( String fileName ) throws Exception
	{
		Collation collation = getFromCache( fileName );
		return collation.getDescription();
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
			Collation collation = getFromCache( fileName );
			return collation.getEncoding();
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
		Collation collation = getFromCache( mvdPath );
		collation.setVersionShortName(versionId, shortName);
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
		Collation collation = getFromCache( mvdPath );
		collation.setVersionLongName(versionId, longName);
	}

	/**
	 * Set a description of an MVD
	 * @param fileName the name of the file on the file system
	 * @param description the new description for the MVD
	 */
	public void setDescription( String fileName, String description ) 
		throws Exception
	{
		Collation collation = getFromCache( fileName );
		collation.setDescription( description );
	}

	/**
	 * Get an array of version descriptions from an MVD
	 * @param fileName the name of the file on the file system
	 * @return an array of version descriptions
	 */
	public String[] getVersionDescriptions( String fileName ) 
		throws Exception
	{
		Collation collation = getFromCache( fileName );
		return collation.getVersionDescriptions();
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
		Collation collation = getFromCache( fileName );
		return collation.getLongNameForVersion( versionId );
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
		Collation collation = getFromCache( fileName );
		updateSavedMatches( versions );
		Hit[] hits = executeSearch(collation, pattern, versions,
			multiple, defaultVersion );
		return matchesToChunks( fileName, hits, defaultVersion );
	}
	/**
	 * Convert an array of matches (or an empty array) to an array of 
	 * Chunks that cover an entire version, namely that version in which 
	 * the match occurs.
	 * @param fileName the mvd file to get the chunks from
	 * @param hits convert the first match in the array, if present
	 * @param defaultVersion the default version to use if no matches 
	 * present. In this case retrieve the entire default version and 
	 * convert that into a chunk.
	 * @return an array of 1-3 chunks
	 */
	private Chunk[] matchesToChunks( String fileName, Hit[] hits,
		short defaultVersion ) throws Exception
	{
		// simple case first
		if ( hits.length == 0 )
			return getVersion( fileName, defaultVersion );
		else
		{
			Chunk[] chunks = getVersion( fileName, hits[0].getVersion() );
			return Chunk.overlay( hits[0], chunks );
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
	 * @param collation the mvd to search
	 * @param pattern the pattern to look for
	 * @param versions an array of version ids
	 * @param multiple true if multiple matches are desired
	 * @param defaultVersion move the hit for this version into the first position
	 * @return an array of size 1 or 0 of matches
	 * @throws Exception if an in/out error occurred
	 */
	private Hit[] executeSearch( Collation collation, byte[] pattern,
		short[] versions, boolean multiple, short defaultVersion ) 
		throws Exception
	{
		BitSet bs = new BitSet();
		for ( int i=0;i<versions.length;i++ )
			bs.set( versions[i] );
		Hit[] hits = cache.getNextMatch( pattern );
		if ( hits.length == 0 )
		{
			hits = collation.search(pattern,bs,multiple);
			// move default version's match if available to the front
			for ( int j=0,i=0;i< hits.length;i++ )
			{
				if ( hits[i].getVersion() == defaultVersion )
				{
					Hit m = hits[j];
					hits[j++] = hits[i];
					hits[i] = m;
				}
			}
			cache.saveMatches(hits, pattern );
			hits = cache.getNextMatch( pattern );
		}
		return hits;
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
		Collation collation = getFromCache( fileName );
		return collation.compare( u, v,
			(markAsDeleted)?ChunkState.DELETED :ChunkState.ADDED);
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
		Collation collation = getFromCache( filePath );
		updateSavedMatches( versions );
		Hit[] searchHits = executeSearch(collation, pattern,
			versions, multiple, u );
		// sMatches is of length 1 or 0.
		// we have to reset the u version here (v is unchanged)
		// because it may have changed during the search
		if ( searchHits.length == 1 )
			u = searchHits[0].getVersion();
		Chunk[] chunks = collation.compare( u, v,
			(markAsDeleted)?ChunkState.DELETED :ChunkState.ADDED);
		if ( searchHits.length == 1 )
			chunks = Chunk.overlay( searchHits[0], chunks );
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
		Collation collation = getFromCache( fileName );
		Variant[] vars = collation.getApparatus( base, offset, len );
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
		Collation collation = getFromCache( fileName );
		Chunk[] chunks = new Chunk[1];
		ChunkState[] cs = new ChunkState[1];
		cs[0] = ChunkState.NONE;
		chunks[0] = new Chunk( collation.getEncoding(), 0, cs, collation.getVersion(versionId) );
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
		Collation collation = getFromCache( mvdPath );
		return collation.getVersionShortName( id );
	}
	/**
	 * Merge the given data into the specified MVD
	 * @param fileName the path to the MVD
	 * @param versionId the version to update
	 * @param data the data to use for the merge
	 * @param folderId of folder to store it in (default 1)
	 * @return percentage of new version that was unique, or 0 
	 * if this was the first version
	 */
	public float update( String fileName, short versionId, 
		int folderId, byte[] data ) throws Exception
	{
		Collation collation = getFromCache( fileName );
		return collation.update( versionId, data );
	}
	/**
	 * Save the MVD in the cache. If its not in the cache there's 
	 * nothing to do.
	 * @param fileName the relative path to the file
	 * @param folderId of folder to store it in (default 1)
	 */
	public void save( String fileName, int folderId ) throws Exception
	{
	}
	/**
	 * Add a new version to the MVD. 
	 * @param fileName the relative path to the MVD file
	 * @throws Exception if there was an error
	 */
	public void addVersion( String fileName )
		throws MVDException
	{
		try
		{
			Collation collation = getFromCache( fileName );
			collation.addVersion( collation.numVersions()+1 );
		}
		catch ( Exception e )
		{
			throw new MVDException( e );
		}
	}

	/**
	 * Delete the version from the MVD but don't save it
	 * @param fileName the relative path to the MVD file
	 * @param vId the id of the version to delete
	 * @throws Exception if there was an error
	 */
	public void deleteVersion( String fileName, int vId ) throws Exception
	{
		Collation collation = getFromCache( fileName );
		collation.removeVersion( vId );
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
	 * Retrieve an MVD from the cache or put it there if it isn't already
	 * @param fileName path to the MVD file
	 * @return the MVD corresponding to the file or a freshly loaded one
	 * @throws Exception if the loading failed
	 */
	private Collation getFromCache( String fileName ) throws Exception
	{
		Collation collation =null;
		try
		{
	    collation = cache.get( fileName );
		}
		catch ( Exception e )
		{
			throw new MVDException( e );
		}
		return collation;
	}
	/**
	 * Reread the mvd from its source
	 * @param fileName path to the MVD file
	 * @throws Exception if the loading failed
	 */
	public void revert( String fileName ) throws Exception
	{
	}

	/**
	 * Find the overall number of versions
	 * @param mvdPath the path to the MVD
	 * @return the number of versions
	 * @throws Exception if an I/O error occurred
	 */
	public int numVersions( String mvdPath ) throws Exception
	{
		Collation collation = getFromCache( mvdPath );
		return collation.numVersions();
	}

	/**
	 * Get the id of the given version previously fetched from 
	 * the mvd
	 * @param v the version to look for
	 * @return the id of the version (its index+1) as an Integer
	 * @throws Exception if an I/O error occurred
	 */
	public Integer getVersionId( String fileName, Witness v )
		throws Exception
	{
		try
		{
			Collation collation = getFromCache( fileName );
			return new Integer( collation.getVersionId(v) );
		}
		catch ( Exception e )
		{
			throw new MVDException( e );
		}
	}
}
