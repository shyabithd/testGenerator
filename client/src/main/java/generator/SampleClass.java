package generator;

public class SampleClass {

    private int inc;

    public void setInc(int inc) {
        this.inc = inc;
    }

    public int getInc() {
        if (inc < 5) {
            return inc + 5;
        } else {
            return inc * 3;
        }
    }
}
