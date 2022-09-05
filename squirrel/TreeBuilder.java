// Copyright 2007 by Gabriel J. Ferrer
//
// This program is part of the Squirrel project.
// 
// Squirrel is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as 
// published by the Free Software Foundation, either version 3 of 
// the License, or (at your option) any later version.
//
// Squirrel is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with Squirrel.  If not, see <http://www.gnu.org/licenses/>.

package squirrel;
import java.util.*;

public class TreeBuilder extends RecursiveNonterminal {
    
    public TreeBuilder(String name, String[][] symbols, Grammar memberOf) {
        super(name, symbols, memberOf);
    }
    
    public Tree makeFinalTree(ArrayList<Tree> symbolTrees, ResultTable results, int inputStart, int charsMatched) {
        Interior result = new Interior(name(), results.getInput(), 
                                       inputStart, charsMatched, symbolTrees);
        return isLeftAssociative() ? result.getLeftRotated() : result;
    }
}
