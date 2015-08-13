package com.example.omgups;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class SideBar extends ActionBarActivity implements
NavigationDrawerFragment.NavigationDrawerCallbacks {

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CharSequence mTitle;
	private String[] viewsNames;
	MenuItem refreshItem;
	boolean visible = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_side_bar);		
		registerReceiver(broadcastReceiver, new IntentFilter("FINISH_UPDATE"));

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
		viewsNames = getResources().getStringArray(R.array.views_array);

		if (savedInstanceState == null) {
			displayView(0);
		}

		getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.schedule_dark));


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
	
	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Приемник широковещательных запросов, а именно, запроса "конец обновления, скрыть progressbar"
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	refreshItem.setActionView(R.layout.actionbar_finish);
	    	new CountDownTimer(1000, 1000) {
				public void onTick(long millisUntilFinished) {}
				public void onFinish() { 
					refreshItem.setVisible(false);
				}
			}.start();
			visible = false;
	    }
	};


	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		displayView(position);
	}

	private void displayView(int position) {
		// update the main content by replacing fragments
		android.app.Fragment fragment = null;
		switch (position) {
		case 0: //Окно подробного расписания
			fragment = new DailyScheduleFragment();
			break;
		case 1: //Окно расписания по типу календарь
			fragment = new CalendarScheduleFragment();
			break;
		case 2: //Окно перехода в настройки
			fragment = new Settings();
			break;
		case 3: //Окно авторизации.. Когда-нибудь
			//Тут должен быть вызов активности авторизации
			break;
		case 4: //Окно обновления. Запустить полное обновление
			visible = true;
			refreshItem.setVisible(visible);
			Intent intent = new Intent(this, AlarmManagerBroadcastReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);
			break;
		default:
			break;
		}

		if (fragment != null) {
			android.app.FragmentManager fragmentManager = getFragmentManager();			
			fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();			
		}
	}

	public void onSectionAttached(int number) {
		mTitle = viewsNames[number];
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			getMenuInflater().inflate(R.menu.global, menu);
			refreshItem = (MenuItem) menu.findItem(R.id.download_pb);
			refreshItem.setActionView(R.layout.actionbar_progress);
			refreshItem.setVisible(visible);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		private static final String ARG_SECTION_NUMBER = "section_number";

		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_side_bar, container,
					false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((SideBar) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
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
		unregisterReceiver(broadcastReceiver);
		SharedPreferences sPref = getSharedPreferences("groups", Context.MODE_PRIVATE);
		sPref.edit().remove("set").apply(); //Запуск снова с главой группы
		super.onDestroy();
	}
	
}
