package zkx;


import Action.Action;
import Action.Express;

import java.util.*;



public class AnalysisFormat {
    Action action;

    public AnalysisFormat(Action action) {
        this.action = action;
    }

    public Set<String> getFinalSymbolSet(){
        Set<String> characterSet = new HashSet<>();
        Set<String> notFinalSymbol = action.getVirSet();
        for (Express express : action.getProList()) {
            for(String symbol:express.getRight()){
                if(!notFinalSymbol.contains(symbol)){
                    characterSet.add(symbol);
                }
            }
        }
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

    public Set<Set<Express>> items(List<Express> G) {
        Express start = G.get(0);
        start.setHopingSymbols("$");
        boolean added = false;
        Set<Set<Express>> C = new HashSet<>() {{
            add(action.closure_method(new HashSet<>() {{
                add(start);
            }}));
        }};
        do {
            for (Set<Express> set : C) {
                for (String symbol : this.getSymbolSet()) {
                    Set<Express> temp = action.goto_method(set, symbol);
                    if (!temp.isEmpty() && !C.contains(temp)) {
                        C.add(temp);
                        added = true;
                    }
                }
            }
        } while (added);
        return C;
    }

    public List<Object> buildFormat() {
        Map<Integer, Map<String, FormatElement>> format = new HashMap<>();
        Set<Set<Express>> C = items(action.getProList());
        Map<Integer, Set<Express>> indexMap = new HashMap<>();
        Map<Set<Express>, Integer> inverseIndexMap = new HashMap<>();
        int indexCode = 0;
        for (Set<Express> expresses : C) {
            //对状态进行编码
            indexMap.put(indexCode, expresses);
            inverseIndexMap.put(expresses, indexCode);
        }
        for (Set<Express> expresses : C) {
            //遍历表达式，构造分析表
            for (Express express : expresses) {
                for (String tail : express.getRight()) {
                    if (express.getIndex() == tail.length()) {
                        String last = String.valueOf(tail.toCharArray()[express.getIndex() - 1]);
                        String S_ = action.getProList().get(0).getLeft();
                        if (action.getVirSet().contains(last) && !express.getLeft().equals(S_)) {
                            format.put(inverseIndexMap.get(expresses), new HashMap<>() {{
                                put(express.getHopingSymbols(), new FormatElement(action.getProList().indexOf(express), "r"));
                            }});
                        } else if (!action.getVirSet().contains(last)) {
                            format.put(inverseIndexMap.get(expresses), new HashMap<>() {{
                                put("$", new FormatElement(-1, "acc"));
                            }});
                        }
                    } else {
                        String next = String.valueOf(tail.toCharArray()[express.getIndex()]);
                        if (!action.getVirSet().contains(next)) {
                            Set<Express> exp = action.goto_method(expresses, next);
                            // 获取这个状态的编号
                            int code = inverseIndexMap.get(exp);
                            format.put(inverseIndexMap.get(expresses), new HashMap<>() {{
                                put(next, new FormatElement(code, "s"));
                            }});
                        }
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
        System.out.println("State||                                         Action                                          ||                                         GOTO");
        Set<String> allSymbol = getSymbolSet();
        // 终结符
        List<String> finalSymbol = new ArrayList<>(getFinalSymbolSet());
        // 文法符号-终结符=非终结符
        allSymbol.removeAll(finalSymbol);
        List<String> notFinalSymbol = new ArrayList<>(allSymbol);
        // 输出表头
        StringBuilder line = new StringBuilder("     || ");
        for(String s:finalSymbol){
            line.append(s).append(" | ");
        }
        line.deleteCharAt(line.length() - 1);
        line.insert(line.length(),"              ");
        for(String s:notFinalSymbol){
            line.append(s).append(" | ");
        }
        line = new StringBuilder(line.substring(0, line.length() - 1));
        System.out.println(line);
        line=new StringBuilder();
        // 遍历状态
        for (int state = 0; state < format.keySet().size(); state++) {
            line.append(state).append("   ||");
            // 遍历Action的终结符
            for(String s:finalSymbol){
                FormatElement formatElement = format.get(state).get(s);
                if(formatElement==null){
                    line.append("error|");
                }
                else if("acc".equals(formatElement.info)){
                    line.append(formatElement.info).append("|");
                }
                else if("s".equals(formatElement.info)){
                    line.append(formatElement.info).append(formatElement.targetState).append("|");
                }
            }
            line.deleteCharAt(line.length()-1);
            // 遍历GOTO的非终结符
            for(String s:notFinalSymbol){
                FormatElement formatElement = format.get(state).get(s);
                if(formatElement==null){
                    line.append("error|");
                }
                else if("acc".equals(formatElement.info)){
                    line.append(formatElement.info).append("|");
                }
                else if("s".equals(formatElement.info)){
                    line.append(formatElement.info).append(formatElement.targetState).append("|");
                }
            }
            line.deleteCharAt(line.length()-1);
            System.out.println(line);
        }

    }
}
