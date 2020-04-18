package grammar;

import java.util.List;

public class Production {

  private String left;
  private List<String> right;
  
  public Production(String left, List<String> right) {
    this.left = left;
    this.right = right;
  }
  
  public String getLeft() {
    return left;
  }
  
  public List<String> getRight() {
    return right;
  }
  
  
  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append(left + " -> ");
    for(String s:right) {
      str.append(s + " ");
    }
    return str.toString();
  }
  
  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + left.hashCode();
    result = 31 * result + right.hashCode();
    return result;
  }
  
  @Override
  public boolean equals(Object that) {
    if(this == that) {
      return true;
    }
    if(that == null) {
      return false;
    }
    if(that instanceof grammar.Production) {
      grammar.Production other = (grammar.Production)(that);
      return other.getLeft().equals(this.left) && other.getRight().equals(this.right);
    }
    return false;
  }
}
