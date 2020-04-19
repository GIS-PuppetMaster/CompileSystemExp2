package application;


import grammar.Anaylser;
import Action.Express;
import zkx.AnalysisFormat;
import zkx.FormatElement;

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
    private static Anaylser anaylser;

    private String faPath;
    //private GramAnaylser anaylser;


    public static void main(String[] args) throws IOException {
        UI.anaylser = new Anaylser();
        JFrame frame = new JFrame("UI");
        frame.setContentPane(new UI().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800,1000));
        frame.pack();
        frame.setVisible(true);
    }

    public UI() {

        {
            AnalysisFormat analysisFormat = new AnalysisFormat(anaylser.action);
            List<Object> list = analysisFormat.buildFormat();
            Map<Integer, Map<String, FormatElement>> format = (Map<Integer, Map<String, FormatElement>>) list.get(0);
            textArea4.setText(analysisFormat.outputFormat(format));
        }
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
                anaylser.errorMessage = new ArrayList<>();
                anaylser.reduceDetail = new ArrayList<Express>();
                anaylser.outputGramTree = new ArrayList<>();
                anaylser.analyse();
                List<String> errorMessage = anaylser.errorMessage;
                List<Express> reduceDetail = anaylser.reduceDetail;
                Map<Integer,Map<String,String>> lrTable = anaylser.lrTable;
                List<String> outputGramTree = anaylser.outputGramTree;

                StringBuilder sb = new StringBuilder();
                for(String s:errorMessage){
                    sb.append(s);
                }
                textArea2.setText(sb.toString());

                sb = new StringBuilder();
                for(String s:outputGramTree){
                    sb.append(s);
                }
                textArea1.setText(sb.toString());

                sb = new StringBuilder();
                for(Express s:reduceDetail){
                    sb.append(s.toString()+"\n");
                }
                textArea3.setText(sb.toString());
            }
        });
    }
}
