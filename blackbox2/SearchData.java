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

public class SearchData<F> {
    private ArrayList<String> fields;
    private Map<String,Set<F>> fieldToElements;
    private Map<String,Map<F,StimulusSeq>> fieldToExamples;
    private int numNodesExpanded;
    private String goodNewsField, goodNews;
    
    public SearchData() {
        fields = new ArrayList<String>();
        fieldToElements = new HashMap<String,Set<F>>();
        fieldToExamples = new HashMap<String,Map<F,StimulusSeq>>();
        numNodesExpanded = 0;
        goodNewsField = goodNews = null;
    }
    
    public void addElement(String category, F element) {
        Set<F> elts = new HashSet<F>();
        elts.add(element);
        addElements(category, elts);
    }
    
    public void addElements(String category, Set<F> elements) {
        fields.add(category);
        fieldToElements.put(category, elements);
    }
    
    public void addExamples(String category, Map<F,StimulusSeq> examples) {
        fields.add(category);
        fieldToExamples.put(category, examples);
    }
    
    public Set<F> getElementsFor(String category) {
        return fieldToElements.get(category);
    }
    
    public int getCountFor(String category) {
        if (fieldToElements.containsKey(category)) {
            return getElementsFor(category).size();
        } else if (fieldToExamples.containsKey(category)) {
            return getExamplesFor(category).size();
        } else {
            return 0;
        }
    }
    
    public Map<F,StimulusSeq> getExamplesFor(String category) {
        return fieldToExamples.get(category);
    }
    
    public void setGoodNewsField(String field, String msg) {
        goodNewsField = field;
        goodNews = msg;
    }
    
    public int getNodesExpanded() {return numNodesExpanded;}
    
    public void setNodesExpanded(int n) {numNodesExpanded = n;}
        
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isAllGood()) {
            sb.append(goodNews);
            sb.append("\n");
        } else {
            for (String category: fields) {
                if (fieldToElements.containsKey(category)) {
                    Set<F> elements = fieldToElements.get(category);
                    if (elements.size() > 0) {
                        sb.append(category);
                        sb.append(":");
                        for (F element: elements) {
                            sb.append(" ");
                            sb.append(element.toString());
                        }
                    } else {
                        sb.append("No ");
                        sb.append(category);
                    }

                } else if (fieldToExamples.containsKey(category)) {
                    Map<F,StimulusSeq> examples = fieldToExamples.get(category);
                    if (examples.size() > 0) {
                        sb.append(category);
                        sb.append(":\n");
                        for (F key: examples.keySet()) {
                        	sb.append("Counterexample for ");
                            sb.append(key.toString());
                            sb.append(": ");
                            sb.append(examples.get(key).toString());
                            sb.append("\n");
                        }
                    } else {
                        sb.append("No ");
                        sb.append(category);
                    }

                } else {
                    throw new IllegalStateException("This should never happen; category = " + category);
                }
                sb.append("\n");
            }
        }
        sb.append("Nodes expanded: " + getNodesExpanded() + "\n");
        return sb.toString();
    }
    
    private boolean isAllGood() {
        boolean allGood = true;
        for (String field: fields) {
            boolean isGoodField = field.equals(goodNewsField);
            boolean isEmpty = (getCountFor(field) == 0);
            if ((isGoodField && isEmpty) || (!isGoodField && !isEmpty)) {
                allGood = false;
            }
        }
        return allGood;
    }
}
