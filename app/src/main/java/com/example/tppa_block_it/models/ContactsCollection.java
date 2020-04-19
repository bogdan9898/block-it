package com.example.tppa_block_it.models;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class ContactsCollection implements Serializable {
    private static final String TAG = "ContactsCollection";

    static private ContactsCollection instance = null;

    private List<Contact> contacts;

    private ContactsCollection() {
        this.contacts = new ArrayList<>();
    }

    private ContactsCollection(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public static ContactsCollection getInstance() {
        if(instance == null) {
            instance = new ContactsCollection();
        }
        return instance;
    }

//    public static ContactsCollection createInstance(List<Contact> contacts) {
//        instance = new ContactsCollection(contacts);
//        return instance;
//    }

    public Contact addUniqueContact(Contact contact) {
        if(this.contacts.contains(contact))
            return this.contacts.get(this.contacts.indexOf(contact));
        this.contacts.add(contact);
        return contact;
    }

    public void removeContact(Contact contact) {
        this.contacts.remove(contact);
    }

    public List<Contact> getContactsWithBlockedCalls() {
        ArrayList<Contact> filteredContacts = new ArrayList<Contact>();
        for(Contact contact : this.contacts) {
            if(contact.hasCallsBlocked()) {
                filteredContacts.add(contact);
            }
        }
        return filteredContacts;
    }

    public List<Contact> getContactsWithBlockedSms() {
        ArrayList<Contact> filteredContacts = new ArrayList<Contact>();
        for(Contact contact : this.contacts) {
            if(contact.hasSmsBlocked()) {
                filteredContacts.add(contact);
            }
        }
        return filteredContacts;
    }

    public static void dumpInstance(Context context, String filename) {
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(context.openFileOutput(filename, Context.MODE_PRIVATE));
            objectOutputStream.writeObject(getInstance().contacts);
            objectOutputStream.flush();

        } catch (IOException e) {
            Log.e(TAG, "dumpInstance: ", e);
        } finally {
            try {
                if(objectOutputStream != null) {
                    objectOutputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "dumpInstance: ", e);
            }
        }
    }

    public static void loadInstance(Context context, String filename) {
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(context.openFileInput(filename));
            getInstance().contacts = (List<Contact>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "loadInstance: ", e);
        } finally {
            try {
                if(objectInputStream != null)
                    objectInputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "loadInstance: ", e);
            }
        }
    }
}
