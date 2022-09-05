package blackbox2;

import java.awt.*;
import java.awt.event.*;
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

import javax.swing.*;

@SuppressWarnings("serial")
public class MacroEditorPanel extends HistoryEditorPanel implements BoxChangeListener {
    private BlackBoxApp app;
    private JComboBox name;
    private JTextField newName;
    private JButton add, del, rename;
    
    public MacroEditorPanel(BlackBoxApp app) {
        super(app);
        this.app = app;
        app.addBoxChangeListener(this);
        loadComboBox();
    }
    
    public String macroName() {
    	Object result = name.getSelectedItem();
    	return (result == null) ? null : result.toString();
    }
    
    public void setMacroName(String name) {
        this.name.setSelectedItem(name);
    }

    protected void commit(HistoryPattern parsed) {
    	try {
    		if (macroName() != null) {
    			app.addMacro(macroName(), parsed);
    		}
    	} catch (Exception exc) {
    		reportError(exc.getMessage());
    		exc.printStackTrace();
    	}
    }
    
    private boolean macrosPresent() {
        return name.getItemCount() > 0;
    }
    
    protected void setInitialText() {
        if (macrosPresent() && app.getBlackBox().allMacros().contains(macroName())) {
            setEditorText(app.getBlackBox().macroDefinition(macroName()).toString());
        }
    }
    
    private void loadComboBox() {
    	//System.out.println("loading combo box");
        name.removeAllItems();
        for (String macro: app.getBlackBox().allMacros()) {
            name.addItem(macro);
            //System.out.println("macro: " + macro);
        }
    }
    
    public void boxChanged(BlackBox bb) {
        if (bb.allMacros().isEmpty()) {
            name.removeAllItems();
        } else if (name.getItemCount() == 0) {
            loadComboBox();
            setInitialText();
        } else {
            String n = macroName();
            loadComboBox();
            if (app.getBlackBox().allMacros().contains(n)) {
                setMacroName(n);
                setInitialText();
            }
        }
        clearErrors();
    }
    
    protected void addHeaders() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout());
        headerPanel.add(new JLabel("Select macro:"));
        name = new JComboBox();
        name.setFont(courier);
        name.addActionListener(new MacroChanger());
        headerPanel.add(name);
        
        rename = new JButton("Change macro name");
        rename.addActionListener(new Renamer());
        headerPanel.add(rename);
        
        newName = new JTextField(10);
        newName.setFont(courier);
        headerPanel.add(newName);
        
        //add = new JButton("Add new macro:");
        add = new gui.JImageButton("addButtonUp.gif", "addButtonDown.gif", "Add new macro");
        add.addActionListener(new Adder());
        headerPanel.add(add);
        
        //del = new JButton("Delete macro");
        del = new gui.JImageButton("delButtonUp.gif", "delButtonDown.gif", "Delete macro");
        del.addActionListener(new Deleter());
        headerPanel.add(del);
        
        add(headerPanel);
    }
    
    private class MacroChanger implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            boxChanged(app.getBlackBox());
        }
    }
    
    private String getApprovedName() {
    	return newName.getText().replace(' ', '_');
    }
    
    private class Adder implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (newName.getText().length() > 0) {
                String newMacro = getApprovedName();
                app.addMacro(newMacro, getEditorText());
                if (app.lastOpSucceeded()) {
                    newName.setText("");
                    clearErrors();
                } else {
                    reportError(app.lastOpMessage());
                }
            } else {
                reportError("No name for new macro given");
            }
        }
    }
    
    private class Deleter implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		try {
    			app.deleteMacro(macroName());
    		} catch (Exception exc) {
        		JOptionPane.showMessageDialog(null, exc.getMessage());
    		}
    	}
    }
    
    private class Renamer implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		try {
    			app.renameMacro(macroName(), getApprovedName());
    		} catch (Exception exc) {
    			JOptionPane.showMessageDialog(null, exc.getMessage());
        		//exc.printStackTrace();
    		}
    	}
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: MacroEditorPanel blackbox.bbx");
            System.exit(1);
        }
        
        BlackBoxApp bba = new BlackBoxApp();
        MacroEditorPanel hep = new MacroEditorPanel(bba);
        bba.startFrame(hep, args[0], "Macro Editor", 500, 500);
    }
}
