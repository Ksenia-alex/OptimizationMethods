package lab1;

public class LPProblem {
  public boolean minimize;  // true - минимизация, false - максимизация
  public double[] coefficients;
  public double[][] a;  // матрица коэфф ограничений (n * m)
  public double[] b;
  public String[] ops;  // <= or >= or =
  public int n() { return coefficients.length; }
  public int m() { return b.length; }
}
