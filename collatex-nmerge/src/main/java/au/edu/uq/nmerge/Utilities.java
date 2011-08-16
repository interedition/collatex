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
import java.io.FileInputStream;
import java.util.Properties;

public class Utilities {
	/**
	 * Load a properties file describing the database properties for connecting to
	 * @param dbConn the name of the properties file (in the class path)
	 * @return a loaded resource bundle
	 */
	public static Properties loadDBProperties( String dbConn )
	{
		if ( dbConn == null )
			return null;
		else
		{
			String wd = System.getProperty("user.dir");
			File wdDir = new File( wd );
			Properties props = new Properties();
	        try
	        {
	        	FileInputStream fis = new FileInputStream(wdDir
	        		+File.separator+dbConn+".properties");
		        props.load(fis);    
		        fis.close();
		        return props;
	        }
	        catch ( Exception e )
	        {
	        	System.out.println(e.getMessage());
	        	return null;
	        }
		}
	}
}
