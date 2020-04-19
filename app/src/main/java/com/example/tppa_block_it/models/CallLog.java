package com.example.tppa_block_it.models;

import java.io.Serializable;
import java.util.Date;

public class CallLog implements Serializable {
    private Date date;

    public CallLog(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return date.toString();
    }
}
