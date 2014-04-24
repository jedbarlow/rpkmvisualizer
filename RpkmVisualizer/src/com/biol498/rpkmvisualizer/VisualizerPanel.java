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
import java.util.List;
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
    private List<RpkmRegion> rpkmRegions;
    
    public VisualizerPanel(List<RpkmRegion> regions) {
        //seq = s;
        //this.setLayout(null);
        
        diameter = 300;
        zoom = 1.5d;
        randomData = new ArrayList<ArrayList<Integer>>();
        int spot = 0;
        Random r = new Random();

        ArrayList<Integer> a;
        maxMagnitude = 0;
        for(int i = 0; i < regions.size(); i++) {
            if((int)regions.get(i).getRpkm() > maxMagnitude)
                maxMagnitude = (int)regions.get(i).getRpkm();
        }
        length = spot + 50;

        this.setPreferredSize(new Dimension(getWholeWidth(),getWholeHeight()));
    }
    
    private int getWholeWidth() {
        return (2 * margin) + getFigureWidth() + (2 * getTextColumnWidth()); // Add a margin between text columns and figure
    }
    
    private int getWholeHeight() {
        return (2 * margin) + getFigureWidth() + (2 * getTextColumnWidth()); // Add a margin between text columns and figure
    }
    
    private int getTextColumnWidth() {
        return 100; // Find max label width
    }

    private int getFigureWidth() {
        return (int)((diameter + maxMagnitude) * zoom);
    }
    
    public void paint(Graphics graphics) {
        super.paint(graphics);
        Graphics2D g = (Graphics2D)graphics;
        int zoomdiam = (int)(diameter * zoom);
        int centerx = getWholeWidth()/2;
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
                            90 - startDeg,
                            -(endDeg - startDeg),
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
        int rightcolumn_left = margin + 100 + getFigureWidth();
        int column_top = margin;
        int leftcolumn_right = margin + 100;
        int pos = column_top;
        int i = 0;
        for (; i < randomData.size(); i++) {
            if ((randomData.get(i).get(0) + randomData.get(i).get(1))/2 > length/2)
                break;
            bounds = fm.getStringBounds(randomData.get(i).get(3).toString(), g);
            g.drawString(
                    randomData.get(i).get(3).toString(),
                    rightcolumn_left,
                    (int)(pos + bounds.getHeight()));
            pos = pos + (int)bounds.getHeight();
        }
        pos = column_top;
        for (; i < randomData.size(); i++) {
            bounds = fm.getStringBounds(randomData.get(i).get(3).toString(), g);
            g.drawString(
                    randomData.get(i).get(3).toString(),
                    (int)(leftcolumn_right - bounds.getWidth()),
                    (int)(pos + bounds.getHeight()));
            pos = pos + (int)bounds.getHeight();
        }
    }
}
