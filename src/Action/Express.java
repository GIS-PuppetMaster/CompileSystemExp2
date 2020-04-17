package Action;

import java.util.HashSet;
import java.util.Set;

public class Express {
  String head;
  String tail;
  int index;
  public int getIndex() {
    return index;
  }
  public void setIndex(int index) {
    this.index = index;
  }
  String[] tailStrings;
  String hopingSymbol;
  public Express(String head, String tail) {
    this.head = head;
    this.tail = tail;
    tailStrings = tail.split("\\s");
  }

  public String getLeft() {
    return head;
  }
  public void setHead(String head) {
    this.head = head;
  }
  public String[] getRight() {
    return tailStrings;
  }
  public void setTail(String tail) {
    this.tail = tail;
  }
  public String getTail() {
    return tail;
  }
  public String getHopingSymbols(){
    return hopingSymbol;
  }
  public void setHopingSymbols(String hopingSymbol){
    this.hopingSymbol = hopingSymbol;
    return;
  }
}