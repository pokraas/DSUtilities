import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Stream;

public class GraphAnalyzer {
	
	private static String dsToString(int[] ds) {
		String ret = "(";
		for (int i = 0; i < ds.length - 1; i++) {
			ret += ds[i] + ", ";
		}
		return ret + ds[ds.length - 1] + ")";
	}
	
	private static int v(int[] ds) {
		return ds.length;
	}
	
	private static int e(int[] ds) {
		return Arrays.stream(ds).sum() / 2;
	}
	
	private static void verticesAndEdges(int[] ds) {
		System.out.println("|V| = " + v(ds));
		System.out.println("|E| = " + e(ds));
	}
	
	private static boolean havelHakimi(int[] ds) {
		int[] dsc = Arrays.copyOf(ds, ds.length); //copy of ds (degree sequence)
		try {
			Arrays.sort(dsc);
			System.out.print(dsToString(dsc));
			if (dsc[dsc.length - 1] > dsc.length - 1) {
				System.out.println();
				throw new ArrayIndexOutOfBoundsException();
			}
			System.out.println(" ist realisierbar");
			
			while (dsc[dsc.length - 1] > 0) {
				if (dsc[0] < 0) {
					System.out.println("Nicht realisierbar: es gibt einen Knoten mit Grad " + dsc[0]);
					return false;
				}
				int max = dsc[dsc.length - 1];
				int j = dsc.length - 2;
				for (int i = 0; i < max; i++) dsc[j--]--;
				dsc = Arrays.copyOf(dsc, dsc.length - 1);
				Arrays.sort(dsc);
				System.out.println("gdw. " + dsToString(dsc) + " ist realisierbar");
				if (dsc[0] < 0) {
					System.out.println("Nicht realisierbar: es gibt einen Knoten mit Grad " + dsc[0]);
					return false;
				}
			}
			return true;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Nicht realisierbar: es gibt einen Knoten mit Grad " + dsc[dsc.length - 1] + 
					", aber zu wenig anderen Knoten");
			return false;
		}
	}
	
	private static void treeAndForest(int[] ds) {
		if (v(ds) > e(ds) + 1) System.out.println("Kann ein Wald sein: |V| > |E| + 1");
		else if (v(ds) == e(ds) + 1) System.out.println("Kann ein Baum (und offensichtlich ein Wald) sein: "
				+ "|V| = |E| + 1");
	}
	
	private static void connectedAndAcyclic(int[] ds) {
		if (v(ds) > e(ds) + 1) 
			System.out.println("Ist nicht zusammenhängend (Wald: |V| > |E| + 1)"
					+ "\nKann kreisfrei (azyklisch) sein");
		else if (v(ds) == e(ds) + 1) 
			System.out.println("Kann zusammenhängend sein (Baum: |V| = |E| + 1)"
					+ "\nKann kreisfrei (azyklisch) sein");
		else System.out.println("Kann zusammenhängend sein \nIst nicht kreisfrei (azyklisch): |V| < |E| + 1)");
	}
	
	private static void eulerTour(int[] ds) {
		for (int d : ds) {
			if (d % 2 != 0) {
				System.out.println("Keine Eulertour: es gibt Knoten mit ungeradem Grad.");
				return;
			}
		}
		System.out.println("Besitzt eine Eulertour, weil alle Knoten geraden Grad haben.");
	}
	
	private static void hamiltonCycle(int[] ds) {
		if (ds.length < 3) {
			System.out.println("Kein Hamiltonkreis: hat weniger als 3 Knoten");
			return;
		}
		int v = v(ds);
		for (int d : ds) {
			if (d < v / 2) {
				System.out.println("Wahrscheinlich hat kein Hamiltonkreis");
				return;
			}
		}
		System.out.println("Besitzt einen Hamiltonkreis, weil jeder Knoten den Grad >= |V| / 2 hat.");
	}
	
	private static Boolean planarity(int[] ds) {
		System.out.println("Teilt die Ebene in " + (2 + e(ds) - v(ds)) + " Flächen (Eulersche Polyederformel)");
		boolean flag = false;
		for (int d : ds) {
			if (d <= 5) {
				flag = true;
				break;
			}
		}
		if (!flag) {
			System.out.println("Ist nicht planar: es gibt keinen Knoten mit Grad <= 5 "
					+ "(Korollar aus Eulerscher Polyederformel)");
			return false;
		}
		if (e(ds) <= 3*v(ds) - 6 && v(ds) >= 3) {
			System.out.println("Ist planar: |E| ≤ 3|V| - 6 und |V| ≥ 3");
			return true;
		}
		
		//Kuratowski's theorem
		if (e(ds) < 9) {
			System.out.println("Ist planar: hat weniger Kanten (" + e(ds) + ") als K3,3 und als K5");
			System.out.println("(K3,3 hat 9 Kanten, K5 hat 10 Kanten)");
			return true;
		}
		System.out.println("Manuell überprüfen, ob K3,3 oder K5 ein Minor von diesem Graph ist.");
		System.out.println("Wenn ja, dann ist der Graph laut Satz von Kuratowski planar.");
		return null;
	}
	
	private static void chromaticNumber(int[] ds, Boolean planar) {
		String explanation = "χ(G) ≤ 1/2 + sqrt(2|E| + 1/4)";
		int chi = (int) Math.floor(1.0/2.0 + Math.sqrt(2.0 * e(ds) + 1.0 / 4.0));
		Arrays.sort(ds);
		int chi2 = 1 + ds[ds.length - 1];
		if (chi2 < chi) {
			chi = chi2;
			explanation = "χ(G) ≤ 1 + maximaler Grad eines Knotens";
		}
		
		if (planar != null) {
			if (planar && chi >= 4) {
				chi = 4;
				explanation = "Vier-Farben-Satz: χ(G) ≤ 4, weil G planar ist";
			}
		} else {
			explanation += "\nKonnte nicht die Planarität des Graphen bestimmen."
					+ "\nBitte diese manuell überprüfen und ggf. Vier-Farben-Satz anwenden: "
					+ "χ(G) ≤ 4, wenn G planar ist";
		}
		System.out.println("Chromatische Zahl: χ(G)=" + chi + " (Der Graph ist " + chi + "-färbbar)"
				+ "\n" + explanation);
	}
	
	public static void main(String[] args) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter the degree sequence (DE: Gradfolge)");
		//Read the degree sequence as an array of ints
		int[] ds = Stream.of(in.readLine())
			.flatMap(line -> Arrays.stream(line.split("[^0-9]")))
			.filter(s -> !s.isEmpty())
			.mapToInt(Integer::parseInt)
			.toArray();
		
		System.out.println();
		verticesAndEdges(ds);
		System.out.println("\nAlgorithmus von Havel-Hakimi (Realisierbarkeit):");
		if (!havelHakimi(ds)) return;
		System.out.println();
		connectedAndAcyclic(ds);
		treeAndForest(ds);
		System.out.println();
		eulerTour(ds);
		hamiltonCycle(ds);
		System.out.println();
		boolean planar = planarity(ds);
		System.out.println();
		chromaticNumber(ds, planar);
	}
}
