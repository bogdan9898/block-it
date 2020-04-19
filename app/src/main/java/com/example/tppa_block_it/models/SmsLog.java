package com.example.tppa_block_it.models;

import java.io.Serializable;
import java.util.Date;

public class SmsLog implements Serializable {
    private String message;
    private Date date;

    public SmsLog(String message, Date date) {
        this.message = message;
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message + " (" + date.toString() + ")";
    }
}
