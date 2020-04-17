package zkx;


import Action.Action;
import Action.Express;

import java.util.*;

class FormatElement {
    // 要跳转到的状态
    int targetState;
    // s/r/acc，acc时targetState为null
    String info;

    public FormatElement(int targetState, String info) {
        this.targetState = targetState;
        this.info = info;
    }
}

public class AnalysisFormat {
    Action action;

    public AnalysisFormat(Action action) {
        this.action = action;
    }

    public Set<Character> getSymbolSet() {
        Set<Character> characterSet = new HashSet<>();
        for (Express express : action.getProList()) {
            for (char c : express.getLeft().toCharArray()) {
                characterSet.add(c);
            }
            for (String s : express.getRight()) {
                for (char c : s.toCharArray()) {
                    characterSet.add(c);
                }
            }
        }
        return characterSet;
    }

    public Set<Set<Express>> items(List<Express> G) {
        Express start = G.get(0);
        start.setHopingSymbol("$");
        boolean added = false;
        Set<Set<Express>> C = new HashSet<>() {{
            add(action.closure_method(new HashSet<>() {{
                add(start);
            }}));
        }};
        do {
            for (Set<Express> set : C) {
                for (char symbol:this.getSymbolSet()){
                    Set<Express> temp = action.goto_method(set, String.valueOf(symbol));
                    if(temp!=null && !C.contains(temp)){
                        C.add(temp);
                        added=true;
                    }
                }
            }
        } while (!added);
        return C;
    }

    public List<Object> buildFormat() {
        Map<Integer, Map<String, FormatElement>> format = new HashMap<>();
        Set<Set<Express>> C = items(action.getProList());
        Map<Integer,Set<Express>> indexMap = new HashMap<>();
        Map<Set<Express>,Integer> inverseIndexMap = new HashMap<>();
        int indexCode = 0;
        for(Set<Express> expresses:C) {
            //对状态进行编码
            indexMap.put(indexCode, expresses);
            inverseIndexMap.put(expresses, indexCode);
        }
        for(Set<Express> expresses:C){
            //遍历表达式，构造分析表
            for(Express express:expresses){
                for(String tail:express.getRight()){
                    if(express.getIndex()==tail.length()){
                        String last = String.valueOf(tail.toCharArray()[express.getIndex()-1]);
                        String S_ = action.getProList().get(0).getLeft();
                        if(action.getVirSet().contains(last)&& !express.getLeft().equals(S_)){
                            format.put(inverseIndexMap.get(expresses), new HashMap<>() {{
                                put(express.getHopingSymbol(), new FormatElement(action.getProList().indexOf(express), "r"));
                            }});
                        }
                        else if(!action.getVirSet().contains(last)){
                            format.put(inverseIndexMap.get(expresses), new HashMap<>() {{
                                put("$", new FormatElement(-1, "acc"));
                            }});
                        }
                    }
                    else {
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
        return new ArrayList<>(){{add(format);add(indexMap);}};
    }

}
