package com.hyunju.jin.movie.datamodel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TokenReceipt implements Serializable {

    @SerializedName("receiptCode") private int receiptCode;
    @SerializedName("sender") private int sender;
    @SerializedName("receiver") int receiver;

    @SerializedName("taskType") private String taskType;
    @SerializedName("val") private String val;
    @SerializedName("writeDate") String writeDate;
    @SerializedName("state") private String state;

    public int getReceiptCode() {
        return receiptCode;
    }

    public void setReceiptCode(int receiptCode) {
        this.receiptCode = receiptCode;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public int getReceiver() {
        return receiver;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getWriteDate() {
        return writeDate;
    }

    public void setWriteDate(String writeDate) {
        this.writeDate = writeDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
