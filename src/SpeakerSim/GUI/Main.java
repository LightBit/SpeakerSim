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

import SpeakerSim.Driver;
import SpeakerSim.HandledException;
import SpeakerSim.Project;
import java.awt.*;
import java.beans.*;
import javax.swing.*;
import java.io.File;

public class Main
{
    public static void main(String[] args)
    {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException(Thread t, Throwable e)
            {
                if (!t.isInterrupted())
                {
                    UI.throwable(null, e);
                }
            }
        });
        
        try
        {
            if (Integer.parseInt(System.getProperty("java.specification.version")) < 8)
            {
                UI.error(null, "Your Java Runtime Environment is too old. Java 8 or newer required.");
                System.exit(-1);
            }
        }
        catch (Exception ex)
        {
            // ignore
        }
        
        try
        {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (Exception ex)
        {
            // ignore
        }
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                final Object newValue = e.getNewValue();
                if (newValue instanceof JTextField)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            ((JTextField)newValue).selectAll();
                        }
                    });
                }
            }
        });
        
        ToolTipManager.sharedInstance().setInitialDelay(200);
        
        try
        {
            if (args.length >= 1)
            {
                String file = args[0];
                
                if (file.endsWith(".ssim"))
                {
                    new MainWindow(file).setVisible(true);
                }
                else if (file.endsWith(".sdrv"))
                {
                    Project p = new Project();
                    File f = new File(file);
                    Driver drv = Driver.importFromFile(f);
                    
                    if (new DriverWindow(null, p, drv).showDialog())
                    {
                        drv.save(f);
                    }
                }
                else
                {
                    throw new HandledException("Unsupported file format!");
                }
            }
            else
            {
                new MainWindow(null).setVisible(true);
            }
        }
        catch (Throwable ex)
        {
            UI.throwable(null, ex);
            System.exit(-1);
        }
    }
}
