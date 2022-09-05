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
import java.util.ArrayList;

abstract public class SearchThread<F> extends Thread {
    private ArrayList<SearchListener<F>> listeners;
    private boolean stop;
    
    public SearchThread() {
        listeners = new ArrayList<SearchListener<F>>();
        stop = false;
    }
    
    public void terminate() {stop = true;}
    
    public boolean terminated() {return stop;}

    public void addSearchListener(SearchListener<F> sl) {
        listeners.add(sl);
    }
    
    public void informListeners() {
        for (SearchListener<F> sl: listeners) {
            sl.report(makeSearchData());
        }
    }
    
    public void reportError(String msg) {
    	for (SearchListener<F> sl: listeners) {
			sl.errorMessage(msg);
		}
    }
    
    public void run() {
    	//try {
    		search();
    		informListeners();
    	//} catch (Exception e) {
    	//	System.out.println("here:");
    	//	e.printStackTrace();
    	//	reportError(e.getMessage());
     	//}
    }
    
    abstract protected void search();
    
    abstract public SearchData<F> makeSearchData();
}
