package upjv.auroreleclerc.practicalworknotation;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;


public class WifiDirectActivity extends AppCompatActivity {
	private final IntentFilter intentFilter = new IntentFilter();
	WifiP2pManager.Channel channel;
	WifiP2pManager manager;
	WiFiDirectBroadcastReceiver receiver;
	private SwitchMaterial wifiState;
	private ProgressBar loading;
	private ChipGroup devices;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_direct);
		this.wifiState = findViewById(R.id.wifiState);
		this.wifiState.setClickable(false);
		this.devices = findViewById(R.id.devices);
		this.loading = findViewById(R.id.progressBar);

		// Indicates a change in the Wi-Fi Direct status.
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

		// Indicates a change in the list of available peers.
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

		// Indicates the state of Wi-Fi Direct connectivity has changed.
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

		// Indicates this device's details have changed.
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);
	}

	public void updateDevices(List<WifiP2pDevice> peers) {
		devices.removeAllViews();
		for (WifiP2pDevice peer : peers) {
			Chip chip = new Chip(this);
			chip.setText(peer.deviceName);
			chip.setOnClickListener(view -> {
				receiver.connect(peers.indexOf(peer));
				chip.setChecked(true);
			});
			this.devices.addView(chip);
		}
		loading.setVisibility(View.GONE);
		devices.setVisibility(View.VISIBLE);
	}

	@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
	private boolean checkNearbyRights() {
		if (ActivityCompat.checkSelfPermission(WifiDirectActivity.this, android.Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED) {
			return true;
		} else {
			Toast.makeText(WifiDirectActivity.this, "NEARBY_WIFI_DEVICES required ; search was cancelled", Toast.LENGTH_LONG).show();
			ActivityCompat.requestPermissions(WifiDirectActivity.this, new String[]{Manifest.permission.NEARBY_WIFI_DEVICES}, 200);
			return false;
		}
	}

	public void startSearch(View view) {
		devices.setVisibility(View.GONE);
		loading.setVisibility(View.VISIBLE);
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU || checkNearbyRights()) {
			manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
				@Override
				public void onSuccess() {
					// Code for when the discovery initiation is successful goes here.
					// No services have actually been discovered yet, so this method
					// can often be left blank. Code for peer discovery goes in the
					// onReceive method, detailed below.
				}

				@Override
				public void onFailure(int reasonCode) {
					switch (reasonCode) {
						case WifiP2pManager.ERROR:
							Toast.makeText(WifiDirectActivity.this, "Searched failed", Toast.LENGTH_SHORT).show();
							break;
						case WifiP2pManager.P2P_UNSUPPORTED:
							Toast.makeText(WifiDirectActivity.this, "WiFi Direct not supported", Toast.LENGTH_LONG).show();
							break;
						case WifiP2pManager.BUSY:
							Toast.makeText(WifiDirectActivity.this, "Already searching", Toast.LENGTH_SHORT).show();
							break;
						default:
							Toast.makeText(WifiDirectActivity.this, reasonCode + " is not a know error code of WifiP2pManager", Toast.LENGTH_LONG).show();
							Log.e("ERROR MANAGER", reasonCode + " is not a know error code of WifiP2pManager");
					}

					// Code for when the discovery initiation fails goes here.
					// Alert the user that something went wrong.
				}
			});
		}
	}

	public void setIsWifiP2pEnabled(boolean enabled) {
		loading.setVisibility(View.GONE);
		devices.setVisibility(View.GONE);
		this.wifiState.setChecked(enabled);
		findViewById(R.id.search).setEnabled(enabled);
	}

	/** register the BroadcastReceiver with the intent values to be matched */
	@Override
	public void onResume() {
		super.onResume();
		receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
		registerReceiver(receiver, intentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}
}
