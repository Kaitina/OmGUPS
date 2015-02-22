package com.example.omgups;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

public class UpdateService extends Service {
	/** 
	 * —амозапускающийс€ сервис, всегда висит в процессах
	 * через указанные промежутки времени вызывает AlarmMenegerBroadcastReverse
	 */

	SharedPreferences sPref;
	NotificationManager nm; //ƒл€ показа уведомлений
	AlarmManager alarmManager;
	public static long INTERVAL;
	public static long alarmTime;
	public static boolean need = true;


	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		startService();
		return START_REDELIVER_INTENT;
	}

	private void startService() {
		firstRun();
		if (need) {
		Intent intent = new Intent(this, AlarmManagerBroadcastReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.setRepeating(
				AlarmManager.RTC_WAKEUP,
				alarmTime,
				INTERVAL,
				pendingIntent);	  
		}
	}

	private void firstRun() { //¬рем€ первого запуска, исход€ из настроек синхронизации и текущего времени
		sPref = getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE);
		if (!sPref.contains("timing_date") || sPref.getString("timing_date", "").equals("ever") || sPref.getString("timing_date", "").equals("nothing")) {
			need = false;
			return; //≈сли стоит "при запуске" или "никогда", или поле вообще не существует, обновление не осуществл€ть
		}
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, sPref.getInt("timing_h", 0));
		calendar.set(Calendar.MINUTE, sPref.getInt("timing_m", 0));
		calendar.set(Calendar.SECOND, 00);  //¬ыставить врем€ запуска
		switch (sPref.getString("timing_date", "")) {
		case "daily": //”становить первый запуск и интервал дл€ ежедневного
			alarmTime = calendar.getTimeInMillis();
			if (alarmTime < System.currentTimeMillis() + 500)
				alarmTime += 24*60*60*1000;
			INTERVAL = AlarmManager.INTERVAL_DAY;
			break;
		case "weekly":
			calendar.set(Calendar.DAY_OF_WEEK, 1);
			alarmTime = calendar.getTimeInMillis();
			if (alarmTime < System.currentTimeMillis() + 500)
				alarmTime += 24*60*60*1000*7;
			INTERVAL = AlarmManager.INTERVAL_DAY*7;
			break;
		case "monthly":
			Calendar c = Calendar.getInstance();
			int dayInMonth = 0;
			int month = c.getTime().getMonth();
			if (month == 30) {
				if (c.getTime().getYear() % 4 == 0) {
					dayInMonth = 29;
				}
				else dayInMonth = 28;
			}
			if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
				dayInMonth = 31;
			}
			if (month == 2 || month == 4 || month == 6 || month == 9 || month == 11) {
				dayInMonth = 30;
			}
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			alarmTime = calendar.getTimeInMillis();
			if (alarmTime < System.currentTimeMillis() + 500)
				alarmTime += 24*60*60*1000*dayInMonth;
			INTERVAL = AlarmManager.INTERVAL_DAY*dayInMonth;
			break;
		case "inAugust":
			calendar.set(Calendar.MONTH, Calendar.AUGUST);
			calendar.set(Calendar.DAY_OF_MONTH, 25);
			Calendar cal = Calendar.getInstance();
			int days = 365;
			if (cal.getTime().getYear() % 4 == 0) {
				days = 366;
			}
			alarmTime = calendar.getTimeInMillis();
			if (alarmTime < System.currentTimeMillis() + 500)
				alarmTime += 24*60*60*1000*days;
			INTERVAL = AlarmManager.INTERVAL_DAY*days;
			break;
		default:
			need = false;
			break;
		}
	}

}
