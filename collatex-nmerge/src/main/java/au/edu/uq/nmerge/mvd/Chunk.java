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
import java.nio.charset.Charset;
import java.util.Vector;
/**
 * Represent a piece of a version that has some characteristics, 
 * e.g. a run of text not found in a previous or other version.
 * This could be displayed in another colour or highlighted as 
 * the result of a search etc.
 * @author Desmond Schmidt 19/9/07
 */
public class Chunk extends BracketedData
{
	ChunkStateSet states;
	/** after search say what version the Chunk belongs to */
	short version;
	/** parent, child or merge id or 0, unique for each version */
	int id;
	/** global chunk id */
	static int chunkId;
	/**
	 * Basic constructor for a chunk. Add states versions and ids later.
	 * @param encoding the encoding of the data
	 * @param backup the backup version or NO_BACKUP
	 */
	public Chunk( String encoding, short backup )
	{
		super( encoding );
		this.states = new ChunkStateSet( backup );
	}
	/**
	 * Create a new Chunk
	 * @param encoding the encoding of the data
	 * @param id the parent or child id or 0
	 * @param cs an initial set of chunk states
	 * @param data the data to add
	 * @param backup the backup version or NO_BACKUP
	 */
	public Chunk( String encoding, int id, ChunkState[] cs, 
		byte[] data, short backup )
	{
		super( encoding, data );
		this.states = new ChunkStateSet( cs, backup );
		this.id = id;
	}
	/**
	 * Create a Chunk from a byte array where the chunks are marked 
	 * up using the [statelist:id:version:...] syntax. "statelist" is 
	 * a list of comma-separated chunk states and ":id" is used to 
	 * link parent and child chunks. Square brackets in the text 
	 * (the "..." are escaped using backslashes. Real backslashes are 
	 * double-backslashes. version is only used with the found chunk 
	 * state and comes after the id.
	 * @param chunkData should contain escaped ']'s
	 * @param pos position in the byte array to read from
	 * @param backup the backup version
	 */
	public Chunk( byte[] chunkData, int pos, short backup )
	{
		super( Charset.defaultCharset().toString() );
		int start = pos;
		states = new ChunkStateSet( backup );
		while ( chunkData[pos] != '[' )
			pos++;
		// point to first char after '['
		pos++;
		pos += readHeader( chunkData, pos );
		pos += readData( chunkData, pos );
		srcLen = pos - start;
	}
	/**
	 * Add a state to the set
	 * @param state the state to add
	 */
	public void addState( ChunkState state )
	{
		if ( state != ChunkState.none )
			states.add( state );
	}
	/**
	 * Get the states of a chunk
	 * @return an array of chunk states
	 */
	public ChunkStateSet getStates()
	{
		return states;
	}
	/**
	 * Set the version. Only valid if it is already a found state.
	 * @param version the version the state belongs to
	 */
	public void setVersion( short version )
	{
		this.version = version;
	}
	/**
	 * Overlay a match onto an array of chunks. Return the modified 
	 * chunk array.
	 * @param match the match to overlay
	 * @param chunks an array of chunks
	 * @return an array of chunks with the match incorporated
	 */
	public static Chunk[] overlay( Match match, Chunk[] chunks )
	{
		int begin = 0;
		int matchStart = match.offset;
		int matchEnd = match.offset+match.length;
		Vector<Chunk> newChunks = new Vector<Chunk>();
		for ( int i=0;i<chunks.length;i++ )
		{
			Chunk current = chunks[i];
			// 1. current overlaps match on the left
			if ( matchStart < begin+current.getLength() 
				&& matchStart > begin )
			{
				Chunk[] parts = current.split( matchStart-begin );
				newChunks.add( parts[0] );
				begin += parts[0].getLength();
				current = parts[1];
			}
			// 2. current overlaps match on the right
			if ( matchEnd < begin+current.getLength()
				&& matchEnd > begin )
			{
				Chunk[] parts = current.split( matchEnd-begin );
				parts[0].addState( match.state );
				newChunks.add( parts[0] );
				begin += parts[0].getLength();
				current = parts[1];
			}
			// 3. match completely overlaps current 
			if ( matchStart <= begin && matchEnd 
				>= begin+current.getLength() )
			{
				current.addState( match.state );
				current.version = match.getVersion();
				begin += current.getLength();
				newChunks.add( current );
			}
			// 4. match doesn't overlap current at all
			else /*if ( matchEnd <= begin || matchStart 
				> begin+current.getLength() )*/
			{
				newChunks.add( current );
				begin += current.getLength();
			}
		}
		Chunk[] array = new Chunk[newChunks.size()];
		return newChunks.toArray( array );
	}
	/**
	 * Split a chunk into two halves at a single point
	 * @param offset the point within the chunk to split
	 * @return an array of 2 chunks
	 */
	Chunk[] split( int offset )
	{
		// make the two data arrays
		byte[] first = new byte[offset];
		for ( int j=0;j<offset;j++ )
			first[j] = realData[j];
		byte[] second = new byte[realData.length-offset];
		for ( int i=0,j=offset;j<realData.length;j++,i++ )
			second[i] = realData[j];
		Chunk[] parts = new Chunk[2];
		// duplicate ids: this doesn't matter for chunks
		parts[0] = new Chunk( encoding, id, states.getStates(), 
			first, states.getBackup() );
		parts[0].version = this.version;
		parts[1] = new Chunk( encoding, id, 
			new ChunkStateSet(states).getStates(), 
			second, states.getBackup() );
		parts[1].version = this.version;
		return parts;
	}
	/**
	 * Set the id. 
	 * @param id the id of the state. If 0 no exception is raised
	 */
	public void setId( int id )
	{
		this.id = id;
	}
	/**
	 * Get the id of this parent/child (or neither)
	 * @return an int parent/child id
	 */
	public int getId()
	{
		return id;
	}
	/**
	 * Read the version
	 * @param chunkData the data to read from
	 * @param pos the offset to start reading from
	 * @return the number of bytes consumed
	 */
	private int readVersion( byte[] chunkData, int pos )
	{
		int start = pos;
		while ( Character.isDigit((char)chunkData[pos]) )
			pos++;
		int len = pos - start;
		version = Short.parseShort( new String(chunkData,start,len) );
		return len;
	}
	/**
	 * Read the id
	 * @param chunkData the data to read from
	 * @param pos the offset to start reading from
	 * @return the number of bytes consumed
	 */
	private int readId( byte[] chunkData, int pos )
	{
		int start = pos;
		while ( Character.isDigit((char)chunkData[pos]) )
			pos++;
		int len = pos - start;
		id = Integer.parseInt( new String(chunkData,start,len) );
		return len;
	}
	/**
	 * Read the chunk header, including the state list, the id and 
	 * version if present, saving them as instance vars
	 * @param chunkData the data to read the states from
	 * @param pos the first byte pos for the state name
	 * @return the number of bytes read
	 */
	private int readHeader( byte[] chunkData, int pos )
	{
		int start = pos;
		pos += readStates( chunkData, pos );
		// read id and version
		if ( chunkData[pos] == ':' &&(states.isParent() || states.isChild()) )
		{
			pos += 1;	// no, ++pos doesn't work
			pos += readId( chunkData, pos );
		}
		if ( chunkData[pos] == ':' && states.isFound() )
		{
			pos += 1;
			pos += readVersion( chunkData, pos );
		}
		return ++pos - start;
	}
	/**
	 * Read a chunk's list of states
	 * @param chunkData the data to parse
	 * @param pos the starting offset within chunkData
	 * @return the number of bytes of chunkData consumed
	 */
	private int readStates( byte[] chunkData, int pos )
	{
		int start = pos;
		while ( chunkData[pos] != ':' )
		{
			pos += readState( chunkData, pos );
			if ( chunkData[pos] == ',' )
				pos++;
		}
		return pos - start;
	}
	/**
	 * Read a single chunk state. Leave pos pointing to a : or ,
	 * @param chunkData the data to parse
	 * @param pos the starting offset within chunkData
	 * @return the number of bytes of chunkData consumed
	 */
	private int readState( byte[] chunkData, int pos )
	{
		int start = pos;
		while ( chunkData[pos] != ':' && chunkData[pos] != ',' )
			pos++;
		String stateName = new String( chunkData, start, pos-start );
		ChunkState state = ChunkState.valueOf( stateName );
		// install new state in states array
		addState( state );	
		return pos - start;
	}
	/**
	 * Get the length of the source data used to construct this 
	 * chunk
	 * @return the src data len in bytes
	 */
	public int getSrcLen()
	{
		return srcLen;
	}
	/**
	 * Get the actual data represented by this Chunk
	 * @return the data
	 */
	public byte[] getData() 
	{
		return realData;
	}
	/**
	 * Get the version of this chunk.
	 * @return the version given when the chunk was created
	 */
	public short getVersion()
	{
		return version;
	}
	/**
	 * Get the length of a Chunk's actual data
	 * @return the chunk's length
	 */
	public int getLength()
	{
		return (realData==null)?0:realData.length;
	}
	/**
	 * Convert a chunk into a string for debugging
	 * @return a String representation of the contents plus the state
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		{
			sb.append( createHeader() );
			try
			{
				sb.append( new String(realData,encoding) );
			}
			catch ( Exception e )
			{
				// this won't happen
			}
			sb.append( "]" );
		}
		return sb.toString();
	}
	/**
	 * Create a String representing the header part of a chunk
	 * @return the header as a String including the trailing ':'
	 */
	protected String createHeader()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( "[" );
		if ( states != null )
			sb.append( states.toString() );
		if ( id != 0 )
			sb.append( ":"+Integer.toString(id) );
		if ( version != 0 )
			sb.append( ":"+Integer.toString(version) );
		sb.append( ":" );
		return sb.toString();
	}
}
