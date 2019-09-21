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
import SpeakerSim.PowerFilter;
import SpeakerSim.Settings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class SettingsWindow extends javax.swing.JDialog
{
    private final Settings settings;
    private boolean result;
    
    public SettingsWindow(final java.awt.Frame parent, final Settings settings)
    {
        super(parent, true);
        ((JPanel)this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
        initComponents();
        setLocationRelativeTo(parent);
        
        this.settings = settings;
        result = false;
        
        if (settings.Smoothing == 0)
        {
            smoothingComboBox.setSelectedItem("None");
        }
        else
        {
            smoothingComboBox.setSelectedItem("1/" + settings.Smoothing);
        }

        startFrequencyField.setValue(settings.StartFrequency);
        endFrequencyField.setValue(settings.EndFrequency);
        pointsField.setValue(settings.Points);
        dbRangeField.setValue(settings.dBRange);
        maxImpedanceField.setValue(settings.MaxImpedance);
        maxPowerField.setValue(settings.MaxPower);
        simulateRoomCheckBox.setSelected(this.settings.RoomSimulation);
        simulateBaffleCheckBox.setSelected(this.settings.BaffleSimulation);
        powerFilterComboBox.setSelectedItem(this.settings.PowerFilter.toString());
        
        startFrequencyField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                UI.validate(e, 1, UI.getInt(endFrequencyField) / 2);
            }
        });
        
        endFrequencyField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                UI.validate(e, UI.getInt(startFrequencyField) * 2);
            }
        });
        
        pointsField.addPropertyChangeListener("value", UI.validator(2));
        dbRangeField.addPropertyChangeListener("value", UI.validator(3));
        maxImpedanceField.addPropertyChangeListener("value", UI.validator(3));
        maxPowerField.addPropertyChangeListener("value", UI.validator(3));
    }
    
    public boolean showDialog()
    {
        setVisible(true);
        return result;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel7 = new javax.swing.JLabel();
        endFrequencyField = new javax.swing.JFormattedTextField();
        controlPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        pointsField = new javax.swing.JFormattedTextField();
        jLabel9 = new javax.swing.JLabel();
        startFrequencyField = new javax.swing.JFormattedTextField();
        jLabel10 = new javax.swing.JLabel();
        dbRangeField = new javax.swing.JFormattedTextField();
        jLabel11 = new javax.swing.JLabel();
        simulateRoomCheckBox = new javax.swing.JCheckBox();
        simulateBaffleCheckBox = new javax.swing.JCheckBox();
        maxImpedanceField = new javax.swing.JFormattedTextField();
        jLabel12 = new javax.swing.JLabel();
        maxPowerField = new javax.swing.JFormattedTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        smoothingComboBox = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        powerFilterComboBox = new javax.swing.JComboBox(PowerFilter.FILTERS);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Simulation settings");
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setModal(true);
        setResizable(false);
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 5, 0};
        layout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        getContentPane().setLayout(layout);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Start frequency (Hz):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel7, gridBagConstraints);

        endFrequencyField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0.###"))));
        endFrequencyField.setMinimumSize(new java.awt.Dimension(80, 19));
        endFrequencyField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(endFrequencyField, gridBagConstraints);

        okButton.setText("Save");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        controlPanel.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        controlPanel.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(controlPanel, gridBagConstraints);

        pointsField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0.###"))));
        pointsField.setMinimumSize(new java.awt.Dimension(80, 19));
        pointsField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(pointsField, gridBagConstraints);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("dB range (dB):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel9, gridBagConstraints);

        startFrequencyField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0.###"))));
        startFrequencyField.setMinimumSize(new java.awt.Dimension(80, 19));
        startFrequencyField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(startFrequencyField, gridBagConstraints);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("End frequency (Hz):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel10, gridBagConstraints);

        dbRangeField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0.###"))));
        dbRangeField.setMinimumSize(new java.awt.Dimension(80, 19));
        dbRangeField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(dbRangeField, gridBagConstraints);

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Points:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel11, gridBagConstraints);

        simulateRoomCheckBox.setText("Simulate room");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(simulateRoomCheckBox, gridBagConstraints);

        simulateBaffleCheckBox.setText("Simulate baffle");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(simulateBaffleCheckBox, gridBagConstraints);

        maxImpedanceField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0.###"))));
        maxImpedanceField.setMinimumSize(new java.awt.Dimension(80, 19));
        maxImpedanceField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(maxImpedanceField, gridBagConstraints);

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Maximal impedance (Ω):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel12, gridBagConstraints);

        maxPowerField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0.###"))));
        maxPowerField.setMinimumSize(new java.awt.Dimension(80, 19));
        maxPowerField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(maxPowerField, gridBagConstraints);

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("Power filter:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel13, gridBagConstraints);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Smoothing:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel14, gridBagConstraints);

        smoothingComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "1/48", "1/24", "1/12", "1/6", "1/3", "1/2", "1/1" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(smoothingComboBox, gridBagConstraints);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Maximal power (W):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel15, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(powerFilterComboBox, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        try
        {
            result = true;
            
            String smoothing = (String)smoothingComboBox.getSelectedItem();
            settings.Smoothing = "None".equals(smoothing) ? 0 : Fnc.parseNumber(smoothing.split("/")[1]).intValue();
            
            settings.StartFrequency = UI.getInt(startFrequencyField);
            settings.EndFrequency = UI.getInt(endFrequencyField);
            settings.Points = UI.getInt(pointsField);
            settings.dBRange = UI.getInt(dbRangeField);
            settings.MaxImpedance = UI.getInt(maxImpedanceField);
            settings.MaxPower = UI.getInt(maxPowerField);
            settings.RoomSimulation = simulateRoomCheckBox.isSelected();
            settings.BaffleSimulation = simulateBaffleCheckBox.isSelected();
            settings.PowerFilter.setType(PowerFilter.valueOf(powerFilterComboBox.getSelectedItem().toString()));
            
            dispose();
        }
        catch (ParseException ex)
        {
            UI.exception(this, ex);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JFormattedTextField dbRangeField;
    private javax.swing.JFormattedTextField endFrequencyField;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JFormattedTextField maxImpedanceField;
    private javax.swing.JFormattedTextField maxPowerField;
    private javax.swing.JButton okButton;
    private javax.swing.JFormattedTextField pointsField;
    private javax.swing.JComboBox<String> powerFilterComboBox;
    private javax.swing.JCheckBox simulateBaffleCheckBox;
    private javax.swing.JCheckBox simulateRoomCheckBox;
    private javax.swing.JComboBox<String> smoothingComboBox;
    private javax.swing.JFormattedTextField startFrequencyField;
    // End of variables declaration//GEN-END:variables
}
