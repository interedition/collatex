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

import au.edu.uq.nmerge.mvd.MVDFile;

import au.edu.uq.nmerge.mvd.MVD;
import au.edu.uq.nmerge.mvd.Chunk;
import au.edu.uq.nmerge.mvd.Match;
import au.edu.uq.nmerge.mvd.ChunkState;
import au.edu.uq.nmerge.exception.MVDException;
import au.edu.uq.nmerge.exception.MVDToolException;
import au.edu.uq.nmerge.exception.MVDTestException;
import au.edu.uq.nmerge.graph.Converter;
import au.edu.uq.nmerge.graph.VariantGraph;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Vector;
import java.util.Random;
import java.util.HashSet;
/**
 * Test the NMerge library
 * @author Desmond Schmidt 2/5/09
 */
public class MvdToolTest
{
	static boolean printStackTrace = false;
	static int testsPassed,testsFailed;
	static PrintStream out;
	static EmptyOutputStream emptyStream;
	static String TEST_FOLDER = "MvdTestFolder";
	static String TEST_DATA= "testdata";
	static String TEMP_OUT= "temp.out";
	static String BLESSED_DAMOZEL = "BlessedDamozel";
	static String RENAISSANCE = "Renaissance";
	static HashSet<String> testMVDs;
	/**
	 * Call this main entry point instead of the MvdTool if you want 
	 * to just test nmerge.
	 * @param args only one: -e means print stack trace
	 */
	public static void main( String[] args )
	{
		if ( MvdTool.testJavaVersion() )
		{
			if ( args.length > 0 )
			{
				if ( args[0].equals("-e") )
					printStackTrace = true;
				else if ( args[0].equals("-h") )
				{
					System.out.println(
						"java -jar nmerge.jar [-e] [-h]\n"
						+"-e: enable print stack traces\n"
						+"-h: print help message");
					return;
				}
			}		
			// verify on each save
			MVDFile.debug = true;
			// create empty output stream to catch usage printout, version 
			// lists etc and throw them away.
			emptyStream = new EmptyOutputStream();
			try
			{
				out = new PrintStream( emptyStream, true, "UTF-8" );
			}
			catch ( Exception e )
			{
				// won't happen
			}
			// now run the tests!
			doUsageTest();
			doCreateTest();
			doHelpTest();
			doAddTest();
			doReadTest();
			doDeleteTest();
			doDescriptionTest();
			doUnarchiveTest();
			doImportExportTest();
			doUpdateTest();
			doListTest();
			doCompareTest();
			doFindTest();
			doVariantsTest();
			System.out.println( "Tests passed = "+testsPassed );
			System.out.println( "Tests failed = "+testsFailed );
		}
		else
			System.out.println( "Minimum version of java is 1.5.0" );
	}
	/**
	 * Test the variants function. To do this we choose 5 random 
	 * short stretches in one version, then calculate the variants 
	 * and compare them to the files that correspond to the versions.
	 */
	private static void doVariantsTest()
	{
		try
		{
			System.out.print("Testing variants command ");
			String folderName = TEST_DATA+File.separator+BLESSED_DAMOZEL;
			File folder = new File( folderName );
			String mvdName = createTestMVD( folder );
			MVD mvd = MVDFile.internalise( mvdName, null );
			Random rand = new Random( System.currentTimeMillis() );
			File[] files = folder.listFiles();
			files = removeDotFiles( files );
			// select 5 random patterns in five files
			// compute their variants and check that they 
			// are in the original files.
			for ( int i=0;i<5;i++ )
			{
				int fileNo = rand.nextInt( files.length );
				int offset = rand.nextInt( (int)files[fileNo].length()-30 );
				int length = rand.nextInt( 15 ) + 15;
				int baseId = mvd.getVersionByLongName(files[fileNo].getName());
				if ( baseId != -1 )
				{
					String[] args0 = {"-c","variants","-m",mvdName,"-o",
						Integer.toString(offset),"-k",Integer.toString(length),
						"-v",Integer.toString(baseId)};
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					PrintStream ps = new PrintStream( bos );
					MvdTool.run( args0, ps );
					ps.close();
					byte[] varData = bos.toByteArray();
					compareVariants( varData, files, mvd );
				}
				else
					throw new MVDTestException("Couldn't find version "
						+files[fileNo].getName());
				System.out.print(".");
			}
			testsPassed++;
			System.out.println(" test passed.");
		}
		catch ( Exception e )
		{
			doTestFailed( e );
		}
	}
	/**
	 * Compare the variants against the original files
	 * @param varData the variant data returned by the MvdTool
	 * @param files an array of files of the original versions
	 * @param mvd the mvd built from them
	 * @throws Exception
	 */
	private static void compareVariants( byte[] varData, File[] files, 
		MVD mvd ) throws Exception
	{
		/*int pos = 0;
		while ( pos < varData.length )
		{
			Variant v = new Variant( varData, pos, mvd );
			BitSet bs = v.getVersions();
			for ( int i=bs.nextSetBit(1);i>=1;i=bs.nextSetBit(i+1) ) 
			{
			    String fileName = mvd.getVersionLongName( i );
			    for ( int j=0;j<files.length;j++ )
			    {
			    	if ( fileName.equals(files[j].getName()) )
	    			{
			    		FileInputStream fis = new FileInputStream( 
			    			files[j] );
			    		byte[] origData = new byte[(int)files[j].length()];
			    		fis.read( origData );
			    		fis.close();
			    		int index = KMPSearch.search( origData, 
			    			0, v.getData() );
			    		if ( index == -1 )
			    			throw new MVDTestException( 
			    				"Couldn't find variant "
			    				+v+" in file "+fileName );
			    		break;
	    			}
			    }
			}
			pos += v.getSrcLen();
		}*/
	}
	/**
	 * Test the find function. We choose some phrases at random in random 
	 * files and then search for them using string search. Note the results. 
	 * Then perform the same searches using MvdTool and see if they are 
	 * the same.
	 */
	private static void doFindTest()
	{
		try
		{
			System.out.print("Testing find command ");
			String folderName = TEST_DATA+File.separator+BLESSED_DAMOZEL;
			File folder = new File( folderName );
			String mvdName = createTestMVD( folder );
			MVD mvd = MVDFile.internalise( mvdName, null );
			Random rand = new Random( System.currentTimeMillis() );
			File[] files = folder.listFiles();
			files = removeDotFiles( files );
			// select 5 random patterns in five files
			for ( int i=0;i<5;i++ )
			{
				int fileNo = rand.nextInt( files.length );
				String pattern = getRandomPattern( files[fileNo], rand, 25, 15 );
				if ( pattern.length()<25 )
				{
					String[] args0 = {"-c","find","-m",mvdName,"-f",pattern};
					Match[] matches = getMatches( files, pattern, mvd );
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					PrintStream ps = new PrintStream( bos );
					MvdTool.run( args0, ps );
					ps.close();
					byte[] matchData = bos.toByteArray();
					compareMatches( matchData, matches );
				}
				System.out.print(".");
			}
			testsPassed++;
			System.out.println(" test passed.");
		}
		catch ( Exception e )
		{
			doTestFailed( e );
		}
	}
	/**
	 * Check the matches found by brute force against those returned
	 * by the MvdTool. They should match one for one. If not, then 
	 * throw an exception.
	 * @param matchData the matches expressed in text form as computed 
	 * by the mvd
	 * @param matches the matches retrieved by brute force search of 
	 * the versions
	 * @throws MVDTestException if they two don't match
	 */
	private static void compareMatches( byte[] matchData, Match[] matches )
		throws MVDTestException
	{
		int pos = 0;
		while ( pos < matchData.length )
		{
			Match match = new Match( matchData, pos );
			System.out.println(match);
			for ( int i=0;i<matches.length;i++ )
			{
				if ( !matches[i].isFound() )
				{
					if ( matches[i].equals(match) )
					{
						matches[i].setFound( true );
						break;
					}
				}
			}
			pos += match.getSrcLen();
		}
		for ( int i=0;i<matches.length;i++ )
			if ( !matches[i].isFound() )
				throw new MVDTestException("Match "+matches[i]+" not found");
	}
	/**
	 * Get all the matches in all of the files
	 * @param files the files to search
	 * @param pattern the pattern to search for
	 * @param mvd the mvd to get the vIds from
	 * @return an array of matches for the pattern
	 * @throws Exception 
	 */
	private static Match[] getMatches( File[] files, String pattern, MVD mvd )
		throws Exception
	{
		Vector<Match> matches = new Vector<Match>();
		for ( int i=0;i<files.length;i++ )
			findPatternInFile( files[i], pattern, matches, mvd );
		Match[] array = new Match[matches.size()];
		matches.toArray( array );
		return array;
	}
	/**
	 * Find a pattern in one file. It may be there several times. 
	 * We use a brute force search using String.indexOf. Crude 
	 * but this will do for testing.
	 * @param src the file to search
	 * @param pattern the pattern to search for
	 * @param matches a vector of matches to be updated
	 * @throws Exception
	 */
	private static void findPatternInFile( File src, String pattern, 
		Vector<Match> matches, MVD mvd ) throws Exception
	{
		short vId = mvd.getVersionByLongName( src.getName() );
		if ( vId != -1 )
		{
			int length = (int)src.length();
			FileInputStream fis = new FileInputStream( src );
			byte[] data = new byte[length];
			fis.read( data );
			fis.close();
			int offset = 0;
			int index = -1;
			byte[] bPattern = pattern.getBytes("UTF-8");
			do 
			{
				index = KMPSearch.search( data, offset, bPattern );
				if ( index != -1 )
				{
					Match m = new Match( index, bPattern.length, vId, 
						mvd.getVersionShortName(vId) );
					matches.add( m );
					offset += index + pattern.length();
				}
			}
			while ( index != -1 );
		}
	}
		
	/**
	 * Get a random pattern from the given file of between 25 and 
	 * 40 characters
	 * @param src the file to extract the data from
	 * @param rand a random number generator
	 * @param minSize the minimum length of the pattern
	 * @param range the extra size the pattern may take
	 * @return null if no pattern possible (file empty) or the pattern
	 */
	private static String getRandomPattern( File src, Random rand, 
		int minSize, int range ) throws Exception
	{
		int size = rand.nextInt( range ) + minSize;
		String result = null;
		if ( src.exists() )
		{
			int length = (int)src.length();
			FileInputStream fis = new FileInputStream( src );
			byte[] data = new byte[length];
			fis.read( data );
			fis.close();
			String str = new String( data, "UTF-8" );
			int start = rand.nextInt( str.length() - size );
			result = str.substring( start, start+size );
		}
		return result;
	}
	/**
	 * Test the compare function. We do a compare of each version 
	 * with another version chosen at random. We carry out the 
	 * deletion and transposition events indicated in the first 
	 * version and check it against the second version. 
	 */
	private static void doCompareTest()
	{
		try
		{
			System.out.print("Testing compare command ");
			String folderName = TEST_DATA+File.separator+BLESSED_DAMOZEL;
			File folder = new File( folderName );
			String mvdName = createTestMVD( folder );
			Random rand = new Random( System.currentTimeMillis() );
			MVD mvd = MVDFile.internalise( mvdName, null );
			int numVersions = mvd.numVersions();
			int v1 = 1;
			for ( v1=1;v1<=numVersions;v1++ )
			{
				int v2 = -1;
				while ( v2 == -1 || v2 == v1 )
					v2 = rand.nextInt(numVersions)+1;
				byte[] text1 = mvd.getVersion( v1 );
				byte[] text2 = mvd.getVersion( v2 );
				byte[] ver1 = collectVersionFromCompare(mvdName,
					v1,v2,ChunkState.deleted,text1);
				byte[] ver2 = collectVersionFromCompare(mvdName,
					v2,v1,ChunkState.added,text2);
				compareTwoByteArrays( text1, ver1 );
				compareTwoByteArrays( text2, ver2 );
				System.out.print(".");
			}
			testsPassed++;
			System.out.println(" test passed.");
		}
		catch ( Exception e )
		{
			doTestFailed( e );
		}
	}
	/**
	 * Compare one version with another then extract the first version 
	 * for later verification
	 * @param mvdName the mvd to compare in
	 * @param v1 the first version
	 * @param v2 the second version
	 * @param unique the state for unique text of the first version
	 * @param original original text of version 1 for comparison (debug)
	 * @return a byte array containing a copy of version 1
	 */
	private static byte[] collectVersionFromCompare( String mvdName, 
		int v1, int v2, ChunkState unique, byte[] original ) 
		throws Exception
	{
		String vId1 = Integer.toString(v1);
		String vId2 = Integer.toString(v2);
		String[] args0 = {"-c","compare","-m",mvdName,"-v",vId1,
				"-w",vId2,"-u",unique.toString()};
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream( bos );
		MvdTool.run( args0, ps );
		ps.close();
		int pos = 0;
		int j = 0;
		byte[] chunkData = bos.toByteArray(); 
		ByteArrayOutputStream bos2 = new ByteArrayOutputStream( 
			chunkData.length );
		while ( pos < chunkData.length )
		{
			Chunk chunk = new Chunk( chunkData, pos, (short)0 );
			byte[] bytes = chunk.getData();
			for ( int i=0;i<bytes.length;i++,j++ )
			{
				if ( original[j] != bytes[i] )
					throw new MVDTestException(
						"Retrieved version from compare "
						+"not the same as original");
			}
			pos += chunk.getSrcLen();
			bos2.write( chunk.getData() );
		}
		return bos2.toByteArray();
	}
	/**
	 * Test the list function. This writes all the groups and versions 
	 * currently defined to standard out. Capture this, and compare it 
	 * to the information contained in the MVD. Assume that the 
	 * Blessed Damozel MVD has already been created.	 
	 */
	private static void doListTest()
	{
		try
		{
			System.out.print("Testing list command ");
			File testFolder = new File( TEST_DATA+File.separator+BLESSED_DAMOZEL );
			String mvdName = createTestMVD( testFolder );
			MVD mvd = MVDFile.internalise( mvdName, null );
			String[] args0 = {"-c","list","-m",mvdName};
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream( bos );
			MvdTool.run( args0, ps );
			ps.close();
			byte[] list = bos.toByteArray();
			compareListToMvd( list, mvd );
			testsPassed++;
			System.out.println(" test passed.");
		}
		catch ( Exception e )
		{
			doTestFailed( e );
		}
	}
	/**
	 * Compare a list of versions and groups output on the 
	 * commandline to the contents of an MVD.
	 * @param list the list data as a byte array
	 * @param mvd the mvd it came from
	 * @throws MVDTestException if the data don't match
	 */
	private static void compareListToMvd( byte[] list, MVD mvd ) 
		throws MVDTestException
	{
		String response = new String( list );
		String[] parts = response.split("\n");
		for ( int i=0;i<parts.length;i++ )
			readLine( parts[i], mvd );
	}
	/**
	 * Read a single line of the version information
	 * @param line the line to read
	 * @param mvd the mvd to get version info from
	 * @throws MVDTestException if the data didn't match
	 * that in the mvd
	 */
	private static void readLine( String line, MVD mvd )
		throws MVDTestException
	{
		String[] parts = line.split(
			"Id=|group=|backup=|short-name=|long-name=");
		if ( parts.length == 6 )
		{
			int vId = Integer.parseInt( parts[1].trim() );
			String shortName = mvd.getVersionShortName( vId );
			String longName = mvd.getVersionLongName( vId );
			short gId = mvd.getGroupForVersion( vId );
			short backup = mvd.getBackupForVersion( vId );
			String group = mvd.getGroupName( gId );
			if ( !shortName.equals(parts[4].trim()) )
				throw new MVDTestException("version short name "
					+shortName
					+" and that read by list command ("
					+parts[4].trim()+") not equal");
			if ( !longName.equals(parts[5].trim()) )
				throw new MVDTestException("version long name "
					+longName
					+" and that read by list command ("
					+parts[5].trim()+") not equal");
			if ( !group.equals(parts[2].trim()) )
				throw new MVDTestException("version group name "
					+group
					+" and that read by list command ("
					+parts[2].trim()
					+") not equal");
			if ( backup != Short.parseShort(parts[3].trim()) )
				throw new MVDTestException("version backup value "
					+backup
					+"and that read by list command ("
					+parts[3].trim()
					+") not equal");
			// signal success of this iteration
			System.out.print(".");
		}
		else
			throw new MVDTestException(
				"Invalid list response line: "+line );
	}
	/**
	 * Read a version, capturing it. Compare the result to the original 
	 * put into the file. 
	 */
	private static void doReadTest()
	{
		try
		{
			System.out.print("Testing read command ");
			String testData = TEST_DATA+File.separator+BLESSED_DAMOZEL;
			File folder = new File( testData );
			String mvdName = createTestMVD( folder );
			// now read the source files one at a time
			File testDataFolder = new File( testData );
			File[] versions = testDataFolder.listFiles();
			versions = removeDotFiles( versions );
			for ( int vId=1;vId<=versions.length;vId++ )
			{
				String vIdStr = Integer.toString( vId );
				String[] args0 = {"-c","read","-m",mvdName,"-v",vIdStr};
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream( bos, true, "UTF-8" );
				MvdTool.run( args0, ps );
				ps.close();
				byte[] data1 = bos.toByteArray();
				FileInputStream fis = new FileInputStream( versions[vId-1] );
				byte[] data2 = new byte[(int)versions[vId-1].length()];
				fis.read( data2 );
				compareTwoByteArrays( data1, data2 );
				System.out.print(".");
			}
			testsPassed++;
			System.out.println(" test passed.");
		}
		catch ( Exception e )
		{
			doTestFailed( e );
		}
	}
	/**
	 * Compare two byte arrays and throw an exception if not equal
	 * @param data1 the first byte array
	 * @param data2 the second byte array
	 * @throws MVDTestException the exception raised
	 */
	private static void compareTwoByteArrays( byte[] data1, byte[] data2 )
		throws MVDTestException
	{
		if ( data2.length != data1.length )
			throw new MVDTestException(
				"Original and read versions not the same length");
		for ( int i=0;i<data2.length;i++ )
		{
			if ( data1[i] != data2[i] )
				throw new MVDTestException(
					"Data before and after read not equal");
		}
	}
	/**
	 * Update one version in the MVD. Take out a version at random. 
	 * Then change it somehow. Then save it back. Check that the saved 
	 * back text is what you put in.
	 */
	private static void doUpdateTest()
	{
		try
		{
			System.out.print("Testing update command ");
			// make mvd & load it
			String testData = TEST_DATA+File.separator+BLESSED_DAMOZEL;
			File testDataFolder = new File( testData );
			String mvdName = createTestMVD( testDataFolder );
			File mvdFile = new File( mvdName );
			MVD mvd = MVDFile.internalise( mvdName, null );
			int numVersions = mvd.numVersions();
			Random rand = new Random( System.currentTimeMillis() );
			int vId = rand.nextInt(numVersions)+1;
			byte[] data1 = mvd.getVersion( vId );
			// replace every 10th 'b' with 'x'
			for ( int bCount=0,i=0;i<data1.length;i++ )
			{
				if ( data1[i] == 'b' )
				{
					if ( bCount == 9 )
					{
						bCount = 0;
						data1[i] = 'x';
					}
					else
						bCount++;
				}
			}
			String vIdStr = new Integer(vId).toString();
			String tempName = TEST_FOLDER+File.separator+"temp.txt";
			File tempFile = new File( tempName );
			FileOutputStream fos = new FileOutputStream( tempFile );
			fos.write( data1 );
			fos.close();
			String[] args0 = {"-c","update","-m",mvdName,"-v",vIdStr,"-t",
				tempName};
			MvdTool.run( args0, out );
			mvd = MVDFile.internalise( mvdName, null );
			byte[] data2 = mvd.getVersion( vId );
			compareTwoByteArrays( data1, data2 );
			System.out.print(".");
			// updated file can't be used for other tests
			mvdFile.delete();
			testsPassed++;
			System.out.println(" test passed.");
		}
		catch ( Exception e )
		{
			doTestFailed( e );
		}
	}
	/**
	 * Construct an MVD and export it to XML. Then reimport it, check 
	 * that the versions are the same, that the text of each version is 
	 * the same, that the same groups and description are as originally 
	 * specified.
	 */
	private static void doImportExportTest()
	{
		try
		{
			System.out.print("Testing export command ");
			// create an MVD from the Blessed Damozel
			String testData = TEST_DATA+File.separator+BLESSED_DAMOZEL;
			String mvdXMLName = TEST_FOLDER+File.separator
				+BLESSED_DAMOZEL+".xml";
			File testDataFolder = new File( testData );
			String mvdName = createTestMVD( testDataFolder );
			MVD mvd1 = MVDFile.internalise( mvdName, null );
			// export it to XML
			String[] args0 = {"-c","export","-m",mvdName,"-e",
				"UTF-8","-x",mvdXMLName};
			MvdTool.run( args0, out );
			System.out.print(".");
			testsPassed++;
			System.out.println(" test passed.");
			// now test the import command
			System.out.print("Testing import command ");
			String[] args1 = {"-c","import","-m",mvdName,"-x",mvdXMLName};
			MvdTool.run( args1, out );
			// this should now be a different MVD
			MVD mvd2 = MVDFile.internalise( mvdName, null );
			compareDescriptions( mvd1, mvd2 );
			compareGroups( mvd1, mvd2 );
			compareVersionDefinitions( mvd1, mvd2 );
			compareVersions( mvd1, mvd2 );
			testsPassed++;
			System.out.println(" test passed.");
		}
		catch ( Exception e )
		{
			doTestFailed( e );
		}
	}
	/**
	 * Compare the actual versions of two MVDs. 
	 * @param mvd1 the first MVD
	 * @param mvd2 the second MVD
	 * @throws MVDTestException if they were not the same
	 */
	private static void compareVersions( MVD mvd1, MVD mvd2 ) 
		throws MVDTestException
	{
		int numVersions = mvd1.numVersions();
		for ( int vId=1;vId<=numVersions;vId++ )
		{
			byte[] data1 = mvd1.getVersion( vId );
			byte[] data2 = mvd2.getVersion( vId );
			compareTwoByteArrays( data1, data2 );
		}
	}
	/**
	 * Compare the version definitions of two MVDs. 
	 * @param mvd1 the first MVD
	 * @param mvd2 the second MVD
	 * @throws MVDTestException if they were not the same
	 */
	private static void compareVersionDefinitions( MVD mvd1, MVD mvd2 ) 
		throws MVDTestException
	{
		int numVersions1 = mvd1.numVersions();
		int numVersions2 = mvd2.numVersions();
		if ( numVersions1 != numVersions2 )
			throw new MVDTestException(
				"Number of versions in MVDs don't match" );
		for ( int vId=1;vId<=numVersions1;vId++ )
		{
			// check short names
			String shortName1 = mvd1.getVersionShortName( vId );
			String shortName2 = mvd2.getVersionShortName( vId );
			if ( !shortName1.equals(shortName2) )
				throw new MVDTestException(
					"Short names for version "+vId+" don't match" );
			// check long names
			String longName1 = mvd1.getVersionLongName( vId );
			String longName2 = mvd2.getVersionLongName( vId );
			if ( !longName1.equals(longName2) )
				throw new MVDTestException(
					"Long names for version "+vId+" don't match" );
			// check group ids
			short group1 = mvd1.getGroupForVersion( vId );
			short group2 = mvd2.getGroupForVersion( vId );
			if ( group1 != group2 )
				throw new MVDTestException(
					"Groups for version "+vId+" don't match" );
			// check backup values
			int backup1 = mvd1.getBackupForVersion( vId );
			int backup2 = mvd2.getBackupForVersion( vId );
			if ( backup1 != backup2 )
				throw new MVDTestException(
					"Bckup versions for version "+vId+" don't match" );
		}
	}
	/**
	 * Compare the group definitions from two MVDs. 
	 * @param mvd1 the first MVD
	 * @param mvd2 the second MVD
	 * @throws MVDTestException if they were not the same
	 */
	private static void compareGroups( MVD mvd1, MVD mvd2 ) 
		throws MVDTestException
	{
		int numGrps1 = mvd1.numGroups();
		int numGrps2 = mvd2.numGroups();
		if ( numGrps1 != numGrps2 )
			throw new MVDTestException(
				"Number of groups in MVDs don't match" );
		for ( short gId=1;gId<=numGrps1;gId++ )
		{
			String gName1 = mvd1.getGroupName( gId );
			String gName2 = mvd2.getGroupName( gId );
			if ( !gName1.equals(gName2) )
				throw new MVDTestException(
					"Group names don't match" );
			short gParent1 = mvd1.getGroupParent( gId );
			short gParent2 = mvd2.getGroupParent( gId );
			if ( gParent1 != gParent2 )
				throw new MVDTestException(
					"Group parents don't match" );
		}
	}
	/**
	 * Compare the description strings from two MVDs. 
	 * @param mvd1 the first MVD
	 * @param mvd2 the second MVD
	 * @throws MVDTestException if they were not the same
	 */
	private static void compareDescriptions( MVD mvd1, MVD mvd2 ) 
		throws MVDTestException
	{
		String firstDesc = mvd1.getDescription();
		String secondDesc = mvd2.getDescription();
		if ( !firstDesc.equals(secondDesc) )
			throw new MVDTestException("First MVD description: "
				+firstDesc+" not same as second: "+secondDesc );
		System.out.print(".");
	}
	/**
	 * Use the previously built archive to reconstruct an MVD. Check 
	 * that each version is the same as that in the archive folder.
	 */
	private static void doUnarchiveTest()
	{
		System.out.print("Testing unarchive command ");
		try
		{
			// assume that this already exists
			String archiveName = TEST_FOLDER+File.separator+RENAISSANCE;
			String mvdName = TEST_FOLDER+File.separator+RENAISSANCE+".mvd";
			File archiveFolder = new File( archiveName );
			String[] args0 = {"-c","unarchive","-m",mvdName,"-a",archiveName};
			MvdTool.run( args0, out );
			MVD mvd = MVDFile.internalise( mvdName, null );
			for ( int i=0;i<mvd.numVersions();i++ )
			{
				byte[] data = mvd.getVersion( i+1 );
				String shortName = mvd.getVersionShortName( i+1 );
				File archiveFile = new File( archiveFolder, shortName );
				if ( !archiveFile.exists() )
					throw new MVDTestException("Archive file "
						+shortName+" notFound");
				if ( archiveFile.length() != data.length )
					throw new MVDTestException(
						"Archive file and version in MVD not the same length");
				System.out.print(".");
			}
			testsPassed++;
			System.out.println(" test passed.");
		}
		catch ( Exception e )
		{
			doTestFailed( e );
		}
	}

	/**
	 * Create an empty MVD, set its description to something then read
	 * the description to see if it is the same as the new value.
	 */
	private static void doDescriptionTest()
	{
		System.out.print("Testing description command ");
		try
		{
			File testFolder = new File( TEST_FOLDER );
			String mvdName = TEST_FOLDER+File.separator+"test.mvd";
			if ( !testFolder.exists() )
				testFolder.mkdir();
			String[] args0 = { "-c","create","-m",mvdName,"-d","test" };
			MvdTool.run( args0, out );
			String[] args2 = {"-c","description","-m",mvdName,"-d","new value"};
			MvdTool.run( args2, out );
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream( bos );
			String[] args3 = {"-c","description","-m",mvdName};
			MvdTool.run( args3, ps );
			ps.close();
			String description = bos.toString().trim();
			if ( !description.equals("new value") )
				throw new MVDTestException("Description string not equal to set value");
			System.out.print(".");
			testsPassed++;
			System.out.println(" test passed.");
		}
		catch ( Exception e )
		{
			doTestFailed( e );
		}
	}
	/**
	 * Test that versions can be successfully deleted from 
	 * an existing MVD until it is empty. Test the validity 
	 * of the graph after each deletion. Rely on the  
	 * Blessed Damozel mvd having been already created by the 
	 * add test
	 */
	private static void doDeleteTest()
	{
		System.out.print("Testing delete command ");
		try
		{
			// create an MVD from the Blessed Damozel
			File testFolder = new File( TEST_DATA+File.separator+BLESSED_DAMOZEL );
			String mvdName = createTestMVD( testFolder );
			File mvdFile = new File( mvdName );
			// choose versions at random to delete
			File[] versions = testFolder.listFiles();
			versions = removeDotFiles( versions );
			Random rand = new Random( System.currentTimeMillis() );
			MVD mvd = MVDFile.internalise( mvdName, null );
			for ( int i=0;i<versions.length;i++ )
			{
				int n = rand.nextInt( mvd.numVersions() );
				// temporary debug
				String vId = new Integer(n+1).toString();
				String[] args0 = {"-c","delete","-m",mvdName,"-v",vId};
				MvdTool.run( args0, out );
				mvd = MVDFile.internalise( mvdName, null );
				if ( mvd.numVersions() != versions.length-(i+1) )
					throw new MVDTestException("Failed to delete version!");
				Converter conv = new Converter();
				VariantGraph g = conv.create( mvd.getPairs(), mvd.numVersions() );
				g.verify();
				System.out.print(".");
			}
			mvdFile.delete();
			testsPassed++;
			System.out.println(" test passed.");
		}
		catch ( Exception e )
		{
			doTestFailed( e );
		}
	}
	/**
	 * Build MVDs from all the folders in the TEST_DATA folder
	 */
	private static void doAddTest()
	{
		System.out.print("Testing add command ");
		try
		{
			File testFolder = new File( TEST_FOLDER );
			// remove mvds
			File[] mvds = testFolder.listFiles();
			for ( int i=0;i<mvds.length;i++ )
			{
				if ( mvds[i].isFile() && mvds[i].getName().endsWith(".mvd") )
					mvds[i].delete();
			}
			if ( !testFolder.exists() )
				testFolder.mkdir();
			File testData = new File( TEST_DATA );
			if ( !testData.exists() )
				throw new MVDTestException("Missing test data folder");
			String[] testFolders = testData.list();
			if ( testFolders.length == 0 )
				throw new MVDTestException("Nothing in test data folder");
			for ( int i=0;i<testFolders.length;i++ )
			{
				File subFolder = new File( testData, testFolders[i] );
				if ( subFolder.isDirectory() )
					createTestMVD( subFolder );
			}
			testsPassed++;
			System.out.println(" test passed.");
		}
		catch ( Exception e )
		{
			doTestFailed( e );
		}
	}
	/**
	 * Build a single MVD from the files in the given folder
	 * @param versions the src files
	 * @return the built MVD's path
	 * @throws Exception
	 */
	private static void checkMVD( File[] versions, String mvdName ) 
		throws Exception
	{
		String[] args2 = {"-c","read","-m",mvdName,"-v",""};
		for ( int vId=1,i=0;i<versions.length;i++,vId++ )
		{
			// create a temporary file containing version i
			String tempOutName = TEST_FOLDER+File.separator+TEMP_OUT;
			File tempOutFile = new File( tempOutName );
			if ( tempOutFile.exists() )
				tempOutFile.delete();
			PrintStream tempOut = new PrintStream( new FileOutputStream(tempOutFile) );
			args2[5] = Integer.toString( vId );
			MvdTool.run( args2, tempOut );
			tempOut.close();
			// now read it back in and compare it to the original
			FileInputStream fis1 = new FileInputStream( versions[i] );
			FileInputStream fis2 = new FileInputStream( tempOutFile );
			byte[] data1 = new byte[(int)versions[i].length()];
			byte[] data2 = new byte[(int)tempOutFile.length()];
			fis1.read( data1 );
			fis2.read( data2 );
			compareTwoByteArrays( data1, data2 );
		}
	}
	/**
	 * Create an MVD from a folder for testing
	 * @param folder the folder to build it from
	 * @return the MVD filename
	 * @throws MVDException if the creation failed
	 */
	private static String createTestMVD( File folder ) 
		throws Exception
	{
		if ( !folder.isDirectory() || !folder.exists() )
			throw new MVDTestException("Missing folder "
				+folder.getName() );
		String mvdName = TEST_FOLDER+File.separator
			+folder.getName()+".mvd";
		if ( new File(mvdName).exists() )
			return mvdName;
		else
		{
			String[] args0 = { "-c","create","-m",mvdName,"-d",folder.getName()+" test" };
			MvdTool.run( args0, out );
			// add each version
			File[] versions = folder.listFiles();
			versions = removeDotFiles( versions );
			String[] args1 = { "-c","add","-m",mvdName,"-g",
				"TOP LEVEL","-s","","-l","","-t","","-v","" };
			for ( int vId=1,i=0;i<versions.length;i++,vId++ )
			{
				args1[7] = "V"+vId;
				args1[9] = versions[i].getName();
				args1[11] = versions[i].getAbsolutePath();
				args1[13] = Integer.toString( vId );
				emptyStream.clear();
				MvdTool.run( args1, out );
				System.out.print( "." );
				// else ignore nested folders
			}
			checkMVD( versions, mvdName );
			return mvdName;
		}
	}
	/**
	 * Remove dot files and nested directories
	 * @param versions versions to be sanitised
	 * @return new versions with no files starting with dot or folders
	 */
	private static File[] removeDotFiles( File[] versions )
	{
		Vector<File> files = new Vector<File>();
		for ( int i=0;i<versions.length;i++ )
			if ( !versions[i].getName().startsWith(".") 
				&& versions[i].isFile() )
				files.add( versions[i] );
		versions = new File[files.size()];
		files.toArray( versions );
		return versions;
	}
	/**
	 * Test help commands - examples of usage
	 */
	private static void doHelpTest()
	{
		String[] args0 = { "-h" };
		String[] args1 = { "-h","add"};
		String[] args2 = { "-h","archive"};
		String[] args3 = { "-h","compare"};
		String[] args4 = { "-h","create"};
		String[] args5 = { "-h","delete"};
		String[] args6 = { "-h","description"};
		String[] args7 = { "-h","export"};
		String[] args8 = { "-h","find"};
		String[] args9 = { "-h","help"};
		String[] args10 = { "-h","import"};
		String[] args11 = { "-h","list"};
		String[] args12 = { "-h","read"};
		String[] args13 = { "-h","update"};
		String[] args14 = { "-h","usage"};
		String[] args15 = { "-h","variants"};
		emptyStream.clear();
		try
		{
			System.out.print("Testing help command ");
			try
			{
				MvdTool.run( args0, out );
				throw new MVDException("Failed to detect faulty arguments");
			}
			catch ( Exception e )
			{
				if ( !(e instanceof MVDToolException) )
					throw new MVDTestException( e );
				System.out.print(".");
			}
			testHelpCommand( args1 );
			testHelpCommand( args2 );
			testHelpCommand( args3 );
			testHelpCommand( args4 );
			testHelpCommand( args5 );
			testHelpCommand( args6 );
			testHelpCommand( args7 );
			testHelpCommand( args8 );
			testHelpCommand( args9 );
			testHelpCommand( args10 );
			testHelpCommand( args11 );
			testHelpCommand( args12 );
			testHelpCommand( args13 );
			testHelpCommand( args14 );
			testHelpCommand( args15 );
			testsPassed++;
			System.out.println(" test passed.");
		}
		catch ( Exception e )
		{
			doTestFailed( e );
		}
	}
	/**
	 * Test a single help command
	 * @param args the arguments to generate the help
	 * @throws MVDException if the tool or the test failed
	 */
	static void testHelpCommand( String[] args ) throws MVDException
	{
		emptyStream.clear();
		MvdTool.run( args, out );
		if ( emptyStream.printedBytes() == 0 )
			throw new MVDTestException( "tool failed to produce example text");
		//System.out.print(emptyStream.printedBytes());
		System.out.print(".");
	}
	/**
	 * Create an empty MVD and check that it reloads
	 */
	private static void doCreateTest()
	{
		File testFolder = new File( TEST_FOLDER );
		if ( !testFolder.exists() )
			testFolder.mkdir();
		String[] args0 = { "-c","create","-m",TEST_FOLDER
			+File.separator+"test.mvd" };
		try
		{
			System.out.print("Testing create command ");
			MvdTool.run( args0, out );
			System.out.print(".");
			MvdTool.loadMVD();
			System.out.print(".");
			testsPassed++;
			System.out.println(" test passed.");
		}
		catch ( Exception e )
		{
			doTestFailed( e );
		}
	}
	/**
	 * Run test on the variants of the usage command
	 */
	private static void doUsageTest()
	{
		String[] args0 = {};
		String[] args1 = {"-?"};
		String[] args2 = {"-x","dontcreate.xml","-p","-?","-m","notthere.mvd"};
		// and two sets of invalid arguments should throw MVDToolExceptions
		String[] args3 = {"-c","add","-t","textfile.txt","-?","-m","notthere.mvd"};
		String[] args4 = {"-?","-t","textfile.txt","-m","notthere.mvd","-c","add"};
		try
		{
			System.out.print("Testing usage command ");
			MvdTool.run( args0, out );
			System.out.print(".");
			MvdTool.run( args1, out );
			System.out.print(".");
			MvdTool.run( args2, out );
			System.out.print(".");
			// these two should generate a specific error
			try
			{
				MvdTool.run( args3, out );
				throw new MVDException("Failed to detect faulty arguments");
			}
			catch ( MVDException mvde )
			{
				if ( !(mvde instanceof MVDToolException) )
					throw new MVDTestException( mvde );
				System.out.print(".");
			}
			try
			{
				MvdTool.run( args4, out );
				throw new MVDException("Failed to detect faulty arguments");
			}
			catch ( MVDException mvde )
			{
				if ( !(mvde instanceof MVDToolException) )
					throw new MVDTestException( mvde );
				System.out.print(".");
			}
			testsPassed++;
			System.out.println(" test passed.");
		}
		catch ( Exception e )
		{
			doTestFailed( e );
		}
	}
	/**
	 * Shared code for each test failure
	 * @param e the exception that caused the failure
	 */
	private static void doTestFailed( Exception e )
	{
		testsFailed++;
		if ( printStackTrace )
			e.printStackTrace();
		System.out.print( " error: "+e.getMessage()+".");
		System.out.println(" test failed.");
	}
}
