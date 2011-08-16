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

/**
 * <h4>MVD file format</h4>
 * <ul><li>outer wrapper: base64 encoding</li>
 * <li>inner wrapper: zip compression</li></ul>
 * <p>Header:</p>
 * <ul><li>magic string: 8 bytes must be 0xDEADC0DE</li>
 * <li>data mask: 4 bytes 0=NONE, 1=XML, 2=TEXT</li>
 * <li>group-table offset: 4-byte int offset from start of file</li>
 * <li>version-table offset: 4-byte int offset from start of file</li>
 * <li>pairs-table offset: 4-byte int offset from start of file</li>
 * <li>data-table offset: 4-byte int offset from start of file</li>
 * <li>description: 2-byte int preceded utf-8 string</li></ul>
 * <li>encoding: 2-byte int preceded utf-8 string</li></ul>
 * <p>Tables:</p>
 * <ul><li>group-table: number of group-definitions: 2 byte int;<br/>
 * for each group: parent: 2-byte int (0 if a top level group); name: 
 * 2-byte int preceded utf-8 string; id implied by position in the table 
 * starting at 1</li>
 * <li>version-table: number of versions: 2-byte int; version-set size: 
 * 2-byte int;<br/>
 * for each version: group: 2 byte int; shortName: 2-byte int 
 * preceded utf-8 string; longName: 2-byte int preceded utf-8 string; 
 * version numbers implied by position in table starting at 1</li>
 * <li>pairs table: number of pairs: 4-byte int;<br/>
 * for each pair: version-set size bytes, LSB first. first bit of 
 * first byte is hint bit (all other bits refer to versions 1 up to number  
 * of versions); data offset: 4-byte unsigned indexing into data-table; 
 * data len: 4-byte unsigned, first 2 bits forming the transpose flag 
 * 0=DATA,1=CHILD,2=PARENT. If PARENT or CHILD an extra integer containing 
 * the ID of the parent or that of the child's parent.</li>
 * <li>data-table: format: raw bytes</li></ul>
 * <p>all ints are signed big-endian as per Java VM</p>
 */

package au.edu.uq.nmerge.mvd;
import java.sql.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Properties;
import au.edu.uq.nmerge.exception.*;

/**
 *	Load and save an MVD file in binary form. The raw MVD binary 
 *	structure is first ZIP encoded and then Base64 wrapped.
 */
public class MVDFile 
{
	public static boolean debug = false;
	/** magic string '0xC0DEDEAD' */
	static byte[] MVD_MAGIC = {(byte)'\336',(byte)'\255',
		(byte)'\300',(byte)'\336'};
	/**
	 * Write to a Mysql database named mvd which has a table called works 
	 * with fields name, description and data.
	 * @param file the name of the file (key of table)
	 * @param description the description of the mvd
	 * @param data a String with the MVD data encoded in base64
	 * @param folderId id of the containing folder
	 * @param properties property file containing db connection
	 * @throws Exception
	 */
	private static void writeToDatabase( String file, String data, 
		String description, int folderId, Properties props ) throws Exception
	{
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		String username = (String)props.get("username");
		String password = (String)props.get("password");
		String url = (String)props.get("jdbc-url");
		String jdbcClass = (String)props.get("jdbc-class");
		String dbName = (String)props.get("mvd-db-name");
		String nameField = (String)props.get("mvd-name-field");
		String descField = (String)props.get("mvd-desc-field");
		String dataField = (String)props.get("mvd-data-field");
		String tableName = (String)props.get("mvd-table-name");
		String folderField = (String)props.get("mvd-folder-field");
/*		// debug
		Set<Object> keys = props.keySet();
		Iterator<Object> iter = keys.iterator();
		while ( iter.hasNext() )
		{
			String key = (String) iter.next();
			String value = (String)props.getProperty(key);
		}
		// end debug
 */
		String name = (file.endsWith(".mvd"))
			?file.substring(0,file.length()-4)
			:file;
		description = description.replace("\"", "\\\"");
		if ( username == null )
			username = "root";
		if ( password == null )
			password = "jabberw0cky";
		if ( url == null )
			url = "jdbc:mysql://localhost:3306/";
		if ( jdbcClass == null )
			jdbcClass = "com.mysql.jdbc.Driver";
		if ( dbName == null )
			dbName = "dv";
		if ( nameField == null )
			nameField = "name";
		if ( descField == null )
			descField = "description";
		if ( dataField == null )
			dataField = "data";
		if ( tableName == null )
			tableName = "works";
		if ( folderField == null )
			folderField = "folder_id";
		try
        {
            Class.forName(jdbcClass).newInstance();
            conn = DriverManager.getConnection(url+dbName, username, password);
            if ( conn != null )
            {
	            stmt = conn.createStatement();
	            if ( stmt != null )
	            {
	            	String query;
	            	rs = stmt.executeQuery( "select * from "+dbName+"."+tableName
	            		+" where "+nameField+"=\""+name+"\"");
	            	if ( rs.first() )
	            	{
	            		query = "update "+dbName+"."+tableName
	            			+" set "+dbName+"."+tableName+"."+descField+"=\""
	            			+description+"\","+dbName+"."+tableName+"."+dataField+"=\""
	            			+data+"\","+folderField+"=\""+folderId+"\" where "
	            			+nameField+"=\""+name+"\"";
	            	}
	            	else
	            	{
	            		query = "insert into "+dbName+"."+tableName+" ("+nameField+","
	            			+descField+","+dataField+","+folderField+") values(\""
	            			+name+"\",\""+description+"\",\""+data+"\",\""+folderId+"\");";
	            	}
	            	stmt = conn.createStatement();
	            	stmt.executeUpdate( query );
	            }
	            conn.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
           	if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore

                rs = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            if (conn != null)
            {
                 }
                try
                {
                    conn.close ();
                }
                catch (Exception e) { /* ignore close errors */ }
            }
        }
	}
	/**
	 * Write to an actual file
	 * @param dst the location of the file
	 * @param str the string of data to write
	 * @throws Exception
	 */
	private static void writeToFile( File dst, String str ) throws Exception
	{
		if ( dst.exists() )
			dst.delete();
		dst.createNewFile();
		FileWriter fw = new FileWriter( dst );
		fw.write( str );
		fw.close();
	}
	/**
	 * Save an MVD to a file
	 * @param mvd the MVD to save
	 * @param dst the file to save it to
	 * @param rb database properties file
	 * @param folderId id of the folder to contain it in
	 * @throws Exception raised if an error occurred
	 */
	public static void externalise( MVD mvd, File dst, int folderId, 
			Properties rb ) throws Exception
	{
		int size = mvd.dataSize();
		byte[] data = new byte[size];
		int nBytes = mvd.serialise( data );
		assert nBytes==size: "MVD shorter than predicted";
		String str = Base64.encodeBytes( data, Base64.GZIP );
		if ( rb == null )
			writeToFile( dst, str );
		else
			writeToDatabase( dst.getName(), str, mvd.description, folderId, rb );
	}
	/**
	 * Read from an mvd file on disk
	 * @param src the src mvd
	 * @return a char array with the file contents
	 * @throws Exception
	 */
	private static char[] readFromFile( File src ) throws Exception
	{
		char[] data = null;
		FileReader fr = new FileReader( src );
		if ( src.length() <= Integer.MAX_VALUE )
		{
			int len = (int) src.length();
			data = new char[len];
			fr.read( data, 0, data.length );
			fr.close();
		}
		else
			throw new MVDException( "file "+src.toString()+" too big" );
		return data;
	}
	/**
	 * Read from a Mysql database named mvd which has a table called files 
	 * and a field called name and another called contents. Yes, it's rigid 
	 * but do you want to add yet more parameters to the MvdTool?
	 * @param file the name of the file (key of table)
	 * @param props database properties file
	 * @return a char array with database contents
	 * @throws Exception
	 */
	private static char[] readFromDatabase( String file, Properties props ) 
		throws Exception
	{
		char[] data = null;
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		String name = (file.endsWith(".mvd"))
			?file.substring(0,file.length()-4)
			:file;
		String username = (String)props.get("username");
		String password = (String)props.get("password");
		String url = (String)props.get("jdbc-url");
		String jdbcClass = (String)props.get("jdbc-class");
		String dbName = (String)props.get("mvd-db-name");
		String nameField = (String)props.get("mvd-name-field");
		String descField = (String)props.get("mvd-desc-field");
		String dataField = (String)props.get("mvd-data-field");
		String tableName = (String)props.get("mvd-table-name");
		if ( username == null )
			username = "root";
		if ( password == null )
			password = "jabberw0cky";
		if ( url == null )
			url = "jdbc:mysql://localhost:3306/";
		if ( jdbcClass == null )
			jdbcClass = "com.mysql.jdbc.Driver";
		if ( dbName == null )
			dbName = "mvd";
		if ( nameField == null )
			nameField = "name";
		if ( descField == null )
			descField = "description";
		if ( dataField == null )
			dataField = "data";
		if ( tableName == null )
			tableName = "works";
        try
        {
            Class.forName (jdbcClass).newInstance();
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.createStatement();
            String query = "select "+dataField+" from "+dbName+"."+tableName
        		+" where "+nameField+"='"+name+"';";
            rs = stmt.executeQuery( query );
            if ( rs.first() )
            {
            	String result = rs.getString(dataField);
            	data = new char[result.length()];
	            result.getChars( 0, result.length(), data, 0 );
            }
            else
            	throw new Exception("No results for: "+query);
        }
        catch (Exception e)
        {
            System.err.println ("Error accessing database. Message="
            	+e.getMessage()+" with password="+password
            	+" and username="+username );
        }
        finally
        {
           	if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore

                rs = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            if (conn != null)
            {
                 }
                try
                {
                    conn.close ();
                }
                catch (Exception e) { /* ignore close errors */ }
            }
        }
		return data;
	}
	/**
	 * Read an MVD from a file or a database
	 * @param src the file name to read it from
	 * @param props database property file
	 * @throws Exception
	 */
	public static MVD internalise( String src, Properties props ) throws Exception
	{
		MVD mvd = null;
		char[] data = null;
		if ( props == null )
			data = readFromFile( new File(src) );
		else
			data = readFromDatabase( src, props );
		if ( data != null && data.length != 0 )
		{
			byte[] bytes = base64Decode( new String(data) );
			mvd = parse( bytes );
		}
		else
			throw new MVDException( "data is empty");
		return mvd;
	}
	/**
	 *  Decode the base-64 encoded text into a byte array, check the magic 
	 *	string at the start of the file, and decompress it.
	 *  @param base64Data the base64 data to decode
	 *  @return the byte array containing the decoded data.
	 *  @throws an exception if it is not an MVD file
	 */
	private static byte[] base64Decode( String base64Data ) throws Exception
	{
		byte[] data = Base64.decode( base64Data );
		if ( !magicOK(data) )
			throw new MVDException( "Not a valid MVD file" );
		return data;
	}
	/**
	 *	Check that the array as given contains MVD_MAGIC
	 *	as its first 4 bytes.
	 *	@param array an array of raw bytes, unbase64ed and unzipped
	 *	@return true if it has the right magic string, false otherwise
	 */
	private static boolean magicOK( byte[] array )
	{
		for ( int i=0;i<MVD_MAGIC.length;i++ )
			if ( array[i] != MVD_MAGIC[i] )
				return false;
		return true;
	}
	/**
	 * Convert the MVD file format data into an MVD object
	 * @param bytes a byte array of the decompressed file contents
	 * @return a finished MVD
	 * @throws and exception if it is not a valid MVD
	 */
	private static MVD parse( byte[] bytes ) throws Exception
	{
		MVD mvd = null;
		// point after magic
		int p = MVD_MAGIC.length;
		// mask type
		int maskType = readInt( bytes, p );
		p += 4;
		int groupTableOffset = readInt( bytes, p );
		p += 4;
		int versionTableOffset = readInt( bytes, p );
		p += 4;
		int pairsTableOffset = readInt( bytes, p );
		p += 4;
		int dataTableOffset = readInt( bytes, p );
		p += 4;
		short strLen = readShort( bytes, p );
		String description = readUtf8String( bytes, p );
		p += strLen + 2;
		strLen = readShort( bytes, p );
		String encoding = readUtf8String( bytes, p );
		mvd = new MVD( description, encoding );
		mvd.setMask( Mask.values()[maskType] );
		p = groupTableOffset;
		readGroupTable( bytes, p, mvd );
		p = versionTableOffset;
		readVersionTable( bytes, p, mvd );
		p = pairsTableOffset;
		readPairsTable( bytes, p, dataTableOffset, mvd );
		int i = 0;
		try
		{
			for ( i=0;i<mvd.pairs.size();i++ )
			{
				Pair z = mvd.pairs.get( i );
				z.verify();
			}
		}
		catch ( Exception e )
		{
			System.out.println("Index:"+i+" "+e.getMessage());
		}
		return mvd;
	}
	/**
	 * Read the group table for an MVD from a byte array
	 * @param data the byte array containing the group definitions
	 * @param p the start offset of the groups within data
	 * @param mvd an mvd to add the group definitions to
	 */
	private static void readGroupTable( byte[] data, int p, MVD mvd )
		throws Exception
	{
		short nGroups = readShort( data, p );
		p += 2;
		if ( nGroups < 0 )
			throw new MVDException( 
				"Invalid number of groups: "+nGroups ); 
		for ( short i=0;i<nGroups;i++ )
		{
			short parent = readShort( data, p );
			p += 2;
			short len = readShort( data, p );
			String name = readUtf8String( data, p );
			p += 2 + len;
			mvd.addGroup( new Group(parent, name) );
		}
	}
	/**
	 * Read the version table for an MVD from a byte array
	 * @param data the byte array containing the version definitions
	 * @param p the start offset of the versions within data
	 * @param mvd an mvd to add the version definitions to
	 */
	private static void readVersionTable( byte[] data, int p, MVD mvd )
		throws Exception
	{
		short nVersions = readShort( data, p );
		p += 2;
		if ( nVersions < 0 )
			throw new MVDException( 
				"Invalid number of versions: "+nVersions ); 
		short setSize = readShort( data, p );
		p += 2;
		mvd.setVersionSetSize( setSize );
		for ( short i=0;i<nVersions;i++ )
		{
			short group = readShort( data, p );
			p += 2;
			short backup = readShort( data, p );
			p += 2;
			short len = readShort( data, p );
			String shortName = readUtf8String( data, p );
			p += 2 + len;
			len = readShort( data, p );
			String longName = readUtf8String( data, p );
			p += 2 + len;
			mvd.addVersion( new Version(group, backup, shortName, longName) );
		}
	}
	/**
	 * Read the pairs table for an MVD from a byte array
	 * @param data the byte array containing the version definitions
	 * @param p the start offset of the versions within data
	 * @param dataTableOffset offset within data of the pairs data 
	 * @param mvd an mvd to add the version definitions to
	 */
	private static void readPairsTable( byte[] data, int p, 
		int dataTableOffset, MVD mvd ) throws Exception
	{
		// record any pairs declaring themselves as parents
		HashMap<Integer,Pair> parents = new HashMap<Integer,Pair>();
		HashMap<Integer,LinkedList<Pair>> orphans = 
			new HashMap<Integer,LinkedList<Pair>>();
		int nPairs = readInt( data, p );
		p += 4;
		if ( nPairs < 0 )
			throw new MVDException( 
				"Invalid number of pairs: "+nPairs ); 
		for ( int i=0;i<nPairs;i++ )
		{
			byte[] copy;
			Pair pair;
			BitSet versions = readVersionSet( mvd.versionSetSize, 
				data, p );
			p += mvd.versionSetSize;
			int offset = readInt( data, p );
			p += 4;
			int len = readInt( data, p );
			int flag = len & Pair.TRANSPOSE_MASK;
			// clear top two bits
			len &= Pair.INVERSE_MASK;
			p += 4;
			if ( flag == Pair.PARENT_FLAG )
			{
				// read special parent id field 
				int pId = readInt( data, p );
				p += 4;
				// transpose parent
				copy = copyData( len, dataTableOffset+offset, data );
				pair = new Pair( versions, copy );
				Integer key = new Integer( pId );
				// check for orphans of this parent
				LinkedList<Pair> children = orphans.get( key );
				if ( children != null )
				{
					ListIterator<Pair> iter = children.listIterator();
					// match them up with this parent
					while ( iter.hasNext() )
					{
						Pair child = iter.next();
						child.setParent( pair );
						pair.addChild( child );
					}
					// now they're not orphans any more
					orphans.remove( key );
				}
				// always do this in case more children turn up
				parents.put( key, pair );
			}
			else if ( flag == Pair.CHILD_FLAG )
			{
				// read special parent id field 
				int pId = readInt( data, p );
				p += 4;
				// transpose child
				Integer key = new Integer( pId );
				Pair parent = parents.get( key );
				if ( parent == null )
				{
					LinkedList<Pair> children = orphans.get( key );
					if ( children == null )
					{
						children = new LinkedList<Pair>();
						orphans.put( key, children );
					}
					pair = new Pair( versions, null );
					children.add( pair );
				}
				else	// parent available
				{
					pair = new Pair( versions, null );
					pair.setParent( parent );
					parent.addChild( pair );
				}
			}
			else
			{
				// no transposition
				copy = copyData( len, dataTableOffset+offset, data );
				pair = new Pair( versions, copy );
			}
			mvd.addPair( pair );
		}
	}
	/**
	 * Read a version set MSB first and convert it to a BitSet
	 * @param versionSetSize number of bytes in the BitSet
	 * @param data the data to read it from
	 * @param p offset within data to start
	 * @return the finished BitSet
	 */
	private static BitSet readVersionSet( int versionSetSize, 
		byte[] data, int p )
	{
		BitSet versions = new BitSet( versionSetSize*8 );
		p += versionSetSize-1;
		for ( int j=0;j<versionSetSize;j++,p-- )
		{
			byte mask = (byte)1;
			for ( int k=0;k<8;k++ )
			{
				if ( (mask & data[p]) != 0 )
					versions.set( k+(j*8) );
				mask <<= 1;
			}
		}
		return versions;
	}
	/**
	 * Make a copy of a portion of a larger byte array
	 * @param len the length of the copy
	 * @param offset the offset within data where the copy begins
	 * @param data the larger byte array from which to copy
	 * @return a copy of a section off the data array
	 */
	private static byte[] copyData( int len, int offset, byte[] data )
	{
		byte[] raw = new byte[len];
		for ( int j=0,q=offset;j<len;j++,q++ )
			raw[j] = data[q];
		return raw;
	}
	/**
	 * Read a 2-byte integer from an array of bytes in big-endian order
	 * @param data an array of bytes
	 * @param p offset into data to begin
	 * @return the int read from data
	 */
	private static short readShort( byte[] data, int p ) throws 
		NumberFormatException
	{
		short x = 0;
		if ( p+1 < data.length )
		{
			for ( int i=p;i<p+2;i++ )
			{
				x <<= 8;
				x = data[i];
			}
		}
		else
			throw new NumberFormatException(
				"ran off the end of MVD file while reading short");
		return x;
	}
	/**
	 * Read a 4-byte integer from an array of bytes in big-endian order
	 * @param data an array of bytes
	 * @param p offset into data to begin
	 * @return the int read from data
	 */
	private static int readInt( byte[] data, int p ) throws 
		NumberFormatException
	{
		int x = 0;
		if ( p+3 < data.length )
		{
			for ( int i=p;i<p+4;i++ )
			{
				x <<= 8;
				x |= 0xFF & data[i];
			}
		}
		else
			throw new NumberFormatException(
				"ran off the end of MVD file while reading int");
		return x;
	}
	/**
	 * Read a 2-byte preceded UTF-8 string from an array of data bytes
	 * @param data the data to read from
	 * @param p the offset of the string start
	 * @return a finished Java String
	 * @throws an exception if we index outside the array or the string 
	 * is not valid UTF-8
	 */
	private static String readUtf8String( byte[] data, int p ) 
		throws IndexOutOfBoundsException, UnsupportedEncodingException
	{
		short len = readShort( data, p );
		p += 2;
		byte[] str = new byte[len];
		for ( int i=0;i<len;i++ )
		{
			str[i] = data[p+i];
		}
		return new String( str, "UTF-8" );
	}
}
