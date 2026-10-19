package lab1;

import java.util.Arrays;

public class SimplexSolver {
  private static final double EPS = 1e-9;
  private static final int MAX_ITER = 10000;

  public static Result solve(LPProblem p) {
    int n = p.n();
    int m = p.m();

    int[] varType = new int[n];
    if (p.varType != null && p.varType.length == n) varType = p.varType.clone();

    int[] col1 = new int[n];
    int[] col2 = new int[n];
    int shiftedN = 0;
    for (int j = 0; j < n; j++) {
      if (varType[j] == LPProblem.FREE) {
        col1[j] = shiftedN;
        col2[j] = shiftedN + 1;
        shiftedN += 2;
      } else {
        col1[j] = shiftedN;
        col2[j] = -1;
        shiftedN += 1;
      }
    }

    // добавляем переменные для каждого <= и >=
    int slackAndSurplusCount = 0;
    for (String op : p.ops) if (!op.equals("=")) slackAndSurplusCount++;

    int baseVars = shiftedN + slackAndSurplusCount;
    double[][] T = new double[m + 1][baseVars + m + 1];
    int[] basis = new int[m];
    Arrays.fill(basis, -1);

    int idx = 0;
    for (int i = 0; i < m; i++) {
      double b = p.b[i];
      String op = p.ops[i];
      double sign = 1.0;

      // нормализация: b_i >= 0
      if (b < 0) {
        sign = -1.0;
        b = -b;
        if (op.equals("<=")) op = ">=";
        else if (op.equals(">=")) op = "<=";
      }

      for (int j = 0; j < n; j++) {
        double aij = sign * p.a[i][j];
        switch (varType[j]) {
          case LPProblem.NONNEG:                // x_j = +y_j
            T[i][col1[j]] = aij;
            break;
          case LPProblem.NONPOS:                // x_j = -y_j
            T[i][col1[j]] = -aij;
            break;
          case LPProblem.FREE:                  // x_j = y_j+ - y_j-
            T[i][col1[j]] = aij;
            T[i][col2[j]] = -aij;
            break;
        }
      }

      if (op.equals("<=")) {
        T[i][n + idx] = 1.0;
        idx++;
      } else if (op.equals(">=")) {
        T[i][n + idx] = -1.0;
        idx++;
      }

      T[i][baseVars + m] = b;
    }

    // поиск базиса
    int[] basisCandidate = new int[m];
    Arrays.fill(basisCandidate, -1);

    for (int j = 0; j < baseVars; j++) {
      int nonZeroRow = -1;
      int nonZeroCount = 0;
      boolean isOne = false;

      for (int i = 0; i < m; i++) {
        if (Math.abs(T[i][j]) > EPS) {
          nonZeroCount++;
          nonZeroRow = i;
          isOne = Math.abs(T[i][j] - 1.0) < EPS;
          if (nonZeroCount > 1) break;   // уже не единичный
        }
      }

      if (nonZeroCount == 1 && isOne && basisCandidate[nonZeroRow] == -1) {
        basisCandidate[nonZeroRow] = j;
      }
    }
    for (int i = 0; i < m; i++) basis[i] = basisCandidate[i];

    // искусственные
    int artCount = 0;
    for (int i = 0; i < m; i++) {
      if (basis[i] == -1) {
        int col = baseVars + artCount;
        T[i][col] = 1.0;
        basis[i] = col;
        artCount++;
      }
    }

    Result r = new Result();

    // вспомогательная задача
    if (artCount > 0) {
      // W = сумма искусственных
      for (int j = 0; j <= baseVars + m; j++) T[m][j] = 0.0;
      for (int j = baseVars; j < baseVars + artCount; j++) T[m][j] = 1.0;

      // обнуляем коэффициенты при искусственных базисных
      for (int i = 0; i < m; i++) {
        if (basis[i] >= baseVars) {
          for (int j = 0; j <= baseVars + m; j++) T[m][j] -= T[i][j];
        }
      }

      if (simplex(T, basis, m, baseVars + m, 0, baseVars + artCount) < 0) {
        r.status = Result.Status.INFEASIBLE;
        return r;
      }

      double phase1Val = -T[m][baseVars + m];

      if (phase1Val > EPS) {
        r.status = Result.Status.INFEASIBLE;
        return r;
      }

      // убираем искусственные из базиса
      for (int i = 0; i < m; i++) {
        if (basis[i] >= baseVars) {
          for (int j = 0; j < baseVars; j++) {
            if (Math.abs(T[i][j]) > EPS) {
              pivot(T, basis, m, baseVars + m, i, j);
              break;
            }
          }
        }
      }
    }

    for (int j = 0; j <= baseVars + m; j++) T[m][j] = 0.0;
    for (int j = 0; j < n; j++) {
      double cj = p.minimize ? p.coefficients[j] : -p.coefficients[j];
      switch (varType[j]) {
        case LPProblem.NONNEG:
          T[m][col1[j]] = cj;
          break;
        case LPProblem.NONPOS:
          T[m][col1[j]] = -cj;
          break;
        case LPProblem.FREE:
          T[m][col1[j]] = cj;
          T[m][col2[j]] = -cj;
          break;
      }
    }

    for (int i = 0; i < m; i++) {
      if (basis[i] < shiftedN) {
        double coeff = T[m][basis[i]];
        if (Math.abs(coeff) > EPS) {
          for (int j = 0; j <= baseVars + m; j++) T[m][j] -= coeff * T[i][j];
        }
      }
    }

    if (simplex(T, basis, m, baseVars + m, 0, baseVars) < 0) {
      r.status = Result.Status.UNBOUNDED;
      return r;
    }

    double[] x = new double[n];
    for (int j = 0; j < n; j++) {
      double y1 = columnValue(T, basis, m, baseVars + m, col1[j]);
      switch (varType[j]) {
        case LPProblem.NONNEG:
          x[j] = y1;
          break;
        case LPProblem.NONPOS:
          x[j] = -y1;
          break;
        case LPProblem.FREE:
          double y2 = columnValue(T, basis, m, baseVars + m, col2[j]);
          x[j] = y1 - y2;
          break;
      }
    }

    double value = 0;
    for (int j = 0; j < n; j++) value += p.coefficients[j] * x[j];

    r.status = Result.Status.OPTIMAL;
    r.x = x;
    r.z = value;
    return r;
  }

  private static double columnValue(double[][] T, int[] basis, int m, int RHS, int col) {
    for (int i = 0; i < m; i++) if (basis[i] == col) return T[i][RHS];
    return 0.0;
  }

  // итерации симплекс-метода
  private static int simplex(double[][] T, int[] basis, int m, int totalVars, int colMin, int colMax) {
    int iter = 0;
    while (iter < MAX_ITER) {
      // разрешающий столбец — самый отрицательный в целевой строке
      int pivotCol = -1;
      double minCoeff = -EPS;
      for (int j = colMin; j < colMax; j++) {
        if (T[m][j] < minCoeff) { minCoeff = T[m][j]; pivotCol = j; }
      }
      if (pivotCol == -1) return iter;   // оптимум

      // разрешающая строка — минимальное отношение b/a
      int pivotRow = -1;
      double minRatio = Double.POSITIVE_INFINITY;
      for (int i = 0; i < m; i++) {
        if (T[i][pivotCol] > EPS) {
          double ratio = T[i][totalVars] / T[i][pivotCol];
          if (ratio < minRatio) { minRatio = ratio; pivotRow = i; }
        }
      }
      if (pivotRow == -1) return -1; // не ограничена

      pivot(T, basis, m, totalVars, pivotRow, pivotCol);
      iter++;
    }
    return iter;
  }

  // пересчет таблицы
  private static void pivot(double[][] T, int[] basis, int m, int n, int pivotRow, int pivotCol) {
    double piv = T[pivotRow][pivotCol];
    for (int j = 0; j <= n; j++) T[pivotRow][j] /= piv;
    for (int i = 0; i <= m; i++) {
      if (i == pivotRow) continue;
      double factor = T[i][pivotCol];
      if (Math.abs(factor) < EPS) continue;
      for (int j = 0; j <= n; j++) T[i][j] -= factor * T[pivotRow][j];
    }
    basis[pivotRow] = pivotCol;
  }
}
