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

import java.util.Vector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.NodeList;

/**
 * Read and write and represent a guide file for MVD archives
 * @author Desmond Schmidt 9/5/09
 */
public class XMLGuideFile 
{
	/** fixed name for groups element */
	static String GROUPS = "groups";
	/** fixed name for versions element */
	static String VERSIONS = "versions";
	/** fixed name for single version element */
	static String VERSION = "version";
	/** fixed name for single group element or attribute */
	static String GROUP = "group";
	/** fixed name for NAME attribute */
	static String NAME = "name";
	/** fixed name for parent attribute */
	static String PARENT = "parent";
	/** fixed name for parent attribute */
	static String BACKUP = "backup";
	/** fixed name for parent attribute */
	static String SHORTNAME = "shortname";	
	/** fixed name for parent attribute */
	static String LONGNAME = "longname";
	/** fixed name for DESCRIPTION element */
	static String DESCRIPTION = "description";
	/** fixed name for root element */
	static String MVDGUIDE = "mvdguide";
	/** name of guide file */
	public static String GUIDE_FILE = "guide.xml";
	/** description of the MVD */
	String description;
	/** nested groups of the MVD; id = position in table+1 */
	Vector<Group> groups;		// 
	/** versions of the MVD; id = position in table+1 */
	Vector<VersionInfo> versions;
	/**
	 * Our own private version info structure
	 */
	public class VersionInfo extends Version
	{
		private static final long serialVersionUID = 1;
		/** name of file containing version data inside the archive folder */
		String file;
		/**
		 * Construct a version info object. Note the only difference 
		 * is the file name
		 * @param group the group id
		 * @param backup the backup id or 0 for no backup (and not then partial)
		 * @param shortName short name for version
		 * @param longName long name for version
		 * @param file the file name in the folder containing the version
		 */
		VersionInfo( short group, short backup, String shortName, 
			String longName, String file )
		{
			super( group, backup, shortName, longName );
			this.file = file;
		}
		/**
		 * We will have to do this when serialising the file out 
		 * during archiving
		 * @param file the name of the file corresponding to the version
		 */
		void setFile( String file )
		{
			this.file = file;
		}
	}
	/**
	 * Create an empty XMLGuideFile
	 */
	private XMLGuideFile()
	{
		groups = new Vector<Group>();
		versions = new Vector<VersionInfo>();
	}
	/**
	 * Create a guide file from the information in an MVD. We make deep
	 * copies of the data because we have to represent it independently.
	 * @param mvd the source mvd from which to copy version and group data 
	 * and the description
	 */
	public XMLGuideFile( MVD mvd )
	{
		this.description = new String( mvd.getDescription() );
		this.groups = new Vector<Group>();
		this.versions = new Vector<VersionInfo>();
		// make deep copy of groups
		for ( int i=0;i<mvd.groups.size();i++ )
		{
			Group g = mvd.groups.get( i );
			Group h = new Group( g.parent, new String(g.name) );
			groups.add( h );
		}
		// make deep copy of versions
		for ( int i=0;i<mvd.versions.size();i++ )
		{
			Version v = mvd.versions.get( i );
			VersionInfo w = new VersionInfo( v.group, v.backup, 
				new String(v.shortName), new String(v.longName), "" );
			versions.add( w );
		}
	}
	/**
	 * Get the version information for a given version
	 * @param vId the version id
	 * @return the version info
	 */
	public VersionInfo getVersionInfo( short vId ) throws MVDToolException
	{
		if ( vId-1 < 0 || vId-1 > versions.size() )
			throw new MVDToolException("Invalid version id "+vId );
		VersionInfo vi = versions.get( vId-1 );
		return vi;
	}
	/**
	 * Get the number of versions stored in this archive
	 * @return the number of versions
	 */
	public int numVersions()
	{
		return versions.size();
	}
	/**
	 * Get the name of a group
	 * @param gId the group id
	 * @return the group name
	 */
	public String getGroupName( short gId ) throws MVDToolException
	{
		if ( gId-1 < 0 || gId-1 > groups.size() )
			throw new MVDToolException("Invalid group id "+gId );
		Group g = groups.get( gId-1 );
		return g.name;
	}
	/**
	 * Set the file name for the version just written out
	 * @param vId the id of the version
	 * @param file the name of the file
	 */
	public void setVersionFile( short vId, String file ) 
		throws MVDToolException
	{
		if ( vId-1 < 0 || vId-1 > versions.size() )
			throw new MVDToolException("Invalid version id "+vId );
		VersionInfo vi = versions.get( vId-1 );
		vi.setFile( file );
	}
	/**
	 * Get the names of all versions in the archive
	 * @return an array of version file names in the archive folder
	 */
	public String[] getVersionFileNames()
	{
		String[] files = new String[versions.size()];
		for ( int i=0;i<files.length;i++ )
		{
			VersionInfo vi = versions.get( i );
			files[i] = vi.file;
		}
		return files;
	}
	/**
	 * Get the description of the MVD
	 * @return a String
	 */
	public String getDescription()
	{
		return description;
	}
	/**
	 * Write an MVDGuideFile out as XML.
	 * @param dst the destination guide XML file
	 * @throws an exception if it couldn't write the file or find a 
	 * serialiser
	 */
	public void externalise( File dst ) throws MVDException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try 
		{
			DocumentBuilder db = dbf.newDocumentBuilder();
			// create an instance of DOM
			Document dom = db.newDocument();
			Element rootElement = dom.createElement( MVDGUIDE );
			Element descElement = dom.createElement( DESCRIPTION );
			descElement.setTextContent( description );
			dom.appendChild( rootElement );
			rootElement.appendChild( descElement );
			writeGroups( dom, rootElement );
			writeVersions( dom, rootElement );
			// print it out manually since DOM serialisation escapes CRLF  
			// as &13;LF on the Mac and this behaviour can't be turned off. 
			// We need a portable externalise routine that works the same 
			// on all platforms
			FileOutputStream fos = new FileOutputStream( dst );
			XMLPretty.writeXMLDeclaration( fos, "UTF-8" );
			XMLPretty.writeNode( dom.getFirstChild(), fos, "UTF-8", true, 0 );
			fos.close();
		}
		catch ( Exception ioe )
		{
			throw new MVDException("Failure to write MVD XML file "+ioe );
		}
	}
	/**
	 * Write the groups out
	 * @param dom the document to add them to
	 * @param parent the parent element to add them to
	 */
	private void writeGroups( Document dom, Element parent )
	{
		Element groupParent = dom.createElement( GROUPS );
		for ( short i=0;i<groups.size();i++ )
		{
			Group g = groups.get( i );
			Element gElement = dom.createElement( GROUP );
			gElement.setAttribute( PARENT, Short.toString(g.parent) );
			gElement.setAttribute( NAME, g.name );
			groupParent.appendChild( gElement );
		}
		parent.appendChild( groupParent );
	}
	/**
	 * Write the version definitions out
	 * @param dom the document to add them to
	 * @param parent the parent element to add them to
	 */
	private void writeVersions( Document dom, Element parent )
	{
		Element vParent = dom.createElement( VERSIONS );
		for ( int i=0;i<versions.size();i++ )
		{
			VersionInfo v = versions.get( i );
			Element vElement = dom.createElement( VERSION );
			short backup = v.getBackup();
			if ( backup != Version.NO_BACKUP )
				vElement.setAttribute( BACKUP, Short.toString(backup) );
			vElement.setAttribute( GROUP, Short.toString(v.group) );
			vElement.setAttribute( SHORTNAME, v.shortName );
			vElement.setAttribute( LONGNAME, v.longName );
			vElement.setTextContent( v.file );
			vParent.appendChild( vElement );
		}
		parent.appendChild( vParent );
	}
	/**
	 * Create an XMLGuideFile in one easy step
	 * @param src specifies the source to read in
	 * @return a fully built XMLGuideFile
	 */
	public static XMLGuideFile internalise( File src ) throws MVDException
	{
		try
		{
			XMLGuideFile guide = new XMLGuideFile();
			// use DOM3 to parse
			DOMImplementationRegistry registry = 
				DOMImplementationRegistry.newInstance();
			DOMImplementationLS impl = 
			    (DOMImplementationLS)registry.getDOMImplementation("LS");
			LSParser parser = impl.createLSParser(
				DOMImplementationLS.MODE_SYNCHRONOUS, null );
			LSInput input = impl.createLSInput();
			input.setByteStream( new FileInputStream(src) );
			Document document = parser.parse( input );
			// get the MVDGUIDE element
			Element root = document.getDocumentElement();
			NodeList nl = root.getChildNodes();
			for ( int i=0;i<nl.getLength();i++ )
			{
				org.w3c.dom.Node node = nl.item( i );
				if ( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE )
				{
					String nodeName = node.getNodeName();
					if ( nodeName != null && nodeName.equals(GROUPS) )
						guide.readGroups( (Element)node );
					if ( nodeName != null && nodeName.equals(VERSIONS) )
						guide.readVersions( (Element)node );
					if ( nodeName != null && nodeName.equals(DESCRIPTION) )
						guide.description = node.getTextContent();
				}
			}
			return guide;
		}
		catch ( Exception e )
		{
			throw new MVDException( e );
		}
	}
	/**
	 * Read the groups as defined by the groups node
	 * @param groupsElement the groups element read from the DOM
	 */
	private void readGroups( Element groupsElement  )
	{
		NodeList nl = groupsElement.getChildNodes();
		for ( int i=0;i<nl.getLength();i++ )
		{
			org.w3c.dom.Node node = nl.item( i );
			if ( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE )
			{
				String nodeName = node.getNodeName();
				if ( nodeName != null && nodeName.equals(GROUP) )
				{
					Element e = (Element)node;
					String groupName = e.getAttribute( NAME );
					String parentStr = e.getAttribute( PARENT );
					short parent = Short.parseShort( parentStr );
					Group g = new Group( parent, groupName );
					groups.add( g );
				}
			}
		}
	}
	/**
	 * Read the versions as defined by the versions node
	 * @param versionsElement the versions element read from the DOM
	 */
	private void readVersions( Element versionsElement  )
	{
		NodeList nl = versionsElement.getChildNodes();
		for ( int i=0;i<nl.getLength();i++ )
		{
			org.w3c.dom.Node node = nl.item( i );
			if ( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE )
			{
				String nodeName = node.getNodeName();
				if ( nodeName != null && nodeName.equals(VERSION) )
				{
					Element e = (Element)node;
					String shortName = e.getAttribute( SHORTNAME );
					String longName = e.getAttribute( LONGNAME );
					String backupStr = e.getAttribute( BACKUP );
					String groupStr = e.getAttribute( GROUP );
					String file = e.getTextContent();
					short backup = Version.NO_BACKUP;
					if ( backupStr != null && backupStr.length() > 0 )
						backup = Short.parseShort( backupStr );
					short group = Short.parseShort( groupStr );
					VersionInfo v = new VersionInfo( group, backup, 
						shortName, longName, file );
					versions.add( v );
				}
			}
		}
	}
}
