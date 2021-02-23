import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * A console application that solves tasks on cyclic permutations (AKA Zykelschreibweise) for the DS exam.
 * @author Mariia Bogatyreva
 * @author Alex Pokras
 */
public class CyclicPermutations {
	private static class Permutation {
		private final int max;
		final int[] p;

		Permutation(int max) {
			this.max = max;
			p = new int[max + 1];
			for (int i = 1; i <= max; i++) {
				p[i] = i;
			}
		}

		void insertParen(String parenString) {
			String[] intStrings = parenString.split(",");
			int[] paren = new int[intStrings.length];
			for (int i = 0; i < paren.length; i++) {
				paren[i] = Integer.parseInt(intStrings[i]);
			}
			
			for (int i = 0; i < paren.length - 1; i++) {
				p[paren[i]] = paren[i + 1];
			}
			p[paren[paren.length - 1]] = paren[0];
		}

		int next(int i) {
			return p[i];
		}
	}
	
	/** Calculate g(f(i)) */
	private static int gfi(Permutation f, Permutation g, int i) {
		return g.next(f.next(i));
	}

	/** Calculate g º f */
	private static String gAfterF(Permutation f, Permutation g) {
		int max = f.max;
		if (max != g.max) throw new IllegalArgumentException();
		boolean[] seen = new boolean[max + 1];
		String ret = "";
		
		for (int i = 1; i <= max; i++) {
			if (seen[i]) continue;
			ret += "(" + i;
			seen[i] = true;
			int buf = i;
			
			while (!seen[gfi(f,g,i)]) {
				i = gfi(f,g,i);
				ret += ", " + i;
				seen[i] = true;
			}
			
			i = buf;
			ret += ")";
			//Remove parentheses with only one element - i.e. (5)
			if (ret.lastIndexOf(")") - ret.lastIndexOf("(") == 2) ret = ret.substring(0, ret.lastIndexOf("("));
		}
		if (ret.isEmpty() || !ret.contains(",")) return "Id";
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		ArrayList<String> fStrings = new ArrayList<>();
		ArrayList<String> gStrings = new ArrayList<>();
		
		ArrayList<Permutation> fs = new ArrayList<>();
		ArrayList<Permutation> gs = new ArrayList<>();
		
		while (true) {
			System.out.println("Enter a permutation on the column or press Enter to continue");
			String s = in.readLine();
			if (s.isEmpty()) break;
			fStrings.add(s);
		}
		
		while (true) {
			System.out.println("Enter a permutation on the row or press Enter to continue.");
			System.out.println("Press Enter if the rows are the same as the columns");
			String s = in.readLine();
			if (s.isEmpty()) break;
			gStrings.add(s);
		}
		
		//Find the maximum integer
		int max = -1;
		ArrayList<String> allStrings = new ArrayList<>();
		allStrings.addAll(fStrings);
		allStrings.addAll(gStrings);
		
		for (String fString : allStrings) {
			String[] a = fString.split("[^0-9]");
			for (String s : a) {
				if (s.isEmpty()) continue;
				try {
					int curr = Integer.parseInt(s);
					if (curr > max) max = curr;
				} catch (NumberFormatException e) { /* expected */}
			}
		}

		for (String fString : fStrings) {
			//Initialize all the permutations of fs
			Permutation f = new Permutation(max);
			String[] parenStrings = fString.split("\\)");
			for (String parenString : parenStrings) {
				parenString = parenString.replace("(", "");
				parenString = parenString.replace(" ", "");
				f.insertParen(parenString);
			}
			fs.add(f);
		}
		
		if (gStrings.isEmpty()) {
			gStrings = (ArrayList<String>) fStrings.clone(); 
			gs = (ArrayList<Permutation>) fs.clone();
		}
		else for (String gString : gStrings) {
			//Initialize all the permutations of fs
			Permutation g = new Permutation(max);
			String[] parenStrings = gString.split("\\)");
			for (String parenString : parenStrings) {
				parenString = parenString.replace("(", "");
				parenString = parenString.replace(" ", "");
				g.insertParen(parenString);
			}
			gs.add(g);
		}

		for (int i = -1; i < fs.size(); i++) {
			System.out.format("%30s", i == -1 ? "" : fStrings.get(i));
			System.out.print(" ");
		}
		System.out.println();
		
		System.out.print("------------------------------|");
		for (int i = 0; i < fs.size(); i++) {
			System.out.print("-------------------------------");			
		}
		System.out.println();
		
		for (int i = 0; i < gs.size(); i++) {
			System.out.format("%30s", gStrings.get(i));		
			System.out.print("|");
			for (int j = 0; j < fs.size(); j++) {
				System.out.format("%30s", gAfterF(fs.get(j), gs.get(i)));				
			}
			System.out.println();
		}
//		Permutation f = new Permutation(8);
//		f.insertParen("6,8");
//		f.insertParen("7,1");
//		Permutation g = new Permutation(8);
//		g.insertParen("6,8");
//		g.insertParen("7,1");
	}
}
