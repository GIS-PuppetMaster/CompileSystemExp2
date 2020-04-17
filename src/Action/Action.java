package Action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Action {
  //存储读入的文法表达式
  List<Express> proList = new ArrayList<Express>();
  //存储所有非终结符
  Set<String> virSet = new HashSet<String>();
  
  public void init() throws IOException {
    File synFile = new File("syn.txt");
    BufferedReader fileReader = new BufferedReader(new FileReader(synFile));
    String line = null;
    while ((line = fileReader.readLine()) != null) {
      String[] words = line.split("->");
      String[] right = words[2].split("\\|");
      for (int i = 0; i < right.length; i++) {
        proList.add(new Express(words[0], right[i]));
      }
      virSet.add(words[0]);
      
    }
  }
 
  
  public void getFirst(String[] string) {
    
  }
  
  


  /**
   * 判断是不是终结符，如果左边没这个作为开头的，那就是终结符了。
   * @param str
   * @return
   */
  public boolean isVariable(String str) {
    for (Iterator<Express> iterator = proList.iterator(); iterator
        .hasNext();) {
      Express production = (Express) iterator.next();
      if (production.getLeft().equals(str)) {
        // 一旦找到左边有等于str的字符，就说明str不算终结符，返回真：是变量
        return true;
      }
    }
    return false;
  }


  /**
   * 判断是不是空产生式集
   * @param str
   * @return
   */
  public boolean isEmpty(String str) {
    for (Iterator<Express> iterator = proList.iterator(); iterator
        .hasNext();) {
      Express production = (Express) iterator.next();
      if (production.getLeft().equals(str)) {
        for (int i = 0; i < production.getRight().length; i++) {
          // System.out.println(production.getRight()[i]);
          if (production.getRight()[i].equals("null")) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * 返回包含这个左部的产生式集合，
   * @param B
   * @param productions
   * @return
   */
  public List<Express> findLeft(String B) {
    List<Express> list = new ArrayList<>();
    for (Iterator<Express> iterator = proList.iterator(); iterator
        .hasNext();) {
      Express production = (Express) iterator.next();
      // System.out.println(production.getLeft());
      if (production.getLeft().equals(B)) {
        list.add(production);
      }
    }
    return list;
  }

  /**
   * 获取非终结符号的产生式的first集合X->Y1Y2Y3Y4Y5……这样的,
   * @param str X
   * @return
   */
  public List<String> getFirstItem(Express production) {
    List<String> list = new ArrayList<>();// 获取包含这个str左部的产生式
    // 遍历这个产生式的每一项，其中每个产生式的每一项也需要遍历。
    for (int i = 0; i < production.getRight().length; i++) {
      if (!production.getLeft().equals(production.getRight()[i])) {
        list.addAll(getFirst(production.getRight()[i]));
        System.out.println(production.getRight()[i]);
      } // 没有左递归
      if (!isEmpty(production.getRight()[i])) {
        // 这个项里没有包含空产生式的话，就继续求解，否则结束。
        return list;
      }

    }
    return list;
  }

  /**
   * 判断是不是空产生式集
   * @param strings
   * @return
   */
  public boolean isEmpty(String[] strings) {
    for (int i = 0; i < strings.length; i++) {
      if (strings[i].equals("null")) {
        return true;
      }
    }
    return false;
  }


  /**
   * 获取first集合
   * @param str
   * @return
   */
  public List<String> getFirst(String str) {
    List<String> list = new ArrayList<>();
    List<Express> productions = findLeft(str);
    System.out.println(productions);
    for (Iterator<Express> iterator = productions.iterator(); iterator
        .hasNext();) {
      Express production = (Express) iterator.next();
      if (isEmpty(production.getRight())) {
        System.out.println("-------------------null------------------");
        // 检查X->null是否成立
        list.add("null");
      } else if (!isVariable(production.getRight()[0])
          && !isEmpty(production.getRight())) {
        // 是终结符的话就直接加入。
        System.out.println("-------------------vict------------------");
        list.add(production.getRight()[0]);
      } else {
        System.out.println("-------------------set------------------");
        list.addAll(getFirstItem(production));
      }
    }
    return list;
  }
  
  
  
  
  public Set<Express> closure_method(Set<Express> project) {
    for (Express express : project) {
      //表示非归约状态
      if (express.getIndex() < express.getRight().length) {
        List<Express> avail = findLeft(express.getRight()[express.getIndex()]);
        for (int i = 0; i < avail.size(); i++) {
          Express add = avail.get(i);
          add.setIndex(0);
          List<String> first = getFirst(express.getRight()[express.getIndex()]+express.getHopingSymbols());
          for (String hope : first) {
            Set<String> hopeSet = new HashSet<String>();
            hopeSet.add(hope);
            add.setHopingSymbols(hopeSet);
            project.add(add);
          }
        }
      } else {
        //归约状态直接返回
        return project;
      }
    }
    return project;
  }
  
  public Set<Express> goto_method(Set<Express> project, String next) {
    Set<Express> jSet = new HashSet<Express>();
    for (Express express : project) {
      Express newExp = new Express(express.getLeft(), express.getTail());
      newExp.setIndex(express.getIndex()+1);
      newExp.setHopingSymbols(newExp.getHopingSymbols());
    }
    return jSet;
  }


  public List<Express> getProList() {
    return proList;
  }

  public Set<String> getVirSet() {
    return virSet;
  }

}