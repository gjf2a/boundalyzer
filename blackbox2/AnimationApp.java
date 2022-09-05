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

public class AnimationApp implements BoxChangeListener {
    private BlackBox bb;
    private StimulusSeq seq;
    
    public AnimationApp(BlackBox bb) {
        this.bb = bb;
        clear();
    }
    
    public void clear() {seq = new StimulusSeq(bb);}
    
    public BlackBox blackBox() {return bb;}
    
    public StimulusSeq seq() {return seq;}
    
    public void stimulate(String stim) {
    	seq = seq.with(stim);
    }
    
    public void retract() {
    	seq = seq.withoutLast();
    }

    public void boxChanged(BlackBox box) {
        bb = box;
        clear();
    }
}
