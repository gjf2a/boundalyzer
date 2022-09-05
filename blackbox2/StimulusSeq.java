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
import util.FuncArray;

public class StimulusSeq {
    private BlackBox src;
    private FuncArray<Integer> rows;
    private FuncArray<String> stimuli;
    private int start, end;
    private String str;
    
    private static final boolean debug = false;
    
    public StimulusSeq(BlackBox src) {
        rows = new FuncArray<Integer>();
        stimuli = new FuncArray<String>();
        this.src = src;
        start = 0;
        end = -1;
    }
    
    private StimulusSeq(StimulusSeq prefix, int row, String stim) {
    	if (prefix.rows.size() > prefix.end + 1) {
    		rows = prefix.rows.set(prefix.end + 1, row);
    		stimuli = prefix.stimuli.set(prefix.end + 1, stim);
    	} else {
    		rows = prefix.rows.add(row);
    		stimuli = prefix.stimuli.add(stim);
    	}
        start = prefix.start;
        end = prefix.end + 1;
        src = prefix.src;
    }

    private StimulusSeq(StimulusSeq source, int subStart, int subEnd) {
        this.rows = source.rows;
        this.stimuli = source.stimuli;
        this.src = source.src;
        if (subStart > subEnd) {
            subEnd = subStart - 1;
        }
        this.start = source.start + subStart;
        this.end = source.start + subEnd;
    }
    
    public StimulusSeq prefixedBy(int row, String stim) {
        StimulusSeq seq = new StimulusSeq(src);
        seq.rows = seq.rows.add(row);
        seq.stimuli = seq.stimuli.add(stim);
        seq.end = 0;
        for (int i = start; i <= end; ++i) {
            seq.rows = seq.rows.add(rows.get(i));
            seq.stimuli = seq.stimuli.add(stimuli.get(i));
            seq.end += 1;
        }
        return seq;
    }
    
    public BlackBox getBox() {return src;}
    
    public boolean isImpossible() {
        for (int i = 0; i < length(); ++i) {
            if (responseAt(i).isImpossible()) {return true;}
        }
        return false;
    }
    
    public boolean isValid() {
        if (debug) {System.out.println(this + ".isValid(): length(): " + length());}
        for (int n = 0; n < length(); ++n) {
            StimulusSeq prefix = subseq(0, n - 1);
            String stim = stimulusAt(n);
            int boxRow = src.rowMatched(prefix, stim);
            if (debug) {
                System.out.println("prefix: " + prefix + " stim: " + stim + " row: " + boxRow + " rowAt(" + n + "): " + rowAt(n));
            }
            if (boxRow != rowAt(n)) {
                if (debug) {System.out.println("False: boxRow: " + boxRow + " rowAt(n): " + rowAt(n));}
                return false;
            }
        }
        if (debug) {System.out.println("Valid; returning true");}
        return true;
    }
    
    public StimulusSeq with(String stim) {
        if (isImpossible()) {
            return this;
        } else {
            int row = getBox().rowMatched(this, stim);
            if (!src.rowLegal(row)) {
                return this;
                //throw new IllegalArgumentException(stim + " is not a legal stimulus to add to " + this);
            }
            return new StimulusSeq(this, row, stim);
        }
    }
    
    public StimulusSeq withoutLast() {
    	return subseq(0, length() - 2);
    }
    
    public StimulusSeq subseq(int start, int end) {
        if (end >= length() || start < 0) {
            throw new IllegalArgumentException("Improper subsequence bounds [" + start + ", " + end + "]");
        }
        
        return new StimulusSeq(this, start, end);
    }
    
    public int length() {return end - start + 1;}
    
    private int pos(int n) {return n + start;}
    
    public String stimulusAt(int n) {
        try {
            return stimuli.get(pos(n));
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            aioobe.printStackTrace();
            System.out.println("start: " + start + " end: " + end + " length(): " + length());
            System.out.println("n: " + n + " pos(" + n + "): " + pos(n));
            for (int i = 0; i < stimuli.size(); ++i) {
                System.out.println("i: " + i + " stimuli.get(" + i + "): " + stimuli.get(i));
            }
            System.exit(1);
            return null;
        }
    }
    
    public Responses responseAt(int n) {
        return getBox().responseAt(rowAt(n));
    }
    
    public int rowAt(int n) {
        return rows.get(pos(n));
    }
    
    public Responses finalResponse() {
    	return responseAt(end);
    }
    
    public int finalRow() {return rowAt(end);}
    
    public String toString() {
        if (str == null) {
            if (length() == 0) {
                str = "Zero-length history";
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < length(); ++i) {
                	sb.append("[Row:");
                	if (rowAt(i) < 0) {
                		sb.append("Unspecified");
                	} else {
                		sb.append(rowAt(i));
                	}
                	sb.append(", Stimulus:");
                	sb.append(stimulusAt(i));
                	sb.append(", Response:");
                	sb.append(responseAt(i));
                	sb.append("]");
                	/*
                    sb.append("[");
                    sb.append(stimulusAt(i));
                    sb.append(": ");
                    if (rowAt(i) < 0) {
                        sb.append("Unspecified");
                    } else {
                        sb.append(rowAt(i));
                        sb.append(", ");
                        sb.append(responseAt(i));
                    }
                    sb.append("]");
                    */
                }
                str = sb.toString();
            }
        }
        
        return str;
    }
    
    public int hashCode() {return toString().hashCode();}
    
    // Primitive, but effective.
    public boolean equals(Object other) {
        return toString().equals(other.toString());
    }
}
