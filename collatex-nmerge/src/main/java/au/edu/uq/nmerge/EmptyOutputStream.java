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

import java.io.OutputStream;
/**
 * This class is used to throw away output from the MvdTool during testing
 * @author Desmond Schmidt 3/5/09
 */
public class EmptyOutputStream extends OutputStream 
{
	int received;
	/**
	 * Find out if we received any data
	 * @return the number of bytes received since last flush
	 */
	public int printedBytes()
	{
		return received;
	}
	/**
	 * Does nothing
	 */
	public void close()
	{
	}
	/**
	 * Don't use flush because the system calls it unexpectedly
	 */
	public void clear()
	{
		received = 0;
	}
	/**
	 * Does nothing
	 */
    public void flush()
    {
    }
    /**
     * Does nothing
     * @param b the data
     */
	public void write( byte[] b )
	{
		received += b.length;
	}
	/**
	 * Does nothing
	 * @param b the data
	 * @param off the start offset in the data.
     * @param len the number of bytes to write. 
	 */
	public void write( byte[] b, int off, int len )
	{
		received += len;
	}
	/**
	 * Does nothing
	 * @param b a byte of data to write
	 */
	public void write( int b )
	{
		received++;
	}
}
