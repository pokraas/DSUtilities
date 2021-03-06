import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A console application that solves tasks on Algebra part for the DS exam.
 * @author Andriy Manucharyan
 */


/*
 just for me:
 n - input
 neutral - das neutrale Element
 numbers - list of all elements of phi(n)
 ordnung - list of numbers which build ordnung of one element
 ordnungen - set of ordnung.size for each element to find Gruppenexponent
 erzeuger - boolean value if current element is Erzeuger
 erzeugerList - list of Erzeuger
 */
public class AlgebraModuloGroups {

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Bitte die Modulogruppenzahl eingeben: ");
        int n = Integer.parseInt(in.readLine());
        System.out.println("Geben Sie * für multiplikative Gruppe modulo " + n + " und + für additive");
        char input = in.readLine().charAt(0);
        if (input != '*' && input != '+') {
            System.err.print("Invalid character!");
            System.exit(69);
        }
        System.out.println("Enter-Taste für alle Ordnungen oder Zahlen, für die die Ordnungen bestimmt werden sollen, aufzählen");
        System.out.println("z.B. für: '3 5 7 19' werden 4 Ordnungen angezeigt");
        String user = in.readLine();
        int neutral;
        if (input == '+')
            neutral = 0;
        else neutral = 1;
        List<Integer> numbers = new ArrayList<>();
        List<Integer> DividerOfN = new ArrayList<>();
        for (int i = 2; i < n; i++) {
            if (n % i == 0)
                DividerOfN.add(i);
        }
        boolean toRemove = false;
        if (input == '*') {
            for (int i = 1; i < n; i++) {
                for (Integer integer : DividerOfN) {
                    if (i % integer == 0) {
                        toRemove = true;
                        break;
                    }
                }
                if (!toRemove)
                    numbers.add(i);
                toRemove = false;
            }
        } else {
            for (int i = 0; i < n; i++) {
                numbers.add(i);
            }
        }
        List<Integer> temp1 = numbers;
        if (numbers.size() <= 30) {
            int[][] table = new int[numbers.size()][numbers.size()];
            for (int i = 0; i < numbers.size(); i++)
                for (int j = 0; j < numbers.size(); j++) {
                    if (input == '*')
                        table[i][j] = numbers.get(j) * numbers.get(i) % n;
                    else table[i][j] = (numbers.get(j) + numbers.get(i)) % n;
                }
            System.out.println("──────┬".repeat(numbers.size() + 1));
            System.out.print("      │ ");
            for (Integer number : numbers) {
                System.out.print(" " + number + "    │ ".substring(Integer.toString(number).length()));
            }
            for (int i = 0; i < numbers.size(); i++) {
                System.out.print(System.lineSeparator() + "──────┼".repeat(numbers.size() + 1) + System.lineSeparator());
                System.out.print(" " + numbers.get(i) + "     │  ".substring(Integer.toString(numbers.get(i)).length()));
                for (int j = 0; j < numbers.size(); j++)
                    System.out.print(table[i][j] + "    │  ".substring(Integer.toString(table[i][j]).length()));
            }
            System.out.println();
            System.out.println("──────┴".repeat(numbers.size() + 1));
            System.out.print(System.lineSeparator() + System.lineSeparator());
        }
        ArrayList<Integer> userPref = new ArrayList<>();
        if (!user.isEmpty()) {
            String[] newNumbers = user.split(" ");
            for (int i = 0; i < newNumbers.length; i++) {
                userPref.add(Integer.parseInt(newNumbers[i]));
            }
            for (Integer integer : userPref) {
                if (!numbers.contains(integer)) {
                    System.out.println(numbers.toString());
                    System.err.println("Phi(n) doesn't contain " + integer);
                    System.exit(6969);
                }
            }
        }
        System.out.println("Gruppengröße = Phi(" + n + ") = " + numbers.size() + System.lineSeparator());
        numbers = temp1;
        boolean erzeuger = false;
        HashSet<Integer> ordnungen = new HashSet<>();
        List<Integer> erzeugerList = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i++) {
            List<Integer> ordnung = new ArrayList<>();
            int temp = numbers.get(i);
            ordnung.add(temp);
            int k = temp;
            do {
                if (input == '*')
                    k = k * temp % n;
                else k = (k + temp) % n;
                if (!ordnung.contains(k))
                    ordnung.add(k);
            }
            while (k != neutral);
            ordnungen.add(ordnung.size());
            if (ordnung.size() == numbers.size()) {
                erzeuger = true;
                erzeugerList.add(temp);
            }
            if (user.isBlank() || userPref.contains(temp)) {
                System.out.print("Ord(" + temp + "): " + ordnung.size() + "; ");
                System.out.print("<" + temp + "> = " + "{");
                for (int j = 0; j < ordnung.size() - 1; j++) {
                    System.out.print(ordnung.get(j) + ", ");
                }
                System.out.println(ordnung.get(ordnung.size() - 1) + "}" + System.lineSeparator());
            }
        }
        if (!erzeuger)
            System.out.println("Diese Gruppe enthält leider keinen Erzeuger und ist deswegen nicht zyklisch");
        else {
            System.out.print("Erzeuger von |G|: {");
            for (int i = 0; i < erzeugerList.size() - 1; i++)
                System.out.print(erzeugerList.get(i) + ", ");
            System.out.println(erzeugerList.get(erzeugerList.size() - 1) + "}");
            System.out.println("Anzahl von Erzeuger: " + erzeugerList.size() + ". Diese Gruppe ist eine zyklische Gruppe!" + System.lineSeparator());
        }
        if (erzeuger)
            System.out.println("Gruppenexponent λ = " + numbers.size());
        else System.out.println("Gruppenexponent λ bitte selbst nachrechnen: (kgV von den folgenden Ordnungen: {" + ordnungen + "})");
    }
}
