package blackbox2;

import squirrel.*;
import java.io.IOException;

//Copyright 2009 by Gabriel J. Ferrer
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

public class HistoryPatternParser implements HasGrammar {
    private Grammar g;
    private static boolean debug = false;
    private final static boolean induceBug = false;
    
    public HistoryPatternParser() {
        g = new Grammar();
        g.importFrom(new Standard());
        g.addRule("<start>", "<boolExpr>");
        g.addRule("<boolExpr>", "[<sp>] <orExpr> [<sp>]");
        g.addRule("<orExpr>", "<orExpr> <or> <andExpr>", "<andExpr>");
        g.addRule("<andExpr>", "<andExpr> <and> <notExpr>", "<notExpr>");
        g.addRule("<notExpr>", "[<not>] <keywordExpr>");
        g.addRule("<keywordExpr>", "[<keyword>] <seqExpr>");
        g.addRule("<seqExpr>", "<compareExpr> : <boolExpr>", "<compareExpr>");
        g.addRule("<compareExpr>", "<numExpr> [<sp>] <compareOp> [<sp>] <numExpr>", "<boolValue>");
        g.addRule("<boolValue>", "( <boolExpr> )", "<responseHistory>");
        g.addRule("<responseHistory>", "[<responds>] <word>");

        g.addRule("<numExpr>", "[<sp>] <addExpr> [<sp>]");
        g.addRule("<addExpr>", "<addExpr> [<sp>] <addOp> [<sp>] <unaryExpr>", "<unaryExpr>");
        g.addRule("<unaryExpr>", "- [<sp>] <numValue>", "<numValue>");
        g.addRule("<numValue>", "( <numExpr> )", "<computedNum>");
        g.addRule("<computedNum>", "count <sp> <boolValue>", "<simpleNum>");
        
        g.addToken("<simpleNum>", "<nonzero> <digits>", "<digit>");
        g.addToken("<digits>", "<digit> [<digits>]");
        g.addCharRange("<nonzero>", '1', '9');
        
        g.importFrom(new WordGrammar());
        
        g.addToken("<or>", "<sp> or <sp>");
        g.addToken("<and>", "<sp> and <sp>");
        g.addToken("<not>", "not <sp>");
        
        g.addToken("<keyword>", "<kword> <sp>");
        g.addToken("<kword>", "includes", "oneof", "only");
        g.addToken("<responds>", "response <sp>");
        
        g.addToken("<addOp>", "+", "-");
        if (induceBug) {
            g.addToken("<compareOp>", "<=", ">=", "==", "=", "!=", "<", ">");
        } else {
            g.addCharClass("<bang>", '!');
            g.addToken("<compareOp>", "<=", ">=", "==", "=", "<bang> =", "<", ">");
        }
    }
    
    public Grammar getGrammar() {return g;}
    
    public HistoryPattern parse(Object obj) throws IOException {
        Parser p = new Parser(g, obj);
        if (debug) {p.debugMode();}
        Tree parsed = p.bestTree();
        //if (parsed.isError()) {System.out.println("Error with " + parsed + "; " + parsed.errorMessage());} else {System.out.println("No error here...");}
        HistoryPattern hist = walk(parsed);
        return hist.isError() ? hist.findError() : hist;
    }
    
    public void debugMode() {debug = true;}
    
    public static void main(String[] args) throws IOException {
        HistoryPatternParser hp = new HistoryPatternParser();
        if (args.length > 1) {hp.debugMode();}
        HistoryPattern hist = hp.parse(args[0]);
        System.out.println(hist);
    }
    
    private HistoryPattern walk(Tree t) {
        if (t.isError()) {
            return new PatternError(t.errorMessage());
            
        } else if (t.numChildren() == 1) {
            return walk(t.nthChild(0));
            
        } else if (t.name().equals("<start>")) {
            return walk(t.namedChild("<boolExpr>"));
            
        } else if (t.name().equals("<boolExpr>")) {
            return walk(t.namedChild("<orExpr>"));
            
        } else if (t.name().equals("<orExpr>")) {
            return new UnionMatcher(walk(t.nthChild(0)), walk(t.nthChild(2)));          
            
        } else if (t.name().equals("<andExpr>")) {
            return new IntersectMatcher(walk(t.nthChild(0)), walk(t.nthChild(2)));
            
        } else if (t.name().equals("<notExpr>")) {
            HistoryPattern arg = walk(t.nthChild(1));
            return t.nthChild(0).isEmpty() ? arg : new NegatePattern(arg);
            
        } else if (t.name().equals("<keywordExpr>")) {
            HistoryPattern arg = walk(t.namedChild("<seqExpr>"));
            
            if (t.namedChild("<keyword>").isEmpty()) {
                return arg;
            } else {
                String key = t.namedChild("<keyword>").toString().trim();
                if (key.equals("includes")) {
                    return new IncludesMatcher(arg);
                } else if (key.equals("oneof")) {
                    return new OneOfHistory(arg);
                } else if (key.equals("only")) {
                    return new OnlyHistory(arg);
                } else {
                    return new PatternError("Unrecognized keyword " + key);
                }
            }
            
        } else if (t.name().equals("<seqExpr>")) {
            return new SequenceMatcher(walk(t.nthChild(0)), walk(t.nthChild(2)));
            
        } else if (t.name().equals("<compareExpr>")) {
            return new ComparePattern(numWalk(t.namedChild("<numExpr>")), 
                                      numWalk(t.namedChild("<numExpr>", 1)), 
                                      t.namedChild("<compareOp>").toString().trim());
            
        } else if (t.name().equals("<boolValue>")) {
            return new ParenHistory(walk(t.namedChild("<boolExpr>")));
            
        } else if (t.name().equals("<responseHistory>")) {
            Tree argTree = t.namedChild("<word>");
            if (t.namedChild("<responds>").isEmpty()) {
                return walk(argTree);
            } else {
                return new ResponseMatcher(argTree.toString());
            }
            
        } else if (t.name().equals("<word>")) {
            String word = t.toString();
            if (word.equals("any")) {
                return new AnyMatcher();
            } else if (word.equals("stim")) {
                return new SingleInteractionMatcher();
            } else if (word.equals("none")) {
                return new NoStimuli();
            } else {
                return new SymbolMatcher(word);
            }
            
        } else {
            throw new IllegalArgumentException(t.name() + " is not recognized");
        }
    }
    
    private CountingPattern numWalk(Tree t) {
        if (t.isError()) {
            return new CountingPatternError(t.errorMessage());
            
        } else if (t.numChildren() == 1) {
            return numWalk(t.nthChild(0));
            
        } else if (t.name().equals("<numExpr>")) {
            return numWalk(t.namedChild("<addExpr>"));
            
        } else if (t.name().equals("<addExpr>")) {
            String op = t.namedChild("<addOp>").toString();
            CountingPattern left = numWalk(t.namedChild("<addExpr>"));
            CountingPattern right = numWalk(t.namedChild("<unaryExpr>"));
            if (op.equals("+")) {
                return new AddOp(left, right);
            } else if (op.equals("-")) {
                return new SubOp(left, right);
            } else {
                return new CountingPatternError(op + " is not recognized");
            }
            
        } else if (t.name().equals("<unaryExpr>")) {
            return new UnaryMinus(numWalk(t.namedChild("<numValue>")));
            
        } else if (t.name().equals("<numValue>")) {
            return new NumParenPattern(numWalk(t.namedChild("<numExpr>")));

        } else if (t.name().equals("<computedNum>")) {
            return new CountPattern(walk(t.namedChild("<boolValue>")));
            
        } else if (t.name().equals("<simpleNum>")) {
            return new NumConstPattern(Integer.parseInt(t.toString()));
            
        } else {
            throw new IllegalArgumentException(t.name() + " is not recognized");
        }
    }
}
