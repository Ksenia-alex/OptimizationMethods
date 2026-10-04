package lab1;

import java.io.*;
import java.util.*;

public class Main {

  public static void main(String[] args) throws IOException {
    String filename = "lab1/input.txt";
    LPProblem p = readFromFile(filename);

    System.out.println("Исходная задача:");
    printProblem(p);

    Result r = SimplexSolver.solve(p);

    System.out.println();
    System.out.println("ОТВЕТ");
    if (r.feasible) {
      System.out.print("Оптимальная точка: x = (");
      for (int i = 0; i < r.x.length; i++) {
        System.out.print(String.format("%.2f", r.x[i]));
        if (i < r.x.length - 1) System.out.print(", ");
      }
      System.out.println(")");
      System.out.printf("Значение целевой функции: %.2f%n", r.z);
    }
  }

  public static LPProblem readFromFile(String filename) throws IOException {
    List<String> lines = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) continue;
        lines.add(line);
      }
    }
    if (lines.isEmpty()) throw new RuntimeException("Файл пуст");

    LPProblem p = new LPProblem();

    // max/min + коэффициенты целевой функции
    String[] parts = lines.get(0).split("\\s+");
    p.minimize = parts[0].equalsIgnoreCase("min");
    p.coefficients = new double[parts.length - 1];
    for (int i = 1; i < parts.length; i++)
      p.coefficients[i - 1] = Double.parseDouble(parts[i].replace(',', '.'));
    int n = p.coefficients.length;

    // остальные строки - ограничения
    List<double[]> a = new ArrayList<>();
    List<Double> b = new ArrayList<>();
    List<String> ops = new ArrayList<>();

    for (int k = 1; k < lines.size(); k++) {
      parts = lines.get(k).split("\\s+");
      int opIdx = -1;
      for (int j = 0; j < parts.length; j++) {
        String t = parts[j];
        if (t.equals("<=") || t.equals(">=") || t.equals("=") ||
          t.equals("<")  || t.equals(">")) { opIdx = j; break; }
      }
      if (opIdx != n)
        throw new RuntimeException("Ожидалось " + n + " коэффициентов в ограничении: " + lines.get(k));

      double[] row = new double[n];
      for (int j = 0; j < n; j++)
        row[j] = Double.parseDouble(parts[j].replace(',', '.'));
      a.add(row);

      String op = parts[opIdx];
      if (op.equals("<")) op = "<=";
      if (op.equals(">")) op = ">=";
      ops.add(op);

      b.add(Double.parseDouble(parts[opIdx + 1].replace(',', '.')));
    }

    p.a = a.toArray(new double[0][]);
    p.ops = ops.toArray(new String[0]);
    p.b = new double[b.size()];
    for (int i = 0; i < b.size(); i++) p.b[i] = b.get(i);
    return p;
  }

  private static void printProblem(LPProblem p) {
    System.out.println(p.minimize ? "min" : "max");
    System.out.println(printRow(p.coefficients));
    System.out.println("Ограничения:");
    for (int i = 0; i < p.m(); i++) {
      System.out.println(printRow(p.a[i]) + p.ops[i] + " " + p.b[i]);
    }
  }

  private static String printRow(double[] c) {
    StringBuilder sb = new StringBuilder();
    for (int j = 0; j < c.length; j++) {
      if (Math.abs(c[j]) < 1e-12) continue;
      sb.append(String.format(java.util.Locale.US, "%+.1f*x%d ", c[j], j + 1));
    }
    if (sb.isEmpty()) sb.append("0");
    String s = sb.toString();
    if (s.startsWith("+")) s = s.substring(1);
    return s;
  }
}