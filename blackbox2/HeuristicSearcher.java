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
import java.io.*;

public class HeuristicSearcher {
    private HistoryPattern goal;
    private BlackBox box;
    private PriorityQueue<Node> queue;
    private Set<StimulusSeq> visited;
    private Node last;
    private int numExpanded;
    
    private static boolean debug = false;
    
    public HeuristicSearcher(BlackBox box, HistoryPattern goal) {
        this.box = box;
        this.goal = goal;
        startSearchFrom(new StimulusSeq(box));
    }
    
    private void enqueue(Node n) {
        if (n.isViable() && !visited.contains(n.prefix())) {
            queue.add(n);
            visited.add(n.prefix());
            if (debug) {
                System.out.println("Enqueuing: " + n.prefix() + " (" + n.distanceEstimate() + ")");
            }
        } else if (debug) {
            System.out.println("Killing: " + n.prefix() + " (" + n.distanceEstimate() + ")");
        }
    }
    
    public void startSearchFrom(StimulusSeq start) {
        queue = new PriorityQueue<Node>();
        visited = new HashSet<StimulusSeq>();
        last = null;
        numExpanded = 0;
        enqueue(new Node(start));
    }
    
    public boolean canExpand() {
        return queue.size() > 0;
    }
    
    // Pre: canExpand()
    // Post: numExpanded() = numExpanded() + 1
    public void expandNext() {
        Node next = queue.remove();
        last = next;
        numExpanded += 1;
        if (debug) {
            System.out.println("Dequeued " + last);
            //System.out.println("Estimate: " + next.distanceEstimate());
        }
        for (String stim: box.allStimuli()) {
            try {
                enqueue(new Node(stim, next));
            } catch (IllegalArgumentException iae) {
                System.out.println("Node creation failed; program continues");
            }
        }
    }
    
    protected StimulusSeq makeChildSeq(int row, String stim, StimulusSeq suffix) {
        return suffix.prefixedBy(row, stim);
    }
    
    public int numExpanded() {return numExpanded;}
    
    public int numEnqueued() {return visited.size();}
    
    public StimulusSeq last() {return last.prefix();}
    
    public int lastHeuristic() {return last.heuristicEstimate();}
    
    public int lastTotalDistance() {return last.distanceEstimate();}
    
    public boolean hasLast() {return last != null;}
    
    public boolean hasValidLast() {
        return hasLast() && goal.matches(last());
    }
    
    public BlackBox getBox() {return box;}
    
    private class Node implements Comparable<Node> {
        private StimulusSeq prefix;
        private int estimate;
        
        public Node(String stim, Node prev) {
            this(prev.prefix.with(stim));
        }

        private Node(StimulusSeq prefix) {
            this.prefix = prefix;
            if (debug) {
                System.out.println("Calculating estimate from " + prefix + " to " + goal);
            }
            ResponseEstimates respTable = new ResponseEstimates(prefix);
            respTable.estimateAllRows();
            if (debug) {
                System.out.println("Response estimates:");
                System.out.println(respTable);
            }
            estimate = goal.heuristic(prefix, respTable);
            if (debug) {
                System.out.println("Estimate for " + prefix +":" +estimate);
            }
        }
         
        public int heuristicEstimate() {
            return estimate;
        }
        
        // Post: Returns length of current suffix + estimate to goal,
        //       thus giving the total distance to the goal from the
        //       start.
        public int distanceEstimate() {
            if (heuristicEstimate() == Integer.MAX_VALUE) {
                return heuristicEstimate();
            } else {
                return prefix.length() + heuristicEstimate();
            }
        }
        
        public StimulusSeq prefix() {return prefix;}
        
        public boolean isViable() {
            return heuristicEstimate() < Integer.MAX_VALUE && heuristicEstimate() >= 0;
        }

        // NOTE: Inconsistent with equals()
        public int compareTo(Node that) {
            return this.distanceEstimate() - that.distanceEstimate();
        }
    }
    
    public void debug() {debug = true;}
    
    public static void main(String[] args) throws IOException, NumberFormatException {
        if (args.length < 2) {
            System.out.println("Usage: HeuristicSearcher blackbox.bbx (row | goalSet) [-stim startSequence] [-showall] [-n numberToGenerate]");
            System.exit(1);
        }
        
        BlackBoxApp bba = new BlackBoxApp();
        bba.load(new File(args[0]));
        if (!bba.lastOpSucceeded()) {
            System.out.println(bba.lastOpMessage());
            System.exit(1);
        }

        HistoryPattern hist = null;
        try {
            int row = Integer.parseInt(args[1]);
            if (row < 0 || row >= bba.getBlackBox().numRows()) {
                System.out.println("Illegal row " + row);
                System.exit(1);
            }
            hist = bba.getBlackBox().historyAt(row);
        } catch (NumberFormatException nfe) {
            hist = bba.parseHistory(args[1]);
        }
        
        if (hist == null || hist.isError()) {
            System.out.println(hist);
            System.exit(1);
        }
        
        System.out.println("Goal: " + hist);
        
        HeuristicSearcher searcher = new HeuristicSearcher(bba.getBlackBox(), hist);
        searchTest(searcher, args, 2);
    }
    
    public static void searchTest(HeuristicSearcher searcher, String[] args, int firstArg) throws NumberFormatException {
        int num = 1;
        for (int i = firstArg; i < args.length; ++i) {
            if (args[i].equals("-showall")) {
                searcher.debug();
                System.out.print("Arguments:");
                for (String arg: args) {
                    System.out.print(" " + arg);
                }
                System.out.println();
            } else if (args[i].equals("-n") && args.length >= i+2) {
                num = Integer.parseInt(args[i+1]);
            } else if (args[i].equals("-stim")) {
                StimulusSeq seq = new StimulusSeq(searcher.getBox());
                for (int j = i + 1; j < args.length && args[j].charAt(0) != '-'; ++j) {
                    String stim = args[j];
                    if (searcher.getBox().allStimuli().contains(stim)) {
                        seq = seq.with(args[j]);
                    } else {
                        System.out.println(stim + " is not defined in the black box");
                        System.exit(1);
                    }
                }
                searcher.startSearchFrom(seq);
            } else if (args[i].substring(0, 4).equals("-est")) {
                searcher.expandNext();
                System.out.println("Heuristic: " + searcher.lastHeuristic());
                System.out.println("Total distance: " + searcher.lastTotalDistance());
                System.exit(0);
            }
        }
        
        searchTest(searcher, num);
    }
    
    public static void searchTest(HeuristicSearcher searcher, int num) {
        int count = 0;
        int found = 0;
        while (searcher.canExpand() && found < num) {
            searcher.expandNext();
            count += 1;
            if (debug) {System.out.println("loop " + count);}
            if (searcher.hasValidLast()) {
                ++found;
                System.out.println("Result " + found + ": " + searcher.last());
            }
        }
        if (!searcher.hasLast()) {
            System.out.println("last() cannot be printed, as it is not present");
        }

        System.out.println("Total found: " + found);
        System.out.println("number of loops: " + count);
        System.out.println("total nodes enqueued: " + searcher.numEnqueued());
    }
}
