package com.biol498.rpkmvisualizer;

import javax.swing.*;

import com.clcbio.api.free.editors.framework.sidepanel.SidePanelView;
import com.clcbio.api.free.editors.framework.sidepanel.SidePanelModel;
import com.clcbio.api.free.gui.StandardLayout;

public class RpkmVisualizerView extends SidePanelView {
    private StandardLayout panel = null;

    public RpkmVisualizerView(final RpkmVisualizerModel model) {
        super(model);
        createUI();
    }

    private void createUI() {
        if (panel == null) {
            panel = new StandardLayout();
        }
    }
    
    public JComponent getComponent() {
        createUI();
        return panel;
    }

    @Override
    public void updateUI(SidePanelModel model) {
        createUI();
        // Called whenever the model changes
    }
}