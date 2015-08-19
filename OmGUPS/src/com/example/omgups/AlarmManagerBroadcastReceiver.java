package com.example.omgups;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;

/** Широковещательный приемник для автосинхронизации с сервером
 * Ловит события от UpdateService (постоянно через n времени)
 * или SideBar(для разового принудительного запуска)
 * Запускает task для обновления. При обновлении выводит уведомление
 */

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

	Context context;
	int count; //Переехал из определения пришедших данных. в зависимости от числа оповещения в различных случаях

	@Override
	public void onReceive(Context context, Intent intent) {
		AsyncTask<MenuItem, Void, Boolean> glt;
		AsyncTask<String, Void, Integer> gsht = null;
		this.context = context;
		SharedPreferences sPref;
		sPref = context.getSharedPreferences("groups", Context.MODE_PRIVATE);
		if (SideBar.isNetworkConnected(context)) {
			glt = new GetListTask(context);
			glt.execute(); //Выполняем запрос за получение полного листа	
			Set<String> listId = sPref.getStringSet("listId", new HashSet<String>());
			if (!listId.toString().equals("[]")) { //Заглушка на случай, если списка групп нет
				String str[] = new String[listId.size()]; //Сюда все сохраненные группы
				int i = 0;
				for(String l : listId) {
					str[i] = l; //Заполняем массив всех айдишников групп для передачи
					i++;

				}
				gsht = new GetScheduleTask(context);
				gsht.execute(str); //Выполняем запрос на получение нужных расписаний
			}
			try {
				if (glt.get(7, TimeUnit.SECONDS)) {
					sendGlobalNotif();
				}
				if (!listId.toString().equals("[]")) {
					count = gsht.get();
					if (count > 0) {
						sendScheduleNotif();
					}
				}
				Log.d("11", "смена");
				context.sendBroadcast(new Intent("FINISH_UPDATE")); //Окончание обновления. Широковещательное сообщение для sidebar's progressbar

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void sendGlobalNotif() { //Уведомляет о закачке изменений в структуре
		Intent notificationIntent = new Intent(context, SideBar.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context,
				0, notificationIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(context);
		builder.setContentIntent(contentIntent)
		.setSmallIcon(R.drawable.ic_launcher)
		.setTicker("Загружены изменения")
		.setAutoCancel(true)
		.setContentTitle("Загружены изменения")
		.setContentText("Изменен список групп или преподавателей")
		.setWhen(System.currentTimeMillis());
		Notification n = builder.build();
		nm.notify(101, n);
	}

	void sendScheduleNotif() {//Уведомляет о закачке изменений в расписании
		//Заметка о настраиваемых уведомлениях
		//Count: 1 - изменения есть, 0 - изменений нет
		//1 число - основное расписание в активной группе
		//2 число - основное расписание в дополнительной группе
		//3 число - модификации в активной группе
		//4 число - модификации расписание в дополнительной группе
		Intent notificationIntent = new Intent(context, SideBar.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context,
				0, notificationIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(context);
		builder.setContentIntent(contentIntent)
		.setSmallIcon(R.drawable.ic_launcher)
		.setTicker("Загружены изменения")
		.setWhen(System.currentTimeMillis())
		.setAutoCancel(true)
		.setContentTitle("Загружены изменения")
		.setContentText("Изменилось рассписание одной из загруженных групп");
		Notification n = builder.build();
		nm.notify(101, n);
	}
}

