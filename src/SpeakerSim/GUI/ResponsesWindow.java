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
   
    public ResponsesWindow(java.awt.Frame parent, Driver driver)
    {
        super(parent, true);
        initComponents();
        setLocationRelativeTo(parent);
       
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
                if (evt.getClickCount() == 2)
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
        
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                List<ResponseData> hfrd = new ArrayList<ResponseData>();
                List<ResponseData> vfrd = new ArrayList<ResponseData>();
                Iterator iter = model.iterator();

                while (iter.hasNext())
                {
                    ResponseData rd = (ResponseData) iter.next();
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
                    else
                    {
                        //TODO: throw exception
                    }
                }

                if (hfrd.size() > 0)
                {
                    Collections.sort(hfrd);
                    drv.hFRD = new ResponseData[hfrd.size()];
                    drv.hFRD = hfrd.toArray(drv.hFRD);
                }

                if (vfrd.size() > 0)
                {
                    Collections.sort(vfrd);
                    drv.vFRD = new ResponseData[vfrd.size()];
                    drv.vFRD = vfrd.toArray(drv.vFRD);
                }
            }
        });
       
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
                refresh();
            }
        });
       
        importButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                FileSelector fc = new FileSelector(".frd");
                fc.setFileFilter(new FileNameExtensionFilter("Frequency Response Data", "frd"));

                if (fc.showOpenDialog(null) == FileSelector.APPROVE_OPTION)
                {
                    try
                    {
                        ResponseData rd = new ResponseData(null);
                        rd.importData(fc.getSelectedFile(), true);
                        rd = edit(rd, false);
                        if (rd != null)
                        {
                            model.addElement(rd);
                        }
                        refresh();
                    }
                    catch (IOException ex)
                    {
                        UI.throwable(null, ex);
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
        String selectedTab = UI.getSelectedTab(tabs);
        tabs.removeAll();
       
        ResponseData frd = list.getSelectedValue();
        if (frd != null)
        {
            Graph spl = new Graph("Frequency response", "Hz", "dB");
            Graph phase = new Graph("Phase", "Hz", "");

            for (double f = Project.getInstance().Settings.StartFrequency; f <= Project.getInstance().Settings.EndFrequency; f *= Project.getInstance().Settings.multiplier())
            {
                Complex r = frd.response(f, drv.SPL_2_83V - drv.SPL_1W);
                spl.add(Fnc.toDecibels(r.abs()), f);
                phase.add(Math.toDegrees(r.phase()), f);
            }

            spl.setYRange(spl.getMaxY() - Project.getInstance().Settings.dBRange, spl.getMaxY() + 1);

            tabs.addTab("Frequency response", spl.getGraph());
            tabs.addTab("Phase", phase.getGraph());

            UI.setSelectedTab(tabs, selectedTab);
        }
    }
   
    private ResponseData edit(ResponseData frd, boolean editing)
    {
        String[] measures = { "volts", "watts" };
        JComboBox<String> inputMeasure = new JComboBox<String>(measures);
       
        JFormattedTextField input = new JFormattedTextField();
        input.setFormatterFactory(UI.FORMATTER);
       
        JFormattedTextField distance = new JFormattedTextField();
        distance.setFormatterFactory(UI.FORMATTER);
       
        String[] directions = { "horizontal", "vertical" };
        JComboBox<String> angleDirection = new JComboBox<String>(directions);
       
        JFormattedTextField angle = new JFormattedTextField();
        angle.setFormatterFactory(UI.FORMATTER);
       
        JCheckBox minimumPhase = new JCheckBox("Has minimum phase", frd.isMinimumPhase); 
       
        input.setValue(frd.input);
       
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
        
        final JComponent[] inputs = new JComponent[]
        {
            new JLabel("Input: "), input, inputMeasure,
            new JLabel("Distance (cm): "), distance,
            new JLabel("Angle (°): "), angle, angleDirection,
            minimumPhase
        };
        
        if (UI.dialog(this, "FRD info", inputs))
        {
            ResponseData rd = new ResponseData(null);
            rd.data = frd.data;
            
            rd.inputIsVoltage = inputMeasure.getSelectedIndex() == 0;
            rd.input = UI.getDouble(input);
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
            
            if (model.contains(rd) && !(editing && rd.equals(frd)))
            {
                if (!UI.ask(this, "Data with same angles already exists.\nWould you like to overwrite it?"))
                {
                    return null;
                }
            }
           
            return rd;
        }
       
        return null;
    }
    
    private void edit()
    {
        ResponseData current_rd = list.getSelectedValue();
        if (current_rd != null)
        {
            ResponseData new_rd = edit(current_rd, true);
            if (new_rd != null)
            {
                model.removeElement(current_rd);
                model.addElement(new_rd);
                refresh();
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        scrollPane = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();
        tabs = new javax.swing.JTabbedPane();
        importButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

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

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton editButton;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton importButton;
    private javax.swing.JList<ResponseData> list;
    private javax.swing.JButton removeButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTabbedPane tabs;
    // End of variables declaration//GEN-END:variables
}
