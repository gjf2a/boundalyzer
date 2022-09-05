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
import gui.*;

@SuppressWarnings("serial")
public class BlackBoxPanel extends JPanel {
    private BlackBoxApp app;
    private BlackBoxModel boxModel;
    private GridPanel table;
    private JTabbedPane otherPanels;
    private Component[] otherPanelElements;
    private RowEditorPanel rowEditor;
    private StimulusEditorPanel stimEditor;
    private ResponseEditorPanel respEditor;
    private MacroEditorPanel macroEditor;
    
    private Animator animator = new Animator();
    private ConsistencyHandler consistencyHandler = new ConsistencyHandler();
    private CompletenessHandler completenessHandler = new CompletenessHandler();
    private IndependenceHandler independenceHandler = new IndependenceHandler();
    
    private JButton stopSearch = new JButton("Stop search");
    private JButton clearField = new JButton("Clear messages");
    
    private JButton protoT, compButton;
    private JTextArea messages = new JTextArea(10, 41);
    private JScrollPane messageScroller = 
	    new JScrollPane(messages, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
    private Font courier = new Font("Courier", Font.PLAIN, 12);
    
    public BlackBoxPanel(BlackBoxApp app) {
        super();
        this.app = app;
        app.addBoxChangeListener(new Updater());
        
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        
        topPanel.setLayout(new BorderLayout());
        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setLayout(new FlowLayout());
        
        add(topPanel, BorderLayout.NORTH);
        topPanel.add(topButtonPanel, BorderLayout.SOUTH);
        clearField.setToolTipText("Clears the Messages tab");
        stopSearch.setToolTipText("Stops the current search");
        
        paintButtons();
        
        topButtonPanel.add(stopSearch);
        topButtonPanel.add(protoT);
        topButtonPanel.add(compButton);
        topButtonPanel.add(clearField);
        
        //JPanel infoPanel = new JPanel();
        //infoPanel.setLayout(new GridLayout(2, 1));
        // JSplitPane defaults to divider at around 0.1.  Annoying.
        JSplitPane infoPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        boxModel = new BlackBoxModel(app);
        table = new GridPanel(boxModel);
        table.enableSwapRows();
        infoPanel.add(new JScrollPane(table));
        
        otherPanels = new JTabbedPane();
        messages.setEditable(false);
        messages.setFont(courier);
        otherPanels.add("Messages", messageScroller);
        
        rowEditor = new RowEditorPanel(app);
        otherPanels.add("Row Editor", new JScrollPane(rowEditor));
        
        stimEditor = new StimulusEditorPanel(app);
        otherPanels.add("Stimulus Editor", new JScrollPane(stimEditor));
        
        respEditor = new ResponseEditorPanel(app);
        otherPanels.add("Response Editor", new JScrollPane(respEditor));
        
        macroEditor = new MacroEditorPanel(app);
        otherPanels.add("Macro Editor", new JScrollPane(macroEditor));
        
        otherPanelElements = new Component[]{messages,rowEditor,stimEditor,respEditor,macroEditor};

        infoPanel.add(otherPanels);
        //add(otherPanels, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.CENTER);
        
        int splitPanelHeight = otherPanels.getPreferredSize().height + table.getPreferredSize().height;
        infoPanel.setDividerLocation(splitPanelHeight / 2);
        
        protoT.addActionListener(animator);
        compButton.addActionListener(consistencyHandler);
        compButton.addActionListener(completenessHandler);
        compButton.addActionListener(independenceHandler);
        
        stopSearch.addActionListener(new Stopper());
        clearField.addActionListener(new Clearer());
        
        table.addGridPanelListener(new RowEditSelector());
    }
    
    private int tabIndexOf(Component c) {
        for (int i = 0; i < otherPanelElements.length; ++i) {
            if (otherPanelElements[i] == c) {
                return i;
            }
        }
        return -1;
    }
    
    private void paintButtons() {
        
        protoT = new JImageButton("protoButUp.gif", "protoButUp.gif", "Create an animation");
        compButton = new JImageButton("compButUp.gif", "compButDown.gif", "Check completeness, row well-definition, and row independence");
    }

    private void addScrollingList(JPanel sideScroll, JList list, String headerStr, JButton... buttons) {
        JScrollPane listPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(courier);
        
        JPanel header = new JPanel();
        header.add(new JLabel(headerStr));
        for (JButton jb: buttons) {header.add(jb);}
        sideScroll.add(header);
        sideScroll.add(listPane);
    }
    
    protected void paintComponent(Graphics g) {
    	table.repaint();
    }
    
    private class Updater implements BoxChangeListener {
        public void boxChanged(BlackBox box) {
            table.repaint();
        }
    }
    
    // Pre: None
    // Post: appends msg to the message text area for persistent viewing
    public void persistentUserMsg(String msg) {
        messages.append(msg + "\n");
        JScrollBar v = messageScroller.getVerticalScrollBar();
        v.setValue(v.getMaximum() + v.getVisibleAmount());
    }
    
    public void persistentUserMsg(String msg, String alternate) {
        persistentUserMsg(msg.equals("") ? alternate : msg);
    }
    
    public void animate() {
        AnimationPanel ap = new AnimationPanel(app.getBlackBox());
        app.addBoxChangeListener(ap);
        ap.setScrollPane(new JScrollPane(ap));
        ap.addTabCloseListener(new AnimationCloser());
        otherPanels.add("Animation", ap.getOuterPanel());
    }
    
    private class Animator implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            animate();
        }
    }
    
    public void searchError(String msg) {
    	persistentUserMsg("Error from search: " + msg);
    }

    public void checkCompleteness() {
    	try {
    		app.checkCompleteness(completenessHandler);
    		persistentUserMsg("Checking completeness...");
    	} catch (Exception e) {
    		searchError(e.getMessage());
    		e.printStackTrace();
    	}
    }
    
    private class SearchHandler implements ErrorListener {
		public void errorMessage(String msg) {
			searchError(msg);
		}
    }
    
    private class CompletenessHandler extends SearchHandler implements ActionListener, SearchListener<String> {
        public void actionPerformed(ActionEvent e) {
            checkCompleteness();
        }
        
        public void report(SearchData<String> data) {
            persistentUserMsg("Completeness result:\n" + data.toString());
        }
    }

    public void checkIndependence() {
    	try {
    		app.checkDisjunction(independenceHandler);
    		persistentUserMsg("Checking row independence...");
    	} catch (Exception e) {
    		searchError(e.getMessage());
    	}
    }

    private class IndependenceHandler extends SearchHandler implements ActionListener, SearchListener<String> {
        public void actionPerformed(ActionEvent e) {
            checkIndependence();
        }
        
        public void report(SearchData<String> data) {
            persistentUserMsg("Row independence result:\n" + data.toString());
        }
    }

    public void checkConsistency() {
    	try {
    		app.checkConsistency(consistencyHandler);
    		persistentUserMsg("Checking row well-definition...");
    	} catch (Exception e) {
    		searchError(e.getMessage());
    	}
    }
    
    private class ConsistencyHandler extends SearchHandler implements ActionListener, SearchListener<Integer> {
        public void actionPerformed(ActionEvent e) {
            checkConsistency();
        }
        
        public void report(SearchData<Integer> data) {
            persistentUserMsg("Well-definition result:\n" + data.toString());
        }
    }
    
    private class Stopper implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            app.stopSearching();
        }
    }
    
    private class Clearer implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            messages.setText("");
        }
    }
    
    private class RowEditSelector implements GridPanelListener {
        public void cellClicked(int col, int row) {
            otherPanels.setSelectedIndex(tabIndexOf(rowEditor));
            rowEditor.goToRow(row - 1); // skip header row
        }
    }

    private class AnimationCloser implements TabCloseListener {
        public void closeTab(Component c) {
            int whatTab = otherPanels.indexOfComponent(c);
            otherPanels.remove(whatTab);
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: BlackBoxPanel blackbox.bbx");
            System.exit(1);
        }
        
        BlackBoxApp bba = new BlackBoxApp();
        BlackBoxPanel bbp = new BlackBoxPanel(bba);
        bba.startFrame(bbp, args[0], "Black Box Editor", 725, 700);
    }
}
