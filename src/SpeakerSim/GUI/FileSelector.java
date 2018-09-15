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

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class FileSelector extends JFileChooser
{
    private final String defaultExt;
    
    public FileSelector(String defaultExtention)
    {
        super();
        
        defaultExt = defaultExtention;
    }
    
    @Override
    public File getSelectedFile()
    {
        File f = super.getSelectedFile();

        if (f != null)
        {
            if (!super.getFileFilter().accept(f))
            {
                return new File(f.toString() + defaultExt);
            }
        }
        return f;
    }

    @Override
    public void approveSelection()
    {
        File f = getSelectedFile();

        if (f.exists())
        {
            if (getDialogType() == JFileChooser.SAVE_DIALOG)
            {
                switch (JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION))
                {
                    case JOptionPane.YES_OPTION:
                        super.approveSelection();
                    case JOptionPane.NO_OPTION:
                    case JOptionPane.CLOSED_OPTION:
                        return;

                    case JOptionPane.CANCEL_OPTION:
                        cancelSelection();
                        return;
                }
            }
            else
            {

            }
        }
        super.approveSelection();
    }
}
