package org.xlbean.xlscript.sample;

public class SampleBizLogic {

    private String sample;

    private int test;

    public String testMethod(String test) {
        return test + sample;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public int getTest() {
        return test;
    }

    public void setTest(int test) {
        this.test = test;
    }
}
