import Action.Action;
import grammar.Anaylser;
import zkx.AnalysisFormat;
import zkx.FormatElement;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws IOException {
        Anaylser anaylser;
        //String res = dfa.parse("code.txt");
        //System.out.println(res);
        //textArea1.setText(res);
        try {
            anaylser = new Anaylser();
            anaylser.analyse();
            int a =0 ;
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
