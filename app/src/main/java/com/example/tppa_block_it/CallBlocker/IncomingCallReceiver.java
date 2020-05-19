package com.example.tppa_block_it.CallBlocker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import com.android.internal.telephony.ITelephony;
import com.example.tppa_block_it.Notification.NotificationHelper;
import com.example.tppa_block_it.models.CallLog;
import com.example.tppa_block_it.models.Contact;
import com.example.tppa_block_it.models.ContactsCollection;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class IncomingCallReceiver extends BroadcastReceiver {
    private static final String TAG = "IncomingCallReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ITelephony telephonyService;
        try {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean show_notification = sharedPreferences.getBoolean("notify_call", true);
                boolean block_all = sharedPreferences.getBoolean("block_all_calls", false);
                boolean block_unknown = sharedPreferences.getBoolean("block_calls_unknown_numbers", false);
                boolean block_private = sharedPreferences.getBoolean("block_calls_private_numbers", false);

                List<Contact> blockedContacts = ContactsCollection.getInstance().getContactsWithBlockedCalls();
                int contactIndex = blockedContacts.indexOf(new Contact(number));
                if(contactIndex == -1) {
                    if(block_all || (block_unknown && isUnknown(context, number)) || (block_private && isPrivate(number))) {
                        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        try{
                            Method method = telephonyManager.getClass().getDeclaredMethod("getITelephony");
                            method.setAccessible(true);
                            telephonyService = (ITelephony) method.invoke(telephonyManager);
                            if(number != null) {
                                telephonyService.endCall();
                                if(show_notification) {
                                    showNotification(context, number);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    blockedContacts.get(contactIndex).addCallLog(new CallLog(new Date()));
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    try{
                        Method method = telephonyManager.getClass().getDeclaredMethod("getITelephony");
                        method.setAccessible(true);
                        telephonyService = (ITelephony) method.invoke(telephonyManager);
                        if(number != null) {
                            telephonyService.endCall();
                            if(show_notification) {
                                showNotification(context, number);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

//            if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
//                Toast.makeText(context, "Answered: " + number, Toast.LENGTH_SHORT).show();
//            }
//
//            if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
//                Toast.makeText(context, "Idle: " + number, Toast.LENGTH_SHORT).show();
//            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isPrivate(String number) {
         return Integer.parseInt(number) < 0;
    }

    public boolean isUnknown(Context context, String number) {
        Uri uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] projection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        try {
            if(cursor == null) {
                return true;
            }
            if (cursor.moveToFirst()) {
                cursor.close();
                return false;
            }
        } catch (Exception e) {
            return true;
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        return true;
    }

    public String getName(Context context, String number) {
        Uri uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] projection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        try {
            if(cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
        } catch (Exception e) {
            return null;
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public void showNotification(Context context, String number) {
        String name = getName(context, number);
        if(name == null) {
            NotificationHelper.showNotification(context, "New call blocked", "Call blocked from: " + number);
        } else {
            NotificationHelper.showNotification(context, "New call blocked", "Call blocked from: " + name + " (" + number + ")");
        }
    }
}
