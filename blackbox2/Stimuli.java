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

abstract public class Stimuli {
    public Stimuli() {}
    
    public static Stimuli make(String s) {
        if (s.equals("all")) {
            return new AllStimuli();
        } else {
            StimulusList sl = new StimulusList();
            for (String stim: s.split("\\s")) {
                sl = sl.with(stim);
            }
            return sl;
        }
    }
    
    // Invariants:
    // \forall bb:BlackBox | bb.allStimuli().contains(stim) && stim:String, stimuli(bb).contains(stim) <=> includes(stim)
    // \forall stim:String, includesByName(stim) => includes(stim)
    // \forall stim:String, with(stim) => includesByName(stim)
    // \forall stim:String, without(stim) => !includes(stim)
    
    abstract public SharedSet<String> stimuli(BlackBox bb);
    
    abstract public Stimuli with(String stim);
    
    abstract public Stimuli without(String stim, BlackBox bb);
    
    abstract public String toString();
    
    abstract public boolean includes(String stim);
    
    abstract public boolean includesByName(String stim);
    
    // Pre: None
    // Post: includesByName(oldStim) => !includesByName(oldStim) && includesByName(newStim)
    abstract public Stimuli withRenamedStimulus(String oldStim, String newStim);
}
