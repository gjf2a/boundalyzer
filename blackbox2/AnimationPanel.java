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
import javax.swing.event.*;
import javax.swing.table.*;
import java.io.*;
import gui.*;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class AnimationPanel extends JPanel implements BoxChangeListener {
    private AnimationApp app;
    private Stimulator doer;
    private JButton[] stimuli;
    private JButton clear, quit, backUp;
    private JTextField current;
    private JTable histTable;
    private JPanel stimPanel;
    private ProtoTable histModel;
    private JScrollPane histScroll, outerScroll;
    private GridPanel macroValues;
    private ViewMacroModel macroValuesModel;

    private LinkedList<TabCloseListener> tabCloseListeners;
    
    private Font courier = new Font("Courier", Font.PLAIN, 12);
    
    public AnimationPanel(BlackBox bb) {
    	super();
    	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    	JPanel mainPanel = new JPanel();
    	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

    	try {
    		app = new AnimationApp(bb);

    		doer = new Stimulator();        

    		JPanel buttonPanel = new JPanel();
    		buttonPanel.setLayout(new FlowLayout());

    		backUp = new JButton("Back up");
    		backUp.addActionListener(new BackerUp());
    		buttonPanel.add(backUp);

    		clear = new JButton("Clear history");
    		clear.addActionListener(new Clearer());
    		buttonPanel.add(clear);

    		quit = new JButton("End animation");
    		quit.addActionListener(new Quitter());
    		buttonPanel.add(quit);

    		mainPanel.add(buttonPanel);

    		JPanel responsePanel = new JPanel();
    		responsePanel.setLayout(new FlowLayout());
    		current = new JTextField(40);
    		current.setEditable(false);
    		responsePanel.add(new JLabel("Current response"));
    		responsePanel.add(current);

    		mainPanel.add(responsePanel);

    		int size = bb.allStimuli().size();
    		int width = (int)(Math.sqrt(size));
    		int remainder = size - (width*width);
    		int height = (remainder > 0) ? width + 1 : width;

    		stimPanel = new JPanel();
    		if (size > 0) {
    			stimPanel.setLayout(new GridLayout(height, width));
    			stimPanel.setBorder(BorderFactory.createEtchedBorder());
    			addStimulusButtons();
    		}
    		mainPanel.add(stimPanel);

    		histModel = new ProtoTable();
    		histTable = new JTable(histModel);
    		TableColumn c = histTable.getColumnModel().getColumn(1);
    		c.setPreferredWidth(180);
    		c = histTable.getColumnModel().getColumn(2);
    		c.setPreferredWidth(180);
    		histScroll = new JScrollPane(histTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    		histScroll.setPreferredSize(new Dimension(450, 200));
    		histTable.setFont(courier);
    		mainPanel.add(histScroll);

    		tabCloseListeners = new LinkedList<TabCloseListener>();

    		outerScroll = null;

    		add(mainPanel);

    		macroValuesModel = new ViewMacroModel(bb);
    		macroValues = new GridPanel(macroValuesModel);
    		add(macroValues);
    	} catch (Exception exc) {
    		JOptionPane.showMessageDialog(null, exc.getMessage());
    		exc.printStackTrace();
        }
    }
    
    private void addStimulusButtons() {
    	BlackBox bb = app.blackBox();
        stimuli = new JButton[bb.allStimuli().size()];
        int index = 0;
        for (String stim: bb.allStimuli()) {
            stimuli[index] = new JButton(stim);
            stimuli[index].setFont(courier);
            stimuli[index].addActionListener(doer);
            stimPanel.add(stimuli[index]);
            index += 1;
        }    	
    }

    public void setScrollPane(JScrollPane outer) {
        outerScroll = outer;
    }

    public Component getOuterPanel() {
        return outerScroll == null ? this : outerScroll;
    }

    public void addTabCloseListener(TabCloseListener tcl) {
        tabCloseListeners.add(tcl);
    }
    
    private void histTableChanged() {
        histTable.tableChanged(new TableModelEvent(histModel));
        macroValuesModel.updateSequence(app.seq());
        macroValues.repaint();
    }
    
    private class BackerUp implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		if (app.seq().length() > 0) {
    			app.retract();
    			histTableChanged();
    		}
    	}
    }
    
    private class Stimulator implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JButton) {
            	try {
                	String stimulus = ((JButton)e.getSource()).getText();
                	int startLength = app.seq().length();
            		app.stimulate(stimulus);
            		if (startLength < app.seq().length()) {
            			current.setText(app.seq().finalResponse().toString());
            			histTableChanged();
            		} else if (app.seq().isImpossible()) {
            			current.setText("Impossible; no further responses possible");
            		} else {
            			current.setText("No specified response to " + stimulus);
            		}
                } catch (Exception exc) {
                	if (exc.getMessage() == null) {
                		current.setText("No message from " + exc.getClass().getName());
                	} else {
                		current.setText("Msg:"+exc.getMessage());                	
                		if (exc instanceof NullPointerException || exc instanceof ArrayIndexOutOfBoundsException) {
                			exc.printStackTrace();
                		} 
                	}
                }
            }
        }
    }
    
    private void clearHistTable() {
        app.clear();
        current.setText("");
        histTableChanged();    	
    }
    
    private class Clearer implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        	clearHistTable();
        }
    }
    
    private class ProtoTable extends AbstractTableModel {
        public ProtoTable() {}
        
        public int getColumnCount(){return 4;}
        public int getRowCount(){return app.seq().length();}
        
        public String getColumnName(int index) {
            switch (index) {
                case 0: return "#";
                case 1: return "Stimulus";
                case 2: return "Response";
                case 3: return "Row";
            }
            throw new IllegalArgumentException("Illegal column #" + index);
        }
        
        public Object getValueAt(int row, int column){
            switch (column) {
                case 0: return row + 1;
                case 1: return app.seq().stimulusAt(row);
                case 2: return app.seq().responseAt(row);
                case 3: return app.seq().rowAt(row);
            }
            throw new IllegalArgumentException("Illegal column #" + column);
        }
    }

    private class Quitter implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            for (TabCloseListener tcl: tabCloseListeners) {
                tcl.closeTab(getOuterPanel());
            }
        }
    }
    
    public static AnimationPanel make(File f) {
        BlackBoxApp bba = new BlackBoxApp();
        bba.load(f);
        if (!bba.lastOpSucceeded()) {
            System.out.println(bba.lastOpMessage());
            System.exit(1);
        }
        return new AnimationPanel(bba.getBlackBox());
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: AnimationPanel blackbox.bbx");
            System.exit(1);
        }
        
        AnimationPanel ap = AnimationPanel.make(new File(args[0]));
        
        JFrame frame = new JFrame();
        frame.setTitle("Prototype");
        frame.setSize(800, 500);
        frame.setContentPane(ap);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void boxChanged(BlackBox box) {
    	clearHistTable();
        app.boxChanged(box);
        macroValuesModel.boxChanged(box);
        replaceStimulusButtons();
    }
    
    public void replaceStimulusButtons() {
    	stimPanel.removeAll();
        addStimulusButtons();
    }
}
