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
import java.util.ArrayList;
import util.*;

@SuppressWarnings("serial")
public class HistoryEditorPanel extends JPanel {
    private BlackBoxApp app;
    private JTextArea editor = new JTextArea(5, 20);
    private JTextArea errors = new JTextArea(5, 20);
    private JButton checkSyntax = new JButton("Check Syntax");
    private JButton commit = new JButton("Commit History Changes");
    private JButton consistency = new JButton("Check Consistency");
    private JButton makeExamples = new JButton("Generate Examples");
    private JButton stopSearch = new JButton("Stop Search");
    private boolean initialTextSet = false;
    
    protected Font courier = new Font("Courier", Font.PLAIN, 12);

    public HistoryEditorPanel(BlackBoxApp app) {
        super();
        this.app = app;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        addHeaders();
        add(new JScrollPane(editor));
        
        JPanel checkBtns = new JPanel();
        checkBtns.setLayout(new FlowLayout());
        checkBtns.add(checkSyntax);
        checkBtns.add(commit);
        add(checkBtns);
        
        JPanel searchBtns = new JPanel();
        searchBtns.setLayout(new FlowLayout());
        searchBtns.add(consistency);
        searchBtns.add(makeExamples);
        searchBtns.add(stopSearch);
        add(searchBtns);
        
        add(new JScrollPane(errors));
        
        editor.setFont(courier);
        errors.setFont(courier);
        errors.setEditable(false);

        Checker c = new Checker();
        checkSyntax.addActionListener(c);
        commit.addActionListener(c);
        consistency.addActionListener(c);
        makeExamples.addActionListener(c);
        stopSearch.addActionListener(c);
    }
    
    protected String getEditorText() {return editor.getText();}
    
    protected void setEditorText(String text) {
        editor.setText(text);
    }
    
    protected void commit(HistoryPattern parsed) {}
    
    protected void addHeaders() {}
    
    protected boolean hasInitialText() {return initialTextSet;}
    
    protected void setInitialText() {setEditorText(""); initialTextSet = true;}
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!initialTextSet) {
            setInitialText();
            initialTextSet = true;
        }
    }

    protected void reportError(String msg) {
        errors.setText(msg);
    }

    protected void clearErrors() {
        errors.setText("");
    }
    
    private class Checker implements ActionListener, GeneratorThreadListener, SearchListener<Integer> {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == stopSearch) {
                app.stopGenerating();
            } else {
                HistoryPattern result = app.parseHistory(editor.getText());
                if (app.lastOpSucceeded()) {
                    if (result.isError()) {
                        errors.setText(result.toString());
                    } else {
                        SharedSet<String> invalidSymbols = result.invalidSymbols(app.getBlackBox());
                        if (invalidSymbols.size() == 0) {
                            if (e.getSource() == checkSyntax) {
                                errors.setText("Syntax is fine");
                            } else if (e.getSource() == commit) {
                                commit(result);
                            } else if (e.getSource() == makeExamples) {
                                try {
                                    app.stopGenerating();
                                    int numNeeded = getUserInt("Enter number of examples sought");
                                    errors.setText("Generating " + numNeeded + " sequences...");
                                    app.generateSequences(result, numNeeded, this);
                                } catch (NumberFormatException nfe) {
                                    errors.setText("Sorry, no number received");
                                }
                            } else if (e.getSource() == consistency) {
                                app.checkConsistency(this);
                                errors.setText("Checking consistency...");
                            }
                        } else {
                            errors.setText("Unbound symbols:");
                            for (String unbound: invalidSymbols) {
                                errors.append(" ");
                                errors.append(unbound);
                            }
                        }
                    }
                } else {
                    errors.setText(app.lastOpMessage());
                }
            }
        }
        
        public void report(SearchData<Integer> data) {
            errors.setText("Nodes expanded: " + data.getNodesExpanded());
            errors.append("\n" + data.toString());
        }
        
        public void reportFromGenerator(ArrayList<StimulusSeq> generated) {
            errors.setText("Number of sequences generated: " + generated.size());
            for (StimulusSeq seq: generated) {
                errors.append("\n" + seq.toString());
            }
        }

		public void errorMessage(String msg) {
			errors.setText("Error from search: " + msg);
		}
    }

    private int getUserInt(String requestMsg) throws NumberFormatException {
        String result = JOptionPane.showInputDialog(requestMsg);
        return Integer.parseInt(result);
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: HistoryEditorPanel blackbox.bbx");
            System.exit(1);
        }
        
        BlackBoxApp bba = new BlackBoxApp();
        HistoryEditorPanel hep = new HistoryEditorPanel(bba);
        bba.startFrame(hep, args[0], "Stimulus History Editor", 500, 500);
    }
}
