package com.robkonarski.workflowapp;

import java.util.List;

public class JavaOutput {

    private List <String> titles;

    public JavaOutput() {
    }

    public JavaOutput(List<String> titles) {
        this.titles = titles;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }
}
