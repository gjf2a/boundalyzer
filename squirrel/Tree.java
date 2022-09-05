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
// along with Squirrel.  If not, see <http://www.gnu.org/licenses/>..

package squirrel;
import java.util.ArrayList;

abstract public class Tree {
    private String name;
    private Input input;
    private int start, length;
    
    /*[this.name(), this.input(), this.start(), this.length() := 
       name, input, start, length]*/
    public Tree(String name, Input in, int start, int length) {
        this.name = name;
        input = in;
        this.start = start;
        this.length = length;
    }
    
    public boolean matchesMoreThan(Tree that) {
        return that == null || this.end() > that.end();
    }
    
    public String name() {return name;}
    
    public Input input() {return input;}
    
    public int line() {
        return (start < input.length()) ? input.lineNumberAt(start) : 0;
    }
    
    public int start() {return start;}
    
    public int end() {return start + length - 1;}
    
    public int length() {return length;}
        
    public boolean isLeaf() {return numChildren() == 0;}
    
    abstract public boolean isError();
    
    /*[isError() => return := error message
      | true => throws := IllegalStateException]*/
    abstract public String errorMessage();
    
    public boolean isEmpty() {
        return !isError() && length() == 0;
    }
    
    /*[return := original input]*/
    public String toString() {
        return input().stringAt(start(), length());
    }
    
    public int numChildren() {return 0;}
    
    public int numNamed(String name) {return 0;}
    
    /*[0 <= n < numChildren() => return := child n
      | true => throws exception]*/
    public Tree nthChild(int n) {
        throw new UnsupportedOperationException("Tree " + this + " has no children");
    }
    
    public Tree lastChild() {
        return nthChild(numChildren() - 1);
    }
    
    /*[numNamed(name) > 0 => return := first child with name
      | true => throws exception]*/
    public Tree namedChild(String name) {
        return namedChild(name, 0);
    }
    
    /*[0 <= n < numNamed(name) => return := child n with name
      | true => throws exception]*/
    public Tree namedChild(String name, int n) {
        throw new UnsupportedOperationException("Tree " + this + " has no children");
    }
    
    /*[return := a preorder-traversal parse tree]*/
    public String textTree() {
        StringBuilder sb = new StringBuilder();
        appendPreorderString(sb, 0);
        return sb.toString();
    }
    
    /*[sb != null => sb := sb ++ a textual description of this
      | true => throws := exception]*/
    abstract protected void appendTreeNodeStr(StringBuilder sb);
    
    protected void appendPreorderString(StringBuilder sb, int indent) {
        appendTabStr(sb, indent);
        appendTreeNodeStr(sb);
        sb.append('\n');
        for (int c = 0; c < numChildren(); ++c) {
            nthChild(c).appendPreorderString(sb, indent + 1);
        }
    }
    
    private void appendTabStr(StringBuilder sb, int indent) {
        while (indent > 0) {
            sb.append('\t');
            --indent;
        }
    }
    
    public Tree wrapped(String wrapperName) {
        ArrayList<Tree> wrapChildren = new ArrayList<Tree>();
        wrapChildren.add(this);
        return new Interior(wrapperName, input(), start(), length(), wrapChildren);
    }
}
