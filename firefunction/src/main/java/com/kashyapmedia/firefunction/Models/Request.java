package com.kashyapmedia.firefunction.Models;

import java.lang.reflect.Type;

public class Request {
    private String functionName;
    private Class bodyType;
    private Type returnType;
    private Class returnEnclosingClass;
    private Object body;
    private String functionRegion;

    public Request() {
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public Class getBodyType() {
        return bodyType;
    }

    public void setBodyType(Class bodyType) {
        this.bodyType = bodyType;
    }


    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String getFunctionRegion() {
        return functionRegion;
    }

    public void setFunctionRegion(String functionRegion) {
        this.functionRegion = functionRegion;
    }

    public Class getReturnEnclosingClass() {
        return returnEnclosingClass;
    }

    public void setReturnEnclosingClass(Class returnEnclosingClass) {
        this.returnEnclosingClass = returnEnclosingClass;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }
}
