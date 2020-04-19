package lexical;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LexicalAnalyzer {
  
  private static Map<String, String> symbols;
  private static TransitionTable dfa;
  private static String fileName;
  
  private static List<String> tokenMessage;
  private static List<List<String>> tokens;
  private static List<String> errors;
  private static Map<String, String> symbol_table;
  
  
  public LexicalAnalyzer(String fileName) {
    LexicalAnalyzer.fileName = fileName;
  }
  
  public void scanner() {
    tokenMessage = new ArrayList<String>();
    tokens = new ArrayList<List<String>>();
    errors = new ArrayList<String>();
    symbol_table = new HashMap<String, String>();
    
    initialSymbols("src/lexical/symbols.txt");
    dfa = new TransitionTable();
    List<String> text = readFile(fileName);
    int line_number = 1;
    for(String str:text) {
      if(!str.equals("")) {
        lexicalAnalyze(str, line_number);
      }
      line_number ++;
    }  
  }
  
  public void lexicalAnalyze(String line, int line_number) {
    char[] chars = line.toCharArray();
    int loc=0;
    while(true) {
      int start_state = 0;
      int end_state = 0;
      StringBuilder word = new StringBuilder();
      
      while(loc < chars.length - 1 && (chars[loc] == ' ' || String.valueOf(chars[loc]).equals("\t"))) {
        loc++;
      }
      
      while(true) {
        start_state = end_state;
        char now_chr = chars[loc];
        end_state = dfa.transfer(start_state, now_chr);
        if(end_state == 0) {
          break;
        } else {
          word.append(String.valueOf(now_chr));
          loc++;
          if(loc >= chars.length) {
            break;
          }
        }
      }
      
      while(loc < chars.length && (chars[loc] == ' ' || String.valueOf(chars[loc]).equals("\t"))) {
        loc++;
      }
      
      String str = word.toString();
      String output = "";
      List<String> token = new ArrayList<String>();
      switch(start_state) {
        
        case 0:
          if(end_state == 0) {
            output = String.valueOf(chars[loc]) + "\t" + "Invalid symbol\t" + "line: " + line_number;
            if(loc < chars.length) {
              loc++;
            }
            errors.add(output);
          } else if(end_state == 17) {
            output = String.valueOf(chars[loc-1]) + "\t" + "Wrong char constant\t" + "line: " + line_number;
            errors.add(output);
          } else {
            if(end_state == 1) {
              output = str + "\t" + "< " + "IDN" + ", " + str + ">";
              symbol_table.put(str, "IDN");
              token.add("id");
              // ******************************
              token.add(str);
              token.add(String.valueOf(line_number));
            } else if(end_state == 7 || end_state == 11) {
              output = str + "\t" + "< " + "CONST" + ", " + str + " >";
              
              token.add("num");
              token.add(str);
              token.add(String.valueOf(line_number));
            } else {
              output = str + "\t" + "< " + symbols.get(str) + ", _ >";
              
              token.add(symbols.get(str));
              token.add("_");
              token.add(String.valueOf(line_number));
            }
        
            tokenMessage.add(output);
          }
          break;
        case 1:
          boolean flag = false;
          for(Map.Entry<String, String> entry:symbols.entrySet()) {
            if(entry.getKey().equals(str)) {
              flag = true;
            }
          }
          if(flag) {
            output = str + "\t" + "< " + symbols.get(str) + ", _ >";
            tokenMessage.add(output);
            
            token.add(symbols.get(str));
            token.add("_");
            token.add(String.valueOf(line_number));
          } else {
            output = str + "\t" + "< " + "IDN" + ", " + str + "> ";
            symbol_table.put(str, "IDN");
            tokenMessage.add(output);
            
            token.add("id");
            token.add(str);
            token.add(String.valueOf(line_number));
          }
          break;
        case 2:
          output = str + "\t" + "< " + symbols.get(str) + ", _ >";
          
          token.add(symbols.get(str));
          token.add("_");
          token.add(String.valueOf(line_number));
          tokenMessage.add(output);
          break;
        case 3:
          output = str + "\t" + "< " + "DIV" + ", _ >";
          
          token.add("/");
          token.add("_");
          token.add(String.valueOf(line_number));
          tokenMessage.add(output);
          break;
        case 4:
          output = str + "\t" + "Wrong note format\t" + "line: " + line_number;
          errors.add(output);
          break;
        case 5:
          if(end_state != 6) {
            output = str + "\t" + "Wrong note format\t" + "line: " + line_number;
            errors.add(output);
          } else {
            output = str + "\t" + "< " + "NOTE" + ", _ >";
            tokenMessage.add(output);
            break;
          }
          break;
        case 6:
          output = str + "\t" + "< " + "NOTE" + ", _ >";
          tokenMessage.add(output);
          break;
        case 7:
          if(end_state != 9) {
            output = str + "\t" + "< " + "CONST" + ", " + str + " >";
            tokenMessage.add(output);
            
            token.add("num");
            token.add(str);
            token.add(String.valueOf(line_number));
          } else {
            output = str + "\t" + "Wrong numeric constant\t" + "line: " + line_number;
            errors.add(output);
          }
          break;
        case 8:
          output = str + "\t" + "< " + "CONST" + ", " + str + " >";
          tokenMessage.add(output);
          
          token.add("num");
          token.add(str);
          token.add(String.valueOf(line_number));
          break;
        case 9:
          if(end_state == 0) {
            output = str + "\t" + "Wrong numeric constant\t" + "line: " + line_number;
            errors.add(output);
          }
          if(end_state == 10) {
            output = str + "\t" + "< " + "CONST" + ", " + str + " >";
            tokenMessage.add(output);
            
            token.add("num");
            token.add(str);
            token.add(String.valueOf(line_number));
          }
          break;
        case 10:
          output = str + "\t" + "< " + "CONST" + ", " + str + " >";
          tokenMessage.add(output);
          
          token.add("num");
          token.add(str);
          token.add(String.valueOf(line_number));
          break;
        case 11:
          if(end_state == 12) {
            output = str + "\t" + "Wrong numeric constant\t" + "line: " + line_number;
            errors.add(output);
          } else if(end_state == 14) {
            output = str + "\t" + "Wrong numeric constant\t" + "line: " + line_number;
            errors.add(output);
          } else {
            output = str + "\t" + "< " + "CONST" + ", " + str + " >";
            tokenMessage.add(output);
            
            token.add("num");
            token.add(str);
            token.add(String.valueOf(line_number));
          }
          break;
        case 12:
          if(end_state == 0) {
            output = str + "\t" + "Wrong numeric constant\t" + "line: " + line_number;
            errors.add(output);
          }
          if(end_state == 13) {
            output = str + "\t" + "< " + "CONST" + ", " + str + " >";
            tokenMessage.add(output);
            
            token.add("num");
            token.add(str);
            token.add(String.valueOf(line_number));
          }
          break;
        case 13:
          if(end_state != 14) {
            output = str + "\t" + "< " + "CONST" + ", " + str + " >";
            tokenMessage.add(output);
            
            token.add("num");
            token.add(str);
            token.add(String.valueOf(line_number));
          } else {
            output = str + "\t" + "Wrong numeric constant\t" + "line: " + line_number;
            errors.add(output);
          }
          break;
        case 14:
          if(end_state == 16) {
            output = str + "\t" + "< " + "CONST" + ", " + str + " >";
            tokenMessage.add(output);
            
            token.add("num");
            token.add(str);
            token.add(String.valueOf(line_number));
          } else {
            output = str + "\t" + "Wrong numeric constant\t" + "line: " + line_number;
            errors.add(output);
          }
          break;
        case 15:
          if(end_state == 16) {
            output = str + "\t" + "< " + "CONST" + ", " + str + " >";
            tokenMessage.add(output);
            
            token.add("num");
            token.add(str);
            token.add(String.valueOf(line_number));
          } else {
            output = str + "\t" + "Wrong numeric constant\t" + "line: " + line_number;
            errors.add(output);
          }
          break;
        case 16:
          output = str + "\t" + "< " + "CONST" + ", " + str +" >";
          tokenMessage.add(output);
          
          token.add("num");
          token.add(str);
          token.add(String.valueOf(line_number));
          break;
        case 17:
          output = str + "\t" + "Wrong char constant\t" + "line: " + line_number;
          errors.add(output);
          break;
          
        // **************字符类型的token  
        case 18:
          if(end_state == 19) {
            output = str + "\t" + "< " + "CONST" + ", " + str +" >";
            tokenMessage.add(output);
            
            token.add("CONST");
            token.add(str);
            token.add(String.valueOf(line_number));
          } else {
            output = str + "\t" + "Wrong char constant\t" + "line: " + line_number;
            errors.add(output);
          }
          break;
        case 19:
          output = str + "\t" + "< " + "CONST" + ", " + str +" >";
          tokenMessage.add(output);
          
          token.add("CONST");
          token.add(str);
          token.add(String.valueOf(line_number));
          break;
        case 20:
          if(symbols.get(str) == null) {
            output = str + "\t" + "Wrong operator\t" + "line: " + line_number;
            errors.add(output);
          } else {
            output = str + "\t" + "< " + symbols.get(str) + ", _ >";
            tokenMessage.add(output);
            
            token.add(symbols.get(str));
            token.add("_");
            token.add(String.valueOf(line_number));
          }
          break;
        case 21:
          if(symbols.get(str) == null) {
            output = str + "\t" + "Wrong operator\t" + "line: " + line_number;
            errors.add(output);
          } else {
            output = str + "\t" + "< " + symbols.get(str) + ", _ >";
            tokenMessage.add(output);
            
            token.add(symbols.get(str));
            token.add("_");
            token.add(String.valueOf(line_number));
          }
          break;
        case 23:
          output = str + "\t" + "< " + symbols.get(str) + ", _ >";
          tokenMessage.add(output);
          
          token.add(symbols.get(str));
          token.add("_");
          token.add(String.valueOf(line_number));
          break;
        case 24:
          output = str + "\t" + "< " + symbols.get(str) + ", _ >";
          tokenMessage.add(output);
          
          token.add(symbols.get(str));
          token.add("_");
          token.add(String.valueOf(line_number));
          break;
        case 25:
          output = str + "\t" + "< " + symbols.get(str) + ", _ >";
          tokenMessage.add(output);
          
          token.add(symbols.get(str));
          token.add("_");
          token.add(String.valueOf(line_number));
          break;
        default:
          System.out.println(word.toString() + "  " + start_state + "  " + end_state);
      }
      if(token.size() == 3) {
        tokens.add(token);
      }
//      tokens.add(token);
      
      if(loc >= chars.length) {
        break;
      }
    }
  }
  
  public List<String> readFile(String filePath) {
    List<String> text = new ArrayList<String>();
    File file = new File(filePath);
    BufferedReader bufferedReader = null;
    
    try {
      InputStreamReader read = new InputStreamReader(new FileInputStream(file));
      bufferedReader = new BufferedReader(read);
      String line = null;
      while((line = bufferedReader.readLine()) != null) {
        text.add(line);
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
    return text;
  }
  
  public void initialSymbols(String filePath) {
    symbols = new HashMap<String, String>();
    File file = new File(filePath);
    BufferedReader bufferedReader = null;
    
    try {
      InputStreamReader read = new InputStreamReader(new FileInputStream(file));
      bufferedReader = new BufferedReader(read);
      String line = null;
      while((line = bufferedReader.readLine()) != null) {
          String[] words = line.split(" ");
          symbols.put(words[0], words[1]);
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
  
  public List<String> getTokenMessages() {
    return tokenMessage;
  }
  
  public List<List<String>> getTokens() {
    return tokens;
  }
  
  public List<String> getErrors() {
    return errors;
  }
  
  public Map<String, String> getSymbolTable() {
    return symbol_table;
  }
  
}
