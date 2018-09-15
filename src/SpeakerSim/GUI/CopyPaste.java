/*
 * Copyright (C) 2017 Gregor Pintar <grpintar@gmail.com>
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package SpeakerSim.GUI;

import SpeakerSim.IItem;
import SpeakerSim.Item;

import com.eclipsesource.json.Json;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.io.IOException;

public class CopyPaste implements Transferable, ClipboardOwner
{
    private static final DataFlavor itemDataFlavor = DataFlavor.stringFlavor;//new DataFlavor(IItem.class, "SpeakerSim");
    private static final DataFlavor[] flavors = new DataFlavor[] { itemDataFlavor };
    
    private final IItem item;
    
    public CopyPaste(IItem item)
    {
        this.item = item;
    }
    
    public static void set(IItem item)
    {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        CopyPaste obj = new CopyPaste(item);
        clipboard.setContents(obj, obj);
    }
    
    public static IItem get()
    {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        if (contents != null && contents.isDataFlavorSupported(CopyPaste.itemDataFlavor))
        {
            try
            {
                return (IItem) Item.constructItem(Json.parse((String) contents.getTransferData(CopyPaste.itemDataFlavor)));
            }
            catch (Exception ex)
            {
                return null;
            }
            /*catch (UnsupportedFlavorException ex)
            {
                return null;
            }
            catch (IOException ex)
            {
                UI.exception(ex);
            }*/
        }
        
        return null;
    }
    
    public static Class getType()
    {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        if (contents != null && contents.isDataFlavorSupported(CopyPaste.itemDataFlavor))
        {
            try
            {
                return Class.forName("SpeakerSim." + Json.parse((String) contents.getTransferData(CopyPaste.itemDataFlavor)).asObject().getString("Type", ""));
            }
            catch (Exception ex)
            {
                return null;
            }
            /*catch (UnsupportedFlavorException | ClassNotFoundException ex)
            {
                return null;
            }
            catch (IOException ex)
            {
                UI.exception(ex);
            }*/
        }
        
        return null;
    }
    
    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return itemDataFlavor == flavor;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
    {
        if (flavor == itemDataFlavor)
        {
            return Item.itemToJSON(item).toString();
        }

        return null;
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents)
    {
        
    }
}
