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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public final class AmplifierWindow extends javax.swing.JDialog
{
    private final Amplifier amp;
    private boolean result;
    private boolean listen = true;
    
    private final DefaultListModel<IActiveFilter> model;
    
    public AmplifierWindow(final java.awt.Frame parent, final Amplifier amp)
    {
        super(parent, true);
        ((JPanel)this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
        initComponents();
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(okButton);
        
        this.amp = amp;
        result = false;
        
        PeField.setValue(amp.Pe);
        ZoField.setValue(amp.Zo);
        
        model = new DefaultListModel<IActiveFilter>();
        
        for (IActiveFilter af: amp.Filters)
        {
            model.addElement(af);
        }
        
        list.setModel(model);
        
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                boolean enabled = list.getSelectedIndex() != -1;
                
                editButton.setEnabled(enabled);
                removeButton.setEnabled(enabled);
            }
        });
        
        list.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent evt)
            {
                if (evt.getClickCount() == 2)
                {
                    int index = list.getSelectedIndex();
                    if (index >= 0)
                    {
                        edit(model.get(index));
                    }
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
                    int index = list.getSelectedIndex();
                    if (index >= 0)
                    {
                        model.remove(index);
                    }
                }
                else if (key == KeyEvent.VK_ENTER)
                {
                    int index = list.getSelectedIndex();
                    if (index >= 0)
                    {
                        IActiveFilter item = edit(model.get(index));
                        if (item != null)
                        {
                            model.setElementAt(item, index);
                        }
                    }
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

        mainPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        PeField = UI.decimalField(1);
        ZoField = UI.decimalField(0);
        jLabel2 = new javax.swing.JLabel();
        filtersPanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        controlPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Amplifier");
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setModal(true);
        setPreferredSize(new java.awt.Dimension(600, 600));
        getContentPane().setLayout(new java.awt.BorderLayout(0, 5));

        java.awt.GridBagLayout jPanel1Layout = new java.awt.GridBagLayout();
        jPanel1Layout.columnWidths = new int[] {0, 5, 0};
        jPanel1Layout.rowHeights = new int[] {0, 5, 0};
        mainPanel.setLayout(jPanel1Layout);

        jLabel1.setText("Power (W): ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        mainPanel.add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        mainPanel.add(PeField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        mainPanel.add(ZoField, gridBagConstraints);

        jLabel2.setText("Output impedance (Ω): ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        mainPanel.add(jLabel2, gridBagConstraints);

        getContentPane().add(mainPanel, java.awt.BorderLayout.PAGE_START);

        filtersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Active filters"));
        java.awt.GridBagLayout jPanel2Layout = new java.awt.GridBagLayout();
        jPanel2Layout.columnWidths = new int[] {0, 5, 0};
        jPanel2Layout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0};
        filtersPanel.setLayout(jPanel2Layout);

        scrollPane.setViewportView(list);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        filtersPanel.add(scrollPane, gridBagConstraints);

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        filtersPanel.add(addButton, gridBagConstraints);

        removeButton.setText("Remove");
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        filtersPanel.add(removeButton, gridBagConstraints);

        editButton.setText("Edit");
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        filtersPanel.add(editButton, gridBagConstraints);

        getContentPane().add(filtersPanel, java.awt.BorderLayout.CENTER);

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

        getContentPane().add(controlPanel, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        result = true;
        
        amp.Pe = UI.getDouble(PeField);
        amp.Zo = UI.getDouble(ZoField);
        
        amp.Filters = new IActiveFilter[model.size()];
        for (int i = 0; i < amp.Filters.length; i++)
        {
            amp.Filters[i] = model.get(i);
        }

        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private IActiveFilter edit(IActiveFilter item)
    {
        if (item instanceof Equalizer)
        {
            Equalizer filter = (Equalizer) item;
            
            JFormattedTextField fField = UI.decimalField(1);
            fField.setValue(filter.getF());
            
            JFormattedTextField qField = UI.decimalField(0.000001);
            qField.setValue(filter.getQ());
            
            JFormattedTextField dbField = UI.decimalField();
            dbField.setValue(filter.getDecibels());
            
            JCheckBox linearPhaseCheckBox = new JCheckBox();
            linearPhaseCheckBox.setText("Linear phase (FIR)");
            linearPhaseCheckBox.setSelected(filter.isLinearPhase());
            
            final JComponent[] inputs = new JComponent[]
            {
                new JLabel("Frequency (Hz): "), fField,
                new JLabel("Q: "), qField,
                new JLabel("Amplitude (dB): "), dbField,
                linearPhaseCheckBox
            };
            
            if (UI.dialog(this, "Equalizer", inputs))
            {
                filter.set(UI.getDouble(fField), UI.getDouble(qField), UI.getDouble(dbField));
                filter.setLinearPhase(linearPhaseCheckBox.isSelected());
                return filter;
            }
        }
        else if (item instanceof ActivePassFilter)
        {
            ActivePassFilter filter = (ActivePassFilter) item;
            
            final JFormattedTextField frequencyField = UI.decimalField(1);
            frequencyField.setValue(filter.getFrequency());
            
            final JFormattedTextField qField = UI.decimalField(0.000001);
            qField.setValue(filter.getQ());
            
            DefaultComboBoxModel<String> typesModel = new DefaultComboBoxModel<String>();
            for (String t : ActivePassFilter.TYPES)
            {
                typesModel.addElement(t);
            }
            
            final JComboBox<String> typeComboBox = new JComboBox<String>();
            typeComboBox.setModel(typesModel);
            typeComboBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    qField.setEnabled(ActivePassFilter.isCustom(typeComboBox.getSelectedIndex()));
                }
            });
            typeComboBox.setSelectedIndex(filter.getType());
            
            JCheckBox linearPhaseCheckBox = new JCheckBox();
            linearPhaseCheckBox.setText("Linear phase (FIR)");
            linearPhaseCheckBox.setSelected(filter.isLinearPhase());
            
            final JComponent[] inputs = new JComponent[]
            {
                new JLabel("Type: "), typeComboBox,
                new JLabel("Frequency (Hz): "), frequencyField,
                new JLabel("Q: "), qField,
                linearPhaseCheckBox
            };
            
            if (UI.dialog(this, filter.name(), inputs))
            {
                filter.setType(typeComboBox.getSelectedIndex());
                filter.setFrequency(UI.getDouble(frequencyField));
                filter.setQ(UI.getDouble(qField));
                filter.setLinearPhase(linearPhaseCheckBox.isSelected());
                return filter;
            }
        }
        else if (item instanceof Gain)
        {
            Gain filter = (Gain) item;
            
            JFormattedTextField dbField = UI.decimalField();
            dbField.setValue(filter.dB);
            
            final JComponent[] inputs = new JComponent[]
            {
                new JLabel("Gain (dB): "), dbField
            };
            
            if (UI.dialog(this, "Gain", inputs))
            {
                filter.dB = UI.getDouble(dbField);
                return filter;
            }
        }
        else if (item instanceof Delay)
        {
            final Delay filter = (Delay) item;
            final double speedOfSound = Environment.getInstance().SpeedOfSound;
            
            final JFormattedTextField timeField = UI.decimalField(0);
            final JFormattedTextField distanceField = UI.decimalField(0);
            
            timeField.addPropertyChangeListener("value", new PropertyChangeListener()
            {
                @Override
                public void propertyChange(PropertyChangeEvent e)
                {
                    if (listen)
                    {
                        listen = false;
                        distanceField.setValue(Delay.calcDistance(speedOfSound, UI.getDouble(e) / 1000) * 100);
                    }
                    listen = true;
                }
            });
            
            distanceField.addPropertyChangeListener("value", new PropertyChangeListener()
            {
                @Override
                public void propertyChange(PropertyChangeEvent e)
                {
                    if (listen)
                    {
                        listen = false;
                        timeField.setValue(Delay.calcTime(speedOfSound, UI.getDouble(e) / 100) * 1000);
                    }
                    listen = true;
                }
            });
            
            timeField.setValue(filter.time * 1000);
            
            final JComponent[] inputs = new JComponent[]
            {
                new JLabel("Time (ms): "), timeField,
                new JLabel("Distance (cm): "), distanceField
            };
            
            if (UI.dialog(this, "Delay", inputs))
            {
                filter.time = UI.getDouble(timeField) / 1000;
                return filter;
            }
        }
        
        return null;
    }
    
    private JPopupMenu addMenu()
    {
        JPopupMenu popup = new JPopupMenu();
        
        JMenuItem mi = new JMenuItem("Equalizer");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Equalizer filter = new Equalizer();
                if (edit(filter) != null)
                {
                    model.addElement(filter);
                }
            }
        });
        popup.add(mi);
        
        mi = new JMenuItem("Low pass filter");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ActiveLowPassFilter filter = new ActiveLowPassFilter();
                if (edit(filter) != null)
                {
                    model.addElement(filter);
                }
            }
        });
        popup.add(mi);
        
        mi = new JMenuItem("High pass filter");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ActiveHighPassFilter filter = new ActiveHighPassFilter();
                if (edit(filter) != null)
                {
                    model.addElement(filter);
                }
            }
        });
        popup.add(mi);
        
        mi = new JMenuItem("Gain");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Gain filter = new Gain();
                if (edit(filter) != null)
                {
                    model.addElement(filter);
                }
            }
        });
        popup.add(mi);
        
        mi = new JMenuItem("Delay");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Delay filter = new Delay();
                if (edit(filter) != null)
                {
                    model.addElement(filter);
                }
            }
        });
        popup.add(mi);
        
        return popup;
    }
    
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addButtonActionPerformed
    {//GEN-HEADEREND:event_addButtonActionPerformed
        addMenu().show(addButton, 0, addButton.getBounds().height);
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeButtonActionPerformed
    {//GEN-HEADEREND:event_removeButtonActionPerformed
        model.remove(list.getSelectedIndex());
    }//GEN-LAST:event_removeButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editButtonActionPerformed
    {//GEN-HEADEREND:event_editButtonActionPerformed
        int index = list.getSelectedIndex();
        IActiveFilter item = edit(model.get(index));
        if (item != null)
        {
            model.setElementAt(item, index);
        }
    }//GEN-LAST:event_editButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField PeField;
    private javax.swing.JFormattedTextField ZoField;
    private javax.swing.JButton addButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JButton editButton;
    private javax.swing.JPanel filtersPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JList<IActiveFilter> list;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
}
