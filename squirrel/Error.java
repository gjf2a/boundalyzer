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

public class Error extends Tree {
    private String msg;
    private int lineNumber;
    
    public Error(String name, Input in, int start, int length, String message) {
        super(name, in, start, length);
        msg = message;
        lineNumber = (start < in.length()) ? in.lineNumberAt(start) : 0;
    }
    
    public Error(String name, Error childError) {
        this(name, childError.input(), childError.start(), childError.length(), childError.msg);
    }
    
    public boolean isError() {return true;}
    
    public String errorMessage() {
        if (input().length() == 0) {
            return "Parse error: No input";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Parse error in line ");
        sb.append(lineNumber);
        sb.append(": ");
        sb.append(msg);
        sb.append('\n');
        
        if (input().legal(start())) {
            int first = input().startOfLine(start());
            int last = input().endOfLine(start());
            for (int i = first; i <= last; ++i) {
                sb.append(input().charAt(i));
            }
            if (!input().endsLine(last)) {sb.append('\n');}
            for (int i = first; i <= end(); ++i) {
                sb.append(' ');
            }
            sb.append("^\n");
        }
        
        return sb.toString();
    }
    
    protected void appendTreeNodeStr(StringBuilder sb) {
        sb.append(name());
        sb.append(": \"");
        sb.append(errorMessage());
        sb.append('"');
    }
}
