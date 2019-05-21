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

import SpeakerSim.Project;
import java.awt.*;
import java.beans.*;
import java.lang.reflect.*;
import javax.swing.*;
import java.util.Base64;
import io.sentry.Sentry;
import io.sentry.SentryClientFactory;
import io.sentry.DefaultSentryClientFactory;
import io.sentry.context.ContextManager;
import io.sentry.context.SingletonContextManager;
import io.sentry.dsn.Dsn;

public class Main
{
    public static void main(String[] args)
    {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException(Thread t, Throwable e)
            {
                UI.throwable(e);
            }
        });
        
        SentryClientFactory scf = new DefaultSentryClientFactory()
        {
            @Override
            protected ContextManager getContextManager(Dsn dsn)
            {
                return new SingletonContextManager();
            }
        };
        Sentry.init(scf);
        Sentry.getContext().addTag("version", Project.currentVersionString());
        Sentry.getContext().addTag("machine_id", Base64.getEncoder().encodeToString(UI.machineId()));
        Sentry.getContext().addTag("arch", System.getProperty("os.arch"));
        Sentry.getContext().addTag("os", System.getProperty("os.name"));
        Sentry.getContext().addTag("os_version", System.getProperty("os.version"));
        Sentry.getContext().addTag("java_runtime", System.getProperty("java.runtime.name"));
        Sentry.getContext().addTag("java_version", System.getProperty("java.runtime.version"));

        try
        {
            if (System.getProperty("os.name").contains("Windows"))
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        }
        catch (Exception ex)
        {
            // ignore - non critical
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
            // ignore - non critical
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
            UI.throwable(ex);
            System.exit(-1);
        }
    }
}
