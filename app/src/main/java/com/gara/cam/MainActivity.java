package com.gara.cam;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gara.cam.data.RecSettings;

public class MainActivity extends AppCompatActivity implements ForegroundService.statusListener {

	private static SettingsFragment settingsFragment;
	private static BroadcastReceiver broadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ThemeUtils.setStatusBarAppearance(this);
		if (broadcastReceiver==null) broadcastReceiver = new DeviceStatusReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.ACTION_BATTERY_LOW");
		filter.addAction("android.intent.action.ACTION_DEVICE_STORAGE_LOW");
		filter.addAction("android.intent.action.ACTION_SHUTDOWN");
		ContextCompat.registerReceiver(getApplicationContext(),broadcastReceiver, filter, ContextCompat.RECEIVER_EXPORTED);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
			if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) ||
					(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
			}
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 123);
			}
		} else {
			if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) ||
					(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) ||
					(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_VIDEO))) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.READ_MEDIA_VIDEO}, 123);
			}
		}

		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SettingsCommon.setDefaultValues(context, prefs);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ForegroundService.registerStatusListener(this);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
			if (settingsFragment==null) {
				settingsFragment = new SettingsFragment();
				settingsFragment.setRetainInstance(true);
			}
				getFragmentManager().beginTransaction()
						.replace(R.id.fragment_container, settingsFragment)
						.commit();

		} else 	Toast.makeText(this, getString(R.string.error_missing_permission), Toast.LENGTH_SHORT).show();
	}

	public void actionStart(MenuItem item) {
		ServiceHelper helper = new ServiceHelper(getApplicationContext());
		helper.start(true);
		invalidateOptionsMenu();
	}

	@Override
	protected void onDestroy() {
		broadcastReceiver = null;
		super.onDestroy();
	}

	public void actionStop(MenuItem item) {
		ServiceHelper helper = new ServiceHelper(getApplicationContext());
		helper.stop();
		invalidateOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (ForegroundService.mIsRunning){
			menu.findItem(R.id.action_start).setEnabled(false);
			menu.findItem(R.id.action_stop).setEnabled(true);
		} else {
			menu.findItem(R.id.action_start).setEnabled(true);
			menu.findItem(R.id.action_stop).setEnabled(false);
		}
		return true;
	}

	@Override
	public void onServiceStatusChange(boolean status) {
		invalidateOptionsMenu();
	}
}
