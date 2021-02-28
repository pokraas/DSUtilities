import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Draws a KV-diagram (AKA K-map, Karnaugh map) from the given boolean expression.
 * 
 * @author Mariia Bogatyreva
 * @author Alex Pokras
 */
public class KNFToKV extends KVAnalyzer {
	static final String PREFERRED_ORDER_OF_VARIABLES = "orul";

	KNFToKV(String o, String r, String u, String l) {
		super(o, r, u, l, new boolean[4][4]);
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
		System.out.println("Geben Sie die Formel in Klauselmengendarstellung (KNF) ein.");
		System.out.println("Empfohlen für Windows: ! anstatt ¬ für Negation von Variablen verwenden.");
		String formula = in.readLine().replace(" ", "")
				.replace("{{", "").replace(",{", "").replace("}}", "}");
		String[] clausesArray = formula.split("}");
		
		KNFToKV k = new KNFToKV(o, r, u, l);
		ArrayList<Clause> dnf = new ArrayList<>();
		String[] names = new String[] {k.o, k.r, k.u, k.l};
		char[] orulChars = "orul".toCharArray();
		
		for (String clauseString : clausesArray) {
			Clause clause = k.new Clause();
			for (String variable : clauseString.split(",")) {
				boolean negated = variable.startsWith("¬") || variable.startsWith("!");
				if (negated) variable = variable.replace("¬", "").replace("!", "");
				for (int i = 0; i < 4; i++) {
					String name = names[i];
					if (name.equals(variable)) {
						clause.add(orulChars[i], negated);
					}
				}
			}
			dnf.add(clause);
		}
		
		boolean[][]kv = new boolean[4][4];
		for (Clause clause : dnf) {
			for (int x = 0; x < 4; x++) {
				label: for (int y = 0; y < 4; y++) {
					//Check if the point (x,y) of the KV diagram is contained in the DNF clause
					for (char c : clause.allChars()) {
						if (checkNegationOnField(c, x, y) != clause.get(c)) continue label;
					}
					kv[y][x] = true;
				}
			}
		}
		
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				kv[y][x] = !kv[y][x];
			}
		}
		
		System.out.println("\nErgebnis (1 steht für angekreuztes Feld):");
		printKVMap(o, r, u, l, kv);
	}
}
