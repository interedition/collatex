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
import java.io.PrintStream;
import au.edu.uq.nmerge.mvd.MVD;
import au.edu.uq.nmerge.mvd.Mask;
import au.edu.uq.nmerge.mvd.ChunkState;
import au.edu.uq.nmerge.mvd.Chunk;
import au.edu.uq.nmerge.mvd.Match;
import au.edu.uq.nmerge.mvd.Variant;
import au.edu.uq.nmerge.exception.*;
import au.edu.uq.nmerge.fastme.FastME;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.text.DateFormat;
import java.util.Date;

/**
 * MvdTool: adds or removes one version to/from an mvd, and 
 * optionally exports or creates one.
 * @author Desmond Schmidt 3/9/07
 */
public class MvdTool 
{
  private static final Logger LOG = LoggerFactory.getLogger(MvdTool.class);

	/** version id of backup version */
	static int backup;
	/** user issued command */
	static Commands command;
	/** description of MVD */
	static String description;
	/** encoding of text file to be merged */
	static String encoding;
	/** text to be found */
	static String findString;
	/** group name of new version */
	static String groupName;
	/** length of variant in base version */
	static int variantLen;
	/** long name of new version */
	static String longName;
	/** name of mvd file */
	static String mvdFile;
	/** name of archive */
	static String archiveName;
	/** from offset in base version */
	static int fromOffset;
	/** specified version is partial */
	static boolean partial;
	/** short name of new version */
	static String shortName;
	/** name of text file for merging */
	static String textFile;
	/** id of specified version */
	static short version;
	/** short name of desired version (instead of versionId) */
	static String versionName;
	/** version to compare with version */
	static short with;	
	/** file name for database connection */
	static String dbConn;
	/** name of xml file for export only */
	static String xmlFile;
	/** command to print example of */
	static String helpCommand;
	/** String for defining default sigla */
	static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	/** default break before and after byte arrays */
	static byte[] BREAK_BEFORE = {'<'};
	static byte[] BREAK_AFTER = {'>',' ','\n','\r','\t'};
	/** Stream to report results to */
	static PrintStream out;
	/** mask to apply to mvd data */
	static Mask mvdMask = Mask.NONE;
	/** state to label version text not found in with text 
	 * during compare */
	static ChunkState uniqueState;
	/* id of default folder */
	static int folderId;
	/**
	 * Commandline entry point
	 * @param args the commandline arguments
	 */
	public static void main( String[] args )
	{
		try
		{
			StringBuffer sb = new StringBuffer();
			sb.append("["+DateFormat.getInstance().format(new Date())+"] ");
			for ( int i=0;i<args.length;i++ )
			{
				if ( !args[i].startsWith("-") )
					sb.append( "\""+args[i]+"\"" );
				else
					sb.append(args[i]);
				sb.append(" ");
			}
			LOG.info(sb.toString());
			if ( !testJavaVersion() )
				System.out.println( "Minimum version of java is 1.5.0" );
			else
			{
				MvdTool.out = System.out;
				readArgs(args );
				doCommand();
			}
		}
		catch ( Exception e )
		{
			LOG.error(e.getMessage(), e);
		}
	}
	/**
	 * Test if the java environment is OK
	 * @return true if it can run the program, false otherwise
	 */
	static boolean testJavaVersion()
	{
		String jreVersion = System.getProperty( "java.version" );
		String[] parts = jreVersion.split("\\056");
		int majVersion = Integer.parseInt(parts[0]);
		int minVersion = Integer.parseInt(parts[1]);
		return majVersion == 1 && minVersion >= 5;
	}
	/**
	 * Execute the arguments
	 * @throws MVDToolException if something went wrong
	 */
	private static void doCommand() throws MVDException
	{
		setDefaults();
		switch ( command )
		{
			case ADD:
				doAddVersion();
				break;
			case COMPARE:
				doCompare();
				break;
			case DELETE:
				doDeleteVersion();
				break;
			case CREATE:
				doCreate();
				break;
			case DESCRIPTION:
				doDescription();
				break;
			case FIND:
				doFind();
				break;
			case HELP:
				printExample();
				break;
			case READ:
				doReadVersion();
				break;
			case UPDATE:
				doUpdateMVD();
				break;
			case USAGE:
				usage();
				break;
			case VARIANTS:
				doFindVariants();
				break;
			case TREE:
				doTree();
				break;
		}
	}
	/**
	 * Create a phylogenetic tree using the fastME method.
	 */
	private static void doTree() throws MVDToolException
	{
		try
		{
			MVD mvd = loadMVD();
			double[][] d = mvd.computeDiffMatrix();
			FastME fastME = new FastME();
			int numVersions = mvd.numVersions();
			String[] shortNames = new String[numVersions];
			for ( int i=0;i<numVersions;i++ )
				shortNames[i] = mvd.getVersionShortName( i+1 );
			fastME.buildTree( d, shortNames );
			fastME.refineTree();
			fastME.T.NewickPrintTree( out );
		}
		catch ( Exception e )
		{
			throw new MVDToolException( e );
		}
	}
	/**
	 * Print or change the MVD description
	 * @throws MVDToolException if MVD not present etc
	 */
	private static void doDescription() throws MVDToolException
	{
		try
		{
			MVD mvd = loadMVD();
			if ( description == null )
				out.println( mvd.getDescription() );
			else
			{
				mvd.setDescription(description);
			}
		}
		catch ( Exception e )
		{
			throw new MVDToolException( e );
		}
	}
	/**
	 * Create a new and empty MVD
	 */
	private static void doCreate() throws MVDToolException
	{
		try
		{
			File file = new File( mvdFile );
			if ( file.exists() && !file.delete() )
				throw new MVDToolException( 
					"Couldn't delete existing file "+mvdFile);
			else
			{
				MVD mvd = (description==null)?new MVD():new MVD(description);
				mvd.setMask(mvdMask);
			}
		}
		catch ( Exception e )
		{
			throw new MVDToolException( e );
		}
	}
	/**
	 * Print an example for the given helpCommand
	 */
	private static void printExample() throws MVDToolException
	{
		try
		{
			if ( helpCommand == null )
				throw new MVDToolException("Please specify a help command!");
			Commands hComm = Commands.valueOf(helpCommand.toUpperCase());
			switch ( hComm )
			{
				case ADD:
					out.println( "nmerge -c add -g \""
						+"MS 5 versions\" -s \"P\" -l \"third correction layer\""
						+" -t p.txt -m work.mvd");
					break;
				case ARCHIVE:
					out.println( "nmerge -c archive -m work.mvd -a myArchiveFolder");
					break;
				case COMPARE:
					out.println( "nmerge -c compare -m work.mvd -v 4 -w 7");
					break;
				case DELETE:
					out.println( "nmerge -c delete -m work.mvd "
						+"-v 7");
					break;
				case CREATE:
					out.println( "nmerge -c create -m work.mvd -d "
						+"\"my new MVD\" -e UTF-8");
					break;
				case DESCRIPTION:
					out.println( "nmerge -c description -m work.mvd\n"
						+"nmerge -c description -m work.mvd -d \"my "
						+"new mvd description\"");
					break;
				case TREE:
					out.println( "nmerge -c tree -m work.mvd -d existing.mvd" );
					break;
				case EXPORT:
					out.println( "nmerge -c export -m work.mvd "
						+"-x work.xml");
					break;
				case FIND:
					out.println( "nmerge -c find -m work.mvd -f \"bananas are nice\"");
					break;
				case IMPORT:
					out.println( "nmerge -c import -m work.mvd -x work.xml");
					break;
				case LIST:
					out.println( "nmerge -c list -m work.mvd");
					break;
				case READ:
					out.println( "nmerge -c read -m work.mvd -v 4");
					break;
				case UNARCHIVE:
					out.println( "nmerge -c unarchive -m work.mvd "+
						"-a myArchiveFolder");
					break;
				case UPDATE:
					out.println( "nmerge -c update -m work.mvd -v 3 -t 5.txt");
					break;
				case VARIANTS:
					out.println( "nmerge -c variants -m work.mvd "
						+"-v 3 -o 1124 -k 23");					
					break;
				case HELP: case USAGE:
					usage();
					break;
				default:
					throw new Exception( "Command "+hComm+" not catered for " );
			}
		}
		catch ( Exception e )
		{
			throw new MVDToolException( e );
		}
	}
	/**
	 * Compare two versions
	 * @throws MVDToolException
	 */
	private static void doCompare() throws MVDToolException
	{
		try
		{
			MVD mvd = loadMVD();
			if ( version == 0 || with == 0 )
				throw new MVDToolException( 
					"can't compare version="+version+" with version "+with );
			Chunk[] chunks = mvd.compare( version, with, uniqueState );
			for ( int i=0;i<chunks.length;i++ )
			{
				byte[] bytes = chunks[i].getBytes();
				out.write( bytes, 0, bytes.length );
			}
		}
		catch ( Exception e )
		{
			throw new MVDToolException( e );
		}
	}
	/**
	 * Load an MVD file from the specified path
	 * @return the MVD, loaded
	 */
	static MVD loadMVD() throws Exception
	{
		return new MVD();
	}
	/**
	 * Update an MVD by replacing the specified version with the given 
	 * textfile
	 */
	private static void doUpdateMVD() throws MVDToolException
	{
		try
		{
			File t = new File( textFile );
			if ( t.exists() )
			{
				MVD mvd = loadMVD();
				//mvd.removeVersion( version );
				FileInputStream fis = new FileInputStream( t );
				byte[] data = new byte[(int)t.length()];
				fis.read(data);
				mvd.update(version, data);
			}
			else
				throw new MVDToolException( "No text for replacement version");
		}
		catch ( Exception e )
		{
			throw new MVDToolException( e );
		}
	}
	/**
	 * Find the variants of the specified range within the specified 
	 * version
	 */
	private static void doFindVariants() throws MVDToolException
	{
		try
		{
			MVD mvd = loadMVD();
			if ( version == 0 )
				version = 1;
			Variant[] variants = mvd.getApparatus( version, fromOffset, 
				variantLen );
			for ( int i=0;i<variants.length;i++ )
			{
				byte[] bytes = variants[i].getBytes();
				out.write( bytes, 0, bytes.length );
				out.write( "\n".getBytes(), 0, 1 );
			}
		}
		catch ( Exception e )
		{
			throw new MVDToolException( e );
		}
	}
	/**
	 * Precede all cases of double quote with backslash-double quote
	 * @param text the text to escape
	 * @return the escaped text
	private static String escapeText( String text )
	{
		StringBuffer sb = new StringBuffer();
		for ( int i=0;i<text.length();i++ )
		{
			if ( text.charAt(i) == '"' )
				sb.append( "\\\"");
			else
				sb.append( text.charAt(i) );
		}
		return sb.toString();
	}
	 */
	/**
	 * Find the specified searchText
	 * @throws MVDToolException
	 */
	private static void doFind() throws MVDToolException
	{
		try
		{
			byte[] pattern = findString.getBytes();
			if ( pattern.length > 0 )
			{
				MVD mvd = loadMVD();
				BitSet bs = new BitSet();
				if ( version == 0 )
				{
					for ( int i=1;i<=mvd.numVersions();i++ )
						bs.set( i );
				}
				else
					bs.set( version );
				Match[] matches = mvd.search( pattern, bs, true ); 
				for ( int i=0;i<matches.length;i++ )
				{
					out.print( matches[i] );
				}
				out.print("\n");
			}
			else
				throw new MVDToolException( "Empty search text" );
		}
		catch ( Exception e )
		{
			throw new MVDToolException( e );
		}
	}

	/**
	 * Add the specified version to the MVD. Don't replace an existing 
	 * version, so we reset the supplied version parameter to the number 
	 * of versions+1
	 */
	private static void doAddVersion() throws MVDToolException
	{
		try
		{
			File t = new File( textFile );
			if ( t.exists() )
			{
				MVD mvd = new MVD( description );;
					mvd.newVersion( shortName, longName );
				FileInputStream fis = new FileInputStream( t );
				byte[] data = new byte[(int)t.length()];
				fis.read( data );
				version = (short)(mvd.numVersions());
				mvd.update( version, data );
				LOG.info("Unique percentage={}", mvd.getUniquePercentage(version));
				MvdTool.out.println(mvd.getUniquePercentage(version));
			}
			else
				throw new FileNotFoundException(
					"Couldn't find file "+textFile );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			throw new MVDToolException( e );
		}
	}
	/**
	 * Read a single version and write it to standard out
	 */
	private static void doReadVersion() throws MVDException
	{
		try
		{
			MVD mvd = loadMVD();
			byte[] data = mvd.getVersion( version );
			out.write(data, 0, data.length);
		}
		catch ( Exception e )
		{
			throw new MVDToolException( e );
		}
	}
	/**
	 * Remove the specified version from the specified mvd
	 * and save it back out.
	 */
	private static void doDeleteVersion() throws MVDException
	{
		try
		{
			MVD mvd = loadMVD();
			mvd.removeVersion(version);
		}
		catch ( Exception e )
		{
			throw new MVDToolException( e );
		}
	}

	/**
	 * Set defaults for unset parameters. 
	 */
	private static void setDefaults()
	{
		// variables that always must be set
		if ( encoding == null )
			encoding = Charset.defaultCharset().name();
		if ( groupName == null )
			groupName = "top level";
		if ( longName == null )
			longName = "version "+Short.toString(version);
		if ( shortName == null )
			shortName = Short.toString(version);
		if ( mvdFile == null )
			mvdFile = "untitled.mvd";
		if ( folderId == 0 )
			folderId = 1;
		if ( xmlFile == null )
		{
			int dotPos = mvdFile.indexOf(".");
			if ( dotPos == -1 )
				dotPos = mvdFile.length();
			xmlFile = mvdFile.substring( 0,dotPos )+".xml";
		}
	}
	/**
	 * Run a set of arguments through the tool
	 * @param args the args to test
	 * @param out the stream to write result to
	 * @throws MVDToolException if the arguments don't work
	 */
	public static void run( String[] args, PrintStream out ) throws MVDException
	{
		MvdTool.out = out;
		readArgs( args );
		doCommand();
	}
	/**
	 * Read in the arguments
	 * @param args an array of Strings from the command line
	 * @throws MVDToolException if the arguments are unusable
	 */
	private static void readArgs( String[] args ) throws MVDToolException
	{
		// clear previous values. remember they are static
		clearToDefaults();
		if ( args.length > 0 )
		{
			HashMap<String,String> argMap = new HashMap<String,String>();
			for ( int i=0;i<args.length;i++ )
			{
				if ( args[i].startsWith("-") )
				{
					String option = args[i].substring(1);
					if ( option.length() != 1 )
					{
						throw new MVDToolException("Option length must be 1");
					}
					else
					{
						if ( i < args.length-1 && !args[i+1].startsWith("-") )
							argMap.put( option, args[i+1] );
						else
							argMap.put( option, "" );
					}
				}
			}
			Set<String> keys = argMap.keySet();
			Iterator<String> iter = keys.iterator();
			while ( iter.hasNext() )
			{
				String key = iter.next();
				String value = argMap.get( key );
				if ( key.equals("c") )
				{
					if ( command != null && command == Commands.USAGE )
						throw new MVDToolException( "Only one of -c and -? is allowed");
					command = Commands.valueOf(argMap.get(key).toUpperCase());
				}
				else if ( key.equals("p") )
					partial = true;
				else if ( key.equals("?") )
				{
					if ( command != null )
						throw new MVDToolException( "Only one of -c and -? is allowed");
					command = Commands.USAGE;
				}
				else 
				{
					if ( value.length()==0 )
						throw new MVDToolException( "Missing option argument for key "+key );
					else if ( key.equals("a") )
						archiveName = value;
					else if ( key.equals("b") )
						backup = Integer.parseInt( value );
					else if ( key.equals("d") )
						description = value;
					else if ( key.equals("e") )
						encoding = value;
					else if ( key.equals("f") )
						findString = value;
					else if ( key.equals("g") )
						groupName = value;
					else if ( key.equals("h") )
					{
						command = Commands.HELP;
						helpCommand = value;
					}
					else if ( key.equals("k") )
						variantLen = Integer.parseInt(value);
					else if ( key.equals("l") )
						longName = value;
					else if ( key.equals("m") )
						mvdFile = value;
					else if ( key.equals("n") )
						mvdMask = Mask.valueOf(value.toUpperCase());
					else if ( key.equals("o") )
						fromOffset = Integer.parseInt(value);
					else if ( key.equals("s") )
						shortName = value;
					else if ( key.equals("t") )
						textFile = value;
					else if ( key.equals("u") )
						uniqueState = ChunkState.valueOf(value);
					else if ( key.equals("v") )
						version = Short.parseShort(value);
					else if ( key.equals("w") )
						with = Short.parseShort(value);
					else if ( key.equals("x") )
						xmlFile = value;
					else if ( key.equals("y") )
						dbConn = value;
					else if ( key.equals("z") )
						folderId = Integer.parseInt(value);
					else 
						throw new MVDToolException( "Unknown option "+key );
				}
			}
		}
		if ( command == null )
			command = Commands.USAGE;
	}
	/**
	 * Reset all the static variables to sensible defaults
	 */
	private static void clearToDefaults()
	{
		archiveName = description = findString = groupName = 
			helpCommand = shortName = longName = mvdFile = textFile = 
			xmlFile = null;
		command = null;
		encoding = "UTF-8";
		backup = variantLen = fromOffset = 0;
		version = 0;
		mvdMask = Mask.NONE;
		uniqueState = ChunkState.deleted;
		dbConn = null;
	}
	/**
	 * Tell the user about how to use this program
	 */
	private static void usage()
	{
		out.println(
				"usage: nmerge [-c command] [-a archive] [-b backup]  [-d description]\n"
				+"     [-e encoding] [-f string] [-g group] [-h command] [-k length]\n"
				+"     [-l longname] [-m MVD] [-n mask] [-o offset] [-p] [-s shortname]\n"
				+"     [-t textfile] [-v version] [-w with] [-x XMLfile] [-y] dbconn [-?] \n\n"
				+"-a archive - folder to use with archive and unarchive commands\n"
				+"-b backup - the version number of a backup (for partial versions)\n"
				+"-c command - operation to perform. One of:\n"
				+"     add - add the specified version to the MVD\n"
				+"     archive - save MVD in a folder as a set of separate versions\n"
				+"     compare - compare specified version 'with' another version\n"
				+"     create - create a new empty MVD\n"
				+"     description - print or change the MVD's description string\n"
				+"     delete - delete specified version from the MVD\n"
				+"     export - export the MVD as XML\n"
				+"     find - find specified text in all versions or in specified version\n"
				+"     import - convert XML file to MVD\n"
				+"     list - list versions and groups\n"
				+"     read - print specified version to standard out\n"
				+"     tree - compute phylogenetic tree\n"
				+"     update - replace specified version with contents of textfile\n"
				+"     unarchive - convert an MVD archive into an MVD\n"
				+"     variants - find variants of specified version, offset and length\n"
				+"-d description - specified when setting/changing the MVD description\n"
				+"-e encoding - the encoding of the version's text e.g. UTF-8\n"
				+"-f string - to be found (used with command find)\n"
				+"-g group - name of group for new version\n"
				+"-h command - print example for command\n"
				+"-k length - find variants of this length in the base version's text\n"
				+"-l longname - the long name/description of the new version (quoted)\n"
				+"-m MVD - the MVD file to create/update\n"
				+"-n mask - mask out which kind of data in new mvd: none, xml or text\n"
				+"-o offset - in given version to look for variants\n"
				+"-p - specified version is partial\n"
				+"-s shortname - short name or siglum of specified version\n"
				+"-t textfile - the text file to add to/update in the MVD\n"
				+"-u unique - name of state to label text found in the main -v version,\n"
				+"   not in -w version during compare - e.g. 'added' or 'deleted'(default)\n"
				+"-v version - number of version for command (starting from 1)\n"
				+"-w with - another version to compare with version\n"
				+"-x XML - the XML file to export or import\n"
				+"-y dbconn - name of database connection file\n"
				+"-z folderId - id of folder to store mvd (default 1)\n"
				+"-? - print this message\n"
		);
	}
}
