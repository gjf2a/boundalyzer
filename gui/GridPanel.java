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

package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class GridPanel extends JPanel {
    private GridModel model;
    private Dimension size;
    private int xSize, ySize, numHeaderRows;
    private boolean rowSwapper = false;
    private int highlitRow = -1;
    private ArrayList<GridPanelListener> listeners;
    
    private String[][] strings;
    private int ascent, lineHeight;
    private int[] maxHeights, maxWidths;
    
    private static final int cellPad = 2;
    private final static boolean debug = false;
    
    public GridPanel(GridModel gm) {
        super();
        numHeaderRows = 1;
        model = gm;
        Font courier = new Font("Courier", Font.PLAIN, 12);
        setFont(courier);
        setBackground(Color.white);
        computePreferredSize(this.getFontMetrics(courier));
        
        Mouser m = new Mouser();
        addMouseMotionListener(m);
        addMouseListener(m);
        listeners = new ArrayList<GridPanelListener>();
    }
    
    public void addGridPanelListener(GridPanelListener gpl) {
        listeners.add(gpl);
    }
    
    public Dimension getPreferredSize() {
        return size;
    }
    
    public void enableSwapRows() {rowSwapper = true;}
    
    public boolean canSwapRows() {return rowSwapper;}
    
    private void computePreferredSize(FontMetrics fm) {
    	strings = findStrings();
    	ascent = fm.getMaxAscent();
    	lineHeight = ascent + fm.getMaxDescent();
        maxHeights = findMaxHeights(lineHeight, strings);
        maxWidths = findMaxWidths(fm, strings);
        
        xSize = 0;
        for (int w: maxWidths) {xSize += w + cellPad * 2;}
        
        ySize = 0;
        for (int h: maxHeights) {ySize += h + cellPad * 2;}
        
        size = new Dimension(xSize, ySize);
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        computePreferredSize(g.getFontMetrics());
        if (size.height == 0) {return;}
        
        Color[][] highlights = findHighlights();
        int xIndent = 0;
        g.setColor(Color.black);
        g.drawLine(0, 0, 0, ySize);
        for (int col = 0; col < model.numColumns(); ++col) {
            int yBaseline = ascent;
            xIndent += cellPad;
            for (int row = 0; row < model.numRows(); ++row) {
                if (highlights[col][row] != null) {
                    if (debug) {System.out.println("Highlighting " + col + ", " + row);}
                    g.setColor(highlights[col][row]);
                    g.fillRect(xIndent, yBaseline - ascent, maxWidths[col] + cellPad * 2, maxHeights[row] + cellPad * 2);
                }
                
                g.setColor(model.textColorOf(col, row));
                yBaseline += cellPad;
                for (String line: strings[col][row].split("\\n")) {
                    g.drawString(line, xIndent, yBaseline);
                    yBaseline += lineHeight;
                }
                yBaseline += cellPad;
            }
            xIndent += maxWidths[col] + cellPad;
            g.setColor(Color.black);
            g.drawLine(xIndent, 0, xIndent, ySize);
        }
        
        int yStart = 0;
        for (int row = 0; row <= model.numRows(); ++row) {
            g.drawLine(0, yStart, xSize, yStart);
            if (row < maxHeights.length) {yStart += maxHeights[row] + cellPad*2;}
        }
        
        highlitRow = -1;
        
        revalidate();
    }
    
    private int[] findMaxHeights(int lineHeight, String[][] strings) {
        int[] maxHeights = new int[model.numRows()];
        for (int row = 0; row < model.numRows(); ++row) {
            maxHeights[row] = 0;
            for (int col = 0; col < model.numColumns(); ++col) {
                int numLines = strings[col][row].split("\\n").length;
                int height = numLines * lineHeight;
                if (height > maxHeights[row]) {maxHeights[row] = height;}
            }
        }
        return maxHeights;
    }
    
    private int[] findMaxWidths(FontMetrics fm, String[][] strings) {
        int[] maxWidths = new int[model.numColumns()];
        for (int col = 0; col < model.numColumns(); ++col) {
            maxWidths[col] = 0;
            for (int row = 0; row < model.numRows(); ++row) {
                int width = fm.stringWidth(strings[col][row]);
                if (width > maxWidths[col]) {maxWidths[col] = width;}
            }
        }
        return maxWidths;
    }
    
    private Color[][] findHighlights() {
        Color[][] c = new Color[model.numColumns()][model.numRows()];
        for (int col = 0; col < model.numColumns(); ++col) {
            for (int row = 0; row < model.numRows(); ++row) {
                if (model.highlighted(col, row)) {
                    c[col][row] = model.highlight(col, row);
                } else if (row > 0 && row == highlitRow) {
                    c[col][row] = Color.yellow;
                }
            }
        }
        return c;
    }
    
    private String[][] findStrings() {
        String[][] str = new String[model.numColumns()][model.numRows()];
        for (int col = 0; col < model.numColumns(); ++col) {
            for (int row = 0; row < model.numRows(); ++row) {
                str[col][row] = model.at(col, row).toString();
            }
        }
        return str;
    }
    
    // Mouse functionality:
    //
    // - Pressing down the mouse button and dragging will drag a 
    //   row to a new location in the table.  
    // - Moving the mouse over a row highlights the row
    // - Clicking on a cell triggers an event which is relayed to listeners
    private class Mouser implements MouseMotionListener, MouseListener {
        private int prevRow;
        
        private int whichRow(int y) {
            return model.numRows() * y / ySize;
        }
        
        private int whichCol(int x) {
            return model.numColumns() * x / xSize;
        }
        
        private boolean rowValid(int r) {
            return 0 <= r && r < model.numRows() - numHeaderRows;
        }
        
        // Invoked when a mouse button is pressed on a component and then dragged.
        public void mouseDragged(MouseEvent e) {
            if (canSwapRows()) {
                int row = whichRow(e.getY());
                int modelRow = row - numHeaderRows;
                int modelPrev = prevRow - numHeaderRows;
                if (row != prevRow && rowValid(modelRow) && rowValid(modelPrev)) {
                    model.swapRows(modelRow, modelPrev);
                }
                highlitRow = prevRow = row;
                repaint();
            }
        }
          
        // Invoked when the mouse cursor has been moved onto a component but no buttons have been pushed.
        public void mouseMoved(MouseEvent e) {
            if (canSwapRows() && ySize > 0) {
                highlitRow = whichRow(e.getY());
                prevRow = highlitRow;
                repaint();
            }
        }

        public void mouseClicked(MouseEvent click) {
            for (GridPanelListener gpl: listeners) {
                gpl.cellClicked(whichCol(click.getX()), whichRow(click.getY()));
            }
        }

        public void mouseEntered(MouseEvent arg0) {}
        public void mouseExited(MouseEvent arg0) {}
        public void mousePressed(MouseEvent arg0) {}
        public void mouseReleased(MouseEvent arg0) {}
    }
}
