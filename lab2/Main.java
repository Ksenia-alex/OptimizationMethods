package lab2;

import javax.swing.*;
import java.util.Scanner;
import java.util.function.DoubleUnaryOperator;

public class Main {
  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    sc.useLocale(java.util.Locale.US);

    System.out.println("Ввод данных");
    System.out.print("Введите f(x) = ");
    String funcStr = sc.nextLine().trim();

    System.out.print("Введите через пробел границы a, b и eps: ");
    double a   = sc.nextDouble();
    double b   = sc.nextDouble();
    double eps = sc.nextDouble();

    MathExpression expr = new MathExpression(funcStr);
    DoubleUnaryOperator f = expr::eval;

    sc.nextLine();
    System.out.print("Введите L: ");
    String lInput = sc.nextLine().trim();

    double L;
    if (lInput.isEmpty()) {
      L = estimateLipschitz(f, a, b, 2000);
      System.out.printf("Оценка L = %.4f%n", L);
    } else {
      L = Double.parseDouble(lInput);
    }

    Result result = PiyavskiiMethod.minimize(f, a, b, L, eps);

    System.out.println("\nРезультаты:");
    System.out.println("Функция: f(x) = " + funcStr);
    System.out.println("Отрезок: [" + a + ", " + b + "]");
    System.out.println("Точность eps: " + eps);
    System.out.println("Константа L: " + L + "\n");
    System.out.println("Количество итераций: " + result.iteration);
    System.out.printf ("Время: %.3f мс%n", result.timeNanos / 1e6);
    System.out.println("Количество пробных точек: " + result.points.size() + "\n");
    System.out.printf ("Найденный x*: %.8f%n", result.x);
    System.out.printf ("Значение f(x*): %.8f%n", result.y);

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
