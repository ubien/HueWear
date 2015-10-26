package huewear.huewear;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import huewear.common.MessagePaths;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnSeekBarChangeListener {
	public static final String TAG = "MainActivity";
	private GoogleApiClient mGoogleApiClient;
	private SeekBar mBrightnessBar;
	private static final int MAX_BRIGHTNESS = 254;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
		stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
			@Override
			public void onLayoutInflated(WatchViewStub stub) {
				mBrightnessBar = (SeekBar) stub.findViewById(R.id.brightnessBar);
				mBrightnessBar.setOnSeekBarChangeListener((OnSeekBarChangeListener)stub.getContext());
			}
		});
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		mGoogleApiClient.connect();

	}

	@Override
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
		super.onStop();
	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.d(TAG, "onConnected");
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.d(TAG, "onConnectionSuspended");
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.e(TAG, "Failed to connect to Google API Client");
	}

	public void sendMessage(String path){
		sendMessage(path, "");
	}

	public void sendMessage(String path, String val){
		if(mGoogleApiClient.isConnected()) {
			final String pathLocation = path;
			final String msgVal = val;
			new Thread(new Runnable() {
				@Override
				public void run() {
					NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
					for(Node node : nodes.getNodes()) {
						MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), pathLocation, msgVal.getBytes()).await();
						if(!result.getStatus().isSuccess()){
							Log.e(TAG, "error");
						} else {
							Log.i(TAG, "success!! sent to: " + node.getDisplayName());
						}
					}
				}
			}).start();
		} else {
			Log.e(TAG, "not connected");
		}
	}

	public void onRandomClicked(View target){
		this.sendMessage(MessagePaths.LIGHTS_RANDOM);
	}

	public void onOffClicked(View target){ this.sendMessage(MessagePaths.LIGHTS_OFF); }

	//SeekBar methods
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
								  boolean fromUser) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		int progress = seekBar.getProgress();
		Log.i("Brightness ", String.valueOf(progress));
		int newVal = (int)(progress * .01 * MAX_BRIGHTNESS);
		this.sendMessage(MessagePaths.LIGHTS_BRIGTHNESS, String.valueOf(newVal));
	}

	public void showToast(final String toast)
	{
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
			}
		});
	}
}


