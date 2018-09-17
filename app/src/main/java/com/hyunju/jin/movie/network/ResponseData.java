package com.hyunju.jin.movie.network;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author hjjin on 2018-05-26.
 *         Description:
 */

public class ResponseData implements Serializable{

    public static final int RESPONSE_OK = 1;

    @SerializedName("code") private int code;
    @SerializedName("msg") private String msg;

    public ResponseData(){}

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
