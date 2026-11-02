package lab1;

public class Result {
  public enum Status { OPTIMAL, INFEASIBLE, UNBOUNDED, ITER_LIMIT }
  public Status status;
  public double[] x;
  public double z;
}
