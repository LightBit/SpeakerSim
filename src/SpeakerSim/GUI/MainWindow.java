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
import io.sentry.Sentry;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.*;

public final class MainWindow extends javax.swing.JFrame
{
    private File file;
    private final FileSelector fc;
    private boolean listen = true;
    
    public double[] freq;
    private double[] response;
    private double[] responsePhase;
    private double[] impedance;
    private double[] impedancePhase;
    private Component enclosurePanel;
    private Component bafflePanel;
    private Component driverPositionPanel;
    private Thread worker;
    
    public MainWindow(String arg) throws IOException
    {
        URL iconURL = getClass().getClassLoader().getResource("SpeakerSim.png");
        setIconImage(new ImageIcon(iconURL).getImage());

        if (arg != null)
        {
            file = new File(arg);
            Project.setInstance(new Project(file));
            
            projectVersionCheck();
            
            setTitle("SpeakerSim - " + file.getName() + " (" + Project.currentVersionString() + ")");
        }
        else
        {
            setTitle("SpeakerSim (" + Project.currentVersionString() + ")");
            
            Project.setInstance(new Project(getClass().getClassLoader().getResourceAsStream("default.ssim")));
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
                    showNode((DefaultMutableTreeNode) newPath.getLastPathComponent());
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
                else if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
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
                if (UI.validate(e, 0, Environment.getInstance().RoomX * 100))
                {
                    Project.getInstance().ListeningPosition.X = UI.getDouble(e) / 100;
                    refresh();
                }
            }
        });
        
        listeningPosYField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0, Environment.getInstance().RoomY * 100))
                {
                    Project.getInstance().ListeningPosition.Y = UI.getDouble(e) / 100;
                    refresh();
                }
            }
        });
        
        listeningPosZField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                if (UI.validate(e, 0, Environment.getInstance().RoomZ * 100))
                {
                    Project.getInstance().ListeningPosition.Z = UI.getDouble(e) / 100;
                    refresh();
                }
            }
        });
        
        newerVersionCheck();
    }
    
    private void projectVersionCheck()
    {
        if (Project.getInstance().Version.compareTo(Project.currentVersion()) > 0)
        {
            UI.warning(this, "Project file was created with newer version. It may not work correctly!");
        }
    }
    
    private static void newerVersionCheck()
    {
        (new Thread()
        {
            @Override
            public void run()
            {
                try (InputStream in = new URL("https://lightbit.gitlab.io/file/speakersim-version").openStream())
                {
                    try (InputStreamReader reader = new InputStreamReader(in, "UTF-8"))
                    {
                        char[] buf = new char[100];
                        int c = reader.read(buf, 0, 100);
                        String version = new String(buf, 0, c).trim();
                        final String prefix = "SpeakerSim version ";
                        
                        if (version.startsWith(prefix))
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date latest = sdf.parse(version.replaceFirst(prefix, ""));
                            
                            if (latest.compareTo(Project.currentVersion()) > 0)
                            {
                                SwingUtilities.invokeLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if (UI.options(null, "New version is available. It is recommended to update.", new String[]{"Visit web page", "Continue"}) == 0)
                                        {
                                            UI.openURL("https://gitlab.com/LightBit/SpeakerSim/releases");
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    Sentry.capture(ex);
                }
            }
        }).start();
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
                
                if (above)
                {
                    speaker.getChildren().add((IItem) node.getUserObject());
                }
                
                if (editItem(speaker))
                {
                    speaker.setSimulators();
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
                addingItem(new LowPassFilter(), above, node);
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
                addingItem(new HighPassFilter(), above, node);
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
                addingItem(new LPad(), above, node);
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
                addingItem(new Zobel(), above, node);
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
                addingItem(new ParallelNotchFilter(), above, node);
            }
        });
        menu.add(mi);

        mi = new JMenuItem("Serial notch filter");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addingItem(new SerialNotchFilter(), above, node);
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
                addingItem(new Resistor(), above, node);
            }
        });
        menu.add(mi);

        mi = new JMenuItem("Capacitor");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addingItem(new Capacitor(), above, node);
            }
        });
        menu.add(mi);

        mi = new JMenuItem("Inductor");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addingItem(new Inductor(), above, node);
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
                
                popup.addSeparator();
                
                if (!(node.getUserObject() instanceof Amplifier))
                {
                    JMenu addAboveMenu = new JMenu("Add above");
                    submenuAdd(addAboveMenu, node, true);
                    popup.add(addAboveMenu);
                }
                
                JMenu addBelowMenu = new JMenu("Add below");
                submenuAdd(addBelowMenu, node, false);
                popup.add(addBelowMenu);
                
                popup.addSeparator();
                
                mi = new JMenuItem("Remove");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        removeItem();
                    }
                });
                mi.setEnabled(canRemoveItem());
                popup.add(mi);
                
                mi = new JMenuItem("Delete subtree");
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
        
        Enumeration<?> e = node.depthFirstEnumeration();
        while (e.hasMoreElements())
        {
            Object x = ((DefaultMutableTreeNode)e.nextElement()).getUserObject();
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
        }
        else
        {
            model.insertNodeInto(node, target, target.getChildCount());
            
            targetItem.getChildren().add(item);
        }
        
        Project.getInstance().setModified();
        selectNode(node);
        showNode(node);
    }
    
    protected void addingItem(IItem item, boolean above, DefaultMutableTreeNode target)
    {
        if (above)
        {
            item.getChildren().add((IItem) target.getUserObject());
        }
        
        if (editItem(item))
        {
            addItem(item, above, target);
        }
    }
    
    protected void addItem(IItem item, DefaultMutableTreeNode target)
    {
        addItem(item, false, target);
    }
    
    private boolean editItem(IItem item)
    {
        if (item instanceof Speaker)
        {
            if (new DriverWindow(this, Project.getInstance(), ((Speaker) item).Driver).showDialog())
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
            final double Z = filter.Zmin();
            
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
                        
                        if (Z != 0)
                        {
                            attenuation.setValue(Math.abs(Fnc.toDecibels(z)));
                        }
                        
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
    
    protected boolean canRemoveItem()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Enumeration<?> children = node.children();
        while (children.hasMoreElements())
        {
            if (!canMoveLeftItem((DefaultMutableTreeNode) children.nextElement()))
            {
                return false;
            }
        }
        
        return true;
    }
    
    protected void removeItem(DefaultMutableTreeNode node)
    {
        Enumeration<?> children = node.children();
        while (children.hasMoreElements())
        {
            moveLeftItem((DefaultMutableTreeNode) children.nextElement());
        }
        
        deleteItem(node);
    }
    
    protected void removeItem()
    {
        removeItem((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
    }
    
    protected void deleteItem(DefaultMutableTreeNode node)
    {
        stopWorker();
        
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        
        ((IItem) parent.getUserObject()).getChildren().remove((IItem) node.getUserObject());
        
        model.removeNodeFromParent(node);
        selectNode(parent);
        Project.getInstance().setModified();
    }
    
    protected void deleteItem()
    {
        TreePath[] paths = tree.getSelectionPaths();
        for (TreePath path : paths)
        {
            deleteItem((DefaultMutableTreeNode) path.getLastPathComponent());
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
                stopWorker();
                
                IItem item = Item.constructItem(JSON.open(fs.getSelectedFile()));
                
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                
                ((IItem) parent.getUserObject()).getChildren().add(item);
                
                addItemChildren(parent, item);
                
                Project.getInstance().setModified();
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
                waitWorker();
                
                try (PrintWriter writer = new PrintWriter(fs.getSelectedFile(), "UTF-8"))
                {
                    for (int i = 0; i < freq.length; i++)
                    {
                        double a = response[i];
                        double p = responsePhase[i];
                        
                        if (Double.isFinite(a) && Double.isFinite(p))
                        {
                            writer.print(new ResponseEntry(freq[i], a, p).toString());
                        }
                    }
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
                waitWorker();
                
                try (PrintWriter writer = new PrintWriter(fs.getSelectedFile(), "UTF-8"))
                {
                    for (int i = 0; i < freq.length; i++)
                    {
                        double a = impedance[i];
                        double p = impedancePhase[i];
                        
                        if (Double.isFinite(a) && Double.isFinite(p))
                        {
                            writer.print(new ResponseEntry(freq[i], a, p).toString());
                        }
                    }
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
    
    protected boolean canMoveLeftItem(DefaultMutableTreeNode node)
    {
        Object item = node.getUserObject();
        Object target = ((DefaultMutableTreeNode) node.getParent()).getUserObject();
        
        return !(item instanceof Amplifier || target instanceof Amplifier);
    }
    
    protected boolean canMoveLeftItem()
    {
        return canMoveLeftItem((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
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
        stopWorker();
        
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
        Project.getInstance().setModified();
    }
    
    protected void moveDownItem()
    {
        stopWorker();
        
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
        Project.getInstance().setModified();
    }
    
    protected void moveLeftItem(DefaultMutableTreeNode node)
    {
        stopWorker();
        
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        
        DefaultMutableTreeNode target = (DefaultMutableTreeNode) parent.getParent();
        DefaultMutableTreeNode nextSibling = parent.getNextSibling();
        int pos = nextSibling == null ? parent.getSiblingCount() : target.getIndex(nextSibling);
        
        ((IItem) parent.getUserObject()).getChildren().remove((IItem) node.getUserObject());
        ((IItem) target.getUserObject()).getChildren().add(pos, (IItem) node.getUserObject());
        
        model.removeNodeFromParent(node);
        model.insertNodeInto(node, target, pos);
    }
    
    protected void moveLeftItem()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        moveLeftItem(node);
        selectNode(node);
        Project.getInstance().setModified();
    }
    
    protected void moveRightItem()
    {
        stopWorker();
        
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        
        DefaultMutableTreeNode target = node.getPreviousSibling();
        
        ((IItem) parent.getUserObject()).getChildren().remove((IItem) node.getUserObject());
        ((IItem) target.getUserObject()).getChildren().add((IItem) node.getUserObject());
        
        model.removeNodeFromParent(node);
        model.insertNodeInto(node, target, target.getChildCount());
        
        selectNode(node);
        Project.getInstance().setModified();
    }
    
    protected boolean canPasteItem()
    {
        Class<?> type = CopyPaste.getType();
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
            stopWorker();
            
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            ((IItem) parent.getUserObject()).getChildren().add(item);
            
            addItemChildren(parent, item);
            
            Project.getInstance().setModified();
        }
    }
    
    private void createTables()
    {
        freq = new double[Project.getInstance().Settings.Points - 1];
        response = new double[freq.length];
        responsePhase = new double[freq.length];
        impedance = new double[freq.length];
        impedancePhase = new double[freq.length];

        double f = Project.getInstance().Settings.StartFrequency;
        double m = Project.getInstance().Settings.multiplier();
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
                UI.setPanelEnabled((JPanel) bafflePanel, Project.getInstance().Settings.BaffleSimulation);
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
        Complex baffleResponse = Project.getInstance().Settings.BaffleSimulation ? item.responseWithBaffle(f).divide(r) : new Complex(1);
        Complex roomResponse = Project.getInstance().Settings.RoomSimulation ? item.responseWithRoom(f).divide(r) : new Complex(1);
        return r.multiply(baffleResponse).multiply(roomResponse);
    }
    
    private void stopWorker()
    {
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
    }
    
    private void waitWorker()
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
    }
    
    private void showNode(final DefaultMutableTreeNode node)
    {
        // calculate center of all speakers
        List<Speaker> speakers = getSpeakers(node);
        Project.getInstance().CenterPosition = new Position(0, 0, 0);

        for (Speaker speaker : speakers)
        {
            Project.getInstance().CenterPosition = Project.getInstance().CenterPosition.add(speaker.Position);
        }
        Project.getInstance().CenterPosition = Project.getInstance().CenterPosition.divide(speakers.size());

        // refresh item
        final IItem item = (IItem) node.getUserObject();
        item.refresh();
        
        // update properties panel
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
        stopWorker();
        
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
                    final List<IItem> subitems = item.getChildren();
                    final List<Speaker> speakers = getSpeakers(node);

                    if (!speakers.isEmpty())
                    {
                        double[] filter = new double[freq.length];
                        double[] listeningWindow = new double[freq.length];
                        double[] power = new double[freq.length];
                        double[] directivity = new double[freq.length];
                        double[] maxSPL = new double[freq.length];
                        double[] maxPower = new double[freq.length];
                        double[] excursion = new double[freq.length];
                        double[] groupDelay = new double[freq.length];
                        double[] baffle = new double[freq.length];
                        double[] room = new double[freq.length];
                        double[][] responses = new double[subitems.size()][freq.length];
                        double[][] phases = new double[subitems.size()][freq.length];
                        double[][] filters = new double[subitems.size()][freq.length];
                        double[][] excursions = new double[subitems.size()][freq.length];

                        for (int i = 0; i < freq.length; i++)
                        {
                            if (Thread.currentThread().isInterrupted()) return;

                            double f = freq[i];

                            Complex r = item.response(f);
                            Complex baffleResponse = Project.getInstance().Settings.BaffleSimulation ? item.responseWithBaffle(f).divide(r) : new Complex(1);
                            Complex roomResponse = Project.getInstance().Settings.RoomSimulation ? item.responseWithRoom(f).divide(r) : new Complex(1);
                            r = r.multiply(baffleResponse).multiply(roomResponse);
                            response[i] = Fnc.toDecibels(r.abs());
                            
                            for (int j = 0; j < subitems.size(); j++)
                            {
                                IItem subitem = subitems.get(j);
                                Complex sr = totalResponse(subitem, f);
                                responses[j][i] = Fnc.toDecibels(sr.abs());
                                phases[j][i] = sr.phase();
                                filters[j][i] = Fnc.toDecibels(subitem.filter(f).abs());
                                excursions[j][i] = subitem.excursion(f, Double.MAX_VALUE);
                            }

                            filter[i] = Fnc.toDecibels(item.filter(f).abs());
                            listeningWindow[i] = Fnc.toDecibels(item.listeningWindowResponse(f).abs());
                            power[i] = Fnc.toDecibels(item.powerResponse(f).abs());
                            directivity[i] = listeningWindow[i] - power[i];
                            responsePhase[i] = r.phase();
                            Complex z = item.impedance(f);
                            impedance[i] = z.abs();
                            impedancePhase[i] = z.phase();
                            maxPower[i] = item.maxPower(f);
                            maxSPL[i] = Fnc.toDecibels(item.response1W(f).multiply(baffleResponse).multiply(roomResponse).abs()) + Fnc.powerToDecibels(maxPower[i]);
                            excursion[i] = item.excursion(f, Double.MAX_VALUE);
                            groupDelay[i] = Math.abs((Math.abs(Math.toDegrees(totalResponse(item, f + 0.1).phase())) - Math.abs(Math.toDegrees(responsePhase[i]))) / (360 * 0.1)) * 1000;
                            baffle[i] = Fnc.toDecibels(baffleResponse.abs());
                            room[i] = Fnc.toDecibels(roomResponse.abs());
                        }
                        
                        double delay = Fnc.min(Fnc.smooth(groupDelay, (int)Math.round(Project.getInstance().Settings.pointsPerOctave()))) / 1000;
                        
                        if (Project.getInstance().Settings.Smoothing > 0)
                        {
                            int points = (int)Math.round(Project.getInstance().Settings.pointsPerOctave()) / Project.getInstance().Settings.Smoothing;

                            response = Fnc.smooth(response, points);
                            for (int j = 0; j < subitems.size(); j++)
                            {
                                responses[j] = Fnc.smooth(responses[j], points);
                                filters[j] = Fnc.smooth(filters[j], points);
                                excursions[j] = Fnc.smooth(excursions[j], points);
                                //phases[j] = Fnc.unwrapPhase(freq, phases[j]);
                                //phases[j] = Fnc.smooth(phases[j], points);
                            }
                            //responsePhase = Fnc.unwrapPhase(freq, responsePhase);
                            //responsePhase = Fnc.smooth(responsePhase, points);
                            filter = Fnc.smooth(filter, points);
                            listeningWindow = Fnc.smooth(listeningWindow, points);
                            power = Fnc.smooth(power, points);
                            directivity = Fnc.smooth(directivity, points);
                            maxSPL = Fnc.smooth(maxSPL, points);
                            maxPower = Fnc.smooth(maxPower, points);
                            groupDelay = Fnc.smooth(groupDelay, points);
                            baffle = Fnc.smooth(baffle, points);
                            room = Fnc.smooth(room, points);
                        }
                        
                        for (int i = 0; i < freq.length; i++)
                        {
                            Complex d = Complex.toComplex(1, 2 * Math.PI * freq[i] * delay);
                            responsePhase[i] = Math.toDegrees(Complex.toComplex(1, responsePhase[i]).multiply(d).phase());
                        }

                        final Graph graphResponse = new Graph(item.toString(), "Hz", freq, "dB", response);
                        final Graph graphPhase = new Graph("Hz", "");
                        final Graph graphFilters = new Graph(item.toString(), "Hz", freq, "dB", filter);
                        final Graph graphExcursion = new Graph("Excursion", "Hz", freq, "mm", excursion);
                        
                        if (subitems.size() < 1)
                        {
                            graphPhase.add("Phase", freq, responsePhase);
                        }
                        else
                        {
                            for (int j = 0; j < subitems.size(); j++)
                            {
                                String subitem = subitems.get(j).toString();

                                graphResponse.add(subitem, freq, responses[j]);
                                graphFilters.add(subitem, freq, filters[j]);
                                graphExcursion.add(subitem, freq, excursions[j]);

                                for (int i = 0; i < freq.length; i++)
                                {
                                    Complex d = Complex.toComplex(1, 2 * Math.PI * freq[i] * delay);
                                    phases[j][i] = Math.toDegrees(Complex.toComplex(1, phases[j][i]).multiply(d).phase());
                                }
                                graphPhase.add(subitem, freq, phases[j]);
                            }
                        }
                        
                        final Graph graphDirectivity = new Graph("Hz", "dB");
                        graphDirectivity.add("Listening window", freq, listeningWindow);
                        graphDirectivity.add("Power response", freq, power);
                        graphDirectivity.add("Directivity", freq, directivity);
                        
                        final Graph graphMaxSPL = new Graph("Maximal response", "Hz", freq, "dB", maxSPL);
                        final Graph graphMaxPower = new Graph("Maximal power", "Hz", freq, "W", maxPower);
                        final Graph graphGroupDelay = new Graph("Group delay", "Hz", freq, "ms", groupDelay);
                        final Graph graphBaffle = new Graph("Baffle", "Hz", freq, "dB", baffle);
                        final Graph graphRoom = new Graph("Room", "Hz", freq, "dB", room);
                        final Graph graphImpedance = new Graph("Impedance", "Hz", freq, "Ω", impedance);

                        graphResponse.setYRange(Project.getInstance().Settings.dBRange);
                        graphFilters.setYRange(Project.getInstance().Settings.dBRange);
                        graphDirectivity.setYRange(0, graphDirectivity.getMaxY() + 1);
                        graphMaxPower.setYRange(0, Math.min(Project.getInstance().Settings.MaxPower, graphMaxPower.getMaxY() + 1));
                        graphExcursion.setYRange(0, graphExcursion.getMaxY() + 1);
                        graphPhase.addYMark(0, "");
                        
                        for (Speaker s : speakers)
                        {
                            graphExcursion.addYMark(s.Driver.Xmax * 1000, s.Driver.Name);
                        }
                        
                        graphBaffle.setYRange(Project.getInstance().Settings.dBRange);
                        graphRoom.setYRange(Project.getInstance().Settings.dBRange);
                        graphImpedance.setYRange(0, Math.min(graphImpedance.getMaxY() + 1, Project.getInstance().Settings.MaxImpedance));
                        
                        if (enclosurePanel != null)
                        {
                            ((ISpeakerPanel) enclosurePanel).simulate();
                        }

                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                tabs.addTab("Response", graphResponse.getGraph());
                                tabs.addTab("Directivity", graphDirectivity.getGraph());
                                tabs.addTab("Phase", graphPhase.getGraph());
                                tabs.addTab("Filters", graphFilters.getGraph());
                                tabs.addTab("Maximal response", graphMaxSPL.getGraph());
                                tabs.addTab("Maximal power", graphMaxPower.getGraph());
                                tabs.addTab("Excursion", graphExcursion.getGraph());
                                tabs.addTab("Group delay", graphGroupDelay.getGraph());
                                
                                if (Project.getInstance().Settings.BaffleSimulation)
                                {
                                    tabs.addTab("Baffle", graphBaffle.getGraph());
                                }
                                
                                if (Project.getInstance().Settings.RoomSimulation)
                                {
                                    tabs.addTab("Room", graphRoom.getGraph());
                                }
                                
                                if (!(item instanceof Project))
                                {
                                    tabs.addTab("Impedance", graphImpedance.getGraph());
                                }

                                if (enclosurePanel != null)
                                {
                                    ((ISpeakerPanel) enclosurePanel).addGraphs(tabs);
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
                        graphImpedance.setYRange(0, Math.min(graphImpedance.getMaxY() + 1, Project.getInstance().Settings.MaxImpedance));

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
                    if (!Thread.currentThread().isInterrupted())
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
            }
        };

        worker.start();
    }
    
    public void refresh()
    {
        Project.getInstance().setModified();
        showNode((DefaultMutableTreeNode) tree.getLastSelectedPathComponent());
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
        
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(Project.getInstance());
        addChildren(rootNode, Project.getInstance().getChildren());
        tree.setModel(new DefaultTreeModel(rootNode));
        expandAllNodes(tree);
        tree.setSelectionPath(new TreePath(((DefaultMutableTreeNode) tree.getModel().getRoot()).getPath()));
        
        listeningPosXField.setValue(Project.getInstance().ListeningPosition.X * 100);
        listeningPosYField.setValue(Project.getInstance().ListeningPosition.Y * 100);
        listeningPosZField.setValue(Project.getInstance().ListeningPosition.Z * 100);
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
        if (new EnvironmentWindow(this, Environment.getInstance(), Project.getInstance().Settings.RoomSimulation).showDialog())
        {
            refresh();
        }
    }//GEN-LAST:event_menuEnvironmentActionPerformed
    
    private boolean request_close()
    {
        if (Project.getInstance() != null && Project.getInstance().isModified())
        {
            switch (UI.options(this, "Project has been modified.", new String[]{"Save", "Do not save", "Cancel"}))
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
                
                setTitle("SpeakerSim - " + file.getName() + " (" + Project.currentVersionString() + ")");
                
                Project.getInstance().save(file);
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
                Project.getInstance().save(file);
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
                    Project.setInstance(new Project(file));
                    
                    projectVersionCheck();
                    load();
                    
                    setTitle("SpeakerSim - " + file.getName() + " (" + Project.currentVersionString() + ")");
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
                    setTitle("SpeakerSim (" + Project.currentVersionString() + ")");
                    
                    file = null;
                    Project.setInstance(p);
                    load();
                    refresh();
                    
                    Amplifier amp = new Amplifier();
                    if (editItem(amp))
                    {
                        addItem(amp, (DefaultMutableTreeNode) tree.getModel().getRoot());
                    }
                }
            }
        }
    }//GEN-LAST:event_menuFileNewActionPerformed

    private void menuSettingsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuSettingsActionPerformed
    {//GEN-HEADEREND:event_menuSettingsActionPerformed
        if (new SettingsWindow(this, Project.getInstance().Settings).showDialog())
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
