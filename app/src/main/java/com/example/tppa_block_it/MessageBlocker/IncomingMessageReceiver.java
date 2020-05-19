package com.example.tppa_block_it.MessageBlocker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import com.example.tppa_block_it.Notification.NotificationHelper;
import com.example.tppa_block_it.models.CallLog;
import com.example.tppa_block_it.models.Contact;
import com.example.tppa_block_it.models.ContactsCollection;
import com.example.tppa_block_it.models.SmsLog;

import java.util.Date;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class IncomingMessageReceiver extends BroadcastReceiver {
    private static final String TAG = "IncomingMessageReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean show_notification = sharedPreferences.getBoolean("notify_message", true);
        boolean block_all = sharedPreferences.getBoolean("block_all_messages", false);
        boolean block_unknown = sharedPreferences.getBoolean("block_messages_unknown_numbers", false);
        boolean block_private = sharedPreferences.getBoolean("block_messages_private_numbers", false);

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            SmsMessage smsMessage = Telephony.Sms.Intents.getMessagesFromIntent(intent)[0];
            String number = smsMessage.getOriginatingAddress();
            String content = smsMessage.getDisplayMessageBody();

            List<Contact> blockedContacts = ContactsCollection.getInstance().getContactsWithBlockedMessages();
            int contactIndex = blockedContacts.indexOf(new Contact(number));
            if(block_all || (block_unknown && isUnknown(context, number)) || (block_private && isPrivate(number))) {
                abortBroadcast();
                if(contactIndex != -1) {
                    blockedContacts.get(contactIndex).addSmsLog(new SmsLog(content, new Date()));
                }
                if(show_notification) {
                    showNotification(context, number);
                }
                return;
            }

            if(contactIndex != -1) {
                List<String> messagesFilter = blockedContacts.get(contactIndex).getMessagesFilter();

                if(messagesFilter.isEmpty()) {
                    abortBroadcast();
                    blockedContacts.get(contactIndex).addSmsLog(new SmsLog(content, new Date()));
                    if(show_notification) {
                        showNotification(context, number);
                    }
                    return;
                }

                for(String filter : messagesFilter) {
                    if(content.contains(filter)) {
                        abortBroadcast();
                        blockedContacts.get(contactIndex).addSmsLog(new SmsLog(content, new Date()));
                        if(show_notification) {
                            showNotification(context, number);
                        }
                        return;
                    }
                }
            }
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
            NotificationHelper.showNotification(context, "New message blocked", "Message blocked from: " + number);
        } else {
            NotificationHelper.showNotification(context, "New message blocked", "Message blocked from: " + name + " (" + number + ")");
        }
    }
}
