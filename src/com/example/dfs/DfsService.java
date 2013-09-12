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

public class DfsService extends Service{

	 private static final String VOLTAGE_FILES =  "/sys/class/power_supply/battery/voltage_now";
	 private static final String POWERBIAS_FILES =  "/sys/devices/system/cpu/cpufreq/ondemand/powersave_bias";
	 private static final String THRESHOLD_FILES =  "/sys/devices/system/cpu/cpufreq/ondemand/up_threshold";
	 private static final String MAXFREQ_FILES =  "/sys/devices/system/cpu/cpufreq/scaling_max_freq";
	 private static final String CURFREQ_FILES =  "/sys/devices/system/cpu/cpufreq/scaling_cur_freq";
	
	 String voltageFile;
	 String powerBiasFile;
	 String thresholdFile;
	 String maxFreqFile;
	 String curFreqFile;
	 
	 Boolean serviceFlag = true;
	 double B = 0.9;
	 double a = 0.1;
	 static long Ua1;
	 long Ua2;
	 long Uave;
	 long res;
	 Timer myTimer = null;
	 
	 
		long maxFreq; 
		long curFreq;
		long thres;
		long currUsage;
		float temper;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		if(new File(VOLTAGE_FILES).exists()) voltageFile = VOLTAGE_FILES;         
        if(new File(POWERBIAS_FILES).exists()) powerBiasFile = POWERBIAS_FILES;         
        if(new File(THRESHOLD_FILES).exists())thresholdFile = THRESHOLD_FILES;
        if(new File(MAXFREQ_FILES).exists()) maxFreqFile = MAXFREQ_FILES;
        if(new File(CURFREQ_FILES).exists()) curFreqFile = CURFREQ_FILES;       
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "Service ended", Toast.LENGTH_LONG).show();
		myTimer.cancel();
		serviceFlag = false;
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();	
		Ua1 = 100;
        myTimer = new Timer();
        myTimer.scheduleAtFixedRate(
            new TimerTask()
             {
                        @Override
                        public void run()
                        {
                    		maxFreq=  DfsActivity.readInput(maxFreqFile);
                    		curFreq =  DfsActivity.readInput(curFreqFile);
                    		thres =  DfsActivity.readInput(thresholdFile);
                    		currUsage= (long) DfsActivity.readUsage();
                    		temper = 0;
                    		Ua2 = (long) ((long)  (a*Ua1+B*currUsage)/(B+a));
                    		Ua1 =Ua2;
                    		Uave = (maxFreq*Ua2 )/curFreq;
                    		if(Uave < thres ){
                    			temper = (float)(thres -Uave)/thres;
                    			res  = (long) ((float)temper*1000);
                    		} else{res = 0;}
                    		try {
                    			File root = new File(powerBiasFile)	;
                    			if (root.canWrite()){
                    				FileWriter gpxwriter = new FileWriter(root);
                    				BufferedWriter out = new BufferedWriter(gpxwriter);
                    				out.write(String.valueOf(res));
                    				out.close();
                    			}
                    		} catch (IOException e) {}			
                        }
             },10,10);

        serviceFlag = true;
		return START_STICKY;
	}
	
}
