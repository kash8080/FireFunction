package com.kashyapmedia.firebaselib.Models;

public class ProfileResponseBody {
    private int age;
    private String name;

    public ProfileResponseBody() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
