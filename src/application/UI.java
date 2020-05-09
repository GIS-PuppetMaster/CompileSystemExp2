package application;


import grammar.Anaylser;
import Action.Express;
import zkx.AnalysisFormat;
import zkx.FormatElement;

import javax.sound.sampled.Line;
import javax.swing.*;
import javax.swing.text.TabableView;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class UI {
    private JPanel panel1;
    private JTextArea textArea;
    private JButton Button3;
    private JTabbedPane tabbedPane1;
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JTextArea textArea3;
    private JTextArea textArea4;
    private Anaylser anaylser;

    private String faPath;
    //private GramAnaylser anaylser;


    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("UI");
        frame.setContentPane(new UI().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800,1000));
        frame.pack();
        frame.setVisible(true);
    }
    public static String outputSymbolTable(Map<String, List<String>> table){
        StringBuilder stringBuilder = new StringBuilder();
        String form = "%22s %20s %20s %16s\n";
        stringBuilder.append(String.format("%16s %16s %16s %8s\n","标识符","类型","偏移量","行号"));
        for(Map.Entry<String,List<String>> entry : table.entrySet()){
            stringBuilder.append(String.format(form,entry.getKey(),entry.getValue().get(0),entry.getValue().get(1),entry.getValue().get(2)));
        }
        return stringBuilder.toString();
    }
    public static String outputCode(List<String> addr3code, List<String> tuple4code){
        StringBuilder stringBuilder = new StringBuilder();
        String form = "%32s %30s\n";
        stringBuilder.append(String.format("%24s %24s\n","三地址码","四元式"));
        int size = addr3code.size();
        for(int i=0;i<size;i++){
            stringBuilder.append(String.format(form,addr3code.get(i),tuple4code.get(i),"行号"));
        }
        return stringBuilder.toString();
    }
    public UI() {

        //词法分析
        Button3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String text = textArea.getText();
                File file = new File("src/grammar/test.txt");
                try {
                    PrintWriter writer = new PrintWriter(file);
                    writer.print(text);
                    writer.close();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
                //String res = dfa.parse("code.txt");
                //System.out.println(res);
                //textArea1.setText(res);
                try {
                    anaylser = new Anaylser();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                anaylser.analyse();
                List<String> errorMessage = anaylser.errorLog;
                List<Express> reduceDetail = anaylser.reduceDetail;
                Map<Integer,Map<String,String>> lrTable = anaylser.lrTable;
                List<String> outputGramTree = anaylser.outputGramTree;

                StringBuilder sb = new StringBuilder();
                for(String s:errorMessage){
                    sb.append(s);
                }
                textArea2.setText(sb.toString());
                textArea4.setText(outputCode(anaylser.add3Code, anaylser.tuple4Code));

                textArea1.setText(outputSymbolTable(anaylser.symbolTable));
            }
        });
    }
}
