package com.gara.cam.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.CamcorderProfile;
import android.os.Environment;

import static android.os.Environment.DIRECTORY_MOVIES;

public class RecSettings {
	private int cameraId;
	private String projectName;
	private String projectPath;
	private int frameRate;
	private boolean muteShutter;
	private boolean stopOnLowBattery;
	private boolean stopOnLowStorage;
	private int frameWidth;
	private int frameHeight;
	private int recProfile;
	private int stopRecAfter;
	private int exposureCompensation;
	private int zoom;
	private int videoEncodingBitRate;

	public static int getInteger(SharedPreferences prefs, String key, int def) {
		try {
			return Integer.parseInt(prefs.getString(key, ""));
		} catch (NumberFormatException e) {}
		return def;
	}

	private boolean checkRecProfile(int profile) {
		try {
			if (CamcorderProfile.hasProfile(cameraId, profile)) {
				return true;
			}
		} catch (Exception e) {}
		return false;
	}

	private int selectRecVideoProfile() {
		if (checkRecProfile(CamcorderProfile.QUALITY_1080P)
				&& frameHeight == 1080)
			return CamcorderProfile.QUALITY_1080P;
		if (checkRecProfile(CamcorderProfile.QUALITY_720P)
				&& frameHeight == 720)
			return CamcorderProfile.QUALITY_720P;
		if (checkRecProfile(CamcorderProfile.QUALITY_480P)
				&& frameHeight == 480)
			return CamcorderProfile.QUALITY_480P;

		if (checkRecProfile(CamcorderProfile.QUALITY_HIGH)
				&& frameHeight >= 480)
			return CamcorderProfile.QUALITY_HIGH;

		if (checkRecProfile(CamcorderProfile.QUALITY_QVGA)
				&& frameHeight == 240)
			return CamcorderProfile.QUALITY_QVGA;

		if (checkRecProfile(CamcorderProfile.QUALITY_QCIF)
				&& frameHeight == 144)
			return CamcorderProfile.QUALITY_QCIF;

		if (checkRecProfile(CamcorderProfile.QUALITY_LOW))
			return CamcorderProfile.QUALITY_LOW;

		return CamcorderProfile.QUALITY_HIGH;
	}

	public void load(Context context, SharedPreferences prefs) {
		videoEncodingBitRate = getInteger(prefs,"pref_video_encoding_br", 0);
		cameraId = getInteger(prefs, "pref_camera", 0);
		projectName = prefs.getString("pref_project_title", "");
		projectPath = Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES).getPath();

		frameRate = getInteger(prefs, "pref_frame_rate", 30);
		muteShutter = prefs.getBoolean("pref_mute_shutter", true);
		stopOnLowBattery = prefs.getBoolean("pref_stop_low_battery", true);
		stopOnLowStorage = prefs.getBoolean("pref_stop_low_storage", true);
		stopRecAfter = prefs.getInt("pref_stop_recording_after", 60 * 48);
		exposureCompensation = prefs.getInt("pref_exposurecomp",0);
		zoom = prefs.getInt("pref_zoom",0);
		if (stopRecAfter >= 47 * 60)
			stopRecAfter = -1;
		else
			stopRecAfter *= 60 * 1000;

		String[] size = prefs.getString("pref_frame_size", "1920x1080").split("x");
		try {
			frameWidth = Integer.parseInt(size[0]);
			frameHeight = Integer.parseInt(size[1]);
		} catch (NumberFormatException e) {
			frameWidth = 1920;
			frameHeight = 1080;
		}

		recProfile = selectRecVideoProfile();
	}

	public int getCameraId() {
		return cameraId;
	}

	public void setCameraId(int cameraId) {
		this.cameraId = cameraId;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public int getFrameRate() {
		return frameRate;
	}

	public boolean isMuteShutter() {
		return muteShutter;
	}

	public int getFrameWidth() {
		return frameWidth;
	}

	public int getFrameHeight() {
		return frameHeight;
	}

	public int getRecProfile() {
		return recProfile;
	}

	public boolean isStopOnLowBattery() {
		return stopOnLowBattery;
	}

	public boolean isStopOnLowStorage() {
		return stopOnLowStorage;
	}

	public int getStopRecAfter() {
		return stopRecAfter;
	}

	public int getExposureCompensation() { return exposureCompensation; }

	public int getZoom() { return zoom;}

	public int getVideoEncodingBitRate() {return  videoEncodingBitRate;}
}
