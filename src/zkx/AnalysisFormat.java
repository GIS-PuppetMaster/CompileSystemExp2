package zkx;


import Action.Action;
import Action.Express;

import java.util.*;
class FormatElement{
    // 要跳转到的状态
    Set<Express> targetState;
    // s/r/acc，acc时targetState为null
    String info;

    public FormatElement(Set<Express> targetState, String info) {
        this.targetState = targetState;
        this.info = info;
    }
}
public class AnalysisFormat {
    Action action;

    public AnalysisFormat(Action action) {
        this.action = action;
    }
    public Set<Character> getSymbolSet(){
        for(Express express:action.)
    }
    public Set<Set<Express>> items(List<Express> G) {
        Express start = G.get(0);
        start.addHopingSymbol("$");
        boolean added = false;
        Set<Set<Express>> C = new HashSet<>(){{add(action.closure_method(new HashSet<>(){{add(start);}}));}};
        do {
            for(Set<Express> set:C){
                for(action.)
            }
        } while (!added);

    }
    //  Map<Integer, Map<Character, FormatElement>>
    public List<Object> buildFormat(){
        Set<Express> C = items()
    }

}
