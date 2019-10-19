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
    private static final DataFlavor DATA_FLAVOR = new DataFlavor(IItem.class, "SpeakerSim");
    private static final DataFlavor[] FLAVORS = new DataFlavor[] { DATA_FLAVOR };
    
    private final String item;
    
    public CopyPaste(IItem item)
    {
        this.item = item.toJSON().toString();
    }
    
    public static void set(IItem item)
    {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        CopyPaste obj = new CopyPaste(item);
        clipboard.setContents(obj, obj);
    }
    
    private static String getString()
    {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        if (contents != null && contents.isDataFlavorSupported(CopyPaste.DATA_FLAVOR))
        {
            try
            {
                return (String) contents.getTransferData(CopyPaste.DATA_FLAVOR);
            }
            catch (UnsupportedFlavorException | IOException ex)
            {
                return null;
            }
        }
        
        return null;
    }
    
    public static IItem get()
    {
        String data = getString();
        if (data != null)
        {
            return Item.createItem(Json.parse(data));
        }
        
        return null;
    }
    
    public static Class<?> getType()
    {
        String data = getString();
        if (data != null)
        {
            try
            {
                return Class.forName("SpeakerSim." + Json.parse(data).asObject().getString("Type", ""));
            }
            catch (ClassNotFoundException ex)
            {
                return null;
            }
        }
        
        return null;
    }
    
    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        return FLAVORS;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return DATA_FLAVOR == flavor;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
    {
        if (flavor == DATA_FLAVOR)
        {
            return item;
        }

        throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents)
    {
        
    }
}
