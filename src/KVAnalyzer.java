import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

/**
 * Generates a boolean expression in KNF from the given KV-diagram (AKA K-map, Karnaugh map) 
 * 
 * @author Mariia Bogatyreva
 * @author Alex Pokras
 */
public class KVAnalyzer {
	public static final String PREF_ORDER_OF_VARIABLES = "olur";
	protected class Clause {
		final int[] orul;
		/** Form a new DNF clause for one (!) field (x,y) */
		Clause(int x, int y) {
			orul = new int[4];
			for (char c : "orul".toCharArray()) {
				add(c, checkNegationOnField(c, x, y));
			}
		}
		/** Form a new DNF clause with the specified array */
		Clause(int[] orul){
			this.orul = orul;
		}
		/** Create an empty Clause */
		Clause() {
			this(new int[4]);
		}
		
		private boolean predictForChar (char c, IntPredicate f) {
			int index = -1;
			switch(c) {
			case 'o' -> index = 0;
			case 'r' -> index = 1;
			case 'u' -> index = 2;
			case 'l' -> index = 3;
			default -> throw new IllegalArgumentException();
			}
			return f.test(index);
		}
		
		private void doForChar(char c, IntConsumer f) {
			predictForChar(c, i -> {
				f.accept(i);
				return false;
			});
		}
		
		void add(char toAdd, boolean nonNegated) {
			doForChar(toAdd, index -> orul[index] = nonNegated ? 1 : 2);
		}
		
		void remove(char toRemove) {
			doForChar(toRemove, index -> orul[index] = 0);
		}
		
		void negate(char toNegate) {
			doForChar(toNegate, index -> {
				if (orul[index] == 1) orul[index] = 2;
				else if (orul[index] == 2) orul[index] = 1;
			});
		}
		
		/** True if toGet is contained in this clause and is not negated,
		 * false if contained and negated or if not contained */
		boolean get(char toGet) {
			return predictForChar(toGet, index -> orul[index] == 1);
		}
		
		/** True if toGet is contained in this clause */
		boolean contains(char toGet) {
			return predictForChar(toGet, index -> orul[index] != 0);
		}
		
		String name(char toGet) {
			switch(toGet) {
			case 'o': return o;
			case 'r': return r;
			case 'u': return u;
			case 'l': return l;
			default: throw new IllegalArgumentException();
			}
		}
		
		boolean isParentOf(Clause child) {
			for (int i = 0; i < 4; i++) {
				if (orul[i] == 0) continue;
				if (orul[i] != child.orul[i]) return false;
			}
			return true;
		}
		
		/**Returns an array of all chars of "orul" contained in this clause */
		char[] allChars() {
			String ret = "";
			for (char c : "orul".toCharArray()) {
				if (contains(c)) ret += c;
			}
			return ret.toCharArray();
		}
		
		@Override
		public String toString() {
			String ret = "{";
			for (char c : PREF_ORDER_OF_VARIABLES.toCharArray()) {//TODO preferred order of variables!
				ret += contains(c) ? get(c) ? name(c) + ", " : "¬" + name(c) + ", " : "";
			}
			if (ret.equals("{")) return "{}";
			return ret.substring(0, ret.length() - 2) + "}";
		}
		
		@Override
		public Clause clone() {
			return new Clause(Arrays.copyOf(orul, orul.length));
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Clause)) return false;
			Clause o = (Clause) other;
			for (int i = 0; i < 4; i++) {
				if (o.orul[i] != this.orul[i]) return false;
			}
			return true;
		}
	}
	
	protected final String o,r,u,l;
	protected final boolean[][] kv;
	
	KVAnalyzer(String o, String r, String u, String l, boolean[][] kv) {
		this.o = o;
		this.r = r;
		this.u = u;
		this.l = l;
		this.kv = kv;
	}
	
	protected static boolean checkNegationOnField(char toFind, int x, int y) {
		switch(toFind) {
		case 'o': return x > 0 && x < 3;
		case 'r': return y > 1;
		case 'u': return x > 1;
		case 'l': return y > 0 && y < 3;
		default: throw new IllegalArgumentException();
		}
	}
	
	private ArrayList<Clause> dnfStart() {
		ArrayList<Clause> list = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (kv[i][j]) continue;
				list.add(new Clause(j, i));
			}
		}
		return list;
	}
	
	private LinkedList<ArrayList<Clause>> dnfAllVariants() {
		Queue<ArrayList<Clause>> q = new LinkedList<>();
		LinkedList<ArrayList<Clause>> allVariants = new LinkedList<>();
		q.add(dnfStart());
		allVariants.add(q.peek());
		
		while (!q.isEmpty()) {
			ArrayList<Clause> curr = q.poll();
			for (int i = 0; i < curr.size(); i++) {
				for (int j = i + 1; j < curr.size(); j++) {
					ArrayList<Clause> next = unite(curr, curr.get(i), curr.get(j));
					next = shorten(next);
					next = deleteChildren(next);
					
					if (next == null) continue;
					
					//Filter
					boolean throwNextOut = false;
					label: for (ArrayList<Clause> other : allVariants) {
						if (other.size() != next.size()) continue;
						for (Clause c1 : next) {
							boolean c1IsInOther = false;
							for (Clause c2 : other) {
								if (c1.equals(c2)) c1IsInOther = true;
							}
							if (!c1IsInOther) continue label;
						}
						throwNextOut = true;
					}
					if (throwNextOut) continue;
					
					q.add(next);
					allVariants.add(next);
				}
			}
		}
		return allVariants;
	}
	
	private ArrayList<Clause> unite(ArrayList<Clause> dnf, Clause c1, Clause c2) {
		int[] orul = new int[4];
		boolean foundADifferentlyNegatedVariable = false;
		for (int i = 0; i < 4; i++) {
			int a1 = c1.orul[i];
			int a2 = c2.orul[i];
			if (a1 * a2 == 0 && a1 + a2 != 0) return null; //if exactly one of a1 and a2 is 0
			if (a1 != a2) {
				if (foundADifferentlyNegatedVariable) return null;
				foundADifferentlyNegatedVariable = true;
			} else orul[i] = a1;
		}
		
		@SuppressWarnings("unchecked")
		ArrayList<Clause> ret = (ArrayList<Clause>) dnf.clone();
		ret.remove(c1);
		ret.remove(c2);
		ret.add(new Clause(orul));
		return ret;
	}
	
	private ArrayList<Clause> shorten(ArrayList<Clause> dnf) {
		if (dnf == null) return null;
 		//Create a deep copy of dnf (Clause objects are mutable)
		ArrayList<Clause> ret = new ArrayList<Clause>();
		for (Clause clause : dnf) {			
			ret.add(clause.clone());
		}
		
		for (int i = 0; i < ret.size(); i++) {
			for (int j = 0; j < ret.size(); j++) { //TODO
				Clause c1 = ret.get(i);
				Clause c2 = ret.get(j);
				for (char s : "orul".toCharArray()) {
					if (!c1.contains(s) && !c2.contains(s)) continue;
					if (c1.get(s) == c2.get(s)) continue;
					
					String k = "";
					for (char kc : "orul".toCharArray()) {
						if (kc == s) continue;
						if (c1.contains(kc)) k += kc;
					}
					
					boolean flag = true;
					for (char kc : k.toCharArray()) {
						if (!c2.contains(kc)) flag = false;
						if (c2.get(kc) != c1.get(kc)) flag = false;
						if (!flag) break;
					}
					if (!flag) continue;
					
					c2.remove(s);
				}
			}
		}
		
		return ret;
	}
	
	private ArrayList<Clause> deleteChildren(ArrayList<Clause> dnf) {
		if (dnf == null) return null;
 		//Create a deep copy of dnf (Clause objects are mutable)
		ArrayList<Clause> ret = new ArrayList<Clause>();
		label: for (int i = 0; i < dnf.size(); i++) {	
			Clause clause = dnf.get(i);
			for (int j = i + 1; j < dnf.size(); j++) {	
				Clause clause2 = dnf.get(j);
				if (clause2 == clause) continue;
				if (clause2.isParentOf(clause)) continue label;
			}
			ret.add(clause.clone());
		}
		return ret;
	}
	
	private LinkedList<ArrayList<Clause>> knfAllVariants(
			LinkedList<ArrayList<Clause>> dnfAllVariants) {
		var knfAllVariants = new LinkedList<ArrayList<Clause>>();
		for (ArrayList<Clause> dnf : dnfAllVariants) {
			var knf = new ArrayList<Clause>();
			for (Clause cd : dnf) {
				Clause ck = cd.clone();
				for (char c : "orul".toCharArray()) {
					ck.negate(c);
				}
				knf.add(ck);
			}
			knfAllVariants.add(knf);
		}
		return knfAllVariants;
	}
	
	private static void printLineOfKVMap(int row, boolean[][] kv) {
		System.out.printf("     %s║ %s │ %s │ %s │ %s ║%s\n", 
				row == 1 | row == 2 ? "│" : " ",
				kv[row][0] ? "1" : " ",
				kv[row][1] ? "1" : " ",
				kv[row][2] ? "1" : " ",
				kv[row][3] ? "1" : " ",
				row > 1 ? "│" : " "
				);
	}
	
	protected static void printKVMap(String o, String r, String u, String l, boolean[][] kv) {
		System.out.printf("            %s\n", o);
		System.out.printf("           ───────\n");
		System.out.printf("      ╔═══╤═══╤═══╤═══╗\n");
		printLineOfKVMap(0, kv);
		System.out.printf("      ╟───┼───┼───┼───╢\n");	
		printLineOfKVMap(1, kv);
		System.out.printf   ("%s│╟───┼───┼───┼───╢\n", "     ".substring(l.length()) + l);	
		printLineOfKVMap(2, kv);
		System.out.printf("      ╟───┼───┼───┼───╢│%s\n", r);	
		printLineOfKVMap(3, kv);
		System.out.printf("      ╚═══╧═══╧═══╧═══╝\n");
		System.out.printf("               ───────\n");
		System.out.printf("                %s\n", u);
	}

	public static void main(String[] args) throws IOException {
		printKVMap("oben", "rechts", "unten", "links", new boolean[4][4]);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Geben Sie den Namen der oberen Variable ein");
		String o = in.readLine();
		System.out.println("Geben Sie den Namen der rechten Variable ein");
		String r = in.readLine();
		System.out.println("Geben Sie den Namen der unteren Variable ein");
		String u = in.readLine();
		System.out.println("Geben Sie den Namen der linken Variable ein");
		String l = in.readLine();
		System.out.println("Geben Sie die Felder des KV-Diagramms ein: 1 für gefärbtes, "
				+ "beliebiger Symbol für ungefärbtes");
		System.out.println("Beispiel: 0001");
		System.out.println("          0001");
		System.out.println("          0001");
		System.out.println("          0001 bedeutet, dass nur die rechte Spalte des Diagramms gefärbt ist.");
		boolean[][] kvArray = new boolean[4][4];
		for (int i = 0; i < 4; i++) {
			String row = in.readLine();
			for (int j = 0; j < 4; j++) {
				
				if (j >= row.length()) break;
				if (row.charAt(j) == '1') kvArray[i][j] = true;
			}
		}
		
		System.out.println("Ihre Eingabe:");
		printKVMap(o, r, u, l, kvArray);
		
		KVAnalyzer k = new KVAnalyzer(o, r, u, l, kvArray);
		var variants = k.knfAllVariants(k.dnfAllVariants());
		for (var dnf : variants) {
			System.out.println(dnf);
		}
	}
}
