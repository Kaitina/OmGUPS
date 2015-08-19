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

/** ����������������� �������� ��� ����������������� � ��������
 * ����� ������� �� UpdateService (��������� ����� n �������)
 * ��� SideBar(��� �������� ��������������� �������)
 * ��������� task ��� ����������. ��� ���������� ������� �����������
 */

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

	Context context;
	int count; //�������� �� ����������� ��������� ������. � ����������� �� ����� ���������� � ��������� �������

	@Override
	public void onReceive(Context context, Intent intent) {
		AsyncTask<MenuItem, Void, Boolean> glt;
		AsyncTask<String, Void, Integer> gsht = null;
		this.context = context;
		SharedPreferences sPref;
		sPref = context.getSharedPreferences("groups", Context.MODE_PRIVATE);
		if (SideBar.isNetworkConnected(context)) {
			glt = new GetListTask(context);
			glt.execute(); //��������� ������ �� ��������� ������� �����	
			Set<String> listId = sPref.getStringSet("listId", new HashSet<String>());
			if (!listId.toString().equals("[]")) { //�������� �� ������, ���� ������ ����� ���
				String str[] = new String[listId.size()]; //���� ��� ����������� ������
				int i = 0;
				for(String l : listId) {
					str[i] = l; //��������� ������ ���� ���������� ����� ��� ��������
					i++;

				}
				gsht = new GetScheduleTask(context);
				gsht.execute(str); //��������� ������ �� ��������� ������ ����������
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
				Log.d("11", "�����");
				context.sendBroadcast(new Intent("FINISH_UPDATE")); //��������� ����������. ����������������� ��������� ��� sidebar's progressbar

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void sendGlobalNotif() { //���������� � ������� ��������� � ���������
		Intent notificationIntent = new Intent(context, SideBar.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context,
				0, notificationIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(context);
		builder.setContentIntent(contentIntent)
		.setSmallIcon(R.drawable.ic_launcher)
		.setTicker("��������� ���������")
		.setAutoCancel(true)
		.setContentTitle("��������� ���������")
		.setContentText("������� ������ ����� ��� ��������������")
		.setWhen(System.currentTimeMillis());
		Notification n = builder.build();
		nm.notify(101, n);
	}

	void sendScheduleNotif() {//���������� � ������� ��������� � ����������
		//������� � ������������� ������������
		//Count: 1 - ��������� ����, 0 - ��������� ���
		//1 ����� - �������� ���������� � �������� ������
		//2 ����� - �������� ���������� � �������������� ������
		//3 ����� - ����������� � �������� ������
		//4 ����� - ����������� ���������� � �������������� ������
		Intent notificationIntent = new Intent(context, SideBar.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context,
				0, notificationIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(context);
		builder.setContentIntent(contentIntent)
		.setSmallIcon(R.drawable.ic_launcher)
		.setTicker("��������� ���������")
		.setWhen(System.currentTimeMillis())
		.setAutoCancel(true)
		.setContentTitle("��������� ���������")
		.setContentText("���������� ����������� ����� �� ����������� �����");
		Notification n = builder.build();
		nm.notify(101, n);
	}
}

