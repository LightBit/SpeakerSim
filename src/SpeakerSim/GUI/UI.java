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

import SpeakerSim.HandledException;
import SpeakerSim.Project;
import io.sentry.Sentry;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.filechooser.*;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.Enumeration;

public final class UI
{
    private final static NumberFormat DECIMAL_FORMAT = NumberFormat.getNumberInstance();
    private final static NumberFormat INTEGER_FORMAT = NumberFormat.getIntegerInstance();
    
    private UI()
    {
        
    }
    
    static
    {
        DECIMAL_FORMAT.setMaximumFractionDigits(10);
        DECIMAL_FORMAT.setGroupingUsed(false);
        
        INTEGER_FORMAT.setGroupingUsed(false);
    }
    
    private static JFormattedTextField field(NumberFormat format, Comparable<?> min, Comparable<?> max)
    {
        NumberFormatter f = new NumberFormatter(format);
        f.setMinimum(min);
        f.setMaximum(max);
        //f.setAllowsInvalid(false);
        //f.setCommitsOnValidEdit(true);
        return new JFormattedTextField(f);
    }
    
    private static JFormattedTextField field(NumberFormat format, Comparable<?> min)
    {
        NumberFormatter f = new NumberFormatter(format);
        f.setMinimum(min);
        //f.setAllowsInvalid(false);
        //f.setCommitsOnValidEdit(true);
        return new JFormattedTextField(f);
    }
    
    private static JFormattedTextField field(NumberFormat format)
    {
        NumberFormatter f = new NumberFormatter(format);
        //f.setAllowsInvalid(false);
        //f.setCommitsOnValidEdit(true);
        return new JFormattedTextField(f);
    }
    
    public static JFormattedTextField decimalField(double min, double max)
    {
        return field(DECIMAL_FORMAT, min, max);
    }
    
    public static JFormattedTextField decimalField(double min)
    {
        return field(DECIMAL_FORMAT, min);
    }
    
    public static JFormattedTextField decimalField()
    {
        return field(DECIMAL_FORMAT);
    }
    
    public static JFormattedTextField integerField(int min, int max)
    {
        return field(INTEGER_FORMAT, min, max);
    }
    
    public static JFormattedTextField integerField(int min)
    {
        return field(INTEGER_FORMAT, min);
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
        Sentry.capture(e);
        
        String stackTrace = "SpeakerSim " + Project.currentVersionString()
                + "\r\n" + System.getProperty("java.runtime.name")
                + " " + System.getProperty("java.runtime.version")
                + " on " + System.getProperty("os.name")
                + "\r\n\r\nException:\r\n" + e.toString() 
                + "\r\n\r\nStack trace:\r\n" + stackTraceToString(e);

        JTextArea ta = new JTextArea(15, 50);
        ta.setText(stackTrace);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        ta.setCaretPosition(0);
        ta.setEditable(false);

        switch (JOptionPane.showOptionDialog(parent, new JScrollPane(ta), "Unexpected error", 0, JOptionPane.ERROR_MESSAGE, null, new String[]{"Close", "Copy", "Save"}, "Close"))
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
            error(parent, message);
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
            
            error(parent, message);
        }
        else if (e instanceof IOException)
        {
            error(parent, "Error reading file: " + e.getLocalizedMessage());
        }
        else
        {
            exception(parent, e);
        }
    }
    
    public static void warning(Component parent, String message)
    {
        JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    public static void error(Component parent, String message)
    {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public static int options(Component parent, String message, String[] options)
    {
        return JOptionPane.showOptionDialog(parent, message, "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
    }
    
    public static boolean ask(Component parent, String question)
    {
        int dialogButton = JOptionPane.showConfirmDialog(parent, question, "Warning", JOptionPane.YES_NO_OPTION);
        return dialogButton == JOptionPane.YES_OPTION;
    }
    
    public static String askInput(Component parent, String title, String question)
    {
        return JOptionPane.showInputDialog(parent, question, title, JOptionPane.PLAIN_MESSAGE);
    }
    
    public static String askInput(Component parent, String title, String question, String defaultValue)
    {
        return (String) JOptionPane.showInputDialog(parent, question, title, JOptionPane.PLAIN_MESSAGE, null, null, defaultValue);
    }
    
    public static boolean dialog(Component parent, String title, Object content)
    {
        String[] opts = {"Save", "Cancel"};
        return JOptionPane.showOptionDialog(parent, content, title, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]) == 0;
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
            if (tabs.getTitleAt(i).equals(tab))
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
                // ignore
            }
        }
    }
    
    public static byte[] machineId()
    {
        try
        {
            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            
            hash.update(System.getProperty("os.arch").getBytes());
            hash.update(System.getProperty("os.name").getBytes());
            
            try
            {
                Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
                while (nis.hasMoreElements())
                {
                    NetworkInterface ni = nis.nextElement();
                    try
                    {
                        byte[] mac = ni.getHardwareAddress();
                        if (mac != null)
                        {
                            hash.update(mac);
                        }
                    }
                    catch (SocketException ex)
                    {
                        // ignore
                    }
                }
            }
            catch (SocketException ex)
            {
                // ignore
            }
            
            try
            {
                hash.update(InetAddress.getLocalHost().getHostName().getBytes());
            }
            catch (UnknownHostException ex)
            {
                // ignore
            }
            
            return hash.digest();
        }
        catch (NoSuchAlgorithmException ex)
        {
            return new byte[0];
        }
    }
}
