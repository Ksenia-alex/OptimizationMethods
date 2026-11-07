package lab3;

public class Lagrange {

  // численная точность
  static final double EPS = 1e-8;
  static final double H = 1e-5;

  // целевая функция
  static double f(double x, double y, double z) {
    return x * x + y * y + z * z;
  }

  // ограничения
  static double h1(double x, double y, double z) {
    return x + y + z - 3;
  }

  static double h2(double x, double y) {
    return x - y - 1;
  }

  // функция Лагранжа
  static double L(double x, double y, double z, double lambda1, double lambda2) {
    return f(x, y, z) + lambda1 * h1(x, y, z) + lambda2 * h2(x, y);
  }

  // частные производные
  static double[] partialDerivatives(double x, double y, double z, double lambda1, double lambda2) {
    return new double[]{
      (L(x + H, y, z, lambda1, lambda2) - L(x - H, y, z, lambda1, lambda2)) / (2 * H),
      (L(x,y + H, z, lambda1, lambda2) - L(x, y - H, z, lambda1, lambda2)) / (2 * H),
      (L(x, y,z + H, lambda1, lambda2) - L(x, y, z - H, lambda1, lambda2)) / (2 * H)
    };
  }

  // система F(x, y, z, lambda1, lambda2) = 0
  static double[] F(double x, double y, double z, double lambda1, double lambda2) {
    double[] partialDerivatives = partialDerivatives(x, y, z, lambda1, lambda2);
    return new double[]{
      partialDerivatives[0],
      partialDerivatives[1],
      partialDerivatives[2],
      h1(x, y, z),
      h2(x, y)
    };
  }

  // якобиан системы
  static double[][] jacobian(double x, double y, double z, double lambda1, double lambda2) {
    double[] F0 = F(x, y, z, lambda1, lambda2);
    double[][] Fp = {
      F(x + H, y, z, lambda1, lambda2),       // d/dx
      F(x,y + H, z, lambda1, lambda2),        // d/dy
      F(x, y,z + H, lambda1, lambda2),        // d/dz
      F(x, y, z, lambda1 + H, lambda2),  // d/dl1
      F(x, y, z, lambda1, lambda2 + H)   // d/dl2
    };

    double[][] J = new double[5][5];
    for (int j = 0; j < 5; j++)
      for (int i = 0; i < 5; i++)
        J[i][j] = (Fp[j][i] - F0[i]) / H;
    return J;
  }

  // решение СЛАУ
  static double[] solve(double[][] A, double[] b) {
    int n = 5;
    double[][] M = new double[n][n + 1];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) M[i][j] = A[i][j];
      M[i][n] = b[i];
    }
    for (int col = 0; col < n; col++) {
      int piv = col;
      for (int r = col + 1; r < n; r++)
        if (Math.abs(M[r][col]) > Math.abs(M[piv][col])) piv = r;
      if (Math.abs(M[piv][col]) < 1e-14) return null;
      double[] t = M[col]; M[col] = M[piv]; M[piv] = t;
      double d = M[col][col];
      for (int j = col; j <= n; j++) M[col][j] /= d;
      for (int r = 0; r < n; r++) {
        if (r == col) continue;
        double fac = M[r][col];
        for (int j = col; j <= n; j++) M[r][j] -= fac * M[col][j];
      }
    }
    double[] xx = new double[n];
    for (int i = 0; i < n; i++) xx[i] = M[i][n];
    return xx;
  }

  static double norm(double[] v) {
    double s = 0; for (double a : v) s += a * a; return Math.sqrt(s);
  }

  // решение системы
  static double[] newton(double x, double y, double z, double l1, double l2) {
    for (int it = 0; it < 80; it++) {
      double[] Fv = F(x, y, z, l1, l2);
      if (norm(Fv) < EPS) return new double[]{x, y, z, l1, l2};

      double[] d = solve(jacobian(x, y, z, l1, l2), Fv);
      if (d == null) return null;

      x -= d[0]; y -= d[1]; z -= d[2]; l1 -= d[3]; l2 -= d[4];

      if (norm(d) < EPS) break;
    }
    return norm(F(x, y, z, l1, l2)) < 100 * EPS
      ? new double[]{x, y, z, l1, l2}
      : null;
  }

  // ранг Якоби
  static int constraintRank(double x, double y, double z) {
    double[] g1 = {
      (h1(x + H, y, z) - h1(x - H, y, z)) / (2 * H),
      (h1(x, y + H, z) - h1(x, y - H, z)) / (2 * H),
      (h1(x, y, z + H) - h1(x, y, z - H)) / (2 * H)
    };
    double[] g2 = {
      (h2(x + H, y) - h2(x - H, y)) / (2 * H),
      (h2(x, y + H) - h2(x, y - H)) / (2 * H),
      0
    };
    double d12 = g1[0] * g2[1] - g1[1] * g2[0];
    double d13 = g1[0] * g2[2] - g1[2] * g2[0];
    double d23 = g1[1] * g2[2] - g1[2] * g2[1];
    double m = Math.max(Math.abs(d12), Math.max(Math.abs(d13), Math.abs(d23)));
    return m > 1e-6 ? 2 : 1;
  }
}
