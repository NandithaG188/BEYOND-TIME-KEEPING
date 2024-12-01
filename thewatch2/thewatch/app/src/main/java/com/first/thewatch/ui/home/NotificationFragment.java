package com.first.thewatch.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.first.thewatch.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationFragment extends Fragment {

    private EditText editTextFCMToken;
    private Button buttonSendNotification;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        editTextFCMToken = view.findViewById(R.id.editTextFCMToken);
        buttonSendNotification = view.findViewById(R.id.buttonSendNotification);

        buttonSendNotification.setOnClickListener(v -> sendNotification());

        return view;
    }

    private void sendNotification() {
        String token = editTextFCMToken.getText().toString().trim();
        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a valid FCM token", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construct data payload for notification
        RemoteMessage.Builder messageBuilder = new RemoteMessage.Builder(token + "@fcm.googleapis.com")
                .setMessageId(Integer.toString((int) System.currentTimeMillis()))
                .addData("title", "Test Notification")
                .addData("message", "Hello, this is a test notification!");

        // Send the notification
        FirebaseMessaging.getInstance().send(messageBuilder.build());
        Toast.makeText(getContext(), "Notification sent to the provided token", Toast.LENGTH_SHORT).show();
    }
}
