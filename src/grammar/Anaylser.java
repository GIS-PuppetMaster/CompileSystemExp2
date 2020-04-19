package grammar;

import Action.Action;
import Action.Express;

import lexical.LexicalAnalyzer;
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
    public List<String> errorMessage = new ArrayList<>();//错误信息
    public Map<Integer,String> errorCases = new HashMap<>();//错误提示

    public void init() throws IOException {
        //清空栈
        while(!statusStack.empty()) {
            statusStack.pop();
        }
        while(!symbolStack.empty()){
            symbolStack.pop();
        }
        //获取token
        String testFilePath = "src/grammar/test.txt";
        LexicalAnalyzer l = new LexicalAnalyzer(testFilePath);
        l.scanner();
        tokens = l.getTokens();
        tokens.add(Arrays.asList("$","_","0"));
//        tokens.add(Arrays.asList("char","_","0"));
//        tokens.add(Arrays.asList("a","_","0"));
//        tokens.add(Arrays.asList("=","_","0"));
//        tokens.add(Arrays.asList("character","_","0"));
//        tokens.add(Arrays.asList("$","_","0"));
        //初始化产生式集合，非终结符集合，分析表，follow集
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
        //初始化错误提示
        errorCases.put(26,"[ Missing '{' ]");
        errorCases.put(6,"[ Missing variable ]");
        errorCases.put(156,"[ Missing ';' ]");
        errorCases.put(35,"[ Missing ';' ]");
        errorCases.put(220,"[ Missing ';' ]");
        errorCases.put(133,"[ Missing ')' ]");
        errorCases.put(36,"[ Missing '(' ]");
        errorCases.put(20,"[ Missing '(' ]");
        errorCases.put(204,"[ Loop structure error ]");
        errorCases.put(158,"[ Boolean expression error ]");
        errorCases.put(54,"[ Missing valuable ]");
        errorCases.put(25,"[ Missing ; ]");
        errorCases.put(69,"[ Missing do ]");


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
        System.out.println("错误信息：");
        for(String s:errorMessage){
            System.out.println(s);
        }
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
            if(lrTable.get(currentStatus).get(left)!=null) {
                statusStack.add(Integer.valueOf(lrTable.get(currentStatus).get(left).substring(0)));
            }else{
                handleError();
            }
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
        //恐慌模式，退回到能接受非终结符D的状态,并丢掉不能跟在非终结符D后面的输入。
        int currStatus = statusStack.peek();
        String lineNumber = tokens.get(tokenIndex).get(2);
        errorMessage.add("行数："+tokens.get(tokenIndex).get(2)+" 错误状态："+String.valueOf(currStatus));

        List<Integer> statusList = new ArrayList<>(statusStack);
        for(int i=statusList.size()-1;i>=0;i--){
            String goAction = lrTable.get(statusList.get(i)).get("D");
            if(goAction == null){
                //若该状态无对应D的状态转移，则弹出
                if(statusStack.peek() == 0) break;
                statusStack.pop();
                symbolStack.pop();
            }else{
                //若该状态有对应D的状态转移，则分情况处理
                //非特殊处理的状态
                //压入D以及跳转的状态
                if(currStatus!=2 && currStatus!=104){
                    int targetStatus = Integer.valueOf(goAction);
                    symbolStack.push("D");
                    statusStack.push(Integer.valueOf(goAction));
                }else{
                    //状态为2和104时，上述过程会陷入死循环，所以将前面的非初始状态全部弹出并压入D
                    while(true){
                        if(statusStack.peek()!=0){
                            statusStack.pop();
                            symbolStack.pop();
                        }else{
                            symbolStack.push("D");
                            statusStack.push(Integer.valueOf(lrTable.get(0).get("D")));
                            break;
                        }
                    }
                }
            }
        }

        //抛弃所有不在follow(D)中的token
        while(!follow.get("D").contains(tokens.get(tokenIndex).get(0)) ){
            tokenIndex++;
        }
        //特殊情况：}，代表一个程序块的结束，直接将所有非初始状态都弹出，压入D
        if(tokens.get(tokenIndex).get(0).equals("}")){
            while(true){
                if(statusStack.peek()!=0){
                    statusStack.pop();
                    symbolStack.pop();
                }else{
                    symbolStack.push("D");
                    statusStack.push(Integer.valueOf(lrTable.get(0).get("D")));
                    break;
                }
            }
            tokenIndex++;
        }

        //查找错误识别信息
        if(errorCases.get(currStatus) != null) {
            errorMessage.add("Error at Line " + lineNumber + ": " + errorCases.get(currStatus));
        } else {
            // 识别不到的语法错误
            errorMessage.add("Error at Line " + lineNumber + ": " + "[ Error ]");
        }
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
        follow.put("Program", Arrays.asList("$"));
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
        //anaylser.tokens = Arrays.asList(Arrays.asList("int"),Arrays.asList("id"),Arrays.asList(";"),Arrays.asList("$"));
        anaylser.analyse();
    }
}
