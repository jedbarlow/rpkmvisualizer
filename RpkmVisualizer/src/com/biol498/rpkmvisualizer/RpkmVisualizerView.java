package com.biol498.rpkmvisualizer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clcbio.api.free.editors.framework.sidepanel.SidePanelView;
import com.clcbio.api.free.editors.framework.sidepanel.SidePanelModel;
import com.clcbio.api.free.gui.StandardLayout;

public class RpkmVisualizerView extends SidePanelView {
    private StandardLayout panel = null;
    private JSlider zoom;

    public RpkmVisualizerView(final RpkmVisualizerModel model) {
        super(model);
        createUI();
    }

    private void createUI() {
        if (panel == null) {
            zoom = new JSlider(0, 200);
            zoom.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    ((RpkmVisualizerModel) getModel()).setZoom(0.2 + (5.8 * (zoom.getValue()/200.0d)));
                }
            });
            panel = new StandardLayout();
            fillPanel();
        }
    }
    
    private void fillPanel() {
        createUI();
        panel.removeAll();
        panel.addComps(new JLabel("Zoom Level"), zoom);
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