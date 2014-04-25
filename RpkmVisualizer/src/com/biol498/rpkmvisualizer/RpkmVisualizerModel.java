package com.biol498.rpkmvisualizer;

import com.clcbio.api.base.util.State;
import com.clcbio.api.free.editors.framework.sidepanel.SidePanelModel;
import com.clcbio.api.free.workbench.WorkbenchManager;

public class RpkmVisualizerModel extends SidePanelModel {
    private double zoom = 1.5d;

    public RpkmVisualizerModel(WorkbenchManager manager) {
        super("RPKM Visualizer");
    }

    @Override
    public String getId() {
        return "SidePanelModel";
    }

    @Override
    protected void loadModel(State model) {
    }

    @Override
    protected State saveModel() {
        State model = new State();
        return model;
    }

    public void setToFactory() {
    }

    public void setZoom(double z) {
        zoom = z;
        fireModelChanged();
    }

    public double getZoom() {
        return zoom;
    }
}
