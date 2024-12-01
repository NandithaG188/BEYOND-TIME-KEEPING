package com.first.thewatch.ui.home;

import androidx.fragment.app.Fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.first.thewatch.DatabaseHelper;
import com.first.thewatch.R;

import java.util.Objects;

public class AddContactFragment extends Fragment {

    private EditText editTextNewEmail;
    private Button buttonAddNewEmail, buttonDeleteSelected;
    private ListView listViewContacts;
    private ArrayAdapter<String> contactsAdapter;
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_contact, container, false);

        editTextNewEmail = view.findViewById(R.id.editTextNewEmail);
        buttonAddNewEmail = view.findViewById(R.id.buttonAddNewEmail);
        buttonDeleteSelected = view.findViewById(R.id.buttonDeleteSelected);
        listViewContacts = view.findViewById(R.id.listViewContacts);

        databaseHelper = new DatabaseHelper(requireContext());

        // Set up adapter for the ListView
        contactsAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_multiple_choice);
        listViewContacts.setAdapter(contactsAdapter);

        // Populate ListView with saved contacts
        displaySavedContacts();

        // Button to add new email
        buttonAddNewEmail.setOnClickListener(v -> addNewEmail());

        // Button to delete selected contacts
        buttonDeleteSelected.setOnClickListener(v -> deleteSelectedContacts());

        return view;
    }

    // Method to display saved contacts in the ListView
    private void displaySavedContacts() {
        contactsAdapter.clear(); // Clear existing items
        Cursor cursor = databaseHelper.getAllEmails();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String emailColumnName = DatabaseHelper.getColumnEmail();
                int columnIndex = cursor.getColumnIndex(emailColumnName);

                // Check if the column index is valid (not equal to -1)
                if (columnIndex != -1) {
                    // Retrieve the email value using the valid column index
                    String email = cursor.getString(columnIndex);

                    // Check for null values before adding to the adapter
                    if (email != null) {
                        contactsAdapter.add(email);
                    }
                }

            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    // Method to add a new email to the database
    private void addNewEmail() {
        String newEmail = editTextNewEmail.getText().toString().trim();
        if (!newEmail.isEmpty()) {
            long result = databaseHelper.addEmail(newEmail);
            if (result != -1) {
                editTextNewEmail.setText(""); // Clear input field
                contactsAdapter.add(newEmail);
                Toast.makeText(requireContext(), "Email added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Failed to add email", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Please enter an email address", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to delete selected contacts
    private void deleteSelectedContacts() {
        // Get the positions of checked items
        SparseBooleanArray checkedPositions = listViewContacts.getCheckedItemPositions();
        for (int i = checkedPositions.size() - 1; i >= 0; i--) {
            int position = checkedPositions.keyAt(i);
            if (checkedPositions.valueAt(i)) {
                // Check if the position is within the bounds of the adapter
                if (position >= 0 && position < contactsAdapter.getCount()) {
                    String email = contactsAdapter.getItem(position);
                    if (email != null) {
                        databaseHelper.deleteEmail(email);
                        contactsAdapter.remove(email);
                    }
                }
            }
        }
        Toast.makeText(requireContext(), "Selected contacts deleted", Toast.LENGTH_SHORT).show();
    }
}
