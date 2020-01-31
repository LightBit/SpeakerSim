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
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.NumberFormat;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;

public final class Graph
{
    private final LogAxis xAxis;
    private final NumberAxis yAxis;
    private final XYSeriesCollection series;
    private final XYPlot plot;
    private ChartPanel chartPanel;
    private int mouseX;
    private int mouseY;
    private String value;
    
    private class IdString implements Comparable<IdString>
    {
        private final String string;
        private final int id;

        public IdString(String string, int id)
        {
            this.string = string;
            this.id = id;
        }

        @Override
        public String toString()
        {
            return string;
        }

        @Override
        public int compareTo(IdString x)
        {
            return Integer.compare(id, x.id);
        }
    }
    
    public Graph(String xAxis, String yAxis)
    {
        this.xAxis = new LogAxis(xAxis);
        this.xAxis.setNumberFormatOverride(NumberFormat.getInstance());
        this.xAxis.setBase(10);
        this.xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        this.xAxis.setLowerMargin(0);
        this.xAxis.setUpperMargin(0);
    
        this.yAxis = new NumberAxis(yAxis);
        
        series = new XYSeriesCollection();
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setDefaultShapesVisible(false);
        
        plot = new XYPlot();
        plot.setRenderer(renderer);
        
        for (int x = 1, step = 1; x < 100000; step *= 10)
        {
            for (int i = 1; i < 10; i++)
            {
                plot.addDomainMarker(new ValueMarker(x));
                x += step;
            }
        }
    }
    
    public Graph(String title, String xAxis, String yAxis)
    {
        this(xAxis, yAxis);
        
        series.addSeries(new XYSeries(new IdString(title, 0)));
    }
    
    public Graph(String title, String xAxis, double[] x, String yAxis, double[] y)
    {
        this(xAxis, yAxis);
        
        add(title, x, y);
    }
    
    public void add(String title, double[] x, double[] y)
    {
        XYSeries s = new XYSeries(new IdString(title, series.getSeriesCount()));
        
        for (int i = 0; i < x.length; i++)
        {
            s.add(x[i], y[i]);
        }
        
        series.addSeries(s);
    }
    
    public void add(double y, double x)
    {
        if (series.getSeriesCount() < 1)
        {
            series.addSeries(new XYSeries(new IdString("", 0)));
        }
        
        series.getSeries(0).add(x, y);
    }
    
    public void addXMark(double x, String label)
    {
        final Marker mark = new ValueMarker(x);
        mark.setPaint(Color.BLUE);
        mark.setLabel(label);
        mark.setLabelPaint(Color.BLUE);
        mark.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        mark.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        plot.addDomainMarker(mark);
    }
    
    public void addYMark(double y, String label)
    {
        final Marker mark = new ValueMarker(y);
        mark.setPaint(Color.BLUE);
        mark.setLabel(label);
        mark.setLabelPaint(Color.BLUE);
        mark.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        mark.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        plot.addRangeMarker(mark);
    }
    
    public double getMaxY()
    {
        double max = 0;
        
        for (int i = 0; i < series.getSeriesCount(); i++)
        {
            max = Math.max(max, series.getSeries(i).getMaxY());
        }
        
        return max;
    }
    
    public void setYRange(double min, double max)
    {
        yAxis.setRange(min, max);
    }
    
    public void setYRange(double range)
    {
        double maxY = getMaxY();
        setYRange(maxY - range, maxY + 1);
    }
    
    private JFreeChart getChart()
    {
        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);
        plot.setDataset(series);
        
        JFreeChart chart = new JFreeChart(null, plot);
        
        if (series.getSeriesCount() < 2)
        {
            chart.removeLegend();
        }
        
        return chart;
    }
    
    public Component getGraph()
    {
        chartPanel = new ChartPanel(getChart(), false, true, true, false, true)
        {
            @Override
            public void paintComponent(Graphics g)
            {
                super.paintComponent(g);

                if (value != null)
                {
                    ((Graphics2D) g).setRenderingHints(new RenderingHints
                    (
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                    ));
                    
                    int x = mouseX + 10;
                    int y = mouseY - 10;
                    
                    FontMetrics fm = g.getFontMetrics();
                    Rectangle2D rect = fm.getStringBounds(value, g);
                    int rectWidth = (int) Math.ceil(rect.getWidth());
                    int rectHeight = (int) Math.ceil(rect.getHeight());
                    
                    if (g.getClipBounds().width < x + rectWidth + 6)
                    {
                        x -= rectWidth + 20;
                    }
                    
                    if (0 > y - rectHeight - 6)
                    {
                        y += rectHeight + 30;
                    }
                    
                    g.setColor(Color.ORANGE);
                    g.drawRect(x - 3,
                               y - fm.getAscent() - 3,
                               rectWidth + 6,
                               rectHeight + 6);

                    g.setColor(Color.YELLOW);
                    g.fillRect(x - 2,
                               y - fm.getAscent() - 2,
                               rectWidth + 4,
                               rectHeight + 4);

                    g.setColor(Color.BLACK);
                    g.drawString(value, x, y);
                }
            }
        };
        chartPanel.setMinimumDrawWidth(0);
        chartPanel.setMinimumDrawHeight(0);
        chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);
        chartPanel.setMouseZoomable(false);
        chartPanel.addChartMouseListener(new ChartMouseListener()
        {
            @Override
            public void chartMouseMoved(ChartMouseEvent event)
            {
                mouseX = event.getTrigger().getX();
                mouseY = event.getTrigger().getY();
                Point2D p = chartPanel.translateScreenToJava2D(new Point(mouseX, mouseY));
                Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
                double xValue = xAxis.java2DToValue(p.getX(), dataArea, plot.getDomainAxisEdge());
                double yValue = yAxis.java2DToValue(p.getY(), dataArea, plot.getRangeAxisEdge());
                String v = null;
                
                if
                (
                    xValue < xAxis.getRange().getUpperBound() &&
                    xValue > xAxis.getRange().getLowerBound() &&
                    yValue < yAxis.getRange().getUpperBound() &&
                    yValue > yAxis.getRange().getLowerBound()
                )
                {
                    v = Fnc.twoDecimalFormat(yValue) + yAxis.getLabel() + " at " + Math.round(xValue) + xAxis.getLabel();
                }
                
                if (value != v)
                {
                    value = v;
                    chartPanel.repaint();
                }
            }
            
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {}
        });
        chartPanel.addMouseListener(new MouseListener()
        {
            @Override
            public void mouseExited(MouseEvent e)
            {
                if (value != null)
                {
                    value = null;
                    chartPanel.repaint();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseClicked(MouseEvent e) {}
        });
        
        return chartPanel;
    }
}
