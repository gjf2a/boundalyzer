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

abstract public class HistThread extends SearchThread<String> {
    private Map<String,HeuristicSatSearcher> stimToSearcher;
    private int count;
    private Set<String> unknown, goodStimuli;
    private Map<String,StimulusSeq> badExamples;
    protected BlackBox box;
    
    private static final boolean debug = false;
    
    public HistThread(BlackBoxApp boxApp) {
        super();
        box = boxApp.getBlackBox();
        badExamples = new TreeMap<String,StimulusSeq>();
        stimToSearcher = new TreeMap<String,HeuristicSatSearcher>();
        unknown = new TreeSet<String>();
        goodStimuli = new TreeSet<String>();
        count = 0;
        
        Set<String> unspecified = box.unusedStimuli();
        
        for (String stim: box.allStimuli()) {
            if (unspecified.contains(stim)) {
                badExamples.put(stim, new StimulusSeq(box));
            } else {
                stimToSearcher.put(stim, makeSearcher(boxApp, stim));
                unknown.add(stim);
            }
        }
    }

    // **Bug**: When dealing with a stimulus with incomplete histories,
    // the example of an "uncovered history" may itself not be valid.  Hence,
    // it may fail to be found by search(), which then quits when it can no
    // longer expand.
    public void search() {
        while (unknown.size() > 0 && !terminated()) {
            for (String stim: stimToSearcher.keySet()) {
                if (unknown.contains(stim)) {
                    HeuristicSatSearcher stimSearcher = stimToSearcher.get(stim);
                    if (stimSearcher.canExpand()) {
                        stimSearcher.expandNext();
                        count += 1;
                        if (stimSearcher.hasValidLast()) {
                            badExamples.put(stim, stimSearcher.last().with(stim));
                            unknown.remove(stim);
                        }
                    } else {
                        if (debug) {System.out.println(stim + " is good");}
                        goodStimuli.add(stim);
                        unknown.remove(stim);
                    }
                }
            }
        }
    }
    
    public SearchData<String> makeSearchData() {
        SearchData<String> data = new SearchData<String>();
        data.setNodesExpanded(count);
        data.addElements(goodTitle(), goodStimuli);
        data.addExamples(badTitle(), badExamples);
        data.addElements("Unknown Stimuli", unknown);
        data.setGoodNewsField(goodTitle(), goodNews());
        return data;
    }
    
    // Pre: box.allHistoriesFor(stim).size() >= 1
    abstract protected HeuristicSatSearcher makeSearcher(BlackBoxApp boxApp, String stim);
    abstract protected String goodTitle();
    abstract protected String badTitle();
    abstract protected String goodNews();
}
