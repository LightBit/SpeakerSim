/*
 * Copyright (C) 2018 Gregor Pintar
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

import SpeakerSim.*;
import java.awt.Component;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

public class ResponsesWindow extends JDialog
{
    private final Driver drv;
    private final SortedListModel<ResponseData> model;
    private final Graph spl;
    private final Graph phase;
   
    public ResponsesWindow(java.awt.Frame parent, Driver driver)
    {
        super(parent, true);
        initComponents();
        setLocationRelativeTo(parent);
        
        spl = new Graph("Hz", "dB");
        phase = new Graph("Hz", "");
        
        spl.setXRange(Settings.getInstance().StartFrequency, Settings.getInstance().EndFrequency);
        phase.setXRange(Settings.getInstance().StartFrequency, Settings.getInstance().EndFrequency);
        
        spl.setYRange(Settings.getInstance().MinSPL, Settings.getInstance().MaxSPL);
        phase.setYRange(-180, 180);
        
        tabs.addTab("Frequency response", spl.getPanel());
        tabs.addTab("Phase", phase.getPanel());
       
        drv = driver;
       
        model = new SortedListModel<ResponseData>();
        if (driver.FRD != null)
        {
            model.addElement(driver.FRD);
        }
        if (driver.hFRD != null)
        {
            model.addAll(driver.hFRD);
        }
        if (driver.vFRD != null)
        {
            model.addAll(driver.vFRD);
        }
        
        list.setModel(model);
        
        if (model.getSize() > 0)
        {
            list.setSelectedIndex(0);
        }
       
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                refresh();
            }
        });
       
        list.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent evt)
            {
                if (evt.getClickCount() == 2 && list.getSelectedIndices().length == 1)
                {
                    edit();
                }
            }
        });
       
        list.addKeyListener(new KeyListener()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                int key = e.getKeyCode();
               
                if (key == KeyEvent.VK_DELETE)
                {
                    ResponseData frd = list.getSelectedValue();
                    if (frd != null)
                    {
                        model.removeElement(frd);
                        list.clearSelection();
                        refresh();
                    }
                }
                else if (key == KeyEvent.VK_ENTER)
                {
                    edit();
                }
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
               
            }
           
            @Override
            public void keyTyped(KeyEvent e)
            {
               
            }
        });
        
        okButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                List<ResponseData> hfrd = new ArrayList<ResponseData>();
                List<ResponseData> vfrd = new ArrayList<ResponseData>();
                Iterator<ResponseData> iter = model.iterator();
                
                drv.FRD = null;

                while (iter.hasNext())
                {
                    ResponseData rd = iter.next();
                    if (rd.horizontalAngle == 0 && rd.verticalAngle == 0)
                    {
                        drv.FRD = rd;
                        hfrd.add(rd);
                        vfrd.add(rd);
                    }
                    else if (rd.verticalAngle == 0)
                    {
                        hfrd.add(rd);
                    }
                    else if (rd.horizontalAngle == 0)
                    {
                        vfrd.add(rd);
                    }
                }

                Collections.sort(hfrd);
                drv.hFRD = new ResponseData[hfrd.size()];
                drv.hFRD = hfrd.toArray(drv.hFRD);

                Collections.sort(vfrd);
                drv.vFRD = new ResponseData[vfrd.size()];
                drv.vFRD = vfrd.toArray(drv.vFRD);
                
                dispose();
            }
        });
        
        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                dispose();
            }
        });
        
        /*this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                
            }
        });*/
       
        editButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                edit();
            }
        });
       
        removeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                List<ResponseData> selected = list.getSelectedValuesList();
                for (ResponseData e : selected)
                {
                    model.removeElement(e);
                }
                list.clearSelection();
                refresh();
            }
        });
       
        importButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                ResponseData rd = importFRD();
                if (rd != null)
                {
                    rd = edit(rd, model.getSize() > 0, false);
                    if (rd != null)
                    {
                        model.addElement(rd);
                        list.setSelectedValue(rd, true);
                        refresh();
                    }
                }
            }
        });
       
        exportButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                FileSelector fc = new FileSelector(".frd");
                fc.setFileFilter(new FileNameExtensionFilter("Frequency Response Data", "frd"));

                if (fc.showSaveDialog(null) == FileSelector.APPROVE_OPTION)
                {
                    try
                    {
                        ResponseData frd = list.getSelectedValue();
                        frd.exportData(fc.getSelectedFile());
                    }
                    catch (FileNotFoundException | UnsupportedEncodingException ex)
                    {
                        UI.throwable(null, ex);
                    }
                }
            }
        });
       
        refresh();
    }
   
    private void refresh()
    {
        int selected = list.getSelectedIndices().length;
        editButton.setEnabled(selected == 1);
        removeButton.setEnabled(selected > 0);
        exportButton.setEnabled(selected == 1);
       
        renderGraphs();
    }
   
    private void renderGraphs()
    {
        spl.clear();
        phase.clear();
        
        ResponseData frd = list.getSelectedValue();
        if (frd != null)
        {
            spl.addSeries("Frequency response");
            phase.addSeries("Phase");

            for (double f = Settings.getInstance().StartFrequency; f <= Settings.getInstance().EndFrequency; f *= Settings.getInstance().multiplier())
            {
                Complex r = frd.response(f, drv.SPL_2_83V - drv.SPL_1W);
                spl.add(0, f, Fnc.toDecibels(r.abs()));
                phase.add(0, f, Math.toDegrees(r.phase()));
            }
        }
        
        phase.addYMark(0, "");
    }
    
    public static ResponseData importFRD()
    {
        FileSelector fc = new FileSelector(".frd");
        fc.setFileFilter(new FileNameExtensionFilter("Frequency Response Data", "frd", "txt", "csv"));

        if (fc.showOpenDialog(null) == FileSelector.APPROVE_OPTION)
        {
            try
            {
                ResponseData rd = new ResponseData(fc.getSelectedFile(), true);
                return rd;
            }
            catch (IOException ex)
            {
                UI.throwable(null, ex);
            }
        }
        
        return null;
    }
   
    public static ResponseData editDialog(Component parent, ResponseData frd, boolean showAngles)
    {
        String[] measures = { "volts", "watts" };
        JComboBox<String> inputMeasure = new JComboBox<String>(measures);
       
        JFormattedTextField input = UI.decimalField(0.000001);
        JFormattedTextField distance = UI.decimalField(0.000001);
       
        String[] directions = { "horizontal", "vertical" };
        JComboBox<String> angleDirection = new JComboBox<String>(directions);
       
        JFormattedTextField angle = UI.decimalField(-180, 180);
       
        JCheckBox minimumPhase = new JCheckBox("Has minimum phase", frd.isMinimumPhase); 
       
        input.setValue(frd.inputValue);
       
        if (frd.inputIsVoltage)
        {
            inputMeasure.setSelectedIndex(0);
        }
        else
        {
            inputMeasure.setSelectedIndex(1);
        }
       
        distance.setValue(frd.distance * 100);
       
        if (frd.verticalAngle != 0)
        {
            angleDirection.setSelectedIndex(1);
            angle.setValue(frd.verticalAngle);
        }
        else
        {
            angleDirection.setSelectedIndex(0);
            angle.setValue(frd.horizontalAngle);
        }
        
        final JComponent[] inputs = showAngles ? new JComponent[]
        {
            new JLabel("Input: "), input, inputMeasure, new JSeparator(),
            new JLabel("Distance (cm): "), distance, new JSeparator(),
            minimumPhase, new JSeparator(),
            new JLabel("Angle (°): "), angle, angleDirection
        } : new JComponent[]
        {
            new JLabel("Input: "), input, inputMeasure, new JSeparator(),
            new JLabel("Distance (cm): "), distance, new JSeparator(),
            minimumPhase
        };
        
        if (UI.dialog(parent, "FRD info", inputs))
        {
            ResponseData rd = new ResponseData(null);
            rd.data = frd.data;
            
            rd.inputIsVoltage = inputMeasure.getSelectedIndex() == 0;
            rd.inputValue = UI.getDouble(input);
            rd.distance = UI.getDouble(distance) / 100;
            
            if (angleDirection.getSelectedIndex() == 0)
            {
                rd.horizontalAngle = UI.getDouble(angle);
                rd.verticalAngle = 0;
            }
            else
            {
                rd.horizontalAngle = 0;
                rd.verticalAngle = UI.getDouble(angle);
            }
            
            rd.isMinimumPhase = minimumPhase.isSelected();
           
            return rd;
        }
       
        return null;
    }
    
    private ResponseData edit(ResponseData frd, boolean showAngles, boolean editing)
    {
        ResponseData rd = editDialog(this, frd, showAngles);
        if (rd != null)
        {
            if (model.contains(rd) && !(editing && rd.equals(frd)))
            {
                if (UI.options(this, "Data with same angles already exists.", new String[]{"Overwrite", "Cancel"}) != 0)
                {
                    return null;
                }
            }
        }
        
        return rd;
    }
    
    private void edit()
    {
        ResponseData current_rd = list.getSelectedValue();
        if (current_rd != null)
        {
            ResponseData new_rd = edit(current_rd, model.getSize() > 1, true);
            if (new_rd != null)
            {
                model.removeElement(current_rd);
                model.addElement(new_rd);
                refresh();
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        scrollPane = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();
        tabs = new javax.swing.JTabbedPane();
        importButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        controlPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Frequency response data");
        setMinimumSize(new java.awt.Dimension(800, 600));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        scrollPane.setMaximumSize(new java.awt.Dimension(200, 32767));
        scrollPane.setMinimumSize(new java.awt.Dimension(200, 100));
        scrollPane.setPreferredSize(new java.awt.Dimension(200, 100));
        scrollPane.setViewportView(list);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.1;
        getContentPane().add(scrollPane, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        getContentPane().add(tabs, gridBagConstraints);

        importButton.setText("Import");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(importButton, gridBagConstraints);

        editButton.setText("Edit");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(editButton, gridBagConstraints);

        exportButton.setText("Export");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(exportButton, gridBagConstraints);

        removeButton.setText("Remove");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(removeButton, gridBagConstraints);

        okButton.setText("Save");
        controlPanel.add(okButton);

        cancelButton.setText("Cancel");
        controlPanel.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        getContentPane().add(controlPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JButton editButton;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton importButton;
    private javax.swing.JList<ResponseData> list;
    private javax.swing.JButton okButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTabbedPane tabs;
    // End of variables declaration//GEN-END:variables
}
