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

import javax.swing.*;
import util.*;

@SuppressWarnings("serial")
public class ResponseEditorPanel extends StimRespEditorPanel {
    public ResponseEditorPanel(BlackBoxApp app) {
        super(app);
    }
    
    protected void addTitleTo(JPanel jp) {
        jp.add(new JLabel("Responses"));
    }
    
    protected SharedSet<String> nameSet() {
        return app().getBlackBox().allResponses();
    }
    
    protected String getDefinition() {
        return app().getBlackBox().respDefinition(currentName());
    }
    
    protected void update(String name, String def) {
        app().addResponse(name, def);
    }
    
    protected void remove(String name) {
        app().delResponse(name);
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: ResponseEditorPanel blackbox.bbx");
            System.exit(1);
        }
        
        BlackBoxApp bba = new BlackBoxApp();
        ResponseEditorPanel hep = new ResponseEditorPanel(bba);
        bba.startFrame(hep, args[0], "Response Editor", 400, 300);
    }

	@Override
	protected void rename(String oldName, String newName) {
		app().renameResponse(oldName, newName);
	}
}
