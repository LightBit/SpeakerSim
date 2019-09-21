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

import SpeakerSim.BassReflex;
import SpeakerSim.BassReflexSimulation;
import SpeakerSim.Project;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class PortCalculator extends javax.swing.JDialog
{
    private final Project project;
    protected final BassReflex box;
    private final BassReflexSimulation sim;
    private boolean result;
    
    public PortCalculator(final java.awt.Frame parent, final Project project, final BassReflex box, final BassReflexSimulation sim)
    {
        super(parent, true);
        ((JPanel)this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
        initComponents();
        setLocationRelativeTo(parent);
        
        this.project = project;
        this.box = box;
        this.sim = sim;
        result = false;

        DefaultComboBoxModel<String> shapeModel = new DefaultComboBoxModel<String>();
        for (String item : BassReflex.SHAPES)
        {
            shapeModel.addElement(item);
        }

        shapeComboBox.setModel(shapeModel);
        shapeComboBox.setSelectedIndex(box.PortShape);
        
        diameterField.setValue(box.Dv * 100);
        widthField.setValue(box.Wv * 100);
        heightField.setValue(box.Hv * 100);
        numberField.setValue(box.Np);
        thicknessField.setValue(box.Thickness * 1000);
        
        DefaultComboBoxModel<String> endsModel = new DefaultComboBoxModel<String>();
        for (String item : BassReflex.ENDS)
        {
            endsModel.addElement(item);
        }

        endsComboBox.setModel(endsModel);
        endsComboBox.setSelectedIndex(box.Ends);
        
        calc();
        
        ActionListener al = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                calc();
            }
        };
        
        shapeComboBox.addActionListener(al);
        endsComboBox.addActionListener(al);
        
        PropertyChangeListener pcl = new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e))
                {
                    calc();
                }
            }
        };
        
        diameterField.addPropertyChangeListener("value", pcl);
        widthField.addPropertyChangeListener("value", pcl);
        heightField.addPropertyChangeListener("value", pcl);
        numberField.addPropertyChangeListener("value", pcl);
        thicknessField.addPropertyChangeListener("value", pcl);
    }
    
    private void calc()
    {
        double Dv;
        double Np = UI.getInt(numberField);
        
        switch (shapeComboBox.getSelectedIndex())
        {
            case 0: // round
                Dv = UI.getDouble(diameterField) / 100;
                widthField.setEnabled(false);
                heightField.setEnabled(false);
                diameterField.setEnabled(true);
                break;
            
            default: // rectangular
                Dv = BassReflexSimulation.calcSlotDv(UI.getDouble(widthField), UI.getDouble(heightField)) / 100;
                diameterField.setEnabled(false);
                widthField.setEnabled(true);
                heightField.setEnabled(true);
                break;
        }
        
        double Lv = sim.Lv(Dv, Np, BassReflex.K[endsComboBox.getSelectedIndex()]);
        lengthField.setValue(Lv * 100);
        volumeField.setValue(BassReflexSimulation.calcPortVolume(Lv, Dv + UI.getDouble(thicknessField) / 1000 * 2, Np) * 1000);
        
        Graph airSpeed = new Graph("Air speed", "Hz", "m/s");
        
        for (double f = project.Settings.StartFrequency; f <= project.Settings.EndFrequency; f *= project.Settings.multiplier())
        {
            airSpeed.add(sim.portVelocity(f, Dv, Np), f);
        }
        
        airSpeed.addYMark(project.Environment.SpeedOfSound * 0.05, "5% speed of sound");
        airSpeed.addYMark(project.Environment.SpeedOfSound * 0.1, "10% speed of sound");
        
        graphPanel.removeAll();
        graphPanel.add(airSpeed.getGraph());
        graphPanel.revalidate();
        graphPanel.repaint();
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
        shapeComboBox = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        diameterField = new javax.swing.JFormattedTextField();
        jLabel10 = new javax.swing.JLabel();
        widthField = new javax.swing.JFormattedTextField();
        jLabel9 = new javax.swing.JLabel();
        heightField = new javax.swing.JFormattedTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        controlPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        lengthField = new javax.swing.JFormattedTextField();
        endsComboBox = new javax.swing.JComboBox<>();
        numberField = new javax.swing.JFormattedTextField();
        jLabel13 = new javax.swing.JLabel();
        graphPanel = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        thicknessField = new javax.swing.JFormattedTextField();
        volumeField = new javax.swing.JFormattedTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Port calculator");
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setMinimumSize(new java.awt.Dimension(800, 400));
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 5, 0, 5, 0};
        layout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        getContentPane().setLayout(layout);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Shape:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel7, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(shapeComboBox, gridBagConstraints);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Thickness (mm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel8, gridBagConstraints);

        diameterField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0.###"))));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(diameterField, gridBagConstraints);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Volume (l):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel10, gridBagConstraints);

        widthField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0.###"))));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(widthField, gridBagConstraints);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Number of ports:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel9, gridBagConstraints);

        heightField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0.###"))));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(heightField, gridBagConstraints);

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Ends:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel12, gridBagConstraints);

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Length (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel11, gridBagConstraints);

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
        gridBagConstraints.gridwidth = 5;
        getContentPane().add(controlPanel, gridBagConstraints);

        lengthField.setEditable(false);
        lengthField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0.###"))));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(lengthField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(endsComboBox, gridBagConstraints);

        numberField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(numberField, gridBagConstraints);

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("Height (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel13, gridBagConstraints);

        graphPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(graphPanel, gridBagConstraints);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Inner diameter (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel14, gridBagConstraints);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Width (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel15, gridBagConstraints);

        thicknessField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0.###"))));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(thicknessField, gridBagConstraints);

        volumeField.setEditable(false);
        volumeField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0.###"))));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(volumeField, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    protected void save()
    {
        box.PortShape = shapeComboBox.getSelectedIndex();
        box.Dv = UI.getDouble(diameterField) / 100;
        box.Wv = UI.getDouble(widthField) / 100;
        box.Hv = UI.getDouble(heightField) / 100;
        box.Np = UI.getInt(numberField);
        box.Ends = endsComboBox.getSelectedIndex();
        box.Thickness = UI.getDouble(thicknessField) / 1000;
    }
    
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        save();

        result = true;

        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JFormattedTextField diameterField;
    private javax.swing.JComboBox<String> endsComboBox;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JFormattedTextField heightField;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JFormattedTextField lengthField;
    private javax.swing.JFormattedTextField numberField;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox<String> shapeComboBox;
    private javax.swing.JFormattedTextField thicknessField;
    private javax.swing.JFormattedTextField volumeField;
    private javax.swing.JFormattedTextField widthField;
    // End of variables declaration//GEN-END:variables
}
