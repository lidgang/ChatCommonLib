package com.jiubaisoft.chatlib.entity;

public class AppEntity {

    private int id;
    private int icon;
    private String funcName;

    public int getIcon() {
        return icon;
    }

    public String getFuncName() {
        return funcName;
    }

    public int getId() {
        return id;
    }

    public AppEntity(int icon, String funcName){
        this.icon = icon;
        this.funcName = funcName;
    }
}
