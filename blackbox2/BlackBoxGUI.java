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

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class BlackBoxGUI extends JFrame {
    private JTabbedPane tabs = new JTabbedPane();
    private ArrayList<BlackBoxApp> bbApps = new ArrayList<BlackBoxApp>();
    
    private JMenuItem newBox = new JMenuItem("New");
    private JMenuItem loadBox = new JMenuItem("Open");
    private JMenuItem export = new JMenuItem("Export");
    private JMenuItem exportAs = new JMenuItem("Export as...");
    private JMenuItem saveBox = new JMenuItem("Save");
    private JMenuItem saveAsBox = new JMenuItem("Save as...");
    private JMenuItem quit = new JMenuItem("Exit");
    
    private JMenuItem undo = new JMenuItem("Undo");
    private JMenuItem redo = new JMenuItem("Redo");
    
    private JMenuItem showHist = new JMenuItem("View");
    
    private JMenuItem makePrototype =
        new JMenuItem("Create animation of the black box");
    private JMenuItem consist = new JMenuItem("Check consistency");
    private JMenuItem complete = new JMenuItem("Check completeness");
    private JMenuItem independent = new JMenuItem("Check independence");
    
    private JMenuItem howTo = new JMenuItem("How to write histories");
    private JMenuItem showSyntax = new JMenuItem("Show all syntax elements");
    private JMenuItem findSyntax = new JMenuItem("Look up a syntax element");

    private TabTitler titler = new TabTitler();

    private final static String newTabTitle = "New";

    private JFileChooser fileChooser = new JFileChooser();
    private class BoxFilter extends FileFilter {
        public boolean accept(File f) {
            String name = f.getName().toLowerCase();
            return f.isDirectory() || name.endsWith(".bbx");
        }
        
        public String getDescription() {
            return "Black Box files";
        }
    }
    
    public BlackBoxGUI() {
        setTitle("Boundalyzer: Black Box Editor");
        setSize(725, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Quitter q = new Quitter();
        addWindowListener(q);
        JMenuBar bar = new JMenuBar();
        setJMenuBar(bar);
        
        fileChooser.setFileFilter(new BoxFilter());
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        
        JMenu fileMenu = new JMenu("File");
        bar.add(fileMenu);
        newBox.addActionListener(new NewBoxer());
        fileMenu.add(newBox);
        loadBox.addActionListener(new BoxLoader());
        fileMenu.add(loadBox);
        /*
        export.addActionListener(this);
        fileMenu.add(export);
        exportAs.addActionListener(this);
        fileMenu.add(exportAs);
        */
        saveBox.addActionListener(new BoxSaver());
        fileMenu.add(saveBox);
        saveAsBox.addActionListener(new BoxSaver());
        fileMenu.add(saveAsBox);
        fileMenu.addSeparator();
        quit.addActionListener(q);
        fileMenu.add(quit);
        
        JMenu editMenu = new JMenu("Edit");
        bar.add(editMenu);
        undo.addActionListener(new Undoer());
        editMenu.add(undo);
        redo.addActionListener(new Redoer());
        editMenu.add(redo);
        
        JMenu testMenu = new JMenu("Analysis");
        bar.add(testMenu);
        makePrototype.addActionListener(new Animator());
        testMenu.add(makePrototype);
        consist.addActionListener(new ConsistChecker());
        testMenu.add(consist);
        complete.addActionListener(new CompleteChecker());
        testMenu.add(complete);
        independent.addActionListener(new IndependentChecker());
        testMenu.add(independent);
        
        /*
        JMenu helpMenu = new JMenu("Help");
        bar.add(helpMenu);
        howTo.addActionListener(this);
        helpMenu.add(howTo);
        showSyntax.addActionListener(this);
        helpMenu.add(showSyntax);
        findSyntax.addActionListener(this);
        helpMenu.add(findSyntax);
        */
        
        // Java 6.0 option to close the tab panes
        //tabs.putClientProperty(SubstanceLookAndFeel.TABBED_PANE_CLOSE_BUTTONS_PROPERTY, Boolean.TRUE);
        setContentPane(tabs);
        addNewFile();
    }
    
    private BlackBoxApp tabApp() {
        return bbApps.get(tabs.getSelectedIndex());
    }
    
    private BlackBoxPanel tabPanel() {
        return (BlackBoxPanel)(tabs.getSelectedComponent());
    }
    
    private class Animator implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabPanel().animate();
        }
    }
    
    private class ConsistChecker implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabPanel().checkConsistency();
        }
    }
    
    private class CompleteChecker implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabPanel().checkCompleteness();
        }
    }
    
    private class IndependentChecker implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabPanel().checkIndependence();
        }
    }
    
    private void addNewFile() {
        addNewTab(new BlackBoxApp(), newTabTitle);
    }
    
    private void addNewTab(BlackBoxApp app, String title) {
        BlackBoxPanel bbp = new BlackBoxPanel(app);
        app.addBoxSaveListener(titler);
        tabs.add(title, bbp);
        bbApps.add(app);
        tabs.setSelectedIndex(tabs.getTabCount() - 1);
    }

    private class BoxLoader implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		try {
    			int option = fileChooser.showOpenDialog(BlackBoxGUI.this);
    			if (option == JFileChooser.APPROVE_OPTION) {
    				File choice = fileChooser.getSelectedFile();
    				BlackBoxApp newApp = new BlackBoxApp();
    				newApp.load(choice);
    				if (newApp.lastOpSucceeded()) {
    					addNewTab(newApp, choice.getName());
    				} else {
    					userMsg(newApp.lastOpMessage());
    				}
    			}
    		} catch (Exception exc) {
    			JOptionPane.showMessageDialog(null, exc.getMessage());
    			exc.printStackTrace();
    		}
        }
    }
    
    private class NewBoxer implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            addNewFile();
        }
    }
    
    private class BoxSaver implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            File f = null;
            if (e.getSource() == saveAsBox || tabApp().currentFile() == null) {
                int option = fileChooser.showSaveDialog(BlackBoxGUI.this);
                if (option != JFileChooser.APPROVE_OPTION) {return;}
                f = fileChooser.getSelectedFile();
                if (f.exists() && !userSaysYes("File exists; overwrite?", "File save")) {
                    return;
                }
                
                String fName = f.getAbsolutePath();
                if (fName.length() < 5 || !fName.substring(fName.length() - 4, fName.length()).equals(".bbx")) {
                    f = new File(fName + ".bbx");
                }
            } else if (tabApp().needsSaving()) {
                f = tabApp().currentFile();
            } else {
                userMsg(tabApp().currentFile().getName() + " already saved");
                return;
            }
            
            tabApp().save(f);
            if (tabApp().lastOpSucceeded()) {
                tabPanel().persistentUserMsg("File " + f.getName() + " saved");
                tabs.setTitleAt(tabs.getSelectedIndex(), f.getName());
            } else {
                userMsg("Sorry, trouble saving " + f.getName());
            }
        }
    }
    
    private class Undoer implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabApp().undo();
        }
    }
    
    private class Redoer implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tabApp().redo();
        }
    }

    // Pre: msg != null
    // Post: Brings up a simple noninteractive user dialog box with a message
    private void userMsg(String msg) {userMsg(msg, "");}
    
    private void userMsg(String msg, String alternate) {
        if (msg.equals("")) {msg = alternate;}
        JOptionPane.showMessageDialog(null, msg);
    }
    
    private String getUserString(String requestMsg) {
        String result = JOptionPane.showInputDialog(requestMsg);
        if (result == null) {
            //throw new IllegalArgumentException("Operation cancelled");
        } else if (result.equals("")) {
            throw new IllegalArgumentException("No text entered");
        }
        return result;
    }
    
    private int getUserInt(String requestMsg) {
        String result = getUserString(requestMsg);
        try {
            return Integer.parseInt(result);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(result + " is not an integer");
        }
    }
    
    private boolean userSaysYes(String msg, String title) {
        int choice = JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return (choice == JOptionPane.YES_OPTION);
    }
    
    private class Quitter extends WindowAdapter implements ActionListener {
        public void actionPerformed(ActionEvent e) {close();}
        
        public void windowClosing(WindowEvent e) {close();}
        
        public void close() {
            //System.exit(0);
            if (passesSavedCheck()) {
                System.exit(0);
            } else {
                int reply = JOptionPane.showConfirmDialog(null, "Unsaved files.  Continue exiting?", "Unsaved files", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        }
    }

    public boolean passesSavedCheck() {
        for (int i = 0; i < tabs.getTabCount(); ++i) {
            if (bbApps.get(tabs.getSelectedIndex()).needsSaving()) {
                return false;
            }
        }
        return true;
    }

    private class TabTitler implements BoxSaveListener {
        private int currentTab;
        private String currentTitle;

        public TabTitler() {
            currentTab = 0;
            currentTitle = newTabTitle;
        }

        public void titler(BlackBoxApp src) {
            File tabFile = src.currentFile();
            currentTitle = (tabFile == null) ? newTabTitle : tabFile.getName();
            currentTab = bbApps.indexOf(src);
        }

        public void boxNeedsSaving(BlackBoxApp src) {
            titler(src);
            tabs.setTitleAt(currentTab, currentTitle + "*");
        }

        public void boxSaved(BlackBoxApp src) {
            titler(src);
            tabs.setTitleAt(currentTab, currentTitle);
        }
    }

    public static void main(String[] args) {
        BlackBoxGUI bbg = new BlackBoxGUI();
        bbg.setVisible(true);
    }
} 

