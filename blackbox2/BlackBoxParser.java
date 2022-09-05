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

import squirrel.*;
import grammars.StringLiteralGrammar;
import java.io.*;

public class BlackBoxParser implements HasGrammar {
    private Grammar g;
    
    public BlackBoxParser() {
        g = new Grammar();
        g.importStandard();
        g.importFrom(new StringLiteralGrammar().getGrammar());
        g.addRule("<box>", "[<sp>] <list> [<box>]");
        g.addRule("<list>", "{ [<sp>] [<contents>] [<sp>] } [<sp>]");
        g.addRule("<contents>", "<list> [<sp>] [<contents>]", 
                                "<atom> [<sp>] [<contents>]");
        g.importFrom(new WordGrammar());
        g.addToken("<atom>", "<string>", "<word>");
    }
    
    public Grammar getGrammar() {return g;}
    
    public void read(Object obj, BlackBoxApp bba) throws IOException {
        Parser p = new Parser(g, obj);
        p.parse();
        Tree t = p.results().bestTree();
        if (t.isError()) {
            throw new IllegalArgumentException(t.errorMessage());
        } else {
            bba.resetBox();
            walk(bba, p.results().bestTree());
        }
    }
    
    private void walk(BlackBoxApp bba, Tree t) {
        if (t.isEmpty()) {
            return;
        } else if (t.name().equals("<box>")) {
            walk(bba, t.namedChild("<list>"));
            walk(bba, t.namedChild("<box>"));
        } else if (t.name().equals("<list>")) {
            walk(bba, t.namedChild("<contents>"));
        } else if (t.name().equals("<contents>")) {
            String label = t.namedChild("<atom>").toString();
            Tree rest = t.namedChild("<contents>");
            if (label.equals("Stimuli")) {
                new StimulusWalker().walkPairs(bba, rest);
            } else if (label.equals("Responses")) {
                new ResponseWalker().walkPairs(bba, rest);
            } else if (label.equals("Macros")) {
                new MacroWalker().walkPairs(bba, rest);
            } else if (label.equals("Row")) {
                new RowWalker().walkPairs(bba, rest);
            } else {
                throw new IllegalArgumentException("Unrecognized label: " + label);
            }
        } else {
            throw new IllegalArgumentException("Unrecognized input: " + t.toString());
        }
    }
    
    public void write(File f, BlackBox bb) throws IOException {
        PrintWriter toFile = new PrintWriter(new FileWriter(f));
        toFile.print(toFileText(bb));
        toFile.close();
    }
    
    public String toFileText(BlackBox bb) {
        StringBuilder sb = new StringBuilder();
        appendStimuli(sb, bb);
        sb.append('\n');
        appendResponses(sb, bb);
        sb.append('\n');
        appendMacros(sb, bb);
        sb.append('\n');
        appendRows(sb, bb);
        return sb.toString();
    }
    
    private void appendStimuli(StringBuilder sb, BlackBox bb) {
        sb.append("{Stimuli");
        for (String stim: bb.allStimuli()) {
            sb.append(" {");
            appendEscaped(sb, stim);
            sb.append(" ");
            appendEscaped(sb, bb.stimDefinition(stim));
            sb.append("}");
        }
        sb.append("}");
    }
    
    private void appendResponses(StringBuilder sb, BlackBox bb) {
        sb.append("{Responses");
        for (String resp: bb.allResponses()) {
            sb.append(" {");
            appendEscaped(sb, resp);
            sb.append(" ");
            appendEscaped(sb, bb.respDefinition(resp));
            sb.append("}");
        }
        sb.append("}");
    }
    
    private void appendMacros(StringBuilder sb, BlackBox bb) {
        sb.append("{Macros ");
        for (String name: bb.allMacros()) {
            sb.append(" {");
            appendEscaped(sb, name);
            sb.append(" ");
            appendEscaped(sb, bb.macroDefinition(name).toString());
            sb.append("}");
        }
        sb.append("}");
    }
    
    private void appendRows(StringBuilder sb, BlackBox bb) {
        for (int row = 0; row < bb.numRows(); ++row) {
            sb.append("{Row");
            sb.append(" {History ");
            appendEscaped(sb, bb.historyAt(row).toString());
            sb.append("} {Stimulus ");
            appendEscaped(sb, bb.stimuliAt(row).toString());
            sb.append("} {Response ");
            appendEscaped(sb, bb.responseAt(row).toString());
            sb.append("}}\n");
        }
    }
    
    private void appendEscaped(StringBuilder esc, String s) {
        esc.append('"');
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': esc.append("\\\\"); break;
                case '\n': esc.append("\\n"); break;
                case '\t': esc.append("\\t"); break;
                case '"':  esc.append("\\\""); break;
                default:   esc.append(c);
            }
        }    
        esc.append('"');
    }
    
    private String unescape(String input) {
        if (input.length() == 0) {return input;}
        
        StringBuilder sb = new StringBuilder();
        if (input.charAt(0) == '"' && input.charAt(input.length() - 1) == '"') {
            input = input.substring(1, input.length() - 1);
        }
        int i = 0;
        while (i < input.length()) {
            if (i < input.length() - 1 && input.charAt(i) == '\\') {
                char coded = input.charAt(i+1);
                switch (coded) {
                    case 'n':  sb.append('\n'); break;
                    case 't':  sb.append('\t'); break;
                    case '\\': sb.append('\\'); break;
                    case '"':  sb.append('"'); break;
                    default: throw new IllegalStateException("Unrecognized escape sequence '" + input.substring(i,i+2) + "'");
                }
                i += 2;
            } else {
                sb.append(input.charAt(i));
                i += 1;
            }
        }
        return sb.toString();
    }
    
    abstract private class PairWalker {
        public void walkPairs(BlackBoxApp bba, Tree t) {
            if (!t.name().equals("<contents>")) {
                throw new IllegalArgumentException("Can't handle node-type " + t.name());
            }
            
            if (t.isEmpty()) {
                finish(bba);
            } else {
                Tree first = t.namedChild("<list>").namedChild("<contents>");
                String one = first.namedChild("<atom>").toString();
                String two = first.namedChild("<contents>").namedChild("<atom>").toString();
                recordPair(bba, one, two);
                walkPairs(bba, t.namedChild("<contents>"));
            }
        }
        
        abstract protected void recordPair(BlackBoxApp bba, String first, String second);
        abstract protected void finish(BlackBoxApp bba);
    }
    
    private class RowWalker extends PairWalker {
        private String hist, stim, resp;
        
        protected void finish(BlackBoxApp bba) {
        	stim = stim.trim();
        	resp = resp.trim();
        	// The "if" statement is intended to ignore output from an earlier buggy version.
        	if (stim.length() > 0 && resp.length() > 0) {
        		bba.addRow(hist, stim, resp);
        	}
        }
        
        protected void recordPair(BlackBoxApp bba, String one, String two) {
            if (one.equals("History")) {hist = unescape(two);}
            else if (one.equals("Response")) {resp = unescape(two);}
            else if (one.equals("Stimulus")) {stim = unescape(two);}
            else {
                throw new IllegalArgumentException("Unrecognized label " + one);
            }
        }
    }
    
    abstract private class DefineWalker extends PairWalker {
        protected void finish(BlackBoxApp bba) {}
        protected void recordPair(BlackBoxApp bba, String one, String two) {
            if (one.split("\\s").length != 1) {
                throw new IllegalArgumentException("Spaces not allowed inside \"" + one + "\"");
            } else if (!one.equals("\"\"")) {
            	// This ignores some output that was generated by an old buggy version
            	define(bba, unescape(one), unescape(two));
            }
        }
        abstract protected void define(BlackBoxApp bba, String name, String def);
    }
    
    private class StimulusWalker extends DefineWalker {
        protected void define(BlackBoxApp bba, String name, String def) {
            bba.addStimulus(name, def);
        }
    }

    private class ResponseWalker extends DefineWalker {
        protected void define(BlackBoxApp bba, String name, String def) {
            bba.addResponse(name, def);
        }
    }
    
    private class MacroWalker extends DefineWalker {
        protected void define(BlackBoxApp bba, String name, String def) {
            bba.addMacro(name, def);
        }
    }
}
