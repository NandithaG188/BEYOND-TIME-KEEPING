package com.first.thewatch.ui.home;

import static androidx.fragment.app.Fragment.*;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.first.thewatch.R;
import com.first.thewatch.databinding.FragmentHomeBinding;

import java.util.Set;

public class HomeFragment extends Fragment {

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter bluetoothAdapter;
    private TextView pairedDeviceTextView;
    private BroadcastReceiver bluetoothStateReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get TextView reference
        pairedDeviceTextView = view.findViewById(R.id.text_home);

        // Check if Bluetooth is supported on this device
        if (bluetoothAdapter == null) {
            pairedDeviceTextView.setText("Bluetooth is not supported on this device");
        } else {
            // Check if Bluetooth is enabled, if not, ask the user to enable it
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                // Bluetooth is enabled, show the list of paired devices
                showPairedDevices();
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register BroadcastReceiver to listen for changes in Bluetooth state
        bluetoothStateReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_OFF) {
                        // Bluetooth is turned off, handle accordingly
                        pairedDeviceTextView.setText("Bluetooth is turned off");
                    } else if (state == BluetoothAdapter.STATE_ON) {
                        // Bluetooth is turned on, show the list of paired devices
                        showPairedDevices();
                    }
                } else if (action.equals(BluetoothDevice.ACTION_FOUND) || action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                    // A new Bluetooth device is discovered or bond state changed, update the list of paired devices
                    showPairedDevices();
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        requireActivity().registerReceiver(bluetoothStateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister BroadcastReceiver when the fragment is paused
        requireActivity().unregisterReceiver(bluetoothStateReceiver);
    }

    // Function to show paired Bluetooth devices
    private void showPairedDevices() {
        StringBuilder stringBuilder = new StringBuilder();
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (((Set<?>) pairedDevices).size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                stringBuilder.append(device.getName()).append("\n");
            }
        } else {
            stringBuilder.append("No paired devices found");
        }
        pairedDeviceTextView.setText(stringBuilder.toString());
    }
}