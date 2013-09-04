package com.example.dfs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class DfsService extends Service {

	private double B = 0.5;
	private double a = 0.5;
	private static long Ua1;
	private long Ua2;
	private long Uave;
	private long res;
	private Timer myTimer = null;

	private long maxFreq;
	private long curFreq;
	private long threshold;
	private long currUsage;
	private float temper;

	@Override
	public IBinder onBind(Intent i) {
		return null;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "CPU Monitoring Ended", Toast.LENGTH_LONG).show();
		myTimer.cancel();
		try {
			File powerbiasFile = new File(MainActivity.POWERBIAS_FILE);
			BufferedWriter out = new BufferedWriter(new FileWriter(
					powerbiasFile));
			out.write("0");
			out.close();
		} catch (IOException e) {}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "CPU Monitoring Started", Toast.LENGTH_LONG)
				.show();
		Ua1 = 100;
		maxFreq = MainActivity.readInput(MainActivity.MAXFREQ_FILE);
		myTimer = new Timer();
		myTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				maxFreq = MainActivity.readInput(MainActivity.MAXFREQ_FILE);
				curFreq = MainActivity.readInput(MainActivity.CURFREQ_FILE);
				threshold = MainActivity.readInput(MainActivity.THRESHOLD_FILE);
				currUsage = (long) MainActivity.readUsage();
				temper = 0;
				Ua2 = (long) ((long) ((a * Ua1) + (B * currUsage)) / (B + a));
				Ua1 = Ua2;
				Uave = (maxFreq * Ua2) / curFreq;
				if (Uave < threshold) {
					temper = (float) (threshold - Uave) / threshold;
					res = (long) (temper * 1000F);
				} else
					res = 0;
				try {
					File powerbiasFile = new File(MainActivity.POWERBIAS_FILE);
					BufferedWriter out = new BufferedWriter(new FileWriter(
							powerbiasFile));
					out.write(String.valueOf(res));
					out.close();
				} catch (Exception e) {}
			}
		}, 1000, 100);
		// TODO where does START_STICKY come from?
		return START_STICKY;
	}
}
