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

public class BlackBoxModel extends gui.GridAdapter {
    private BlackBoxApp bba;
    
    public BlackBoxModel(BlackBoxApp bba) {this.bba = bba;}
    
    public int numRows() {return bba.getBlackBox().numRows() + 1;}
    
    public int numColumns() {return 4;}
    
    public Object at(int col, int row) {
        row -= 1;
        if (row < 0) {
            switch (col) {
                case 0: return "Row";
                case 1: return "History Pattern";
                case 2: return "Stimulus";
                case 3: return "Response";
                default: throw new IllegalArgumentException("Illegal column number " + col);
            }
        } else {
            switch (col) {
                case 0: return Integer.toString(row);
                case 1: return bba.getBlackBox().historyAt(row);
                case 2: return bba.getBlackBox().stimuliAt(row);
                case 3: return bba.getBlackBox().responseAt(row);
                default: throw new IllegalArgumentException("Illegal column number " + col);
            }
        }
    }
    
    public void swapRows(int row1, int row2) {
        bba.swapRows(row1, row2);
    }
}
