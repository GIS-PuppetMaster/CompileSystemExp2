package zkx;


import Action.Action;
import Action.Express;

import javax.print.attribute.HashAttributeSet;
import java.sql.ClientInfoStatus;
import java.util.*;


public class AnalysisFormat {
    Action action;

    public AnalysisFormat(Action action) {
        this.action = action;
    }

    public Set<String> getFinalSymbolSet() {
        Set<String> characterSet = new HashSet<>();
        Set<String> notFinalSymbol = action.getVirSet();
        for (Express express : action.getProList()) {
            for (String symbol : express.getRight()) {
                if (!notFinalSymbol.contains(symbol)) {
                    characterSet.add(symbol);
                }
            }
        }
        characterSet.add("$");
        return characterSet;
    }

    public Set<String> getSymbolSet() {
        Set<String> notFinalSymbol = action.getVirSet();
        Set<String> finalSymbol = getFinalSymbolSet();
        Set<String> characterSet = new HashSet<>();
        characterSet.addAll(notFinalSymbol);
        characterSet.addAll(finalSymbol);
        return characterSet;
    }

    public List<Object> items(List<Express> G) {
        Express tempStart = G.get(0);
        Express start = new Express(tempStart.getLeft(), tempStart.getTail());
        start.setHopingSymbols("$");
        boolean added;
        Set<Express> firstState = action.closure_method(new HashSet<>() {{
            add(start);
        }});
        Set<Set<Express>> C = new HashSet<>() {{
            add(firstState);
        }};
        ArrayList<Set<Express>> arrayList = new ArrayList<>(C);
        int size;
        do {
            added = false;
            for (int i = 0; ; i++) {
                size = arrayList.size();
                if (i >= size) {
                    break;
                }
                Set<Express> set = arrayList.get(i);
                Set<String> symbolSet = this.getSymbolSet();
                symbolSet.remove("$");
                for (String symbol : symbolSet) {
                    Set<Express> temp = action.goto_method(set, symbol);
                    if (!temp.isEmpty() && !arrayList.contains(temp)) {
                        added = arrayList.add(temp);
                    }
                }
            }
        } while (added);
        Set<Set<Express>> res = new HashSet<>(arrayList);
        return new ArrayList<>() {{
            add(firstState);
            add(res);
        }};
    }

    public List<Object> buildFormat() {
        Map<Integer, Map<String, FormatElement>> format = new HashMap<>();
        List<Object> list = items(action.getProList());
        Set<Express> firstState = (Set<Express>) list.get(0);
        Set<Set<Express>> C = (Set<Set<Express>>) list.get(1);
        Map<Integer, Set<Express>> indexMap = new HashMap<>();
        Map<Set<Express>, Integer> inverseIndexMap = new HashMap<>();
        indexMap.put(0, firstState);
        inverseIndexMap.put(firstState, 0);
        format.put(0, new HashMap<>());
        int indexCode = 1;
        for (Set<Express> expresses : C) {
            //对状态进行编码
            if(expresses==firstState){
                continue;
            }
            indexMap.put(indexCode, expresses);
            inverseIndexMap.put(expresses, indexCode);
            format.put(indexCode, new HashMap<>());
            indexCode++;
        }
        for (Set<Express> expresses : C) {
            //遍历表达式，构造分析表
            for (Express express : expresses) {
                String[] right = express.getRight();
                // 条件为真则继续判断伪代码中第3、4个if
                if (express.getIndex() == right.length) {
                    // 第一个非终结符
                    String S_ = action.getProList().get(0).getLeft();
                    // 用于获取表达式编号的实例，构造的不带展望符， index=0的实例
                    Express expressTemp = new Express(express.getLeft(), express.getTail());
                    // 用于判读是否和第一个表达式相等，构造的不带展望符，index=0的实例
                    Express expressTemp2 = new Express(action.getProList().get(0).getLeft(), action.getProList().get(0).getTail());
                    expressTemp2.setIndex(expressTemp2.getRight().length);
                    expressTemp2.setHopingSymbols("$");
                    // 伪代码中第三个if
                    if (!express.getLeft().equals(S_)) {
                        format.get(inverseIndexMap.get(expresses)).put(express.getHopingSymbols(), new FormatElement(action.getProList().indexOf(expressTemp), "r"));
                    }
                    // 伪代码中第四个if
                    else if (express.equals(expressTemp2)) {
                        format.get(inverseIndexMap.get(expresses)).put("$", new FormatElement(-1, "acc"));
                    }
                }
                // 伪代码中前两个if
                else {
                    // index后面的语法符号
                    String next = right[express.getIndex()];
                    // GOTO(Ii,a)=Ij
                    Set<Express> exp = action.goto_method(expresses, next);
                    // 伪代码中第一个if，下一个语法符号为终结符
                    if (!action.getVirSet().contains(next)) {
                        // 获取这个状态的编号
                        int j = inverseIndexMap.get(exp);
                        format.get(inverseIndexMap.get(expresses)).put(next, new FormatElement(j, "s"));
                    }
                    // 伪代码中第二个if
                    else {
                        // 获取这个状态的编号
                        int j = inverseIndexMap.get(exp);
                        format.get(inverseIndexMap.get(expresses)).put(next, new FormatElement(j, ""));
                    }
                }
            }
            indexCode++;
        }
        return new ArrayList<>() {{
            add(format);
            add(indexMap);
        }};
    }

    public void outputFormat(Map<Integer, Map<String, FormatElement>> format) {
        StringBuilder firstHead = new StringBuilder("State||Action||GOTO");

        Set<String> allSymbol = getSymbolSet();
        // 终结符
        List<String> finalSymbol = new ArrayList<>(getFinalSymbolSet());
        // 文法符号-终结符=非终结符
        allSymbol.removeAll(finalSymbol);
        List<String> notFinalSymbol = new ArrayList<>(allSymbol);
        for (int i = 0; i < finalSymbol.size() * 10 - 7; i++) {
            firstHead.insert(13, " ");
        }

        System.out.println(firstHead);
        // 构造表头format
        String formatHead = "    ";
        for (int i = 0; i < finalSymbol.size(); i++) {
            formatHead += "|{%5s}";
        }
        formatHead += "||";
        for (int i = 0; i < notFinalSymbol.size(); i++) {
            formatHead += "|{%5s}";
        }
        // 输出表头
        StringBuilder line = new StringBuilder("     ||");
        for (String s : finalSymbol) {
            line.append(String.format("%9s", s)).append("|");
        }
        line.append("|");
        for (String s : notFinalSymbol) {
            line.append(String.format("%9s", s)).append("|");
        }
        System.out.println(line);
        // 遍历状态
        for (int state = 0; state < format.keySet().size(); state++) {
            line = new StringBuilder();
            line.append(String.format("%5s||", state));
            // 遍历Action的终结符
            for (String s : finalSymbol) {
                FormatElement formatElement = format.get(state).get(s);
                if (formatElement == null) {
                    line.append(String.format("%9s|", ""));
                } else {
                    line.append(String.format("%9s|", formatElement));
                }
            }
            line.append("|");
            // 遍历GOTO的非终结符
            for (String s : notFinalSymbol) {
                FormatElement formatElement = format.get(state).get(s);
                if (formatElement == null) {
                    line.append(String.format("%9s|", ""));
                } else {
                    line.append(String.format("%9s|", formatElement));
                }
            }
            System.out.println(line);
        }

    }
}
