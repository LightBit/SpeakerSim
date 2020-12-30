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

import SpeakerSim.Position;
import SpeakerSim.Project;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class DriverPositionPanel extends javax.swing.JPanel
{
    private Position position;
    private boolean listen;
    
    public DriverPositionPanel(final MainWindow main)
    {
        listen = false;
        
        initComponents();
        
        xField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen)
                {
                    position.X = UI.getDouble(e) / 100;
                    distanceField.setValue(Project.getInstance().ListeningPosition.distance(position) * 100);
                    relativeVerticalAngleField.setValue(Math.round(position.verticalAngle(Project.getInstance().ListeningPosition)));
                    relativeHorizontalAngleField.setValue(Math.round(position.horizontalAngle(Project.getInstance().ListeningPosition)));
                    main.refresh();
                }
            }
        });
        
        yField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen)
                {
                    position.Y = UI.getDouble(e) / 100;
                    distanceField.setValue(Project.getInstance().ListeningPosition.distance(position) * 100);
                    relativeVerticalAngleField.setValue(Math.round(position.verticalAngle(Project.getInstance().ListeningPosition)));
                    relativeHorizontalAngleField.setValue(Math.round(position.horizontalAngle(Project.getInstance().ListeningPosition)));
                    main.refresh();
                }
            }
        });
        
        zField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen)
                {
                    position.Z = UI.getDouble(e) / 100;
                    distanceField.setValue(Project.getInstance().ListeningPosition.distance(position) * 100);
                    relativeVerticalAngleField.setValue(Math.round(position.verticalAngle(Project.getInstance().ListeningPosition)));
                    relativeHorizontalAngleField.setValue(Math.round(position.horizontalAngle(Project.getInstance().ListeningPosition)));
                    main.refresh();
                }
            }
        });
        
        verticalAngleField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen)
                {
                    position.VerticalAngle = UI.getDouble(e);
                    relativeVerticalAngleField.setValue(Math.round(position.verticalAngle(Project.getInstance().ListeningPosition)));
                    relativeHorizontalAngleField.setValue(Math.round(position.horizontalAngle(Project.getInstance().ListeningPosition)));
                    main.refresh();
                }
            }
        });
        
        horizontalAngleField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (listen)
                {
                    position.HorizontalAngle = UI.getDouble(e);
                    relativeVerticalAngleField.setValue(Math.round(position.verticalAngle(Project.getInstance().ListeningPosition)));
                    relativeHorizontalAngleField.setValue(Math.round(position.horizontalAngle(Project.getInstance().ListeningPosition)));
                    main.refresh();
                }
            }
        });
    }
    
    public void show(Position position)
    {
        listen = false;
        
        this.position = position;
        
        xField.setValue(position.X * 100);
        yField.setValue(position.Y * 100);
        zField.setValue(position.Z * 100);
        verticalAngleField.setValue(position.VerticalAngle);
        horizontalAngleField.setValue(position.HorizontalAngle);
        relativeVerticalAngleField.setValue(Math.round(position.verticalAngle(Project.getInstance().ListeningPosition)));
        relativeHorizontalAngleField.setValue(Math.round(position.horizontalAngle(Project.getInstance().ListeningPosition)));
        distanceField.setValue(Project.getInstance().ListeningPosition.distance(position) * 100);
        
        listen = true;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel9 = new javax.swing.JLabel();
        xField = UI.decimalField(0);
        jLabel12 = new javax.swing.JLabel();
        yField = UI.decimalField(0);
        jLabel10 = new javax.swing.JLabel();
        zField = UI.decimalField(0);
        verticalAngleField = UI.decimalField(-180, 180);
        jLabel14 = new javax.swing.JLabel();
        horizontalAngleField = UI.decimalField(-180, 180);
        jLabel15 = new javax.swing.JLabel();
        relativeHorizontalAngleField = UI.decimalField(-180, 180);
        relativeVerticalAngleField = UI.decimalField(-180, 180);
        jLabel16 = new javax.swing.JLabel();
        distanceField = UI.decimalField(0);

        setBorder(javax.swing.BorderFactory.createTitledBorder("Driver position"));
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 5, 0, 5, 0};
        layout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        setLayout(layout);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("X (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel9, gridBagConstraints);

        xField.setMinimumSize(new java.awt.Dimension(85, 19));
        xField.setPreferredSize(new java.awt.Dimension(85, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        add(xField, gridBagConstraints);

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Y (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel12, gridBagConstraints);

        yField.setMinimumSize(new java.awt.Dimension(85, 19));
        yField.setPreferredSize(new java.awt.Dimension(85, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        add(yField, gridBagConstraints);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Horizontal angle (°):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel10, gridBagConstraints);

        zField.setMinimumSize(new java.awt.Dimension(85, 19));
        zField.setPreferredSize(new java.awt.Dimension(85, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        add(zField, gridBagConstraints);

        verticalAngleField.setToolTipText("Relative to room walls");
        verticalAngleField.setMinimumSize(new java.awt.Dimension(40, 19));
        verticalAngleField.setPreferredSize(new java.awt.Dimension(40, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        add(verticalAngleField, gridBagConstraints);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Vertical angle (°):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel14, gridBagConstraints);

        horizontalAngleField.setToolTipText("Relative to room walls");
        horizontalAngleField.setMinimumSize(new java.awt.Dimension(40, 19));
        horizontalAngleField.setPreferredSize(new java.awt.Dimension(40, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        add(horizontalAngleField, gridBagConstraints);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Z (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel15, gridBagConstraints);

        relativeHorizontalAngleField.setEditable(false);
        relativeHorizontalAngleField.setToolTipText("Relative to listening position");
        relativeHorizontalAngleField.setMinimumSize(new java.awt.Dimension(40, 19));
        relativeHorizontalAngleField.setPreferredSize(new java.awt.Dimension(40, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        add(relativeHorizontalAngleField, gridBagConstraints);

        relativeVerticalAngleField.setEditable(false);
        relativeVerticalAngleField.setToolTipText("Relative to listening position");
        relativeVerticalAngleField.setMinimumSize(new java.awt.Dimension(40, 19));
        relativeVerticalAngleField.setPreferredSize(new java.awt.Dimension(40, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        add(relativeVerticalAngleField, gridBagConstraints);

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("Distance (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel16, gridBagConstraints);

        distanceField.setEditable(false);
        distanceField.setToolTipText("Distance to listening position");
        distanceField.setMinimumSize(new java.awt.Dimension(85, 19));
        distanceField.setPreferredSize(new java.awt.Dimension(85, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        add(distanceField, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField distanceField;
    private javax.swing.JFormattedTextField horizontalAngleField;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JFormattedTextField relativeHorizontalAngleField;
    private javax.swing.JFormattedTextField relativeVerticalAngleField;
    private javax.swing.JFormattedTextField verticalAngleField;
    private javax.swing.JFormattedTextField xField;
    private javax.swing.JFormattedTextField yField;
    private javax.swing.JFormattedTextField zField;
    // End of variables declaration//GEN-END:variables
}
