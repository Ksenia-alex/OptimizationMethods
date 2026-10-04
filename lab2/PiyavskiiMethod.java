package lab2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class PiyavskiiMethod {
  public static Result minimize(DoubleUnaryOperator f, double a, double b, double L, double eps) {
    List<Point> pts = new ArrayList<>();
    // начинаем с двух концов отрезка (a, f(a)) и (b, f(b))
    pts.add(new Point(a, f.applyAsDouble(a)));
    pts.add(new Point(b, f.applyAsDouble(b)));

    int iter = 0;
    long startTime = System.nanoTime();

    while (true) {
      pts.sort(Comparator.comparingDouble(p -> p.x));

      Point best = pts.getFirst();
      for (Point p : pts) if (p.y < best.y) best = p;

      double minZ = Double.POSITIVE_INFINITY;
      double xNext = Double.NaN;

      for (int i = 0; i < pts.size() - 1; i++) {
        Point p1 = pts.get(i);
        Point p2 = pts.get(i + 1);

        if (p2.x - p1.x <= 1e-15) continue;

        // точка пересечения
        double xIntersection = (p1.x + p2.x) / 2.0 + (p1.y - p2.y) / (2.0 * L);
        // значение ломанной в этой точке
        double z = (p1.y + p2.y) / 2.0 - L * (p2.x - p1.x) / 2.0;

        // для поиска интервала с наименьшей нижней оценкой
        if (z < minZ) {
          minZ = z;
          xNext = xIntersection;
        }
      }

      // критерий остановки
      if (best.y - minZ <= eps) {
        long endTime = System.nanoTime();

        Result result = new Result();
        result.x = best.x;
        result.y = best.y;
        result.iteration = iter;
        result.timeNanos = endTime - startTime;
        result.points = pts;
        result.L = L;
        result.a = a;
        result.b = b;
        result.eps = eps;
        return result;
      }

      if (xNext < a) xNext = a;
      if (xNext > b) xNext = b;

      // считаем f в найденной точке и добавляем в список
      double yNext = f.applyAsDouble(xNext);
      pts.add(new Point(xNext, yNext));
      iter++;
    }
  }
}