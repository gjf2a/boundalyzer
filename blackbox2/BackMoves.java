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

public class BackMoves {
    private BlackBox box;
    private SortedSet<Integer> strongPref, preferred, permitted, forbidden;
    private final static boolean debug = false, debugNull = false;
    
    private BackMoves(BlackBox box) {
        this.box = box;
        strongPref = new TreeSet<Integer>();
        preferred = new TreeSet<Integer>();
        forbidden = new TreeSet<Integer>();
        permitted = new TreeSet<Integer>();
    }
    
    // Invariant: blackBox().responseAt(row).isImpossible() => forbids(row)
    
    public static BackMoves makeForbidAll(BlackBox box) {
        BackMoves result = new BackMoves(box);
        for (int i = 0; i < box.numRows(); ++i) {
            result.forbid(i);
        }
        return result;
    }
    
    public static BackMoves makePermitAll(BlackBox box) {
        BackMoves result = new BackMoves(box);
        if (debugNull) {System.out.println("box: " + box);}
        for (int i = 0; i < box.numRows(); ++i) {
            result.permit(i);
        }
        return result;
    }
    
    public static BackMoves makePreferAll(BlackBox box) {
        BackMoves result = new BackMoves(box);
        for (int i = 0; i < box.numRows(); ++i) {
            result.prefer(i);
        }
        return result;
    }
    
    public BlackBox blackBox() {return box;}
    
    public void stronglyPrefer(int row) {
        prefer(row);
        if (prefers(row)) {strongPref.add(row);}
    }

    // Pre: None
    // Post: prefers(row); !permits(row); !forbids(row)
    public void prefer(int row) {
        if (box.responseAt(row).isImpossible()) {
            forbid(row);
        } else {
            preferred.add(row);
            strongPref.remove(row);
            forbidden.remove(row);
            permitted.remove(row);
        }
    }
    
    // Pre: None
    // Post: permits(row); !prefers(row); !forbids(row)
    public void permit(int row) {
        if (box.responseAt(row).isImpossible()) {
            forbid(row);
        } else {
            preferred.remove(row);
            forbidden.remove(row);
            strongPref.remove(row);
            permitted.add(row);
        }
    }
    
    // Pre: None
    // Post: forbids(row); !permits(row); !prefers(row)
    public void forbid(int row) {
        preferred.remove(row);
        strongPref.remove(row);
        permitted.remove(row);
        forbidden.add(row);
    }
    
    public void preferNothing() {
        for (int i = 0; i < box.numRows(); ++i) {
            if (prefers(i)) {permit(i);}
        }
    }
    
    public Set<Integer> allPreferred() {
        return preferred;
    }
    
    public Set<Integer> allPermitted() {
        return permitted;
    }
    
    public Set<Integer> allNotForbidden() {
        Set<Integer> result = new TreeSet<Integer>();
        result.addAll(preferred);
        result.addAll(permitted);
        return result;
    }
    
    public Set<Integer> allForbidden() {
        return forbidden;
    }
    
    public boolean stronglyPrefers(int row) {return strongPref.contains(row);}
    
    public boolean prefers(int row) {return preferred.contains(row);}
    
    public boolean permits(int row) {return permitted.contains(row);}
    
    public boolean forbids(int row) {return forbidden.contains(row);}
    
    public int maxRow() {
        int max = 0;
        if (preferred.size() > 0) {max = Math.max(max, preferred.last());}
        if (permitted.size() > 0) {max = Math.max(max, permitted.last());}
        if (forbidden.size() > 0) {max = Math.max(max, forbidden.last());}
        return max;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Preferred:");
        for (int row: preferred) {
            sb.append(" ");
            sb.append(row);
            if (strongPref.contains(row)) {sb.append("*");}
        }
        
        sb.append(" Permitted:");
        for (int row: permitted) {
            sb.append(" ");
            sb.append(row);
        }
        
        sb.append(" Forbidden:");
        for (int row: forbidden) {
            sb.append(" ");
            sb.append(row);
        }
        return sb.toString();
    }
    
    // Pre: oneBack.blackBox() == twoBack.blackBox()
    // Post: None
    private static void checkBoxes(BackMoves a, BackMoves b) {
        if (a.blackBox() != b.blackBox()) {
            throw new IllegalArgumentException("Underlying black boxes differ");
        }
    }
    
    // Pre: oneBack.blackBox() == twoBack.blackBox()
    // Post: All "permitteds" in either are permitted in the result;
    //       All "preferreds" in either are preferred in the result;
    //       If a row is both permitted and preferred, it will be preferred
    //       in the result.  (I think this is to help prune the search.)
    //       All "forbiddens" in both are forbidden in the result
    public static BackMoves union(BackMoves oneBack, BackMoves twoBack) {
        checkBoxes(oneBack, twoBack);
        BackMoves combo = new BackMoves(oneBack.blackBox());
        for (int row: oneBack.allForbidden()) {combo.forbid(row);}
        for (int row: twoBack.allForbidden()) {combo.forbid(row);}
        for (int row: oneBack.allPermitted()) {combo.permit(row);}
        for (int row: twoBack.allPermitted()) {combo.permit(row);}
        for (int row: oneBack.allPreferred()) {combo.prefer(row);}
        for (int row: twoBack.allPreferred()) {combo.prefer(row);}
        return combo;
    }
    
    // Pre: oneBack.blackBox() == twoBack.blackBox()
    // Post: All "permitteds" in both are permitted in the result;
    //       All "preferreds" in either are preferred in the result;
    //       All "forbiddens" in either are forbidden in the result;
    //       If a "preferred" and "forbidden" conflict, then ALL 
    //       rows are forbidden!
    public static BackMoves intersection(BackMoves oneBack, BackMoves twoBack) {
        checkBoxes(oneBack, twoBack);
        BackMoves combo = new BackMoves(oneBack.blackBox());
        
        if (debug) {
            System.out.println("oneBack: " + oneBack);
            System.out.println("twoBack: " + twoBack);
        }
        
        for (int row: oneBack.allPermitted()) {combo.permit(row);}
        for (int row: twoBack.allPermitted()) {combo.permit(row);}
        
        if (debug) {
            System.out.println("Permitted intersection: " + combo);
        }
        
        for (int row: oneBack.allForbidden()) {combo.forbid(row);}
        for (int row: twoBack.allForbidden()) {combo.forbid(row);}
        
        if (debug) {
            System.out.println("Permitted * forbidden: " + combo);
        }
        
        combo = withPrefsFrom(oneBack, combo, twoBack);
        combo = withPrefsFrom(twoBack, combo, oneBack);
        
        if (debug) {
            System.out.println("Permitted * forbidden * preferred: " + combo);
            try {
                throw new IllegalStateException();
            } catch (IllegalStateException e) {
                System.out.println("Called from: ");
                e.printStackTrace();
            }
        }
        
        return combo;
    }
    
    private static BackMoves withPrefsFrom(BackMoves back, BackMoves combo, BackMoves other) {
        boolean strongKilled = false;
        for (int row: back.allPreferred()) {
            if (back.stronglyPrefers(row)) {
                if (combo.forbids(row)) {
                    strongKilled = true;
                } else {
                    if (other.stronglyPrefers(row)) {
                        combo.stronglyPrefer(row);
                    } else {
                        combo.prefer(row);
                    }
                }
            } else {
                if (!combo.forbids(row)) {
                    combo.prefer(row);
                }
            }
        }
        if (strongKilled) {
            for (int i = 0; i <= combo.maxRow(); ++i) {
                combo.forbid(i);
            }
        } 
        return combo;
    }
    /*
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: BackMoves blackbox.html rule");
            System.exit(1);
        }
        
        BlackBox bb = BlackBox.makeFrom(new File(args[0]));
        StimulusHistory rule = bb.parseHistory(args[1]);
        System.out.println(rule.backwards());
    }
    */
}
