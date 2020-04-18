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

    @Override
    public int hashCode() {
        return (info+targetState).hashCode();
    }

    @Override
    public String toString() {
        if(targetState!=-1) {
            return info + targetState;
        }
        else {
            return info;
        }
    }
}

