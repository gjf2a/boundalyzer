package blackbox2;
import util.SharedSet;
import java.lang.StringBuilder;

@SuppressWarnings("serial")
public class MacroInUseException extends RuntimeException {
	public MacroInUseException(String macroInUse, SharedSet<String> macroUsers, SharedSet<Integer> rowUsers) {
		super(errorMsg(macroInUse, macroUsers, rowUsers));
	}
	
	static private String errorMsg(String macroInUse, SharedSet<String> macroUsers, SharedSet<Integer> rowUsers) {
		StringBuilder result = new StringBuilder();
		result.append(macroInUse);
		if (macroUsers.size() > 0) {
			result.append(" used by macros: [");
			for (String macroUser: macroUsers) {
				result.append(macroUser);
				result.append(' ');
			}
			result.deleteCharAt(result.length() - 1);
			result.append(']');
		}
		
		if (rowUsers.size() > 0) {
			result.append(" used by rows: [");
			for (int row: rowUsers) {
				result.append(row);
				result.append(' ');
			}
			result.deleteCharAt(result.length() - 1);
			result.append(']');
		}
		return result.toString();
	}
}
