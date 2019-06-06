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

import SpeakerSim.Baffle;
import SpeakerSim.Project;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class BafflePanel extends javax.swing.JPanel
{
    private Baffle baffle;
    private boolean listen;
    
    public BafflePanel(final MainWindow main)
    {
        listen = false;
        
        initComponents();
        
        enabledCheckBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if (listen)
                {
                    baffle.Enabled = enabledCheckBox.isSelected();
                    main.refresh();
                }
                
                /*widthField.setEnabled(baffle.Enabled);
                heightField.setEnabled(baffle.Enabled);
                leftField.setEnabled(baffle.Enabled);
                bottomField.setEnabled(baffle.Enabled);
                edgeField.setEnabled(baffle.Enabled);*/
            }
        });
        
        widthField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen && UI.validate(e, 0))
                {
                    baffle.Width = UI.getDouble(e) / 100;
                    main.refresh();
                }
            }
        });
        
        heightField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen && UI.validate(e, 0))
                {
                    baffle.Height = UI.getDouble(e) / 100;
                    main.refresh();
                }
            }
        });
        
        leftField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen && UI.validate(e, 0, baffle.Width * 100))
                {
                    baffle.X = UI.getDouble(e) / 100;
                    main.refresh();
                }
            }
        });
        
        bottomField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen && UI.validate(e, 0, baffle.Height * 100))
                {
                    baffle.Y = UI.getDouble(e) / 100;
                    main.refresh();
                }
            }
        });
        
        edgeField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen && UI.validate(e, 0))
                {
                    baffle.EdgeRadius = UI.getDouble(e) / 1000;
                    main.refresh();
                }
            }
        });
        
        UI.setPanelEnabled(this, Project.getInstance().Settings.BaffleSimulation);
    }
    
    public void show(Baffle baffle)
    {
        listen = false;
        
        this.baffle = baffle;
        
        enabledCheckBox.setSelected(baffle.Enabled);
        widthField.setValue(baffle.Width * 100);
        heightField.setValue(baffle.Height * 100);
        leftField.setValue(baffle.X * 100);
        bottomField.setValue(baffle.Y * 100);
        edgeField.setValue(baffle.EdgeRadius * 1000);
        
        listen = true;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        widthField = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        heightField = new javax.swing.JFormattedTextField();
        jLabel5 = new javax.swing.JLabel();
        leftField = new javax.swing.JFormattedTextField();
        jLabel6 = new javax.swing.JLabel();
        bottomField = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        edgeField = new javax.swing.JFormattedTextField();
        enabledCheckBox = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Baffle"));
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 5, 0};
        layout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        setLayout(layout);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Baffle width (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel1, gridBagConstraints);

        widthField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        widthField.setMinimumSize(new java.awt.Dimension(80, 19));
        widthField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        add(widthField, gridBagConstraints);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Baffle height (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel2, gridBagConstraints);

        heightField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        heightField.setMinimumSize(new java.awt.Dimension(80, 19));
        heightField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        add(heightField, gridBagConstraints);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Driver position from left (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel5, gridBagConstraints);

        leftField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        leftField.setMinimumSize(new java.awt.Dimension(80, 19));
        leftField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        add(leftField, gridBagConstraints);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Driver position from bottom (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel6, gridBagConstraints);

        bottomField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        bottomField.setMinimumSize(new java.awt.Dimension(80, 19));
        bottomField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        add(bottomField, gridBagConstraints);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Edge radius (mm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel3, gridBagConstraints);

        edgeField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        edgeField.setMinimumSize(new java.awt.Dimension(80, 19));
        edgeField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        add(edgeField, gridBagConstraints);

        enabledCheckBox.setText("Enable baffle simulation");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        add(enabledCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField bottomField;
    private javax.swing.JFormattedTextField edgeField;
    private javax.swing.JCheckBox enabledCheckBox;
    private javax.swing.JFormattedTextField heightField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JFormattedTextField leftField;
    private javax.swing.JFormattedTextField widthField;
    // End of variables declaration//GEN-END:variables
}
