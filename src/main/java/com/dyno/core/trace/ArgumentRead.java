package com.dyno.core.trace;

public class ArgumentRead extends RWOperation{
    private String value;
    private int argumentNumber;
    private String calledFunction;

    public String getValue() {
        return value;
    }

    public void setValue(String o) {
        value = o;
    }
    
    public int getArgumentNumber() {
        return argumentNumber;
    }

    public void setArgumentNumber(int o) {
        argumentNumber = o;
    }
    
    public String getFunctionName() {
        return calledFunction;
    }

    public void setFunctionName(String s) {
    	calledFunction = s;
    }
}
