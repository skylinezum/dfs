package com.example.dfs;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	// file directories.
	protected static final String VOLTAGE_FILE = 
		"/sys/class/power_supply/battery/voltage_now";
	protected static final String POWERBIAS_FILE = 
		"/sys/devices/system/cpu/cpufreq/ondemand/powersave_bias";
	protected static final String THRESHOLD_FILE = 
		"/sys/devices/system/cpu/cpufreq/ondemand/up_threshold";
	protected static final String MAXFREQ_FILE = 
		"/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
	protected static final String CURFREQ_FILE = 
		"/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";

	// graphics stuff
	private Button buttonStart, buttonStop;
	private TextView voltage;
	private TextView voltageLab;
	private TextView powerBias;
	private TextView powerBiasLab;
	private TextView current;
	private TextView currentLab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if any of the files do not exist.
		if (!(new File(VOLTAGE_FILE).exists()
				&& new File(POWERBIAS_FILE).exists()
				&& new File(THRESHOLD_FILE).exists()
				&& new File(MAXFREQ_FILE).exists() 
				&& new File(CURFREQ_FILE).exists())) {
			// TODO let the user know that the files needed do not exist.
			
			// and gracefully exit.
			System.exit(0);
		}

		setContentView(R.layout.main);
		voltageLab = (TextView) findViewById(R.id.voltageLabel);
		voltageLab.setText("voltage:");
		voltage = (TextView) findViewById(R.id.voltage);

		powerBiasLab = (TextView) findViewById(R.id.powerBiasLab);
		powerBiasLab.setText("PowerSave_bias:");

		powerBias = (TextView) findViewById(R.id.powerBias);
		buttonStart = (Button) findViewById(R.id.buttonStart);
		buttonStop = (Button) findViewById(R.id.buttonStop);

		buttonStart.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startService(new Intent(MainActivity.this, DfsService.class));
			}
		});
		buttonStop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				stopService(new Intent(MainActivity.this, DfsService.class));

			}
		});
		try {
			String[] args = { "chmod", "777", POWERBIAS_FILE };
			ProcessBuilder cmd = new ProcessBuilder(args);
			cmd.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static long readInput(String file) {
		long val = 1;
		try {
			DataInputStream inputStream = new DataInputStream(
					new FileInputStream(file));
			val = Long.valueOf(inputStream.readLine());
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}

	static float readUsage() {
		try {
			RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
			String[] tokens = reader.readLine().split(" ");
			long startIdle = Long.parseLong(tokens[5]);
			long startCpu = Long.parseLong(tokens[2]) + Long.parseLong(tokens[3])
					+ Long.parseLong(tokens[4]) + Long.parseLong(tokens[6])
					+ Long.parseLong(tokens[7]) + Long.parseLong(tokens[8]);
			// TODO make this a timer instead of sleep due to weird thread stuff?
			// or simply this.wait(360); ?
			try {
				Thread.sleep(360);
			} catch (Exception ex) {}
			reader.seek(0);
			tokens = reader.readLine().split(" ");
			reader.close();
			
			long endIdle = Long.parseLong(tokens[5]);
			long endCpu = Long.parseLong(tokens[2]) + Long.parseLong(tokens[3])
					+ Long.parseLong(tokens[4]) + Long.parseLong(tokens[6])
					+ Long.parseLong(tokens[7]) + Long.parseLong(tokens[8]);
			return (float) 100 * ((endCpu - startCpu)
					/ ((endCpu + endIdle) - (startCpu + startIdle)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
