package zkx;

public class FormatElement {
    // 要跳转到的状态
    int targetState;
    // s/r/acc，acc时targetState为null
    String info;

    public FormatElement(int targetState, String info) {
        this.targetState = targetState;
        this.info = info;
    }
}

