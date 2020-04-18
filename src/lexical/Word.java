package lexical;

import java.util.ArrayList;
import java.util.List;

public class Word {

  private String text;
  private int length;
  private int row_num = 1;

  private List<String[]> lexical_message = new ArrayList<String[]>();
  private List<String[]> dfa_message = new ArrayList<String[]>();
  private List<String[]> error_message = new ArrayList<String[]>();

  public List<String[]> token = new ArrayList<String[]>();
  
  public List<String[]> getToken() {
    return token;
  }

  public void setToken(List<String[]> token) {
    this.token = token;
  }

  public Word(String text) {
    this.text = text;
    length = text.length();
  }

  public void analyse() {
    int cur = 0;
    while (cur < length) {
      if (text.charAt(cur) == '\n' || text.charAt(cur) == '\r') {
        row_num++;
      } else if (text.charAt(cur) == ' ') {
        cur++;
        continue;
      }
      cur = handleChar(cur);
    }
  }

  public int handleChar(int cur) {
    if (Symbols.LETTER_.indexOf(text.charAt(cur)) != -1) {
      cur = isID(cur);
    } else if (Symbols.DIGIT.indexOf(text.charAt(cur)) != -1) {
      cur = isNum(cur);
    } else if (Symbols.DELIMITER.indexOf(text.charAt(cur)) != -1) {
      cur = isDELI(cur);
    } else if (Symbols.OP.indexOf(text.charAt(cur)) != -1) {
      cur = isOP(cur);
    } else {
      cur++;
    }
    return cur;
  }

  // 标识符
  public int isID(int cur) {
    String word = "";
    while (cur < length && (Symbols.DIGIT.indexOf(text.charAt(cur)) != -1 || Symbols.LETTER_.indexOf(text.charAt(cur)) != -1)) {
      word += String.valueOf(text.charAt(cur));
      cur++;
    }
    
    // 关键字
    if (Symbols.KEYWORD.containsKey(word)) {
      printInfo(word, Symbols.KEYWORD.get(word), Symbols.IDENTIFIER_DFA, row_num, "关键字");
    } else {
      printInfo(word, "IDN", Symbols.IDENTIFIER_DFA, row_num, "标识符");
    }
    return cur;
  }

  // 界符
  public int isDELI(int cur) {
    String word = String.valueOf(text.charAt(cur));
    printInfo(word, Symbols.DELM_MAP.get(word), Symbols.DELIMITER_DFA, row_num, "界符");
    cur++;
    return cur;
  }

  // 识别运算符
  public int isOP(int cur) {
    String word = "";
    while (cur < length && Symbols.OP.indexOf(text.charAt(cur)) != -1 && word.length() < 2) {
      word += String.valueOf(text.charAt(cur));
      cur++;
    }
    // 单个运算符
    if (word.length() == 1) {
      printInfo(word, Symbols.OP_MAP.get(word), Symbols.OP_DFA,
          row_num, "运算符");
    } else if (word.length() == 2) {
      if (Symbols.OP_MAP.containsKey(word)) {
        // 注释
        if (word.equals("/*")) {
          cur = isNOTES_1(cur);
        } else if (word.equals("//")) {
          cur = isNOTES_2(cur);
        } else {// 匹配的运算符
          printInfo(word, Symbols.OP_MAP.get(word), Symbols.NUM_DECIMAL, row_num, "运算符");
        }
      // 2个运算符，没有对应的
      } else {
        // 形如，“x/*”或“x//”，x是运算符
        boolean flag = cur < length ? word.charAt(1) == '/' && (text.charAt(cur) == '*' || text.charAt(cur) == '/') : false;
        if (flag) {
          word = String.valueOf(word.charAt(0));
          printInfo(word, Symbols.OP_MAP.get(word), Symbols.OP_DFA, row_num, "运算符");
          // 回退到“/”，重新识别
          cur--;
        } else {
          printInfo(word, "不存在的运算符", null, row_num, "wrong");
        }
      }
    }
    return cur;
  }

  public int isNOTES_1(int cur) {
    boolean isNoteEnd = false;
    boolean isTextEnd = false;
    while (cur < length) {
      if (text.charAt(cur) == '*') {
        try {
          if (text.charAt(cur + 1) == '/') {
            cur += 2;
            printInfo("/**/", Symbols.OP_MAP.get("/*"), Symbols.NOTES_1_DFA, row_num, "注释");
            isNoteEnd = true;
            break;
          }
        } catch (Exception e) {
          // 溢出
          printInfo("/*", "缺少*/", null, row_num, "wrong");
          cur = length;
          // 防止重复打印的标志
          isTextEnd = true;
          break;
        }
      } else if (text.charAt(cur) == '\n' || text.charAt(
          cur) == '\r') {
        row_num++;
      }
      cur++;
    }
    if (cur == length && !isNoteEnd && !isTextEnd) {
      printInfo("/*", "缺少*/", null, row_num, "wrong");
    }
    return cur;
  }

  // 识别这种形式的注释//
  public int isNOTES_2(int cur) {
    while (cur < length && text.charAt(cur) != '\n') {
      cur++;
    }
    printInfo("//", Symbols.OP_MAP.get("//"), Symbols.NOTES_2_DFA,
        row_num, "注释");
    return cur;
  }

  // 数字
  public int isNum(int cur) {
    String word = "";
    if (cur < length && Symbols.DIGIT_P.indexOf(text.charAt(cur)) != -1) {
      cur = isDecimal(cur);
    } else if (cur < length && '0' == text.charAt(cur)) {
      word += text.charAt(cur);
      cur++;
      try {
        if (text.charAt(cur) == '.') {
          word += text.charAt(cur);
          cur++;
          cur = isFraction(cur, word);
        } else if (text.charAt(cur) == 'x') {
          cur++;
          cur = isHexadecimal(cur);
        } else if (Symbols.DIGIT.indexOf(text.charAt(cur)) != -1) {
          word += text.charAt(cur);
          cur = isOctonary(cur, word);
        } else {
          word = "0";
          printInfo(word, "NUM", Symbols.NUM_DECIMAL, row_num, "十进制数字");
        }
      } catch (Exception e) {
        printInfo(word, "NUM", Symbols.NUM_DECIMAL, row_num, "十进制数字");
      }
    }
    return cur;
  }

  public int isOctonary(int cur, String word) {
    if (Symbols.DIGIT_O.indexOf(text.charAt(cur)) != -1) {
      cur++;
      try {
        while (cur < length && Symbols.DIGIT_O.indexOf(text.charAt(cur)) != -1) {
          word += String.valueOf(text.charAt(cur));
          cur++;
        }
      } catch (Exception e) {
        
      }
      printInfo(word, "NUM", Symbols.NUM_OCTONARY, row_num, "八进制数字");
    } else {
      printInfo(word, "不规范的八进制数", null, row_num, "wrong");
      cur++;
    }
    return cur;
  }

  public int isHexadecimal(int cur) {
    String word = "0x";
    try {
      if (Symbols.DIGIT_H.indexOf(text.charAt(cur)) != -1) {
        while (cur < length && Symbols.DIGIT_H.indexOf(text.charAt(cur)) != -1) {
          word += String.valueOf(text.charAt(cur));
          cur++;
        }
        printInfo(word, "NUM", Symbols.NUM_HEXADECIMAL, row_num, "十六进制数字"); // 不是最后位置
      } else { 
        word += text.charAt(cur);
        cur++;
        printInfo(word, "不规范的十六进制数", null, row_num, "wrong");
      }
    } catch (Exception e) {
      printInfo(word, "不规范的十六进制数", null, row_num, "wrong");
    }
    return cur;
  }

  public int isDecimal(int cur) {
    String word = "";
    while (cur < length && Symbols.DIGIT.indexOf(text.charAt(cur)) != -1) {
      word += String.valueOf(text.charAt(cur));
      cur++;
    }
    if (cur == length && Symbols.DIGIT.indexOf(text.charAt(cur - 1)) != -1) {
      printInfo(word, "NUM", Symbols.NUM_DECIMAL, row_num, "十进制数字");
    } else {
      try {
        if (text.charAt(cur) == '.') {// 小数
          word += text.charAt(cur);
          cur++;
          cur = isFraction(cur, word);
        } else if (text.charAt(cur) == 'E') {// 指数
          word += text.charAt(cur);
          cur++;
          cur = isExponential(cur, word);
        } else {// 整数，并且不是最后位置
          printInfo(word, "NUM", Symbols.NUM_DECIMAL, row_num, "十进制数字");
        }
      } catch (Exception e) {
        printInfo(word, "不规范的数", null, row_num, "wrong");
      }
    }
    return cur;
  }

  // 指数
  public int isExponential(int cur, String word) {
    if (Symbols.DIGIT.indexOf(text.charAt(cur)) != -1 || text.charAt(cur) == '+' || text.charAt(cur) == '-') {// E后是数字，或+，或-
      if (Symbols.DIGIT.indexOf(text.charAt(cur)) == -1) {
        word += text.charAt(cur);
        cur++;
        if (Symbols.DIGIT.indexOf(text.charAt(cur)) == -1) {
          printInfo(word, "不规范的指数形式", null, row_num, "wrong");
          return cur;
        }
      }
      while (cur < length && Symbols.DIGIT.indexOf(text.charAt(cur)) != -1) {
        word += text.charAt(cur);
        cur++;
      }
      printInfo(word, "NUM", Symbols.NUM_DECIMAL, row_num, "十进制数字");
    } else {
      word += text.charAt(cur);
      printInfo(word, "不规范的指数形式", null, row_num, "wrong");
      cur++;
    }
    return cur;
  }

  // 小数
  public int isFraction(int cur, String word) {
    if (Symbols.DIGIT.indexOf(text.charAt(cur)) == -1) {
      word += text.charAt(cur);
      printInfo(word, "不规范的小数形式", null, row_num, "wrong");
      cur++;
    } else {
      while (cur < length && Symbols.DIGIT.indexOf(text.charAt(cur)) != -1) {
        word += text.charAt(cur);
        cur++;
      }
      printInfo(word, "NUM", Symbols.NUM_DECIMAL, row_num, "十进制数字");
    }
    return cur;
  }
  
  private boolean isGrammarAnalyse = true;

  // 打印输出
  public void printInfo(String word, String byWord, String dfa, int row_num, String kind) {
    String[] tuple = new String[4];
    boolean isWrong = kind.equals("wrong");
    if (!isGrammarAnalyse) {
      if (isWrong) {
        error_message.add(new String[] {word, byWord, String.valueOf(row_num)});
      } else {
        if (byWord != null) {
          if (byWord.equals("NUM")) {
            lexical_message.add(new String[] {word, "< " + byWord + " , " + word + " >", String.valueOf(row_num), kind});
          } else if (byWord.equals("IDN")) {
            lexical_message.add(new String[] {word, "< " + byWord + " , " + word + ">", String.valueOf(row_num), kind});
          } else {
            lexical_message.add(new String[] {word, "< " + byWord + " , _ >", String.valueOf(row_num), kind});
          }
          dfa_message.add(new String[] {word, dfa});
        } else {
          System.out.println("空" + word + "," + kind);
        }
      }
    } else {
      if (isWrong) {
        tuple[2] = String.valueOf(row_num);
        if (byWord.equals("缺少*/")) {
          tuple[0] = null;
          tuple[1] = "NOTE";
          tuple[3] = "缺少*/";
          token.add(tuple);
          return;
        } else if (byWord.equals("不规范的八进制数")) {
          tuple[0] = "0";
          tuple[1] = "NUM";
          tuple[3] = "不规范的八进制数：" + word;
        } else if (byWord.equals("不规范的十六进制数")) {
          tuple[0] = "0";
          tuple[1] = "NUM";
          tuple[3] = "不规范的十六进制数：" + word;
        } else if (byWord.equals("不规范的数")) {
          tuple[0] = "0";
          tuple[1] = "NUM";
          tuple[3] = "不规范的数：" + word;
        } else if (byWord.equals("不规范的指数形式")) {
          tuple[0] = "0";
          tuple[1] = "NUM";
          tuple[3] = "不规范的指数形式：" + word;
        } else if (byWord.equals("不规范的小数形式")) {
          tuple[0] = "0";
          tuple[1] = "NUM";
          tuple[3] = "不规范的小数形式：" + word;
        } else if (byWord.equals("不存在的运算符")) {
          tuple[0] = String.valueOf(word.charAt(0));
          tuple[1] = "OP";
          tuple[3] = "不存在的运算符：" + word;
        }
        token.add(tuple);
      } else {
        if (!word.equals("/**/") && !word.equals("//")) {
          tuple[0] = word;
          tuple[2] = String.valueOf(row_num);
          if (byWord != null) {
            if (byWord.equals("NUM")) {
              tuple[1] = "NUM";
            } else if (byWord.equals("IDN")) {
              tuple[1] = "IDN";
            }
          } else {
            System.out.println("空" + word + "," + kind);
          }
          token.add(tuple);
        }
      }
    }
  }
}
