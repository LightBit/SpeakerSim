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

import SpeakerSim.Fnc;
import SpeakerSim.HandledException;
import SpeakerSim.Project;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.filechooser.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URI;

public final class UI
{
    public final static DefaultFormatterFactory FORMATTER = new DefaultFormatterFactory(new NumberFormatter(Fnc.DECIMAL_FORMAT));
    
    private UI()
    {
        
    }
    
    public static PropertyChangeListener validator()
    {
        return new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                UI.validate(e);
            }
        };
    }
    
    public static PropertyChangeListener validator(final double min, final double max)
    {
        return new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                UI.validate(e, min, max);
            }
        };
    }
    
    public static PropertyChangeListener validator(final double min)
    {
        return validator(min, Double.MAX_VALUE);
    }
    
    public static boolean validate(PropertyChangeEvent e)
    {
        Number number = (Number) e.getNewValue();

        if (number == null || number.doubleValue() <= 0)
        {
            number = (Number) e.getOldValue();
            
            if (number == null || number.doubleValue() <= 0)
            {
                ((JFormattedTextField) e.getSource()).setValue(1);
            }
            else
            {
                ((JFormattedTextField) e.getSource()).setValue(number);
            }
            return false;
        }
        
        return true;
    }
    
    public static boolean validate(PropertyChangeEvent e, double min, double max)
    {
        Number number = (Number) e.getNewValue();
        
        if (number == null)
        {
            number = (Number) e.getOldValue();
            
            if (number == null || number.doubleValue() < min)
            {
                ((JFormattedTextField) e.getSource()).setValue(min);
            }
            else if (number.doubleValue() > max)
            {
                ((JFormattedTextField) e.getSource()).setValue(max);
            }
            else
            {
                ((JFormattedTextField) e.getSource()).setValue(number);
            }
            return false;
        }
        
        double value = number.doubleValue();

        if (value < min || value > max)
        {
            number = (Number) e.getOldValue();
            
            if (number == null || number.doubleValue() < min)
            {
                ((JFormattedTextField) e.getSource()).setValue(min);
            }
            else if (number.doubleValue() > max)
            {
                ((JFormattedTextField) e.getSource()).setValue(max);
            }
            else
            {
                ((JFormattedTextField) e.getSource()).setValue(number);
            }
            return false;
        }
        
        return true;
    }
    
    public static boolean validate(PropertyChangeEvent e, double min)
    {
        return validate(e, min, Double.MAX_VALUE);
    }
    
    public static double getDouble(JFormattedTextField field)
    {
        Number value = (Number) field.getValue();
        
        if (value != null)
        {
            return value.doubleValue();
        }
        
        return 0;
    }
    
    public static double getDouble(PropertyChangeEvent e)
    {
        Number value = (Number) e.getNewValue();
        
        if (value != null)
        {
            return value.doubleValue();
        }
        
        return 0;
    }
    
    public static int getInt(JFormattedTextField field)
    {
        Number value = (Number) field.getValue();
        
        if (value != null)
        {
            return value.intValue();
        }
        
        return 0;
    }
    
    public static int getInt(PropertyChangeEvent e)
    {
        Number value = (Number) e.getNewValue();
        
        if (value != null)
        {
            return value.intValue();
        }
        
        return 0;
    }
    
    public static void hideButton(JButton button)
    {
        button.setText("");
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setEnabled(false);
    }
    
    public static void setPanelEnabled(JPanel panel, Boolean isEnabled)
    {
        panel.setEnabled(isEnabled);

        Component[] components = panel.getComponents();

        for (Component component : components)
        {
            if (component instanceof JPanel)
            {
                setPanelEnabled((JPanel) component, isEnabled);
            }
            if (component != null)
            {
                component.setEnabled(isEnabled);
            }
        }
    }
    
    public static void warning(String message)
    {
        JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    public static void error(String message)
    {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private static String stackTraceToString(Throwable e)
    {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace())
        {
            sb.append(element.toString());
            sb.append("\r\n");
        }
        return sb.toString();
    }
    
    public static void exception(Component parent, Throwable e)
    {
        //Runtime runtime = Runtime.getRuntime();
        
        String stackTrace = "SpeakerSim " + Project.currentVersion()
                + "\r\n" + System.getProperty("java.runtime.name")
                + " " + System.getProperty("java.runtime.version")
                + " on " + System.getProperty("os.name")
                //+ "\r\nMemory usage: " + runtime.totalMemory() / 1024 / 1024 + "/" + runtime.maxMemory() / 1024 / 1024
                + "\r\n\r\nException:\r\n" + e.toString() 
                + "\r\n\r\nStack trace:\r\n" + stackTraceToString(e);

        JTextArea ta = new JTextArea(15, 50);
        ta.setText(stackTrace);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        ta.setCaretPosition(0);
        ta.setEditable(false);

        switch (JOptionPane.showOptionDialog(parent, new JScrollPane(ta), "Unexpected error", 0, JOptionPane.ERROR_MESSAGE, null, new String[] {"OK", "Copy", "Save"}, "OK"))
        {
            case 1:
                StringSelection selection = new StringSelection(stackTrace);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                break;

            case 2:
                FileSelector fc = new FileSelector(".txt");
                fc.setFileFilter(new FileNameExtensionFilter("Text file", "txt"));

                if (fc.showSaveDialog(null) == FileSelector.APPROVE_OPTION)
                {
                    File file = fc.getSelectedFile();
                    try (FileWriter fw = new FileWriter(file))
                    {
                        fw.write(stackTrace);
                    }
                    catch (IOException ex)
                    {
                        throwable(parent, ex);
                    }
                }
                break;

            default:
                break;
        }
    }
    
    public static void throwable(Component parent, Throwable e)
    {
        if (e instanceof Error)
        {
            String message = e.getLocalizedMessage();
            if (message == null)
            {
                message = e.toString();
            }
            error(message);
            System.exit(-1);
        }
        else if (e instanceof HandledException)
        {
            String message = e.getMessage();
            
            while ((e = e.getCause()) != null)
            {
                String m = e.getLocalizedMessage();
                if (m != null)
                {
                    message += ": " + m;
                }
                else
                {
                    message += ": " + e.toString();
                }
            }
            
            error(message);
        }
        else if (e instanceof IOException)
        {
            error("Error reading file: " + e.getLocalizedMessage());
        }
        else
        {
            exception(parent, e);
        }
    }
    
    public static boolean ask(Component parent, String question)
    {
        int dialogButton = JOptionPane.showConfirmDialog(null, question, "Warning", JOptionPane.YES_NO_OPTION);
        return dialogButton == JOptionPane.YES_OPTION;
    }
    
    public static String askInput(Component parent, String title, String question)
    {
        return JOptionPane.showInputDialog(null, question, title, JOptionPane.PLAIN_MESSAGE);
    }
    
    public static String askInput(Component parent, String title, String question, String defaultValue)
    {
        return (String) JOptionPane.showInputDialog(null, question, title, JOptionPane.PLAIN_MESSAGE, null, null, defaultValue);
    }
    
    public static boolean dialog(Component parent, String title, Object content)
    {
        return JOptionPane.showConfirmDialog(parent, content, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }
    
    public static String getSelectedTab(JTabbedPane tabs)
    {
        int index = tabs.getSelectedIndex();
        
        if (index != -1)
        {
            return tabs.getTitleAt(index);
        }
        
        return null;
    }
    
    public static void setSelectedTab(JTabbedPane tabs, String tab)
    {
        for (int i = 0; i < tabs.getTabCount(); i++)
        {
            if (tabs.getTitleAt(i) == tab)
            {
                tabs.setSelectedIndex(i);
            }
        }
    }
    
    public static void openURL(String url)
    {
        try
        {
            Desktop.getDesktop().browse(new URI(url));
        }
        catch (Exception ex)
        {
            try
            {
                String os = System.getProperty("os.name").toLowerCase();
                Runtime rt = Runtime.getRuntime();

                if (os.contains("win"))
                {
                    rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
                }
                else if (os.contains("nux"))
                {
                    rt.exec("xdg-open " + url);
                }
                else if (os.contains("mac"))
                {
                    rt.exec("open " + url);
                }
            }
            catch (Exception e)
            {
                
            }
        }
    }
}
