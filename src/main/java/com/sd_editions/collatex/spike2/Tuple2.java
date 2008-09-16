package com.sd_editions.collatex.spike2;

public class Tuple2<T> {
  final T left;
  final T right;

  public Tuple2(T _left, T _right) {
    this.left = _left;
    this.right = _right;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Tuple2)) {
      return false;
    }
    Tuple2 other_tuple = (Tuple2) obj;
    return other_tuple.left.equals(left) && other_tuple.right.equals(right);
  }

  @Override
  public int hashCode() {
    return left.hashCode() + right.hashCode();
  }

  @Override
  public String toString() {
    return "{" + left.toString() + "; " + right.toString() + "}";
  }
}
