import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.stream.Collectors;

public class TruthTableBuilder {
	private static final String[][] OP_STRINGS = new String[][] {
		"¬ ! ~ - not".split(" "),
		"∧ & * and".split(" "),
		"∨ | + or".split(" "),
		"⊻ ^ ⊕ xor".split(" "),
		"→ -> ⇒ => impl imp".split(" "),
		"↔ <-> ⇔ <=> gdw bikond bicond equiv".split(" "),
	};
	private static final int[] PRECEDENCE = new int[] {0,1,2,3,3,3};
	private static final char[] OPS = "!&|^>#".toCharArray();
	private final ArrayList<String> vars;
	private final String parsedExprString;
	private final ArrayList<Character> exprRPN = new ArrayList<>(); //expression in reverse Polish notation
	private final int[] mapExpr; //maps the position of each operator in exprString to its position in expr
	private final Stack<Character> opStack = new Stack<>();
	private final int[] mapOpStack; //maps the position of each operator in exprString to its position in opStack
	
	TruthTableBuilder(ArrayList<String> vars, String exprString) {
		this.vars = vars;
		exprString = exprString.replace(" ", "");
		for (int i = OPS.length - 1; i >= 0; i--) { //Important: the symbol <-> contains the symbol -> as a substring,
													//so <-> should be replaced first.
			for (String opString : OP_STRINGS[i]) {
				exprString = exprString.replace(opString, OPS[i] + "");
			}
		}
		for (int i = 0; i < vars.size(); i++) {
			exprString = exprString.replace(vars.get(i), i + "");			
		}
		
		//At this point, each variable, each operation and each parenthesis is represented by exactly one char in exprString.
		parsedExprString = exprString;
		
		//Parse the exprString to RPN
		mapExpr = new int[exprString.length()];
		mapOpStack = new int[exprString.length()];
		
		for (int i = 0; i < exprString.length(); i++) {
			char token = exprString.charAt(i);
			if (token >= '0' && token <= '9') { //if the token is a variable, push it directly onto the queue
				add (token, i);
			}
			else if (token == '(') push(token, i); //if the token is a left parenthesis, push it onto the stack
			else if (token == ')') {//if the token is a right parenthesis,
				//pop all the operators from the stack and add them to expr until ( is found
				while (true) {
					if (opStack.isEmpty()) throw new IllegalArgumentException("Nicht zueinander passende Klammern!");
					if (opStack.peek() == '(') { //if ( found, throw it away
						opStack.pop();
						break;
					}
					popAndAdd();
				}
			}
			else { //if the token is an operator
				//popAndAdd all the operators (that are not left parenthesis) with greater precedence than this one
				while (!opStack.isEmpty()
						&& opStack.peek() != '('
						&& precedence(opStack.peek()) < precedence(token)) {
					popAndAdd();
				}
				push(token, i);
			}
		}
		//popAndAdd the rest of the stack
		while (!opStack.isEmpty()) {
			if(opStack.peek() == '(' || opStack.peek() == ')') 
				throw new IllegalArgumentException("Nicht zueinander passende Klammern!");
			popAndAdd();
		}
	}
	
	/** Add the character c to expr (the queue of operations). Map its position in mapExpr. */
	private void add(char c, int positionInExprString) {
		mapExpr[positionInExprString] = exprRPN.size();
		exprRPN.add(c);
	}
	
	/** Add the character c to opStack. Map its position in mapOpStack. */
	private void push(char c, int positionInExprString) {
		mapOpStack[opStack.size()] = positionInExprString;
		opStack.push(c);
	}
	
	/** Pop a character from the stack and add it directly to expr. Map its position in mapExpr. */
	private char popAndAdd() {
		char c = opStack.pop();
		add(c, mapOpStack[opStack.size()]);
		return c;
	}
	
	/** The operators with smaller precedence should be applied first */
	private int precedence(char c) {
		for (int i = 0; i < OPS.length; i++) {
			if (OPS[i] == c) return PRECEDENCE[i];
		}
		throw new IllegalArgumentException(c + " ist keine unterstützte Operation");
	}
	/** 
	 * Generate all possible variable assignments (combinations of true/false values for each variable, DE: Belegung)
	 * @return List of boolean arrays. Each array is a variable assignment with array[i] = value of ith variable.
	 */
	private ArrayList<boolean[]> variableAssignments() {
		int n = vars.size(); //number of variables + 1
		//Generate the list of empty arrays
		ArrayList<boolean[]> ret = new ArrayList<boolean[]>();
		for (int i = 0; i < 1 << n; i++) {
			ret.add(new boolean[n]);
		}
		
		int shift = n - 1;
		int j = 0;
		boolean assignment = false;
		for (int v = 0; v < n; v++) {
			for (int i = 0; i < 1 << n; i++) {
				ret.get(i)[v] = assignment;
				j++;
				if (j >= 1 << shift) {
					assignment = !assignment;
					j = 0;
				}
			}
			shift--;
		}
		return ret;
	}
	
	private int[] evaluate(boolean[] assignment) {
		Stack<Boolean> vals = new Stack<>();
		int[] a = new int[exprRPN.size()];
		
		for (int i = 0; i < exprRPN.size(); i++) {
			char token = exprRPN.get(i);
			if (token >= '0' && token <= '9') { //if the token is a variable
				vals.add(assignment[token - '0']);
				a[i] = assignment[token - '0'] ? 1 : 0;
			}
			else if (token == '!') { //if the token is ! (the only unary operator)
				boolean notVal = !vals.pop();
				vals.add(notVal);
				a[i] = notVal ? 1 : 0;
			}
			else { //if the token is a binary operator
				boolean arg2 = vals.pop();
				boolean arg1 = vals.pop();
				boolean res = applyToTwoArgs(token, arg1, arg2); 
				vals.add(res);
				a[i] = res ? 1 : 0;
			}
		}
		return a;
	}
	
	private boolean applyToTwoArgs(char op, boolean arg1, boolean arg2) {
		switch(op) {
		case '&' : return arg1 & arg2;
		case '|' : return arg1 | arg2;
		case '^' : return arg1 ^ arg2;
		case '>' : return !arg1 | arg2;
		case '#' : return !(arg1 ^ arg2);
		default: throw new IllegalArgumentException(op + " ist kein unterstützter Operator");
		}
	}
	
	private void printTable() {
		var assignments = variableAssignments();
		int height = assignments.size();
		int width = assignments.get(0).length;
		
		//Print the variable names
		for (String var : vars) System.out.print(var + "    ".substring(var.length()));
		System.out.print("║    ");
		
		//Print the expression
		String curr = "";
		for (char c : parsedExprString.toCharArray()) {
			if (c == '(' || c == ')') curr += c + " ";
			else if (c >= '0' && c <= '9') {
				curr += vars.get(c - '0');
				curr += "        ".substring(curr.length());
				System.out.print(curr);
				curr = "";
			} 
			else {
				for (int i = 0; i < OPS.length; i++) {
					if (OPS[i] == c) {
						String opString = OP_STRINGS[i][1];
						curr += opString;
						curr += "        ".substring(curr.length());
						System.out.print(curr);
						curr = "";
						break;
					}
				}
			}
		}
		System.out.println(curr);
		
		//Print the horizontal line
		for (String var : vars) System.out.print("────");
		System.out.print("╫────");
		for (int i = 0; i < parsedExprString.replace("(", "").replace(")", "").length(); i++) {
			System.out.print("────────");
		}
		System.out.println();
		
		//Print each row of the table
		for (boolean[] assignment : assignments) {
			//Print an assignment
			for (int i = 0; i < vars.size(); i++) {
				System.out.print((assignment[i] ? 1 : 0) + "   ");
			}
			System.out.print("║    ");
			
			//Print a row of the truth table for the concrete assignment
			int[] evaluated = evaluate(assignment);
			for (int i = 0; i < parsedExprString.length(); i++) {
				char c = parsedExprString.charAt(i);
				if ("()".contains(c + "")) continue;
				System.out.print(evaluated[mapExpr[i]] + "       ");
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("Bitte alle Variablen getrennt durch Leerzeichen eingeben, danach Enter drücken");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		ArrayList<String> vars = new ArrayList<String>(
				Arrays.stream(in.readLine().split(" "))
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList()));
		
		System.out.println("Bitte den auszuwertenden Ausdruck eingeben. Unterstützte Operationen:");
		for (String[] opStrings : OP_STRINGS) {
			for (String symbol : opStrings) {
				System.out.print(symbol + " ");
			}
			System.out.println();
		}
		System.out.println("(\n)");
		
		TruthTableBuilder builder = new TruthTableBuilder(vars, in.readLine());
		//builder.variableAssignments(); //TODO
		builder.printTable();
	}
}
