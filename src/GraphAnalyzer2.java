
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pgdp.MiniJava;

public class GraphAnalyzer2 {

    List<String> gegeben;
    int[] gradFolge;
    int anzKnoten;
    int anzKanten;

    public GraphAnalyzer2() {
        gegeben = List.of("Gradfolge", "Anzahl Knoten", "Anzahl Kanten", "Anzahl Knoten und Kanten");
    }

    static int[] getGradFolge() {
        return Arrays
                .stream(MiniJava.readString("Bitte gebe eine Gradfolge in folgender Form ein: (x1,x2,x3,x4,...)")
                        .replace("(", "").replace(")", "").split(","))
                .mapToInt(string -> Integer.parseInt(string.trim())).toArray();
    }

    public static void main(String[] args) {
        GraphAnalyzer2 g = new GraphAnalyzer2();
        g.gradFolge = getGradFolge();
        g.gradFolgenOperationen();
    }

    static public int getAnzKnotenFromGradfolge(int[] gradFolge) {
        return Arrays.stream(gradFolge).sum() / 2;
    }

    // Gebe alle Fakten aus, die man über die Gradfolge rausfinden kann
    void gradFolgenOperationen() {
        int anzKnoten = gradFolge.length;
        int anzKanten = getAnzKnotenFromGradfolge(gradFolge);

        System.out.printf("|V| = %d, |E| = %d\n", anzKnoten, anzKanten);
        if (anzKanten == (anzKnoten - 1)) {
            System.out.println("Es existiert ein Baum mit der gegebenen Gradfolge (da |E| = |V| - 1).");
        } else {
            System.out.println("Es existiert kein Baum mit der gegebenen Gradfolge (da |E| =/= |V| - 1).");
        }

        // Source: Skript S.42 #130
        StringBuilder zshgString = new StringBuilder("Ein Graph mit der gegebenen Gradfolge kann");
        boolean kannZshgdSein = false;
        if (anzKnoten >= 1) {
            if (kannZshgdSein = anzKanten >= anzKnoten - 1)
                zshgString.append(" zshgd. sein (da |V| >= 1 und |E| >= |V| - 1)");
            else
                zshgString.append(" nicht zshgd. sein da |E| < |V| - 1.");
        } else {
            zshgString.append(" nicht zshgd. sein da |V| < 1.");
        }
        System.out.println(zshgString);

        // Zshgd.; Source: Skript S.42 #131
        if (kannZshgdSein && anzKnoten >= 3 && anzKanten >= anzKnoten) {
            System.out.println("Jeder endliche einfache zshgd. Graph mit der gegebenen Gradfolge besitzt einen Kreis " +
                    "(da |V| >= 3 und |E| >= |V|");
        }
        if (Arrays.stream(gradFolge).filter(deg -> deg == 1).count() == 1) {
            System.out.println("Der Graph hat nur einen Knoten mit Grad 1, muss also Zsh");
        }

        // Planarität; Source: Skript S.58 #166
        if (anzKnoten >= 3) {
            if (anzKanten <= (3 * anzKnoten - 6))
                System.out.println("Ein Graph mit der gegebenen Gradfolge kann planar sein da |V| >= 3 und |E| <= 3*|V| - 6");
            else
                System.out.println("Ein Graph mit der gegebenen Gradfolge kann nicht planar sein da |E| > 3*|V| - 6");
        } else {
            System.out.println("Jeder Graph mit der gegebenen Gradfolge ist planar da |V| < 3.");
        }

        if (anzKnoten >= 3) {
            // Eulertouren; Source: Skript S.54 #160
            if (!kannZshgdSein)
                System.out.println("Ein Graph mit der gegebenen Gradfolge kann keine Eulertour besitzen da er nicht zshgd. sein kann.");
            else if (Arrays.stream(gradFolge).allMatch(deg -> deg % 2 == 0)) {
                System.out.println("Jeder zshgd. einfacher Graph mit der gegebenen Gradfolge besitzt eine Eulertour da jeder Knoten geraden Grad hat.");
            }

            // Hamiltonkreis; Source: Skript S.55 #161
            if (Arrays.stream(gradFolge).allMatch(deg -> deg >= (float) anzKnoten / 2)) {
                System.out.println("Jeder einfache Graph mit der gegebenen Gradfolge besitzt einen Hamiltonkreis da jeder Knoten deg >= |V|/2 hat.");
            }
        } else {
            System.out.println("Denk selber über Hamiltonkreise und Eulertouren weil |V| < 3.");
        }

        List<String> reasonsForKuratowskiK5 = new ArrayList<>();
        if (anzKnoten < 5) reasonsForKuratowskiK5.add("K5 kein Minor da |V| < 5");
        if (anzKanten < 10) reasonsForKuratowskiK5.add("K5 kein Minor da |E| < 10");

        List<String> reasonsForKuratowskiK33 = new ArrayList<>();
        if (anzKnoten < 6) reasonsForKuratowskiK33.add("K3,3 kein Minor da |V| < 6");
        if (anzKanten < 9) reasonsForKuratowskiK33.add("K3,3 kein Minor da |E| < 9");

        if (!reasonsForKuratowskiK33.isEmpty() && !reasonsForKuratowskiK5.isEmpty()) {
            System.out.println("Der Graph ist planar da: ");
            List<String> reasonsForKuratowski = new ArrayList<>();
            reasonsForKuratowski.addAll(reasonsForKuratowskiK5);
            reasonsForKuratowski.addAll(reasonsForKuratowskiK33);
            reasonsForKuratowski.forEach(reason -> System.out.printf("\t%s\n", reason));
        }
    }

    // Gebe alle Fakten aus, die man über die anzahl der Kanten rausfinden kann
    void kantenOperationen() {
        //TODO
    }

    // Gebe alle Fakten aus, die man über die anzahl der Knoten rausfinden kann
    void knotenOperationen() {
        //TODO
    }

    // Gebe alle Fakten aus, die man über die anzahl der Knoten und der Kanten
    // rausfinden kann
    void kantenUndKnotenOperationen() {
        kantenOperationen();
        knotenOperationen();
        //TODO (wenn man beides weiß, kann probably noch mehr rausfinden)
    }
}
