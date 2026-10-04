package lab2;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.function.DoubleUnaryOperator;

public class Plot extends JPanel {
  private final DoubleUnaryOperator f;
  private final Result result;
  private static final int SAMPLES = 2000;

  private static final Color BG = new Color(250, 250, 252);
  private static final Color GRID = new Color(225, 228, 235);
  private static final Color AXIS = new Color(60, 60, 70);
  private static final Color FUNC_COLOR = new Color(30, 30, 40);
  private static final Color ENV_COLOR = new Color(60, 120, 225);
  private static final Color TRIAL_COLOR = new Color(18, 90, 50);
  private static final Color MIN_COLOR = new Color(220, 40, 40);
  private static final Color TEXT_COLOR = new Color(40, 40, 50);

  public Plot(DoubleUnaryOperator f, Result result) {
    this.f = f;
    this.result = result;
    setBackground(BG);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

    int w = getWidth();
    int h = getHeight();
    int marginL = 70;
    int marginR = 30;
    int marginT = 30;
    int marginB = 50;

    int plotW = w - marginL - marginR;
    int plotH = h - marginT - marginB;

    double a = result.a;
    double b = result.b;
    double L = result.L;

    double[] xs = new double[SAMPLES + 1];
    double[] ysFunc = new double[SAMPLES + 1];
    double[] ysEnv = new double[SAMPLES + 1];

    double yMin = Double.POSITIVE_INFINITY;
    double yMax = Double.NEGATIVE_INFINITY;

    for (int i = 0; i <= SAMPLES; i++) {
      double x = a + (b - a) * i / SAMPLES;
      xs[i] = x;

      double yf = f.applyAsDouble(x);
      ysFunc[i] = yf;
      if (yf < yMin) yMin = yf;
      if (yf > yMax) yMax = yf;

      double env = Double.NEGATIVE_INFINITY;
      for (Point p : result.points) {
        double v = p.y - L * Math.abs(x - p.x);
        if (v > env) env = v;
      }
      ysEnv[i] = env;
      if (env < yMin) yMin = env;
      if (env > yMax) yMax = env;
    }

    double yRange = yMax - yMin;
    if (yRange <= 0) yRange = 1.0;
    double yPad = yRange * 0.08;
    yMin -= yPad;
    yMax += yPad;

    final double xMinF = a;
    final double xMaxF = b;
    final double yMinF = yMin;
    final double yMaxF = yMax;

    // Преобразование координат
    DoubleUnaryOperator X = x -> marginL + (x - xMinF) / (xMaxF - xMinF) * plotW;
    DoubleUnaryOperator Y = y -> marginT + (yMaxF - y) / (yMaxF - yMinF) * plotH;

    // Сетка
    g2.setStroke(new BasicStroke(1f));
    g2.setColor(GRID);
    int gridX = 10;
    int gridY = 8;
    for (int i = 0; i <= gridX; i++) {
      double xv = xMinF + (xMaxF - xMinF) * i / gridX;
      int px = (int) X.applyAsDouble(xv);
      g2.drawLine(px, marginT, px, marginT + plotH);
    }
    for (int i = 0; i <= gridY; i++) {
      double yv = yMinF + (yMaxF - yMinF) * i / gridY;
      int py = (int) Y.applyAsDouble(yv);
      g2.drawLine(marginL, py, marginL + plotW, py);
    }

    // Оси с подписями
    g2.setColor(AXIS);
    g2.setStroke(new BasicStroke(1.3f));
    // Рамка вокруг области графика
    g2.drawRect(marginL, marginT, plotW, plotH);

    // Подписи по X
    g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
    FontMetrics fm = g2.getFontMetrics();
    for (int i = 0; i <= gridX; i++) {
      double xv = xMinF + (xMaxF - xMinF) * i / gridX;
      int px = (int) X.applyAsDouble(xv);
      String label = fmtTick(xv);
      int tw = fm.stringWidth(label);
      g2.drawString(label, px - tw / 2, marginT + plotH + 18);
      g2.drawLine(px, marginT + plotH, px, marginT + plotH + 4);
    }

    for (int i = 0; i <= gridY; i++) {
      double yv = yMinF + (yMaxF - yMinF) * i / gridY;
      int py = (int) Y.applyAsDouble(yv);
      String label = fmtTick(yv);
      int tw = fm.stringWidth(label);
      g2.drawString(label, marginL - 8 - tw, py + 4);
      g2.drawLine(marginL - 4, py, marginL, py);
    }

    // Подписи осей
    g2.setFont(new Font("SansSerif", Font.BOLD, 13));
    g2.drawString("x", marginL + plotW + 10, marginT + plotH + 18);
    g2.drawString("f(x)", 8, marginT - 10);

    // График функции
    g2.setColor(FUNC_COLOR);
    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    Path2D pathFunc = new Path2D.Double();
    pathFunc.moveTo(X.applyAsDouble(xs[0]), Y.applyAsDouble(ysFunc[0]));
    for (int i = 1; i <= SAMPLES; i++) pathFunc.lineTo(X.applyAsDouble(xs[i]), Y.applyAsDouble(ysFunc[i]));
    g2.draw(pathFunc);

    // Нижняя огибающая (ломаная)
    g2.setColor(ENV_COLOR);
    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{6f, 4f}, 0f));
    Path2D pathEnv = new Path2D.Double();
    pathEnv.moveTo(X.applyAsDouble(xs[0]), Y.applyAsDouble(ysEnv[0]));
    for (int i = 1; i <= SAMPLES; i++) pathEnv.lineTo(X.applyAsDouble(xs[i]), Y.applyAsDouble(ysEnv[i]));
    g2.draw(pathEnv);

    // Пробные точки
    for (Point p : result.points) {
      int px = (int) X.applyAsDouble(p.x);
      int py = (int) Y.applyAsDouble(p.y);
      g2.setColor(Color.WHITE);
      g2.fillOval(px - 5, py - 5, 10, 10);
      g2.setColor(TRIAL_COLOR);
      g2.fillOval(px - 4, py - 4, 8, 8);
    }

    // Найденный минимум
    int mx = (int) X.applyAsDouble(result.x);
    int my = (int) Y.applyAsDouble(result.y);

    g2.setColor(new Color(255, 200, 200, 120));
    g2.fillOval(mx - 12, my - 12, 24, 24);
    g2.setColor(Color.WHITE);
    g2.fillOval(mx - 8, my - 8, 16, 16);
    g2.setColor(MIN_COLOR);
    g2.fillOval(mx - 6, my - 6, 12, 12);

    // Легенда
    drawLegend(g2, w - marginR - 240, marginT + 12);
  }

  private void drawLegend(Graphics2D g2, int x, int y) {
    int boxW = 230;
    int boxH = 110;

    g2.setColor(new Color(255, 255, 255, 230));
    g2.fillRoundRect(x, y, boxW, boxH, 12, 12);
    g2.setColor(new Color(200, 205, 215));
    g2.setStroke(new BasicStroke(1f));
    g2.drawRoundRect(x, y, boxW, boxH, 12, 12);

    g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
    int lx = x + 14;
    int ly = y + 24;
    int lineH = 22;

    // Функция
    g2.setColor(FUNC_COLOR);
    g2.setStroke(new BasicStroke(2.5f));
    g2.drawLine(lx, ly - 4, lx + 26, ly - 4);
    g2.setColor(TEXT_COLOR);
    g2.drawString("функция f(x)", lx + 34, ly);

    // Огибающая
    ly += lineH;
    g2.setColor(ENV_COLOR);
    g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{6f, 4f}, 0f));
    g2.drawLine(lx, ly - 4, lx + 26, ly - 4);
    g2.setColor(TEXT_COLOR);
    g2.drawString("нижняя огибающая", lx + 34, ly);

    // Пробные точки
    ly += lineH;
    g2.setColor(Color.WHITE);
    g2.fillOval(lx + 8, ly - 9, 10, 10);
    g2.setColor(TRIAL_COLOR);
    g2.fillOval(lx + 9, ly - 8, 8, 8);
    g2.setColor(TEXT_COLOR);
    g2.drawString("пробные точки", lx + 34, ly);

    // Минимум
    ly += lineH;
    g2.setColor(Color.WHITE);
    g2.fillOval(lx + 7, ly - 10, 12, 12);
    g2.setColor(MIN_COLOR);
    g2.fillOval(lx + 9, ly - 8, 8, 8);
    g2.setColor(TEXT_COLOR);
    g2.drawString("найденный минимум", lx + 34, ly);
  }

  private static String fmtTick(double v) {
    double abs = Math.abs(v);
    if (abs < 1e-9) return "0";
    if (abs >= 1e5 || abs < 1e-3) {
      return String.format("%.1e", v);
    }
    if (abs >= 100) return String.format("%.0f", v);
    if (abs >= 10)  return String.format("%.1f", v);
    if (abs >= 1)   return String.format("%.2f", v);
    return String.format("%.3f", v);
  }
}
