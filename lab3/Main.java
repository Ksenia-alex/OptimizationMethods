package lab3;

import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    System.out.println("Целевая функция: f(x,y,z) = x^2 + y^2 + z^2");
    System.out.println("Ограничения:");
    System.out.println("  h1: x + y + z - 3 = 0");
    System.out.println("  h2: x - y - 1 = 0");
    System.out.println("Функция Лагранжа:");
    System.out.println("  L = x^2 + y^2 + z^2 + λ1·(x + y + z - 3) + λ2·(x - y - 1)");
    System.out.println("Уравнения стационарности:");
    System.out.println("  ∂L/∂x = 2x + λ1 + λ2 = 0");
    System.out.println("  ∂L/∂y = 2y + λ1 - λ2 = 0");
    System.out.println("  ∂L/∂z = 2z + λ1 = 0");
    System.out.println("  ∂L/∂λ1 = x + y + z - 3 = 0");
    System.out.println("  ∂L/∂λ2 = x - y - 1 = 0");
    System.out.println();

    List<double[]> candidates = new ArrayList<>();
    int total = 0;
    // сетка начальных приближений
    for (double sx = -5; sx <= 5; sx += 2.5)
      for (double sy = -5; sy <= 5; sy += 2.5)
        for (double sz = -5; sz <= 5; sz += 2.5)
          for (double sl1 = -1; sl1 <= 1; sl1 += 1)
            for (double sl2 = -1; sl2 <= 1; sl2 += 1) {
              total++;
              double[] u = Lagrange.newton(sx, sy, sz, sl1, sl2);
              if (u == null) continue;
              if (Double.isNaN(u[0]) || Double.isNaN(u[1]) || Double.isNaN(u[2])) continue;

              double maxRes = Math.max(Math.abs(Lagrange.h1(u[0], u[1], u[2])), Math.abs(Lagrange.h2(u[0], u[1])));
              if (maxRes > 1e-5) continue;

              boolean dup = false;
              for (double[] v : candidates)
                if (Math.hypot(u[0] - v[0], u[1] - v[1]) < 1e-5 && Math.abs(u[2] - v[2]) < 1e-5) { dup = true; break; }
              if (!dup) candidates.add(u);
            }

    System.out.println("Начальных приближений: " + total);
    if (candidates.isEmpty()) { System.out.println("Стационарных точек не найдено"); return; }
    System.out.println("Уникальных допустимых кандидатов: " + candidates.size());
    System.out.println();

    double minVal = Double.POSITIVE_INFINITY;
    double[] minPt = null;

    for (int i = 0; i < candidates.size(); i++) {
      double[] u = candidates.get(i);
      double fv = Lagrange.f(u[0], u[1], u[2]);
      double r1 = Lagrange.h1(u[0], u[1], u[2]);
      double r2 = Lagrange.h2(u[0], u[1]);
      boolean ok = Math.abs(r1) < 1e-5 && Math.abs(r2) < 1e-5;
      int r  = Lagrange.constraintRank(u[0], u[1], u[2]);

      System.out.printf("Кандидат %d:%n", i + 1);
      System.out.printf("  точка: (%.6f, %.6f, %.6f)%n", u[0], u[1], u[2]);
      System.out.printf("  множители: λ1 = %.6f, λ2 = %.6f%n", u[3], u[4]);
      System.out.printf("  невязки: h1 = %.3e, h2 = %.3e%n", r1, r2);
      System.out.printf("  допустимость: %s%n", ok ? "да" : "нет");
      System.out.printf("  f: %.6f%n", fv);
      System.out.printf("  ранг Якоби: %d => %s%n", r, r == 2 ? "регулярная" : "нерегулярная");

      if (ok) {
        System.out.println("  гессиан L = diag(2, 2, 2) > 0");
        System.out.println("  d²L/dε² = 2|v|² > 0");
        System.out.println("  так как гессиан - диагональная матрица с положительными числами на диагонали => она положительно определена => строго выпукла на R³");
        System.out.println("  ограничения аффинны => допустимое множество выпукло");
        System.out.println("  доказательство глобальности:");
        System.out.println("    так как функция строго выпукла, ограничения аффинны => найденный условный минимум единственный и глобальный");
        System.out.println("    f(y) = 6y² - 6y + 5,  f''(y) = 12 > 0,");
        System.out.println("    f(y) → +∞ при |y| → ∞ => глобального максимума нет");
        if (fv < minVal) { minVal = fv; minPt = u; }
      }
    }
    for (int k = 0; k < candidates.size(); k++) {
      double[] u = candidates.get(k);
      System.out.printf("#%d: (%.6f, %.6f, %.6f), λ=(%.6f, %.6f), f=%.6f%n",
        k + 1, u[0], u[1], u[2], u[3], u[4],
        Lagrange.f(u[0], u[1], u[2]));
    }
    System.out.println("Ответ:");
    if (minPt != null)
      System.out.printf("Глобальный минимум: f_min = %.6f в точке (%.6f, %.6f, %.6f)%n", minVal, minPt[0], minPt[1], minPt[2]);
    else
      System.out.println("Глобальный минимум не найден.");
    System.out.println("Глобальный максимум: не существует");
  }
}
