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
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.*;

public class MainWindow extends javax.swing.JFrame
{
    private File file;
    private final FileSelector fc;
    private boolean listen = true;
    public Project project;
    
    private double[] freq;
    private double[] response;
    private double[] responsePhase;
    private double[] impedance;
    private double[] impedancePhase;
    
    public MainWindow(String arg) throws IOException
    {
        URL iconURL = getClass().getClassLoader().getResource("SpeakerSim.png");
        setIconImage(new ImageIcon(iconURL).getImage());

        if (arg != null)
        {
            file = new File(arg);
            project = new Project(file);
            
            versionCheck();
            
            setTitle("SpeakerSim - " + file.getName() + " (" + Project.currentVersion() + ")");
        }
        else
        {
            setTitle("SpeakerSim (" + Project.currentVersion() + ")");
            
            project = new Project(getClass().getClassLoader().getResourceAsStream("default.ssim"));
        }
        
        initComponents();
        setLocationRelativeTo(null);
        
        ButtonGroup group = new ButtonGroup();
        group.add(menuSimulatorBassReflex);
        group.add(menuSimulatorClosedBox);
        group.add(menuSimulatorAperiodic);
        group.add(menuSimulatorOpenBaffle);
        
        fc = new FileSelector(".ssim");
        fc.setFileFilter(new FileNameExtensionFilter("SpeakerSim project", "ssim"));
        
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
        
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        ActionMap actionMap = tree.getActionMap();
        actionMap.remove("cut");
        actionMap.getParent().remove("cut");
        actionMap.remove("copy");
        actionMap.getParent().remove("copy");
        actionMap.remove("paste");
        actionMap.getParent().remove("paste");
        
        tree.addTreeSelectionListener(new TreeSelectionListener()
        {
            @Override
            public void valueChanged(TreeSelectionEvent e)
            {
                TreePath newPath = e.getNewLeadSelectionPath();
                TreePath oldPath = e.getOldLeadSelectionPath();
                
                if (newPath == null)
                {
                    if (((DefaultMutableTreeNode) oldPath.getLastPathComponent()).getParent() != null)
                    {
                        tree.setSelectionPath(oldPath);
                    }
                }
                else
                {
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) newPath.getLastPathComponent();
                    
                    if (oldPath != null || selectedNode.isRoot())
                    {
                        List<Speaker> speakers = getSpeakers(selectedNode);
                        project.CenterPosition = new Position(0, 0, 0);

                        for (Speaker speaker : speakers)
                        {
                            project.CenterPosition = project.CenterPosition.add(speaker.Position);
                        }
                        project.CenterPosition = project.CenterPosition.divide(speakers.size());

                        ((IItem) selectedNode.getUserObject()).refresh();
                        refreshNode();
                    }
                }
            }
        });
        
        tree.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    tree.setSelectionPath(selPath);
                    showMenu(e.getX(), e.getY());
                }
                 
                Object item = getSelectedItem();
                if (item != null && e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e))
                {
                    editItem();
                }
            }
        });
        
        tree.addKeyListener(new KeyListener()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                int key = e.getKeyCode();
                
                if (key == KeyEvent.VK_DELETE)
                {
                    Object node = tree.getLastSelectedPathComponent();
                    if (node != tree.getModel().getRoot())
                    {
                        deleteItem();
                    }
                }
                else if (key == KeyEvent.VK_ENTER)
                {
                    editItem();
                }
                else if (key == KeyEvent.VK_CONTEXT_MENU)
                {
                    showMenu();
                }
                else if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)
                {
                    switch (key)
                    {
                        case KeyEvent.VK_C:
                            if (tree.getLastSelectedPathComponent() != tree.getModel().getRoot())
                            {
                                copyItem();
                            }
                            break;
                        
                        case KeyEvent.VK_X:
                            if (tree.getLastSelectedPathComponent() != tree.getModel().getRoot())
                            {
                                cutItem();
                            }
                            break;
                        
                        case KeyEvent.VK_V:
                            if (canPasteItem())
                            {
                                pasteItem();
                            }
                            break;
                        
                        default: break;
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
        
        tree.addTreeWillExpandListener(new TreeWillExpandListener()
        {
            @Override
            public void treeWillExpand(TreeExpansionEvent e)
            {
            }
            
            @Override
            public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException
            {
                throw new ExpandVetoException(e);
            }
        });

        load();
        
        listeningPosXField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0, project.Environment.RoomX * 100))
                {
                    project.ListeningPosition.X = UI.getDouble(e) / 100;
                    refresh();
                }
            }
        });
        
        listeningPosYField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0, project.Environment.RoomY * 100))
                {
                    project.ListeningPosition.Y = UI.getDouble(e) / 100;
                    refresh();
                }
            }
        });
        
        listeningPosZField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0, project.Environment.RoomZ * 100))
                {
                    project.ListeningPosition.Z = UI.getDouble(e) / 100;
                    refresh();
                }
            }
        });
    }
    
    private void versionCheck()
    {
        if (project.Version.compareTo(Project.currentVersion()) > 0)
        {
            UI.warning("Project file was created with newer version. It may not work correctly!");
        }
    }
    
    private IItem getSelectedItem()
    {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (selectedNode != null)
        {
            return (IItem) selectedNode.getUserObject();
        }
        
        return null;
    }
    
    protected void submenuAdd(JMenu menu, final DefaultMutableTreeNode node, final boolean above)
    {
        JMenuItem mi;
        
        Object target = above ? ((DefaultMutableTreeNode) node.getParent()).getUserObject() : node.getUserObject();
        Object child = above ? node.getUserObject() : null;
        
        mi = new JMenuItem("Speaker");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Speaker speaker = new Speaker();
                if (editItem(speaker))
                {
                    addItem(speaker, above, node);
                }
            }
        });
        mi.setEnabled(!(child instanceof PassFilter) && !(child instanceof LPad) && !(child instanceof Zobel));
        menu.add(mi);

        menu.addSeparator();

        mi = new JMenuItem("Low pass filter");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                LowPassFilter filter = new LowPassFilter();
                if (editItem(filter))
                {
                    addItem(filter, above, node);
                }
            }
        });
        mi.setEnabled(!(target instanceof Speaker));
        menu.add(mi);

        mi = new JMenuItem("High pass filter");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                HighPassFilter filter = new HighPassFilter();
                if (editItem(filter))
                {
                    addItem(filter, above, node);
                }
            }
        });
        mi.setEnabled(!(target instanceof Speaker));
        menu.add(mi);
        
        menu.addSeparator();

        mi = new JMenuItem("L pad");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                LPad filter = new LPad();
                if (editItem(filter))
                {
                    addItem(filter, above, node);
                }
            }
        });
        mi.setEnabled(!(target instanceof Speaker));
        menu.add(mi);
        
        mi = new JMenuItem("Zobel");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Zobel filter = new Zobel();
                if (editItem(filter))
                {
                    addItem(filter, above, node);
                }
            }
        });
        mi.setEnabled(!(target instanceof Speaker));
        menu.add(mi);

        menu.addSeparator();

        mi = new JMenuItem("Parallel notch filter");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ParallelNotchFilter filter = new ParallelNotchFilter();
                if (editItem(filter))
                {
                    addItem(filter, above, node);
                }
            }
        });
        menu.add(mi);

        mi = new JMenuItem("Serial notch filter");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                SerialNotchFilter filter = new SerialNotchFilter();
                if (editItem(filter))
                {
                    addItem(filter, above, node);
                }
            }
        });
        menu.add(mi);

        menu.addSeparator();

        mi = new JMenuItem("Resistor");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Resistor filter = new Resistor();
                if (editItem(filter))
                {
                    addItem(filter, above, node);
                }
            }
        });
        menu.add(mi);

        mi = new JMenuItem("Capacitor");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Capacitor filter = new Capacitor();
                if (editItem(filter))
                {
                    addItem(filter, above, node);
                }
            }
        });
        menu.add(mi);

        mi = new JMenuItem("Inductor");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Inductor filter = new Inductor();
                if (editItem(filter))
                {
                    addItem(filter, above, node);
                }
            }
        });
        menu.add(mi);
    }
    
    protected JPopupMenu itemMenu()
    {
        JPopupMenu popup = new JPopupMenu("Item");
        
        if (!tree.isSelectionEmpty())
        {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            
            if (node != tree.getModel().getRoot())
            {
                JMenuItem mi = new JMenuItem("Edit");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        editItem();
                    }
                });
                popup.add(mi);
                
                if (!(node.getUserObject() instanceof Amplifier))
                {
                    JMenu addAboveMenu = new JMenu("Add above");
                    submenuAdd(addAboveMenu, node, true);
                    popup.add(addAboveMenu);
                }
                
                JMenu addBelowMenu = new JMenu("Add below");
                submenuAdd(addBelowMenu, node, false);
                popup.add(addBelowMenu);
                
                mi = new JMenuItem("Delete");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        deleteItem();
                    }
                });
                popup.add(mi);
                
                popup.addSeparator();
                
                mi = new JMenuItem("Import");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        importItem();
                    }
                });
                popup.add(mi);
                
                mi = new JMenuItem("Export");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        exportItem();
                    }
                });
                popup.add(mi);
                
                popup.addSeparator();
                
                mi = new JMenuItem("Move Up");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        moveUpItem();
                    }
                });
                mi.setEnabled(canMoveUpItem());
                popup.add(mi);
                
                mi = new JMenuItem("Move Down");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        moveDownItem();
                    }
                });
                mi.setEnabled(canMoveDownItem());
                popup.add(mi);
                
                mi = new JMenuItem("Move Left");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        moveLeftItem();
                    }
                });
                mi.setEnabled(canMoveLeftItem());
                popup.add(mi);
                
                mi = new JMenuItem("Move Right");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        moveRightItem();
                    }
                });
                mi.setEnabled(canMoveRightItem());
                popup.add(mi);
                
                popup.addSeparator();
                
                mi = new JMenuItem("Cut");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        cutItem();
                    }
                });
                popup.add(mi);
                
                mi = new JMenuItem("Copy");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        copyItem();
                    }
                });
                popup.add(mi);
                
                mi = new JMenuItem("Paste");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        pasteItem();
                    }
                });
                mi.setEnabled(canPasteItem());
                popup.add(mi);
                
                popup.addSeparator();
                
                if (!getSpeakers(node).isEmpty())
                {
                    mi = new JMenuItem("Export FRD");
                    mi.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            exportFRD();
                        }
                    });
                    popup.add(mi);
                }
                
                mi = new JMenuItem("Export ZMA");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        exportZMA();
                    }
                });
                popup.add(mi);
            }
            else
            {
                JMenuItem mi = new JMenuItem("Add amplifier");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        Amplifier filter = new Amplifier();
                        if (editItem(filter))
                        {
                            addItem(filter, node);
                        }
                    }
                });
                popup.add(mi);
                
                popup.addSeparator();
                
                mi = new JMenuItem("Import");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        importItem();
                    }
                });
                popup.add(mi);
                
                popup.addSeparator();
                
                mi = new JMenuItem("Paste");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        pasteItem();
                    }
                });
                mi.setEnabled(canPasteItem());
                popup.add(mi);
                
                if (!getSpeakers(node).isEmpty())
                {
                    popup.addSeparator();
                    
                    mi = new JMenuItem("Export FRD");
                    mi.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            exportFRD();
                        }
                    });
                    mi.setEnabled(!(worker != null && worker.isAlive()));
                    popup.add(mi);
                }
            }
        }
        
        return popup;
    }
    
    protected void showMenu(int x, int y)
    {
        if (!tree.isSelectionEmpty())
        {
            itemMenu().show(tree, x, y);
        }
    }
    
    protected void showMenu()
    {
        Rectangle bounds = tree.getPathBounds(tree.getSelectionPath());
        showMenu(bounds.x, (int) bounds.getMaxY());
    }
    
    private static void expandAllNodes(JTree tree)
    {
        int j = tree.getRowCount();
        int i = 0;
        
        while (i < j)
        {
            tree.expandRow(i);
            i++;
            j = tree.getRowCount();
        }
    }
    
    private static List<Speaker> getSpeakers(DefaultMutableTreeNode node)
    {
        List<Speaker> speakers = new ArrayList<Speaker>();
        
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> e = node.depthFirstEnumeration();
        while (e.hasMoreElements())
        {
            Object x = e.nextElement().getUserObject();
            if (x instanceof Speaker)
            {
                speakers.add((Speaker) x);
            }
        }
        
        return speakers;
    }
    
    private void selectNode(DefaultMutableTreeNode node)
    {
        expandAllNodes(tree);
        
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        TreePath path = new TreePath(model.getPathToRoot(node));

        tree.setSelectionPath(path);
    }
    
    protected void addItem(IItem item, boolean above, DefaultMutableTreeNode target)
    {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(item);
        IItem targetItem = (IItem) target.getUserObject();
        
        if (above)
        {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) target.getParent();
            IItem parentItem = (IItem) parent.getUserObject();
            
            model.insertNodeInto(node, parent, parent.getIndex(target));
            model.removeNodeFromParent(target);
            model.insertNodeInto(target, node, 0);
            
            int i = parentItem.getChildren().indexOf(targetItem);
            parentItem.getChildren().set(i, item);
            item.getChildren().add(targetItem);
        }
        else
        {
            model.insertNodeInto(node, target, target.getChildCount());
            
            targetItem.getChildren().add(item);
        }
        
        project.setModified();
        item.refresh();
        selectNode(node);
        showNode(node);
    }
    
    protected void addItem(IItem item, DefaultMutableTreeNode target)
    {
        addItem(item, false, target);
    }
    
    private boolean editItem(IItem item)
    {
        if (item instanceof Speaker)
        {
            if (new DriverWindow(this, project, ((Speaker) item).Driver).showDialog())
            {
                return true;
            }
        }
        else if (item instanceof Amplifier)
        {
            if (new AmplifierWindow(this, (Amplifier) item).showDialog())
            {
                return true;
            }
        }
        else if (item instanceof PassFilter)
        {
            if (new PassFilterWindow(this, (PassFilter) item).showDialog())
            {
                return true;
            }
        }
        else if (item instanceof NotchFilter)
        {
            NotchFilter filter = (NotchFilter) item;
            
            JFormattedTextField l = new JFormattedTextField();
            l.setFormatterFactory(UI.FORMATTER);
            l.setValue(filter.L * 1000);
            
            JFormattedTextField c = new JFormattedTextField();
            c.setFormatterFactory(UI.FORMATTER);
            c.setValue(filter.C * 1000000);
            
            JFormattedTextField r = new JFormattedTextField();
            r.setFormatterFactory(UI.FORMATTER);
            r.setValue(filter.R);
            
            Picture picture = null;
            try
            {
                picture = new Picture(getClass().getClassLoader().getResourceAsStream(filter.getClass().getSimpleName() + ".png"));
            }
            catch (IOException ex)
            {
                
            }
            
            final JComponent[] inputs = new JComponent[]
            {
                picture == null ? new JPanel() : picture,
                new JLabel("Inductance (mH): "), l,
                new JLabel("Capacitance (μF): "), c,
                new JLabel("Resistance (Ω): "), r
            };
            
            if (UI.dialog(this, filter.name(), inputs))
            {
                filter.L = UI.getDouble(l) / 1000;
                filter.C = UI.getDouble(c) / 1000000;
                filter.R = UI.getDouble(r);
                return true;
            }
        }
        else if (item instanceof LPad)
        {
            final LPad filter = (LPad) item;
            
            List<Speaker> speakers = getSpeakers((DefaultMutableTreeNode) tree.getLastSelectedPathComponent()); // TODO?
            final double Z = speakers.size() == 1 ? speakers.get(0).Driver.Re : 0;
            
            final JFormattedTextField attenuation = new JFormattedTextField();
            attenuation.setFormatterFactory(UI.FORMATTER);
            attenuation.setEnabled(Z != 0);
            
            final JFormattedTextField s = new JFormattedTextField();
            s.setFormatterFactory(UI.FORMATTER);
            s.setValue(filter.Rs);
            
            final JFormattedTextField p = new JFormattedTextField();
            p.setFormatterFactory(UI.FORMATTER);
            
            attenuation.addPropertyChangeListener("value", new PropertyChangeListener()
            {
                @Override
                public void propertyChange(PropertyChangeEvent e)
                {
                    if (listen && UI.validate(e, 0))
                    {
                        listen = false;
                        
                        double a = Fnc.toAmplitude(UI.getDouble(e));
                        s.setValue(Z * ((a - 1) / a));
                        p.setValue(Z * (1 / (a - 1)));
                        
                        listen = true;
                    }
                }
            });
            
            PropertyChangeListener updateAttenuation = new PropertyChangeListener()
            {
                @Override
                public void propertyChange(PropertyChangeEvent e)
                {
                    if (listen && UI.validate(e, 0))
                    {
                        listen = false;
                        
                        double Rs = UI.getDouble(s);
                        double Rp = UI.getDouble(p);
                        double z = Z;
                        
                        if (Rp > 0)
                        {
                            z = z * Rp / (z + Rp);
                        }
                        z = z / (z + Rs);
                        
                        attenuation.setValue(Math.abs(Fnc.toDecibels(z)));
                        
                        listen = true;
                    }
                }
            };
            
            s.addPropertyChangeListener("value", updateAttenuation);
            p.addPropertyChangeListener("value", updateAttenuation);
            
            p.setValue(filter.Rp);
            
            Picture picture = null;
            try
            {
                picture = new Picture(getClass().getClassLoader().getResourceAsStream("LPad.png"));
            }
            catch (IOException ex)
            {
                
            }
            
            final JComponent[] inputs = new JComponent[]
            {
                picture == null ? new JPanel() : picture,
                new JLabel("Attenuation (dB): "), attenuation,
                new JLabel("Serial resistance (Ω): "), s,
                new JLabel("Parallel resistance (Ω): "), p,
            };
            
            if (UI.dialog(this, "L pad", inputs))
            {
                filter.Rs = UI.getDouble(s);
                filter.Rp = UI.getDouble(p);
                return true;
            }
        }
        else if (item instanceof Zobel)
        {
            final Zobel filter = (Zobel) item;
            
            List<Speaker> speakers = getSpeakers((DefaultMutableTreeNode) tree.getLastSelectedPathComponent()); // TODO?
            final double R = speakers.size() == 1 ? speakers.get(0).Driver.Re : 0;
            final double Le = speakers.size() == 1 ? speakers.get(0).Driver.Le : 0;
            
            final JButton calc = new JButton("Calculate");
            calc.setEnabled(R != 0 && Le != 0);
            
            final JFormattedTextField c = new JFormattedTextField();
            c.setFormatterFactory(UI.FORMATTER);
            c.setValue(filter.C * 1000000);
            
            final JFormattedTextField r = new JFormattedTextField();
            r.setFormatterFactory(UI.FORMATTER);
            r.setValue(filter.R);
            
            calc.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    c.setValue(Zobel.calcC(R, Le) * 1000000);
                    r.setValue(R);
                }
            });
            
            Picture picture = null;
            try
            {
                picture = new Picture(getClass().getClassLoader().getResourceAsStream("Zobel.png"));
            }
            catch (IOException ex)
            {
                
            }
            
            final JComponent[] inputs = new JComponent[]
            {
                picture == null ? new JPanel() : picture,
                calc,
                new JLabel("Capacitance (μF): "), c,
                new JLabel("Resistance (Ω): "), r,
            };
            
            if (UI.dialog(this, "Zobel", inputs))
            {
                filter.C = UI.getDouble(c) / 1000000;
                filter.R = UI.getDouble(r);
                return true;
            }
        }
        else if (item instanceof Resistor)
        {
            Resistor resistor = (Resistor) item;
            JFormattedTextField value = new JFormattedTextField();
            value.setFormatterFactory(UI.FORMATTER);
            value.setValue(resistor.R);
            final JComponent[] inputs = new JComponent[] { new JLabel("Resistance (Ω): "), value };
            
            if (UI.dialog(this, "Resistor", inputs))
            {
                resistor.R = UI.getDouble(value);
                return true;
            }
        }
        else if (item instanceof Capacitor)
        {
            Capacitor capacitor = (Capacitor) item;
            JFormattedTextField value = new JFormattedTextField();
            value.setFormatterFactory(UI.FORMATTER);
            value.setValue(capacitor.C * 1000000);
            final JComponent[] inputs = new JComponent[] { new JLabel("Capacitance (μF): "), value };
            
            if (UI.dialog(this, "Capacitor", inputs))
            {
                capacitor.C = UI.getDouble(value) / 1000000;
                return true;
            }
        }
        else if (item instanceof Inductor)
        {
            Inductor inductor = (Inductor) item;
            JFormattedTextField value = new JFormattedTextField();
            value.setFormatterFactory(UI.FORMATTER);
            value.setValue(inductor.L * 1000);
            final JComponent[] inputs = new JComponent[] { new JLabel("Inductance (mH): "), value };
            
            if (UI.dialog(this, "Inductor", inputs))
            {
                inductor.L = UI.getDouble(value) / 1000;
                return true;
            }
        }
        
        return false;
    }
    
    private void editItem()
    {
        if (editItem(getSelectedItem()))
        {
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            model.nodeChanged((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
            refresh();
        }
    }
    
    protected void deleteItem()
    {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        TreePath[] paths = tree.getSelectionPaths();
        for (TreePath path : paths)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
            
            ((IItem) parent.getUserObject()).getChildren().remove((IItem) node.getUserObject());
            
            selectNode(parent);
            model.removeNodeFromParent(node);
            project.setModified();
        }
    }
    
    protected void addItemChildren(DefaultMutableTreeNode parent, IItem item)
    {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(item);
        model.insertNodeInto(node, parent, parent.getChildCount());
        
        for (IItem i : item.getChildren())
        {
            addItemChildren(node, i);
        }
        
        selectNode(node);
    }
    
    protected void importItem()
    {
        try
        {
            FileSelector fs = new FileSelector(".spart");
            fs.setFileFilter(new FileNameExtensionFilter("SpeakerSim export file", "spart"));

            if (fs.showOpenDialog(this) == FileSelector.APPROVE_OPTION)
            {
                IItem item = Item.constructItem(JSON.open(fs.getSelectedFile()));
                
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                
                ((IItem) parent.getUserObject()).getChildren().add(item);
                
                addItemChildren(parent, item);
                
                project.setModified();
            }
        }
        catch (Exception ex)
        {
            UI.throwable(this, ex);
        }
    }
    
    protected void exportItem()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        IItem part = (IItem) node.getUserObject();
        
        try
        {
            FileSelector fs = new FileSelector(".spart");
            fs.setFileFilter(new FileNameExtensionFilter("SpeakerSim export file", "spart"));
            
            if (fs.showSaveDialog(this) == FileSelector.APPROVE_OPTION)
            {
                JSON.save(Item.itemToJSON(part), fs.getSelectedFile());
            }
        }
        catch (Exception ex)
        {
            UI.throwable(this, ex);
        }
    }
    
    private void exportFRD()
    {
        try
        {
            FileSelector fs = new FileSelector(".frd");
            fs.setFileFilter(new FileNameExtensionFilter("Frequency Response Data", "frd"));

            if (fs.showSaveDialog(this) == FileSelector.APPROVE_OPTION)
            {
                if (worker != null && worker.isAlive())
                {
                    try
                    {
                        worker.join();
                    }
                    catch (InterruptedException ex)
                    {
                    }
                }
                
                try (PrintWriter writer = new PrintWriter(fs.getSelectedFile(), "UTF-8"))
                {
                    for (int i = 0; i < freq.length; i++)
                    {
                        writer.print(new ResponseEntry(freq[i], response[i], responsePhase[i]).toString());
                    }

                    writer.close();
                }
            }
        }
        catch (Exception ex)
        {
            UI.throwable(this, ex);
        }
    }

    private void exportZMA()
    {
        try
        {
            FileSelector fs = new FileSelector(".zma");
            fs.setFileFilter(new FileNameExtensionFilter("Impedance Data", "zma"));

            if (fs.showSaveDialog(this) == FileSelector.APPROVE_OPTION)
            {
                if (worker != null && worker.isAlive())
                {
                    try
                    {
                        worker.join();
                    }
                    catch (InterruptedException ex)
                    {
                    }
                }
                
                try (PrintWriter writer = new PrintWriter(fs.getSelectedFile(), "UTF-8"))
                {
                    for (int i = 0; i < freq.length; i++)
                    {
                        writer.print(new ResponseEntry(freq[i], impedance[i], impedancePhase[i]).toString());
                    }

                    writer.close();
                }
            }
        }
        catch (Exception ex)
        {
            UI.throwable(this, ex);
        }
    }
    
    protected boolean canMoveUpItem()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        return node.getPreviousSibling() != null;
    }
    
    protected boolean canMoveDownItem()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        return node.getNextSibling() != null;
    }
    
    protected boolean canMoveLeftItem()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Object item = node.getUserObject();
        Object target = ((DefaultMutableTreeNode) node.getParent()).getUserObject();
        
        return !(item instanceof Amplifier || target instanceof Amplifier);
    }
    
    protected boolean canMoveRightItem()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Object item = node.getUserObject();
        Object target = node.getPreviousSibling();
        if (target == null)
        {
            return false;
        }
        target = ((DefaultMutableTreeNode) target).getUserObject();
        
        return !(item instanceof Amplifier || (target instanceof Speaker && (item instanceof PassFilter || item instanceof LPad || item instanceof Zobel)));
    }
    
    protected void moveUpItem()
    {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        
        int pos = parent.getIndex(node.getPreviousSibling());
        model.removeNodeFromParent(node);
        model.insertNodeInto(node, parent, pos);
        
        IItem p = (IItem) parent.getUserObject();
        p.getChildren().remove((IItem) node.getUserObject());
        p.getChildren().add(pos, (IItem) node.getUserObject());
        
        selectNode(node);
        project.setModified();
    }
    
    protected void moveDownItem()
    {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        
        int pos = parent.getIndex(node.getNextSibling());
        model.removeNodeFromParent(node);
        model.insertNodeInto(node, parent, pos);
        
        IItem p = (IItem) parent.getUserObject();
        p.getChildren().remove((IItem) node.getUserObject());
        p.getChildren().add(pos, (IItem) node.getUserObject());
        
        selectNode(node);
        project.setModified();
    }
    
    protected void moveLeftItem()
    {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        
        DefaultMutableTreeNode target = (DefaultMutableTreeNode) parent.getParent();
        DefaultMutableTreeNode nextSibling = parent.getNextSibling();
        int pos = nextSibling == null ? parent.getSiblingCount() : target.getIndex(nextSibling);
        
        ((IItem) parent.getUserObject()).getChildren().remove((IItem) node.getUserObject());
        ((IItem) target.getUserObject()).getChildren().add(pos, (IItem) node.getUserObject());
        
        model.removeNodeFromParent(node);
        model.insertNodeInto(node, target, pos);
        
        selectNode(node);
        project.setModified();
    }
    
    protected void moveRightItem()
    {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        
        DefaultMutableTreeNode target = node.getPreviousSibling();
        
        ((IItem) parent.getUserObject()).getChildren().remove((IItem) node.getUserObject());
        ((IItem) target.getUserObject()).getChildren().add((IItem) node.getUserObject());
        
        model.removeNodeFromParent(node);
        model.insertNodeInto(node, target, target.getChildCount());
        
        selectNode(node);
        project.setModified();
    }
    
    protected boolean canPasteItem()
    {
        Class type = CopyPaste.getType();
        IItem target = getSelectedItem();
        
        return (type == Amplifier.class && target instanceof Project)
                || (type != null && type != Amplifier.class && !(target instanceof Project)
                && !(target instanceof Speaker && (type == LowPassFilter.class || type == HighPassFilter.class || type == LPad.class || type == Zobel.class)));
    }
    
    protected void copyItem()
    {
        CopyPaste.set(getSelectedItem());
    }
    
    protected void cutItem()
    {
        copyItem();
        deleteItem();
    }
    
    protected void pasteItem()
    {
        IItem item = CopyPaste.get();
        if (item != null)
        {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            
            ((IItem) parent.getUserObject()).getChildren().add(item);
            
            addItemChildren(parent, item);
            
            project.setModified();
        }
    }
    
    private void createTables()
    {
        freq = new double[project.Settings.Points - 1];
        response = new double[freq.length];
        responsePhase = new double[freq.length];
        impedance = new double[freq.length];
        impedancePhase = new double[freq.length];

        double f = project.Settings.StartFrequency;
        double m = project.Settings.multiplier();
        for (int i = 0; i < freq.length; i++)
        {
            freq[i] = f;
            f *= m;
        }
    }
    
    private void changeSimulator()
    {                                                  
        Speaker speaker = (Speaker) getSelectedItem();

        if (menuSimulatorClosedBox.isSelected())
        {
            speaker.setSimulator(Speaker.SimulatorType.CLOSED_BOX);
        }
        else if (menuSimulatorBassReflex.isSelected())
        {
            speaker.setSimulator(Speaker.SimulatorType.BASS_REFLEX);
        }
        else if (menuSimulatorAperiodic.isSelected())
        {
            speaker.setSimulator(Speaker.SimulatorType.APERIODIC);
        }
        else if (menuSimulatorOpenBaffle.isSelected())
        {
            speaker.setSimulator(Speaker.SimulatorType.OPEN_BAFFLE);
        }
        
        refresh();
    }
    
    private Component enclosurePanel;
    private Component bafflePanel;
    private Component driverPositionPanel;
    
    private void showPanels(IItem item)
    {
        if (item instanceof Speaker)
        {
            Speaker speaker = (Speaker) item;

            if (bafflePanel == null)
            {
                bafflePanel = new BafflePanel(this);
                propertiesPanel.add(bafflePanel);
            }
            else
            {
                UI.setPanelEnabled((JPanel) bafflePanel, project.Settings.BaffleSimulation);
            }
            
            if (driverPositionPanel == null)
            {
                driverPositionPanel = new DriverPositionPanel(this);
                propertiesPanel.add(driverPositionPanel);
            }

            menuEnclosure.setEnabled(!speaker.Driver.Closed);

            if (!speaker.Driver.Closed)
            {
                switch (speaker.Simulator)
                {
                    case CLOSED_BOX:
                        if (!(enclosurePanel instanceof ClosedPanel))
                        {
                            if (enclosurePanel != null)
                            {
                                propertiesPanel.remove(enclosurePanel);
                            }
                            enclosurePanel = new ClosedPanel(this);
                            propertiesPanel.add(enclosurePanel);
                        }
                        menuSimulatorClosedBox.setSelected(true);
                        break;

                    case BASS_REFLEX:
                        if (!(enclosurePanel instanceof BassReflexPanel))
                        {
                            if (enclosurePanel != null)
                            {
                                propertiesPanel.remove(enclosurePanel);
                            }
                            enclosurePanel = new BassReflexPanel(this);
                            propertiesPanel.add(enclosurePanel);
                        }
                        menuSimulatorBassReflex.setSelected(true);
                        break;

                    case APERIODIC:
                        if (!(enclosurePanel instanceof AperiodicPanel))
                        {
                            if (enclosurePanel != null)
                            {
                                propertiesPanel.remove(enclosurePanel);
                            }
                            enclosurePanel = new AperiodicPanel(this);
                            propertiesPanel.add(enclosurePanel);
                        }
                        menuSimulatorAperiodic.setSelected(true);
                        break;

                    case OPEN_BAFFLE:
                        if (enclosurePanel != null)
                        {
                            propertiesPanel.remove(enclosurePanel);
                        }
                        enclosurePanel = null;
                        menuSimulatorOpenBaffle.setSelected(true);
                        break;
                }
            }
            else if (enclosurePanel != null)
            {
                propertiesPanel.remove(enclosurePanel);
                enclosurePanel = null;
            }
        }
        else
        {
            menuEnclosure.setEnabled(false);
            
            if (enclosurePanel != null)
            {
                propertiesPanel.remove(enclosurePanel);
                enclosurePanel = null;
            }
            
            if (bafflePanel != null)
            {
                propertiesPanel.remove(bafflePanel);
                bafflePanel = null;
            }
            
            if (driverPositionPanel != null)
            {
                propertiesPanel.remove(driverPositionPanel);
                driverPositionPanel = null;
            }
        }
    }
    
    private Complex totalResponse(IItem item, double f)
    {
        Complex r = item.response(f);
        Complex baffleResponse = project.Settings.BaffleSimulation ? item.responseWithBaffle(f).divide(r) : new Complex(1);
        Complex roomResponse = project.Settings.RoomSimulation ? item.responseWithRoom(f).divide(r) : new Complex(1);
        return r.multiply(baffleResponse).multiply(roomResponse);
    }
    
    private Thread worker;
    
    private void showNode(final DefaultMutableTreeNode node)
    {
        // update properties panel
        final IItem item = (IItem) node.getUserObject();
        showPanels(item);

        if (bafflePanel != null)
        {
            ((BafflePanel) bafflePanel).show(((Speaker) item).Baffle);
        }

        if (driverPositionPanel != null)
        {
            ((DriverPositionPanel) driverPositionPanel).show(((Speaker) item).Position);
        }

        if (enclosurePanel != null)
        {
            ((ISpeakerPanel) enclosurePanel).show((Speaker) item);
        }
        
        propertiesPanel.revalidate();
        propertiesPanel.repaint();

        // stop worker, if running
        if (worker != null && worker.isAlive())
        {
            worker.interrupt();
            try
            {
                worker.join();
            }
            catch (InterruptedException ex)
            {
            }
        }
        
        final String selectedTab = UI.getSelectedTab(tabs);
        tabs.removeAll();
        
        final Component parent = this;
        
        worker = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    final IItem item = (IItem) node.getUserObject();
                    final List<Speaker> speakers = getSpeakers(node);

                    if (!speakers.isEmpty())
                    {
                        double[] listeningWindow = new double[freq.length];
                        double[] power = new double[freq.length];
                        double[] directivity = new double[freq.length];
                        double[] maxSPL = new double[freq.length];
                        double[] maxPower = new double[freq.length];
                        double[] excursion = new double[freq.length];
                        double[] groupDelay = new double[freq.length];
                        double[] filter = new double[freq.length];
                        double[] baffle = new double[freq.length];
                        double[] room = new double[freq.length];

                        for (int i = 0; i < freq.length; i++)
                        {
                            if (Thread.currentThread().isInterrupted()) return;

                            double f = freq[i];

                            Complex r = item.response(f);
                            Complex baffleResponse = project.Settings.BaffleSimulation ? item.responseWithBaffle(f).divide(r) : new Complex(1);
                            Complex roomResponse = project.Settings.RoomSimulation ? item.responseWithRoom(f).divide(r) : new Complex(1);
                            r = r.multiply(baffleResponse).multiply(roomResponse);

                            response[i] = Fnc.toDecibels(r.abs());
                            listeningWindow[i] = Fnc.toDecibels(item.listeningWindowResponse(f).abs());
                            power[i] = Fnc.toDecibels(item.powerResponse(f).abs());
                            directivity[i] = listeningWindow[i] - power[i];
                            responsePhase[i] = Math.toDegrees(r.phase());
                            Complex z = item.impedance(f);
                            impedance[i] = z.abs();
                            impedancePhase[i] = z.phase();
                            maxPower[i] = item.maxPower(f);
                            maxSPL[i] = Fnc.toDecibels(item.response1W(f).multiply(baffleResponse).multiply(roomResponse).abs()) + Fnc.powerToDecibels(maxPower[i]);
                            excursion[i] = item.excursion(f, Double.MAX_VALUE);
                            groupDelay[i] = Math.abs((Math.abs(Math.toDegrees(totalResponse(item, f + 0.1).phase())) - Math.abs(responsePhase[i])) / (360 * 0.1)) * 1000;
                            filter[i] = Fnc.toDecibels(item.filter(f).abs());
                            baffle[i] = Fnc.toDecibels(baffleResponse.abs());
                            room[i] = Fnc.toDecibels(roomResponse.abs());
                        }

                        if (project.Settings.Smoothing > 0)
                        {
                            int points = (int)Math.round(project.Settings.pointsPerOctave()) / project.Settings.Smoothing;

                            response = Fnc.smooth(response, points);
                            listeningWindow = Fnc.smooth(listeningWindow, points);
                            power = Fnc.smooth(power, points);
                            directivity = Fnc.smooth(directivity, points);
                            maxSPL = Fnc.smooth(maxSPL, points);
                            baffle = Fnc.smooth(baffle, points);
                            room = Fnc.smooth(room, points);
                        }

                        groupDelay = Fnc.smooth(groupDelay, (int)Math.round(project.Settings.pointsPerOctave()) / (project.Settings.Smoothing > 0 ? Math.min(3, project.Settings.Smoothing) : 3));
                        maxPower = Fnc.smooth(maxPower, (int)Math.round(project.Settings.pointsPerOctave()) / (project.Settings.Smoothing > 0 ? Math.min(6, project.Settings.Smoothing) : 6));
                        filter = Fnc.smooth(filter, (int)Math.round(project.Settings.pointsPerOctave()) / (project.Settings.Smoothing > 0 ? Math.min(6, project.Settings.Smoothing) : 6));

                        final Graph graphSPL = new Graph("Frequency response", "Hz", freq, "dB", response);
                        final Graph graphPower = new Graph("Power response", "Hz", freq, "dB", power);
                        final Graph graphListeningWindow = new Graph("Listening window", "Hz", freq, "dB", listeningWindow);
                        final Graph graphDirectivity = new Graph("Directivity", "Hz", freq, "dB", directivity);
                        final Graph graphPhase = new Graph("Phase", "Hz", freq, "", responsePhase);
                        final Graph graphMaxSPL = new Graph("Maximal response", "Hz", freq, "dB", maxSPL);
                        final Graph graphMaxPower = new Graph("Maximal power", "Hz", freq, "W", maxPower);
                        final Graph graphExcursion = new Graph("Excursion", "Hz", freq, "mm", excursion);
                        final Graph graphGroupDelay = new Graph("Group delay", "Hz", freq, "ms", groupDelay);
                        final Graph graphFilter = new Graph("Filter", "Hz", freq, "dB", filter);
                        final Graph graphBaffle = new Graph("Baffle diffraction", "Hz", freq, "dB", baffle);
                        final Graph graphRoom = new Graph("Room", "Hz", freq, "dB", room);
                        final Graph graphImpedance = new Graph("Impedance", "Hz", freq, "Ω", impedance);

                        graphSPL.setYRange(graphSPL.getMaxY() - project.Settings.dBRange, graphSPL.getMaxY() + 1);
                        graphListeningWindow.setYRange(graphListeningWindow.getMaxY() - project.Settings.dBRange, graphListeningWindow.getMaxY() + 1);
                        graphPower.setYRange(graphPower.getMaxY() - project.Settings.dBRange, graphPower.getMaxY() + 1);
                        graphDirectivity.setYRange(0, project.Settings.dBRange);
                        graphMaxPower.setYRange(0, Math.min(project.Settings.MaxPower, graphMaxPower.getMaxY() + 1));
                        graphExcursion.setYRange(0, graphExcursion.getMaxY() + 1);
                        graphPhase.addYMark(0, "");
                        
                        if (speakers.size() == 1)
                        {
                            graphExcursion.addYMark(speakers.get(0).Driver.Xmax * 1000, "Xmax");
                        }
                        
                        graphBaffle.setYRange(graphBaffle.getMaxY() - project.Settings.dBRange, graphBaffle.getMaxY() + 1);
                        graphImpedance.setYRange(0, Math.min(graphImpedance.getMaxY() + 1, project.Settings.MaxImpedance));

                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                tabs.addTab("Frequency response", graphSPL.getGraph());
                                tabs.addTab("Listening window", graphListeningWindow.getGraph());
                                tabs.addTab("Power response", graphPower.getGraph());
                                tabs.addTab("Directivity", graphDirectivity.getGraph());
                                tabs.addTab("Phase", graphPhase.getGraph());
                                tabs.addTab("Maximal response", graphMaxSPL.getGraph());
                                tabs.addTab("Maximal power", graphMaxPower.getGraph());
                                
                                if (speakers.size() == 1)
                                {
                                    tabs.addTab("Excursion", graphExcursion.getGraph());
                                }
                                
                                tabs.addTab("Group delay", graphGroupDelay.getGraph());
                                tabs.addTab("Filter", graphFilter.getGraph());
                                tabs.addTab("Baffle diffraction", graphBaffle.getGraph());
                                tabs.addTab("Room", graphRoom.getGraph());
                                
                                if (!(item instanceof Project))
                                {
                                    tabs.addTab("Impedance", graphImpedance.getGraph());
                                }

                                if (enclosurePanel != null)
                                {
                                    ((ISpeakerPanel) enclosurePanel).addGraphs(tabs); // graphs shoud be done in worker thread?
                                }

                                tabs.revalidate();
                                tabs.repaint();
                                UI.setSelectedTab(tabs, selectedTab);
                            }
                        });
                    }
                    else
                    {
                        for (int i = 0; i < freq.length; i++)
                        {
                            if (Thread.currentThread().isInterrupted()) return;

                            Complex z = item.impedance(freq[i]);
                            impedance[i] = z.abs();
                            impedancePhase[i] = z.phase();
                        }
                        
                        final Graph graphImpedance = new Graph("Impedance", "Hz", freq, "Ω", impedance);
                        graphImpedance.setYRange(0, Math.min(graphImpedance.getMaxY() + 1, project.Settings.MaxImpedance));

                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (!(item instanceof Project))
                                {
                                    tabs.addTab("Impedance", graphImpedance.getGraph());
                                }

                                tabs.revalidate();
                                tabs.repaint();
                                UI.setSelectedTab(tabs, selectedTab);
                            }
                        });
                    }
                }
                catch (final Throwable e)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            UI.throwable(parent, e);
                        }
                    });
                }
            }
        };

        worker.start();
    }
    
    public void refreshNode()
    {
        showNode((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
    }
    
    public void refresh()
    {
        project.setModified();
        getSelectedItem().refresh();
        refreshNode();
    }
    
    private void addChildren(DefaultMutableTreeNode node, List<IItem> children)
    {
        for (IItem filter: children)
        {
            DefaultMutableTreeNode x = new DefaultMutableTreeNode(filter);
            addChildren(x, filter.getChildren());
            node.add(x);
        }
    }
    
    private void load()
    {
        createTables();
        
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(project);
        addChildren(rootNode, project.getChildren());
        tree.setModel(new DefaultTreeModel(rootNode));
        expandAllNodes(tree);
        tree.setSelectionPath(new TreePath(((DefaultMutableTreeNode) tree.getModel().getRoot()).getPath()));
        
        listeningPosXField.setValue(project.ListeningPosition.X * 100);
        listeningPosYField.setValue(project.ListeningPosition.Y * 100);
        listeningPosZField.setValue(project.ListeningPosition.Z * 100);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        split = new javax.swing.JSplitPane();
        rightPanel = new javax.swing.JPanel();
        tabs = new javax.swing.JTabbedPane();
        propertiesScrollPane = new javax.swing.JScrollPane();
        propertiesPanel = new javax.swing.JPanel();
        listeningPanel = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        listeningPosXField = new javax.swing.JFormattedTextField();
        listeningPosYField = new javax.swing.JFormattedTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        listeningPosZField = new javax.swing.JFormattedTextField();
        treeScrollPane = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        menuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuFileNew = new javax.swing.JMenuItem();
        menuFileOpen = new javax.swing.JMenuItem();
        menuFileSave = new javax.swing.JMenuItem();
        menuFileSaveAs = new javax.swing.JMenuItem();
        menuFileQuit = new javax.swing.JMenuItem();
        menuProject = new javax.swing.JMenu();
        menuSettings = new javax.swing.JMenuItem();
        menuEnvironment = new javax.swing.JMenuItem();
        menuEnclosure = new javax.swing.JMenu();
        menuSimulatorClosedBox = new javax.swing.JRadioButtonMenuItem();
        menuSimulatorBassReflex = new javax.swing.JRadioButtonMenuItem();
        menuSimulatorAperiodic = new javax.swing.JRadioButtonMenuItem();
        menuSimulatorOpenBaffle = new javax.swing.JRadioButtonMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing(evt);
            }
        });

        split.setDividerLocation(250);

        rightPanel.setLayout(new java.awt.BorderLayout());
        rightPanel.add(tabs, java.awt.BorderLayout.CENTER);

        propertiesScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
        flowLayout1.setAlignOnBaseline(true);
        propertiesPanel.setLayout(flowLayout1);

        listeningPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Listening position in room"));
        java.awt.GridBagLayout listeningPanelLayout = new java.awt.GridBagLayout();
        listeningPanelLayout.columnWidths = new int[] {0, 5, 0};
        listeningPanelLayout.rowHeights = new int[] {0, 5, 0, 5, 0};
        listeningPanel.setLayout(listeningPanelLayout);

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("X (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        listeningPanel.add(jLabel11, gridBagConstraints);

        listeningPosXField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        listeningPosXField.setMinimumSize(new java.awt.Dimension(140, 19));
        listeningPosXField.setPreferredSize(new java.awt.Dimension(140, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        listeningPanel.add(listeningPosXField, gridBagConstraints);

        listeningPosYField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        listeningPosYField.setMinimumSize(new java.awt.Dimension(140, 19));
        listeningPosYField.setPreferredSize(new java.awt.Dimension(140, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        listeningPanel.add(listeningPosYField, gridBagConstraints);

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("Y (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        listeningPanel.add(jLabel13, gridBagConstraints);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Z (cm):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        listeningPanel.add(jLabel14, gridBagConstraints);

        listeningPosZField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.##"))));
        listeningPosZField.setMinimumSize(new java.awt.Dimension(140, 19));
        listeningPosZField.setPreferredSize(new java.awt.Dimension(140, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        listeningPanel.add(listeningPosZField, gridBagConstraints);

        propertiesPanel.add(listeningPanel);

        propertiesScrollPane.setViewportView(propertiesPanel);

        rightPanel.add(propertiesScrollPane, java.awt.BorderLayout.NORTH);

        split.setRightComponent(rightPanel);

        treeScrollPane.setMinimumSize(new java.awt.Dimension(0, 22));

        tree.setToggleClickCount(0);
        treeScrollPane.setViewportView(tree);

        split.setLeftComponent(treeScrollPane);

        getContentPane().add(split, java.awt.BorderLayout.CENTER);

        menuFile.setText("File");

        menuFileNew.setText("New");
        menuFileNew.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuFileNewActionPerformed(evt);
            }
        });
        menuFile.add(menuFileNew);

        menuFileOpen.setText("Open");
        menuFileOpen.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuFileOpenActionPerformed(evt);
            }
        });
        menuFile.add(menuFileOpen);

        menuFileSave.setText("Save");
        menuFileSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuFileSaveActionPerformed(evt);
            }
        });
        menuFile.add(menuFileSave);

        menuFileSaveAs.setText("Save As");
        menuFileSaveAs.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuFileSaveAsActionPerformed(evt);
            }
        });
        menuFile.add(menuFileSaveAs);

        menuFileQuit.setText("Quit");
        menuFileQuit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuFileQuitActionPerformed(evt);
            }
        });
        menuFile.add(menuFileQuit);

        menuBar.add(menuFile);

        menuProject.setText("Project");

        menuSettings.setText("Settings");
        menuSettings.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuSettingsActionPerformed(evt);
            }
        });
        menuProject.add(menuSettings);

        menuEnvironment.setText("Environment");
        menuEnvironment.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuEnvironmentActionPerformed(evt);
            }
        });
        menuProject.add(menuEnvironment);

        menuBar.add(menuProject);

        menuEnclosure.setText("Enclosure");
        menuEnclosure.setEnabled(false);

        menuSimulatorClosedBox.setText("Closed Box");
        menuSimulatorClosedBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuSimulatorActionPerformed(evt);
            }
        });
        menuEnclosure.add(menuSimulatorClosedBox);

        menuSimulatorBassReflex.setText("Bass Reflex");
        menuSimulatorBassReflex.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuSimulatorActionPerformed(evt);
            }
        });
        menuEnclosure.add(menuSimulatorBassReflex);

        menuSimulatorAperiodic.setText("Aperiodic");
        menuSimulatorAperiodic.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuSimulatorActionPerformed(evt);
            }
        });
        menuEnclosure.add(menuSimulatorAperiodic);

        menuSimulatorOpenBaffle.setText("Open Baffle");
        menuSimulatorOpenBaffle.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                menuSimulatorActionPerformed(evt);
            }
        });
        menuEnclosure.add(menuSimulatorOpenBaffle);

        menuBar.add(menuEnclosure);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuFileQuitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuFileQuitActionPerformed
    {//GEN-HEADEREND:event_menuFileQuitActionPerformed
        if (request_close())
        {
            System.exit(0);
        }
    }//GEN-LAST:event_menuFileQuitActionPerformed

    private void menuEnvironmentActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuEnvironmentActionPerformed
    {//GEN-HEADEREND:event_menuEnvironmentActionPerformed
        if (new EnvironmentWindow(this, project.Environment, project.Settings.RoomSimulation).showDialog())
        {
            refresh();
        }
    }//GEN-LAST:event_menuEnvironmentActionPerformed
    
    private boolean request_close()
    {
        if (project != null && project.isModified())
        {
            switch (JOptionPane.showOptionDialog(this, "Project has been modified!", "Save changes?", 0, JOptionPane.WARNING_MESSAGE, null, new String[]{"Save", "Do not save", "Cancel"}, "Save"))
            {
                case 0:
                    if (!save())
                    {
                        return false;
                    }
                    break;
                
                case 1:
                    break;
                
                default:
                    return false;
            }
        }
        
        return true;
    }
    
    private boolean saveAs()
    {
        try
        {
            if (fc.showSaveDialog(this) == FileSelector.APPROVE_OPTION)
            {
                file = fc.getSelectedFile();
                
                setTitle("SpeakerSim - " + file.getName() + " (" + Project.currentVersion() + ")");
                
                project.save(file);
                return true;
            }
        }
        catch (Exception ex)
        {
            UI.throwable(this, ex);
        }
        
        return false;
    }
    
    private boolean save()
    {
        try
        {
            if (file == null)
            {
                return saveAs();
            }
            else
            {
                project.save(file);
                return true;
            }
        }
        catch (Exception ex)
        {
            UI.throwable(this, ex);
        }
        
        return false;
    }
    
    private void open()
    {
        try
        {
            if (request_close())
            {
                if (fc.showOpenDialog(this) == FileSelector.APPROVE_OPTION)
                {
                    file = fc.getSelectedFile();
                    project = new Project(file);
                    
                    versionCheck();
                    load();
                    
                    setTitle("SpeakerSim - " + file.getName() + " (" + Project.currentVersion() + ")");
                }
            }
        }
        catch (Exception ex)
        {
            UI.throwable(this, ex);
        }
    }
    
    private void menuFileOpenActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuFileOpenActionPerformed
    {//GEN-HEADEREND:event_menuFileOpenActionPerformed
        open();
    }//GEN-LAST:event_menuFileOpenActionPerformed

    private void menuFileSaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuFileSaveActionPerformed
    {//GEN-HEADEREND:event_menuFileSaveActionPerformed
        save();
    }//GEN-LAST:event_menuFileSaveActionPerformed

    private void menuFileSaveAsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuFileSaveAsActionPerformed
    {//GEN-HEADEREND:event_menuFileSaveAsActionPerformed
        saveAs();
    }//GEN-LAST:event_menuFileSaveAsActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
        if (request_close())
        {
            System.exit(0);
        }
    }//GEN-LAST:event_formWindowClosing

    private void menuFileNewActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuFileNewActionPerformed
    {//GEN-HEADEREND:event_menuFileNewActionPerformed
        if (request_close())
        {
            Project p = new Project();
            
            if (new SettingsWindow(this, p.Settings).showDialog())
            {
                if (new EnvironmentWindow(this, p.Environment, p.Settings.RoomSimulation).showDialog())
                {
                    setTitle("SpeakerSim (" + Project.currentVersion() + ")");
                    
                    file = null;
                    project = p;
                    load();
                    refresh();
                    
                    Amplifier amp = new Amplifier();
                    if (editItem(amp))
                    {
                        addItem(amp, (DefaultMutableTreeNode) tree.getModel().getRoot());
                    }
                }
                else
                {
                    Environment.setInstance(project.Environment);
                }
            }
            else
            {
                Project.setInstance(project);
            }
        }
    }//GEN-LAST:event_menuFileNewActionPerformed

    private void menuSettingsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuSettingsActionPerformed
    {//GEN-HEADEREND:event_menuSettingsActionPerformed
        if (new SettingsWindow(this, project.Settings).showDialog())
        {
            createTables();
            refresh();
        }
    }//GEN-LAST:event_menuSettingsActionPerformed
    
    private void menuSimulatorActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuSimulatorActionPerformed
    {//GEN-HEADEREND:event_menuSimulatorActionPerformed
        changeSimulator();
    }//GEN-LAST:event_menuSimulatorActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JPanel listeningPanel;
    private javax.swing.JFormattedTextField listeningPosXField;
    private javax.swing.JFormattedTextField listeningPosYField;
    private javax.swing.JFormattedTextField listeningPosZField;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu menuEnclosure;
    private javax.swing.JMenuItem menuEnvironment;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem menuFileNew;
    private javax.swing.JMenuItem menuFileOpen;
    private javax.swing.JMenuItem menuFileQuit;
    private javax.swing.JMenuItem menuFileSave;
    private javax.swing.JMenuItem menuFileSaveAs;
    private javax.swing.JMenu menuProject;
    private javax.swing.JMenuItem menuSettings;
    private javax.swing.JRadioButtonMenuItem menuSimulatorAperiodic;
    private javax.swing.JRadioButtonMenuItem menuSimulatorBassReflex;
    private javax.swing.JRadioButtonMenuItem menuSimulatorClosedBox;
    private javax.swing.JRadioButtonMenuItem menuSimulatorOpenBaffle;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JScrollPane propertiesScrollPane;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JSplitPane split;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JTree tree;
    private javax.swing.JScrollPane treeScrollPane;
    // End of variables declaration//GEN-END:variables
}
