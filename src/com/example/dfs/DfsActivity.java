package com.example.dfs;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Timer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DfsActivity extends Activity {
	
	 private static final String VOLTAGE_FILES =  "/sys/class/power_supply/battery/voltage_now";
	 private static final String POWERBIAS_FILES =  "/sys/devices/system/cpu/cpu0/cpufreq/ondemand/powersave_bias";
	 private static final String THRESHOLD_FILES =  "/sys/devices/system/cpu/cpu0/cpufreq/ondemand/up_threshold";
	 private static final String MAXFREQ_FILES =  "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
	 private static final String CURFREQ_FILES =  "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
	 private static final String CURRENT_FILES =  "/sys/class/power_supply/battery/current_now";

	 

	 String TAG = "SystemInfo";
	 String voltageFile;
	 String powerBiasFile;
	 String thresholdFile;
	 String maxFreqFile;
	 String curFreqFile;
	 String currentFile;

	 Timer powerTime;
	 
	 Button buttonStart, buttonStop;
	TextView voltage;
	TextView voltageLab;
	TextView powerBias;
	TextView powerBiasLab;
	TextView current;
	TextView currentLab;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        voltageLab = (TextView) findViewById(R.id.voltageLabel);
        voltageLab.setText("voltage:");
        voltage = (TextView) findViewById(R.id.voltage);
        
        currentLab = (TextView) findViewById(R.id.currentLab);
        currentLab.setText("current:");
        current = (TextView) findViewById(R.id.current);
        
        powerBiasLab = (TextView) findViewById(R.id.powerBiasLab);
        powerBiasLab.setText("PowerSave_bias:");
        
        
        powerBias = (TextView) findViewById(R.id.powerBias);
        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);

        buttonStart.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startService(new Intent(DfsActivity.this, DfsService.class));			
				Thread thread = new Thread( new Runnable() {
				    public void run() {
				        while(true) {
				            runOnUiThread( new Runnable() {
				                public void run() {
				                	TextView powerBias;
				                    powerBias = (TextView) findViewById(R.id.powerBias);
				                    powerBias.setText(String.valueOf(readInput(powerBiasFile)));				               
				                }			    
				            });
				            try {
				                Thread.sleep(1000);
				            } catch (InterruptedException e) {}
				        }
				    }
				});

				thread.start();
			}
			
        });
        buttonStop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				 stopService(new Intent(DfsActivity.this, DfsService.class));
				 try {
         			File root = new File(powerBiasFile)	;
         			if (root.canWrite()){
         				FileWriter gpxwriter = new FileWriter(root);
         				BufferedWriter out = new BufferedWriter(gpxwriter);
         				out.write(String.valueOf(0));
         				out.close();
         			}
         		} catch (IOException e) {
         		}
			}
        });
        ProcessBuilder cmd;
        try{
            String[] args = {"chmod", "777", "/sys/devices/system/cpu/cpu0/cpufreq/ondemand/powersave_bias"};
            cmd = new ProcessBuilder(args);
            cmd.start();
           } catch(IOException ex){
            ex.printStackTrace();
           }
           
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
        if(new File(VOLTAGE_FILES).exists()) voltageFile = VOLTAGE_FILES;
        if(new File(CURRENT_FILES).exists()) currentFile = CURRENT_FILES;         

        if(new File(POWERBIAS_FILES).exists()) powerBiasFile = POWERBIAS_FILES;         
        if(new File(THRESHOLD_FILES).exists())thresholdFile = THRESHOLD_FILES;
        if(new File(MAXFREQ_FILES).exists()) maxFreqFile = MAXFREQ_FILES;
        if(new File(CURFREQ_FILES).exists()) curFreqFile = CURFREQ_FILES;
        if(currentFile != null)current.setText(String.valueOf(readInput(currentFile)));
        voltage.setText(String.valueOf(readInput(voltageFile)));
        powerBias.setText(String.valueOf(readInput(powerBiasFile)));

    }
    
    
    public static long readInput(String file){
    	long val = 1;
    	
    	 try {
 			FileInputStream fin = new FileInputStream(file);
 			DataInputStream din = new DataInputStream(fin);
 			String l = din.readLine();
 			val = Long.valueOf(l);
 			din.close();

 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
    	return val;
    }
    
    public long getVoltage() {
        if(voltageFile == null) return -1;
        long volt = 1;
        return volt;
    }
    
    static float readUsage() {
//      private int readUsage() {

        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" ");

            long idle1 = Long.parseLong(toks[5]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                  + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {}

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" ");

            long idle2 = Long.parseLong(toks[5]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            float ret = (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));
            ret = (float) (ret*100.0);
            return ret;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    } 
}