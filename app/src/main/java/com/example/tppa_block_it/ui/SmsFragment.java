package com.example.tppa_block_it.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.tppa_block_it.R;
import com.example.tppa_block_it.models.Contact;
import com.example.tppa_block_it.models.ContactsCollection;
import com.example.tppa_block_it.models.SmsLog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class SmsFragment extends Fragment {
    private static final int READ_CONTACTS_PERMISSION_CODE = 0;
    private static final String TAG = "SmsFragment";

    private List<Contact> blockedContacts;
    private static boolean areFabsOpen = false;

    public SmsFragment() {
        this.blockedContacts = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_sms, container, false);

        FloatingActionButton fab_contact = getActivity().findViewById(R.id.fab_contact);
        fab_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showContacts();
                playFabAnimations();
            }
        });

        FloatingActionButton fab_number = getActivity().findViewById(R.id.fab_number);
        fab_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("Type a phone number");
                final FrameLayout frameLayout = new FrameLayout(getContext());
                frameLayout.setPadding(32, 16, 32, 16);
                final EditText editText = new EditText(getActivity());
                frameLayout.addView(editText);
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
                builder.setView(frameLayout);
                builder.setPositiveButton("Block", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContactsCollection.getInstance().addUniqueContact(new Contact(editText.getText().toString())).blockMessages();
                        Snackbar.make(getView(), "New phone number blocked", Snackbar.LENGTH_SHORT).show();
                        onResume();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        editText.clearFocus();
                    }
                });
                builder.create().show();
                editText.requestFocus();
                playFabAnimations();
            }
        });

        final FloatingActionButton fab_add = getActivity().findViewById(R.id.fab_add);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playFabAnimations();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        this.blockedContacts = ContactsCollection.getInstance().getContactsWithBlockedMessages();
        if(blockedContacts.isEmpty()) {
            getActivity().findViewById(R.id.messages_empty_list_text).setVisibility(View.VISIBLE);
        } else {
            getActivity().findViewById(R.id.messages_empty_list_text).setVisibility(View.INVISIBLE);
        }

        ListView listView = getActivity().findViewById(R.id.messages_blocked_contacts_list_view);
        ArrayAdapter<Contact> adapter = new ArrayAdapter<Contact>(
                getActivity(),
                R.layout.custom_spinner_dropdown_item,
                blockedContacts
        );
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = blockedContacts.get(position);
                List<SmsLog> logsList = contact.getSmsLogList();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Blocked messages from:\n" + contact);
                if(logsList.isEmpty()) {
                    TextView textView = new TextView(getActivity());
                    textView.setText("No history");
                    textView.setTextColor(Color.parseColor("#aaaaaa"));
                    textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                    textView.setPadding(0, 100, 0, 100);
                    builder.setView(textView);
                } else {
                    ListView logsListView = new ListView(getActivity());
                    ArrayAdapter<SmsLog> logsAdapter = new ArrayAdapter<SmsLog>(
                            getActivity(),
                            R.layout.custom_spinner_dropdown_item_lpadded,
                            logsList);
                    logsListView.setAdapter(logsAdapter);
                    builder.setView(logsListView);
                }
                builder.create().show();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(blockedContacts.get(position).toString());
                String[] options = {"Unblock messages", "Clear logs", "Messages filter"};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        getActivity(),
                        R.layout.custom_spinner_dropdown_item_lpadded,
                        options
                );
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // unblock messages
                                blockedContacts.get(position).unblockMessages();
                                Snackbar.make(getView(), "Phone number removed from blacklist", Snackbar.LENGTH_SHORT).show();
                                onResume();
                                break;
                            case 1: // clear logs
                                blockedContacts.get(position).clearSmsLogs();
                                Snackbar.make(getView(), "Logs cleared successfully", Snackbar.LENGTH_SHORT).show();
                                break;
                            case 2: // messages filter
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                
                                builder.create().show();
                                break;
                            default:
                                Snackbar.make(getView(), "Unknown option", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.create().show();
                return true;
            }
        });
    }

    private void showContacts() {
        if(ActivityCompat.checkSelfPermission(
                getContext(),
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[] {
                            Manifest.permission.READ_CONTACTS
                    },
                    READ_CONTACTS_PERMISSION_CODE);
            return;
        }

        final List<Contact> contactList = new ArrayList<Contact>();

        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null);
        if(cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor contactCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);
                    while(contactCursor.moveToNext()) {
                        String phoneNumber = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Contact contact = new Contact(phoneNumber, name);
                        if(!contactList.contains(contact)) {
                            contactList.add(contact);
                        }
                    }
                }
            }
        }

//        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustomStyle));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if(contactList.isEmpty()) {
            TextView textView = new TextView(getActivity());
            textView.setText("No contact found");
            textView.setTextColor(getResources().getColor(R.color.text_secondary));
            textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            textView.setPadding(0, 100, 0, 100);
            builder.setView(textView);
        } else {
            final ContactsCollection contactsCollection = ContactsCollection.getInstance();
            ArrayAdapter<Contact> adapter = new ArrayAdapter<Contact>(
                    getActivity(),
                    R.layout.custom_spinner_dropdown_item_lpadded,
                    contactList
            );

            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(getActivity(), contactList.get(which).toString(), Toast.LENGTH_SHORT).show();
                    Snackbar.make(getView(), "New contact blocked", Snackbar.LENGTH_SHORT).show();
                    contactsCollection.addUniqueContact(contactList.get(which)).blockMessages();
                    onResume();
                }
            });
        }

        builder.setTitle("Choose a contact");
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_CONTACTS_PERMISSION_CODE:
                if(grantResults.length >= 1) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                        Toast.makeText(getActivity(), "Read contacts permission denied", Toast.LENGTH_SHORT).show();
                    else
                        showContacts();
                }
                break;
        }
    }

    private void playFabAnimations() {
        if(!areFabsOpen) {
            FloatingActionButton fab_add = getActivity().findViewById(R.id.fab_add);
            fab_add.animate().rotation(45).setDuration(250).start();

            FloatingActionButton fab_contact = getActivity().findViewById(R.id.fab_contact);
            fab_contact.animate().alpha(1).setDuration(0).start();
            fab_contact.animate().translationY(-110).setDuration(250).start();

            FloatingActionButton fab_number = getActivity().findViewById(R.id.fab_number);
            fab_number.animate().alpha(1).setDuration(0).start();
            fab_number.animate().translationY(-200).setDuration(250).start();

            TextView textView_contact = getActivity().findViewById(R.id.textView_contact);
            textView_contact.animate().translationY(-110).setDuration(0).start();
            textView_contact.animate().alpha(1).setDuration(250).start();

            TextView textView_number = getActivity().findViewById(R.id.textView_number);
            textView_number.animate().translationY(-200).setDuration(0).start();
            textView_number.animate().alpha(1).setDuration(250).start();
        } else {
            final TextView textView_number = getActivity().findViewById(R.id.textView_number);
            textView_number.animate().alpha(0).setDuration(250).withEndAction(new Runnable() {
                @Override
                public void run() {
                    textView_number.animate().translationY(0).setDuration(0).start();
                }
            }).start();

            final TextView textView_contact = getActivity().findViewById(R.id.textView_contact);
            textView_contact.animate().alpha(0).setDuration(250).withEndAction(new Runnable() {
                @Override
                public void run() {
                    textView_contact.animate().translationY(0).setDuration(0).start();
                }
            }).start();

            FloatingActionButton fab_number = getActivity().findViewById(R.id.fab_number);
            fab_number.animate().translationY(0).setDuration(250).start();
            fab_number.animate().alpha(1).setDuration(0).start();

            FloatingActionButton fab_contact = getActivity().findViewById(R.id.fab_contact);
            fab_contact.animate().translationY(0).setDuration(250).start();
            fab_contact.animate().alpha(1).setDuration(0).start();

            FloatingActionButton fab_add = getActivity().findViewById(R.id.fab_add);
            fab_add.animate().rotation(0).setDuration(250).start();
        }
        areFabsOpen = !areFabsOpen;
    }

}
