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
import squirrel.*;

public class NameChecker implements HasGrammar {
	private Grammar g;
	private String lastMsg;
	
	public NameChecker() {
		g = new WordGrammar();
		lastMsg = "";
	}
	
	public boolean nameIsValid(String name) {
		if (name.length() < 1) {
			return false;
		}
		
		Parser p = new Parser(g, name);
		p.parse();
		if (p.bestTree().isError()) {
			lastMsg = p.bestTree().errorMessage();
			return false;
		} else {
			lastMsg = "";
			return true;
		}
	}
	
	public String getLastMessage() {
		return lastMsg;
	}

	public Grammar getGrammar() {
		return g;
	}
}
