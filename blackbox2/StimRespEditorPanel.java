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
import util.*;

@SuppressWarnings("serial")
abstract public class StimRespEditorPanel extends JPanel implements BoxChangeListener {
    private BlackBoxApp app;
    private JTextField name;
    private JTextArea definition;
    private JComboBox choices;
    private JButton add, del, chg, rename;
    
    private Font courier = new Font("Courier", Font.PLAIN, 12);
    
    public StimRespEditorPanel(BlackBoxApp app) {
        super();
        this.app = app;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel choosePanel = new JPanel();
        choosePanel.setLayout(new FlowLayout());
        addTitleTo(choosePanel);
        choices = new JComboBox();
        choices.addActionListener(new Switcher());
        choosePanel.add(choices);
        
        // Note: Add and Change do the exact same thing.  This is 
        // because at the deeper implementation level, "adding" and
        // "altering" are effectively implemented the same way.
        //add = new JButton("Add");
        add = new gui.JImageButton("addButtonUp.gif", "addButtonDown.gif", "Add new");
        choosePanel.add(add);
        add.addActionListener(new Changer());
        
        //del = new JButton("Delete");
        del = new gui.JImageButton("delButtonUp.gif", "delButtonDown.gif", "Delete selected");
        del.addActionListener(new Remover());
        choosePanel.add(del);
        
        rename = new JButton("Change name");
        rename.addActionListener(new Renamer());
        choosePanel.add(rename);
        chg = new JButton("Change definition");
        choosePanel.add(chg);
        chg.addActionListener(new Changer());
        
        add(choosePanel);
        
        JPanel editNamePanel = new JPanel();
        editNamePanel.setLayout(new FlowLayout());
        editNamePanel.add(new JLabel("Name"));
        name = new JTextField(20);
        name.setFont(courier);
        editNamePanel.add(name);
        
        add(editNamePanel);
        
        JPanel editDefPanel = new JPanel();
        editDefPanel.setLayout(new BoxLayout(editDefPanel, BoxLayout.Y_AXIS));
        editDefPanel.add(new JLabel("Definition"));
        definition = new JTextArea(10, 30);
        definition.setFont(courier);
        editDefPanel.add(new JScrollPane(definition));
        
        add(editDefPanel);
        
        loadComboBox();
        
        app.addBoxChangeListener(this);
    }
    
    protected BlackBoxApp app() {return app;}
    
    protected void addTitleTo(JPanel jp) {jp.add(new JLabel("Test"));}
    
    abstract protected void update(String name, String def);
    
    abstract protected void remove(String name);
    
    public void boxChanged(BlackBox bb) {
        if (nameSet().isEmpty()) {
            choices.removeAllItems();
        } else if (hasNames()) {
            String n = currentName();
            loadComboBox();
            if (nameSet().contains(n)) {
                setCurrentName(n);
                setInitialText();
            }
        } else {
            loadComboBox();
            setInitialText();
        }
    }
    
    private void setCurrentName(String n) {
        if (nameSet().contains(n)) {
            choices.setSelectedItem(n);
        }
    }
    
    private void loadComboBox() {
        choices.removeAllItems();
        for (String s: nameSet()) {choices.addItem(s);}
    }
    
    private boolean hasDefinition() {
        return hasNames() && nameSet().contains(currentName());
    }
    
    private boolean hasNames() {return choices.getItemCount() > 0;}        
    
    abstract protected SharedSet<String> nameSet();
    
    protected String currentName() {
        return choices.getSelectedItem().toString();
    }
    
    abstract protected String getDefinition();
    
    private void setInitialText() {
        if (hasDefinition()) {
            name.setText(currentName());
            definition.setText(getDefinition());
        } else {
            name.setText("");
            definition.setText("");
        }
    }
    
    private class Switcher implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setInitialText();
        }
    }
    
    private String getApprovedName() {
    	return name.getText().replace(' ', '_');
    }
    
    private class Changer implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        	try {
        		String changed = getApprovedName();
        		update(changed, definition.getText());
        		setCurrentName(changed);
        	} catch (Exception exc) {
        		JOptionPane.showMessageDialog(null, exc.getMessage());
        	}
        }
    }
    
    private class Renamer implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		try {
    			rename(currentName(), getApprovedName());
    			setCurrentName(getApprovedName());
    		} catch (Exception exc) {
        		JOptionPane.showMessageDialog(null, exc.getMessage());
        		//exc.printStackTrace();
        	}
    	}
    }
    
    abstract protected void rename(String oldName, String newName);
    
    private class Remover implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        	try {
        		remove(currentName());
        	} catch (Exception exc) {
        		JOptionPane.showMessageDialog(null, exc.getMessage());
        	}
        }
    }
}
