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

import java.awt.*;
import java.beans.*;
import java.lang.reflect.*;
import javax.swing.*;

public class Main
{
    public static void main(String[] args)
    {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException(Thread t, Throwable e)
            {
                UI.throwable(null, e);
            }
        });

        try
        {
            if (System.getProperty("os.name").contains("Windows"))
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        }
        catch (Exception ex)
        {

        }
        
        try
        {
            Toolkit tk = Toolkit.getDefaultToolkit();
            Field awtAppClassNameField = tk.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            awtAppClassNameField.set(tk, "SpeakerSim");
        }
        catch (Exception ex)
        {

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
                new MainWindow(args[0]).setVisible(true);
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
