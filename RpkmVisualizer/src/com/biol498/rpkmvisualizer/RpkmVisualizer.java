package com.biol498.rpkmvisualizer;


import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.clcbio.api.base.persistence.PersistenceException;
import com.clcbio.api.base.util.State;
import com.clcbio.api.free.datatypes.ClcObject;
import com.clcbio.api.free.datatypes.bioinformatics.gis.track.ExpressionTrackTableModel;
import com.clcbio.api.free.datatypes.bioinformatics.gis.track.Track;
import com.clcbio.api.free.datatypes.bioinformatics.sequence.Sequence;
import com.clcbio.api.free.datatypes.bioinformatics.sequence.list.SequenceList;
import com.clcbio.api.free.datatypes.bioinformatics.sequence.region.Region;
import com.clcbio.api.free.datatypes.framework.listener.ObjectEvent;
import com.clcbio.api.free.datatypes.framework.listener.ObjectListener;
import com.clcbio.api.free.datatypes.framework.listener.SelectionEvent;
import com.clcbio.api.free.editors.framework.AbstractEditor;
import com.clcbio.api.free.editors.framework.sidepanel.SidePanelListener;
import com.clcbio.api.free.editors.framework.sidepanel.SidePanelModel;
import com.clcbio.api.free.editors.framework.sidepanel.event.SidePanelEvent;
import com.clcbio.api.free.framework.workspace.Workspace;
import com.clcbio.api.free.gui.components.JTextAreaNotPasteable;
import com.clcbio.api.free.gui.focus.ClcFocusScrollPane;
import com.clcbio.api.free.gui.icon.ClcIcon;
import com.clcbio.api.free.gui.icon.SimpleClcIcon;
import com.clcbio.api.free.gui.icon.EmptyIcon;
import com.clcbio.api.free.workbench.WorkbenchManager;
import com.clcbio.datatypes.bioinformatics.gis.track.ExpressionTrackImpl;

public class RpkmVisualizer extends AbstractEditor {
    public final static String PLUGIN_GROUP = "free";
    private JTextArea textArea;
    private RpkmVisualizerModel model;
    private RpkmVisualizerView view;
    private VisualizerPanel visualizerpanel;
    private ClcFocusScrollPane scrollPane;
    private Sequence seq;
    private List<RpkmRegion> rpkmRegions;
    private ObjectListener sequenceListener;

    private Font font = new Font("Monospaced", Font.PLAIN, 12);
    
    public boolean canEdit(Class<?>[] types) {
        // TODO: Implement
        //for (Class<?> c : types)
         //   if (c != Sequence.class)
          //      return false;
        return true;
    }

    public void initEditorInstance(WorkbenchManager wm, ClcObject[] models, Workspace ws) {
        super.initEditorInstance(wm, models, ws);
        ExpressionTrackImpl eti = (ExpressionTrackImpl)models[0];
        ExpressionTrackTableModel tm = eti.getTableModel();
        rpkmRegions = new ArrayList<RpkmRegion>(tm.getRowCount());

        // Discover columns
        int name_column = -1;
        int region_column = -1;
        int rpkm_column = -1;
        for (int i = 0; i < tm.getColumnCount(); i++) {
            String cn = tm.getColumnName(i);
            if (cn.compareToIgnoreCase("name") == 0)
                name_column = i;
            else if (cn.compareToIgnoreCase("region") == 0)
                region_column = i;
            else if (cn.compareToIgnoreCase("rpkm") == 0)
                rpkm_column = i;
        }

        // TODO: Handle case of columns not found

        for (int i = 0; i < tm.getRowCount(); i++) {
            String name = (String) tm.getValueAt(i, name_column);

            String region = tm.getValueAt(i, region_column).toString();
            int substring_first = region.indexOf('(') != -1 ? region.indexOf('(') + 1 : 0;
            int substring_delim = region.indexOf("..");
            int substring_end = region.indexOf(')') != -1 ? region.indexOf(')') : region.length();

            int start = Integer.parseInt(region.substring(substring_first, substring_delim));
            int end = Integer.parseInt(region.substring(substring_delim + 2, substring_end));

            Double rpkm = (Double) tm.getValueAt(i, rpkm_column);

            rpkmRegions.add(new RpkmRegion(start, end, name, rpkm));
        }

        model = new RpkmVisualizerModel(manager);
        view = new RpkmVisualizerView(model);

        model.addSidePanelListener(new SidePanelListener() {
            public void modelChanged(SidePanelModel m, SidePanelEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (visualizerpanel != null) {
                            visualizerpanel.setZoom(((RpkmVisualizerModel) model).getZoom());
                            visualizerpanel.setDisplayLines(((RpkmVisualizerModel) model).getDisplayLines());
                        }
                    }
                });
            }
        });

        addSidePanelView(view);
    }

    public JComponent createModelView() {
        visualizerpanel = new VisualizerPanel(rpkmRegions);

        scrollPane = new ClcFocusScrollPane(visualizerpanel) {
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };

        return scrollPane;
    }
    
    public String getSideTitle() {
        return "Text";
    }

    public String getName() {
        return "RPKM Heat Map";
    }

    public void doPopup(MouseEvent e) {
        super.doPopup(e);
        //getContextGroup().getPopup().show((Component) e.getSource(), e.getX(), e.getY());
    }
    
    private void update() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    //textArea.setText(seq.getString());
                }
                catch (Exception e) {
                    //textArea.setText("ERROR");
                }
                //textArea.setCaretPosition(0);
            }
        });
    }
    
    protected void setEditorStateToDefault() {
        super.setEditorStateToDefault();
        // Reset font type and scale
    }
    
    @Override
    protected void cleanupInner() {
        super.cleanupInner();
        if (seq != null) {
            seq.removeListener(sequenceListener);
        }
    }
    
    @Override
    protected State getEditorState() {
        State s = super.getEditorState();
        s.putAll(model.save());
        return s;
    }
    
    @Override
    protected void setEditorState(State s) {
        super.setEditorState(s);
        model.load(s);
    }
    
    public ClcObject[] getEditingObjects(boolean isDragging) {
        return new ClcObject[] { /*seq*/ };
    }
    
    public String toString() {
        return getName();
    }

    public double getVersion() {
        return 1.0;
    }

    public String getClassKey() {
        return "simple_text_editor";
    }

    public ClcIcon getMenuIcon() {
        return EmptyIcon.getInstance();
    }
}
