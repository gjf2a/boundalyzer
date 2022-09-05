// Copyright 2009 by Gabriel J. Ferrer
//
// This program is part of the Boundalyzer project.
// 
// Boundalyzer is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Boundalyzer is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Boundalyzer.  If not, see <http://www.gnu.org/licenses/>.

package blackbox2;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class RowEditorPanel extends HistoryEditorPanel implements BoxChangeListener {
    private BlackBoxApp app;
    private JComboBox stimulusChoice, responseChoice, rowChoice;
    private JComboBox stimuliAt, responsesAt;
    private JButton addStim, delStim, addResp, delResp, delRow;
    private final String notNum = "New Row";
    
    public RowEditorPanel(BlackBoxApp app) {
        super(app);
        this.app = app;
    }
    
    public boolean atNewRow() {
        return rowChoice.getSelectedItem().toString().equals(notNum);
    }
    
    // Pre: !atNewRow()
    public int row() {
        return Integer.parseInt(rowChoice.getSelectedItem().toString());
    }
    
    // Pre: None
    // Post: Go to new row, if row is legit; otherwise, do nothing
    public void goToRow(int row) {
        if (!hasInitialText()) {setInitialText();}
        
        String rowStr = Integer.toString(row);
        for (int i = 0; i < rowChoice.getItemCount(); ++i) {
            String item = rowChoice.getItemAt(i).toString();
            if (item.equals(rowStr)) {
                rowChoice.setSelectedIndex(i);
                updateCurrentRow(app.getBlackBox());
            }
        }
    }
    
    protected void commit(HistoryPattern parsed) {
        if (atNewRow()) {
        	String stimuli = getComboStimuli();
        	String responses = getComboResponses();
            app.addRow(getEditorText(), stimuli, responses);
        } else {
            app.setHistoryAt(parsed, row());
        }
    }
    
    private String getComboStimuli() {
    	if (stimuliAt.getItemCount() == 0) {
    		return "all";
    	} else {
    		return fromCombo(stimuliAt);
    	}
    }
    
    private String getComboResponses() {
    	if (responsesAt.getItemCount() == 0) {
    		return "None";
    	} else {
    		return fromCombo(responsesAt);
    	}
    }
    
    protected String fromCombo(JComboBox combo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < combo.getItemCount(); ++i) {
            sb.append(combo.getItemAt(i));
            sb.append(' ');
        }
        return sb.toString();
    }
    
    protected void setInitialText() {
        super.setInitialText();
        app.addBoxChangeListener(this);
        rowChoice.addItem(notNum);
        rowChoice.setSelectedItem(notNum);
        boxChanged(app.getBlackBox());
    }
    
    public void boxChanged(BlackBox bb) {
        Object choice = rowChoice.getSelectedItem();
        int oldRow = -1;
        if (!atNewRow()) {
            int row = Integer.parseInt(choice.toString());
            if (bb.rowLegal(row)) {oldRow = row;}
        }
        
        rowChoice.removeAllItems();
        rowChoice.addItem(notNum);
        if (oldRow < 0) {rowChoice.setSelectedItem(notNum);}
        
        for (int i = 0; i < bb.numRows(); ++i) {
            rowChoice.addItem(i);
            if (i == oldRow) {rowChoice.setSelectedItem(i);}
        }
        
        updateCurrentRow(bb);
    }
    
    private void updateCurrentRow(BlackBox bb) {
        Object choice = rowChoice.getSelectedItem();
        if (choice != null) {
            stimuliAt.removeAllItems();
            responsesAt.removeAllItems();
            redrawComboBox(stimulusChoice, bb.allStimuli().with("all"));
            redrawComboBox(responseChoice, bb.allResponses());
            if (!atNewRow()) {
                int row = row();
                for (String stim: bb.stimuliAt(row).stimuli(bb)) {
                    stimuliAt.addItem(stim);
                    stimulusChoice.removeItem(stim);
                }
                for (String resp: bb.responseAt(row).responses()) {
                    responsesAt.addItem(resp);
                    responseChoice.removeItem(resp);
                }
                setEditorText(bb.historyAt(row).toString());
            }
        }
    }
    
    private void redrawComboBox(JComboBox box, Iterable<String> items) {
        box.removeAllItems();
        for (String s: items) {box.addItem(s);}
    }
    
    protected void addHeaders() {
        rowChoice = new JComboBox();
        rowChoice.addActionListener(new RowUpdated());

        delRow = new JButton("Delete Entire Row");
        delRow.addActionListener(new RowDeleter());

        stimulusChoice = new JComboBox();
        responseChoice = new JComboBox();
        stimuliAt = new JComboBox();
        responsesAt = new JComboBox();
        /*
        addStim = new JButton("Add");
        delStim = new JButton("Delete");
        addResp = new JButton("Add");
        delResp = new JButton("Delete");
        */
        addStim = new gui.JImageButton("addButtonUp.gif", "addButtonDown.gif", "Add stimulus to row");
        delStim = new gui.JImageButton("delButtonUp.gif", "delButtonDown.gif", "Remove stimulus from row");
        addResp = new gui.JImageButton("addButtonUp.gif", "addButtonDown.gif", "Add response to row");
        delResp = new gui.JImageButton("delButtonUp.gif", "delButtonDown.gif", "Remove response from row");
        
        addStim.addActionListener(new StimAdder());
        delStim.addActionListener(new StimDeleter());
        addResp.addActionListener(new RespAdder());
        delResp.addActionListener(new RespDeleter());
        
        JPanel top = new JPanel();
        top.setLayout(new GridLayout(3, 1));
        
        JPanel one = new JPanel();
        one.setLayout(new FlowLayout());
        one.add(new JLabel("Row Number"));
        one.add(rowChoice);
        one.add(delRow);
        top.add(one);
        
        JPanel two = new JPanel();
        two.setLayout(new FlowLayout());
        addCombo(two, stimuliAt, "Stimuli in Row", delStim);
        addCombo(two, responsesAt, "Responses in Row", delResp);
        top.add(two);
        
        JPanel three = new JPanel();
        three.setLayout(new FlowLayout());
        addCombo(three, stimulusChoice, "Stimulus Choices", addStim);
        addCombo(three, responseChoice, "Response Choices", addResp);
        top.add(three);
        
        add(top);
    }
    
    private void addCombo(JPanel panel, JComboBox combo, String label, JButton act) {
        panel.add(new JLabel(label));
        panel.add(combo);
        combo.setFont(courier);
        combo.setEditable(false);
        panel.add(act);
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: RowEditorPanel blackbox.bbx");
            System.exit(1);
        }
        
        BlackBoxApp bba = new BlackBoxApp();
        RowEditorPanel hep = new RowEditorPanel(bba);
        bba.startFrame(hep, args[0], "Row Editor", 650, 500);
    }
    
    private class RowUpdated implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            updateCurrentRow(app.getBlackBox());
        }
    }
    
    private class StimAdder implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object choice = stimulusChoice.getSelectedItem();
            if (choice != null) {
                stimulusChoice.removeItem(choice);
                String toAdd = choice.toString();
                addTo(stimuliAt, toAdd);
                if (!atNewRow()) {app.addStimulusTo(toAdd, row());}
            }
        }
    } 
    
    private class StimDeleter implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object choice = stimuliAt.getSelectedItem();
            if (choice != null) {
                stimuliAt.removeItem(choice);
                String toDel = choice.toString();
                stimulusChoice.addItem(toDel);
                if (!atNewRow()) {app.delStimulusFrom(toDel, row());}
            }
        }
    } 

    private class RespAdder implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object choice = responseChoice.getSelectedItem();
            if (choice != null) {
                responseChoice.removeItem(choice);
                String toAdd = choice.toString();
                addTo(responsesAt, toAdd);
                if (!atNewRow()) {app.addResponseTo(toAdd, row());}
            }
        }
    } 
    
    private class RespDeleter implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object choice = responsesAt.getSelectedItem();
            if (choice != null) {
                responsesAt.removeItem(choice);
                String toDel = choice.toString();
                responseChoice.addItem(toDel);
                if (!atNewRow()) {app.delResponseFrom(toDel, row());}
            }
        }
    }

    private class RowDeleter implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!atNewRow()) {
                app.deleteEntireRow(row());
                boxChanged(app.getBlackBox());
            }
        }
    }
    
    private void addTo(JComboBox box, String item) {
        boolean present = false;
        for (int i = 0; !present && i < box.getItemCount(); ++i) {
            if (item.equals(box.getItemAt(i))) {present = true;}
        }
        if (!present) {box.addItem(item);}
    }
}
