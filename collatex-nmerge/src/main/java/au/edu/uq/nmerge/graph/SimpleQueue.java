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
package au.edu.uq.nmerge.graph;

/**
 * A simple queue implementation designed to remove suspicious 
 * interactions with LinkedList during debugging. Perhaps could be 
 * replaced by LinkedList now.
 * @author Desmond Schmidt
 */
public class SimpleQueue<E> 
{
	Item head,tail;
	int size;
	/**
	 * This is just an item in the queue represented as a list
	 */
	private class Item
	{
		Item next;
		E item;
		Item( E item )
		{
			this.item = item;
		}
	}
    /**
     * Retrieves and removes the head of this queue, or null if this 
     * queue is empty.
     * @return the head of the queue
     */
	public E poll()
	{
		if ( head == null )
			return null;
		else
		{
			Item i = head;
			head = head.next;
			if ( head == null )
				tail = null;
			size--;
			return i.item;
		}
	}
	/**
	 * Add an item to the end of the queue
	 * @param item the item to add
	 */
	public void add( E item )
	{
		Item i = new Item(item);
		if ( head == null )
			head = tail = i;
		else
		{
			tail.next = i;
			tail = i;
		}
		size++;
	}
	/**
	 * Is the queue empty?
	 * @return true if at least one element is present
	 */
	public boolean isEmpty()
	{
		return size == 0;
	}
	/**
	 * Does this queue contain the given element?
	 * @param thing the item to test for
	 * @return true if it is there
	 */
	public boolean contains( E thing )
	{
		Item i = head;
		while ( i != null )
		{
			if ( i.item.equals(thing) )
				return true;
			i = i.next;
		}
		return false;
	}
	/**
	 * Return the size of the queue
	 * @return its size in items
	 */
	public int size()
	{
		return size;
	}
}
