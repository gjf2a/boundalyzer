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

package util;

public class SharedTreeSet<T extends Comparable<? super T>> extends SharedTreeMap<T,String> implements SharedSet<T> {
    public SharedTreeSet() {super();}
    
    public static void main(String[] args) {
        SharedSet<String> root = new SharedTreeSet<String>();
        for (int i = 0; i < args.length; i++) {
            root = root.with(args[i]);
        }
        
        System.out.println("Tree:");
        System.out.println(root);
        
        System.out.println("Ascending order:");
        for (String s: root) {System.out.print(s + " ");}
        System.out.println();
        
        System.out.println("contains() test:");
        for (int i = 0; i < args.length; ++i) {
            if (root.contains(args[i])) {
                System.out.println(args[i] + " found");
            } else {
                System.out.println(args[i] + " not found");
            }
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendSelfKey(sb, 0);
        return sb.toString();
    }
    /*
    public String inOrder() {
        StringBuilder sb = new StringBuilder();
        for (T key: this) {
            sb.append("[");
            sb.append(key);
            sb.append("]");
        }
        return sb.toString();
    }
    */
}
