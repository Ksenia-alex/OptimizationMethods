package lab2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class PiyavskiiMethod {
  public static Result minimize(DoubleUnaryOperator f, double a, double b, double L, double eps, int maxIter) {
    if (!Double.isFinite(a) || !Double.isFinite(b)) return error(a, b, L, eps, "a и b должны быть конечными");
    if (a >= b) return error(a, b, L, eps, "должно быть a < b");
    if (!Double.isFinite(eps) || eps <= 0) return error(a, b, L, eps, "eps должно быть > 0");
    if (maxIter <= 0) return error(a, b, L, eps, "maxIter должно быть > 0");
    if (!Double.isFinite(L) || L <= 0) return error(a, b, L, eps, "L должно быть конечным и > 0");

    double fa = f.applyAsDouble(a);
    double fb = f.applyAsDouble(b);
    if (!Double.isFinite(fa) || !Double.isFinite(fb)) return error(a, b, L, eps, "f(a) или f(b) не конечно (NaN/Inf)");

    if (Math.abs(fb - fa) > L * (b - a) * (1 + 1e-6) + 1e-9)
      return error(a, b, L, eps, "L недостаточна на [a,b]: |Δf|/|Δx| = " + (Math.abs(fb - fa) / (b - a)) + " > L = " + L);

    List<Point> pts = new ArrayList<>();
    // начинаем с двух концов отрезка (a, f(a)) и (b, f(b))
    pts.add(new Point(a, f.applyAsDouble(a)));
    pts.add(new Point(b, f.applyAsDouble(b)));

    int iter = 0;
    long startTime = System.nanoTime();
    double finalGap = Double.NaN;

    while (true) {
      pts.sort(Comparator.comparingDouble(p -> p.x));

      Point best = pts.getFirst();
      for (Point p : pts) if (p.y < best.y) best = p;

      double minZ = Double.POSITIVE_INFINITY;
      double xNext = Double.NaN;
      double leftX = Double.NaN, rightX = Double.NaN;

      for (int i = 0; i < pts.size() - 1; i++) {
        Point p1 = pts.get(i);
        Point p2 = pts.get(i + 1);

        if (p2.x - p1.x <= 1e-15) continue;

        if (Math.abs(p2.y - p1.y) > L * (p2.x - p1.x) * (1 + 1e-6) + 1e-9)
          return error(a, b, L, eps, "L недостаточна: |Δf|/|Δx| = " + (Math.abs(p2.y - p1.y) / (p2.x - p1.x)) + " > L = " + L);

        // точка пересечения
        double xIntersection = (p1.x + p2.x) / 2.0 + (p1.y - p2.y) / (2.0 * L);
        // значение ломанной в этой точке
        double z = (p1.y + p2.y) / 2.0 - L * (p2.x - p1.x) / 2.0;

        // для поиска интервала с наименьшей нижней оценкой
        if (z < minZ) {
          minZ = z;
          xNext = xIntersection;
          leftX = p1.x;
          rightX = p2.x;
        }
      }

      if (!Double.isFinite(minZ) || !Double.isFinite(best.y))
        return error(a, b, L, eps, "Не удалось найти допустимый интервал (все точки совпали или NaN)");
      if (Double.isNaN(xNext) || !Double.isFinite(leftX) || !Double.isFinite(rightX)
        || rightX - leftX <= 1e-15)
        return error(a, b, L, eps, "Не удалось найти интервал для следующей пробы");

      // критерий остановки
      finalGap = best.y - minZ;
      if (!Double.isFinite(finalGap))
        return error(a, b, L, eps, "Итоговый разрыв не конечен: " + finalGap);
      if (finalGap < -1e-6)
        return error(a, b, L, eps, "L недостаточна: разрыв " + finalGap + " < 0");

      if (finalGap <= eps) {
        long endTime = System.nanoTime();
        return buildResult(a, b, iter, endTime - startTime, pts, best, L, eps, finalGap, "сошлось: разрыв " + finalGap + " <= eps");
      }

      // лимит итераций
      if (iter >= maxIter) {
        long endTime = System.nanoTime();
        return buildResult(a, b, iter, endTime - startTime, pts, best, L, eps, finalGap, "достигнул лимит итераций " + maxIter);
      }

      if (xNext < a) xNext = a;
      if (xNext > b) xNext = b;

      if (xNext < leftX || xNext > rightX) xNext = (leftX + rightX) / 2.0;

      // считаем f в найденной точке и добавляем в список
      double yNext = f.applyAsDouble(xNext);
      if (!Double.isFinite(yNext)) {
        long endTime = System.nanoTime();
        return buildResult(a, b, iter, endTime - startTime, pts, best, L, eps, finalGap, "f(x) = NaN/Inf в точке x = " + xNext);
      }
      pts.add(new Point(xNext, yNext));
      iter++;
    }
  }

  private static Result buildResult(double a, double b, int iter, long timeNanos, List<Point> pts, Point best, double L, double eps, double finalGap, String status) {
    Result result = new Result();
    result.x = best.x;
    result.y = best.y;
    result.iteration = iter;
    result.timeNanos = timeNanos;
    result.points = pts;
    result.L = L;
    result.a = a;
    result.b = b;
    result.eps = eps;
    result.finalGap = finalGap;
    result.status = status;
    return result;
  }

  private static Result error(double a, double b, double L, double eps, String msg) {
    Result r = new Result();
    r.x = Double.NaN;
    r.y = Double.NaN;
    r.a = a;
    r.b = b;
    r.L = L;
    r.eps = eps;
    r.iteration = 0;
    r.timeNanos = 0;
    r.points = new ArrayList<>();
    r.finalGap = Double.NaN;
    r.status = "ошибка: " + msg;
    return r;
  }
}