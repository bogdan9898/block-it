package com.example.tppa_block_it.models;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class Contact implements Serializable {
    public static final int BLOCKED_CALLS = 0b1;
    public static final int BLOCKED_SMS = 0b10;

    private String number;
    private String name;
    private List<CallLog> callLogList;
    private List<SmsLog> smsLogList;
    private int blockedState;

    public Contact(String number, String name) {
        this.number = number;
        this.name = name;
        callLogList = new ArrayList<CallLog>();
        smsLogList = new ArrayList<SmsLog>();
        blockedState = 0;
    }

    public Contact(String number) {
        this(number, null);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CallLog> getCallLogList() {
        return callLogList;
    }

    public void setCallLogList(List<CallLog> callLogList) {
        this.callLogList = callLogList;
    }

    public List<SmsLog> getSmsLogList() {
        return smsLogList;
    }

    public void setSmsLogList(List<SmsLog> smsLogList) {
        this.smsLogList = smsLogList;
    }

    public void addCallLog(CallLog callLog) {
        this.callLogList.add(callLog);
    }

    public void addSmsLog(SmsLog smsLog) {
        this.smsLogList.add(smsLog);
    }

    public void blockCalls() {
        this.blockedState = this.blockedState | BLOCKED_CALLS;
    }

    public void unblockCalls() {
        this.blockedState = this.blockedState & ~BLOCKED_CALLS;
    }

    public void blockSms() {
        this.blockedState = this.blockedState | BLOCKED_SMS;
    }

    public void unblockSms() {
        this.blockedState = this.blockedState & ~BLOCKED_SMS;
    }

    public boolean hasCallsBlocked() {
        return (this.blockedState & BLOCKED_CALLS) != 0;
    }

    public boolean hasSmsBlocked() {
        return (this.blockedState & BLOCKED_SMS) != 0;
    }

    public void clearCallsLogs() {
        this.callLogList.clear();
    }

    public void clearSmsLogs() {
        this.smsLogList.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(number, contact.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        if(this.name == null) {
            return this.number;
        } else {
            return this.name + " (" + this.number + ")";
        }
    }
}
