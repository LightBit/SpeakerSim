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

import java.awt.Color;
import java.awt.Component;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
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

public class Graph
{
    private final XYSeries series;
    private final LogAxis xAxis;
    private final NumberAxis yAxis;
    private final XYPlot plot;
    
    public Graph(String graphTitle, String xAxis, String yAxis)
    {
        this.xAxis = new LogAxis(xAxis);
        this.xAxis.setBase(10);
        this.xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        this.xAxis.setLowerMargin(0);
        this.xAxis.setUpperMargin(0);
    
        this.yAxis = new NumberAxis(yAxis);
        this.yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        series = new XYSeries(graphTitle);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(0, false);
        
        plot = new XYPlot();
        plot.setRenderer(renderer);
        
        plot.addDomainMarker(new ValueMarker(20));
        plot.addDomainMarker(new ValueMarker(30));
        plot.addDomainMarker(new ValueMarker(40));
        plot.addDomainMarker(new ValueMarker(50));
        plot.addDomainMarker(new ValueMarker(60));
        plot.addDomainMarker(new ValueMarker(70));
        plot.addDomainMarker(new ValueMarker(80));
        plot.addDomainMarker(new ValueMarker(90));
        plot.addDomainMarker(new ValueMarker(100));
        plot.addDomainMarker(new ValueMarker(200));
        plot.addDomainMarker(new ValueMarker(300));
        plot.addDomainMarker(new ValueMarker(400));
        plot.addDomainMarker(new ValueMarker(500));
        plot.addDomainMarker(new ValueMarker(600));
        plot.addDomainMarker(new ValueMarker(700));
        plot.addDomainMarker(new ValueMarker(800));
        plot.addDomainMarker(new ValueMarker(900));
        plot.addDomainMarker(new ValueMarker(1000));
        plot.addDomainMarker(new ValueMarker(2000));
        plot.addDomainMarker(new ValueMarker(3000));
        plot.addDomainMarker(new ValueMarker(4000));
        plot.addDomainMarker(new ValueMarker(5000));
        plot.addDomainMarker(new ValueMarker(6000));
        plot.addDomainMarker(new ValueMarker(7000));
        plot.addDomainMarker(new ValueMarker(8000));
        plot.addDomainMarker(new ValueMarker(9000));
        plot.addDomainMarker(new ValueMarker(10000));
    }
    
    public Graph(String graphTitle, String xAxis, double[] x, String yAxis, double[] y)
    {
        this(graphTitle, xAxis, yAxis);
        
        for (int i = 0; i < x.length; i++)
        {
            series.add(x[i], y[i]);
        }
    }
    
    public void add(double y, double x)
    {
        series.add(x, y);
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
        return series.getMaxY();
    }
    
    public void setYRange(double min, double max)
    {
        yAxis.setRange(min, max);
    }
    
    private JFreeChart getChart()
    {
        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);
        plot.setDataset(new XYSeriesCollection(series));
        
        JFreeChart chart = new JFreeChart(null, plot);
        chart.removeLegend();
        return chart;
    }
    
    public Component getGraph()
    {
        ChartPanel chartPanel = new ChartPanel(getChart(), false, true, true, false, true);
        chartPanel.setMinimumDrawWidth(0);
        chartPanel.setMinimumDrawHeight(0);
        chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);
        chartPanel.setMouseZoomable(false);
        
        return chartPanel;
    }
}
