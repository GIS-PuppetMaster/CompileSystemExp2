package grammar;

// import lexical.LexicalAnalyzer;

import java.lang.reflect.Array;
import java.util.*;

public class GramAnalyer {

    public List<List<String>> tokens = new ArrayList<List<String>>();//输入token列表
    public Integer tokenIndex = 0;//记录扫描到第几个token
    public Set<String> nonterminal = new HashSet<String>();//非终结符集合
    public List<Production> productions = new ArrayList<Production>();//产出式集合
    public Map<Integer,Map<String,String>> lrTable = new HashMap();//分析表
    public Stack<Integer> statusStack = new Stack<Integer>();//状态栈
    public Stack<String> symbolStack = new Stack<String>();//符号栈
    public List<String> reduceDetail = new ArrayList<String>();//执行栈信息

    //清空栈
    public void init(){
        while(!statusStack.empty()) {
            statusStack.pop();
        }
        while(!symbolStack.empty()){
            symbolStack.pop();
        }
    }

    //初始化LR分析表，产生式表
    public GramAnalyer(String testFilePath){
        
    }

    //LR分析
    public void analyse(){
        statusStack.push(0);
        symbolStack.push("#");
        tokens.add(Arrays.asList("#", "_", "0"));
        while(this.tokenIndex<tokens.size()){
            List<String> currentToken = this.tokens.get(tokenIndex);
            handleInput(currentToken.get(0));
        }
        System.out.println("规约信息:");
        for(String s:reduceDetail){
            System.out.println(s);
        }
    }

    public void handleInput(String symbol){
        int status = this.statusStack.peek();
        String action = this.lrTable.get(status).get(symbol);
        if(action == null){
            handleError();
        }
        //移入
        else if(action.matches("S[0-9]+")){
            statusStack.add(Integer.valueOf(action.substring(1)));
            symbolStack.add(symbol);
            this.tokenIndex++;
        }
        //归约
        else if(action.matches("r[0-9]+")){
            Production prod = this.productions.get(Integer.valueOf(action.substring(1)));
            String left = prod.getLeft();
            List<String> right = prod.getRight();
            StringBuilder sb = new StringBuilder();
            if(right.contains("empty")){
                sb.append("empty");
            }else{
                for(int i=0;i<right.size();i++){
                    statusStack.pop();
                    sb.append(symbolStack.pop()+" ");
                }
            }
            reduceDetail.add(sb+"->"+left);
            symbolStack.add(left);

            //归约后的goto
            int currentStatus = statusStack.peek();
            statusStack.add(Integer.valueOf(lrTable.get(currentStatus).get(left).substring(0)));
        }
        else if(action.equals("acc")){
            System.out.println("识别成功");
            this.tokenIndex++;
        }
        printStack();
        return ;
    }

    public void printStack(){
        /*Stack<Integer> newStack = new Stack<Integer>();
        newStack.addAll(this.statusStack);
        System.out.println("状态栈信息：");
        while(!newStack.empty()){
            System.out.print(newStack.pop()+" ");
        }
        System.out.println();
        System.out.println("符号栈信息：");
        Stack<String> stack2 = new Stack<String>();
        stack2.addAll(this.symbolStack);
        while(!stack2.empty()){
            System.out.println(stack2.pop()+" ");
        }
        System.out.println();*/
        System.out.println(String.format("%-90s", this.statusStack) + String.format("%-90s", this.symbolStack));
    }

    public void handleError(){

    }

    public static void test(){
        GramAnalyer gramAnalyer = new GramAnalyer("");
        gramAnalyer.productions.add(new Production("S", Arrays.asList("x","x","T")));
        gramAnalyer.productions.add(new Production("T", Arrays.asList("y")));
        gramAnalyer.lrTable.put(1,new HashMap<String, String>(){
            {
                put("x", "s2");
                put("S","g6");
            }
        });
        gramAnalyer.lrTable.put(2,new HashMap<String, String>(){
            {
                put("x", "s3");

            }
        });
        gramAnalyer.lrTable.put(3,new HashMap<String, String>(){
            {
                put("y", "s4");
                put("T","g5");
            }
        });
        gramAnalyer.lrTable.put(4,new HashMap<String, String>(){
            {
                put("x", "r2");
                put("y","r2");
                put("$","r2");
            }
        });
        gramAnalyer.lrTable.put(5,new HashMap<String, String>(){
            {
                put("x", "r1");
                put("y","r1");
                put("$","r1");
            }
        });
        gramAnalyer.lrTable.put(6,new HashMap<String, String>(){
            {
                put("$", "acc");
            }
        });
        gramAnalyer.tokens = Arrays.asList(Arrays.asList("x"),Arrays.asList("x"),Arrays.asList("y"),Arrays.asList("$"));
        gramAnalyer.analyse();
    }

    public static void main(String[] args) {
        test();
    }

}
