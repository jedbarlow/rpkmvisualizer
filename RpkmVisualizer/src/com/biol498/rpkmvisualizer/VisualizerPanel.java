package com.biol498.rpkmvisualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
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

    private class RegionLabel {
        public int x;
        public int y;
        public int width;
        public int height;
        public int anchorSide;
        public int regionIndex;

        public static final int LEFT = 0;
        public static final int RIGHT = 1;

        public Point getAnchor() {
            if (anchorSide == LEFT)
                return new Point(x, y - height/2);
            else
                return new Point(x + width, y - height/2);
        }

        public RegionLabel(int rIndex, int anchor, Graphics g) {
            regionIndex = rIndex;
            anchorSide = anchor;

            FontMetrics fm = g.getFontMetrics();
            Rectangle2D bounds = fm.getStringBounds(getText(), g);
            width = (int)bounds.getWidth() + 2;
            height = (int)bounds.getHeight();
        }

        public String getText() {
            return rpkmRegions.get(regionIndex).getName();
        }

        public void anchorAt(int atx, int aty) {
            if (anchorSide == LEFT)
                x = atx;
            else
                x = atx - width;
            y = aty + height/2;
        }

        public void draw(Graphics g) {
            if (anchorSide == LEFT)
                g.drawString(getText(), x + 2, y);
            else
                g.drawString(getText(), x - 2, y);
        }

        public Rectangle2D getRect() {
            return new Rectangle2D.Double(x, y, width, height);
        }
    }

    private Sequence seq;
    private int length;
    private int maxRpkm;
    private int maxMagnitude;
    private static final int margin = 30;
    private int diameter;
    private double zoom;
    private List<RpkmRegion> rpkmRegions;
    private int maxLabelWidth;
    private int columnHeight;
    private boolean displayLines;

    private List<RegionLabel> leftColumn;
    private List<RegionLabel> rightColumn;
    private RegionLabel highlighted;

    private boolean needResize;

    public VisualizerPanel(List<RpkmRegion> regions) {
        rpkmRegions = regions;

        diameter = 300;
        zoom = 1.5d;
        displayLines = false;

        highlighted = null;

        needResize = true;
        // These have to be set when Graphics is available for font measuring
        maxLabelWidth = -1;
        columnHeight = -1;
        leftColumn = new ArrayList<RegionLabel>();
        rightColumn = new ArrayList<RegionLabel>();

        maxRpkm = 0;
        maxMagnitude = 50;
        length = 0;

        for(int i = 0; i < rpkmRegions.size(); i++) {
            if((int)rpkmRegions.get(i).getRpkm() > maxRpkm)
                maxRpkm = (int)rpkmRegions.get(i).getRpkm();
            if(rpkmRegions.get(i).getEnd() > length)
                length = rpkmRegions.get(i).getEnd();
        }

        this.setPreferredSize(new Dimension(getWholeWidth(),getWholeHeight()));

        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                RegionLabel over = null;

                for (int i = 0; i < leftColumn.size(); i++) {
                    if (leftColumn.get(i).getRect().contains(e.getPoint()))
                        over = leftColumn.get(i);
                }
                for (int i = 0; i < rightColumn.size(); i++) {
                    if (rightColumn.get(i).getRect().contains(e.getPoint()))
                        over = rightColumn.get(i);
                }

                if (over != highlighted) {
                    highlighted = over;
                    repaint();
                }
            }
        });
    }

    private Double rpkmToMagnitude(Double rpkm) {
        return rpkm / maxRpkm * 50;
    }

    private int getWholeWidth() {
        return (4 * margin) + getFigureWidth() + getMaxLabelWidth(leftColumn) + getMaxLabelWidth(rightColumn); // Add a margin between text columns and figure
    }

    private int getWholeHeight() {
        return (2 * margin) + Math.max(getFigureWidth(), columnHeight); // Add a margin between text columns and figure
    }

    private int getFigureWidth() {
        return (int)((diameter + maxMagnitude) * zoom);
    }

    private int minColumnHeight(List<RegionLabel> column, Graphics g) {
        int labelHeight = (int)g.getFontMetrics().getStringBounds("A", g).getHeight();
        return labelHeight * column.size();
    }

    private void distributeLabelsVertically(List<RegionLabel> column, int x, int upperBound, int lowerBound, Graphics g) {
        int labelHeight = getLabelHeight(g);

        int top = upperBound + labelHeight/2;
        int bottom = lowerBound - labelHeight/2;

        for (int i = 0; i < column.size(); i++) {
            column.get(i).anchorAt(
                    x,
                    (int)(top + ((bottom - top) * (((double)i)/column.size()))));
        }
    }

    private int getLabelHeight(Graphics g) {
        return (int)g.getFontMetrics().getStringBounds("A", g).getHeight();
    }

    private int getMaxLabelWidth(List<RegionLabel> column) {
        int maxWidth = 0;
        for (int i = 0; i < column.size(); i++)
            if (column.get(i).width > maxWidth)
                maxWidth = column.get(i).width;
        return maxWidth;
    }

    private void layoutEverything(Graphics g) {
        leftColumn.clear();
        rightColumn.clear();

        for (int i = 0; i < rpkmRegions.size(); i++) {
            if ((rpkmRegions.get(i).getStart() + rpkmRegions.get(i).getEnd())/2 < length/2)
                rightColumn.add(
                        new RegionLabel(i, RegionLabel.LEFT, g));
            else
                leftColumn.add(0,
                        new RegionLabel(i, RegionLabel.RIGHT, g));
        }

        columnHeight = Math.max(
                getFigureWidth(),
                Math.max(
                        minColumnHeight(leftColumn, g),
                        minColumnHeight(rightColumn, g)));

        distributeLabelsVertically(
                leftColumn,
                margin + getMaxLabelWidth(leftColumn),
                margin,
                margin + columnHeight,
                g);
        distributeLabelsVertically(
                rightColumn,
                margin + getMaxLabelWidth(leftColumn) + margin + getFigureWidth() + margin,
                margin,
                margin + columnHeight,
                g);

        this.setPreferredSize(new Dimension(getWholeWidth(),getWholeHeight()));
        this.revalidate();
        needResize = false;
    }

    public void paint(Graphics graphics) {
        super.paint(graphics);

        Graphics2D g = (Graphics2D)graphics;
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D bounds;

        if (needResize)
            layoutEverything(g);

        int zoomdiam = (int)(diameter * zoom);
        int centerx = margin + getMaxLabelWidth(leftColumn) + margin + getFigureWidth()/2;
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

        drawColumnAndLines(leftColumn, centerx, centery, g);
        drawColumnAndLines(rightColumn, centerx, centery, g);

        g.setColor(Color.white);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        g.fillOval(left, top, zoomdiam, zoomdiam);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(c);
        g.drawOval(left, top, zoomdiam, zoomdiam);
    }

    private void drawColumnAndLines(List<RegionLabel> column, int centerx, int centery, Graphics2D g) {
        double startDeg;
        double endDeg;
        for (int i = 0; i < column.size(); i++) {
            column.get(i).draw(g);

            if (displayLines || column.get(i) == highlighted) {
                startDeg = 360 * rpkmRegions.get(column.get(i).regionIndex).getStart() / length;
                endDeg = 360 * rpkmRegions.get(column.get(i).regionIndex).getEnd() / length;

                g.draw(
                        new Line2D.Double(
                                centerx,
                                centery,
                                centerx + Math.cos((Math.PI/180) * (90 - (startDeg + (endDeg - startDeg)/2.0d))) * ((diameter + maxMagnitude)/2.0d) * zoom,
                                centery - Math.sin((Math.PI/180) * (90 - (startDeg + (endDeg - startDeg)/2.0d))) * ((diameter + maxMagnitude)/2.0d) * zoom));
                g.draw(
                        new Line2D.Double(
                                centerx + Math.cos((Math.PI/180) * (90 - (startDeg + (endDeg - startDeg)/2.0d))) * ((diameter + maxMagnitude)/2.0d) * zoom,
                                centery - Math.sin((Math.PI/180) * (90 - (startDeg + (endDeg - startDeg)/2.0d))) * ((diameter + maxMagnitude)/2.0d) * zoom,
                                column.get(i).getAnchor().x,
                                column.get(i).getAnchor().y));
            }
        }
    }

    public void setZoom(double z) {
        zoom = z;
        needResize = true;
        repaint();
    }

    public void setDisplayLines(boolean dl) {
        displayLines = dl;
        repaint();
    }
}
