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
import java.awt.Color;

public interface GridModel {
    public int numRows();
    
    public int numColumns();
    
    public Object at(int col, int row);
    
    public boolean highlighted(int col, int row);
    
    public Color highlight(int col, int row);
    
    public Color textColorOf(int col, int row);
    
    public void swapRows(int row1, int row2);
}
