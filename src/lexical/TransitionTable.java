package lexical;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransitionTable {

  private static String letter_ = "[a-zA-Z]|_";
  private static String letter_digit = "[a-zA-Z0-9]|_";
  private static String letter_x = "x";
  private static String letter_E = "E";
  
  private static String digit = "[0-9]";
  private static String digit_zero = "0";
  private static String digit_1to7 = "[1-7]";
  private static String digit_0to7 = "[0-7]";
  private static String digit_1to9 = "[1-9]";
  private static String digit_1to9_letter_af = "[1-9a-f]";
  private static String digit_0to9_letter_af = "[0-9a-f]";
  
  private static String operator = "^(\\+|-|\\*|%|>|<|\\||&|!)$";
  
  private static String operator_with_equal = "^(\\+|-|\\*|%|>|<|\\||&|!|=)$";
  private static String operator_divisor = "^/$";
  private static String operator_mult = "^\\*$";
  private static String operator_add_dec = "\\+|-";
  
  private static String decimal_point = "\\.";
  private static String quote = "'";
  

  private static String delimiter = "\\{|\\}|\\[|\\]|\\(|\\)|,|;|:|\\?";
  private static String delimiter_equal = "=";
  private static String symbol_except_mult = "[^\\*]";
  private static String symbol_except_mult_divisor = "[^(\\*|/)]";
  
  
  private Map<Integer, Map<String, Integer>> dfs;
  private Map<String, String> regexs;
  
  public TransitionTable() {
    dfs = new HashMap<Integer, Map<String, Integer>>();
    initialDfa();
    regexs = new HashMap<String, String>();
    regexs.put("letter_", letter_);
    regexs.put("letter_digit", letter_digit);
    regexs.put("letter_x", letter_x);
    regexs.put("letter_E", letter_E);
    regexs.put("digit", digit);
    regexs.put("digit_zero", digit_zero);
    regexs.put("digit_1to7", digit_1to7);
    regexs.put("digit_0to7", digit_0to7);
    regexs.put("digit_1to9", digit_1to9);
    regexs.put("digit_1to9_letter_af", digit_1to9_letter_af);
    regexs.put("digit_0to9_letter_af", digit_0to9_letter_af);
    regexs.put("operator", operator);
    regexs.put("operator_divisor", operator_divisor);
    regexs.put("operator_mult", operator_mult);
    regexs.put("operator_add_dec", operator_add_dec);
    regexs.put("decimal_point", decimal_point);
    regexs.put("quote", quote);
    regexs.put("delimiter", delimiter);
    regexs.put("symbol_except_mult", symbol_except_mult);
    regexs.put("delimiter_equal", delimiter_equal);
    regexs.put("operator_with_equal", operator_with_equal);
    regexs.put("symbol_except_mult_divisor", symbol_except_mult_divisor);
  }
  
  public void initialDfa() {
    File file = new File("src/lexical/DFA.txt");
    BufferedReader bufferedReader = null;
    try {
      InputStreamReader read = new InputStreamReader(new FileInputStream(file));
      bufferedReader = new BufferedReader(read);
      String line = null;
      while((line = bufferedReader.readLine()) != null) {
          String[] words = line.split("\t");
          int start_state = Integer.parseInt(words[0]);
          String symbol = words[1];
          int end_state = Integer.parseInt(words[2]);
          if(dfs.get(start_state) == null) {
            Map<String, Integer> newmap = new HashMap<String, Integer>();
            newmap.put(symbol, end_state);
            dfs.put(start_state, newmap);
          } else {
            Map<String, Integer> map = dfs.get(start_state);
            map.put(symbol, end_state);
          }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        bufferedReader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  
  public int transfer(int start_state, char chr) {
    int end_state = 0;
    if(dfs.get(start_state) == null) {
      return end_state;
    }
    Map<String, Integer> map = dfs.get(start_state);
    for(Map.Entry<String, Integer> entry : map.entrySet()) {
      String regex = entry.getKey();
      Matcher m = Pattern.compile(regexs.get(regex)).matcher(String.valueOf(chr));
      if(m.find()) {
        end_state = entry.getValue();
        break;
      }
    }
    return end_state;
  }
  
  public static void main(String[] args) {
    TransitionTable test = new TransitionTable();

    int start_state = 4;
    char ch = '*';
    System.out.println(test.transfer(start_state, ch));

  }
}
