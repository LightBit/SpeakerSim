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

import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.*;
import java.text.NumberFormat;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;

public final class Graph
{
    private final LogAxis xAxis;
    private final NumberAxis yAxis;
    private final XYSeriesCollection series;
    private final XYPlot plot;
    private final ChartPanel chartPanel;
    private final CrosshairOverlay crosshairOverlay;
    private final Crosshair xCrosshair;
    private final Crosshair yCrosshair;
    private final List<Crosshair> yCrosshairs;
    private int colorIndex;
    
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
    
    private class GraphCrosshair extends Crosshair
    {
        public GraphCrosshair()
        {
            super(Double.NaN, Color.BLACK, new BasicStroke(1));
            setLabelVisible(true);
            setLabelBackgroundPaint(Color.BLACK);
            setLabelPaint(Color.WHITE);
            setLabelGenerator((Crosshair crosshair) -> String.format(" %.2f ", crosshair.getValue()));
        }
    }
    
    private final static Stroke solidStroke = new BasicStroke(1);
    
    private final static Stroke dashedStroke = new BasicStroke(
        1,
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL,
        0,
        new float[]{10},
        0
    );
    
    private final static Stroke dottedStroke = new BasicStroke(
        1,
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL,
        0,
        new float[]{1, 5},
        0
    );
    
    private static Color generateColor(int n)
    {
        return Color.getHSBColor((n * 0.17f) % 1f, 1f, 0.8f);
    }
    
    public Graph(String x, String y)
    {
        xAxis = new LogAxis(x);
        xAxis.setNumberFormatOverride(NumberFormat.getInstance());
        xAxis.setBase(10);
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        xAxis.setLowerMargin(0);
        xAxis.setUpperMargin(0);
    
        yAxis = new NumberAxis(y);
        
        series = new XYSeriesCollection();
        xCrosshair = new GraphCrosshair();
        xCrosshair.setStroke(dashedStroke);
        yCrosshair = new GraphCrosshair();
        yCrosshair.setStroke(dashedStroke);
        yCrosshairs = new ArrayList<>();
        colorIndex = 0;
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setDefaultShapesVisible(false);
        
        plot = new XYPlot();
        plot.setRenderer(renderer);
        
        for (int i = 1, step = 1; i < 100000; step *= 10)
        {
            for (int j = 1; j < 10; j++)
            {
                plot.addDomainMarker(new ValueMarker(i));
                i += step;
            }
        }
        
        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);
        plot.setDataset(series);
        
        JFreeChart chart = new JFreeChart(null, plot);
        chartPanel = new ChartPanel(chart, false, true, true, false, true);
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
                Rectangle2D dataArea = chartPanel.getScreenDataArea();
                
                double x = plot.getDomainAxis().java2DToValue(
                    event.getTrigger().getX(),
                    dataArea,
                    RectangleEdge.BOTTOM
                );
                xCrosshair.setValue(x);
                
                double y = plot.getRangeAxis().java2DToValue(
                    event.getTrigger().getY(),
                    dataArea,
                    RectangleEdge.LEFT
                );
                yCrosshair.setValue(y);
                
                XYItemRenderer renderer = plot.getRenderer();
                XYDataset d = plot.getDataset();
                
                int i = 0;
                for (Crosshair crosshair : yCrosshairs)
                {
                    crosshair.setValue(DatasetUtils.findYValue(d, i, x));
                    crosshair.setLabelBackgroundPaint(renderer.getSeriesPaint(i));
                    i++;
                }
            }
            
            @Override
            public void chartMouseClicked(ChartMouseEvent event)
            {
            }
        });
        
        chartPanel.addMouseListener(new MouseListener()
        {
            @Override
            public void mouseExited(MouseEvent e)
            {
                xCrosshair.setValue(Double.NaN);
                yCrosshair.setValue(Double.NaN);
                
                for (Crosshair crosshair : yCrosshairs)
                {
                    crosshair.setValue(Double.NaN);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e)
            {
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
            }
        });

        crosshairOverlay = new CrosshairOverlay();
        crosshairOverlay.addDomainCrosshair(xCrosshair);
        crosshairOverlay.addRangeCrosshair(yCrosshair);
        chartPanel.addOverlay(crosshairOverlay);
        chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }
    
    public void clear(boolean all)
    {
        for (Crosshair crosshair : yCrosshairs)
        {
            crosshairOverlay.removeRangeCrosshair(crosshair);
        }
        yCrosshairs.clear();
        
        plot.clearRangeMarkers();
        plot.clearDomainMarkers();
        
        for (int i = 1, step = 1; i < 100000; step *= 10)
        {
            for (int j = 1; j < 10; j++)
            {
                plot.addDomainMarker(new ValueMarker(i));
                i += step;
            }
        }
        
        if (all)
        {
            series.removeAllSeries();
        }
        else
        {
            XYItemRenderer renderer = plot.getRenderer();
        
            for (int i = 0; i < series.getSeriesCount() - colorIndex; )
            {
                series.removeSeries(i);
            }
            
            for (int i = 0; i < series.getSeriesCount(); i++)
            {
                renderer.setSeriesVisibleInLegend(i, false);
                renderer.setSeriesStroke(i, dottedStroke);
            }
        }
        colorIndex = 0;
    }
    
    public void clear()
    {
        clear(true);
    }
    
    private void addCrosshair()
    {
        Crosshair crosshair = new GraphCrosshair();
        crosshairOverlay.addRangeCrosshair(crosshair);
        yCrosshairs.add(crosshair);
    }
    
    public void add(String title, double[] x, double[] y)
    {
        int index = series.getSeriesCount();
        XYSeries s = new XYSeries(new IdString(title, index));
        
        for (int i = 0; i < x.length; i++)
        {
            if (Double.isFinite(x[i]) && Double.isFinite(y[i]))
            {
                s.add(x[i], y[i]);
            }
        }
        
        series.addSeries(s);
        addCrosshair();
        
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesVisibleInLegend(index, true);
        renderer.setSeriesStroke(index, solidStroke);
        renderer.setSeriesPaint(index, generateColor(colorIndex++));
    }
    
    public void addSeries(String title)
    {
        int index = series.getSeriesCount();
        XYSeries s = new XYSeries(new IdString(title, index));
        series.addSeries(s);
        addCrosshair();
        
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesVisibleInLegend(index, true);
        renderer.setSeriesStroke(index, solidStroke);
        renderer.setSeriesPaint(index, generateColor(colorIndex++));
    }
    
    public void add(int series, double x, double y)
    {
        XYSeries s = this.series.getSeries(series);
        
        if (Double.isFinite(x) && Double.isFinite(y))
        {
            s.add(x, y);
        }
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
    
    public void setXRange(double min, double max)
    {
        xAxis.setRange(min, max);
    }
    
    public void setYRange(double min, double max)
    {
        yAxis.setRange(min, max);
    }
    
    public Component getPanel()
    {
        return chartPanel;
    }
}
