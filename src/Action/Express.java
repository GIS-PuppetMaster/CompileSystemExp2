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
  Set<String> hopingSymbol = new HashSet<String>();
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
  public void addHopingSymbol(String s){
    hopingSymbol.add(s);
  }
  public Set<String> getHopingSymbols(){
    return new HashSet<String>(hopingSymbol);
  }
  public void setHopingSymbols(Set<String> hopingSymbol){
    this.hopingSymbol = hopingSymbol;
    return;
  }
}