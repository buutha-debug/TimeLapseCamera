package com.gara.cam;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.gara.cam.data.RecSettings;
import com.gara.cam.preference.SeekBarPreference;
import com.gara.cam.sensor.CameraSettings;

public class SettingsCommon implements OnSharedPreferenceChangeListener, SeekBarPreference.OnFormatOutputValueListener {

	private Context context;
	private CameraSettings cameraSettings;
	private ListPreference prefFrameSize;
	private ListPreference prefFrameRate;
	private ListPreference prefCamera;
	private SeekBarPreference prefStopRecAfter;
	private SeekBarPreference prefExposureComp;
	private SeekBarPreference prefZoom;
	private EditTextPreference prefVideoEncodingBitRate;

	private int calcGcd(int a, int b) {
		if (b == 0) return a;
		return calcGcd(b, a % b);
	}

	private void setFrameSizes(final SharedPreferences prefs) {
		int camId = RecSettings.getInteger(prefs, "pref_camera", 0);
		String defSize = prefs.getString("pref_frame_size", "1920x1080");
		final List<int[]> sizes = cameraSettings.getFrameSizes(prefs, camId, false);

		final ArrayList<String> sizesList = new ArrayList<String>();
		final ArrayList<String> sizesListVal = new ArrayList<String>();
		int defInd = sizes.size() - 1;

		for (int[] size : sizes) {
			int gcd = calcGcd(size[0], size[1]);
			String value = size[0] + "x" + size[1];
			if (defSize.equals(value))
				defInd = sizesListVal.size();
			sizesList.add(value + " (" + (size[0] / gcd) + ":" + (size[1] / gcd) + ")");
			sizesListVal.add(value);
		}

		final int index = defInd;
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				prefFrameSize.setEntries(sizesList.toArray(new String[sizesList.size()]));
				prefFrameSize.setEntryValues(sizesListVal.toArray(new String[sizesListVal.size()]));
				if (index >= 0 && sizesList.size() > 0) {
					prefFrameSize.setValueIndex(index);
					prefFrameSize.setSummary(sizesList.get(index));
				}
				updatePrefStatus(prefs);
			}
		});
	}

	private void setFrameRates(final SharedPreferences prefs) {
		int camId = RecSettings.getInteger(prefs, "pref_camera", 0);
		String defFps = prefs.getString("pref_frame_rate", "30");
		final List<Integer> fpsIntList = cameraSettings.getFrameRates(prefs, camId);
		final ArrayList<String> fpsList = new ArrayList<String>();
		final ArrayList<String> fpsListVal = new ArrayList<String>();
		int defInd = fpsIntList.size() - 1;

		for (Integer fpsInt : fpsIntList) {
			String fps = fpsInt.toString();
			if (defFps.equals(fps)) defInd = fpsList.size();
			fpsListVal.add(fps);
			fpsList.add(fps + " " + context.getString(R.string.format_fps));
		}

		final int index = defInd;
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				prefFrameRate.setEntries(fpsList.toArray(new String[fpsList.size()]));
				prefFrameRate.setEntryValues(fpsListVal.toArray(new String[fpsListVal.size()]));
				if (index >= 0 && fpsList.size() > 0) {
					prefFrameRate.setValueIndex(index);
					prefFrameRate.setSummary(fpsList.get(index));
				}
				updatePrefStatus(prefs);
			}
		});
	}

	private void delayedInit(final SharedPreferences prefs) {
		prefFrameRate.setEnabled(false);
		prefFrameSize.setEnabled(false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				setFrameRates(prefs);
				setFrameSizes(prefs);
			}
		}).start();
	}

	private void setCameras(SharedPreferences prefs) {
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		int cameraCount = Camera.getNumberOfCameras();
		String[] camList = new String[cameraCount];
		String[] camListInd = new String[cameraCount];

		for (int i = 0; i < cameraCount; ++i) {
			Camera.getCameraInfo(i, cameraInfo);
			String item = "Camera " + i + " (";
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
				item += "front";
			else
				item += "back";
			item += ")";
			camList[i] = item;
			camListInd[i] = String.valueOf(i);
		}
		prefCamera.setEntries(camList);
		prefCamera.setEntryValues(camListInd);
		int camId = RecSettings.getInteger(prefs, "pref_camera", 0);
		if (camId < cameraCount)
			prefCamera.setSummary(camList[camId]);
	}

	private void updatePrefStatus(SharedPreferences prefs) {
		if (prefFrameRate.getEntries() == null || prefFrameRate.getEntries().length == 0) {
			prefFrameRate.setSummary(null);
			prefFrameRate.setEnabled(false);
		}
		if (prefFrameSize.getEntries() == null || prefFrameSize.getEntries().length == 0) {
			prefFrameSize.setSummary(null);
			prefFrameSize.setEnabled(false);
		} else {
			prefFrameSize.setEnabled(true);
		}
		if (prefCamera.getEntries() == null || prefCamera.getEntries().length == 0) {
			prefCamera.setSummary(null);
			prefCamera.setEnabled(false);
		}
	}

	private String formatTime(int millis) {
		if (millis < 1000)
			return millis + " ms";
		double secs = ((double) (millis % 60000)) / 1000;
		if (millis >= 1000 && millis < 60000)
			return new DecimalFormat("#.##").format(secs) + " secs";
		int intSecs = millis % 60000 / 1000;
		int mins = (millis % 3600000) / 1000 / 60;
		int hours = (millis / 1000 / 60 / 60);
		String res = "";
		if (hours > 0) res += hours + "h ";
		if (mins > 0) res += mins + "min ";
		if (intSecs > 0) res += intSecs + "s";
		return res.trim();
	}

	@Override
	public String onFormatOutputValue(int value, String suffix) {
		if ("min".equals(suffix)) {
			if (value >= 47 * 60) return "Infinite";
			return formatTime(value * 1000 * 60);
		}
		return null;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals("pref_camera")) {
			CharSequence entry = prefCamera.getEntry();
			if (entry != null) prefCamera.setSummary(entry);
			setFrameRates(prefs);
			setFrameSizes(prefs);
			setExposureCompRange(prefs);
			setZoomRange(prefs);
			prefs.edit().putInt("pref_exposurecomp", 0).apply();
		} else if (key.equals("pref_frame_size")) {
			prefFrameSize.setSummary(prefFrameSize.getEntry());
		} else if (key.equals("pref_frame_rate")) {
			prefFrameRate.setSummary(prefFrameRate.getEntry());
		} else if (key.equals("pref_stop_recording_after")) {
			prefStopRecAfter.setSummary(onFormatOutputValue(prefStopRecAfter.getmValue(), "min"));
		} else if (key.equals("pref_exposurecomp")) {
			prefExposureComp.setSummary(Integer.toString(prefExposureComp.getmValue()));
		} else if (key.equals("pref_zoom")) {
			prefZoom.setSummary(Integer.toString(prefZoom.getmValue()));
		} else if (key.equals("pref_video_encoding_br")) {
			if (RecSettings.getInteger(prefs, "pref_video_encoding_br", 0) == 0) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.remove("pref_video_encoding_br");
				editor.commit();
				prefVideoEncodingBitRate.setText("");
			}
			prefVideoEncodingBitRate.setSummary(RecSettings.getInteger(prefs, "pref_video_encoding_br", 0) == 0 ? context.getString(R.string.encode_best) : context.getString(R.string.format_bps, prefs.getString("pref_video_encoding_br", "0")));
		}
		updatePrefStatus(prefs);
	}

	public void onCreate(Context context, PreferenceScreen screen) {
		this.context = context;
		SharedPreferences prefs = screen.getSharedPreferences();
		cameraSettings = new CameraSettings();
		cameraSettings.prefetch(prefs);
		prefVideoEncodingBitRate = (EditTextPreference) screen.findPreference("pref_video_encoding_br");
		prefFrameSize = (ListPreference) screen.findPreference("pref_frame_size");
		prefFrameRate = (ListPreference) screen.findPreference("pref_frame_rate");
		prefCamera = (ListPreference) screen.findPreference("pref_camera");
		prefStopRecAfter = (SeekBarPreference) screen.findPreference("pref_stop_recording_after");
		prefExposureComp = (SeekBarPreference) screen.findPreference("pref_exposurecomp");
		prefZoom = (SeekBarPreference) screen.findPreference("pref_zoom");

		setZoomRange(prefs);
		setExposureCompRange(prefs);
		setCameras(prefs);
		delayedInit(prefs);

		prefStopRecAfter.setOnFormatOutputValueListener(this);
		int value = prefs.getInt("pref_stop_recording_after", -1);
		if (value != -1)
			prefStopRecAfter.setSummary(onFormatOutputValue(value, "min"));
		prefVideoEncodingBitRate.setSummary(RecSettings.getInteger(prefs, "pref_video_encoding_br", 0) == 0 ? context.getString(R.string.encode_best) : context.getString(R.string.format_bps, prefs.getString("pref_video_encoding_br", "0")));
		prefExposureComp.setSummary(Integer.toString(prefExposureComp.getmValue()));
		prefZoom.setSummary(Integer.toString(prefZoom.getmValue()));

		updatePrefStatus(prefs);
	}

	private void setZoomRange(SharedPreferences prefs) {
		int camId = RecSettings.getInteger(prefs, "pref_camera", 0);
		prefZoom.setMinValue(0);
		prefZoom.setMaxValue(cameraSettings.getMaxZoom(camId));
	}

	private void setExposureCompRange(SharedPreferences prefs) {
		int camId = RecSettings.getInteger(prefs, "pref_camera", 0);
		prefExposureComp.setMinValue(cameraSettings.getMinExposureCompensation(camId));
		prefExposureComp.setMaxValue(cameraSettings.getMaxExposureCompensation(camId));
	}

	public void onResume(PreferenceScreen screen) {
		screen.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	public void onPause(PreferenceScreen screen) {
		screen.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	static public void setDefaultValues(Context context, SharedPreferences prefs) {
		PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
	}
}
