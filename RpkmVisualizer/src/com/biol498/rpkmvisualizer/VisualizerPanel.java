package com.biol498.rpkmvisualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import com.clcbio.api.base.util.NoRemovalIterator;
import com.clcbio.api.free.datatypes.bioinformatics.sequence.Sequence;

public class VisualizerPanel extends JComponent {
    private static final long serialVersionUID = -2830785835576165007L;
    private Sequence seq;
    private ArrayList<ArrayList<Integer>> randomData;
    private int length;
    private int maxMagnitude;
    private static final int margin = 30;
    private int diameter;
    private double zoom;
    
    public VisualizerPanel(Sequence s) {
        seq = s;
        //this.setLayout(null);
        
        diameter = 300;
        zoom = 1.5d;
        randomData = new ArrayList<ArrayList<Integer>>();
        int spot = 0;
        Random r = new Random();

        ArrayList<Integer> a;
        maxMagnitude = 0;
        for(int i = 0; i < 100; i++) {
        	a = new ArrayList<Integer>(2);
        	a.add(spot); // Start
        	spot += r.nextInt(1000);
        	a.add(spot); // End
        	spot += 1;
        	a.add((int)(100 * Math.pow(r.nextDouble(), 3))); // Amount
        	if(a.get(2) > maxMagnitude)
        		maxMagnitude = a.get(2);
        	a.add(r.nextInt(100000));
        	randomData.add(a);
        }
        length = spot + 50;

        this.setPreferredSize(new Dimension(getWholeWidth(),getWholeHeight()));
    }
    
    private int getWholeWidth() {
    	return getFigureWidth() + (2 * getTextColumnWidth()); // Add a margin between text columns and figure
    }
    
    private int getWholeHeight() {
    	return getFigureWidth() + (2 * getTextColumnWidth()); // Add a margin between text columns and figure
    }
    
    private int getTextColumnWidth() {
    	return 100; // Find max label width
    }

    private int getFigureWidth() {
    	return (int)((2*margin) + ((diameter + maxMagnitude) * zoom));
    }
    
    public void paint(Graphics graphics) {
        super.paint(graphics);
        Graphics2D g = (Graphics2D)graphics;
        int zoomdiam = (int)(diameter * zoom);
        int centerx = getTextColumnWidth() + getFigureWidth()/2;
        int centery = getWholeHeight()/2;
        int left = centerx - (zoomdiam/2);
        int top = centery - (zoomdiam/2);
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        Color c = g.getColor();

        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(c);
        g.drawOval(left, top, zoomdiam, zoomdiam);
        g.fillOval(left, top, zoomdiam, zoomdiam);

        double startDeg;
        double endDeg;
        double magnitude;
        for (int i = 0; i < randomData.size(); i++) {
        	startDeg = 360 * randomData.get(i).get(0) / length;
        	endDeg = 360 * randomData.get(i).get(1) / length;
        	
        	magnitude = randomData.get(i).get(2) * zoom;
        	
        	g.fill(
        			new Arc2D.Double(
        					left - magnitude/2,
        					top  - magnitude/2,
        					zoomdiam + magnitude,
        					zoomdiam + magnitude,
        					startDeg,
        					endDeg - startDeg,
        					Arc2D.PIE));
        }
        g.setColor(Color.white);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        g.fillOval(left, top, zoomdiam, zoomdiam);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(c);
        g.drawOval(left, top, zoomdiam, zoomdiam);
        
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D bounds;
        int rightcolumn_right = margin + 100;
        int rightcolumn_top = margin;
        int pos = rightcolumn_top;
        for (int i = 0; i < randomData.size(); i++) {
        	bounds = fm.getStringBounds(randomData.get(i).get(3).toString(), g);
        	g.drawString(
        			randomData.get(i).get(3).toString(),
        			(int)(rightcolumn_right - bounds.getWidth()),
        			(int)(pos + bounds.getHeight()));
        	pos = pos + (int)bounds.getHeight();
        }
    }
}
