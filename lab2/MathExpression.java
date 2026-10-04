package lab2;

public class MathExpression {
  private final String s;
  private int pos;
  private double x;

  public MathExpression(String expression) {
    this.s = expression.replace(" ", "").toLowerCase();
  }

  public double eval(double x) {
    this.x = x;
    this.pos = 0;
    return expr();
  }

   /**
   * Функция складывает и вычитает слагаемые, пока есть + или -
   */
  private double expr() {
    double v = term();
    while (pos < s.length()) {
      char c = s.charAt(pos);
      if (c == '+') { pos++; v += term(); }
      else if (c == '-') { pos++; v -= term(); }
      else break;
    }
    return v;
  }

  /**
   * Функция умножает и делит множители, пока есть * или /
   */
  private double term() {
    double v = factor();
    while (pos < s.length()) {
      char c = s.charAt(pos);
      if (c == '*') { pos++; v *= factor(); }
      else if (c == '/') { pos++; v /= factor(); }
      else break;
    }
    return v;
  }

  /**
   * Функция возводит в степень
   */
  private double factor() {
    double v = unary();
    if (pos < s.length() && s.charAt(pos) == '^') {
      pos++;
      v = Math.pow(v, factor());
    }
    return v;
  }

  /**
   * Функция обрабатывает унарные + и -
   */
  private double unary() {
    if (pos < s.length()) {
      char c = s.charAt(pos);
      if (c == '+') { pos++; return  unary(); }
      if (c == '-') { pos++; return -unary(); }
    }
    return primary();
  }

  /**
   * Функция обрабатывает скобки, числа, имена (pi, e, x ..)
   */
  private double primary() {
    if (pos < s.length() && s.charAt(pos) == '(') {
      pos++;
      double v = expr();
      if (pos < s.length() && s.charAt(pos) == ')') pos++;
      return v;
    }
    if (pos >= s.length()) throw new RuntimeException("Неожиданный конец выражения");

    char c = s.charAt(pos);
    if (Character.isDigit(c) || c == '.') return number();
    if (Character.isLetter(c)) return name();
    throw new RuntimeException("Неожиданный символ: " + c);
  }

  /**
   * Функция читает число из строки
   */
  private double number() {
    int start = pos;
    while (pos < s.length()
      && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.')) pos++;
    return Double.parseDouble(s.substring(start, pos));
  }

  /**
   * Функция читает имя (x, pi, e ..) или имя функции (sin(), cos() ..)
   */
  private double name() {
    int start = pos;
    while (pos < s.length() && Character.isLetter(s.charAt(pos))) pos++;
    String n = s.substring(start, pos);

    if (n.equals("x")) return x;
    if (n.equals("pi")) return Math.PI;
    if (n.equals("e")) return Math.E;

    if (pos < s.length() && s.charAt(pos) == '(') pos++;
    double arg = expr();
    if (pos < s.length() && s.charAt(pos) == ')') pos++;

    switch (n) {
      case "sin": return Math.sin(arg);
      case "cos": return Math.cos(arg);
      case "tan": return Math.tan(arg);
      case "asin": return Math.asin(arg);
      case "acos": return Math.acos(arg);
      case "atan": return Math.atan(arg);
      case "exp": return Math.exp(arg);
      case "log":
      case "ln": return Math.log(arg);
      case "sqrt": return Math.sqrt(arg);
      case "abs": return Math.abs(arg);
      default: throw new RuntimeException("Неизвестная функция: " + n);
    }
  }
}