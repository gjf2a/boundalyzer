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

import java.util.*;

public class GeneratorThread extends Thread {
    private HeuristicSearcher searcher;
    private int count, numToFind;
    private boolean stop;
    private ArrayList<StimulusSeq> generated;
    private ArrayList<GeneratorThreadListener> listeners = new ArrayList<GeneratorThreadListener>();
    
    public GeneratorThread(HeuristicSearcher g, int numToFind) {
        searcher = g;
        this.numToFind = numToFind;
        count = 0;
    }
    
    public void terminate() {
        stop = true;
    }
    
    public void addGeneratorThreadListener(GeneratorThreadListener gtl) {
        listeners.add(gtl);
    }
    
    public void run() {
        count = 0;
        generated = new ArrayList<StimulusSeq>(numToFind);
        while (!stop && searcher.canExpand() && generated.size() < numToFind) {
            searcher.expandNext();
            count += 1;
            if (searcher.hasValidLast()) {
                generated.add(searcher.last());
            }
        }
        sendReport();
    }
    
    public int getNumAttempts() {return count;}
    
    public void sendReport() {
        for (GeneratorThreadListener gtl: listeners) {
            gtl.reportFromGenerator(generated);
        }
    }
}
