package com.example.omgups;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SideBar extends Activity {

	private DrawerLayout myDrawerLayout;
	private ListView myDrawerList;
	private ActionBarDrawerToggle myDrawerToggle;

	private CharSequence myDrawerTitle;
	private CharSequence myTitle;

	private String[] viewsNames;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_side_bar);	

		myTitle =  getTitle();	
		myDrawerTitle = getResources().getString(R.string.menu);

		viewsNames = getResources().getStringArray(R.array.views_array);
		myDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		myDrawerList = (ListView) findViewById(R.id.left_drawer);

//		myDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, viewsNames));
		myDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, viewsNames));

		android.app.ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);   

		myDrawerToggle = new ActionBarDrawerToggle(this, myDrawerLayout,
				R.drawable.ic_drawer, //nav menu toggle icon
				R.string.app_name, // nav drawer open - description for accessibility
				R.string.app_name // nav drawer close - description for accessibility
				){
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(myTitle);
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(myDrawerTitle);
				invalidateOptionsMenu();
			}
		};
		myDrawerLayout.setDrawerListener(myDrawerToggle);

		if (savedInstanceState == null) {
			displayView(1);
		}
		myDrawerList.setOnItemClickListener(new DrawerItemClickListener());


		//		Стартуем сервис, если он не запущен
		boolean tStartService = true;
		ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
		java.util.List<RunningServiceInfo> rs = am.getRunningServices(100);
		for (int i=0; i<rs.size(); i++) { 
			ActivityManager.RunningServiceInfo rsi = rs.get(i);
			if(UpdateService.class.getName().equalsIgnoreCase(rsi.service.getClassName())){
				tStartService = false;
				break;
			}        	
		}         
		if(tStartService){
			startService(new Intent(getApplicationContext(), UpdateService.class));
		}

		//Проверяем обновления, если в настройках стоит "при запуске"
		SharedPreferences sPref = getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE);
		String timing = sPref.getString("timing_date", "");
		if (timing.equals("ever") && SideBar.isNetworkConnected(getApplicationContext())) {
			Intent intent = new Intent(this, AlarmManagerBroadcastReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);
		}
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(
				AdapterView<?> parent, View view, int position, long id
				) {
			displayView(position);
		}               
	}

	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {
		case 0: //Окно обновления. Запустить полное обновление
			Intent intent = new Intent(this, AlarmManagerBroadcastReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);
			break;
		case 1: //Окно подробного расписания
			fragment = new DailyScheduleFragment();
			break;
		case 2: //Окно расписания по типу календарь
			fragment = new CalendarScheduleFragment();
			break;
		case 3: //Окно перехода в настройки
			fragment = new Settings();
			break;
		case 4: //Окно авторизации.. Когда-нибудь
			//Тут должен быть вызов активности авторизации
			break;
		default:
			break;
		}

		if (fragment != null) {
			android.app.FragmentManager fragmentManager = getFragmentManager();			
			fragmentManager.beginTransaction()
			.replace(R.id.content_frame, fragment).commit();

			myDrawerList.setItemChecked(position, true);
			myDrawerList.setSelection(position);
			setTitle(viewsNames[position]);
			myDrawerLayout.closeDrawer(myDrawerList);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.global, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (myDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
//		case R.id.action_settings:
//			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void setTitle(CharSequence title) {
		myTitle = title;
		getActionBar().setTitle(myTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		myDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		myDrawerToggle.onConfigurationChanged(newConfig);
	}

	public static boolean isNetworkConnected(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}
	
	@Override
	public void onDestroy() {
		SharedPreferences sPref = getSharedPreferences("groups", Context.MODE_PRIVATE);
		sPref.edit().remove("set").apply(); //Запуск снова с главой группы
		super.onDestroy();
	}


}