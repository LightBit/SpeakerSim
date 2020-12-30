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
import java.awt.geom.*;
import java.text.NumberFormat;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CrosshairLabelGenerator;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
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
    private ChartPanel chartPanel;
    private Crosshair xCrosshair;
    private List<Crosshair> yCrosshair;
    
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
            setLabelBackgroundPaint(Color.YELLOW);
            setLabelGenerator(new CrosshairLabelGenerator()
            {
                @Override
                public String generateLabel(Crosshair crosshair)
                {
                    return String.format(" %.2f ", crosshair.getValue());
                }
            });
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
        xCrosshair = new GraphCrosshair();
        yCrosshair = new ArrayList<Crosshair>();
        
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
        yCrosshair.add(new GraphCrosshair());
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
            if (Double.isFinite(x[i]) && Double.isFinite(y[i]))
            {
                s.add(x[i], y[i]);
            }
        }
        
        series.addSeries(s);
        yCrosshair.add(new GraphCrosshair());
    }
    
    public void add(double y, double x)
    {
        if (Double.isFinite(y) && Double.isFinite(x))
        {
            if (series.getSeriesCount() < 1)
            {
                series.addSeries(new XYSeries(new IdString("", 0)));
            }

            series.getSeries(0).add(x, y);
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
        
        return new JFreeChart(null, plot);
    }
    
    public Component getGraph()
    {
        chartPanel = new ChartPanel(getChart(), false, true, true, false, true);
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
                JFreeChart chart = event.getChart();
                XYPlot plot = (XYPlot) chart.getPlot();
                double x = plot.getDomainAxis().java2DToValue(event.getTrigger().getX(), dataArea, RectangleEdge.BOTTOM);
                xCrosshair.setValue(x);
                
                XYDataset d = plot.getDataset();
                int i = 0;
                for (Crosshair crosshair : yCrosshair)
                {
                    double y = DatasetUtils.findYValue(d, i, x);
                    crosshair.setValue(y);
                    i++;
                }
            }
            
            @Override
            public void chartMouseClicked(ChartMouseEvent event)
            {
            }
        });
        
        CrosshairOverlay crosshairOverlay = new CrosshairOverlay();
        crosshairOverlay.addDomainCrosshair(xCrosshair);
        for (Crosshair crosshair : yCrosshair)
        {
            crosshairOverlay.addRangeCrosshair(crosshair);
        }
        chartPanel.addOverlay(crosshairOverlay);
        
        return chartPanel;
    }
}
