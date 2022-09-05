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

import java.io.*;
import java.util.*;
import javax.swing.*;

// This program is a state-model for a Black Box application.

public class BlackBoxApp {
    private BlackBox bb, lastSaved;
    private BlackBoxParser bbParser;
    private HistoryPatternParser hp;
    private boolean succeeded, needsSaving;
    private String msg;
    private File currentFile;
    
    private UndoStack<BlackBox> undo, redo;
    private final static int undoLimit = 100;
    
    private ConsistencyThread checkConsist;
    private CompletenessThread checkComplete;
    private IndependenceThread checkDisjoint;
    private Map<String,ExprThread> checkMacro;
    private Map<HistoryPattern,GeneratorThread> generators;
    
    private List<BoxChangeListener> changeListeners;
    private List<ErrorListener> errorListeners;
    private List<BoxSaveListener> saveListeners;
    
    public BlackBoxApp() {
        resetBox();
    	//lastSaved = bb = BlackBox.makeBasicBox();
        lastSaved = bb = new BlackBox();
        bbParser = new BlackBoxParser();
        hp = new HistoryPatternParser();
        changeListeners = new LinkedList<BoxChangeListener>();
        errorListeners = new LinkedList<ErrorListener>();
        saveListeners = new LinkedList<BoxSaveListener>();
    }
    
    public void resetBox() {
        lastSaved = bb = new BlackBox();
        succeeded = true;
        msg = "";
        currentFile = null;
        needsSaving = false;
        
        undo = new UndoStack<BlackBox>();
        undo.setLimit(undoLimit);
        
        redo = new UndoStack<BlackBox>();
        redo.setLimit(undoLimit);
        
        checkConsist = null;
        checkComplete = null;
        checkDisjoint = null;
        checkMacro = new LinkedHashMap<String,ExprThread>();
        generators = new LinkedHashMap<HistoryPattern,GeneratorThread>();
    }
    
    public void addBoxChangeListener(BoxChangeListener bcl) {
        changeListeners.add(bcl);
    }
    
    public void addErrorListener(ErrorListener el) {
        errorListeners.add(el);
    }

    public void addBoxSaveListener(BoxSaveListener bsl) {
        saveListeners.add(bsl);
    }
    
    public void load(File f) {
        try {
            stopSearching();
            bbParser.read(f, this);
            currentFile = f;
            clearUndoInfo();
            lastSaved = bb;
            needsSaving = false;
        } catch (IOException ioe) {
            failure(ioe.getMessage());
        } catch (IllegalArgumentException iae) {
            failure(iae.getMessage());
        }
    }
    
    public File currentFile() {return currentFile;}
    
    public boolean needsSaving() {return needsSaving;}
    
    public void save(File f) {
        try {
            bbParser.write(f, bb);
            if (!f.equals(currentFile)) {
                currentFile = f;
            }
            needsSaving = false;
            lastSaved = bb;
            for (BoxSaveListener bsl: saveListeners) {bsl.boxSaved(this);}

        } catch (IOException ioe) {
            failure(ioe.getMessage());
        }
    }
    
    public boolean lastOpSucceeded() {return succeeded;}
    
    public String lastOpMessage() {return msg;}
    
    private void success() {
        succeeded = true;
        msg = "Success";
    }
    
    private void failure(String errorMsg) {
        succeeded = false;
        msg = errorMsg;
        for (ErrorListener el: errorListeners) {el.errorMessage(errorMsg);}
    }
    
    public BlackBox getBlackBox() {return bb;}
    
    public String getBlackBoxStr() {return bbParser.toFileText(bb);}
    
    public void addRow(String hist, String stim, String resp) {
        HistoryPattern sh = parseHistory(hist);
        if (sh != null) {
            if (sh.isError()) {
                failure(sh.getErrorMsg());
            } else {
                changeBlackBox(bb.withRow(sh, stim, resp));
            }
        }
    }
    
    public void swapRows(int row1, int row2) {
        changeBlackBox(bb.rowsSwapped(row1, row2));
        success();
    }

    public void deleteEntireRow(int row) {
        changeBlackBox(bb.withoutRow(row));
        success();
    }
    
    public void renameStimulus(String oldName, String newName) {
    	changeBlackBox(bb.withRenamedStimulus(oldName, newName));
    	success();
    }
    
    public void renameResponse(String oldName, String newName) {
    	changeBlackBox(bb.withRenamedResponse(oldName, newName));
    	success();
    }
    
    public void renameMacro(String oldName, String newName) {
    	changeBlackBox(bb.withRenamedMacro(oldName, newName));
    	success();
    }
    
    public void replaceStimuliAt(Stimuli s, int row) {
        changeBlackBox(bb.stimuliReplacedAt(s, row));
        success();
    }
    
    public void replaceResponsesAt(Responses r, int row) {
        changeBlackBox(bb.responsesReplacedAt(r, row));
        success();
    }
    
    public void addStimulusTo(String stim, int row) {
        changeBlackBox(bb.withStimulusAt(stim, row));
        success();
    }
    
    public void addResponseTo(String resp, int row) {
        changeBlackBox(bb.withResponseAt(resp, row));
        success();
    }
    
    public void delStimulus(String stim) {
        changeBlackBox(bb.withoutStim(stim));
        success();
    }
    
    public void delResponse(String resp) {
        changeBlackBox(bb.withoutResp(resp));
        success();
    }
    
    public void delStimulusFrom(String stim, int row) {
        replaceStimuliAt(bb.stimuliAt(row).without(stim, bb), row);
    }
    
    public void delResponseFrom(String resp, int row) {
        replaceResponsesAt(bb.responseAt(row).without(resp), row);
    }
    
    public void deleteMacro(String macro) {
    	changeBlackBox(bb.withoutMacro(macro));
    	success();
    }
    
    public void setHistoryAt(String hist, int row) {
        HistoryPattern sh = parseHistory(hist);
        if (sh != null) {
            if (sh.isError()) {
                failure(sh.getErrorMsg());
            } else {
                changeBlackBox(bb.historyReplacedAt(sh, row));
                success();
            }
        }
    }
    
    public void setHistoryAt(HistoryPattern sh, int row) {
        success();
        changeBlackBox(bb.historyReplacedAt(sh, row));
    }
    
    public void addStimulus(String stim, String def) {
        changeBlackBox(bb.withStimDef(stim, def));
        success();
    }
    
    public void addResponse(String resp, String def) {
        changeBlackBox(bb.withRespDef(resp, def));
        success();
    }
    
    public HistoryPattern parseHistory(String input) {
        try {
            return hp.parse(input);
        } catch (IOException ioe) {
            failure(ioe.getMessage());
            return null;
        }
    }
    
    public void addMacro(String name, String def) {
        addMacro(name, parseHistory(def));
    }
    
    public void addMacro(String name, HistoryPattern sh) {
        if (sh != null) {
            if (sh.isError()) {
                failure(sh.getErrorMsg());
            } else {
                changeBlackBox(bb.withMacroDef(name, sh));
                success();
            }
        }
    }
    
    public void generateSequences(HistoryPattern target, int n, GeneratorThreadListener gtl) {
        stopGeneratingFor(target);
        GeneratorThread g = new GeneratorThread(new HeuristicSearcher(bb, target), n);
        generators.put(target, g);
        g.addGeneratorThreadListener(gtl);
        g.start();
    }
    
    public void checkMacro(String name, SearchListener<String> app) {
        stopMacroCheck(name);
        ExprThread checker = new ExprThread(bb, bb.macroDefinition(name));
        checkMacro.put(name, checker);
        checker.addSearchListener(app);
        checker.start();
    }
    
    public void checkConsistency(SearchListener<Integer> app) {
        stopConsistencyCheck();
        checkConsist = new ConsistencyThread(bb);
        checkConsist.addSearchListener(app);
        checkConsist.start();
    }
    
    public void checkDisjunction(SearchListener<String> app) {
        stopDisjunctionCheck();
        checkDisjoint = new IndependenceThread(bb);
        checkDisjoint.addSearchListener(app);
        checkDisjoint.start();
    }
    
    public void checkCompleteness(SearchListener<String> app) {
        stopCompletenessCheck();
        checkComplete = new CompletenessThread(this);
        checkComplete.addSearchListener(app);
        checkComplete.start();
    }
    
    public void stopSearching() {
        stopConsistencyCheck();
        stopCompletenessCheck();
        stopDisjunctionCheck();
        stopMacroCheck();
        stopGenerating();
    }
    
    public void stopConsistencyCheck() {
        if (checkConsist != null) {checkConsist.terminate();}
    }
    
    public void stopCompletenessCheck() {
        if (checkComplete != null) {checkComplete.terminate();}
    }
    
    public void stopDisjunctionCheck() {
        if (checkDisjoint != null) {checkDisjoint.terminate();}
    }
    
    public void stopMacroCheck(String name) {
        ExprThread checker = checkMacro.get(name);
        if (checker != null) {checker.terminate();}
    }
    
    public void stopMacroCheck() {
        for (String name: checkMacro.keySet()) {stopMacroCheck(name);}
    }
    
    public void stopGeneratingFor(HistoryPattern target) {
        GeneratorThread g = generators.get(target);
        if (g != null) {g.terminate();}
    }
    
    public void stopGenerating() {
        for (HistoryPattern target: generators.keySet()) {
            stopGeneratingFor(target);
        }
    }
    
    public void clearUndoInfo() {
        undo.clear();
    }
    
    public void undo() {
        if (!undo.empty()) {
            redo.push(bb);
            bb = undo.pop();
            updateNeedsSaving();
        }
    }
    
    public void redo() {
        if (!redo.empty()) {
            undo.push(bb);
            bb = redo.pop();
            updateNeedsSaving();
        }
    }
    
    private void changeBlackBox(BlackBox updated) {
    	stopSearching();
        if (!bb.equals(updated)) {
            undo.push(bb);
            redo.clear();
            bb = updated;
            updateNeedsSaving();
        }
    }
    
    private void updateNeedsSaving() {
        needsSaving = !bb.equals(lastSaved);
        for (BoxChangeListener bcl: changeListeners) {bcl.boxChanged(bb);}
        for (BoxSaveListener bsl: saveListeners) {
        	if (needsSaving()) {
        		bsl.boxNeedsSaving(this);
        	} else {
        		bsl.boxSaved(this);
        	}
        }
    }
    
    public void startFrame(JPanel content, String filename, String title, int width, int height) {
        load(new File(filename));
        if (lastOpSucceeded()) {
            JFrame frame = new JFrame();
            frame.setContentPane(content);
            frame.setTitle(title);
            frame.setSize(width, height);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        } else {
            System.out.println(lastOpMessage());
            System.exit(1);
        }
    }
}
