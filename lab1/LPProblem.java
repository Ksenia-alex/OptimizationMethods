package lab1;

public class LPProblem {
  public static final int NONNEG = 0;   // x_j >= 0
  public static final int NONPOS = 1;   // x_j <= 0
  public static final int FREE   = 2;

  public boolean minimize;  // true - минимизация, false - максимизация
  public double[] coefficients;
  public double[][] a;  // матрица коэфф ограничений (n * m)
  public double[] b;
  public String[] ops;  // <= or >= or =
  public int[] varType;
  public int n() { return coefficients.length; }
  public int m() { return b.length; }
}
