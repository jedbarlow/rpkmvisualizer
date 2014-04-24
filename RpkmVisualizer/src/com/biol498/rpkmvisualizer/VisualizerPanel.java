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
    private int length;
    private int maxRpkm;
    private int maxMagnitude;
    private static final int margin = 30;
    private int diameter;
    private double zoom;
    private List<RpkmRegion> rpkmRegions;
    private int maxLabelWidth;

    public VisualizerPanel(List<RpkmRegion> regions) {
        //seq = s;
        //this.setLayout(null);

        rpkmRegions = regions;

        diameter = 300;
        zoom = 1.5d;

        maxRpkm = -1;
        maxMagnitude = 50;
        maxLabelWidth = 0;
        length = 0;

        Graphics g = getGraphics();
        FontMetrics fm = g.getFontMetrics();
        for(int i = 0; i < regions.size(); i++) {
            if((int)regions.get(i).getRpkm() > maxRpkm)
                maxRpkm = (int)regions.get(i).getRpkm();
            if(regions.get(i).getEnd() > length)
                length = regions.get(i).getEnd();
            if((int)fm.getStringBounds(rpkmRegions.get(i).getName(), g).getWidth() > maxLabelWidth)
                maxLabelWidth = (int)fm.getStringBounds(rpkmRegions.get(i).getName(), g).getWidth();
        }

        this.setPreferredSize(new Dimension(getWholeWidth(),getWholeHeight()));
    }

    private Double rpkmToMagnitude(Double rpkm) {
        return rpkm / maxRpkm * 50;
    }

    private int getWholeWidth() {
        return (2 * margin) + getFigureWidth() + (2 * getTextColumnWidth()); // Add a margin between text columns and figure
    }

    private int getWholeHeight() {
        return (2 * margin) + getFigureWidth() + (2 * getTextColumnWidth()); // Add a margin between text columns and figure
    }

    private int getTextColumnWidth() {
        return maxLabelWidth; // Find max label width
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
        for (int i = 0; i < rpkmRegions.size(); i++) {
            startDeg = 360 * rpkmRegions.get(i).getStart() / length;
            endDeg = 360 * rpkmRegions.get(i).getEnd() / length;

            magnitude = rpkmToMagnitude(rpkmRegions.get(i).getRpkm()) * zoom;

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
        int rightcolumn_left = margin + getTextColumnWidth() + getFigureWidth();
        int column_top = margin;
        int leftcolumn_right = margin + getTextColumnWidth();
        int pos = column_top;
        int i = 0;
        for (; i < rpkmRegions.size(); i++) {
            if ((rpkmRegions.get(i).getStart() + rpkmRegions.get(i).getEnd())/2 > length/2)
                break;
            bounds = fm.getStringBounds(rpkmRegions.get(i).getName(), g);
            g.drawString(
                    rpkmRegions.get(i).getName(),
                    rightcolumn_left,
                    (int)(pos + bounds.getHeight()));
            pos = pos + (int)bounds.getHeight();
        }
        pos = column_top;
        for (; i < rpkmRegions.size(); i++) {
            bounds = fm.getStringBounds(rpkmRegions.get(i).getName(), g);
            g.drawString(
                    rpkmRegions.get(i).getName(),
                    (int)(leftcolumn_right - bounds.getWidth()),
                    (int)(pos + bounds.getHeight()));
            pos = pos + (int)bounds.getHeight();
        }
    }
}
