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
import java.awt.Color;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.text.ParseException;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DriverWindow extends javax.swing.JDialog
{
    private final Project project;
    private final Driver origDriver;
    private final Driver drv;
    private boolean result;
    private boolean valid;
    
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
        n0Field.setValue(driver.N0 * 100);
        SPL_1WField.setValue(driver.SPL_1W);
        SPL_2_83VField.setValue(driver.SPL_2_83V);
        PeField.setValue(driver.Pe);
        PeFField.setValue(driver.PeF);
        powerFilterComboBox.setSelectedItem(driver.PowerFilter.toString());
        CrossStartField.setValue(driver.CrossStart);
        CrossEndField.setValue(driver.CrossEnd);
        closedCheckBox.setSelected(driver.Closed);
        invertedCheckBox.setSelected(driver.Inverted);
        seriesSpin.setValue(driver.Series);
        parallelSpin.setValue(driver.Parallel);
        
        DiaField.setEnabled(drv.shape == Driver.Shape.Circular);
        WidthField.setEnabled(drv.shape == Driver.Shape.Rectangular);
        HeightField.setEnabled(drv.shape == Driver.Shape.Rectangular);
        
        PeFField.setEnabled(powerFilterComboBox.getSelectedIndex() != 0);
        
        if (driver.hasFRD())
        {
            CrossStartField.setEnabled(true);
            CrossEndField.setEnabled(true);
            FRDButton.setText("Manage");
        }
        else
        {
            CrossStartField.setEnabled(driver.hasZMA());
            CrossEndField.setEnabled(driver.hasZMA());
            FRDButton.setText("Import");
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
                drv.Vas = UI.getDouble(VasField) / 1000;
                refresh();
            }
        });
        
        FsField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Fs = UI.getDouble(FsField);
                refresh();
            }
        });
        
        QesField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Qes = UI.getDouble(QesField);
                refresh();
            }
        });
        
        QmsField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Qms = UI.getDouble(QmsField);
                refresh();
            }
        });
        
        QtsField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Qts = UI.getDouble(QtsField);
                refresh();
            }
        });
        
        ReField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Re = UI.getDouble(ReField);
                refresh();
            }
        });
        
        BLField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Bl = UI.getDouble(BLField);
                refresh();
            }
        });
        
        LeField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Le = UI.getDouble(LeField) / 1000;
                refresh();
            }
        });
        
        XmaxField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Xmax = UI.getDouble(XmaxField) / 1000;
                refresh();
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
                drv.Dia = UI.getDouble(DiaField) / 1000;
                refresh();
            }
        });
        
        WidthField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Width = UI.getDouble(WidthField) / 1000;
                refresh();
            }
        });
        
        HeightField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Height = UI.getDouble(HeightField) / 1000;
                refresh();
            }
        });
        
        SdField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Sd = UI.getDouble(SdField) / 10000;
                refresh();
            }
        });
        
        VdField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Vd = UI.getDouble(VdField) / 1000000;
                refresh();
            }
        });
        
        CmsField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Cms = UI.getDouble(CmsField) / 1000;
                refresh();
            }
        });
        
        MmsField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Mms = UI.getDouble(MmsField) / 1000;
                refresh();
            }
        });
        
        RmsField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Rms = UI.getDouble(RmsField);
                refresh();
            }
        });
        
        n0Field.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.N0 = UI.getDouble(n0Field) / 100;
                refresh();
            }
        });
        
        SPL_1WField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.SPL_1W = UI.getDouble(SPL_1WField);
                refresh();
            }
        });
        
        SPL_2_83VField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.SPL_2_83V = UI.getDouble(SPL_2_83VField);
                refresh();
            }
        });
        
        PeField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.Pe = UI.getDouble(PeField);
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
                drv.PeF = UI.getDouble(PeFField);
            }
        });
        
        CrossStartField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.CrossStart = UI.getDouble(CrossStartField);
                refresh();
            }
        });
        
        CrossEndField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                drv.CrossEnd = UI.getDouble(CrossEndField);
                refresh();
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
        
        seriesSpin.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                int x = (Integer) seriesSpin.getValue();
                if (x > 0)
                {
                    if (drv.Series != x)
                    {
                        drv.Series = x;
                        renderGraphs();
                        lblGroups.setText(" = " + drv.Series * drv.Parallel + " drivers");
                    }
                }
                else
                {
                    seriesSpin.setValue(drv.Series);
                }
            }
        });
        
        parallelSpin.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                int x = (Integer) parallelSpin.getValue();
                if (x > 0)
                {
                    if (drv.Parallel != x)
                    {
                        drv.Parallel = x;
                        renderGraphs();
                        lblGroups.setText(" = " + drv.Series * drv.Parallel + " drivers");
                    }
                }
                else
                {
                    parallelSpin.setValue(drv.Parallel);
                }
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
        
        Graph spl = new Graph("SPL at 2.83V", "Hz", "dB");
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
        
        spl.setYRange(project.Settings.MinSPL, project.Settings.MaxSPL);
        phase.setYRange(-180, 180);
        phase.addYMark(0, "");
        impedance.setYRange(0, project.Settings.MaxImpedance);
        impedancePhase.setYRange(-180, 180);
        impedancePhase.addYMark(0, "");
        
        if (cross)
        {
            spl.addXMark(UI.getDouble(CrossStartField), "Cross start");
            spl.addXMark(UI.getDouble(CrossEndField), "Cross end");

            phase.addXMark(UI.getDouble(CrossStartField), "Cross start");
            phase.addXMark(UI.getDouble(CrossEndField), "Cross end");
        }
        
        tabs.addTab("SPL at 2.83V", spl.getGraph());
        tabs.addTab("Phase", phase.getGraph());
        tabs.addTab("Impedance", impedance.getGraph());
        tabs.addTab("Impedance phase", impedancePhase.getGraph());
        
        UI.setSelectedTab(tabs, selectedTab);
    }
    
    private void refresh()
    {
        valid = true;
        
        setButton(VasButton, VasField, drv.calcVas(project.Environment.AirDensity, project.Environment.SpeedOfSound) * 1000);
        setButton(FsButton, FsField, drv.calcFs());
        setButton(QesButton, QesField, drv.calcQes());
        setButton(QmsButton, QmsField, drv.calcQms());
        setButton(QtsButton, QtsField, drv.calcQts());
        setButton(BLButton, BLField, drv.calcBl());
        setButton(SdButton, SdField, drv.calcSd() * 10000);
        setButton(DiaButton, DiaField, drv.calcDia() * 1000);
        setButton(VdButton, VdField, drv.calcVd() * 1000000);
        setButton(CmsButton, CmsField, drv.calcCms(project.Environment.AirDensity, project.Environment.SpeedOfSound) * 1000);
        setButton(MmsButton, MmsField, drv.calcMms() * 1000);
        setButton(RmsButton, RmsField, drv.calcRms());
        setButton(n0Button, n0Field, drv.calcN0(project.Environment.AirDensity, project.Environment.SpeedOfSound) * 100);
        setButton(SPL_1WButton, SPL_1WField, drv.calcSPL_1W());
        setButton(SPL_2_83VButton, SPL_2_83VField, drv.calcSPL_2_83V());

        renderGraphs();
    }
    
    private void setButton(JButton button, JFormattedTextField field, double calcValue)
    {
        try
        {
            double value = UI.getDouble(field);
            String calcText = Fnc.twoDecimalFormat(calcValue);
            double diff = Math.abs(Math.min((calcValue - value) / value, (Fnc.parseNumber(calcText).doubleValue() - value) / value));
            
            if (!Double.isNaN(calcValue) && !Double.isInfinite(calcValue) && calcValue > 0 && diff > 0.01)
            {
                button.setText(calcText);
                button.setForeground(diff > 0.1 ? Color.RED : Color.BLACK);
                button.setVisible(true);
                
                valid &= diff <= 0.1;
            }
            else
            {
                button.setVisible(false);
            }
        }
        catch (ParseException ex)
        {
            UI.throwable(this, ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
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
        ReField = UI.decimalField(0);
        FsButton = new javax.swing.JButton();
        FsField = UI.decimalField(0);
        lblVas = new javax.swing.JLabel();
        QmsButton = new javax.swing.JButton();
        lblQes = new javax.swing.JLabel();
        QmsField = UI.decimalField(0);
        lblQms = new javax.swing.JLabel();
        LeField = UI.decimalField(0);
        lblQts = new javax.swing.JLabel();
        DiaField = UI.decimalField(0);
        DiaButton = new javax.swing.JButton();
        lblSPL_2_83V = new javax.swing.JLabel();
        lblBL = new javax.swing.JLabel();
        lblLe = new javax.swing.JLabel();
        lblXmax = new javax.swing.JLabel();
        lblDia = new javax.swing.JLabel();
        lblSd = new javax.swing.JLabel();
        VdField = UI.decimalField(0);
        MmsField = UI.decimalField(0);
        n0Field = UI.decimalField(0);
        PeField = UI.decimalField(0);
        VasField = UI.decimalField(0);
        QesField = UI.decimalField(0);
        QtsField = UI.decimalField(0);
        BLField = UI.decimalField(0);
        XmaxField = UI.decimalField(0);
        SdField = UI.decimalField(0);
        CmsField = UI.decimalField(0);
        RmsField = UI.decimalField(0);
        SPL_1WField = UI.decimalField(1);
        VasButton = new javax.swing.JButton();
        VdButton = new javax.swing.JButton();
        MmsButton = new javax.swing.JButton();
        n0Button = new javax.swing.JButton();
        QesButton = new javax.swing.JButton();
        QtsButton = new javax.swing.JButton();
        BLButton = new javax.swing.JButton();
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
        SPL_2_83VField = UI.decimalField(1);
        lblFRD = new javax.swing.JLabel();
        ZMAButton = new javax.swing.JButton();
        lblRe = new javax.swing.JLabel();
        invertedCheckBox = new javax.swing.JCheckBox();
        closedCheckBox = new javax.swing.JCheckBox();
        NameField = new javax.swing.JTextField();
        CrossStartField = UI.decimalField(0);
        lblCrossEnd = new javax.swing.JLabel();
        CrossEndField = UI.decimalField(0);
        lblFs = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        ZMAExportButton = new javax.swing.JButton();
        PeFField = UI.decimalField(0);
        lblPeF = new javax.swing.JLabel();
        lblPe = new javax.swing.JLabel();
        powerFilterComboBox = new javax.swing.JComboBox(PowerFilter.FILTERS);
        shapeComboBox = new javax.swing.JComboBox(Driver.Shape.values());
        lblShape = new javax.swing.JLabel();
        HeightField = UI.decimalField(0);
        lblHeight = new javax.swing.JLabel();
        WidthField = UI.decimalField(0);
        lblWidth = new javax.swing.JLabel();
        SPL_2_83VButton = new javax.swing.JButton();
        seriesSpin = new javax.swing.JSpinner();
        parallelSpin = new javax.swing.JSpinner();
        lblSeries = new javax.swing.JLabel();
        lblParallel = new javax.swing.JLabel();
        lblGroups = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Driver");
        setMinimumSize(new java.awt.Dimension(800, 600));
        setPreferredSize(new java.awt.Dimension(1200, 800));
        getContentPane().setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.1;
        getContentPane().add(tabs, gridBagConstraints);

        OKButton.setText("Save");
        OKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OKButtonActionPerformed(evt);
            }
        });
        controlPanel.add(OKButton);

        CancelButton.setText("Cancel");
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });
        controlPanel.add(CancelButton);

        ImportButton.setText("Import");
        ImportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImportButtonActionPerformed(evt);
            }
        });
        controlPanel.add(ImportButton);

        ExportButton.setText("Export");
        ExportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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
        propertiesPanelLayout.rowHeights = new int[] {0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0};
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

        ReField.setToolTipText("DC resistance of the voice coil");
        ReField.setMinimumSize(new java.awt.Dimension(80, 19));
        ReField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        propertiesPanel.add(ReField, gridBagConstraints);

        FsButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        FsButton.setFocusable(false);
        FsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        FsButton.setMaximumSize(new java.awt.Dimension(120, 19));
        FsButton.setMinimumSize(new java.awt.Dimension(120, 19));
        FsButton.setPreferredSize(new java.awt.Dimension(120, 19));
        FsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(FsButton, gridBagConstraints);

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
        QmsButton.setFocusable(false);
        QmsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        QmsButton.setMaximumSize(new java.awt.Dimension(120, 19));
        QmsButton.setMinimumSize(new java.awt.Dimension(120, 19));
        QmsButton.setPreferredSize(new java.awt.Dimension(120, 19));
        QmsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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

        LeField.setToolTipText("Voice coil inductance at 1kHz");
        LeField.setMinimumSize(new java.awt.Dimension(80, 19));
        LeField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 20;
        propertiesPanel.add(LeField, gridBagConstraints);

        lblQts.setText("Qts:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblQts, gridBagConstraints);

        DiaField.setToolTipText("Effective diameter (membrane + 1/3 surround)");
        DiaField.setMinimumSize(new java.awt.Dimension(80, 19));
        DiaField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 26;
        propertiesPanel.add(DiaField, gridBagConstraints);

        DiaButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        DiaButton.setFocusable(false);
        DiaButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DiaButton.setMaximumSize(new java.awt.Dimension(120, 19));
        DiaButton.setMinimumSize(new java.awt.Dimension(120, 19));
        DiaButton.setPreferredSize(new java.awt.Dimension(120, 19));
        DiaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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

        VdField.setToolTipText("Peak displacement volume");
        VdField.setMinimumSize(new java.awt.Dimension(80, 19));
        VdField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 34;
        propertiesPanel.add(VdField, gridBagConstraints);

        MmsField.setToolTipText("Mass of the diaphragm and coil, including acoustic load");
        MmsField.setMinimumSize(new java.awt.Dimension(80, 19));
        MmsField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 38;
        propertiesPanel.add(MmsField, gridBagConstraints);

        n0Field.setToolTipText("Reference efficiency");
        n0Field.setMinimumSize(new java.awt.Dimension(80, 19));
        n0Field.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 42;
        propertiesPanel.add(n0Field, gridBagConstraints);

        PeField.setToolTipText("Nominal input power");
        PeField.setMinimumSize(new java.awt.Dimension(80, 19));
        PeField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 48;
        propertiesPanel.add(PeField, gridBagConstraints);

        VasField.setToolTipText("Volume of air equal to compliance");
        VasField.setMinimumSize(new java.awt.Dimension(80, 19));
        VasField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        propertiesPanel.add(VasField, gridBagConstraints);

        QesField.setToolTipText("Electrical Q at resonance");
        QesField.setMinimumSize(new java.awt.Dimension(80, 19));
        QesField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        propertiesPanel.add(QesField, gridBagConstraints);

        QtsField.setToolTipText("Total Q at resonance");
        QtsField.setMinimumSize(new java.awt.Dimension(80, 19));
        QtsField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        propertiesPanel.add(QtsField, gridBagConstraints);

        BLField.setToolTipText("Motor strength");
        BLField.setMinimumSize(new java.awt.Dimension(80, 19));
        BLField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        propertiesPanel.add(BLField, gridBagConstraints);

        XmaxField.setToolTipText("Peak linear displacement of cone");
        XmaxField.setMinimumSize(new java.awt.Dimension(80, 19));
        XmaxField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 22;
        propertiesPanel.add(XmaxField, gridBagConstraints);

        SdField.setToolTipText("Effective driver radiating area");
        SdField.setMinimumSize(new java.awt.Dimension(80, 19));
        SdField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 32;
        propertiesPanel.add(SdField, gridBagConstraints);

        CmsField.setToolTipText("Compliance of suspension");
        CmsField.setMinimumSize(new java.awt.Dimension(80, 19));
        CmsField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 36;
        propertiesPanel.add(CmsField, gridBagConstraints);

        RmsField.setToolTipText("Mechanical resistance of suspension");
        RmsField.setMinimumSize(new java.awt.Dimension(80, 19));
        RmsField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 40;
        propertiesPanel.add(RmsField, gridBagConstraints);

        SPL_1WField.setToolTipText("Reference sound pressure level at 1W/1m");
        SPL_1WField.setMinimumSize(new java.awt.Dimension(80, 19));
        SPL_1WField.setPreferredSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 44;
        propertiesPanel.add(SPL_1WField, gridBagConstraints);

        VasButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        VasButton.setFocusable(false);
        VasButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        VasButton.setMaximumSize(new java.awt.Dimension(120, 19));
        VasButton.setMinimumSize(new java.awt.Dimension(120, 19));
        VasButton.setPreferredSize(new java.awt.Dimension(120, 19));
        VasButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VasButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(VasButton, gridBagConstraints);

        VdButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        VdButton.setFocusable(false);
        VdButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        VdButton.setMaximumSize(new java.awt.Dimension(120, 19));
        VdButton.setMinimumSize(new java.awt.Dimension(120, 19));
        VdButton.setPreferredSize(new java.awt.Dimension(120, 19));
        VdButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VdButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 34;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(VdButton, gridBagConstraints);

        MmsButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        MmsButton.setFocusable(false);
        MmsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MmsButton.setMaximumSize(new java.awt.Dimension(120, 19));
        MmsButton.setMinimumSize(new java.awt.Dimension(120, 19));
        MmsButton.setPreferredSize(new java.awt.Dimension(120, 19));
        MmsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MmsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 38;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(MmsButton, gridBagConstraints);

        n0Button.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        n0Button.setFocusable(false);
        n0Button.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        n0Button.setMaximumSize(new java.awt.Dimension(120, 19));
        n0Button.setMinimumSize(new java.awt.Dimension(120, 19));
        n0Button.setPreferredSize(new java.awt.Dimension(120, 19));
        n0Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                n0ButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 42;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(n0Button, gridBagConstraints);

        QesButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        QesButton.setFocusable(false);
        QesButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        QesButton.setMaximumSize(new java.awt.Dimension(120, 19));
        QesButton.setMinimumSize(new java.awt.Dimension(120, 19));
        QesButton.setPreferredSize(new java.awt.Dimension(120, 19));
        QesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                QesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(QesButton, gridBagConstraints);

        QtsButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        QtsButton.setFocusable(false);
        QtsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        QtsButton.setMaximumSize(new java.awt.Dimension(120, 19));
        QtsButton.setMinimumSize(new java.awt.Dimension(120, 19));
        QtsButton.setPreferredSize(new java.awt.Dimension(120, 19));
        QtsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                QtsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(QtsButton, gridBagConstraints);

        BLButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        BLButton.setFocusable(false);
        BLButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        BLButton.setMaximumSize(new java.awt.Dimension(120, 19));
        BLButton.setMinimumSize(new java.awt.Dimension(120, 19));
        BLButton.setPreferredSize(new java.awt.Dimension(120, 19));
        BLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BLButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(BLButton, gridBagConstraints);

        SdButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        SdButton.setFocusable(false);
        SdButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        SdButton.setMaximumSize(new java.awt.Dimension(120, 19));
        SdButton.setMinimumSize(new java.awt.Dimension(120, 19));
        SdButton.setPreferredSize(new java.awt.Dimension(120, 19));
        SdButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SdButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 32;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(SdButton, gridBagConstraints);

        CmsButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        CmsButton.setFocusable(false);
        CmsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        CmsButton.setMaximumSize(new java.awt.Dimension(120, 19));
        CmsButton.setMinimumSize(new java.awt.Dimension(120, 19));
        CmsButton.setPreferredSize(new java.awt.Dimension(120, 19));
        CmsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CmsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 36;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(CmsButton, gridBagConstraints);

        RmsButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        RmsButton.setFocusable(false);
        RmsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        RmsButton.setMaximumSize(new java.awt.Dimension(120, 19));
        RmsButton.setMinimumSize(new java.awt.Dimension(120, 19));
        RmsButton.setPreferredSize(new java.awt.Dimension(120, 19));
        RmsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RmsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 40;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(RmsButton, gridBagConstraints);

        SPL_1WButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        SPL_1WButton.setFocusable(false);
        SPL_1WButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        SPL_1WButton.setMaximumSize(new java.awt.Dimension(120, 19));
        SPL_1WButton.setMinimumSize(new java.awt.Dimension(120, 19));
        SPL_1WButton.setPreferredSize(new java.awt.Dimension(120, 19));
        SPL_1WButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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

        lblCms.setText("Cms (mm/N):");
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

        lblRms.setText("Rms (Ns/m):");
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

        FRDButton.setText("Manage");
        FRDButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        FRDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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
        ZMAButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ZMAButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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
        invertedCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        propertiesPanel.add(invertedCheckBox, gridBagConstraints);

        closedCheckBox.setText("Enclosed");
        closedCheckBox.setToolTipText("Enclosed in it's own enclousure (tweeter)");
        closedCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        closedCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
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
        gridBagConstraints.gridy = 66;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        propertiesPanel.add(jPanel1, gridBagConstraints);

        ZMAExportButton.setText("Export");
        ZMAExportButton.setEnabled(false);
        ZMAExportButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ZMAExportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ZMAExportButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 56;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        propertiesPanel.add(ZMAExportButton, gridBagConstraints);

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
        SPL_2_83VButton.setFocusable(false);
        SPL_2_83VButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        SPL_2_83VButton.setMaximumSize(new java.awt.Dimension(120, 19));
        SPL_2_83VButton.setMinimumSize(new java.awt.Dimension(120, 19));
        SPL_2_83VButton.setPreferredSize(new java.awt.Dimension(120, 19));
        SPL_2_83VButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SPL_2_83VButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 46;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(SPL_2_83VButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 62;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        propertiesPanel.add(seriesSpin, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 64;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        propertiesPanel.add(parallelSpin, gridBagConstraints);

        lblSeries.setText("Series groups:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 62;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblSeries, gridBagConstraints);

        lblParallel.setText("Parallel groups:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 64;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        propertiesPanel.add(lblParallel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 62;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        propertiesPanel.add(lblGroups, gridBagConstraints);

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
        String invalid = "";
        
        if (drv.Name == null || drv.Name.isEmpty())
        {
            invalid += "Name, ";
        }
        
        if (drv.Closed)
        {
            if (!drv.hasFRD())
            {
                invalid += "Response, ";
            }
            
            if (!drv.hasZMA())
            {
                invalid += "Impedance, ";
            }
            
            if (drv.Re <= 0)
            {
                invalid += "Re, ";
            }
        }
        else
        {
            if (drv.Vas <= 0)
            {
                invalid += "Vas, ";
            }
            
            if (drv.Fs <= 0)
            {
                invalid += "Fs, ";
            }
            
            if (drv.Qes <= 0)
            {
                invalid += "Qes, ";
            }
            
            if (drv.Qms <= 0)
            {
                invalid += "Qms, ";
            }
            
            if (drv.Qts <= 0)
            {
                invalid += "Qts, ";
            }
            
            if (drv.Re <= 0)
            {
                invalid += "Re, ";
            }
            
            if (drv.Bl <= 0)
            {
                invalid += "Bl, ";
            }
            
            if (drv.Le <= 0)
            {
                invalid += "Le, ";
            }
            
            if (drv.Xmax <= 0)
            {
                invalid += "Xmax, ";
            }
        }
        
        if (Driver.Shape.Circular.equals(drv.shape))
        {
            if (drv.Dia <= 0)
            {
                invalid += "Dia, ";
            }
        }
        else if (Driver.Shape.Rectangular.equals(drv.shape))
        {
            if (drv.Width <= 0)
            {
                invalid += "Width, ";
            }

            if (drv.Height <= 0)
            {
                invalid += "Height, ";
            }
        }
        
        if (!drv.Closed)
        {
            if (drv.Sd <= 0)
            {
                invalid += "Sd, ";
            }
            
            if (drv.Vd <= 0)
            {
                invalid += "Vd, ";
            }
            
            if (drv.Cms <= 0)
            {
                invalid += "Cms, ";
            }
            
            if (drv.Mms <= 0)
            {
                invalid += "Mms, ";
            }
            
            if (drv.Rms <= 0)
            {
                invalid += "Rms, ";
            }
            
            if (drv.N0 <= 0)
            {
                invalid += "n0, ";
            }
        }
        
        if (drv.SPL_1W <= 0)
        {
            invalid += "SPL at 1W/1m, ";
        }

        if (drv.SPL_2_83V <= 0)
        {
            invalid += "SPL at 2.83V/1m, ";
        }

        if (drv.Pe <= 0)
        {
            invalid += "Pe, ";
        }
        
        if (!invalid.isEmpty())
        {
            UI.error(this, "Invalid parameters:\n" + invalid.substring(0, invalid.length() - 2));
        }
        else
        {
            if (!valid)
            {
                if (UI.options(this, "Parameters are more than 10% off. Inconsistent parameters will result in invalid simulation.", new String[]{"Save anyway", "Cancel"}) != 0)
                {
                    return;
                }
            }
            
            if (!drv.hasFRD() || !drv.hasZMA())
            {
                if (UI.options(this, "There is no frequency or impedance response data. This data is required for good results.", new String[]{"Save anyway", "Cancel"}) != 0)
                {
                    return;
                }
            }
            
            result = true;
            Driver.copy(drv, origDriver);
            origDriver.PowerFilter.setType(PowerFilter.valueOf(powerFilterComboBox.getSelectedItem().toString()));
            dispose();
        }
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
            UI.throwable(this, ex);
        }
        
        button.setVisible(false);
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

    private void BLButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_BLButtonActionPerformed
    {//GEN-HEADEREND:event_BLButtonActionPerformed
        copyFromButtonToField(BLButton, BLField);
    }//GEN-LAST:event_BLButtonActionPerformed

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
            if (!drv.hasFRD())
            {
                drv.FRD = ResponsesWindow.importFRD();
                if (drv.hasFRD())
                {
                    drv.FRD = ResponsesWindow.editDialog(this, drv.FRD, false);
                }
            }
            
            if (drv.hasFRD())
            {
                new ResponsesWindow((java.awt.Frame) SwingUtilities.getWindowAncestor(this), drv).setVisible(true);

                boolean cross = drv.hasZMA() || drv.hasFRD();
                CrossStartField.setEnabled(cross);
                CrossEndField.setEnabled(cross);
                if (drv.hasFRD())
                {
                    FRDButton.setText("Manage");
                }
                else
                {
                    FRDButton.setText("Import");
                }
            }
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
            fc.setFileFilter(new FileNameExtensionFilter("SpeakerSim or WinISD driver file", "sdrv", "wdr"));

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
                fc.setFileFilter(new FileNameExtensionFilter("Impedance Data", "zma", "txt", "csv"));

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
    private javax.swing.JLabel lblGroups;
    private javax.swing.JLabel lblHeight;
    private javax.swing.JLabel lblLe;
    private javax.swing.JLabel lblMms;
    private javax.swing.JLabel lblN0;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblParallel;
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
    private javax.swing.JLabel lblSeries;
    private javax.swing.JLabel lblShape;
    private javax.swing.JLabel lblVas;
    private javax.swing.JLabel lblVd;
    private javax.swing.JLabel lblWidth;
    private javax.swing.JLabel lblXmax;
    private javax.swing.JLabel lblZMA;
    private javax.swing.JButton n0Button;
    private javax.swing.JFormattedTextField n0Field;
    private javax.swing.JSpinner parallelSpin;
    private javax.swing.JComboBox<String> powerFilterComboBox;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JSpinner seriesSpin;
    private javax.swing.JComboBox<String> shapeComboBox;
    private javax.swing.JTabbedPane tabs;
    // End of variables declaration//GEN-END:variables
}
