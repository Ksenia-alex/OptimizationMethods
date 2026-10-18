package lab2;

import javax.swing.*;
import java.util.Scanner;
import java.util.function.DoubleUnaryOperator;

public class Main {
  private static final int MAX_ITER = 100_000;

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    sc.useLocale(java.util.Locale.US);

    System.out.println("Ввод данных");
    System.out.print("Введите f(x) = ");
    String funcStr = sc.nextLine().trim();
    if (funcStr.isEmpty()) { System.out.println("Ошибка: пустая строка функции"); return; }

    System.out.print("Введите через пробел границы a, b и eps: ");
    if (!sc.hasNextDouble()) { System.out.println("Ошибка: ожидалось число a"); return; }
    double a   = sc.nextDouble();
    if (!sc.hasNextDouble()) { System.out.println("Ошибка: ожидалось число b"); return; }
    double b   = sc.nextDouble();
    if (!sc.hasNextDouble()) { System.out.println("Ошибка: ожидалось число eps"); return; }
    double eps = sc.nextDouble();

    MathExpression expr = new MathExpression(funcStr);
    DoubleUnaryOperator f = expr::eval;

    long startTime = System.nanoTime();

    double L = estimateLipschitz(f, a, b, 2000);

    Result result = PiyavskiiMethod.minimize(f, a, b, L, eps, MAX_ITER);

    long totalTimeNanos = System.nanoTime() - startTime;

    System.out.println("\nРезультаты:");
    System.out.println("Функция: f(x) = " + funcStr);
    System.out.println("Отрезок: [" + a + ", " + b + "]");
    System.out.println("Точность eps: " + eps);
    System.out.println("Константа L: " + L);
    System.out.println("Статус: " + result.status + "\n");
    if (!Double.isFinite(result.x) || !Double.isFinite(result.y)) return;
    System.out.println("Количество итераций: " + result.iteration);
    System.out.printf("Время метода: %.3f мс%n", result.timeNanos / 1e6);
    System.out.printf("Общее время: %.3f мс%n", totalTimeNanos / 1e6);
    System.out.println("Количество пробных точек: " + result.points.size());
    System.out.printf("Итоговый разрыв: %.10f%n \n", result.finalGap);
    System.out.printf("Найденный x*: %.8f%n", result.x);
    System.out.printf("Значение f(x*): %.8f%n", result.y);

    SwingUtilities.invokeLater(() -> {
      JFrame frame = new JFrame("Метод ломаных");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.add(new Plot(f, result));
      frame.setSize(1000, 700);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    });
  }

  public static double estimateLipschitz(DoubleUnaryOperator f, double a, double b, int n) {
    double maxSlope = 0.0;
    double prevX = a;
    double prevY = f.applyAsDouble(a);

    for (int i = 1; i <= n; i++) {
      double x = a + (b - a) * i / n;
      double y = f.applyAsDouble(x);
      double slope = Math.abs(y - prevY) / Math.abs(x - prevX);
      if (slope > maxSlope) maxSlope = slope;
      prevX = x;
      prevY = y;
    }
    return maxSlope * 2.1 + 1e-9;
  }
}
