package martin.chess;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility program to compare the number of board positions for a given move and depth.
 * Each file contains rows of the form
 * 
 * <move>: <number of board positions>
 * 
 * e.g.
 * g2g3: 89798
 * c3a4: 83785
 * c3b5: 86102
 * c3d5: 84310
 * 
 * This utility program compares the two files and reports the differences
 */
public class ComparerUtil {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		Map<String, Integer> myValues = getValues("c:\\temp\\me.txt");
		Map<String, Integer> refValues = getValues("c:\\temp\\stockfish.txt");
		
		var myIt = myValues.entrySet().iterator();
		while (myIt.hasNext()) {
			var entry = myIt.next();
			
			Integer referenceValue = refValues.remove(entry.getKey());
			if (referenceValue == null) {
				System.err.println("You have move " + entry.getKey() + " but reference doesn't");
			} else if (!entry.getValue().equals(referenceValue)) {
				System.err.println("You have " + entry.getValue() + " moves for " + entry.getKey() + " but reference has " + referenceValue);
			}
		}
		
		if (refValues.size() > 0) {
			System.err.println("You are missing the following reference moves: " + refValues);
		}
	}

	private static Map<String, Integer> getValues(String string) throws FileNotFoundException, IOException {
		
		Map<String, Integer> map = new HashMap<>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(string)))) {
		
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(":");
				map.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
			}
				
		}
		return map;
	}
}
