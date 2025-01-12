package upjv.auroreleclerc.practicalworknotation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
	private final List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	private final WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
		@Override
		public void onPeersAvailable(WifiP2pDeviceList peerList) {
			List<WifiP2pDevice> refreshedPeers = new ArrayList<>(peerList.getDeviceList());
			if (!refreshedPeers.equals(peers)) {
				peers.clear();
				peers.addAll(refreshedPeers);

				// If an AdapterView is backed by this data, notify it
				// of the change. For instance, if you have a ListView of
				// available peers, trigger an update.
				// ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

				wifiDirectActivity.updateDevices(peers);

				// Perform any other updates needed based on the new list of
				// peers connected to the Wi-Fi P2P network.
			}

			if (peers.isEmpty()) {
				Toast.makeText(wifiDirectActivity, "No devices found", Toast.LENGTH_SHORT).show();
			}
		}
	};

	private final WifiDirectActivity wifiDirectActivity;
	private final WifiP2pManager.Channel channel;
	private final WifiP2pManager manager;

	public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiDirectActivity wifiDirectActivity) {
		super();
		this.manager = manager;
		this.channel = channel;
		this.wifiDirectActivity = wifiDirectActivity;
	}

	public void connect(int position) {
		WifiP2pDevice device = peers.get(position);
		Toast.makeText(wifiDirectActivity, "Connection to : "+ device.toString(), Toast.LENGTH_SHORT).show();

		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;

		manager.connect(channel, config, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Snackbar.make(wifiDirectActivity.findViewById(R.id.search), "WiFi Direct connected", Snackbar.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(int reason) {
				Toast.makeText(wifiDirectActivity, "Connect failed", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Determine if Wi-Fi Direct mode is enabled or not, alert
			// the Activity.
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			boolean isWifiEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED;
			wifiDirectActivity.setIsWifiP2pEnabled(isWifiEnabled);
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			// Request available peers from the wifi p2p manager. This is an
			// asynchronous call and the calling activity is notified with a
			// callback on PeerListListener.onPeersAvailable()
			if (manager != null) {
				manager.requestPeers(channel, peerListListener);
			}
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			Toast.makeText(wifiDirectActivity, "WIFI_P2P_CONNECTION_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
			// Connection state changed! We should probably do something about
			// that.

		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			Toast.makeText(wifiDirectActivity, "DeviceListFragment", Toast.LENGTH_SHORT).show();
			//DeviceListFragment fragment = (DeviceListFragment) wifiDirectActivity.getFragmentManager().findFragmentById(R.id.frag_list);
			//fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
		}
	}
}
