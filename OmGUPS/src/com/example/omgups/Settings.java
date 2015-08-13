package com.example.omgups;

import android.R.color;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
/**Фрагмент - лист с элементами настроек
 */
public class Settings extends Fragment{

	String[] items;
	String item_list;
	FragmentTransaction ft;
	FrameLayout cont;
	SharedPreferences sPref;
	Object item;
	int lastPosition;
	AsyncTask<MenuItem, Void, Boolean> glt;
	ListView settints;
	Boolean reload = false;
	MenuItem refreshItem;
	Fragment groups;


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		setRetainInstance(true); //Запрет на пересоздание объекта
		// Определение элементов
		View view = inflater.inflate(R.layout.fragment_settings, null);	
		items = getResources().getStringArray(R.array.settings_array);
		settints = (ListView)view.findViewById(R.id.settingsView);
		if (getResources().getConfiguration().orientation ==
				Configuration.ORIENTATION_LANDSCAPE) {
			cont = (FrameLayout)view.findViewById(R.id.frgmCont);
			if (cont != null && !reload) {
				ft = getFragmentManager().beginTransaction();
				ft.add(R.id.frgmCont, new SecondFragment()).commit();
				reload = true;
			}
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.settings_item, items);
		settints.setAdapter(adapter);

		groups = new MainGroupFragment();
		//При тыке на пункт меню
		settints.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0: //Нулевая позиция - выбор групп.					
					sPref = getActivity().getSharedPreferences("item_list", Context.MODE_PRIVATE);
					if(!sPref.contains("DEPARTMENTS")) { //Если не существует ключ групп (если не существует файл)
						refreshItem.setActionView(R.layout.actionbar_progress);
						refreshItem.setVisible(true);
						//выполнить task и заполнить
						if (isNetworkConnected(getActivity())) {
							glt = new GetListTask(getActivity()); //Пересоздание для избежания вылетов
							glt.execute(refreshItem);
							// Здесь возможна вторая реализация: заблокировать UI, дождаться ответа сервера, вывести списки. Сейчаз загрузка в фоне, далее необходимо опять нажать на кнопку
							//							try {
							//								if (glt.get(7, TimeUnit.SECONDS)) {}
							//							} catch (Exception e) {
							//								e.printStackTrace();
							//							}
						}
						else {
							Toast.makeText(getActivity(), "Не удалось получить списки" + '\n'
									+ "Проверьте соединение с интернетом", Toast.LENGTH_LONG).show();
						}
					}
					if (sPref.contains("DEPARTMENTS")) { //если выполнено успешно или ключ существует
						settints.setBackgroundColor(Color.WHITE); //перекрасить выделенный элемент
						settints.getChildAt(lastPosition).setBackgroundColor(color.white);
						lastPosition = position;
						if (getResources().getConfiguration().orientation ==
								Configuration.ORIENTATION_LANDSCAPE) {
							settints.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.schedule_light));
						}
						//Запустить активность/фрагмент для выбора группы
						if (getResources().getConfiguration().orientation ==
								Configuration.ORIENTATION_PORTRAIT) {
							Intent intent = new Intent(getActivity(), MainGroup.class);
							startActivity(intent);		
						}
						else {
							ft = getFragmentManager().beginTransaction();
							ft.replace(R.id.frgmCont, groups).commit();
						}
					}
					break;
				case 1: //позиция 1 - ???
					settints.setBackgroundColor(Color.WHITE); //перекрасить выделенный элемент
					settints.getChildAt(lastPosition).setBackgroundColor(color.white);
					lastPosition = position;
					if (getResources().getConfiguration().orientation ==
							Configuration.ORIENTATION_LANDSCAPE) {
						settints.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.schedule_light));
					}
					break;
				case 2: //позиция 2 - настройки синхронизации
					settints.setBackgroundColor(Color.WHITE); //перекрасить выделенный элемент
					settints.getChildAt(lastPosition).setBackgroundColor(color.white);
					lastPosition = position;
					if (getResources().getConfiguration().orientation ==
							Configuration.ORIENTATION_LANDSCAPE) {
						settints.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.schedule_light));
					}
					if (getResources().getConfiguration().orientation ==
							Configuration.ORIENTATION_PORTRAIT) {
						Intent intent = new Intent(getActivity(), Timing.class);
						startActivity(intent);		
					}
					else {
						ft = getFragmentManager().beginTransaction();
						ft.replace(R.id.frgmCont, new TimingFragment()).commit();
					}
					break;
				case 3: //позиция 3 - настройки визуализации (как отображать)
					settints.setBackgroundColor(Color.WHITE); //перекрасить выделенный элемент
					settints.getChildAt(lastPosition).setBackgroundColor(color.white);
					lastPosition = position;
					if (getResources().getConfiguration().orientation ==
							Configuration.ORIENTATION_LANDSCAPE) {
						settints.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.schedule_light));
					}
					if (getResources().getConfiguration().orientation ==
							Configuration.ORIENTATION_PORTRAIT) {
						Intent intent = new Intent(getActivity(), Vitalization.class);
						startActivity(intent);		
					}
					else {
						ft = getFragmentManager().beginTransaction();
						ft.replace(R.id.frgmCont, new VitalizationFragment()).commit();
					}
				default:
					break;
				}
			}
		});
		return view;
	}

	public boolean isNetworkConnected(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.settings, menu);
		refreshItem = (MenuItem) menu.findItem(R.id.download_pb);
		refreshItem.setActionView(R.layout.actionbar_progress);
		refreshItem.setVisible(false);
		super.onCreateOptionsMenu(menu, inflater);
	}



	protected String pullList () {
		try {	
			//Достать список из xml
			sPref = getActivity().getSharedPreferences("item_list", Context.MODE_PRIVATE);
			if(sPref.contains("list")) { //если нужная строка существует

				item_list = sPref.getString("list", "");
			}
			else {
				return "";
			}
			return item_list;
		} catch (Exception e) {
			//Если не достается
			return "";
		}
	}	

	@Override
	public void onDestroy() {
		if (glt != null)
			glt.cancel(false);
		ft = getFragmentManager().beginTransaction(); //удаление фрагмента со списками групп, если был запущен. очищает actionBar от его элементов, запускает сохранение данных
		ft.remove(groups).commit();
		super.onDestroy();
	}
}