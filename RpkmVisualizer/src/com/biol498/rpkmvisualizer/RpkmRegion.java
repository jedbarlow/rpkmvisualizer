package com.biol498.rpkmvisualizer;

public class RpkmRegion {
    private int start;
    private int end;
    private String name;
    private double rpkm;

    public RpkmRegion(int start, int end, String name, double rpkm) {
        this.start = start;
        this.end = end;
        this.name = name;
        this.rpkm = rpkm;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getName() {
        return name;
    }

    public double getRpkm() {
        return rpkm;
    }
}
