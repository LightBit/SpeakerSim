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

import SpeakerSim.Complex;
import SpeakerSim.Driver;
import SpeakerSim.Fnc;
import SpeakerSim.PowerFilter;
import SpeakerSim.Project;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.text.ParseException;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DriverWindow extends javax.swing.JDialog
{
    private final Project project;
    private final Driver origDriver;
    private final Driver drv;
    private boolean result;
    
    private void load(Driver driver)
    {
        NameField.setText(driver.Name);
        VasField.setValue(driver.Vas * 1000);
        FsField.setValue(driver.Fs);
        QesField.setValue(driver.Qes);
        QmsField.setValue(driver.Qms);
        QtsField.setValue(driver.Qts);
        ReField.setValue(driver.Re);
        BLField.setValue(driver.Bl);
        LeField.setValue(driver.Le * 1000);
        XmaxField.setValue(driver.Xmax * 1000);
        shapeComboBox.setSelectedIndex(driver.shape.ordinal());
        DiaField.setValue(driver.Dia * 1000);
        WidthField.setValue(driver.Width * 1000);
        HeightField.setValue(driver.Height * 1000);
        SdField.setValue(driver.Sd * 10000);
        VdField.setValue(driver.Vd * 1000000);
        CmsField.setValue(driver.Cms * 1000);
        MmsField.setValue(driver.Mms * 1000);
        RmsField.setValue(driver.Rms);
        n0Field.setValue(driver.n0 * 100);
        SPL_1WField.setValue(driver.SPL_1W);
        SPL_2_83VField.setValue(driver.SPL_2_83V);
        PeField.setValue(driver.Pe);
        PeFField.setValue(driver.PeF);
        powerFilterComboBox.setSelectedItem(driver.PowerFilter.toString());
        CrossStartField.setValue(driver.CrossStart);
        CrossEndField.setValue(driver.CrossEnd);
        closedCheckBox.setSelected(driver.Closed);
        invertedCheckBox.setSelected(driver.Inverted);
        
        DiaField.setEnabled(drv.shape == Driver.Shape.Circular);
        WidthField.setEnabled(drv.shape == Driver.Shape.Rectangular);
        HeightField.setEnabled(drv.shape == Driver.Shape.Rectangular);
        
        PeFField.setEnabled(powerFilterComboBox.getSelectedIndex() != 0);
        
        if (driver.hasFRD())
        {
            CrossStartField.setEnabled(true);
            CrossEndField.setEnabled(true);
        }
        else
        {
            CrossStartField.setEnabled(driver.hasZMA());
            CrossEndField.setEnabled(driver.hasZMA());
        }
        
        if (driver.hasZMA())
        {
            ZMAButton.setText("Remove");
            ZMAExportButton.setEnabled(true);
            CrossStartField.setEnabled(true);
            CrossEndField.setEnabled(true);
        }
        else
        {
            ZMAButton.setText("Import");
            ZMAExportButton.setEnabled(false);
            CrossStartField.setEnabled(driver.hasFRD());
            CrossEndField.setEnabled(driver.hasFRD());
        }
        
        refresh();
    }
    
    public DriverWindow(java.awt.Frame parent, final Project project, final Driver driver)
    {
        super(parent, true);
        ((JPanel)this.getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
        initComponents();
        setLocationRelativeTo(parent);
        
        this.project = project;
        origDriver = driver;
        drv = driver.copy();
        result = false;
        
        load(drv);
        
        NameField.getDocument().addDocumentListener(new DocumentListener()
        {
            private void setValue()
            {
                drv.Name = NameField.getText();
            }
            
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                setValue();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e)
            {
                setValue();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                setValue();
            }
        });
        
        VasField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Vas = UI.getDouble(VasField) / 1000;
                    refresh();
                }
            }
        });
        
        FsField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Fs = UI.getDouble(FsField);
                    refresh();
                }
            }
        });
        
        QesField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Qes = UI.getDouble(QesField);
                    refresh();
                }
            }
        });
        
        QmsField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Qms = UI.getDouble(QmsField);
                    refresh();
                }
            }
        });
        
        QtsField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Qts = UI.getDouble(QtsField);
                    refresh();
                }
            }
        });
        
        ReField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e))
                {
                    drv.Re = UI.getDouble(ReField);
                    refresh();
                }
            }
        });
        
        BLField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Bl = UI.getDouble(BLField);
                    refresh();
                }
            }
        });
        
        LeField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Le = UI.getDouble(LeField) / 1000;
                    refresh();
                }
            }
        });
        
        XmaxField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Xmax = UI.getDouble(XmaxField) / 1000;
                    refresh();
                }
            }
        });
        
        shapeComboBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if (e.getStateChange() == 1)
                {
                    drv.shape = Driver.Shape.valueOf(e.getItem().toString());
                    DiaField.setEnabled(drv.shape == Driver.Shape.Circular);
                    WidthField.setEnabled(drv.shape == Driver.Shape.Rectangular);
                    HeightField.setEnabled(drv.shape == Driver.Shape.Rectangular);
                    refresh();
                }
            }
        });
        
        DiaField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Dia = UI.getDouble(DiaField) / 1000;
                    refresh();
                }
            }
        });
        
        WidthField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Width = UI.getDouble(WidthField) / 1000;
                    refresh();
                }
            }
        });
        
        HeightField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Height = UI.getDouble(HeightField) / 1000;
                    refresh();
                }
            }
        });
        
        SdField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Sd = UI.getDouble(SdField) / 10000;
                    refresh();
                }
            }
        });
        
        VdField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Vd = UI.getDouble(VdField) / 1000000;
                    refresh();
                }
            }
        });
        
        CmsField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Cms = UI.getDouble(CmsField) / 1000;
                    refresh();
                }
            }
        });
        
        MmsField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Mms = UI.getDouble(MmsField) / 1000;
                    refresh();
                }
            }
        });
        
        RmsField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.Rms = UI.getDouble(RmsField);
                    refresh();
                }
            }
        });
        
        n0Field.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.n0 = UI.getDouble(n0Field) / 100;
                    refresh();
                }
            }
        });
        
        SPL_1WField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e))
                {
                    drv.SPL_1W = UI.getDouble(SPL_1WField);
                    refresh();
                }
            }
        });
        
        SPL_2_83VField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e))
                {
                    drv.SPL_2_83V = UI.getDouble(SPL_2_83VField);
                }
            }
        });
        
        PeField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e))
                {
                    drv.Pe = UI.getDouble(PeField);
                }
            }
        });
        
        powerFilterComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PeFField.setEnabled(powerFilterComboBox.getSelectedIndex() != 0);
            }
        });
        
        PeFField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0))
                {
                    drv.PeF = UI.getDouble(PeFField);
                }
            }
        });
        
        CrossStartField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 1, drv.CrossEnd))
                {
                    drv.CrossStart = UI.getDouble(CrossStartField);
                    refresh();
                }
            }
        });
        
        CrossEndField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, drv.CrossStart))
                {
                    drv.CrossEnd = UI.getDouble(CrossEndField);
                    refresh();
                }
            }
        });
        
        closedCheckBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                drv.Closed = closedCheckBox.isSelected();
            }
        });
        
        invertedCheckBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                drv.Inverted = invertedCheckBox.isSelected();
            }
        });
    }
    
    public boolean showDialog()
    {
        setVisible(true);
        return result;
    }
    
    private void renderGraphs()
    {
        String selectedTab = UI.getSelectedTab(tabs);
        boolean cross = !closedCheckBox.isSelected();
        tabs.removeAll();
        
        Graph spl = new Graph("Frequency response", "Hz", "dB");
        Graph phase = new Graph("Phase", "Hz", "");
        Graph impedance = new Graph("Impedance", "Hz", "Ω");
        Graph impedancePhase = new Graph("Impedance phase", "Hz", "");
        
        for (double f = project.Settings.StartFrequency; f <= project.Settings.EndFrequency; f *= project.Settings.multiplier())
        {
            Complex r = cross ? drv.normResponse(f) : drv.response(f);
            spl.add(Fnc.toDecibels(r.abs()), f);
            phase.add(Math.toDegrees(r.phase()), f);
            
            Complex z = drv.impedance(f);
            impedance.add(z.abs(), f);
            impedancePhase.add(Math.toDegrees(z.phase()), f);
        }
        
        spl.setYRange(spl.getMaxY() - project.Settings.dBRange, spl.getMaxY() + 1);
        
        if (cross)
        {
            spl.addXMark(UI.getDouble(CrossStartField), "Cross start");
            spl.addXMark(UI.getDouble(CrossEndField), "Cross end");

            phase.addXMark(UI.getDouble(CrossStartField), "Cross start");
            phase.addXMark(UI.getDouble(CrossEndField), "Cross end");
        }
        
        tabs.addTab("Frequency response", spl.getGraph());
        tabs.addTab("Phase", phase.getGraph());
        tabs.addTab("Impedance", impedance.getGraph());
        tabs.addTab("Impedance phase", impedancePhase.getGraph());
        
        UI.setSelectedTab(tabs, selectedTab);
    }
    
    private void refresh()
    {
        setButton(VasButton, VasField.getText(), drv.calcVas(project.Environment.AirDensity, project.Environment.SpeedOfSound) * 1000);
        setButton(FsButton, FsField.getText(), drv.calcFs());
        setButton(QesButton, QesField.getText(), drv.calcQes());
        setButton(QmsButton, QmsField.getText(), drv.calcQms());
        setButton(QtsButton, QtsField.getText(), drv.calcQts());
        setButton(BLButton, BLField.getText(), drv.calcBl());
        setButton(SdButton, SdField.getText(), drv.calcSd() * 10000);
        setButton(DiaButton, DiaField.getText(), drv.calcDia() * 1000);
        setButton(VdButton, VdField.getText(), drv.calcVd() * 1000000);
        setButton(CmsButton, CmsField.getText(), drv.calcCms(project.Environment.AirDensity, project.Environment.SpeedOfSound) * 1000);
        setButton(MmsButton, MmsField.getText(), drv.calcMms() * 1000);
        setButton(RmsButton, RmsField.getText(), drv.calcRms());
        setButton(n0Button, n0Field.getText(), drv.calcN0(project.Environment.AirDensity, project.Environment.SpeedOfSound) * 100);
        setButton(SPL_1WButton, SPL_1WField.getText(), drv.calcSPL_1W());
        setButton(SPL_2_83VButton, SPL_2_83VField.getText(), drv.calcSPL_2_83V());

        renderGraphs();
    }
    
    private static void setButton(JButton button, String value, double calcValue)
    {
        if (!Double.isNaN(calcValue) && !Double.isInfinite(calcValue) && calcValue > 0)
        {
            String text = Fnc.roundedDecimalFormat(calcValue);
            if (!text.equals(value))
            {
                button.setText(text);
                button.setOpaque(true);
                button.setContentAreaFilled(true);
                button.setBorderPainted(true);
                button.setEnabled(true);
                return;
            }
        }
        
        UI.hideButton(button);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        tabs = new javax.swing.JTabbedPane();
        controlPanel = new javax.swing.JPanel();
        OKButton = new javax.swing.JButton();
        CancelButton = new javax.swing.JButton();
        ImportButton = new javax.swing.JButton();
        ExportButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        propertiesPanel = new javax.swing.JPanel();
        lblName = new javax.swing.JLabel();
        lblCrossStart = new javax.swing.JLabel();
        ReField = new javax.swing.JFormattedTextField();
        FsButton = new javax.swing.JButton();
        FsField = new javax.swing.JFormattedTextField();
        lblVas = new javax.swing.JLabel();
        QmsButton = new javax.swing.JButton();
        lblQes = new javax.swing.JLabel();
        ReButton = new javax.swing.JButton();
        QmsField = new javax.swing.JFormattedTextField();
        lblQms = new javax.swing.JLabel();
        LeField = new javax.swing.JFormattedTextField();
        LeButton = new javax.swing.JButton();
        lblQts = new javax.swing.JLabel();
        DiaField = new javax.swing.JFormattedTextField();
        DiaButton = new javax.swing.JButton();
        lblSPL_2_83V = new javax.swing.JLabel();
        lblBL = new javax.swing.JLabel();
        lblLe = new javax.swing.JLabel();
        lblXmax = new javax.swing.JLabel();
        lblDia = new javax.swing.JLabel();
        lblSd = new javax.swing.JLabel();
        VdField = new javax.swing.JFormattedTextField();
        MmsField = new javax.swing.JFormattedTextField();
        n0Field = new javax.swing.JFormattedTextField();
        PeField = new javax.swing.JFormattedTextField();
        VasField = new javax.swing.JFormattedTextField();
        QesField = new javax.swing.JFormattedTextField();
        QtsField = new javax.swing.JFormattedTextField();
        BLField = new javax.swing.JFormattedTextField();
        XmaxField = new javax.swing.JFormattedTextField();
        SdField = new javax.swing.JFormattedTextField();
        CmsField = new javax.swing.JFormattedTextField();
        RmsField = new javax.swing.JFormattedTextField();
        SPL_1WField = new javax.swing.JFormattedTextField();
        VasButton = new javax.swing.JButton();
        VdButton = new javax.swing.JButton();
        MmsButton = new javax.swing.JButton();
        n0Button = new javax.swing.JButton();
        QesButton = new javax.swing.JButton();
        QtsButton = new javax.swing.JButton();
        BLButton = new javax.swing.JButton();
        XmaxButton = new javax.swing.JButton();
        SdButton = new javax.swing.JButton();
        CmsButton = new javax.swing.JButton();
        RmsButton = new javax.swing.JButton();
        SPL_1WButton = new javax.swing.JButton();
        lblVd = new javax.swing.JLabel();
        lblCms = new javax.swing.JLabel();
        lblMms = new javax.swing.JLabel();
        lblRms = new javax.swing.JLabel();
        lblN0 = new javax.swing.JLabel();
        lblSPL_1W = new javax.swing.JLabel();
        lblPowerFilter = new javax.swing.JLabel();
        FRDButton = new javax.swing.JButton();
        lblZMA = new javax.swing.JLabel();
        SPL_2_83VField = new javax.swing.JFormattedTextField();
        lblFRD = new javax.swing.JLabel();
        ZMAButton = new javax.swing.JButton();
        lblRe = new javax.swing.JLabel();
        invertedCheckBox = new javax.swing.JCheckBox();
        closedCheckBox = new javax.swing.JCheckBox();
        NameField = new javax.swing.JTextField();
        CrossStartField = new javax.swing.JFormattedTextField();
        lblCrossEnd = new javax.swing.JLabel();
        CrossEndField = new javax.swing.JFormattedTextField();
        lblFs = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        ZMAExportButton = new javax.swing.JButton();
        PeFField = new javax.swing.JFormattedTextField();
        lblPeF = new javax.swing.JLabel();
        lblPe = new javax.swing.JLabel();
        powerFilterComboBox = new javax.swing.JComboBox(PowerFilter.FILTERS);
        shapeComboBox = new javax.swing.JComboBox(Driver.Shape.values());
        lblShape = new javax.swing.JLabel();
        HeightField = new javax.swing.JFormattedTextField();
        lblHeight = new javax.swing.JLabel();
        WidthField = new javax.swing.JFormattedTextField();
        lblWidth = new javax.swing.JLabel();
        SPL_2_83VButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Driver");
        setMinimumSize(new java.awt.Dimension(800, 600));
        setPreferredSize(new java.awt.Dimension(1000, 800));
        getContentPane().setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.1;
        getContentPane().add(tabs, gridBagConstraints);

        OKButton.setText("OK");
        OKButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                OKButtonActionPerformed(evt);
            }
        });
        controlPanel.add(OKButton);

        CancelButton.setText("Cancel");
        CancelButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                CancelButtonActionPerformed(evt);
            }
        });
        controlPanel.add(CancelButton);

        ImportButton.setText("Import");
        ImportButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                ImportButtonActionPerformed(evt);
            }
        });
        controlPanel.add(ImportButton);

        ExportButton.setText("Export");
        ExportButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                ExportButtonActionPerformed(evt);
            }
        });
        controlPanel.add(ExportButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        getContentPane().add(controlPanel, gridBagConstraints);

        scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMinimumSize(new java.awt.Dimension(380, 400));
        scrollPane.setPreferredSize(new java.awt.Dimension(380, 700));

        propertiesPanel.setMinimumSize(new java.awt.Dimension(300, 780));
        propertiesPanel.setPreferredSize(new java.awt.Dimension(300, 700));
        java.awt.GridBagLayout propertiesPanelLayout = new java.awt.GridBagLayout();
        propertiesPanelLayout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0};
        propertiesPanelLayout.rowHeights = new int[] {0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0};
        propertiesPanel.setLayout(propertiesPanelLayout);

        lblName.setText("Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblName, gridBagConstraints);

        lblCrossStart.setText("Cross start (Hz):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 58;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblCrossStart, gridBagConstraints);

        ReField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        ReField.setToolTipText("DC resistance of the voice coil");
        ReField.setMinimumSize(new java.awt.Dimension(80, 19));
        ReField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        propertiesPanel.add(ReField, gridBagConstraints);

        FsButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        FsButton.setBorderPainted(false);
        FsButton.setContentAreaFilled(false);
        FsButton.setEnabled(false);
        FsButton.setFocusable(false);
        FsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        FsButton.setMaximumSize(new java.awt.Dimension(120, 19));
        FsButton.setMinimumSize(new java.awt.Dimension(120, 19));
        FsButton.setPreferredSize(new java.awt.Dimension(120, 19));
        FsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                FsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(FsButton, gridBagConstraints);

        FsField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        FsField.setToolTipText("Free air resonance frequency");
        FsField.setMinimumSize(new java.awt.Dimension(80, 19));
        FsField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        propertiesPanel.add(FsField, gridBagConstraints);

        lblVas.setText("Vas (l):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblVas, gridBagConstraints);

        QmsButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        QmsButton.setBorderPainted(false);
        QmsButton.setContentAreaFilled(false);
        QmsButton.setEnabled(false);
        QmsButton.setFocusable(false);
        QmsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        QmsButton.setMaximumSize(new java.awt.Dimension(120, 19));
        QmsButton.setMinimumSize(new java.awt.Dimension(120, 19));
        QmsButton.setPreferredSize(new java.awt.Dimension(120, 19));
        QmsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                QmsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(QmsButton, gridBagConstraints);

        lblQes.setText("Qes:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblQes, gridBagConstraints);

        ReButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        ReButton.setBorderPainted(false);
        ReButton.setContentAreaFilled(false);
        ReButton.setEnabled(false);
        ReButton.setFocusable(false);
        ReButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ReButton.setMaximumSize(new java.awt.Dimension(120, 19));
        ReButton.setMinimumSize(new java.awt.Dimension(120, 19));
        ReButton.setPreferredSize(new java.awt.Dimension(120, 19));
        ReButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                ReButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(ReButton, gridBagConstraints);

        QmsField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        QmsField.setToolTipText("Mechanical Q at resonance");
        QmsField.setMinimumSize(new java.awt.Dimension(80, 19));
        QmsField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        propertiesPanel.add(QmsField, gridBagConstraints);

        lblQms.setText("Qms:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblQms, gridBagConstraints);

        LeField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        LeField.setToolTipText("Voice coil inductance at 1kHz");
        LeField.setMinimumSize(new java.awt.Dimension(80, 19));
        LeField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 20;
        propertiesPanel.add(LeField, gridBagConstraints);

        LeButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        LeButton.setBorderPainted(false);
        LeButton.setContentAreaFilled(false);
        LeButton.setEnabled(false);
        LeButton.setFocusable(false);
        LeButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LeButton.setMaximumSize(new java.awt.Dimension(120, 19));
        LeButton.setMinimumSize(new java.awt.Dimension(120, 19));
        LeButton.setPreferredSize(new java.awt.Dimension(120, 19));
        LeButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                LeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(LeButton, gridBagConstraints);

        lblQts.setText("Qts:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblQts, gridBagConstraints);

        DiaField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        DiaField.setToolTipText("Effective diameter (membrane + 1/3 surround)");
        DiaField.setMinimumSize(new java.awt.Dimension(80, 19));
        DiaField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 26;
        propertiesPanel.add(DiaField, gridBagConstraints);

        DiaButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        DiaButton.setBorderPainted(false);
        DiaButton.setContentAreaFilled(false);
        DiaButton.setEnabled(false);
        DiaButton.setFocusable(false);
        DiaButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DiaButton.setMaximumSize(new java.awt.Dimension(120, 19));
        DiaButton.setMinimumSize(new java.awt.Dimension(120, 19));
        DiaButton.setPreferredSize(new java.awt.Dimension(120, 19));
        DiaButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                DiaButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(DiaButton, gridBagConstraints);

        lblSPL_2_83V.setText("SPL at 2.83V/1m (dB):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 46;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblSPL_2_83V, gridBagConstraints);

        lblBL.setText("BL (Tm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblBL, gridBagConstraints);

        lblLe.setText("Le (mH):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblLe, gridBagConstraints);

        lblXmax.setText("Xmax (mm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblXmax, gridBagConstraints);

        lblDia.setText("Dia (mm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblDia, gridBagConstraints);

        lblSd.setText("Sd (cm²):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 32;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblSd, gridBagConstraints);

        VdField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        VdField.setToolTipText("Peak displacement volume");
        VdField.setMinimumSize(new java.awt.Dimension(80, 19));
        VdField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 34;
        propertiesPanel.add(VdField, gridBagConstraints);

        MmsField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        MmsField.setToolTipText("Mass of the diaphragm and coil, including acoustic load");
        MmsField.setMinimumSize(new java.awt.Dimension(80, 19));
        MmsField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 38;
        propertiesPanel.add(MmsField, gridBagConstraints);

        n0Field.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        n0Field.setToolTipText("Reference efficiency");
        n0Field.setMinimumSize(new java.awt.Dimension(80, 19));
        n0Field.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 42;
        propertiesPanel.add(n0Field, gridBagConstraints);

        PeField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        PeField.setToolTipText("Nominal input power");
        PeField.setMinimumSize(new java.awt.Dimension(80, 19));
        PeField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 48;
        propertiesPanel.add(PeField, gridBagConstraints);

        VasField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        VasField.setToolTipText("Volume of air equal to compliance");
        VasField.setMinimumSize(new java.awt.Dimension(80, 19));
        VasField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        propertiesPanel.add(VasField, gridBagConstraints);

        QesField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        QesField.setToolTipText("Electrical Q at resonance");
        QesField.setMinimumSize(new java.awt.Dimension(80, 19));
        QesField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        propertiesPanel.add(QesField, gridBagConstraints);

        QtsField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        QtsField.setToolTipText("Total Q at resonance");
        QtsField.setMinimumSize(new java.awt.Dimension(80, 19));
        QtsField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        propertiesPanel.add(QtsField, gridBagConstraints);

        BLField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        BLField.setToolTipText("Motor strength");
        BLField.setMinimumSize(new java.awt.Dimension(80, 19));
        BLField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        propertiesPanel.add(BLField, gridBagConstraints);

        XmaxField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        XmaxField.setToolTipText("Peak linear displacement of cone");
        XmaxField.setMinimumSize(new java.awt.Dimension(80, 19));
        XmaxField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 22;
        propertiesPanel.add(XmaxField, gridBagConstraints);

        SdField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        SdField.setToolTipText("Effective driver radiating area");
        SdField.setMinimumSize(new java.awt.Dimension(80, 19));
        SdField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 32;
        propertiesPanel.add(SdField, gridBagConstraints);

        CmsField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        CmsField.setToolTipText("Compliance of suspension");
        CmsField.setMinimumSize(new java.awt.Dimension(80, 19));
        CmsField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 36;
        propertiesPanel.add(CmsField, gridBagConstraints);

        RmsField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        RmsField.setToolTipText("Mechanical resistance of suspension");
        RmsField.setMinimumSize(new java.awt.Dimension(80, 19));
        RmsField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 40;
        propertiesPanel.add(RmsField, gridBagConstraints);

        SPL_1WField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        SPL_1WField.setToolTipText("Reference sound pressure level at 1W/1m");
        SPL_1WField.setMinimumSize(new java.awt.Dimension(80, 19));
        SPL_1WField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 44;
        propertiesPanel.add(SPL_1WField, gridBagConstraints);

        VasButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        VasButton.setBorderPainted(false);
        VasButton.setContentAreaFilled(false);
        VasButton.setEnabled(false);
        VasButton.setFocusable(false);
        VasButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        VasButton.setMaximumSize(new java.awt.Dimension(120, 19));
        VasButton.setMinimumSize(new java.awt.Dimension(120, 19));
        VasButton.setPreferredSize(new java.awt.Dimension(120, 19));
        VasButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                VasButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(VasButton, gridBagConstraints);

        VdButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        VdButton.setBorderPainted(false);
        VdButton.setContentAreaFilled(false);
        VdButton.setEnabled(false);
        VdButton.setFocusable(false);
        VdButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        VdButton.setMaximumSize(new java.awt.Dimension(120, 19));
        VdButton.setMinimumSize(new java.awt.Dimension(120, 19));
        VdButton.setPreferredSize(new java.awt.Dimension(120, 19));
        VdButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                VdButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 34;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(VdButton, gridBagConstraints);

        MmsButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        MmsButton.setBorderPainted(false);
        MmsButton.setContentAreaFilled(false);
        MmsButton.setEnabled(false);
        MmsButton.setFocusable(false);
        MmsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MmsButton.setMaximumSize(new java.awt.Dimension(120, 19));
        MmsButton.setMinimumSize(new java.awt.Dimension(120, 19));
        MmsButton.setPreferredSize(new java.awt.Dimension(120, 19));
        MmsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                MmsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 38;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(MmsButton, gridBagConstraints);

        n0Button.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        n0Button.setBorderPainted(false);
        n0Button.setContentAreaFilled(false);
        n0Button.setEnabled(false);
        n0Button.setFocusable(false);
        n0Button.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        n0Button.setMaximumSize(new java.awt.Dimension(120, 19));
        n0Button.setMinimumSize(new java.awt.Dimension(120, 19));
        n0Button.setPreferredSize(new java.awt.Dimension(120, 19));
        n0Button.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                n0ButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 42;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(n0Button, gridBagConstraints);

        QesButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        QesButton.setBorderPainted(false);
        QesButton.setContentAreaFilled(false);
        QesButton.setEnabled(false);
        QesButton.setFocusable(false);
        QesButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        QesButton.setMaximumSize(new java.awt.Dimension(120, 19));
        QesButton.setMinimumSize(new java.awt.Dimension(120, 19));
        QesButton.setPreferredSize(new java.awt.Dimension(120, 19));
        QesButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                QesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(QesButton, gridBagConstraints);

        QtsButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        QtsButton.setBorderPainted(false);
        QtsButton.setContentAreaFilled(false);
        QtsButton.setEnabled(false);
        QtsButton.setFocusable(false);
        QtsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        QtsButton.setMaximumSize(new java.awt.Dimension(120, 19));
        QtsButton.setMinimumSize(new java.awt.Dimension(120, 19));
        QtsButton.setPreferredSize(new java.awt.Dimension(120, 19));
        QtsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                QtsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(QtsButton, gridBagConstraints);

        BLButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        BLButton.setBorderPainted(false);
        BLButton.setContentAreaFilled(false);
        BLButton.setEnabled(false);
        BLButton.setFocusable(false);
        BLButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BLButton.setMaximumSize(new java.awt.Dimension(120, 19));
        BLButton.setMinimumSize(new java.awt.Dimension(120, 19));
        BLButton.setPreferredSize(new java.awt.Dimension(120, 19));
        BLButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                BLButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(BLButton, gridBagConstraints);

        XmaxButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        XmaxButton.setBorderPainted(false);
        XmaxButton.setContentAreaFilled(false);
        XmaxButton.setEnabled(false);
        XmaxButton.setFocusable(false);
        XmaxButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        XmaxButton.setMaximumSize(new java.awt.Dimension(120, 19));
        XmaxButton.setMinimumSize(new java.awt.Dimension(120, 19));
        XmaxButton.setPreferredSize(new java.awt.Dimension(120, 19));
        XmaxButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                XmaxButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(XmaxButton, gridBagConstraints);

        SdButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        SdButton.setBorderPainted(false);
        SdButton.setContentAreaFilled(false);
        SdButton.setEnabled(false);
        SdButton.setFocusable(false);
        SdButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        SdButton.setMaximumSize(new java.awt.Dimension(120, 19));
        SdButton.setMinimumSize(new java.awt.Dimension(120, 19));
        SdButton.setPreferredSize(new java.awt.Dimension(120, 19));
        SdButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                SdButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 32;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(SdButton, gridBagConstraints);

        CmsButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        CmsButton.setBorderPainted(false);
        CmsButton.setContentAreaFilled(false);
        CmsButton.setEnabled(false);
        CmsButton.setFocusable(false);
        CmsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        CmsButton.setMaximumSize(new java.awt.Dimension(120, 19));
        CmsButton.setMinimumSize(new java.awt.Dimension(120, 19));
        CmsButton.setPreferredSize(new java.awt.Dimension(120, 19));
        CmsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                CmsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 36;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(CmsButton, gridBagConstraints);

        RmsButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        RmsButton.setBorderPainted(false);
        RmsButton.setContentAreaFilled(false);
        RmsButton.setEnabled(false);
        RmsButton.setFocusable(false);
        RmsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        RmsButton.setMaximumSize(new java.awt.Dimension(120, 19));
        RmsButton.setMinimumSize(new java.awt.Dimension(120, 19));
        RmsButton.setPreferredSize(new java.awt.Dimension(120, 19));
        RmsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                RmsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 40;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(RmsButton, gridBagConstraints);

        SPL_1WButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        SPL_1WButton.setBorderPainted(false);
        SPL_1WButton.setContentAreaFilled(false);
        SPL_1WButton.setEnabled(false);
        SPL_1WButton.setFocusable(false);
        SPL_1WButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        SPL_1WButton.setMaximumSize(new java.awt.Dimension(120, 19));
        SPL_1WButton.setMinimumSize(new java.awt.Dimension(120, 19));
        SPL_1WButton.setPreferredSize(new java.awt.Dimension(120, 19));
        SPL_1WButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                SPL_1WButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 44;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(SPL_1WButton, gridBagConstraints);

        lblVd.setText("Vd (cm³):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 34;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblVd, gridBagConstraints);

        lblCms.setText("Cms (cm/N):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 36;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblCms, gridBagConstraints);

        lblMms.setText("Mms (g):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 38;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblMms, gridBagConstraints);

        lblRms.setText("Rms (Nm/s):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 40;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblRms, gridBagConstraints);

        lblN0.setText("n0 (%):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 42;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblN0, gridBagConstraints);

        lblSPL_1W.setText("SPL at 1W/1m (dB):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 44;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblSPL_1W, gridBagConstraints);

        lblPowerFilter.setText("Power filter:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblPowerFilter, gridBagConstraints);

        FRDButton.setText("Edit");
        FRDButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                FRDButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 54;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        propertiesPanel.add(FRDButton, gridBagConstraints);

        lblZMA.setText("Impedance (.ZMA):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 56;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblZMA, gridBagConstraints);

        SPL_2_83VField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        SPL_2_83VField.setToolTipText("Reference sound pressure level at 2.83V/1m");
        SPL_2_83VField.setMinimumSize(new java.awt.Dimension(80, 19));
        SPL_2_83VField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 46;
        propertiesPanel.add(SPL_2_83VField, gridBagConstraints);

        lblFRD.setText("Response (.FRD):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 54;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblFRD, gridBagConstraints);

        ZMAButton.setText("Import");
        ZMAButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                ZMAButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 56;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        propertiesPanel.add(ZMAButton, gridBagConstraints);

        lblRe.setText("Re (Ω):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblRe, gridBagConstraints);

        invertedCheckBox.setText("Invert polarity");
        invertedCheckBox.setToolTipText("Switch + and - terminals");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        propertiesPanel.add(invertedCheckBox, gridBagConstraints);

        closedCheckBox.setText("Enclosed");
        closedCheckBox.setToolTipText("Enclosed in it's own enclousure (tweeter)");
        closedCheckBox.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                closedCheckBoxStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        propertiesPanel.add(closedCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        propertiesPanel.add(NameField, gridBagConstraints);

        CrossStartField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        CrossStartField.setToolTipText("Frequency where enclousure simulation starts to merge with FRD/ZMA");
        CrossStartField.setEnabled(false);
        CrossStartField.setMinimumSize(new java.awt.Dimension(80, 19));
        CrossStartField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 58;
        propertiesPanel.add(CrossStartField, gridBagConstraints);

        lblCrossEnd.setText("Cross end (Hz):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 60;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblCrossEnd, gridBagConstraints);

        CrossEndField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        CrossEndField.setToolTipText("Frequency where enclousure simulation ends merging with FRD/ZMA");
        CrossEndField.setEnabled(false);
        CrossEndField.setMinimumSize(new java.awt.Dimension(80, 19));
        CrossEndField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 60;
        propertiesPanel.add(CrossEndField, gridBagConstraints);

        lblFs.setText("Fs (Hz):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblFs, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 62;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        propertiesPanel.add(jPanel1, gridBagConstraints);

        ZMAExportButton.setText("Export");
        ZMAExportButton.setEnabled(false);
        ZMAExportButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                ZMAExportButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 56;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        propertiesPanel.add(ZMAExportButton, gridBagConstraints);

        PeFField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        PeFField.setToolTipText("High pass filter frequency (12dB/octave)");
        PeFField.setMinimumSize(new java.awt.Dimension(80, 19));
        PeFField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 52;
        propertiesPanel.add(PeFField, gridBagConstraints);

        lblPeF.setText("Pe defined at (Hz):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 52;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblPeF, gridBagConstraints);

        lblPe.setText("Pe (W):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 48;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblPe, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 50;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        propertiesPanel.add(powerFilterComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        propertiesPanel.add(shapeComboBox, gridBagConstraints);

        lblShape.setText("Shape:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblShape, gridBagConstraints);

        HeightField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        HeightField.setToolTipText("Effective height (membrane + 1/3 surround)");
        HeightField.setMinimumSize(new java.awt.Dimension(80, 19));
        HeightField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 30;
        propertiesPanel.add(HeightField, gridBagConstraints);

        lblHeight.setText("Height (mm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblHeight, gridBagConstraints);

        WidthField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        WidthField.setToolTipText("Effective width (membrane + 1/3 surround)");
        WidthField.setMinimumSize(new java.awt.Dimension(80, 19));
        WidthField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 28;
        propertiesPanel.add(WidthField, gridBagConstraints);

        lblWidth.setText("Width (mm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblWidth, gridBagConstraints);

        SPL_2_83VButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        SPL_2_83VButton.setBorderPainted(false);
        SPL_2_83VButton.setContentAreaFilled(false);
        SPL_2_83VButton.setEnabled(false);
        SPL_2_83VButton.setFocusable(false);
        SPL_2_83VButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        SPL_2_83VButton.setMaximumSize(new java.awt.Dimension(120, 19));
        SPL_2_83VButton.setMinimumSize(new java.awt.Dimension(120, 19));
        SPL_2_83VButton.setPreferredSize(new java.awt.Dimension(120, 19));
        SPL_2_83VButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                SPL_2_83VButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 46;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(SPL_2_83VButton, gridBagConstraints);

        scrollPane.setViewportView(propertiesPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(scrollPane, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void OKButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_OKButtonActionPerformed
    {//GEN-HEADEREND:event_OKButtonActionPerformed
        result = true;
        
        Driver.copy(drv, origDriver);
        origDriver.PowerFilter.setType(PowerFilter.valueOf(powerFilterComboBox.getSelectedItem().toString()));
        
        dispose();
    }//GEN-LAST:event_OKButtonActionPerformed

    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_CancelButtonActionPerformed
    {//GEN-HEADEREND:event_CancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_CancelButtonActionPerformed

    private void copyFromButtonToField(JButton button, JFormattedTextField field)
    {
        try
        {
            field.setValue(Fnc.parseNumber(button.getText()));
            field.requestFocusInWindow();
        }
        catch (ParseException ex)
        {
            UI.exception(this, ex);
        }
        
        UI.hideButton(button);
    }
    
    private void QtsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_QtsButtonActionPerformed
    {//GEN-HEADEREND:event_QtsButtonActionPerformed
        copyFromButtonToField(QtsButton, QtsField);
    }//GEN-LAST:event_QtsButtonActionPerformed

    private void VasButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_VasButtonActionPerformed
    {//GEN-HEADEREND:event_VasButtonActionPerformed
        copyFromButtonToField(VasButton, VasField);
    }//GEN-LAST:event_VasButtonActionPerformed

    private void FsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_FsButtonActionPerformed
    {//GEN-HEADEREND:event_FsButtonActionPerformed
        copyFromButtonToField(FsButton, FsField);
    }//GEN-LAST:event_FsButtonActionPerformed

    private void QesButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_QesButtonActionPerformed
    {//GEN-HEADEREND:event_QesButtonActionPerformed
        copyFromButtonToField(QesButton, QesField);
    }//GEN-LAST:event_QesButtonActionPerformed

    private void QmsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_QmsButtonActionPerformed
    {//GEN-HEADEREND:event_QmsButtonActionPerformed
        copyFromButtonToField(QmsButton, QmsField);
    }//GEN-LAST:event_QmsButtonActionPerformed

    private void ReButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ReButtonActionPerformed
    {//GEN-HEADEREND:event_ReButtonActionPerformed
        copyFromButtonToField(ReButton, ReField);
    }//GEN-LAST:event_ReButtonActionPerformed

    private void BLButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_BLButtonActionPerformed
    {//GEN-HEADEREND:event_BLButtonActionPerformed
        copyFromButtonToField(BLButton, BLField);
    }//GEN-LAST:event_BLButtonActionPerformed

    private void LeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_LeButtonActionPerformed
    {//GEN-HEADEREND:event_LeButtonActionPerformed
        copyFromButtonToField(LeButton, LeField);
    }//GEN-LAST:event_LeButtonActionPerformed

    private void XmaxButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_XmaxButtonActionPerformed
    {//GEN-HEADEREND:event_XmaxButtonActionPerformed
        copyFromButtonToField(XmaxButton, XmaxField);
    }//GEN-LAST:event_XmaxButtonActionPerformed

    private void DiaButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_DiaButtonActionPerformed
    {//GEN-HEADEREND:event_DiaButtonActionPerformed
        copyFromButtonToField(DiaButton, DiaField);
    }//GEN-LAST:event_DiaButtonActionPerformed

    private void SdButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_SdButtonActionPerformed
    {//GEN-HEADEREND:event_SdButtonActionPerformed
        copyFromButtonToField(SdButton, SdField);
    }//GEN-LAST:event_SdButtonActionPerformed

    private void VdButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_VdButtonActionPerformed
    {//GEN-HEADEREND:event_VdButtonActionPerformed
        copyFromButtonToField(VdButton, VdField);
    }//GEN-LAST:event_VdButtonActionPerformed

    private void CmsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_CmsButtonActionPerformed
    {//GEN-HEADEREND:event_CmsButtonActionPerformed
        copyFromButtonToField(CmsButton, CmsField);
    }//GEN-LAST:event_CmsButtonActionPerformed

    private void MmsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_MmsButtonActionPerformed
    {//GEN-HEADEREND:event_MmsButtonActionPerformed
        copyFromButtonToField(MmsButton, MmsField);
    }//GEN-LAST:event_MmsButtonActionPerformed

    private void RmsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_RmsButtonActionPerformed
    {//GEN-HEADEREND:event_RmsButtonActionPerformed
        copyFromButtonToField(RmsButton, RmsField);
    }//GEN-LAST:event_RmsButtonActionPerformed

    private void n0ButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_n0ButtonActionPerformed
    {//GEN-HEADEREND:event_n0ButtonActionPerformed
        copyFromButtonToField(n0Button, n0Field);
    }//GEN-LAST:event_n0ButtonActionPerformed

    private void SPL_1WButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_SPL_1WButtonActionPerformed
    {//GEN-HEADEREND:event_SPL_1WButtonActionPerformed
        copyFromButtonToField(SPL_1WButton, SPL_1WField);
    }//GEN-LAST:event_SPL_1WButtonActionPerformed

    private void FRDButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_FRDButtonActionPerformed
    {//GEN-HEADEREND:event_FRDButtonActionPerformed
        try
        {
            new ResponsesWindow((java.awt.Frame) SwingUtilities.getWindowAncestor(this), drv).setVisible(true);
            
            boolean cross = drv.hasZMA() || drv.hasFRD();
            CrossStartField.setEnabled(cross);
            CrossEndField.setEnabled(cross);
        }
        catch (Exception ex)
        {
            UI.throwable(this, ex);
        }
        
        renderGraphs();
    }//GEN-LAST:event_FRDButtonActionPerformed

    private void ImportButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ImportButtonActionPerformed
    {//GEN-HEADEREND:event_ImportButtonActionPerformed
        try
        {
            FileSelector fc = new FileSelector(".sdrv");
            fc.setFileFilter(new FileNameExtensionFilter("SpeakerSim driver file", "sdrv"));

            if (fc.showOpenDialog(this) == FileSelector.APPROVE_OPTION)
            {
                drv.open(fc.getSelectedFile());
                load(drv);
            }
        }
        catch (Exception ex)
        {
            UI.throwable(this, ex);
        }
    }//GEN-LAST:event_ImportButtonActionPerformed

    private void ExportButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ExportButtonActionPerformed
    {//GEN-HEADEREND:event_ExportButtonActionPerformed
        try
        {
            FileSelector fc = new FileSelector(".sdrv");
            fc.setFileFilter(new FileNameExtensionFilter("SpeakerSim driver file", "sdrv"));
            fc.setSelectedFile(new File(NameField.getText()));

            if (fc.showSaveDialog(this) == FileSelector.APPROVE_OPTION)
            {
                drv.save(fc.getSelectedFile());
            }
        }
        catch (Exception ex)
        {
            UI.throwable(this, ex);
        }
    }//GEN-LAST:event_ExportButtonActionPerformed

    private void ZMAButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ZMAButtonActionPerformed
    {//GEN-HEADEREND:event_ZMAButtonActionPerformed
        try
        {
            if (drv.hasZMA())
            {
                drv.removeZMA();
                ZMAButton.setText("Import");
                ZMAExportButton.setEnabled(false);
                boolean cross = drv.hasFRD();
                CrossStartField.setEnabled(cross);
                CrossEndField.setEnabled(cross);
            }
            else
            {
                FileSelector fc = new FileSelector(".zma");
                fc.setFileFilter(new FileNameExtensionFilter("Impedance Data", "zma"));

                if (fc.showOpenDialog(this) == FileSelector.APPROVE_OPTION)
                {
                    drv.importZMA(fc.getSelectedFile());
                    ZMAButton.setText("Remove");
                    ZMAExportButton.setEnabled(true);
                    CrossStartField.setEnabled(true);
                    CrossEndField.setEnabled(true);
                }
            }
        }
        catch (Exception ex)
        {
            UI.throwable(this, ex);
        }
        
        renderGraphs();
    }//GEN-LAST:event_ZMAButtonActionPerformed

    private void closedCheckBoxStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_closedCheckBoxStateChanged
    {//GEN-HEADEREND:event_closedCheckBoxStateChanged
        renderGraphs();
    }//GEN-LAST:event_closedCheckBoxStateChanged

    private void ZMAExportButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ZMAExportButtonActionPerformed
    {//GEN-HEADEREND:event_ZMAExportButtonActionPerformed
        try
        {
            FileSelector fs = new FileSelector(".zma");
            fs.setFileFilter(new FileNameExtensionFilter("Impedance Data", "zma"));

            if (fs.showSaveDialog(this) == FileSelector.APPROVE_OPTION)
            {
                drv.exportZMA(fs.getSelectedFile());
            }
        }
        catch (Exception ex)
        {
            UI.throwable(this, ex);
        }
    }//GEN-LAST:event_ZMAExportButtonActionPerformed

    private void SPL_2_83VButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_SPL_2_83VButtonActionPerformed
    {//GEN-HEADEREND:event_SPL_2_83VButtonActionPerformed
        copyFromButtonToField(SPL_2_83VButton, SPL_2_83VField);
    }//GEN-LAST:event_SPL_2_83VButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BLButton;
    private javax.swing.JFormattedTextField BLField;
    private javax.swing.JButton CancelButton;
    private javax.swing.JButton CmsButton;
    private javax.swing.JFormattedTextField CmsField;
    private javax.swing.JFormattedTextField CrossEndField;
    private javax.swing.JFormattedTextField CrossStartField;
    private javax.swing.JButton DiaButton;
    private javax.swing.JFormattedTextField DiaField;
    private javax.swing.JButton ExportButton;
    private javax.swing.JButton FRDButton;
    private javax.swing.JButton FsButton;
    private javax.swing.JFormattedTextField FsField;
    private javax.swing.JFormattedTextField HeightField;
    private javax.swing.JButton ImportButton;
    private javax.swing.JButton LeButton;
    private javax.swing.JFormattedTextField LeField;
    private javax.swing.JButton MmsButton;
    private javax.swing.JFormattedTextField MmsField;
    private javax.swing.JTextField NameField;
    private javax.swing.JButton OKButton;
    private javax.swing.JFormattedTextField PeFField;
    private javax.swing.JFormattedTextField PeField;
    private javax.swing.JButton QesButton;
    private javax.swing.JFormattedTextField QesField;
    private javax.swing.JButton QmsButton;
    private javax.swing.JFormattedTextField QmsField;
    private javax.swing.JButton QtsButton;
    private javax.swing.JFormattedTextField QtsField;
    private javax.swing.JButton ReButton;
    private javax.swing.JFormattedTextField ReField;
    private javax.swing.JButton RmsButton;
    private javax.swing.JFormattedTextField RmsField;
    private javax.swing.JButton SPL_1WButton;
    private javax.swing.JFormattedTextField SPL_1WField;
    private javax.swing.JButton SPL_2_83VButton;
    private javax.swing.JFormattedTextField SPL_2_83VField;
    private javax.swing.JButton SdButton;
    private javax.swing.JFormattedTextField SdField;
    private javax.swing.JButton VasButton;
    private javax.swing.JFormattedTextField VasField;
    private javax.swing.JButton VdButton;
    private javax.swing.JFormattedTextField VdField;
    private javax.swing.JFormattedTextField WidthField;
    private javax.swing.JButton XmaxButton;
    private javax.swing.JFormattedTextField XmaxField;
    private javax.swing.JButton ZMAButton;
    private javax.swing.JButton ZMAExportButton;
    private javax.swing.JCheckBox closedCheckBox;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JCheckBox invertedCheckBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblBL;
    private javax.swing.JLabel lblCms;
    private javax.swing.JLabel lblCrossEnd;
    private javax.swing.JLabel lblCrossStart;
    private javax.swing.JLabel lblDia;
    private javax.swing.JLabel lblFRD;
    private javax.swing.JLabel lblFs;
    private javax.swing.JLabel lblHeight;
    private javax.swing.JLabel lblLe;
    private javax.swing.JLabel lblMms;
    private javax.swing.JLabel lblN0;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPe;
    private javax.swing.JLabel lblPeF;
    private javax.swing.JLabel lblPowerFilter;
    private javax.swing.JLabel lblQes;
    private javax.swing.JLabel lblQms;
    private javax.swing.JLabel lblQts;
    private javax.swing.JLabel lblRe;
    private javax.swing.JLabel lblRms;
    private javax.swing.JLabel lblSPL_1W;
    private javax.swing.JLabel lblSPL_2_83V;
    private javax.swing.JLabel lblSd;
    private javax.swing.JLabel lblShape;
    private javax.swing.JLabel lblVas;
    private javax.swing.JLabel lblVd;
    private javax.swing.JLabel lblWidth;
    private javax.swing.JLabel lblXmax;
    private javax.swing.JLabel lblZMA;
    private javax.swing.JButton n0Button;
    private javax.swing.JFormattedTextField n0Field;
    private javax.swing.JComboBox<String> powerFilterComboBox;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JComboBox<String> shapeComboBox;
    private javax.swing.JTabbedPane tabs;
    // End of variables declaration//GEN-END:variables
}
