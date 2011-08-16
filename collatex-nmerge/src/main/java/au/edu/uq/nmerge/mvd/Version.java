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
import au.edu.uq.nmerge.exception.*;
import java.io.UnsupportedEncodingException;
/**
 * Definition of a version. This is not the same as the versions of 
 * a Pair - that records WHICH versions the pair belongs to. This 
 * class records what a particular Version is.
 */
public class Version
{
	static final long serialVersionUID = 1;
	/** parent group in the version hierarchy */
	public short group;
	/** the backup version is returned whenever this version 
	 * is requested but is not available */
	public short backup;
	/** siglum or other short name e.g. A */
	public String shortName;
	/** full description if desired: e.g. Codex Vaticanus 1234 */
	public String longName;
	/** size of version in bytes */
	int versionSize;
	public static short NO_BACKUP = 0;
	/**
	 * Create an instance of Version
	 * @param group the parent group - can be nested
	 * @param backup the backup version or NO_BACKUP
	 * @param shortName siglum or other short name
	 * @param longName longer name if desired
	 */
	public Version( short group, short backup, String shortName, 
		String longName )
	{
		this.shortName = shortName;
		this.longName = longName;
		this.group = group;
		this.backup = backup;
	}
	/**
	 * Return the size of this Group object
	 * @return the size in bytes
	 */
	int dataSize() throws UnsupportedEncodingException
	{
		if ( versionSize == 0 )
		{
			byte[] snBytes = shortName.getBytes( "UTF-8" );
			byte[] lnBytes = longName.getBytes( "UTF-8" );
			versionSize = 2 + 2 + 2 + 2 + snBytes.length 
				+ lnBytes.length;
		}
		return versionSize;
	}

	/**
	 * Get the backup version whose content is returned whenever  
	 * this version is not present
	 * @return the backup version ID or NO_BACKUP
	 */
	public short getBackup()
	{
		return backup;
	}
	/**
	 * Is this version partial?
	 * @return true if it is, false otherwise
	 */
	public boolean isPartial()
	{
		return backup != NO_BACKUP;
	}
	/**
	 * Replace quotation marks with \"
	 * @param input the input string
	 * @return the quotation-escaped string
	 */
	private String escape( String input )
	{
		StringBuffer sb = new StringBuffer();
		for ( int i=0;i<input.length();i++ )
			if ( input.charAt(i)=='"' )
				sb.append( "\"" );
			else
				sb.append( input.charAt(i) );
		return sb.toString();
	}
	/**
	 * Convert this version to a String
	 * @param indent the amount to indent the version XML
	 * @param id the id of the version
	 * @return the version in an XML format
	 */
	String toXML( int indent, int id )
	{
		StringBuffer sb = new StringBuffer();
		for ( int i=0;i<indent;i++ )
			sb.append( " " );
		sb.append( "<version id=\""+id+"\" backup=\""+backup
			+"\" shortName=\""
			+shortName+"\" longName=\""+longName+"\"/>\n" );
		return sb.toString();
	}
	/**
	 * Convert a Version to a string for debugging
	 * @return a human-readable string Version
	 */
	public String toString()
	{
		return "shortName:"+shortName+";longName:"+longName
			+";group:"+group+";backup:"+backup;
	}
}
