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

public class Interior extends Tree {
    private ArrayList<Tree> children;
    private HashMap<String,ArrayList<Tree>> nameToChild;
    private boolean isError;
    private String errorMsg;
    
    /*[numChildren(), nthChild(n), isError() := children.size(), children.get(n), true only if any children are errors]*/
    public Interior(String name, Input in, int start, int length, ArrayList<Tree> children) {
        super(name, in, start, length);
        this.children = children;
        isError = false;
        nameToChild = new HashMap<String,ArrayList<Tree>>();
        for (Tree t: children) {
            if (!nameToChild.containsKey(t.name())) {
                nameToChild.put(t.name(), new ArrayList<Tree>());
            }
            nameToChild.get(t.name()).add(t);
            if (t.isError()) {
                isError = true;
                errorMsg = t.errorMessage();
            }
        }
    }
    
    public boolean isLeaf() {return false;}
    
    public boolean isError() {return isError;}
    
    public String errorMessage() {
        if (isError) {
            return errorMsg;
        } else {
            throw new IllegalStateException("Not an error");
        }
    }

    public Tree getLeftRotated() {
    	Tree rightMost = children.get(children.size() - 1);
    	if (rightMost.isError()) {
    		return rightMost;
    	} else if (!(rightMost instanceof Interior)) {
    		throw new IllegalStateException("Cannot left-rotate " + this.textTree());
    	} else if (!rightMost.name().equals(name())) {
            return this;
        } else if (rightMost.numChildren() < 2) {
            ArrayList<Tree> newNode = new ArrayList<Tree>();
            newNode.add(children.get(0).wrapped(name()));
            for (int i = 1; i < children.size() - 1; ++i) {
                newNode.add(children.get(i));
            }
            newNode.add(rightMost.nthChild(0));
            return new Interior(name(), input(), start(), length(), newNode);
        } else {
            ArrayList<Tree> newChild = new ArrayList<Tree>();
            for (int i = 0; i < children.size() - 1; ++i) {
                newChild.add(children.get(i));
            }
            newChild.add(rightMost.nthChild(0));
            Interior partialRotated = new Interior(name(), input(), start(), length(), newChild);
            Tree newChildTree = partialRotated.getLeftRotated();
            
            ArrayList<Tree> newParent = new ArrayList<Tree>();
            newParent.add(newChildTree);
            for (int i = 1; i < rightMost.numChildren(); ++i) {
                newParent.add(rightMost.nthChild(i));
            }
            return new Interior(name(), input(), start(), length(), newParent);
        }
    }
    
    public int numChildren() {return children.size();}
    
    public Tree nthChild(int c) {
        return children.get(c);
    }
    
    public int numNamed(String name) {
        return nameToChild.containsKey(name) ? nameToChild.get(name).size() : 0;
    }
    
    /*[0 <= n < numNamed(name) => return := child n with name
      | true => throws := exception]*/
    public Tree namedChild(String name, int n) {
        return nameToChild.get(name).get(n);
    }
    
    protected void appendTreeNodeStr(StringBuilder sb) {
        sb.append(name());
    }
}
