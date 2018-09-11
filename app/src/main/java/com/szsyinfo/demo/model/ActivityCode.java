package com.szsyinfo.demo.model;

/** Activity 功能页对应的编号，作用于返回 */
public enum ActivityCode {

    SCan(1,"扫码"),
    Other(2,"..."),
    Other2(3,"...");

    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    ActivityCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}


