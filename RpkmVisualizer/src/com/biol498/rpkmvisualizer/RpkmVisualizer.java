package com.biol498.rpkmvisualizer;


import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.clcbio.api.base.persistence.PersistenceException;
import com.clcbio.api.base.util.State;
import com.clcbio.api.free.datatypes.ClcObject;
import com.clcbio.api.free.datatypes.bioinformatics.sequence.Sequence;
import com.clcbio.api.free.datatypes.bioinformatics.sequence.list.SequenceList;
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

public class RpkmVisualizer extends AbstractEditor {
    public final static String PLUGIN_GROUP = "free";
    private JTextArea textArea;
    private ClcFocusScrollPane scrollPane;
    private Sequence seq;
    private ObjectListener sequenceListener;

    private Font font = new Font("Monospaced", Font.PLAIN, 12);
    private float[] sizeLookup = new float[] {6f, 9f, 12f, 18f, 24f};
    
    public boolean canEdit(Class<?>[] types) {
        // TODO: Implement
        //for (Class<?> c : types)
         //   if (c != Sequence.class)
          //      return false;
        return true;
    }
    
    public void initEditorInstance(WorkbenchManager wm, ClcObject[] models, Workspace ws) {
        super.initEditorInstance(wm, models, ws);
        seq = ((SequenceList)models[0]).getSequence(0);

        sequenceListener = new ObjectListener() {
            public void eventOccurred(ObjectEvent event) {
                if (event instanceof SelectionEvent) {
                    return;
                }
                update();
            }
        };
        seq.addListener(sequenceListener);
    }
    
    public JComponent createModelView() {
        textArea = new JTextAreaNotPasteable();
        textArea.setText(seq.getString());
        textArea.setFont(font);
        textArea.setBackground(Color.WHITE);
        textArea.setEditable(false);
        scrollPane = new ClcFocusScrollPane(textArea);
        textArea.setCaretPosition(0);
        return scrollPane;
    }
    
    public String getSideTitle() {
        return "Text";
    }

    public String getName() {
        return "First Editor Plugin";
    }

    public void doPopup(MouseEvent e) {
        super.doPopup(e);
        //getContextGroup().getPopup().show((Component) e.getSource(), e.getX(), e.getY());
    }
    
    private void update() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    textArea.setText(seq.getString());
                }
                catch (Exception e) {
                    textArea.setText("ERROR");
                }
                textArea.setCaretPosition(0);
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
        //s.putAll(textModel.save());
        return s;
    }
    
    @Override
    protected void setEditorState(State s) {
        super.setEditorState(s);
        //textModel.load(s);
    }
    
    public ClcObject[] getEditingObjects(boolean isDragging) {
        return new ClcObject[] { seq };
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
