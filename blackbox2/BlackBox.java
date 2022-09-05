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
import util.*;

public class BlackBox {
    private FuncArray<Stimuli> stimuli;
    private FuncArray<Responses> responses;
    private FuncArray<HistoryPattern> histories;
    private SharedMultiMap<String,Integer> stimToRow, respToRow;
    private SharedMap<String,String> stimToDesc, respToDesc;
    private SharedMap<String,HistoryPattern> macros;
    private NameChecker nameChecker;
    
    public BlackBox() {
        stimuli = new FuncArray<Stimuli>();
        responses = new FuncArray<Responses>();
        histories = new FuncArray<HistoryPattern>();
        stimToRow = respToRow = null;
        stimToDesc = respToDesc = new SharedTreeMap<String,String>();
        macros = new SharedTreeMap<String,HistoryPattern>();
        
        nameChecker = new NameChecker();
        
        respToDesc = respToDesc.with("Impossible", "Placeholder for an impossible situation");
        respToDesc = respToDesc.with("None", "No response");
    }
    
    private BlackBox(BlackBox that) {
        stimuli = that.stimuli;
        responses = that.responses;
        histories = that.histories;
        stimToRow = null;
        respToRow = null;
        stimToDesc = that.stimToDesc;
        respToDesc = that.respToDesc;
        macros = that.macros;
        nameChecker = that.nameChecker;
    }
    
    // This seemed like a good idea at one time.
    // The concept was that students would have a functioning black box
    // available for testing their first macros.
    // The downside is that this is unhelpful for testing any macros
    // that use responses.  Furthermore, it had some weird bugs.
    public static BlackBox makeBasicBox() {
    	return new BlackBox().withRow(new AnyMatcher(), "all", "None");
    }
    
    public boolean equals(Object other) {
        if (other instanceof BlackBox) {
            BlackBox that = (BlackBox)other;
            return 
                (stimToDesc == that.stimToDesc || stimToDesc.equals(that.stimToDesc)) &&
                (respToDesc == that.respToDesc || respToDesc.equals(that.respToDesc)) &&                
                (stimuli    == that.stimuli    || stimuli.equals(that.stimuli)) &&
                (responses  == that.responses  || responses.equals(that.responses)) &&
                (histories  == that.histories  || histories.equals(that.histories)) &&
                (macros     == that.macros     || macros.equals(that.macros));
        } else {
            return false;
        }
    }
    
    public BlackBox optimizedLookup() {
        BlackBox result = new BlackBox(this);
        result.stimuli = stimuli.refreshed();
        result.responses = responses.refreshed();
        result.histories = histories.refreshed();
        return result;
    }
    
    public HistoryPattern historyAt(int row) {
        return histories.get(row);
    }
    
    public Stimuli stimuliAt(int row) {
        return stimuli.get(row);
    }
    
    public Responses responseAt(int row) {
        return responses.get(row);
    }

    public BlackBox withRow(HistoryPattern h, String s, String r) {
        return withRow(h, Stimuli.make(s), new Responses(r));
    }
    
    public BlackBox withRow(HistoryPattern h, Stimuli s, Responses r) {
        BlackBox result = new BlackBox(this);
        result.stimuli = stimuli.add(s);
        result.responses = responses.add(r);
        result.histories = histories.add(h);
        return result;
    }
    
    public BlackBox withoutRow(int row) {
        BlackBox result = new BlackBox(this);
        result.stimuli = stimuli.remove(row);
        result.responses = responses.remove(row);
        result.histories = histories.remove(row);
        return result;
    }
    
    public BlackBox historyReplacedAt(HistoryPattern h, int row) {
        BlackBox result = new BlackBox(this);
        result.histories = histories.set(row, h);
        return result;
    }
    
    public BlackBox stimuliReplacedAt(Stimuli s, int row) {
        BlackBox result = new BlackBox(this);
        result.stimuli = stimuli.set(row, s);
        return result;
    }
    
    public BlackBox responsesReplacedAt(Responses r, int row) {
        BlackBox result = new BlackBox(this);
        result.responses = responses.set(row, r);
        return result;
    }
    
    public BlackBox rowsSwapped(int row1, int row2) {
        BlackBox result = new BlackBox(this);
        result.stimuli = stimuli.swap(row1, row2);
        result.responses = responses.swap(row1, row2);
        result.histories = histories.swap(row1, row2);
        return result;
    }
    
    public BlackBox withStimulusAt(String s, int row) {
    	// A good idea but it is not tested
    	//return stimuliReplacedAt(stimuli.get(row).with(s), row);
        BlackBox result = new BlackBox(this);
        result.stimuli = stimuli.set(row, stimuli.get(row).with(s));
        return result;
    }
    
    public BlackBox withResponseAt(String r, int row) {
    	// A good idea but it is not tested
    	//return responsesReplacedAt(responses.get(row).with(r), row);
        BlackBox result = new BlackBox(this);
        result.responses = responses.set(row, responses.get(row).with(r));
        return result;
    }
    
    // Pre: seq.from() == this
    // Post: Returns lowest value of row such that stimulusAt(row) == s and historyAt(row).matches(seq);
    //       Returns -1 if no such row exists
    public int rowMatched(StimulusSeq seq, String s) {
        if (seq.getBox() != this) {
            throw new IllegalArgumentException("seq.from() does not match this BlackBox");
        }

        for (int row: rowsMatchingStim(s)) {
        	try {
        		if (historyAt(row).matches(seq)) {return row;}
        	} catch (RuntimeException e) {
        		throw new ExceptionInRowException(e, row);
        	}
        }

        return -1;
    }
    
    public SharedSet<Integer> rowsWithResp(String r) {
        if (respToRow == null) {
            respToRow = new SharedMultiMap<String,Integer>();
            for (int row = 0; row < responses.size(); ++row) {
                for (String resp: responses.get(row).responses()) {
                    respToRow = respToRow.with(resp, row);
                }
            }
        }
        return respToRow.allFor(r);
    }
        
    public SharedSet<Integer> rowsMatchingStim(String s) {
        if (stimToRow == null) {
            stimToRow = new SharedMultiMap<String,Integer>();
            for (int row = 0; row < stimuli.size(); ++row) {
                for (String stim: stimuli.get(row).stimuli(this)) {
                    stimToRow = stimToRow.with(stim, row);
                }
            }
        }
        
        SharedSet<Integer> result = stimToRow.allFor(s);
        return (result == null) ? new SharedTreeSet<Integer>() : result;            
    }
    
    public SharedSet<Integer> rowsUsingMacro(String macro) {
    	SharedSet<Integer> result = new SharedTreeSet<Integer>();
    	for (int row = 0; row < numRows(); ++row) {
    		if (historyAt(row).macrosInUse(this).contains(macro)) {
    			result = result.with(row);
    		}
    	}
    	return result;
    }
    
    public SharedSet<String> macrosUsingMacro(String macro) {
    	SharedSet<String> result = new SharedTreeSet<String>();
    	for (String otherMacro: allMacros()) {
    		if (macroDefinition(otherMacro).macrosInUse(this).contains(macro)) {
    			result = result.with(otherMacro);
    		}
    	}
    	return result;
    }

    public SharedSet<String> allMacrosInUse() {
    	SharedSet<String> result = new SharedTreeSet<String>();
    	for (int row = 0; row < numRows(); ++row) {
    		result = result.with(historyAt(row).macrosInUse(this));
    	}
    	
    	for (String macro: allMacros()) {
    		result = result.with(macroDefinition(macro).macrosInUse(this));
    	}
    	return result;
    }
    
    public SharedSet<String> allStimuli() {return stimToDesc.keySet();}
    
    public SharedSet<String> allResponses() {return respToDesc.keySet();}
    
    public SharedSet<String> allMacros() {return macros.keySet();}
    
    private void checkStimulusName(String newName) {
    	if (allMacros().contains(newName)) {
    		throw new InvalidNameException(newName, " duplicates macro name");
    	}
    	checkName(newName);
    }
    
    private void checkMacroName(String newName) {
    	if (allStimuli().contains(newName)) {
    		throw new InvalidNameException(newName, " duplicates stimulus name");
    	}
    	checkName(newName);
    }
    
    private void checkName(String newName) {
    	if (!nameChecker.nameIsValid(newName)/* || allStimuli().contains(newName) || allResponses().contains(newName) || allMacros().contains(newName)*/) {
    		if (newName.equals("Impossible") || newName.equals("None")) {
    			return;
    		}
    		
    		String msg = nameChecker.getLastMessage();
       		// HACK ALERT:
    		// Magic number cuts out a lot of garbage from the message
    		if (msg.length() > 30) {
    			msg = msg.substring(0, 29);
    		}
    		if (msg.length() == 0) {
    			msg = "Invalid name";
    		}
    		throw new InvalidNameException(newName, msg);
    	}
    }

    public BlackBox withStimDef(String stim, String def) {
    	checkStimulusName(stim);
    	BlackBox result = new BlackBox(this);
    	result.stimToDesc = stimToDesc.with(stim, def);
    	return result;    	
    }

    // Pre: rowsWithStim(stim).size() == 0
    public BlackBox withoutStim(String stim) {
    	if (!unusedStimuli().contains(stim)) {
            int row = rowsMatchingStim(stim).iterator().next();
            throw new IllegalStateException("Cannot remove stimulus " + stim + "; in use in row " + row);
        }
        
        BlackBox result = new BlackBox(this);
        result.stimToDesc = stimToDesc.without(stim);
        return result;
    }
    
    public BlackBox withRespDef(String resp, String def) {
    	checkName(resp);
        BlackBox result = new BlackBox(this);
        result.respToDesc = respToDesc.with(resp, def);
        return result;
    }
    
    // Pre: rowsWithResp(resp) == 0
    public BlackBox withoutResp(String resp) {
        if (rowsWithResp(resp).size() > 0) {
            int row = rowsWithResp(resp).iterator().next();
            throw new IllegalStateException("Cannot remove response " + resp + "; in use in row " + row);
        }

        BlackBox result = new BlackBox(this);
        result.respToDesc = respToDesc.without(resp);
        return result;
    }
    
    public String stimDefinition(String stim) {
        return stimToDesc.get(stim);
    }
    
    public String respDefinition(String resp) {
        return respToDesc.get(resp);
    }
    
    public HistoryPattern macroDefinition(String name) {
        return macros.get(name);
    }
    
    public BlackBox withMacroDef(String name, HistoryPattern def) {
    	checkMacroName(name);
        BlackBox result = new BlackBox(this);
        result.macros = macros.with(name, def);
        return result;
    }

    public BlackBox withRenamedStimulus(String oldName, String newName) {
    	checkName(newName);
    	BlackBox result = withRenamedSymbol(oldName, newName);
    	result.stimToDesc = stimToDesc.withReplacedKey(oldName, newName);
    	result.stimToRow = (stimToRow == null) ? null : stimToRow.withReplacedKey(oldName, newName);
    	result.stimuli = new FuncArray<Stimuli>();
    	for (int row = 0; row < stimuli.size(); ++row) {
    		result.stimuli = result.stimuli.add(this.stimuliAt(row).withRenamedStimulus(oldName, newName));
    	}
    	return result;
    }
    
    public BlackBox withRenamedResponse(String oldName, String newName) {
    	checkName(newName);
    	BlackBox result = withRenamedSymbol(oldName, newName);
    	result.respToDesc = respToDesc.withReplacedKey(oldName, newName);
    	result.respToRow = (respToRow == null) ? null : respToRow.withReplacedKey(oldName, newName);
    	result.responses = new FuncArray<Responses>();
    	for (int row = 0; row < responses.size(); ++row) {
    		result.responses = result.responses.add(this.responseAt(row).withRenamedResponse(oldName, newName));
    	}
    	return result;
    }
    
    public BlackBox withRenamedMacro(String oldName, String newName) {
    	checkName(newName);
    	BlackBox result = withRenamedSymbol(oldName, newName);
    	result.macros = macros.withReplacedKey(oldName, newName);
    	return result;
    }
    
    private BlackBox withRenamedSymbol(String oldName, String newName) {
    	BlackBox result = new BlackBox(this);
    	result.histories = new FuncArray<HistoryPattern>();
    	for (int row = 0; row < this.histories.size(); ++row) {
    		result.histories = result.histories.add((HistoryPattern)this.historyAt(row).withRenamedSymbol(this, oldName, newName));
    	}
    	result.macros = new SharedTreeMap<String,HistoryPattern>();
    	for (String macro: this.macros.keySet()) {
    		result.macros = result.macros.with(macro, (HistoryPattern)this.macros.get(macro).withRenamedSymbol(this, oldName, newName));
    	}
    	return result;
    }
    
    // Pre: !allMacrosInUse().contains(name)
    // Post: Returns this box without name
    public BlackBox withoutMacro(String name) {
    	if (!allMacrosInUse().contains(name)) {
    		BlackBox result = new BlackBox(this);
    		result.macros = macros.without(name);
    		return result;
    	} else {
    		// A better idea: 
    		// Have the constructor take a set of rows and a set of macros
    		// It can then report all users.
    		throw new MacroInUseException(name, macrosUsingMacro(name), rowsUsingMacro(name));
    	}
    }
    
    public int numRows() {return stimuli.size();}
    
    public boolean rowLegal(int row) {
        return row >= 0 && row < numRows();
    }
    
    public Set<String> unusedStimuli() {
        Set<String> result = new LinkedHashSet<String>();
        for (String stim: allStimuli()) {
        	boolean unused = true;
        	for (int row = 0; unused && row < numRows(); ++row) {
        		unused = !stimuliAt(row).includesByName(stim);
        	}
            if (unused) {result.add(stim);}
        }
        return result;
    }
}
