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

public class Input {
    private String input;
    private int[] lineNumbers;
    
    public Input(String s) {
        input = s;
        lineNumbers = new int[length()];
        int currentLine = 1;
        for (int i = 0; i < length(); ++i) {
            lineNumbers[i] = currentLine;
            if (endsLine(i)) {++currentLine;}
        }
    }
    
    public int length() {return input.length();}
    
    public boolean legal(int i) {
        return i >= 0 && i < length();
    }
    
    /*[legal(i) => return := char at i
      | true => throws := exception]*/
    public char charAt(int i) {return input.charAt(i);}
    
    /*[legal(i) => return := line number at which charAt(i) is found
      | true => throws := exception]*/
    public int lineNumberAt(int i) {return lineNumbers[i];}
    
    /*[!legal(i) => throws := exception
      | charAt(i) is an endline => return := true 
      | true => return := false]*/
    public boolean endsLine(int i) {
        return charAt(i) == '\n';
    }
    
    /*[legal(i) => return := position of first character on the line containing i
      | true => throws := exception]*/
    public int startOfLine(int i) {
        int pos = i;
        while (pos > 0 && !endsLine(pos - 1)) {
            pos -= 1;
        }
        return pos;
    }

    /*[!legal(i) => throws := exception
      | endsLine(j) is false for all j such that i <= j < length() => return := length() - 1
      | no endline character exists after i => return := index of final character
      | true => return := next endline character at or after i]*/
    public int endOfLine(int i) {
        if (!legal(i)) {throw new IllegalArgumentException(i + " is out of bounds");}
        
        int pos = i;
        while (pos + 1 < length() && !endsLine(pos)) {
            pos += 1;
        }
        return pos;
    }
    
    public String toString() {return input;}
    
    /*[0 <= start and start + length < length() => return := substr(start, length)
      | true => throws := exception]*/
    public String stringAt(int start, int length) {
        return input.substring(start, start + length);
    }
    
    /*[0 <= start => return := substr(start, end of input)
      | true => throws := exception]*/
    public String stringAt(int start) {
        return input.substring(start);
    }
    
    /*[legal(start) => return := line segment from start to endOfLine(start)
      | true => return := ""]*/
    public String toLineEnd(int start) {
        return legal(start) ? stringAt(start, 1 + endOfLine(start) - start) : "";
    }
    
    /*[legal(start) => return := entire line including start 
      | true => throws := exception]*/
    public String lineContaining(int start) {
        int first = startOfLine(start);
        int length = 1 + endOfLine(start) - first;
        return stringAt(first, length);
    }
}
