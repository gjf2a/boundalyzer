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

import java.awt.Color;
import java.util.*;

public class ViewMacroModel extends gui.GridAdapter implements BoxChangeListener {
	private ArrayList<MacroRecord> rowToMacro;
	
	private class MacroRecord {
		public MacroRecord(String name, boolean matches) {
			macroName = name;
			isMatch = matches;
		}
		public String macroName;
		public boolean isMatch;
	}
	
	public ViewMacroModel(BlackBox box) {
		boxChanged(box);
	}
	
	public void updateSequence(StimulusSeq seq) {
		for (MacroRecord mr: rowToMacro) {
			HistoryPattern matcher = seq.getBox().macroDefinition(mr.macroName);
			mr.isMatch = matcher.matches(seq);
		}
	}

	public Object at(int col, int row) {
		if (col == 0) {
			return rowToMacro.get(row).macroName;
		} else {
			return Boolean.toString(rowToMacro.get(row).isMatch);
		}
	}

	public int numColumns() {
		return 2;
	}

	public int numRows() {
		return rowToMacro.size();
	}

	public Color textColorOf(int col, int row) {
		if (col == 0) {
			return super.textColorOf(col, row);
		} else {
			return rowToMacro.get(row).isMatch ? Color.green : Color.red;
		}
	}

	public void boxChanged(BlackBox box) {
		rowToMacro = new ArrayList<MacroRecord>();
		StimulusSeq seq = new StimulusSeq(box);
		for (String macro: box.allMacros()) {
			rowToMacro.add(new MacroRecord(macro, box.macroDefinition(macro).matches(seq)));
		}
	}
}
