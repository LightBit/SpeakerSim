/*
 * Copyright (C) 2018 Gregor Pintar <grpintar@gmail.com>
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

import SpeakerSim.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JTabbedPane;

public final class AperiodicPanel extends javax.swing.JPanel implements ISpeakerPanel
{
    private final MainWindow main;
    private Speaker speaker;
    private boolean listen;
    
    public AperiodicPanel(final MainWindow main)
    {
        listen = false;
        this.main = main;
        
        initComponents();
        
        vbField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen && UI.validate(e))
                {
                    speaker.Aperiodic.Vb = UI.getDouble(e) / 1000;
                    main.refresh();
                }
            }
        });
        
        qlField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen && UI.validate(e, 5, 100))
                {
                    speaker.Aperiodic.Ql = UI.getDouble(e);
                    main.refresh();
                }
            }
        });
        
        qaField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen && UI.validate(e, 3, 100))
                {
                    speaker.Aperiodic.Qa = UI.getDouble(e);
                    main.refresh();
                }
            }
        });
        
        portPositionXField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen && UI.validate(e, 0, main.project.Environment.RoomX * 100))
                {
                    speaker.Aperiodic.VentPosition.X = UI.getDouble(e) / 100;
                    portPositionDistanceField.setValue(main.project.ListeningPosition.distance(speaker.Aperiodic.VentPosition) * 100);
                    main.refresh();
                }
            }
        });
        
        portPositionYField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen && UI.validate(e, 0, main.project.Environment.RoomY * 100))
                {
                    speaker.Aperiodic.VentPosition.Y = UI.getDouble(e) / 100;
                    portPositionDistanceField.setValue(main.project.ListeningPosition.distance(speaker.Aperiodic.VentPosition) * 100);
                    main.refresh();
                }
            }
        });
        
        portPositionZField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen && UI.validate(e, 0, main.project.Environment.RoomZ * 100))
                {
                    speaker.Aperiodic.VentPosition.Z = UI.getDouble(e) / 100;
                    portPositionDistanceField.setValue(main.project.ListeningPosition.distance(speaker.Aperiodic.VentPosition) * 100);
                    main.refresh();
                }
            }
        });
    }

    @Override
    public int getBaseline(int width, int height)
    {
        return bassReflexPanel.getBaseline(width, height);
    }
    
    Graph box;
    Graph cone;
    Graph vent;
    
    @Override
    public void show(Speaker speaker)
    {
        this.speaker = speaker;
        
        listen = false;
        
        vbField.setValue(speaker.Aperiodic.Vb * 1000);
        qlField.setValue(speaker.Aperiodic.Ql);
        qaField.setValue(speaker.Aperiodic.Qa);
        portPositionXField.setValue(speaker.Aperiodic.VentPosition.X * 100);
        portPositionYField.setValue(speaker.Aperiodic.VentPosition.Y * 100);
        portPositionZField.setValue(speaker.Aperiodic.VentPosition.Z * 100);
        portPositionDistanceField.setValue(main.project.ListeningPosition.distance(speaker.Aperiodic.VentPosition) * 100);
        
        listen = true;
    }
    
    @Override
    public void addGraphs(final JTabbedPane tabs)
    {
        BassReflexSimulation sim = (BassReflexSimulation) speaker.getSimulation();
        
        box = new Graph("Enclosure response", "Hz", "dB");
        cone = new Graph("Cone response", "Hz", "dB");
        vent = new Graph("Vent response", "Hz", "dB");
        
        for (double f = main.project.Settings.StartFrequency; f <= main.project.Settings.EndFrequency; f *= main.project.Settings.multiplier())
        {
            box.add(sim.dBmag(f), f);
            cone.add(sim.dBmagCone(f), f);
            vent.add(sim.dBmagPort(f), f);
        }
        
        box.setYRange(box.getMaxY() - main.project.Settings.dBRange, box.getMaxY() + 1);
        cone.setYRange(cone.getMaxY() - main.project.Settings.dBRange, cone.getMaxY() + 1);
        vent.setYRange(vent.getMaxY() - main.project.Settings.dBRange, vent.getMaxY() + 1);
        
        tabs.addTab("Enclosure response", box.getGraph());
        tabs.addTab("Cone response", cone.getGraph());
        tabs.addTab("Vent response", vent.getGraph());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        portPositionPanel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        portPositionXField = new javax.swing.JFormattedTextField();
        portPositionYField = new javax.swing.JFormattedTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        portPositionZField = new javax.swing.JFormattedTextField();
        portPositionDistanceField = new javax.swing.JFormattedTextField();
        jLabel16 = new javax.swing.JLabel();
        bassReflexPanel = new javax.swing.JPanel();
        vbField = new javax.swing.JFormattedTextField();
        jLabel4 = new javax.swing.JLabel();
        portCalcButton = new javax.swing.JButton();
        qlField = new javax.swing.JFormattedTextField();
        qaField = new javax.swing.JFormattedTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 5, 0);
        flowLayout1.setAlignOnBaseline(true);
        setLayout(flowLayout1);

        portPositionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Vent position in room"));
        java.awt.GridBagLayout portPositionPanelLayout = new java.awt.GridBagLayout();
        portPositionPanelLayout.columnWidths = new int[] {0, 5, 0};
        portPositionPanelLayout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0};
        portPositionPanel.setLayout(portPositionPanelLayout);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("X (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        portPositionPanel.add(jLabel9, gridBagConstraints);

        portPositionXField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        portPositionXField.setMinimumSize(new java.awt.Dimension(80, 19));
        portPositionXField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        portPositionPanel.add(portPositionXField, gridBagConstraints);

        portPositionYField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        portPositionYField.setMinimumSize(new java.awt.Dimension(80, 19));
        portPositionYField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        portPositionPanel.add(portPositionYField, gridBagConstraints);

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Y (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        portPositionPanel.add(jLabel12, gridBagConstraints);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Z (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        portPositionPanel.add(jLabel15, gridBagConstraints);

        portPositionZField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        portPositionZField.setMinimumSize(new java.awt.Dimension(80, 19));
        portPositionZField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        portPositionPanel.add(portPositionZField, gridBagConstraints);

        portPositionDistanceField.setEditable(false);
        portPositionDistanceField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        portPositionDistanceField.setToolTipText("Distance to listening position");
        portPositionDistanceField.setMinimumSize(new java.awt.Dimension(80, 19));
        portPositionDistanceField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        portPositionPanel.add(portPositionDistanceField, gridBagConstraints);

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("Distance (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        portPositionPanel.add(jLabel16, gridBagConstraints);

        add(portPositionPanel);

        bassReflexPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Aperiodic"));
        java.awt.GridBagLayout bassReflexPanelLayout = new java.awt.GridBagLayout();
        bassReflexPanelLayout.columnWidths = new int[] {0, 5, 0};
        bassReflexPanelLayout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0};
        bassReflexPanel.setLayout(bassReflexPanelLayout);

        vbField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        vbField.setToolTipText("Internal enclousure volume");
        vbField.setMinimumSize(new java.awt.Dimension(80, 19));
        vbField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        bassReflexPanel.add(vbField, gridBagConstraints);

        jLabel4.setText("Volume (l):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        bassReflexPanel.add(jLabel4, gridBagConstraints);

        portCalcButton.setText("Vent calculator");
        portCalcButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                portCalcButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        bassReflexPanel.add(portCalcButton, gridBagConstraints);

        qlField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        qlField.setToolTipText("Enclosure leakage losses [5 - 100]");
        qlField.setMinimumSize(new java.awt.Dimension(80, 19));
        qlField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        bassReflexPanel.add(qlField, gridBagConstraints);

        qaField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        qaField.setToolTipText("Enclosure absorption losses (damping) [3 - 100]");
        qaField.setMinimumSize(new java.awt.Dimension(80, 19));
        qaField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        bassReflexPanel.add(qaField, gridBagConstraints);

        jLabel1.setText("Ql:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        bassReflexPanel.add(jLabel1, gridBagConstraints);

        jLabel3.setText("Qa:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        bassReflexPanel.add(jLabel3, gridBagConstraints);

        add(bassReflexPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void portCalcButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_portCalcButtonActionPerformed
    {//GEN-HEADEREND:event_portCalcButtonActionPerformed
        if (new VentCalculator(null, main.project, speaker.Aperiodic, (AperiodicSimulation) speaker.getSimulation(), speaker.Driver).showDialog())
        {
            main.project.setModified();
        }
    }//GEN-LAST:event_portCalcButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bassReflexPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JButton portCalcButton;
    private javax.swing.JFormattedTextField portPositionDistanceField;
    private javax.swing.JPanel portPositionPanel;
    private javax.swing.JFormattedTextField portPositionXField;
    private javax.swing.JFormattedTextField portPositionYField;
    private javax.swing.JFormattedTextField portPositionZField;
    private javax.swing.JFormattedTextField qaField;
    private javax.swing.JFormattedTextField qlField;
    private javax.swing.JFormattedTextField vbField;
    // End of variables declaration//GEN-END:variables
}