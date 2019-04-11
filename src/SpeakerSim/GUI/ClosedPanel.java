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

import SpeakerSim.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JTabbedPane;

public final class ClosedPanel extends javax.swing.JPanel implements ISpeakerPanel
{
    private final MainWindow main;
    private Speaker speaker;
    private boolean listen;
    
    public ClosedPanel(final MainWindow main)
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
                    speaker.ClosedBox.Vb = UI.getDouble(e) / 1000;
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
                    speaker.ClosedBox.Ql = UI.getDouble(e);
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
                    speaker.ClosedBox.Qa = UI.getDouble(e);
                    main.refresh();
                }
            }
        });
    }
    
    Graph box;
    
    @Override
    public void show(Speaker speaker)
    {
        this.speaker = speaker;
        
        listen = false;
        
        vbField.setValue(speaker.ClosedBox.Vb * 1000);
        qlField.setValue(speaker.ClosedBox.Ql);
        qaField.setValue(speaker.ClosedBox.Qa);
        qtcField.setValue(ClosedBoxSimulation.calcQtc(speaker.ClosedBox.Vb, speaker.Driver.Vas, speaker.Driver.Qts));
        
        listen = true;
    }
    
    @Override
    public void addGraphs(final JTabbedPane tabs)
    {
        ClosedBoxSimulation sim = (ClosedBoxSimulation) speaker.getSimulation();
        
        box = new Graph("Enclosure response", "Hz", "dB");
        
        for (double f = main.project.Settings.StartFrequency; f <= main.project.Settings.EndFrequency; f *= main.project.Settings.multiplier())
        {
            box.add(sim.dBmag(f), f);
        }
        
        box.setYRange(box.getMaxY() - main.project.Settings.dBRange, box.getMaxY() + 1);
        
        tabs.addTab("Enclosure response", box.getGraph());
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        qtcField = new javax.swing.JFormattedTextField();
        vbField = new javax.swing.JFormattedTextField();
        jLabel4 = new javax.swing.JLabel();
        qlField = new javax.swing.JFormattedTextField();
        qaField = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Closed Box"));
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 5, 0};
        layout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0};
        setLayout(layout);

        jLabel1.setText("Qtc:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel1, gridBagConstraints);

        qtcField.setEditable(false);
        qtcField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        qtcField.setToolTipText("Total system Q");
        qtcField.setMaximumSize(new java.awt.Dimension(80, 19));
        qtcField.setMinimumSize(new java.awt.Dimension(80, 19));
        qtcField.setName(""); // NOI18N
        qtcField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(qtcField, gridBagConstraints);

        vbField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        vbField.setToolTipText("Internal enclousure volume");
        vbField.setMaximumSize(new java.awt.Dimension(80, 19));
        vbField.setMinimumSize(new java.awt.Dimension(80, 19));
        vbField.setName(""); // NOI18N
        vbField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(vbField, gridBagConstraints);

        jLabel4.setText("Volume (l):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel4, gridBagConstraints);

        qlField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        qlField.setToolTipText("Enclosure leakage losses [5 - 100]");
        qlField.setMaximumSize(new java.awt.Dimension(80, 19));
        qlField.setMinimumSize(new java.awt.Dimension(80, 19));
        qlField.setName(""); // NOI18N
        qlField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(qlField, gridBagConstraints);

        qaField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        qaField.setToolTipText("Enclosure absorption losses (damping) [3 - 100]");
        qaField.setMaximumSize(new java.awt.Dimension(80, 19));
        qaField.setMinimumSize(new java.awt.Dimension(80, 19));
        qaField.setName(""); // NOI18N
        qaField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(qaField, gridBagConstraints);

        jLabel2.setText("Ql:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel2, gridBagConstraints);

        jLabel3.setText("Qa:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel3, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    public javax.swing.JFormattedTextField qaField;
    public javax.swing.JFormattedTextField qlField;
    public javax.swing.JFormattedTextField qtcField;
    private javax.swing.JFormattedTextField vbField;
    // End of variables declaration//GEN-END:variables
}