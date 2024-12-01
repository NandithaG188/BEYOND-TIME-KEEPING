package com.first.thewatch.ui.home;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.first.thewatch.DatabaseHelper;
import com.first.thewatch.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class FCMTokenFragment extends Fragment {

    private TextView textViewToken;
    private EditText editTextEmail, editTextPassword;
    private Button buttonGenerateToken, buttonSignIn, buttonRegister,buttonSaveToken;
    private DatabaseHelper databaseHelper;
    private FirebaseAuth firebaseAuth;

    private Button buttonCopyToken;

    private String currentToken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fcm_token, container, false);

        initializeViews(view);
        setupListeners();

        textViewToken = view.findViewById(R.id.textViewToken);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        buttonGenerateToken = view.findViewById(R.id.buttonGenerateToken);
        buttonSignIn = view.findViewById(R.id.buttonSignIn);
        buttonRegister = view.findViewById(R.id.buttonRegister);
        buttonSaveToken = view.findViewById(R.id.buttonSaveToken);

        databaseHelper = new DatabaseHelper(requireContext());
        firebaseAuth = FirebaseAuth.getInstance();

        buttonSignIn.setOnClickListener(v -> signIn());
        buttonRegister.setOnClickListener(v -> register());
        buttonGenerateToken.setOnClickListener(v -> generateToken());
        buttonSaveToken.setOnClickListener(v -> saveTokenToFirebase());


        return view;
    }

    private void signIn() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        if (!isValidEmail(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Signed in successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getContext(), "Sign in failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void register() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        if (!isValidEmail(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Registered successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getContext(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void generateToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        currentToken = task.getResult();
                        textViewToken.setText(currentToken);
                        saveTokenToDatabase(currentToken);


                    } else {
                        Toast.makeText(getContext(), "Failed to generate FCM token", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void saveTokenToFirebase() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null && user.getEmail() != null && currentToken != null) {
            String email = user.getEmail();
            String sanitizedEmail = sanitizeEmail(email);
            DatabaseReference tokensRef = FirebaseDatabase.getInstance().getReference("tokens");
            tokensRef.child(sanitizedEmail).setValue(currentToken)
                    .addOnSuccessListener(aVoid -> textViewToken.append("\nToken saved in Firebase"))
                    .addOnFailureListener(e -> textViewToken.append("\nFailed to save token in Firebase: " + e.getMessage()));
        } else {
            Toast.makeText(getContext(), "User not authenticated or token not generated", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTokenToDatabase(String token) {
        long result = databaseHelper.saveFCMToken(token);
        if (result == -1) {
            Toast.makeText(getContext(), "Failed to save token locally", Toast.LENGTH_SHORT).show();
        }
    }

    private String sanitizeEmail(String email) {
        return email.replace(".", ",");
    }

    private void initializeViews(View view) {
        textViewToken = view.findViewById(R.id.textViewToken);
        buttonCopyToken = view.findViewById(R.id.buttonCopyToken);
    }

    private void setupListeners() {
        buttonCopyToken.setOnClickListener(v -> copyTokenToClipboard());
    }

    private void copyTokenToClipboard() {
        CharSequence token = textViewToken.getText();
        if (TextUtils.isEmpty(token)) {
            Toast.makeText(getContext(), "No token to copy", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("FCM Token", token);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Token copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
