/*
 * Copyright (C) 2019 Gregor Pintar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package SpeakerSim.GUI;

import java.util.*;
import javax.swing.AbstractListModel;

class SortedListModel<E> extends AbstractListModel<E>
{
    private final SortedSet<E> items;

    public SortedListModel()
    {
        items = new TreeSet<E>();
    }

    @Override
    public int getSize()
    {
        return items.size();
    }

    @Override
    public E getElementAt(int index)
    {
        if (getSize() <= index)
        {
            return null;
        }
        
        Iterator<E> iter = items.iterator();
        
        for (int i = 0; i < index; i++)
        {
            iter.next();
        }
        return iter.next();
    }

    public void addElement(E element)
    {
        items.remove(element);
        
        if (items.add(element))
        {
            fireContentsChanged(this, 0, getSize());
        }
    }

    public void addAll(E elements[])
    {
        items.addAll(Arrays.asList(elements));
        fireContentsChanged(this, 0, getSize());
    }
    
    public boolean removeElement(E element)
    {
        boolean removed = items.remove(element);
        if (removed)
        {
            fireContentsChanged(this, 0, getSize());
        }
        return removed;
    }

    public boolean contains(E element)
    {
        return items.contains(element);
    }
    
    public Iterator<E> iterator()
    {
        return items.iterator();
    }
}
