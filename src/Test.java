import Action.Action;
import zkx.AnalysisFormat;
import zkx.FormatElement;


import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws IOException {
        Action action = new Action();
        action.init();
        AnalysisFormat analysisFormat = new AnalysisFormat(action);
        List<Object> list = analysisFormat.buildFormat();
        Map<Integer, Map<String, FormatElement>> format = (Map<Integer, Map<String, FormatElement>>) list.get(0);
        analysisFormat.outputFormat(format);
    }
}
