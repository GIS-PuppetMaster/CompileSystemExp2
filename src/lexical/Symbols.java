package lexical;

import java.util.HashMap;
import java.util.Map;

public class Symbols {

  public static Map<String, String> KEYWORD = new HashMap<String, String>();
  public static Map<String, String> OP_MAP = new HashMap<String, String>();
  public static Map<String, String> DELM_MAP = new HashMap<String, String>();
  
  public static Map<String, Integer> SYM_INT = new HashMap<String, Integer>();
  public static Map<Integer, String> INT_SYM = new HashMap<Integer, String>();
  
  // DFA信息，这里是用文件存储的形式
  public static String LETTER_ = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM_";
  public static String DIGIT = "0123456789";
  public static String DIGIT_P = "123456789";
  public static String DIGIT_O = "01234567";
  public static String DIGIT_H = "0123456789ABCDEF";
  public static String DELIMITER = "()[]{};\"\',:";
  public static String OP = "+-*/%&|=><!^";
  public static String IDENTIFIER_DFA = "<0,LETTER_,1> <1,LETTER_,1> <1,DIGIT,1>";
  public static String DELIMITER_DFA = "<0,DELIMITER,2>";
  public static String NOTES_1_DFA ="<0,'/',3> <3,'*',4> <4,other,4> <4,'*',5> <5,other,4> <5,'/',6>";
  public static String NOTES_2_DFA = "<0,'/',3> <3,'/',7>";

  public static String NUM_DECIMAL =
      "<0,DIGIT_P,9> <9,DIGIT,9> <9,'.',10> <10,DIGIT,11> <11,DIGIT,11>"
          + " <9,'E',12> <12,DIGIT,14> <14,DIGIT,14> <12,'+/-',13>"
          + " <13,DIGIT,14>";
  
  public static String NUM_OCTONARY =
      "<0,'0',15> <15,DIGIT_O,16> <16,DIGIT_O,16>";

  public static String NUM_HEXADECIMAL = "<0,'0',15> <15,'x',17> <17,DIGIT_H,18> <18,DIGIT_H,18>";
  public static String OP_DFA = "<0,OP,20> <20,OP,21>";
  public static String EMPTY = "empty";

  static {
    KEYWORD.put("int", "INTEGER");
    KEYWORD.put("float", "FLOAT");
    KEYWORD.put("real", "REAL");
    KEYWORD.put("double", "DOUBLE");
    KEYWORD.put("String", "STRING");
    KEYWORD.put("char", "CHAR");
    KEYWORD.put("boolean", "BOOLEAN");
    KEYWORD.put("struct", "STRUCT");
    KEYWORD.put("if", "IF");
    KEYWORD.put("for", "FOR");
    KEYWORD.put("while", "WHILE");
    KEYWORD.put("do", "DO");
    KEYWORD.put("else", "ELSE");
    KEYWORD.put("record", "RECORD");
    KEYWORD.put("then", "THEN");
    KEYWORD.put("proc", "PROC");
    KEYWORD.put("call", "CALL");
    KEYWORD.put("return", "RETURN");
    KEYWORD.put("switch", "SWITCH");
    KEYWORD.put("case", "CASE");
    KEYWORD.put("default", "DEFAULT");
    KEYWORD.put("not", "NOT");
    KEYWORD.put("and", "AND");
    KEYWORD.put("or", "OR");
    KEYWORD.put("true", "TRUE");
    KEYWORD.put("false", "FALSE");

    OP_MAP.put("-", "MINUS");
    OP_MAP.put("*", "MUL");
    OP_MAP.put("/", "DIV");
    OP_MAP.put("+", "PLUS");
    OP_MAP.put("%", "MOD");
    OP_MAP.put("=", "EQU");
    OP_MAP.put(">", "GT");
    OP_MAP.put("<", "LY");
    OP_MAP.put("&", "取地址");
    OP_MAP.put("++", "INC");
    OP_MAP.put("--", "DEC");
    OP_MAP.put("||", "OR");
    OP_MAP.put("&&", "AND");
    OP_MAP.put("!=", "NE");
    OP_MAP.put("==", "EQ");
    OP_MAP.put("<=", "LE");
    OP_MAP.put(">=", "GE");
    OP_MAP.put("+=", "+=");
    OP_MAP.put("-=", "-=");
    OP_MAP.put("*=", "*=");
    OP_MAP.put("/=", "/=");

    // 两种注释
    OP_MAP.put("//", "注释");
    OP_MAP.put("/*", "注释");

    DELM_MAP.put("(", "SLP");
    DELM_MAP.put(")", "SRP");
    DELM_MAP.put("{", "LP");
    DELM_MAP.put("}", "RP");
    DELM_MAP.put(";", "SEMI");
    DELM_MAP.put("[", "LSB");
    DELM_MAP.put("]", "RSB");
    DELM_MAP.put("\"", "\"");
    DELM_MAP.put("\'", "\'");
    DELM_MAP.put(",", ",");
    DELM_MAP.put(":", ":");
  }

}
