package algorithms;

import org.apache.spark.util.AccumulatorV2;

public class ComplianceAccumulatorV2 extends AccumulatorV2<String,String> {
    private String str = "";

    //Returns if this accumulator is zero value or not.
    // e.g. for a counter accumulator, 0 is zero value; for a list accumulator, Nil is zero value.
    public boolean isZero() {
        return str == "";
    }

    //Creates a new copy of this accumulator.
    public AccumulatorV2<String, String> copy() {
        ComplianceAccumulatorV2 newAccumulator = new ComplianceAccumulatorV2();
        newAccumulator.str = this.str;
        return newAccumulator;
    }

    //Resets this accumulator, which is zero value. i.e. call isZero must return true.
    public void reset() {
        str = "";
    }

    //Takes the inputs and accumulates.
    public void add(String v) {
        str += v ;
    }

    //Merges another same-type accumulator into this one and update its state, i.e. this should be merge-in-place.
    public void merge(AccumulatorV2<String, String> other) {
        ComplianceAccumulatorV2 o =(ComplianceAccumulatorV2) other;
        str += o.str;
    }

    //Defines the current value of this accumulator
    public String value() {
        return str;
    }

}
