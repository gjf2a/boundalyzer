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

public class StimulusList extends Stimuli {
    private SharedSet<String> stimuli;
    private String str;
    
    public StimulusList() {
        super();
        stimuli = new SharedTreeSet<String>();
        str = null;
    }
    
    private StimulusList(SharedSet<String> stimuli) {
        this();
        this.stimuli = stimuli;
    }
    
    public SharedSet<String> stimuli(BlackBox bb) {return stimuli;}
    
    public StimulusList with(String stim) {
        return new StimulusList(stimuli.with(stim));
    }
    
    public StimulusList without(String stim, BlackBox bb) {
        return new StimulusList(stimuli.without(stim));
    }
    
    public Stimuli withRenamedStimulus(String oldStim, String newStim) {
    	if (includesByName(oldStim)) {
    		return new StimulusList(stimuli.without(oldStim).with(newStim));
    	} else {
    		return this;
    	}
    }
    
    public String toString() {
        if (str == null) {
            StringBuilder sb = new StringBuilder();
            for (String s: stimuli) {
                sb.append(s);
                sb.append(' ');
            }
            if (sb.length() >= 1) {
                sb.delete(sb.length() - 1, sb.length());
            }
            str = sb.toString();
        }
        return str;
    }
    
    public boolean includes(String stim) {
        return stimuli.contains(stim);
    }

	@Override
	public boolean includesByName(String stim) {
		return includes(stim);
	}
}
