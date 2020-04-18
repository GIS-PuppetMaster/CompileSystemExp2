package grammar;

import Action.Action;
import Action.Express;
import zkx.AnalysisFormat;
import zkx.FormatElement;

import java.io.IOException;
import java.text.Normalizer;
import java.util.*;

public class Anaylser {

    public List<List<String>> tokens = new ArrayList<List<String>>();//输入token列表
    public Integer tokenIndex = 0;//记录扫描到第几个token
    public Set<String> nonterminal = new HashSet<String>();//非终结符集合
    public List<Express> productions = new ArrayList<>();//产出式集合
    public Map<Integer, Map<String,String>> lrTable = new HashMap();//分析表
    public Stack<Integer> statusStack = new Stack<Integer>();//状态栈
    public Stack<String> symbolStack = new Stack<String>();//符号栈
    public List<Express> reduceDetail = new ArrayList<>();//执行栈信息
    public Map<String, List<String>> follow = new HashMap<>();//非终结符的follow集
    Action action;

    public void init() throws IOException {
        //清空栈
        while(!statusStack.empty()) {
            statusStack.pop();
        }
        while(!symbolStack.empty()){
            symbolStack.pop();
        }

        //初始化产生式集合，非终结符集合，分析表
        action = new Action();
        action.init();
        AnalysisFormat analysisFormat = new AnalysisFormat(action);
        List<Object> list = analysisFormat.buildFormat();
        Map<Integer, Map<String, FormatElement>> format = (Map<Integer, Map<String, FormatElement>>) list.get(0);
        System.out.println("LR分析表：");
        analysisFormat.outputFormat(format);

        nonterminal = action.getVirSet();
        productions = action.getProList();
        for(Map.Entry<Integer,Map<String,FormatElement>> item:format.entrySet()){
            Map<String,String> tmp = new HashMap<>();
            Map<String, FormatElement> itemMap = item.getValue();
            for(Map.Entry<String,FormatElement> itemEntry:itemMap.entrySet()){
                tmp.put(itemEntry.getKey(),itemEntry.getValue().toString());
            }
            lrTable.put(item.getKey(),tmp);
        }
        System.out.println("Follow集：");
        getFollow();
    }


    //初始化
    public Anaylser() throws IOException {
        init();
    }

    //LR分析
    public void analyse(){
        statusStack.push(0);
        symbolStack.push("#");
        while(this.tokenIndex<tokens.size()){
            List<String> currentToken = this.tokens.get(tokenIndex);
            handleInput(currentToken.get(0));
        }
        System.out.println("规约信息:");
        for(Express s:reduceDetail){
            System.out.println(s);
        }
        System.out.println("语法分析树输出：");
        outputResult();
    }

    public boolean handleInput(String symbol){
        int status = this.statusStack.peek();
        String action = this.lrTable.get(status).get(symbol);
        //错误处理, 策略：恐慌模式
        if(action == null){
            handleError();
        }
        //移入
        else if(action.matches("s[0-9]+")){
            statusStack.add(Integer.valueOf(action.substring(1)));
            symbolStack.add(symbol);
            this.tokenIndex++;
        }
        //归约
        else if(action.matches("r[0-9]+")){
            Express prod = this.productions.get(Integer.valueOf(action.substring(1)));
            String left = prod.getLeft();
            List<String> right = Arrays.asList(prod.getRight());
            if(right.contains("ε")){

            }else{
                for(int i=0;i<right.size();i++){
                    statusStack.pop();
                    symbolStack.pop();
                }
            }
            reduceDetail.add(prod);
            symbolStack.add(left);

            //归约后的goto
            int currentStatus = statusStack.peek();
            statusStack.add(Integer.valueOf(lrTable.get(currentStatus).get(left).substring(0)));
            //handleInput(left);
        }
        else if(action.equals("acc")){
            System.out.println("识别成功");
            this.tokenIndex++;
            return true;
        }
        printStack();
        return false;
    }

    //打印栈内信息
    public void printStack(){
        System.out.println(String.format("%-90s", this.statusStack) + String.format("%-90s", this.symbolStack));
    }

    //处理错误
    public void handleError(){

    }

    //输出词法分析树
    public void outputResult(){
        System.out.println("P");
        outputRight(reduceDetail.size()-1,2);
    }

    public void outputRight(int root, int indent){
        Express rootProd = reduceDetail.get(root);
        String left = rootProd.getLeft();
        List<String> right = Arrays.asList(rootProd.getRight());
        //System.out.println(left);
        for(int i=0;i<right.size();i++){
            String r = right.get(i);
            System.out.println(new String(new char[indent]).replace("\0"," ")+r);
            //终结符直接输出
            if(!nonterminal.contains(r)){
                continue;
            }
            //非终结符递归查询
            else{
                int lastSymbolIndex = -1;
                for(int j=root-1;j>=0;j--){
                    if(r.equals(reduceDetail.get(j).getLeft())){
                        lastSymbolIndex = j;
                        break;
                    }
                }
                if(lastSymbolIndex!=-1){
                    outputRight(lastSymbolIndex,indent+2);
                }
                else{
                    System.out.println("output error");
                }
            }
        }
    }

    //计算非终结符follow集
    public void getFollow(){
        boolean flag ;
        follow.put("S'", Arrays.asList("$"));
        while(true){
            flag = true;
            for(String nonterm:nonterminal){
                for(int i=0;i<productions.size();i++){
                    List<String> right = Arrays.asList(productions.get(i).getRight());
                    String left = productions.get(i).getLeft();
                    if(right.contains(nonterm)){
                        int index = right.indexOf(nonterm);
                        if(index != right.size()-1){
                            Set<String> backFirst = action.getFirst(right.get(index+1));
                            if(backFirst!=null)
                            {
                                for (String s : backFirst) {
                                    flag = addToFollow(nonterm, s);
                                }
                            }
                            if(backFirst.contains("ε")){
                                List<String> list = follow.get(left);
                                if(list!=null)
                                {
                                    for (String s : list) {
                                        flag = addToFollow(nonterm, s);
                                    }
                                }
                            }
                        }else{
                            List<String> list = follow.get(left);
                            if(list!=null)
                            {
                                for (String s : follow.get(left)) {
                                    flag = addToFollow(nonterm, s);
                                }
                            }
                        }
                    }
                }
            }
            if(!flag){
                break;
            }
        }
    }

    public boolean addToFollow(String key,String value){
        if(follow.get(key) == null){
            List<String> list = new ArrayList<>();
            list.add(value);
            follow.put(key,list);
            return true;
        }else{
            if(!follow.get(key).contains(value)){
                follow.get(key).add(value);
                return true;
            }
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        Anaylser anaylser = new Anaylser();
        anaylser.tokens = Arrays.asList(Arrays.asList("*"),Arrays.asList("*"),Arrays.asList("*"),Arrays.asList("id"),Arrays.asList("="),Arrays.asList("*"),Arrays.asList("id"),Arrays.asList("$"));
        anaylser.analyse();
    }
}
