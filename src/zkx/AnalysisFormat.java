package zkx;


import Action.Action;
import Action.Express;

import java.sql.ClientInfoStatus;
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

    public Set<Set<Express>> items(List<Express> G) {
        Express start = G.get(0);
        start.setHopingSymbols("$");
        boolean added = false;
        Set<Set<Express>> C = new HashSet<>() {{
            add(action.closure_method(new HashSet<>() {{
                add(start);
            }}));
        }};
        ArrayList<Set<Express>> arrayList = new ArrayList<>(C);
        int size;
        do {
            added=false;
            for(int i=0;;i++){
                size = arrayList.size();
                if(i>=size){
                    break;
                }
                Set<Express> set = arrayList.get(i);
                for (String symbol : this.getSymbolSet()) {
                    Set<Express> temp = action.goto_method(set, symbol);
                    if (!temp.isEmpty() && !arrayList.contains(temp)) {
                        Set<Set<Express>> tempSet = new HashSet<>(arrayList);
                        added = tempSet.add(temp);
                        arrayList = new ArrayList<>(tempSet);
                    }
                }
            }
        } while (added);
        return new HashSet<>(arrayList);
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
            format.put(indexCode, new HashMap<>());
            indexCode++;
        }
        for (Set<Express> expresses : C) {
            //遍历表达式，构造分析表
            for (Express express : expresses) {
                String[] right = express.getRight();
                if (express.getIndex() == right.length) {
                    String last = String.valueOf(right[express.getIndex() - 1]);
                    String S_ = action.getProList().get(0).getLeft();
                    Express expressTemp = new Express(express.getLeft(), express.getTail());
                    // 构造一个acc状态
                    Express expressTemp2 = new Express(action.getProList().get(0).getLeft(), action.getProList().get(0).getTail());
                    expressTemp2.setIndex(expressTemp2.getRight().length);
                    expressTemp2.setHopingSymbols("$");
                    if (!express.getLeft().equals(S_)) {
                        format.get(inverseIndexMap.get(expresses)).put(express.getHopingSymbols(), new FormatElement(action.getProList().indexOf(expressTemp), "r"));
                    } else if (express.equals(expressTemp2)) {
                        format.get(inverseIndexMap.get(expresses)).put("$", new FormatElement(-1, "acc"));
                    }
                } else {
                    String next = right[express.getIndex()];
                    // 下一个语法符号为终结符
                    if (!action.getVirSet().contains(next)) {
                        Set<Express> exp = action.goto_method(expresses, next);
                        // 获取这个状态的编号
                        int j = inverseIndexMap.get(exp);
                        format.get(inverseIndexMap.get(expresses)).put(next, new FormatElement(j, "s"));
                    }
                    else {
                        Set<Express> exp = action.goto_method(expresses, next);
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
        for(int i = 0;i<finalSymbol.size()*10-7;i++) {
            firstHead.insert(13," ");
        }

        System.out.println(firstHead);
        // 构造表头format
        String formatHead = "    ";
        for(int i = 0;i<finalSymbol.size();i++){
            formatHead+="|{%5s}";
        }
        formatHead+="||";
        for(int i = 0;i<notFinalSymbol.size();i++){
            formatHead+="|{%5s}";
        }
        // 输出表头
        StringBuilder line = new StringBuilder("     ||");
        for(String s:finalSymbol){
            line.append(String.format("%9s",s)).append("|");
        }
        line.append("|");
        for(String s:notFinalSymbol){
            line.append(String.format("%9s",s)).append("|");
        }
        System.out.println(line);
        // 遍历状态
        for (int state = 0; state < format.keySet().size(); state++) {
            line = new StringBuilder();
            line.append(String.format("%5s||",state));
            // 遍历Action的终结符
            for(String s:finalSymbol){
                FormatElement formatElement = format.get(state).get(s);
                if(formatElement==null || "".equals(formatElement.info)){
                    line.append(String.format("%9s|",""));
                }
                else if("acc".equals(formatElement.info)){
                    line.append(String.format("%9s|","acc"));
                }
                else if("s".equals(formatElement.info) || "r".equals(formatElement.info)){
                    line.append(String.format("%9s|",formatElement.info+formatElement.targetState));
                }
            }
            line.append("|");
            // 遍历GOTO的非终结符
            for(String s:notFinalSymbol){
                FormatElement formatElement = format.get(state).get(s);
                if(formatElement==null){
                    line.append(String.format("%9s|",""));
                }
                else if("acc".equals(formatElement.info)){
                    line.append(String.format("%9s|","acc"));
                }
                else if("s".equals(formatElement.info) || "r".equals(formatElement.info)){
                    line.append(String.format("%9s|",formatElement.info+formatElement.targetState));
                }
                else if("".equals(formatElement.info)){
                    line.append(String.format("%9s|",formatElement.targetState));
                }
            }
            System.out.println(line);
        }

    }
}
