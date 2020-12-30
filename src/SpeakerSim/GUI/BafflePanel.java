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
                
                setEnabled();
            }
        });
        
        widthField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen)
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
                if (listen)
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
                if (listen)
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
                if (listen)
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
                if (listen)
                {
                    baffle.EdgeRadius = UI.getDouble(e) / 1000;
                    main.refresh();
                }
            }
        });
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
        
        setEnabled();
        
        listen = true;
    }
    
    public void setEnabled()
    {
        widthLabel.setEnabled(baffle.Enabled);
        widthField.setEnabled(baffle.Enabled);
        heightLabel.setEnabled(baffle.Enabled);
        heightField.setEnabled(baffle.Enabled);
        leftLabel.setEnabled(baffle.Enabled);
        leftField.setEnabled(baffle.Enabled);
        bottomLabel.setEnabled(baffle.Enabled);
        bottomField.setEnabled(baffle.Enabled);
        edgeLabel.setEnabled(baffle.Enabled);
        edgeField.setEnabled(baffle.Enabled);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        widthLabel = new javax.swing.JLabel();
        widthField = UI.decimalField(0.01);
        heightLabel = new javax.swing.JLabel();
        heightField = UI.decimalField(0.01);
        leftLabel = new javax.swing.JLabel();
        leftField = UI.decimalField(0);
        bottomLabel = new javax.swing.JLabel();
        bottomField = UI.decimalField(0);
        edgeLabel = new javax.swing.JLabel();
        edgeField = UI.decimalField(0);
        enabledCheckBox = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Baffle"));
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 5, 0};
        layout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        setLayout(layout);

        widthLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        widthLabel.setText("Baffle width (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(widthLabel, gridBagConstraints);

        widthField.setMinimumSize(new java.awt.Dimension(80, 19));
        widthField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        add(widthField, gridBagConstraints);

        heightLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        heightLabel.setText("Baffle height (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(heightLabel, gridBagConstraints);

        heightField.setMinimumSize(new java.awt.Dimension(80, 19));
        heightField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        add(heightField, gridBagConstraints);

        leftLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        leftLabel.setText("Driver position from left (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(leftLabel, gridBagConstraints);

        leftField.setMinimumSize(new java.awt.Dimension(80, 19));
        leftField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        add(leftField, gridBagConstraints);

        bottomLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        bottomLabel.setText("Driver position from bottom (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(bottomLabel, gridBagConstraints);

        bottomField.setMinimumSize(new java.awt.Dimension(80, 19));
        bottomField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        add(bottomField, gridBagConstraints);

        edgeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        edgeLabel.setText("Edge radius (mm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(edgeLabel, gridBagConstraints);

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
    private javax.swing.JLabel bottomLabel;
    private javax.swing.JFormattedTextField edgeField;
    private javax.swing.JLabel edgeLabel;
    private javax.swing.JCheckBox enabledCheckBox;
    private javax.swing.JFormattedTextField heightField;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JFormattedTextField leftField;
    private javax.swing.JLabel leftLabel;
    private javax.swing.JFormattedTextField widthField;
    private javax.swing.JLabel widthLabel;
    // End of variables declaration//GEN-END:variables
}
