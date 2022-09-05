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
import util.*;

public class AllStimuli extends Stimuli {
    public AllStimuli() {super();}
    
    public SharedSet<String> stimuli(BlackBox bb) {
        return bb.allStimuli();
    }
    
    public Stimuli with(String stim) {return this;}
    
    public Stimuli without(String stim, BlackBox bb) {
        StimulusList result = new StimulusList();
        for (String s: stimuli(bb)) {
            if (!s.equals(stim)) {result = result.with(stim);}
        }
        return result;
    }
    
    public String toString() {return "all";}
    
    public boolean includes(String stim) {return true;}

	@Override
	public boolean includesByName(String stim) {
		return false;
	}
	
	public Stimuli withRenamedStimulus(String oldStim, String newStim) {
		return this;
	}
}
