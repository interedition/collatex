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
import java.io.File;
import java.util.BitSet;
import java.util.Set;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.ls.DOMImplementationLS;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import org.w3c.dom.DOMConfiguration;

import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSInput;
import au.edu.uq.nmerge.exception.*;

/**
 * <p>Provide a simple XML format for loading and saving an MVD. 
 * This only works if the content is plain text or XML itself.
 * We will escape angle brackets and ampersands into entities.</br>
 * Structure:
 * </br><code>&lt;mvd description="..."&gt;&lt;groups&gt;
 * &lt;/groups&gt;&lt;versions&gt; &lt;/versions&gt;&lt;pairs&gt;
 * &lt;/pairs&gt;&lt;/mvd&gt;</code></p>
 * <p>However, note that we use DOS line endings regardless of 
 * the platform. The reason for this is that url-encoded forms 
 * submitted in web-applications use DOS line endings. This 
 * behaviour cannot be changed. In order to get consistent 
 * behaviour on all platforms (and the server and client may be 
 * different platforms) we need one representation that works.</p>
 */
public class MVDXMLFile 
{
	/**
	 * Load an XML description of a textual/XML MVD
	 * @param src the source file
	 * @return a finished MVD
	 * @throws Exception if the syntax was wrong for example
	 */
	public static MVD internalise( File src ) throws Exception
	{
		// use DOM3 to parse
		DOMImplementationRegistry registry 
			= DOMImplementationRegistry.newInstance();
		DOMImplementationLS impl = 
		    (DOMImplementationLS)registry.getDOMImplementation("LS");
		LSParser parser = impl.createLSParser(
			DOMImplementationLS.MODE_SYNCHRONOUS, null );
		DOMConfiguration config = parser.getDomConfig();
		// don't preserve entities: so &lt; becomes <
		config.setParameter( "entities", false );
		LSInput input = impl.createLSInput();
		input.setByteStream( new FileInputStream(src) );
		Document document = parser.parse( input );
		// get the "mvd" element
		Element root = document.getDocumentElement();
		String description = root.getAttribute("description");
		if ( description == null )
			description = "";
		String encoding = root.getAttribute("encoding");
		if ( encoding == null )
			encoding = "UTF-8";
		MVD mvd = new MVD( description );
		NodeList nl = root.getChildNodes();
		for ( int i=0;i<nl.getLength();i++ )
		{
			Node topLevelNode = nl.item( i );
			if ( topLevelNode.getNodeType() == Node.ELEMENT_NODE )
			{
				String name = topLevelNode.getNodeName();
				if ( name.equals("groups") )
					readGroups( (Element)topLevelNode, mvd );
				else if ( name.equals("versions") )
					readVersions( (Element)topLevelNode, mvd );
				else if ( name.equals("pairs") )
					readPairs( (Element)topLevelNode, mvd, encoding );
			}
		}
		return mvd;
	}
	/**
	 * Read the group definitions of the MVD. A group is just 
	 * a numbered description which has a parent.
	 * @param groupsElement a group element containing the group 
	 * definitions
	 * @param mvd the MVD to put them into
	 */
	private static void readGroups( Element groupsElement, MVD mvd )
		throws Exception
	{
		TreeMap<Short,Group> groups = new TreeMap<Short,Group>();
		NodeList nl = groupsElement.getChildNodes();
		for ( int i=0;i<nl.getLength();i++ )
		{
			Node groupNode = nl.item( i );
			if ( groupNode.getNodeType() == Node.ELEMENT_NODE )
			{
				NamedNodeMap nodeMap = groupNode.getAttributes();
				Node idAttr = nodeMap.getNamedItem( "id" );
				Node parentAttr = nodeMap.getNamedItem( "parent" );
				Node nameAttr = nodeMap.getNamedItem( "name" );
				if ( idAttr == null || parentAttr == null 
					|| nameAttr == null )
					throw new MVDException( 
						"missing id, parent or name for group" );
				String id = idAttr.getTextContent();
				String parent = parentAttr.getTextContent();
				String name = nameAttr.getTextContent();
				Group g = new Group( Short.parseShort(parent), name );
				short idVal = Short.parseShort( id );
				groups.put( new Short(idVal), g );
			}
		}
		Set<Short> keys = groups.keySet();
		Iterator<Short> iter = keys.iterator();
		short old = 0;
		// TreeMap sorts on id
		while ( iter.hasNext() )
		{
			Short curr = iter.next();
			short currVal = curr.shortValue();
			if ( currVal != old+1 )
				throw new MVDException( "Missing group id "+(old+1) );
			mvd.addGroup( groups.get(curr) );
			old = currVal;
		}
	}
	/**
	 * Read the version definitions of the MVD
	 * @param versionsElement a versions element containing all the 
	 * version definitions
	 * @param mvd the MVD to put them into
	 */
	private static void readVersions( Element versionsElement, 
		MVD mvd ) throws Exception
	{
		TreeMap<Short,Version> versions = new TreeMap<Short,Version>();
		NodeList nl = versionsElement.getChildNodes();
		for ( int i=0;i<nl.getLength();i++ )
		{
			Node versionNode = nl.item( i );
			if ( versionNode.getNodeType() == Node.ELEMENT_NODE )
			{
				NamedNodeMap nodeMap = versionNode.getAttributes();
				Node idAttr = nodeMap.getNamedItem( "id" );
				Node groupAttr = nodeMap.getNamedItem( "group" );
				Node backupAttr = nodeMap.getNamedItem( "backup" );
				Node shortNameAttr = nodeMap.getNamedItem( "shortName" );
				Node longNameAttr = nodeMap.getNamedItem( "longName" );
				if ( idAttr == null || groupAttr == null 
					|| shortNameAttr == null || longNameAttr == null )
					throw new MVDException( 
						"missing id, group, short name or long name"
							+" for version" );
				String id = idAttr.getTextContent();
				String group = groupAttr.getTextContent();
				short backup = Version.NO_BACKUP;
				if ( backupAttr != null )
					backup = Short.parseShort( backupAttr.getTextContent() );
				String shortName = shortNameAttr.getTextContent();
				String longName = longNameAttr.getTextContent();
				Version v = new Version( Short.parseShort(group), backup, 
					shortName, longName );
				short idVal = Short.parseShort( id );
				versions.put( new Short(idVal), v );
			}
		}
		Set<Short> keys = versions.keySet();
		Iterator<Short> iter = keys.iterator();
		short old = 0;
		// TreeMap sorts on id
		while ( iter.hasNext() )
		{
			Short curr = iter.next();
			short currVal = curr.shortValue();
			if ( currVal != old+1 )
				throw new MVDException( "Missing version id "+(old+1) );
			mvd.addVersion( versions.get(curr) );
			old = curr.shortValue();
		}
	}
	/**
	 * Read the pair definitions of the MVD
	 * @param pairsElement a group element containing the pair 
	 * definitions
	 * @param mvd the MVD to put them into
	 * @param encoding the encoding for the data
	 * @throws an exception if the format is wrong
	 */
	private static void readPairs( Element pairsElement, MVD mvd, 
		String encoding ) throws Exception
	{
		HashMap<Integer,Pair> parents = new HashMap<Integer,Pair>();
		HashMap<Integer,LinkedList<Pair>> orphans = 
			new HashMap<Integer,LinkedList<Pair>>();
		NodeList nl = pairsElement.getChildNodes();
		for ( int i=0;i<nl.getLength();i++ )
		{
			Node pairNode = nl.item( i );
			if ( pairNode.getNodeType() == Node.ELEMENT_NODE )
			{
				int parentId=-1,id=-1;
				boolean hint=false;
				NamedNodeMap nodeMap = pairNode.getAttributes();
				Node idAttr = nodeMap.getNamedItem( "id" );
				Node parentAttr = nodeMap.getNamedItem( "parent" );
				Node versionsAttr = nodeMap.getNamedItem( "versions" );
				Node hintAttr = nodeMap.getNamedItem( "hint" );
				if ( versionsAttr == null )
					throw new MVDException( 
						"missing versions attribute for pair" );
				if ( parentAttr != null )
				{
					String parent = parentAttr.getTextContent();
					parentId = Integer.parseInt( parent );
				}
				if ( hintAttr != null )
				{
					String hintStr = hintAttr.getTextContent();
					hint = Boolean.parseBoolean( hintStr );
				}
				String data = pairNode.getTextContent();
				BitSet version = buildVersion( versionsAttr.getTextContent(), 
					hint );
				byte[] pairData = null;
				if ( data != null && data.length()>0 )
					pairData = data.getBytes( encoding );
				Pair p = new Pair( version, pairData );
				if ( idAttr != null )
				{
					id = Integer.parseInt( idAttr.getTextContent() );
					parents.put( id, p );
					// find orphans a home
					LinkedList<Pair> llp = orphans.get( id );
					if ( llp != null )
					{
						for ( int j=0;j<llp.size();j++ )
						{
							Pair child = llp.get( j );
							p.addChild( child );
						}
						orphans.remove( id );
					}
				}
				else if ( parentId != -1 )
				{
					Integer pId = new Integer( parentId );
					Pair parent = parents.get( pId );
					// parent already encountered?
					if ( parent == null )
					{
						LinkedList<Pair> llp = orphans.get( pId );
						if ( llp == null )
						{
							llp = new LinkedList<Pair>();
							llp.add( p );
							orphans.put( pId, llp );
						}
						else
							llp.add( p );
					}
					else
						parent.addChild( p );
				}
				mvd.addPair( p );
			}
		}
	}
	/**
	 * Build a BitSet of versions from a string where each version id 
	 * is separated by a comma and runs separated by hyphens.
	 * @param versions a string containing versions in a compact 
	 * readable format
	 * @return the finished BitSet corresponding to the inputs
	 * @throws an Exception if the syntax is wrong
	 */
	private static BitSet buildVersion( String versions, 
		boolean hint ) throws Exception
	{
		BitSet bs = new BitSet();
		if ( hint )
			bs.set( 0 );
		String[] parts = versions.split(",");
		for ( int i=0;i<parts.length;i++ )
		{
			if ( parts[i].indexOf('-') != -1 )
			{
				String[] subParts = parts[i].split("-");
				if ( subParts.length > 2 )
					throw new MVDException( "Invalid version run "
						+parts[i] );
				else
				{
					int start = Integer.parseInt( subParts[0] );
					int end = Integer.parseInt( subParts[1] );
					for ( int j=start;j<=end;j++ )
						bs.set( j );
				}
			}
			else
				bs.set( Integer.parseInt(parts[i]) );
		}
		return bs;
	}
	/**
	 * Write an MVD out as text. If there are angle brackets or 
	 * ampersands in the text, these are automatically escaped 
	 * as entities
	 * @param mvd the source multi-version document
	 * @param dst the destination MVD XML file
	 * @param XMLEncoding the desired encoding of the output 
	 * (not that of the source mvd)
	 * @param srcEncoding the encoding of the data in the MVD
	 * @param pretty if true make some attempt at tidying the output
	 * @throws an exception if it couldn't write the file or find a 
	 * serializer
	 */
	public static void externalise( MVD mvd, File dst, 
		String XMLEncoding, String srcEncoding, boolean pretty ) 
	throws Exception
	{
		DocumentBuilderFactory dbf 
			= DocumentBuilderFactory.newInstance();
		try 
		{
			DocumentBuilder db = dbf.newDocumentBuilder();
			// create an instance of DOM
			Document dom = db.newDocument();
			Element rootElement = dom.createElement( "mvd" );
			rootElement.setAttribute( "description", 
				mvd.getDescription() );
			rootElement.setAttribute( "encoding", 
				mvd.getEncoding() );
			dom.appendChild( rootElement );
			writeGroups( dom, mvd, rootElement );
			writeVersions( dom, mvd, rootElement );
			writePairs( dom, mvd, rootElement, srcEncoding );
			// print it out manually since DOM serialisation escapes 
			// CRLF as &13;LF on the Mac and this behaviour can't be 
			// turned off. We need a portable externalise routine that 
			// works the same on all platforms
			FileOutputStream fos = new FileOutputStream( dst );
			XMLPretty.writeXMLDeclaration( fos, XMLEncoding );
			XMLPretty.writeNode( dom.getFirstChild(), fos, 
				XMLEncoding, pretty, 0 );
			fos.close();
		}
		catch ( Exception ioe )
		{
			throw new MVDException(
				"Failure to write MVD XML file "+ioe );
		}
	}
	/**
	 * Write the groups out
	 * @param dom the document to add them to
	 * @param mvd the mvd to get them from
	 * @param parent the parent element to add them to
	 */
	private static void writeGroups( Document dom, MVD mvd, 
		Element parent )
	{
		Element groupParent = dom.createElement( "groups" );
		for ( short i=0;i<mvd.groups.size();i++ )
		{
			Group g = mvd.groups.get( i );
			Element gElement = dom.createElement( "group" );
			gElement.setAttribute( "id", Short.toString(
				(short)(i+1)) );
			gElement.setAttribute( "parent", 
				Short.toString(g.parent) );
			gElement.setAttribute( "name", g.name );
			groupParent.appendChild( gElement );
		}
		parent.appendChild( groupParent );
	}
	/**
	 * Write the version definitions out
	 * @param dom the document to add them to
	 * @param mvd the mvd to get them from
	 */
	private static void writeVersions( Document dom, MVD mvd, 
		Element parent )
	{
		Element vParent = dom.createElement( "versions" );
		for ( int i=0;i<mvd.versions.size();i++ )
		{
			Version v = mvd.versions.get( i );
			Element vElement = dom.createElement( "version" );
			vElement.setAttribute( "id", Integer.toString(i+1) );
			short backup = v.getBackup();
			if ( backup != Version.NO_BACKUP )
				vElement.setAttribute( "backup", 
					Short.toString(backup) );
			vElement.setAttribute( "group", 
				Short.toString(v.group) );
			vElement.setAttribute( "shortName", v.shortName );
			vElement.setAttribute( "longName", v.longName );
			vParent.appendChild( vElement );
		}
		parent.appendChild( vParent );
	}
	/**
	 * Write the pairs out
	 * @param dom the document to add them to
	 * @param mvd the mvd to get them from
	 * @param encoding the encoding for the data content
	 */
	private static void writePairs( Document dom, MVD mvd, 
		Element parent, String encoding ) throws Exception
	{
		Element pParent = dom.createElement( "pairs" );
		HashMap<Pair,Integer> parents = new HashMap<Pair,Integer>();
		HashMap<Pair,LinkedList<Element>> orphans = 
			new HashMap<Pair,LinkedList<Element>>();
		int id = 1;
		for ( int i=0;i<mvd.pairs.size();i++ )
		{
			Pair p = mvd.pairs.get( i );
			Element pElement = dom.createElement( "pair" );
			if ( p.children != null )
			{
				pElement.setAttribute("id", Integer.toString(id) );
				parents.put( p, new Integer(id++) );
				// check if any orphans belong to this parent
				LinkedList<Element> llp = orphans.get( p );
				if ( llp != null )
				{
					Integer pId = parents.get( p );
					for ( int j=0;j<llp.size();j++ )
					{
						Element child = llp.get( j );
						child.setAttribute( "parent", pId.toString() );
					}
					orphans.remove( p );
				}
			}
			else if ( p.parent != null )
			{
				Integer pId = parents.get( p.parent );
				if ( pId != null )
					pElement.setAttribute( "parent", pId.toString() );
				else
				{
					LinkedList<Element> children = orphans.get( p.parent );
					if ( children == null )
					{
						children = new LinkedList<Element>();
						children.add( pElement );
						orphans.put( p.parent, children );
					}
					else
						children.add( pElement );
				}
			}
			if ( p.versions.nextSetBit(0)==0 )
				pElement.setAttribute("hint", 
					Boolean.toString(true) );
			pElement.setAttribute( "versions", 
				serialiseVersion(p.versions) );
			if ( p.parent == null )
			{
				String contents = new String(p.getData(),encoding);
				pElement.setTextContent( contents );
			}
			pParent.appendChild( pElement );
		}
		// attach pairs node to its parent node
		parent.appendChild( pParent );
	}
	/**
	 * Convert a bitset into a string with the correct syntax. We omit 
	 * the first bit as that is handled separately.
	 * @param bs set of versions
	 * @return return a string representation of the bitset
	 */
	private static String serialiseVersion( BitSet bs )
	{
		StringBuffer sb = new StringBuffer();
		int start = bs.nextSetBit( 1 );
		int end = start;
		while ( end >= 1 )
		{
			int res = bs.nextSetBit( end+1 );
			if ( res == -1 || res > end+1 )
			{
				if ( sb.length() > 0 )
					sb.append(",");
				if ( end > start )
					sb.append( start+"-"+end );
				else
					sb.append( start );
				start = end = res;
			}
			else
				end = res;
		}
		return sb.toString();
	}
}
