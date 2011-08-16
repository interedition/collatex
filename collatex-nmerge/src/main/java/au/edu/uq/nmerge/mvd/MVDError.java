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

import java.io.FileWriter;
import java.io.File;
/**
 * Log errors to a /tmp file
 * OK, this is quick and dirty
 */
public class MVDError 
{
	public static void log( String message )
	{
		try
		{
			File tmpDir = new File( System.getProperty("java.io.tmpdir"));
			File tmpFile = new File( tmpDir,"mvderror.log" );
			FileWriter fw = new FileWriter(tmpFile,true);
			fw.write( message+"\n" );
			fw.close();
		}
		catch ( Exception e )
		{
		}
	}
}
