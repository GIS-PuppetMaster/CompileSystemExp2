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
    public Map<Integer, Map<String, String>> lrTable = new HashMap();//分析表
    public Stack<Integer> statusStack = new Stack<Integer>();//状态栈
    public Stack<String> symbolStack = new Stack<String>();//符号栈
    public Stack<Map<String, List<String>>> valueStack = new Stack<>();//属性栈
    public List<Express> reduceDetail = new ArrayList<>();//执行栈信息
    public Map<String, List<String>> follow = new HashMap<>();//非终结符的follow集
    public Action action;
    public List<String> errorMessage = new ArrayList<>();//错误信息
    public Map<Integer, String> errorCases = new HashMap<>();//错误提示
    public List<String> outputGramTree = new ArrayList<>();//词法分析树
    public Map<String, List<String>> symbolTable = new HashMap<>();//符号表
    public List<String> errorLog = new ArrayList<>();//错误日志
    public List<String> add3Code = new ArrayList<>();//三地址码
    public List<String> tuple4Code = new ArrayList<>();//四元组
    public List<String> paramList = new ArrayList<>();//参数列表
    int offset = 0;
    String t = null;
    String w = null;
    int tmpIndex = 0;//临时变量索引

    public void init() throws IOException {
        //清空栈
        while (!statusStack.empty()) {
            statusStack.pop();
        }
        while (!symbolStack.empty()) {
            symbolStack.pop();
        }

        //获取token
        String testFilePath = "src/grammar/test.txt";
        LexicalAnalyzer l = new LexicalAnalyzer(testFilePath);
        l.scanner();
        tokens = l.getTokens();
        tokens.add(Arrays.asList("$", "_", "0"));

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
        for (Map.Entry<Integer, Map<String, FormatElement>> item : format.entrySet()) {
            Map<String, String> tmp = new HashMap<>();
            Map<String, FormatElement> itemMap = item.getValue();
            for (Map.Entry<String, FormatElement> itemEntry : itemMap.entrySet()) {
                tmp.put(itemEntry.getKey(), itemEntry.getValue().toString());
            }
            lrTable.put(item.getKey(), tmp);
        }
        getFollow();
        printFollowSet();
        printFirstSet();
        //初始化错误提示
        /*errorCases.put(26,"[ Missing '{' ]");
        errorCases.put(6,"[ Missing variable ]");
        errorCases.put(156,"[ Missing ';' ]");
        errorCases.put(35,"[ Missing ';' ]");
        errorCases.put(220,"[ Missing ';' ]");
        errorCases.put(133,"[ Missing ')' ]");
        errorCases.put(36,"[ Missing '(' ]");
        errorCases.put(271,"[ Missing ')' ]");
        errorCases.put(204,"[ Loop structure error ]");
        errorCases.put(158,"[ Boolean expression error ]");
        errorCases.put(54,"[ Missing valuable ]");
        errorCases.put(25,"[ Missing ; ]");
        errorCases.put(69,"[ Missing do ]");*/

        errorCases.put(69, "[ Missing do ]");
        errorCases.put(272, "[ Missing variable ]");
        errorCases.put(293, "[ Missing ';' ]");
        errorCases.put(197, "[ Missing ';' ]");
        errorCases.put(32, "[ Missing then ]");
        errorCases.put(221, "[ Missing variable ]");
        errorCases.put(81, "[ Missing variable ]");
        errorCases.put(156, "[ Missing ( ]");
        errorCases.put(29, "[ Missing ) ]");
        errorCases.put(271, "[ Missing ) ]");
        errorCases.put(269, "[ Missing {\\} ]");
        errorCases.put(18, "[ Missing {\\} ]");
        errorCases.put(93, "[ Missing variable ]");


    }

    public void printFollowSet() {
        System.out.println("Follow集：");
        for (Map.Entry<String, List<String>> entry : follow.entrySet()) {
            StringBuilder line = new StringBuilder(entry.getKey() + ":   ");
            for (String s : entry.getValue()) {
                line.append(s).append(" ");
            }
            line.append("\n");
            System.out.println(line);
        }
    }

    public void printFirstSet() {
        System.out.println("First集：");
        for (String string : action.getVirSet()) {
            System.out.print(string + ":  ");
            Set<String> re = action.getFirst(string);
            for (String s : re) {
                System.out.print(s + " ");
            }
            System.out.println();
        }
    }

    //初始化
    public Anaylser() throws IOException {
        init();
    }

    //LR分析
    public void analyse() {
        //获取token
        String testFilePath = "./src/grammar/test.txt";
        LexicalAnalyzer l = new LexicalAnalyzer(testFilePath);
        l.scanner();
        tokens = l.getTokens();
        tokens.add(Arrays.asList("$", "_", "0"));

        statusStack.push(0);
        symbolStack.push("#");
        valueStack.push(null);
        while (this.tokenIndex < tokens.size()) {
            List<String> currentToken = this.tokens.get(tokenIndex);
            handleInput(currentToken);
        }
        System.out.println("规约信息:");
        for (Express s : reduceDetail) {
            System.out.println(s);
        }
        System.out.println("语法分析树输出：");
        outputResult();
        System.out.println("错误信息：");
        for (String s : errorMessage) {
            System.out.println(s);
        }
    }

    public void addToSymbolTable(String key,List<String> list) {
        if (symbolTable.containsKey(key)) {
            errorLog.add(String.format("Error at Line %s: 重复声明, id: %s, type: %s, offset: %s\n", list.get(2), key, list.get(0), list.get(1)));
        }
        else{
            symbolTable.put(key, list);
        }
    }

    public boolean handleInput(List<String> token) {
        String symbol = token.get(0);
        String lexeme = token.get(1);
        String line = token.get(2);
        int status = this.statusStack.peek();
        String action = this.lrTable.get(status).get(symbol);

        //错误处理, 策略：恐慌模式
        if (action == null) {
            handleError();
        }
        //移入
        else if (action.matches("s[0-9]+")) {
            if(symbolStack.peek().equals("S")){
                Map<String,List<String>> tmpMap = valueStack.peek();
                String nextQuad = String.valueOf(add3Code.size());
                if(tmpMap.containsKey("nextlist")){
                    List<String> nextlist = tmpMap.get("nextlist");
                    for(String s:nextlist){
                        int index = Integer.valueOf(s);
                        String patched = add3Code.get(index) + " "+ nextQuad;
                        add3Code.set(index,patched);
                    }
                }
            }
            statusStack.add(Integer.valueOf(action.substring(1)));
            symbolStack.add(symbol);
            String finalLexeme = lexeme;
            valueStack.add(new HashMap<>() {{
                put("lexeme", Arrays.asList(finalLexeme));
            }});
            this.tokenIndex++;
        }
        //归约
        else if (action.matches("r[0-9]+")) {
            Express prod = this.productions.get(Integer.parseInt(action.substring(1)));
            String left = prod.getLeft();
            List<String> right = Arrays.asList(prod.getRight());

            Map<String,List<String>> tmpMap;

            if (right.contains("ε")) {
                String tail = prod.getTail();
                if ("DM1".equals(left)) {
                    // 将DM1.type赋值
                    Map<String, List<String>> DM1_v = new HashMap<>();
                    DM1_v.put("type", Arrays.asList("record"));
                    // 获取id.lexeme，先出栈再入栈
                    Map<String, List<String>> id_v = valueStack.peek();
                    String id_lexeme = id_v.get("lexeme").get(0);
                    // DM1在属性栈顶
                    valueStack.add(DM1_v);
                    // 添加符号表
                    addToSymbolTable(id_lexeme, Arrays.asList("record", String.valueOf(offset), line));
                } else if ("DM2".equals(left)) {
                    // 将DM1.type赋值
                    Map<String, List<String>> DM2_v = new HashMap<>();
                    DM2_v.put("type", Arrays.asList("proc"));
                    // 获取id.lexeme，先出栈再入栈
                    Map<String, List<String>> id_v = valueStack.peek();
                    String id_lexeme = id_v.get("lexeme").get(0);
                    // 入栈，注意DM2在栈顶
                    valueStack.add(DM2_v);
                    // 添加符号表
                    addToSymbolTable(id_lexeme, Arrays.asList("proc", String.valueOf(offset), line));
                } else if ("TM".equals(left)) {
                    // 栈顶必定为X
                    Map<String, List<String>> x_v = valueStack.peek();
                    t = x_v.get("type").get(0);
                    w = x_v.get("width").get(0);
                    // 放回x
                    valueStack.add(new HashMap<>());
                } else if ("C".equals(left)) {
                    valueStack.add(new HashMap<>() {{
                        put("type", Arrays.asList(t));
                        put("width", Arrays.asList(w));
                    }});
                } else if("BM".equals(left)){
                    valueStack.add(new HashMap<>(){{
                        put("quad",Arrays.asList(String.valueOf(add3Code.size())));
                    }});
                } else if("N".equals(left)){
                    List<String> l = new ArrayList<>();
                    add3Code.add("goto");
                    l.add(String.valueOf(add3Code.size()-1));
                    valueStack.add(new HashMap<>(){{
                        put("nextlist",l);
                    }});
                } else if("P".equals(left)){
                    valueStack.add(new HashMap<>());
                } else if ("C".equals(left)) {
                    valueStack.add(new HashMap<>() {{
                        put("type", Arrays.asList(t));
                        valueStack.add(new HashMap<>());
                        put("width", Arrays.asList(w));
                    }});

                }
            } else {
                List<String> symbolList = new ArrayList<>();
                List<Map<String, List<String>>> valueList = new ArrayList<>();
                for (int i = 0; i < right.size(); i++) {
                    valueList.add(valueStack.pop());
                    statusStack.pop();
                    symbolList.add(symbolStack.pop());
                }
                String tail = prod.getTail();
                if ("D".equals(left)) {
                    if("T id ;".equals(tail)) {
                        addToSymbolTable(valueList.get(1).get("lexeme").get(0), Arrays.asList(valueList.get(2).get("type").get(0), String.valueOf(offset), line));
                        offset += Integer.parseInt(valueList.get(2).get("width").get(0));
                        valueStack.add(new HashMap<>());
                    }
                    else if("struct id DM1 { P }".equals(tail) || "proc X id DM2 ( M ) { P }".equals(tail)){
                        valueStack.add(new HashMap<>());
                    }
                } else if ("T".equals(left) && "X TM C".equals(tail)) {
                    // 为后面压栈的T先压入属性
                    valueStack.add(new HashMap<>() {{
                        put("type", valueList.get(0).get("type"));
                        put("width", valueList.get(0).get("width"));
                    }});
                } else if ("X".equals(left)) {
                    if ("int".equals(tail)) {
                        valueStack.add(new HashMap<>() {{
                            put("type", Arrays.asList("int"));
                            put("width", Arrays.asList("4"));
                        }});
                    } else if ("float".equals(tail)) {
                        valueStack.add(new HashMap<>() {{
                            put("type", Arrays.asList("float"));
                            put("width", Arrays.asList("8"));
                        }});
                    } else if ("char".equals(tail)) {
                        valueStack.add(new HashMap<>() {{
                            put("type", Arrays.asList("char"));
                            put("width", Arrays.asList("1"));
                        }});
                    }
                } else if ("C".equals(left) && "[ num ] C".equals(tail)) {
                    int val = Integer.parseInt(valueList.get(2).get("lexeme").get(0));
                    int c1_width = Integer.parseInt(valueList.get(0).get("width").get(0));
                    valueStack.add(new HashMap<>() {{
                        put("type", Arrays.asList(String.format("array(%d, %s);", val, valueList.get(0).get("type").get(0))));
                        put("width", Arrays.asList(String.valueOf(val * c1_width)));
                    }});
                } else if ("M".equals(left)) {
                    addToSymbolTable(valueList.get(0).get("lexeme").get(0), Arrays.asList(valueList.get(1).get("type").get(0), String.valueOf(offset), line));
                    offset += Integer.parseInt(valueList.get(1).get("width").get(0));
                    if ("M , X id".equals(tail)) {
                        valueStack.add(new HashMap<>() {{
                            put("size", Arrays.asList(String.valueOf(Integer.parseInt(valueList.get(3).get("size").get(0)) + 1)));
                        }});
                    } else if ("X id".equals(tail)) {
                        valueStack.add(new HashMap<>() {{
                            put("size", Arrays.asList("1"));
                        }});
                    }
                } else if("P".equals(left) && ("D P".equals(tail) || "S P".equals(tail))){
                    valueStack.add(new HashMap<>());
                } else if("Program".equals(left) && "P".equals(tail)){
                    valueStack.add(new HashMap<>());
                }

//yu---------------------------------------------------------------------------------------------------
                //S -> L = E ;  // 数组元素引用或赋值
                //{gen(L.array ‘[’ L.offset ‘]’ ‘=’ E.addr);}
                else if("S".equals(left) && "L = E ;".equals(tail)){
                    tmpMap = valueList.get(3);
                    String array = tmpMap.get("array").get(0);
                    String offset = tmpMap.get("offset").get(0);
                    String addr = valueList.get(1).get("addr").get(0);
                    add3Code.add(String.format("%s[%s] = %s",array,offset,addr));
                    valueStack.add(new HashMap<>());
                }
                //S->id = E ;
                //{p = lookup(id.lexeme);
                //if p == null then error
                //else gen(p ‘=’ E.addr);}
                else if("S".equals(left) && "id = E ;".equals(tail)){
                    List<String> p = symbolTable.get(valueList.get(3).get("lexeme").get(0));
                    if(p == null){
                        //TODO
                    }else{
                        String addr = valueList.get(1).get("addr").get(0);
                        add3Code.add(String.format("%s = %s",valueList.get(3).get("lexeme").get(0),addr));
                    }
                    valueStack.add(new HashMap<>());
                }
                //S->if ( B ) BM then S N else BM S  // 分支语句
                //{backpatch(B.truelist, BM1.quad);
                //backbatch(B.falselist, BM2.quad);
                //temp = merge(S1.nextlist, N.nextlist);
                //S.nextlist = merge(temp, S2.nextlist);}
                else if("S".equals(left) && "if ( B ) BM then S N else BM S".equals(tail)){
                    String bm1Quad = valueList.get(6).get("quad").get(0);
                    String bm2Quad = valueList.get(1).get("quad").get(0);
                    List<String> bTrue = valueList.get(8).get("truelist");
                    List<String> bFalse = valueList.get(8).get("falselist");
                    List<String> s1Next = valueList.get(4).get("nextlist");
                    List<String> s2Next = valueList.get(0).get("nextlist");
                    List<String> nNext = valueList.get(3).get("nextlist");
                    for(String s:bTrue){
                        int index = Integer.valueOf(s);
                        String patched = add3Code.get(index) + " "+ bm1Quad;
                        add3Code.set(index,patched);
                    }
                    for(String s:bFalse){
                        int index = Integer.valueOf(s);
                        String patched = add3Code.get(index) + " "+ bm2Quad;
                        add3Code.set(index,patched);
                    }
                    if(s1Next!=null) nNext.addAll(s1Next);
                    if(s2Next!=null) nNext.addAll(s2Next);
                    valueStack.add(new HashMap<>(){{
                        put("nextlist",nNext);
                    }});
                }
                //S->while BM ( B ) do BM S  // 循环语句
                //{backpatch(S1.nextlist, BM1.quad);
                //Backpatch(B.truelist, BM2.quad);
                //S.nextlist = B.falselist;
                //gen(‘goto’ BM1.quad);}
                else if("S".equals(left) && "while BM ( B ) do BM S".equals(tail)){
                    List<String> s1Next = valueList.get(0).get("nextlist");
                    List<String> bTrue = valueList.get(4).get("truelist");
                    List<String> bFalse = valueList.get(4).get("falselist");
                    String bm1Quad = valueList.get(6).get("quad").get(0);
                    String bm2Quad = valueList.get(1).get("quad").get(0);
                    if(s1Next!=null)
                    {
                        for (String s : s1Next) {
                            int index = Integer.valueOf(s);
                            String patched = add3Code.get(index) + " " + bm1Quad;
                            add3Code.set(index, patched);
                        }
                    }
                    for(String s:bTrue){
                        int index = Integer.valueOf(s);
                        String patched = add3Code.get(index) + " "+ bm2Quad;
                        add3Code.set(index,patched);
                    }
                    valueStack.add(new HashMap<>(){{
                        put("nextlist",bFalse);
                    }});
                    add3Code.add(String.format("goto %s",bm1Quad));
                }
                //S->call id ( Elist ) ;  // 过程调用语句
                //{n = 0;
                //for q 中的每个 t
                //do {gen(‘param’ t);
                //n = n + 1;}
                //gen(‘call’ id.addr ‘,’ n);}
                else if("S".equals(left) && "call id ( Elist ) ;".equals(tail)){
                    int n=0;
                    for(String s:paramList){
                        n++;
                        add3Code.add(String.format("param %s",s));
                    }
                    add3Code.add(String.format("call %s , %d",valueList.get(4).get("addr").get(0),n));
                    valueStack.add(new HashMap<>());
                }
                //S->return E ;
                //{gen(‘return’ E.addr);}
                else if("S".equals(left) && "return E ;".equals(tail)){
                    add3Code.add(String.format("return %s",valueList.get(1).get("addr").get(0)));
                    valueStack.add(new HashMap<>());
                }
                //L -> L [ E ]
                // {L.array = L1.array;
                //L.type = L1.type.elem;
                //t = newtemp();
                //gen(t ‘=’ E.addr ‘*’ L.type.width);
                //L.offset = newtemp();
                //gen(L.offset ‘=’ L1.offset ‘+’ t);}
                else if("L".equals(left) && "L [ E ]".equals(tail)){
                    tmpMap = valueList.get(1);
                    String addr = tmpMap.get("addr").get(0);//E-addr
                    tmpMap = valueList.get(3);
                    String array = tmpMap.get("array").get(0);
                    String type = tmpMap.get("type").get(0);
                    //TODO
                    String width = type.substring(type.indexOf("(")+1,type.indexOf(","));
                    String offset = tmpMap.get("offset").get(0);//L1-offset
                    add3Code.add(String.format("t%d = %s * %s",tmpIndex,addr,width));
                    tmpIndex++;
                    add3Code.add(String.format("t%d = %s + t%d",tmpIndex,offset,tmpIndex-1));
                    tmpIndex++;
                    String finalType = type.substring(type.indexOf(",")+2,type.length()-1);
                    valueStack.add(new HashMap<>(){{
                        put("array",Arrays.asList(array));
                        put("type",Arrays.asList(finalType));
                        put("offset",Arrays.asList(offset));
                    }});
                }
                //L -> id [ E ]
                // {p = lookup(id.lexeme);
                //if p == null then error
                //else L.array = p;
                //L.type = p.type.elem;
                //L.offset = newtemp();
                //gen(L.offset ‘=’ E.addr ‘*’ L.type.width);}
                else if("L".equals(left) && "id [ E ]".equals(tail)){
                    tmpMap = valueList.get(3);//id
                    List<String> p = symbolTable.get(tmpMap.get("lexeme").get(0));
                    if(p == null){
                        //TODO
                        //变量未声明
                    }else{
                        String type = p.get(0);
                        String elem = type.substring(type.indexOf(",")+1,type.length()-1);
                        String width = elem.substring(elem.indexOf("(")+1,elem.indexOf(","));
                        tmpMap = valueList.get(1);//E
                        String addr = tmpMap.get("addr").get(0);
                        add3Code.add(String.format("t%d = %s * %s",tmpIndex,addr,width));
                        valueStack.add(new HashMap<>(){{
                            put("array",Arrays.asList(p.get(0)));
                            put("type",Arrays.asList(elem));
                            put("offset",Arrays.asList("t"+tmpIndex));
                        }});
                        tmpIndex++;
                    }
                }
                //E -> E + G
                //{E.addr = newtemp();
                //gen(E.addr ‘=’ E1.addr ‘+’ G.addr);}
                else if("E".equals(left) && "E + G".equals(tail)){
                    valueStack.add(new HashMap<>(){{
                        put("addr",Arrays.asList("t"+String.valueOf(tmpIndex)));
                    }});
                    tmpIndex++;
                    add3Code.add(String.format("t%d = %s + %s",tmpIndex-1,valueList.get(2).get("addr").get(0),valueList.get(0).get("addr").get(0)));
                }
                //E -> G
                //{E.addr = G.addr;}
                else if("E".equals(left) && "G".equals(tail)){
                    tmpMap = valueList.get(0);//G
                    String addr = tmpMap.get("addr").get(0);
                    valueStack.add(new HashMap<>(){{
                        put("addr", Arrays.asList(addr));
                    }});
                }
                //G -> G * F
                //{G.addr = newtemp;
                //gen(G.addr ‘=’ G1.addr ‘*’ F.addr);}
                else if("G".equals(left) && "G * F".equals(tail)){
                    valueStack.add(new HashMap<>(){{
                        put("addr",Arrays.asList("t"+tmpIndex));
                    }});
                    add3Code.add(String.format("t%d = %s * %s",tmpIndex,valueList.get(2).get("addr").get(0),valueList.get(0).get("addr").get(0)));
                    tmpIndex++;
                }
                //G -> F
                // {G.addr = F.addr;}
                else if("G".equals(left) && "F".equals(tail)){
                    valueStack.add(new HashMap<>(){{
                        put("addr",List.copyOf(valueList.get(0).get("addr")));
                    }});
                }
                //F -> ( E )
                //{F.addr = E.addr;}
                else if("F".equals(left) && "( E )".equals(tail)){
                    valueStack.add(new HashMap<>(){{
                        put("addr",List.copyOf(valueList.get(1).get("addr")));
                    }});
                }
                //F -> num
                //{F.addr = num.val;}
                else if("F".equals(left) && "num".equals(tail)){
                    valueStack.add(new HashMap<>(){{
                        put("addr",Arrays.asList(valueList.get(0).get("lexeme").get(0)));
                    }});
                }
                //F -> id
                //{F.addr = lookup(id.lexeme);
                //if F.addr == null then error;}
                else if("F".equals(left) && "id".equals(tail)){
                    List<String> p = valueList.get(0).get("lexeme");
                    if(p == null){
                        //未定义
                    }else{
                        valueStack.add(new HashMap<>(){{
                            put("addr",Arrays.asList(p.get(0)));
                        }});
                    }
                }
                //F -> real
                //{F.addr = real.val;}
                else if("F".equals(left) && "real".equals(tail)){
                    valueStack.add(new HashMap<>(){{
                        put("addr",Arrays.asList(valueList.get(0).get("lexeme").get(0)));
                    }});
                }
                //F->character
                //{F.addr = character.val;}
                else if("F".equals(left) && "character".equals(tail)){
                    valueStack.add(new HashMap<>(){{
                        put("addr",Arrays.asList(valueList.get(0).get("lexeme").get(0)));
                    }});
                }
                //F->L
                //{F.addr ‘=’ L.array ‘[’ L.offset ‘]’;}
                else if("F".equals(left) && "L".equals(tail)){
                    tmpMap = valueStack.get(0);
                    Map<String, List<String>> finalTmpMap2 = tmpMap;
                    valueStack.add(new HashMap<>(){{
                        put("addr",Arrays.asList(String.format("%s[%s]", finalTmpMap2.get("array").get(0), finalTmpMap2.get("offset").get(0))));
                    }});
                }
                //B -> B || BM H
                //{backpatch(B1.falselist, BM.quad);
                //B.truelist = merge(B1.truelist, H.truelist);
                //B.falselist = H.falselist;}
                else if("B".equals(left) && "B || BM H".equals(tail)){
                    List<String> b1False = valueList.get(3).get("falselist");
                    String quad = valueList.get(1).get("quad").get(0);
                    for(String s:b1False){
                        int index = Integer.valueOf(s);
                        String patched = add3Code.get(index) + " "+ quad;
                        add3Code.set(index,patched);
                    }
                    List<String> b1True = valueList.get(3).get("truelist");
                    List<String> hTrue = valueList.get(0).get("truelist");
                    List<String> hFalse = valueList.get(0).get("truelist");
                    b1True.addAll(hTrue);
                    valueStack.add(new HashMap<>(){{
                        put("truelist",List.copyOf(b1True));
                        put("falselist",List.copyOf(hFalse));
                    }});
                }
                // B->H
                //{B.truelist = H.truelist;
                //B.falselist = H.falselist;}
                else if("B".equals(left) && "H".equals(tail)){
                    tmpMap = valueList.get(0);
                    Map<String, List<String>> finalTmpMap = tmpMap;
                    valueStack.add(new HashMap<>(){{
                        put("truelist",List.copyOf(finalTmpMap.get("truelist")));
                        put("falselist",List.copyOf(finalTmpMap.get("falselist")));
                    }});
                }
                //H -> H && BM I
                //{backpatch(H1.truelist, BM.quad);
                //H.truelist = I.truelist;
                //H.falselist = merge(H1.falselist, I.falselist);}
                else if("H".equals(left) && "H && BM I".equals(tail)){
                    List<String> h1True = valueList.get(3).get("truelist");
                    String quad = valueList.get(1).get("quad").get(0);
                    for(String s:h1True){
                        int index = Integer.valueOf(s);
                        String patched = add3Code.get(index)+" "+quad;
                        add3Code.set(index,patched);
                    }
                    List<String> iTrue = valueList.get(0).get("truelist");
                    List<String> h1False = valueList.get(3).get("falselist");
                    List<String> iFalse = valueList.get(0).get("falselist");
                    h1False.addAll(iFalse);
                    valueStack.add(new HashMap<>(){{
                        put("truelist",List.copyOf(iTrue));
                        put("falselist",List.copyOf(h1False));
                    }});
                }
                //H->I
                // {H.truelist = I.truelist;
                //H.falselist = I.falselist;}
                else if("H".equals(left) && "I".equals(tail)){
                    tmpMap = valueList.get(0);
                    Map<String, List<String>> finalTmpMap = tmpMap;
                    valueStack.add(new HashMap<>(){{
                        put("truelist",List.copyOf(finalTmpMap.get("truelist")));
                        put("falselist",List.copyOf(finalTmpMap.get("falselist")));
                    }});
                }
                //I -> ! I
                //{I.truelist = I1.falselist;
                //I.falselist = I1.truelist;}
                else if("I".equals(left) && "! I".equals(tail)){
                    tmpMap = valueList.get(0);
                    Map<String, List<String>> finalTmpMap = tmpMap;
                    valueStack.add(new HashMap<>(){{
                        put("truelist",List.copyOf(finalTmpMap.get("falselist")));
                        put("falselist",List.copyOf(finalTmpMap.get("truelist")));
                    }});
                }
                //I->(B)
                // {I.truelist = B.truelist;
                //I.falselist = B.falselist;}
                else if("I".equals(left) && "( B )".equals(tail)){
                    tmpMap = valueList.get(1);
                    Map<String, List<String>> finalTmpMap = tmpMap;
                    valueStack.add(new HashMap<>(){{
                        put("truelist",List.copyOf(finalTmpMap.get("truelist")));
                        put("falselist",List.copyOf(finalTmpMap.get("falselist")));
                    }});
                }
                //I->E relop E
                //{I.truelist = makelist(nextquad);
                //I.falselist = mekelist(nextquad + 1);
                //gen(‘if’ E1.addr relop.val E2.addr ‘goto’);
                //gen(‘goto’)}
                else if("I".equals(left) && "E relop E".equals(tail)){
                    String e1Addr = valueList.get(2).get("addr").get(0);
                    String e2Addr = valueList.get(0).get("addr").get(0);
                    String rel = valueList.get(1).get("val").get(0);
                    add3Code.add(String.format("if %s %s %s goto",e1Addr,rel,e2Addr));
                    add3Code.add("goto");
                    List<String> l1 = new ArrayList<>();
                    List<String> l2 = new ArrayList<>();
                    l1.add(String.valueOf(add3Code.size()-2));
                    l2.add(String.valueOf(add3Code.size()-1));
                    valueStack.add(new HashMap<>(){{
                        put("truelist",l1);
                        put("falselist",l2);
                    }});
                }
                // I->true
                //{I.truelist = mekelist(nextquad);
                //gen(‘goto’)}
                else if("I".equals(left) && "true".equals(tail)){
                    add3Code.add("goto");
                    List<String> l1 = new ArrayList<>();
                    l1.add(String.valueOf(add3Code.size()-1));
                    valueStack.add(new HashMap<>(){{
                        put("truelist",l1);
                    }});
                }
                // I->false
                //{I.falselist = makelist(nextquad);
                //gen(‘goto’)}
                else if("I".equals(left) && "false".equals(tail)){
                    add3Code.add("goto");
                    List<String> l1 = new ArrayList<>();
                    l1.add(String.valueOf(add3Code.size()-1));
                    valueStack.add(new HashMap<>(){{
                        put("falselist",l1);
                    }});
                }
                //relop -> <
                //{relop.val = ‘<’}
                else if("relop".equals(left) && "<".equals(tail)){
                    valueStack.add(new HashMap<>(){{
                        put("val", Arrays.asList("<"));
                    }});
                }
                else if("relop".equals(left) && ">".equals(tail)){
                    valueStack.add(new HashMap<>(){{
                        put("val", Arrays.asList(">"));
                    }});
                }
                else if("relop".equals(left) && "<=".equals(tail)){
                    valueStack.add(new HashMap<>(){{
                        put("val", Arrays.asList("<="));
                    }});
                }
                else if("relop".equals(left) && ">=".equals(tail)){
                    valueStack.add(new HashMap<>(){{
                        put("val", Arrays.asList(">="));
                    }});
                }
                else if("relop".equals(left) && "!=".equals(tail)){
                    valueStack.add(new HashMap<>(){{
                        put("val", Arrays.asList("!="));
                    }});
                }
                else if("relop".equals(left) && "==".equals(tail)){
                    valueStack.add(new HashMap<>(){{
                        put("val", Arrays.asList("=="));
                    }});
                }
                //Elist -> Elist , E
                //{Elist.size = Elist1.size + 1;}
                else if("Elist".equals(left) && "Elist , E".equals(tail)){
                    tmpMap = valueList.get(2);
                    Map<String, List<String>> finalTmpMap1 = tmpMap;
                    valueStack.add(new HashMap<>(){{
                        put("size",Arrays.asList(String.valueOf((Integer.valueOf(finalTmpMap1.get("size").get(0))+1))));
                    }});
                    paramList.add(valueList.get(0).get("addr").get(0));
                }
                //Elist -> E
                //{Elist.size = 1}
                else if("Elist".equals(left) && "E".equals(tail)){
                    valueStack.add(new HashMap<>(){{
                        put("size",Arrays.asList("1"));
                    }});
                    paramList = new ArrayList<>();
                    paramList.add(valueList.get(0).get("addr").get(0));
                }
            }




            reduceDetail.add(prod);
            symbolStack.add(left);

            //归约后的goto
            int currentStatus = statusStack.peek();
            if (lrTable.get(currentStatus).get(left) != null) {
                statusStack.add(Integer.valueOf(lrTable.get(currentStatus).get(left).substring(0)));
            } else {
                handleError();
            }
        } else if (action.equals("acc")) {
            System.out.println("识别成功");
            this.tokenIndex++;
            return true;
        }
        printStack();
        return false;
    }

    //打印栈内信息
    public void printStack() {
        System.out.println(String.format("%-90s", this.statusStack) + String.format("%-90s", this.symbolStack)+ String.format("%-90s", this.valueStack));
    }

    //处理错误
    public void handleError() {
        //恐慌模式，退回到能接受非终结符D的状态,并丢掉不能跟在非终结符D后面的输入。
        int currStatus = statusStack.peek();
        String lineNumber = tokens.get(tokenIndex).get(2);
        errorMessage.add("行数：" + tokens.get(tokenIndex).get(2) + " 错误状态：" + String.valueOf(currStatus) + "\n");

        List<Integer> statusList = new ArrayList<>(statusStack);
        for (int i = statusList.size() - 1; i >= 0; i--) {
            String goAction = lrTable.get(statusList.get(i)).get("D");
            if (goAction == null) {
                //若该状态无对应D的状态转移，则弹出
                if (statusStack.peek() == 0) break;
                statusStack.pop();
                symbolStack.pop();
            } else {
                //若该状态有对应D的状态转移，则分情况处理
                //非特殊处理的状态
                //压入D以及跳转的状态
                if (currStatus != 2 && currStatus != 104) {
                    int targetStatus = Integer.valueOf(goAction);
                    symbolStack.push("D");
                    statusStack.push(Integer.valueOf(goAction));
                } else {
                    //状态为2和104时，上述过程会陷入死循环，所以将前面的非初始状态全部弹出并压入D
                    while (true) {
                        if (statusStack.peek() != 0) {
                            statusStack.pop();
                            symbolStack.pop();
                        } else {
                            symbolStack.push("D");
                            statusStack.push(Integer.valueOf(lrTable.get(0).get("D")));
                            break;
                        }
                    }
                }
            }
        }

        //抛弃所有不在follow(D)中的token
        while (!follow.get("D").contains(tokens.get(tokenIndex).get(0))) {
            tokenIndex++;
        }
        //特殊情况：}，代表一个程序块的结束，直接将所有非初始状态都弹出，压入D
        if (tokens.get(tokenIndex).get(0).equals("}")) {
            while (true) {
                if (statusStack.peek() != 0) {
                    statusStack.pop();
                    symbolStack.pop();
                } else {
                    symbolStack.push("D");
                    statusStack.push(Integer.valueOf(lrTable.get(0).get("D")));
                    break;
                }
            }
            tokenIndex++;
        }

        //查找错误识别信息
        if (currStatus == 0) {
            errorMessage.add("Error at Line " + lineNumber + ": " + "[ Missing } ]" + "\n");
        } else if (errorCases.get(currStatus) != null) {
            errorMessage.add("Error at Line " + lineNumber + ": " + errorCases.get(currStatus) + "\n");
        } else {
            // 识别不到的语法错误
            errorMessage.add("Error at Line " + lineNumber + ": " + "[ Error ]" + "\n");
        }
    }

    //输出词法分析树
    public void outputResult() {
        System.out.println("P");
        if (reduceDetail.size() > 0)
            outputGramTree.add("P\n");
        outputRight(reduceDetail.size() - 1, 2);
    }

    public void outputRight(int root, int indent) {
        Express rootProd = reduceDetail.get(root);
        String left = rootProd.getLeft();
        List<String> right = Arrays.asList(rootProd.getRight());
        //System.out.println(left);
        for (int i = 0; i < right.size(); i++) {
            String r = right.get(i);
            System.out.println(new String(new char[indent]).replace("\0", " ") + r);
            outputGramTree.add(new String(new char[indent]).replace("\0", " ") + r + "\n");
            //终结符直接输出
            if (!nonterminal.contains(r)) {
                continue;
            }
            //非终结符递归查询
            else {
                int lastSymbolIndex = -1;
                for (int j = root - 1; j >= 0; j--) {
                    if (r.equals(reduceDetail.get(j).getLeft())) {
                        lastSymbolIndex = j;
                        break;
                    }
                }
                if (lastSymbolIndex != -1) {
                    outputRight(lastSymbolIndex, indent + 2);
                } else {
                    System.out.println("output error");
                }
            }
        }
    }

    //计算非终结符follow集
    public void getFollow() {
        boolean flag;
        follow.put("Program", Arrays.asList("$"));
        while (true) {
            flag = true;
            for (String nonterm : nonterminal) {
                for (int i = 0; i < productions.size(); i++) {
                    List<String> right = Arrays.asList(productions.get(i).getRight());
                    String left = productions.get(i).getLeft();
                    if (right.contains(nonterm)) {
                        int index = right.indexOf(nonterm);
                        if (index != right.size() - 1) {
                            Set<String> backFirst = action.getFirst(right.get(index + 1));
                            if (backFirst != null) {
                                for (String s : backFirst) {
                                    flag = addToFollow(nonterm, s);
                                }
                            }
                            if (backFirst.contains("ε")) {
                                List<String> list = follow.get(left);
                                if (list != null) {
                                    for (String s : list) {
                                        flag = addToFollow(nonterm, s);
                                    }
                                }
                            }
                        } else {
                            List<String> list = follow.get(left);
                            if (list != null) {
                                for (String s : follow.get(left)) {
                                    flag = addToFollow(nonterm, s);
                                }
                            }
                        }
                    }
                }
            }
            if (!flag) {
                break;
            }
        }
    }

    public boolean addToFollow(String key, String value) {
        if (follow.get(key) == null) {
            List<String> list = new ArrayList<>();
            list.add(value);
            follow.put(key, list);
            return true;
        } else {
            if (!follow.get(key).contains(value)) {
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
        int line = 0;
        for(String s:anaylser.add3Code){
            line++;
            System.out.println(line+" "+s);
        }
    }
}