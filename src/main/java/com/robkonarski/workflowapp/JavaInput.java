package com.robkonarski.workflowapp;

public class JavaInput {

    private int age;
    private String celebration;

    public JavaInput() {
    }

    public JavaInput(int age, String celebration) {
        this.age = age;
        this.celebration = celebration;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getCelebration() {
        return celebration;
    }

    public void setCelebration(String celebration) {
        this.celebration = celebration;
    }
}
