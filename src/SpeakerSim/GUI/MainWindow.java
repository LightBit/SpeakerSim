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
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private double[] filter;
    private double[] listeningWindow;
    private double[] power;
    private double[] directivity;
    private double[] maxSPL;
    private double[] maxPower;
    private double[] excursion;
    private double[] groupDelay;
    private double[] baffle;
    private double[] room;
    private double[][] responses;
    private double[][] phases;
    private double[][] filters;
    private double[][] excursions;
    
    private JPanel enclosurePanel;
    private BafflePanel bafflePanel;
    private DriverPositionPanel driverPositionPanel;
    private Thread worker;
    
    final Graph graphResponse;
    final Graph graphPhase;
    final Graph graphFilters;
    final Graph graphExcursion;
    final Graph graphDirectivity;
    final Graph graphMaxSPL;
    final Graph graphMaxPower;
    final Graph graphGroupDelay;
    final Graph graphBaffle;
    final Graph graphRoom;
    final Graph graphImpedance;
    
    private void createTables()
    {
        if (freq == null
            || freq.length != Settings.getInstance().Points - 1
            || freq[0] != Settings.getInstance().StartFrequency
            || freq[freq.length - 1] != Settings.getInstance().EndFrequency
        )
        {
            freq = new double[Settings.getInstance().Points - 1];
            response = new double[freq.length];
            responsePhase = new double[freq.length];
            impedance = new double[freq.length];
            impedancePhase = new double[freq.length];
            filter = new double[freq.length];
            listeningWindow = new double[freq.length];
            power = new double[freq.length];
            directivity = new double[freq.length];
            maxSPL = new double[freq.length];
            maxPower = new double[freq.length];
            excursion = new double[freq.length];
            groupDelay = new double[freq.length];
            baffle = new double[freq.length];
            room = new double[freq.length];

            double f = Settings.getInstance().StartFrequency;
            double m = Settings.getInstance().multiplier();
            for (int i = 0; i < freq.length; i++)
            {
                freq[i] = f;
                f *= m;
            }
        }
    }
    
    private void setRanges()
    {
        graphResponse.setXRange(Settings.getInstance().StartFrequency, Settings.getInstance().EndFrequency);
        graphPhase.setXRange(Settings.getInstance().StartFrequency, Settings.getInstance().EndFrequency);
        graphFilters.setXRange(Settings.getInstance().StartFrequency, Settings.getInstance().EndFrequency);
        graphExcursion.setXRange(Settings.getInstance().StartFrequency, Settings.getInstance().EndFrequency);
        graphDirectivity.setXRange(Settings.getInstance().StartFrequency, Settings.getInstance().EndFrequency);
        graphMaxSPL.setXRange(Settings.getInstance().StartFrequency, Settings.getInstance().EndFrequency);
        graphMaxPower.setXRange(Settings.getInstance().StartFrequency, Settings.getInstance().EndFrequency);
        graphGroupDelay.setXRange(Settings.getInstance().StartFrequency, Settings.getInstance().EndFrequency);
        graphBaffle.setXRange(Settings.getInstance().StartFrequency, Settings.getInstance().EndFrequency);
        graphRoom.setXRange(Settings.getInstance().StartFrequency, Settings.getInstance().EndFrequency);
        graphImpedance.setXRange(Settings.getInstance().StartFrequency, Settings.getInstance().EndFrequency);
        
        graphResponse.setYRange(Settings.getInstance().MinSPL, Settings.getInstance().MaxSPL);
        graphPhase.setYRange(-180, 180);
        graphFilters.setYRange(-30, 10);
        graphExcursion.setYRange(0, Settings.getInstance().MaxExcursion);
        graphDirectivity.setYRange(0, Settings.getInstance().MaxSPL);
        graphMaxSPL.setYRange(0, Settings.getInstance().MaxSPL + 30);
        graphMaxPower.setYRange(0, Settings.getInstance().MaxPower);
        graphBaffle.setYRange(-10, 10);
        graphRoom.setYRange(-10, 20);
        graphImpedance.setYRange(0, Settings.getInstance().MaxImpedance);
    }
    
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

        messageLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        messageLabel.setBackground(UIManager.getColor("inactiveCaptionBorder"));
        messageLabel.setVisible(false);
        
        graphResponse = new Graph("Hz", "dB");
        graphPhase = new Graph("Hz", "");
        graphFilters = new Graph("Hz", "dB");
        graphExcursion = new Graph("Hz", "mm");
        graphDirectivity = new Graph("Hz", "dB");
        graphMaxSPL = new Graph("Hz", "dB");
        graphMaxPower = new Graph("Hz", "W");
        graphGroupDelay = new Graph("Hz", "ms");
        graphBaffle = new Graph("Hz", "dB");
        graphRoom = new Graph("Hz", "dB");
        graphImpedance = new Graph("Hz", "Ω");
        
        tabs.addTab("SPL at 2.83V", graphResponse.getPanel());
        tabs.addTab("Directivity", graphDirectivity.getPanel());
        tabs.addTab("Phase", graphPhase.getPanel());
        tabs.addTab("Filters", graphFilters.getPanel());
        tabs.addTab("Max SPL", graphMaxSPL.getPanel());
        tabs.addTab("Max power", graphMaxPower.getPanel());
        tabs.addTab("Excursion", graphExcursion.getPanel());
        tabs.addTab("Group delay", graphGroupDelay.getPanel());
        
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
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)newPath.getLastPathComponent();
                    if (node != null)
                    {
                        showNode(node);
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
                Project.getInstance().ListeningPosition.X = UI.getDouble(e) / 100;
                refresh();
            }
        });
        
        listeningPosYField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                Project.getInstance().ListeningPosition.Y = UI.getDouble(e) / 100;
                refresh();
            }
        });
        
        listeningPosZField.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
                Project.getInstance().ListeningPosition.Z = UI.getDouble(e) / 100;
                refresh();
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
                                            UI.openURL("https://lightbit.gitlab.io/file/");
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
        
        mi = new JMenuItem("Custom impedance");
        mi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addingItem(new CustomImpedance(), above, node);
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
            final IItem item = (IItem) node.getUserObject();
            
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
                
                JMenu statusMenu = new JMenu("Status");
                    
                final JRadioButtonMenuItem connected = new JRadioButtonMenuItem("Connected");
                final JRadioButtonMenuItem bypassed = new JRadioButtonMenuItem("Bypassed");
                final JRadioButtonMenuItem disconnected = new JRadioButtonMenuItem("Disconnected");

                ButtonGroup statusGroup = new ButtonGroup();
                statusGroup.add(connected);
                statusGroup.add(bypassed);
                statusGroup.add(disconnected);

                switch (item.getStatus())
                {
                    case CONNECTED:
                        connected.setSelected(true);
                        break;
                    case BYPASSED:
                        bypassed.setSelected(true);
                        break;
                    case DISCONNECTED:
                        disconnected.setSelected(true);
                        break;
                    default:
                        break;
                }

                ActionListener changeStatus = new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {

                        if (connected.isSelected())
                        {
                            item.setStatus(Item.Status.CONNECTED);
                        }
                        else if (bypassed.isSelected())
                        {
                            item.setStatus(Item.Status.BYPASSED);
                        }
                        else if (disconnected.isSelected())
                        {
                            item.setStatus(Item.Status.DISCONNECTED);
                        }

                        refresh();
                    }
                };

                connected.addActionListener(changeStatus);
                bypassed.addActionListener(changeStatus);
                disconnected.addActionListener(changeStatus);

                statusMenu.add(connected);
                statusMenu.add(bypassed);
                statusMenu.add(disconnected);

                popup.add(statusMenu);
                
                if ((item instanceof Speaker))
                {
                    final Speaker speaker = (Speaker) item;
                    JMenu enclosureMenu = new JMenu("Enclosure");
                    
                    if (speaker.Driver.Closed)
                    {
                        enclosureMenu.setEnabled(false);
                    }
                    else
                    {
                        final JRadioButtonMenuItem bassReflex = new JRadioButtonMenuItem("Bass Reflex");
                        final JRadioButtonMenuItem closedBox = new JRadioButtonMenuItem("Closed Box");
                        final JRadioButtonMenuItem aperiodic = new JRadioButtonMenuItem("Aperiodic");
                        final JRadioButtonMenuItem openBaffle = new JRadioButtonMenuItem("Open Baffle");

                        ButtonGroup enclosureGroup = new ButtonGroup();
                        enclosureGroup.add(bassReflex);
                        enclosureGroup.add(closedBox);
                        enclosureGroup.add(aperiodic);
                        enclosureGroup.add(openBaffle);

                        switch (speaker.Simulator)
                        {
                            case BASS_REFLEX:
                                bassReflex.setSelected(true);
                                break;
                            case CLOSED_BOX:
                                closedBox.setSelected(true);
                                break;
                            case APERIODIC:
                                aperiodic.setSelected(true);
                                break;
                            case OPEN_BAFFLE:
                                openBaffle.setSelected(true);
                                break;
                            default:
                                break;
                        }

                        ActionListener changeEnclosure = new ActionListener()
                        {
                            @Override
                            public void actionPerformed(ActionEvent e)
                            {
                                
                                if (bassReflex.isSelected())
                                {
                                    speaker.setSimulator(Speaker.SimulatorType.BASS_REFLEX);
                                }
                                else if (closedBox.isSelected())
                                {
                                    speaker.setSimulator(Speaker.SimulatorType.CLOSED_BOX);
                                }
                                else if (aperiodic.isSelected())
                                {
                                    speaker.setSimulator(Speaker.SimulatorType.APERIODIC);
                                }
                                else if (openBaffle.isSelected())
                                {
                                    speaker.setSimulator(Speaker.SimulatorType.OPEN_BAFFLE);
                                }

                                refresh();
                            }
                        };

                        bassReflex.addActionListener(changeEnclosure);
                        closedBox.addActionListener(changeEnclosure);
                        aperiodic.addActionListener(changeEnclosure);
                        openBaffle.addActionListener(changeEnclosure);

                        enclosureMenu.add(bassReflex);
                        enclosureMenu.add(closedBox);
                        enclosureMenu.add(aperiodic);
                        enclosureMenu.add(openBaffle);
                    }
                    
                    popup.add(enclosureMenu);
                }
                
                popup.addSeparator();
                
                if (!(item instanceof Amplifier))
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
            final NotchFilter filter = (NotchFilter) item;
            
            final JFormattedTextField l = UI.decimalField(0);
            l.setValue(filter.L * 1000);
            
            final JFormattedTextField c = UI.decimalField(0);
            c.setValue(filter.C * 1000000);
            
            final JFormattedTextField r = UI.decimalField(0);
            r.setValue(filter.R);
            
            Picture picture = null;
            try
            {
                picture = new Picture(getClass().getClassLoader().getResourceAsStream(filter.getClass().getSimpleName() + ".png"));
            }
            catch (IOException ex)
            {
                
            }
            
            JButton calc = new JButton();
            calc.setText("Calculate");
            calc.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    JFormattedTextField f1 = UI.decimalField(1);
                    JFormattedTextField f2 = UI.decimalField(1);

                    if (filter instanceof ParallelNotchFilter)
                    {
                        final JComponent[] inputs = new JComponent[]
                        {
                            new JLabel("Start frequency (Hz): "), f1,
                            new JLabel("End frequency (Hz): "), f2
                        };
                        
                        if (UI.dialog(FocusManager.getCurrentManager().getActiveWindow(), "Calculate", inputs))
                        {
                            ParallelNotchFilter f = (ParallelNotchFilter) filter;
                            f = f.calculate(UI.getDouble(f1), UI.getDouble(f2));

                            l.setValue(f.L * 1000);
                            c.setValue(f.C * 1000000);
                            r.setValue(f.R);
                        }
                    }
                    else if (filter instanceof SerialNotchFilter)
                    {
                        JFormattedTextField z = UI.decimalField(0);
                        
                        final JComponent[] inputs = new JComponent[]
                        {
                            new JLabel("Start frequency (Hz): "), f1,
                            new JLabel("End frequency (Hz): "), f2,
                            new JLabel("Impedance (Ω): "), z
                        };
                        
                        if (UI.dialog(FocusManager.getCurrentManager().getActiveWindow(), "Calculate", inputs))
                        {
                            SerialNotchFilter f = (SerialNotchFilter) filter;
                            f = f.calculate(UI.getDouble(f1), UI.getDouble(f2), UI.getDouble(z));

                            l.setValue(f.L * 1000);
                            c.setValue(f.C * 1000000);
                            r.setValue(f.R);
                        }
                    }
                }
            });
            
            final JComponent[] inputs = new JComponent[]
            {
                picture == null ? new JPanel() : picture,
                calc,
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
            
            final JFormattedTextField attenuation = UI.decimalField(0);
            attenuation.setEnabled(Z != 0);
            
            final JFormattedTextField s = UI.decimalField(0);
            s.setValue(filter.Rs);
            
            final JFormattedTextField p = UI.decimalField(0);
            
            attenuation.addPropertyChangeListener("value", new PropertyChangeListener()
            {
                @Override
                public void propertyChange(PropertyChangeEvent e)
                {
                    if (listen)
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
                    if (listen)
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
                new JLabel("Attenuation (dB): "), attenuation,
                picture == null ? new JPanel() : picture,
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
            final double R = speakers.size() == 1 ? speakers.get(0).Driver.effectiveRe() : 0;
            final double Le = speakers.size() == 1 ? speakers.get(0).Driver.effectiveLe() : 0;
            
            final JButton calc = new JButton("Calculate");
            calc.setEnabled(R != 0 && Le != 0);
            
            final JFormattedTextField c = UI.decimalField(0);
            c.setValue(filter.C * 1000000);
            
            final JFormattedTextField r = UI.decimalField(0);
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
            JFormattedTextField value = UI.decimalField(0);
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
            JFormattedTextField value = UI.decimalField(0);
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
            JFormattedTextField value = UI.decimalField(0);
            value.setValue(inductor.L * 1000);
            final JComponent[] inputs = new JComponent[] { new JLabel("Inductance (mH): "), value };
            
            if (UI.dialog(this, "Inductor", inputs))
            {
                inductor.L = UI.getDouble(value) / 1000;
                return true;
            }
        }
        else if (item instanceof CustomImpedance)
        {
            final CustomImpedance customZ = (CustomImpedance) item;
            ResponseEntry[] cZMA = customZ.ZMA;
            
            if (customZ.ZMA == null)
            {
                try
                {
                    FileSelector fs = new FileSelector(".zma");
                    fs.setFileFilter(new FileNameExtensionFilter("Impedance Data", "zma", "txt", "csv"));

                    if (fs.showOpenDialog(this) == FileSelector.APPROVE_OPTION)
                    {
                        customZ.importZMA(fs.getSelectedFile());
                        return true;
                    }
                }
                catch (Exception ex)
                {
                    UI.throwable(this, ex);
                }
                return false;
            }
            
            JButton importButton = new JButton("Import ZMA");
            importButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Window w = SwingUtilities.getWindowAncestor((JButton) e.getSource());
                    
                    try
                    {
                        FileSelector fs = new FileSelector(".zma");
                        fs.setFileFilter(new FileNameExtensionFilter("Impedance Data", "zma", "txt", "csv"));

                        if (fs.showOpenDialog(w) == FileSelector.APPROVE_OPTION)
                        {
                            customZ.importZMA(fs.getSelectedFile());
                            w.setVisible(false);
                        }
                    }
                    catch (Exception ex)
                    {
                        UI.throwable(w, ex);
                    }
                }
            });
            
            JButton exportButton = new JButton("Export ZMA");
            exportButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Window w = SwingUtilities.getWindowAncestor((JButton) e.getSource());
                    try
                    {
                        FileSelector fs = new FileSelector(".zma");
                        fs.setFileFilter(new FileNameExtensionFilter("Impedance Data", "zma"));

                        if (fs.showSaveDialog(w) == FileSelector.APPROVE_OPTION)
                        {
                            customZ.exportZMA(fs.getSelectedFile());
                            w.setVisible(false);
                        }
                    }
                    catch (Exception ex)
                    {
                        UI.throwable(w, ex);
                    }
                }
            });
            
            final JComponent[] inputs = new JComponent[] { importButton, exportButton };
            String[] opts = {"Close"};
            JOptionPane.showOptionDialog(this, inputs, "Custom impedance", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
            
            return cZMA != customZ.ZMA;
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
                
                IItem item = Item.createItem(JSON.open(fs.getSelectedFile()));
                
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
                JSON.save(part.toJSON(), fs.getSelectedFile());
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
            bafflePanel.setVisible(Settings.getInstance().BaffleSimulation);
            
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
                            ((ISpeakerPanel) enclosurePanel).removeTabs(tabs);
                        }
                        enclosurePanel = null;
                        menuSimulatorOpenBaffle.setSelected(true);
                        break;
                }
            }
            else if (enclosurePanel != null)
            {
                propertiesPanel.remove(enclosurePanel);
                ((ISpeakerPanel) enclosurePanel).removeTabs(tabs);
                enclosurePanel = null;
            }
        }
        else
        {
            menuEnclosure.setEnabled(false);
            
            if (enclosurePanel != null)
            {
                propertiesPanel.remove(enclosurePanel);
                ((ISpeakerPanel) enclosurePanel).removeTabs(tabs);
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
        Complex baffleResponse = Settings.getInstance().BaffleSimulation ? item.responseWithBaffle(f).divide(r) : new Complex(1);
        Complex roomResponse = Settings.getInstance().RoomSimulation ? item.responseWithRoom(f).divide(r) : new Complex(1);
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
        // show message
        messageLabel.setVisible(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // stop worker, if running
        stopWorker();
        
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

        // load item data
        if (bafflePanel != null)
        {
            bafflePanel.show(((Speaker) item).Baffle);
        }

        if (driverPositionPanel != null)
        {
            driverPositionPanel.show(((Speaker) item).Position);
        }

        if (enclosurePanel != null)
        {
            ((ISpeakerPanel) enclosurePanel).show((Speaker) item);
        }
        
        // required to remove panels
        propertiesPanel.revalidate();
        propertiesPanel.repaint();
        
        final Component parent = this;
        
        worker = new Thread()
        {
            @Override
            public void run()
            {
                final Thread thread = Thread.currentThread();
                
                try
                {
                    final List<IItem> subitems = item.getChildren();
                    
                    if (!speakers.isEmpty())
                    {
                        responses = new double[subitems.size()][freq.length];
                        phases = new double[subitems.size()][freq.length];
                        filters = new double[subitems.size()][freq.length];
                        excursions = new double[subitems.size()][freq.length];

                        for (int i = 0; i < freq.length; i++)
                        {
                            if (thread.isInterrupted()) return;

                            double f = freq[i];

                            Complex r = item.response(f);
                            Complex baffleResponse = Settings.getInstance().BaffleSimulation ? item.responseWithBaffle(f).divide(r) : new Complex(1);
                            Complex roomResponse = Settings.getInstance().RoomSimulation ? item.responseWithRoom(f).divide(r) : new Complex(1);
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
                        
                        double delay = Fnc.min(Fnc.smooth(groupDelay, (int)Math.round(Settings.getInstance().pointsPerOctave()))) / 1000;
                        
                        // smoothing
                        if (Settings.getInstance().Smoothing > 0)
                        {
                            int points = (int)Math.round(Settings.getInstance().pointsPerOctave()) / Settings.getInstance().Smoothing;

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
                        
                        // remove distance delay from phase to get minimum phase
                        for (int i = 0; i < freq.length; i++)
                        {
                            Complex d = Complex.toComplex(1, 2 * Math.PI * freq[i] * delay);
                            responsePhase[i] = Math.toDegrees(Complex.toComplex(1, responsePhase[i]).multiply(d).phase());
                        }
                        
                        // get minimum phase of subitems
                        for (int j = 0; j < subitems.size(); j++)
                        {
                            for (int i = 0; i < freq.length; i++)
                            {
                                Complex d = Complex.toComplex(1, 2 * Math.PI * freq[i] * delay);
                                phases[j][i] = Math.toDegrees(Complex.toComplex(1, phases[j][i]).multiply(d).phase());
                            }
                        }
                        
                        if (enclosurePanel != null)
                        {
                            ((ISpeakerPanel) enclosurePanel).simulate();
                        }
                    }
                    else
                    {
                        responses = null;
                        phases = null;
                        filters = null;
                        excursions = null;
                        
                        for (int i = 0; i < freq.length; i++)
                        {
                            if (thread.isInterrupted()) return;

                            Complex z = item.impedance(freq[i]);
                            impedance[i] = z.abs();
                            impedancePhase[i] = z.phase();
                        }
                    }
                    
                    SwingUtilities.invokeAndWait(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (thread.isInterrupted()) return;
                            
                            // clear graphs
                            graphResponse.clear();
                            graphPhase.clear();
                            graphFilters.clear();
                            graphExcursion.clear();
                            graphDirectivity.clear();
                            graphMaxSPL.clear();
                            graphMaxPower.clear();
                            graphGroupDelay.clear();
                            graphBaffle.clear();
                            graphRoom.clear();
                            graphImpedance.clear();
                            
                            graphFilters.addYMark(0, "");
                            graphPhase.addYMark(0, "");
                            graphBaffle.addYMark(0, "");
                            graphRoom.addYMark(0, "");
                            
                            // Baffle tab
                            int tab = tabs.indexOfComponent(graphBaffle.getPanel());
                            if (tab < 0 && Settings.getInstance().BaffleSimulation)
                            {
                                tabs.addTab("Baffle", graphBaffle.getPanel());
                            }
                            else if (tab >= 0 && !Settings.getInstance().BaffleSimulation)
                            {
                                tabs.remove(tab);
                            }

                            // Room tab
                            tab = tabs.indexOfComponent(graphRoom.getPanel());
                            if (tab < 0 && Settings.getInstance().RoomSimulation)
                            {
                                tabs.addTab("Room", graphRoom.getPanel());
                            }
                            else if (tab >= 0 && !Settings.getInstance().RoomSimulation)
                            {
                                tabs.remove(tab);
                            }

                            // Impedance tab
                            tab = tabs.indexOfComponent(graphImpedance.getPanel());
                            if (tab < 0 && !(item instanceof Project))
                            {
                                tabs.addTab("Impedance", graphImpedance.getPanel());
                            }
                            else if (tab >= 0 && (item instanceof Project))
                            {
                                tabs.remove(tab);
                            }

                            // Enclosure tab
                            if (enclosurePanel != null)
                            {
                                ((ISpeakerPanel) enclosurePanel).addTabs(tabs);
                            }
                            
                            if (!speakers.isEmpty())
                            {
                                // add new data
                                graphResponse.add(item.toString(), freq, response);
                                graphFilters.add(item.toString(), freq, filter);
                                graphExcursion.add("Excursion", freq, excursion);

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
                                        graphPhase.add(subitem, freq, phases[j]);
                                    }
                                }

                                graphDirectivity.add("Listening window", freq, listeningWindow);
                                graphDirectivity.add("Power response", freq, power);
                                graphDirectivity.add("Directivity", freq, directivity);

                                graphMaxSPL.add("Max SPL", freq, maxSPL);
                                graphMaxPower.add("Max power", freq, maxPower);
                                graphGroupDelay.add("Group delay", freq, groupDelay);
                                graphBaffle.add("Baffle", freq, baffle);
                                graphRoom.add("Room", freq, room);
                                graphImpedance.add("Impedance", freq, impedance);

                                for (Speaker s : speakers)
                                {
                                    graphExcursion.addYMark(s.Driver.Xmax * 1000, s.Driver.Name);
                                }
                            }
                            else
                            {
                                // add only impedance
                                graphImpedance.add("Impedance", freq, impedance);
                            }
                            
                            // hide message
                            messageLabel.setVisible(false);
                            setCursor(null);
                        }
                    });
                }
                catch (final InterruptedException e)
                {
                    // ignore
                }
                catch (final Throwable e)
                {
                    if (!thread.isInterrupted())
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
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        if (node != null)
        {
            showNode(node);
        }
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
        setRanges();
        
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
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        split = new javax.swing.JSplitPane();
        treeScrollPane = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        rightPanel = new javax.swing.JPanel();
        propertiesScrollPane = new javax.swing.JScrollPane();
        propertiesPanel = new javax.swing.JPanel();
        listeningPanel = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        listeningPosXField = UI.decimalField(0);
        listeningPosYField = UI.decimalField(0);
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        listeningPosZField = UI.decimalField(0);
        layeredPane = new javax.swing.JLayeredPane();
        tabs = new javax.swing.JTabbedPane();
        messageLabel = new javax.swing.JLabel();
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
        menuSimulatorBassReflex = new javax.swing.JRadioButtonMenuItem();
        menuSimulatorClosedBox = new javax.swing.JRadioButtonMenuItem();
        menuSimulatorAperiodic = new javax.swing.JRadioButtonMenuItem();
        menuSimulatorOpenBaffle = new javax.swing.JRadioButtonMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        split.setDividerLocation(300);

        treeScrollPane.setMinimumSize(new java.awt.Dimension(0, 22));

        tree.setCellRenderer(new ItemTreeCellRenderer());
        tree.setToggleClickCount(0);
        treeScrollPane.setViewportView(tree);

        split.setLeftComponent(treeScrollPane);

        rightPanel.setLayout(new java.awt.BorderLayout());

        propertiesScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        propertiesScrollPane.setPreferredSize(new java.awt.Dimension(220, 220));

        propertiesPanel.setMinimumSize(new java.awt.Dimension(400, 220));
        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
        flowLayout1.setAlignOnBaseline(true);
        propertiesPanel.setLayout(flowLayout1);

        listeningPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Listening position"));
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

        listeningPosXField.setMinimumSize(new java.awt.Dimension(140, 19));
        listeningPosXField.setPreferredSize(new java.awt.Dimension(140, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        listeningPanel.add(listeningPosXField, gridBagConstraints);

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

        listeningPosZField.setMinimumSize(new java.awt.Dimension(140, 19));
        listeningPosZField.setPreferredSize(new java.awt.Dimension(140, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        listeningPanel.add(listeningPosZField, gridBagConstraints);

        propertiesPanel.add(listeningPanel);

        propertiesScrollPane.setViewportView(propertiesPanel);

        rightPanel.add(propertiesScrollPane, java.awt.BorderLayout.PAGE_START);

        layeredPane.setLayout(new javax.swing.OverlayLayout(layeredPane));
        layeredPane.add(tabs);

        messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        messageLabel.setText("Simulating ...");
        messageLabel.setAlignmentX(0.5F);
        messageLabel.setOpaque(true);
        layeredPane.setLayer(messageLabel, javax.swing.JLayeredPane.MODAL_LAYER);
        layeredPane.add(messageLabel);

        rightPanel.add(layeredPane, java.awt.BorderLayout.CENTER);

        split.setRightComponent(rightPanel);

        getContentPane().add(split, java.awt.BorderLayout.CENTER);

        menuFile.setText("File");

        menuFileNew.setText("New");
        menuFileNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFileNewActionPerformed(evt);
            }
        });
        menuFile.add(menuFileNew);

        menuFileOpen.setText("Open");
        menuFileOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFileOpenActionPerformed(evt);
            }
        });
        menuFile.add(menuFileOpen);

        menuFileSave.setText("Save");
        menuFileSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFileSaveActionPerformed(evt);
            }
        });
        menuFile.add(menuFileSave);

        menuFileSaveAs.setText("Save As");
        menuFileSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFileSaveAsActionPerformed(evt);
            }
        });
        menuFile.add(menuFileSaveAs);

        menuFileQuit.setText("Quit");
        menuFileQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFileQuitActionPerformed(evt);
            }
        });
        menuFile.add(menuFileQuit);

        menuBar.add(menuFile);

        menuProject.setText("Project");

        menuSettings.setText("Settings");
        menuSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSettingsActionPerformed(evt);
            }
        });
        menuProject.add(menuSettings);

        menuEnvironment.setText("Environment");
        menuEnvironment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEnvironmentActionPerformed(evt);
            }
        });
        menuProject.add(menuEnvironment);

        menuBar.add(menuProject);

        menuEnclosure.setText("Enclosure");
        menuEnclosure.setEnabled(false);

        menuSimulatorBassReflex.setText("Bass Reflex");
        menuSimulatorBassReflex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSimulatorActionPerformed(evt);
            }
        });
        menuEnclosure.add(menuSimulatorBassReflex);

        menuSimulatorClosedBox.setText("Closed Box");
        menuSimulatorClosedBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSimulatorActionPerformed(evt);
            }
        });
        menuEnclosure.add(menuSimulatorClosedBox);

        menuSimulatorAperiodic.setText("Aperiodic");
        menuSimulatorAperiodic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSimulatorActionPerformed(evt);
            }
        });
        menuEnclosure.add(menuSimulatorAperiodic);

        menuSimulatorOpenBaffle.setText("Open Baffle");
        menuSimulatorOpenBaffle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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
    }//GEN-LAST:event_menuFileNewActionPerformed

    private void menuSettingsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuSettingsActionPerformed
    {//GEN-HEADEREND:event_menuSettingsActionPerformed
        if (new SettingsWindow(this, Project.getInstance().Settings).showDialog())
        {
            createTables();
            setRanges();
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
    private javax.swing.JLayeredPane layeredPane;
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
    private javax.swing.JLabel messageLabel;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JScrollPane propertiesScrollPane;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JSplitPane split;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JTree tree;
    private javax.swing.JScrollPane treeScrollPane;
    // End of variables declaration//GEN-END:variables
}
