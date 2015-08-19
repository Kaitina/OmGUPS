package com.example.omgups;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.omgups.Parsers.PAIR;

public class CalendarScheduleFragment extends Fragment implements OnClickListener {

	SharedPreferences sPref;
	Button login, settings, update;
	GetListTask glt = null;
	LinearLayout ll, detailll;
	CheckBox lecture, practice, lab, ksr;
	//	GridView odd, even;
	ListView pairs;
	TextView num, type, auditory, time, teacher, name;
	String[] data;
	//	CalendarScheduleAdapter adapterOdd;
	//	CalendarScheduleAdapter adapterEven; 

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		sPref = getActivity().getSharedPreferences("groups", Context.MODE_PRIVATE);
		View view = null;
		setRetainInstance(true);
		if (sPref.contains("main_group") || sPref.contains("set")) {
			if (sPref.contains(sPref.getString("main_group", "") + "main") || sPref.contains("set")) {
				view = inflater.inflate(R.layout.calendar_schedule_fragment, null); //Если данные существуют, вывести фрагмент с листом
				ll = (LinearLayout)view.findViewById(R.id.filtersLayout);
				detailll = (LinearLayout)view.findViewById(R.id.detailll);
				lecture = (CheckBox)view.findViewById(R.id.lecture);
				practice = (CheckBox)view.findViewById(R.id.practice);
				lab = (CheckBox)view.findViewById(R.id.lab);
				ksr = (CheckBox)view.findViewById(R.id.ksr);
				pairs = (ListView)view.findViewById(R.id.pairs);
				//				odd = (GridView)view.findViewById(R.id.odd);
				//				even = (GridView)view.findViewById(R.id.even);
				num = (TextView)view.findViewById(R.id.detailNumber);
				type = (TextView)view.findViewById(R.id.detailType);
				auditory = (TextView)view.findViewById(R.id.detailAuditory);
				time = (TextView)view.findViewById(R.id.detailTime);
				teacher = (TextView)view.findViewById(R.id.detailTeacher);
				name = (TextView)view.findViewById(R.id.detailName);
				if (getActivity().getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).getBoolean("filter_visible", false)) {
					ll.setVisibility(View.GONE);
				}
				if (getActivity().getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).getBoolean("detail_visible", false)) {
					detailll.setVisibility(View.GONE);
				}

				//заполнение основной сетки ифнормацией в соответствии с настройками
				android.app.FragmentManager fragmentManager = getFragmentManager();						
//				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && //Если по текущей ориентации в настройках установлено расширенное расписание
//						getActivity().getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).getBoolean("full_horisontal", false) == true
//						|| getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT &&
//						getActivity().getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).getBoolean("full_vertical", false) == true) {
//					fragmentManager.beginTransaction().replace(R.id.calendar_container, new CalendarFragment()).commit();
//				} else {
					fragmentManager.beginTransaction().replace(R.id.calendar_container, new CalendarShortFragment()).commit();
//				}
				//				try {
				//					parseGroups(sPref.getString("set", ""), sPref.getString("main_group", ""));
				//				} catch (JSONException e) {
				//					e.printStackTrace();
				//				}
				//				odd.setAdapter(adapterOdd);
				//				even.setAdapter(adapterEven);
			}
			else {
				view = inflater.inflate(R.layout.null_schedule_fragment, null); //Если данные не существуют, вывести информацию
				login = (Button)view.findViewById(R.id.log_id);
				settings = (Button)view.findViewById(R.id.set_id);
				update = (Button)view.findViewById(R.id.upd_id);
				login.setOnClickListener(this);
				settings.setOnClickListener(this);
				update.setOnClickListener(this);
			}
		}
		else {
			view = inflater.inflate(R.layout.null_schedule_fragment, null);
			login = (Button)view.findViewById(R.id.log_id);
			settings = (Button)view.findViewById(R.id.set_id);
			update = (Button)view.findViewById(R.id.upd_id);
			login.setOnClickListener(this);
			settings.setOnClickListener(this);
			update.setOnClickListener(this);
		}
		setHasOptionsMenu(true);
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.log_id:
			Toast.makeText(getActivity(), "А нет авторизации", Toast.LENGTH_SHORT).show();
			// тут должна быть авторизация... когда-нибудь
			// Вызов активности авторизации
			break;
		case R.id.set_id:
			// переход к окну настройки
			sPref = getActivity().getSharedPreferences("item_list", Context.MODE_PRIVATE);
			if(!sPref.contains("DEPARTMENTS")) { //Если не существует ключ групп (если не существует файл)
				//выполнить task и заполнить
				if (SideBar.isNetworkConnected(getActivity())) {
					glt = new GetListTask(getActivity()); //Пересоздание для избежания вылетов
					glt.execute();
				}
				else {
					Toast.makeText(getActivity(), "Не удалось получить списки" + '\n'
							+ "Проверьте соединение с интернетом", Toast.LENGTH_LONG).show();
				}

				try {
					if (glt.get(7, TimeUnit.SECONDS) || sPref.contains("DEPARTMENTS")) {
						Intent intent = new Intent(getActivity(), MainGroup.class);
						startActivity(intent);
					}
				} catch (InterruptedException | ExecutionException | TimeoutException e1) {
					e1.printStackTrace();
				}
			} else {
				Intent intent = new Intent(getActivity(), MainGroup.class);
				startActivity(intent);
			}
			break;
		case R.id.upd_id:
			// обновить окно
			sPref = getActivity().getSharedPreferences("groups", Context.MODE_PRIVATE);
			if (sPref.contains("main_group")) {
				if (sPref.contains(sPref.getString("main_group", "") + "main")) {
					android.app.FragmentManager fragmentManager = getFragmentManager();		
					fragmentManager.beginTransaction()
					.replace(R.id.container, new CalendarScheduleFragment()).commit();
				}
			}
			break;
		}
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.calendar_schedule, menu);

//		if (sPref.contains("list")) {
//			Set<String> list = sPref.getStringSet("list", new HashSet<String>());
//			final String[] data = list.toArray(new String[list.size()]);
//			String group = null;
//			if (sPref.contains(sPref.getString("main_group", "") + "main") || sPref.contains("set")) {
//				group = sPref.getString("set",  sPref.getString("main_group", "Группа"));
//			}
//			if (sPref.contains("list")) {
//				SubMenu subMenuGroup = menu.addSubMenu(Menu.NONE, 100, Menu.NONE, group);
//				subMenuGroup.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
//				for (int i = 0; i < list.size(); i++) {
//					subMenuGroup.add(Menu.NONE, 101+i, Menu.NONE, data[i]);
//				}
//			}
//		}
		
		String group = null;
		if (sPref.contains(sPref.getString("main_group", "") + "main") || sPref.contains("set")) {
			group = sPref.getString("set",  sPref.getString("main_group", "Группа"));
			
		}
		if (sPref.contains("list")) {
			Set<String> list = sPref.getStringSet("list", new HashSet<String>());
			data = list.toArray(new String[list.size()]);

			SubMenu subMenuGroup = menu.addSubMenu(Menu.NONE, 100, Menu.NONE, group);
			subMenuGroup.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			for (int i = 0; i < list.size(); i++) {
				subMenuGroup.add(Menu.NONE, 101+i, Menu.NONE, data[i]);
			}

		}		
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.cs_filters:
			if (ll.getVisibility() == View.GONE) {
				ll.setVisibility(View.VISIBLE);
				getActivity().getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).edit().putBoolean("filter_visible", false).apply();
			} else {
				ll.setVisibility(View.GONE);
				getActivity().getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).edit().putBoolean("filter_visible", true).apply();
			}
			break;
		case R.id.cs_detail:
			if (detailll.getVisibility() == View.GONE) {
				detailll.setVisibility(View.VISIBLE);
				getActivity().getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).edit().putBoolean("detail_visible", false).apply();
			} else {
				detailll.setVisibility(View.GONE);
				getActivity().getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).edit().putBoolean("detail_visible", true).apply();
			}
			break;
		case 100: break;
		default:
			Editor ed = sPref.edit();
			ed.putString("set", data[item.getItemId()-101]).apply();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.container, new CalendarScheduleFragment()).commit();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	//	private void parseGroups(String set, String main) throws JSONException {	 //Установка расписания. Если есть элемент set, то его расписание, иначе основное
	//		
	//		String str = new String(); //Основное расписание
	//		str = set.isEmpty() ?  new String (sPref.getString(sPref.getString("main_group", "") + "main", "")) : 
	//			new String (sPref.getString(sPref.getString("set", "") + "main", ""));
	//		JSONObject obj = new JSONObject(str);
	//		String group = set.isEmpty() ?  new String (sPref.getString("main_group", "")) : new String (sPref.getString("set", "")); //определяем, группа ли для забора параметров
	//		Boolean isGroup; 
	//		try {
	//			Integer.parseInt(group.charAt(0) + "");
	//			isGroup = true; //проверка пройдена - первый символ число, объъект группа
	//		} catch (NumberFormatException e) {
	//			isGroup = false; //проверка не пройдена, первый символ не число, объект - преподаватель
	//		}
	//		ArrayList<ShModel> listOdd = new ArrayList<ShModel>(); //Основной лист для записывания данных
	//		ArrayList<ShModel> listEven = new ArrayList<ShModel>();
	//		Iterator<String> iterator = obj.keys();
	//		int iOdd = 0, iEven = 0;
	//		while (iterator.hasNext()) {
	//			String key = iterator.next();
	//			String date = new String();	
	//			switch (key.replace("ODD_", "").replace("EVEN_", "")) {
	//			case "MONDAY":
	//				date = "ПН"; break;
	//			case "TUESDAY":
	//				date = "ВТ"; break;
	//			case "WEDNESDAY":
	//				date = "СР"; break;
	//			case "THURSDAY":
	//				date = "ЧТ"; break;
	//			case "FRIDAY":
	//				date = "ПТ"; break;
	//			case "SATURDAY":
	//				date = "СБ"; break;
	//			}
	//			//			Log.d("11",key.next().toString());
	//			ArrayList<PAIR> dayList = PAIR.fromJson(obj.getJSONArray(key), isGroup);
	//			ArrayList<Integer> nums = new ArrayList<Integer>();
	//			for (int i = 0; i < dayList.size(); i++) { //Оформление расписания для конкретного дня				
	//				nums.add(dayList.get(i).PAIR_NUMBER);
	//				String teacher = new String();				
	//				teacher = dayList.get(i).NAME;
	//				for (int j = 1; j < dayList.size() - i; j++) { //Цикл для преподавателей: чтобы не дублировались пары
	//					if (dayList.get(i).PAIR_NUMBER == dayList.get(i + j).PAIR_NUMBER && //Если совпадают данные о номерах сравниваемых пар
	//							dayList.get(i).DISCIPLINE.equals(dayList.get(i + j).DISCIPLINE)) { //И их названиях						
	//						teacher += ", " + dayList.get(i + j).NAME; //Дописать группу к прошлой паре
	//					} else {
	//						//Если нет - пара другая, писать отдельно, но с той пары, которая отличается
	//						i += j - 1;
	//						break;
	//					}
	//				}
	//
	//				String time = new String();	
	//				switch (dayList.get(i).PAIR_NUMBER) {
	//				case 1 : time = "8:00 - 9:35"; break;
	//				case 2 : time = "9:45 - 11:20"; break;
	//				case 3 : time = "11:30 - 13:05"; break;
	//				case 4 : time = "13:55 - 15:30"; break;
	//				case 5 : time = "15:40 - 17:15"; break;
	//				}
	//
	//				
	//				//				String dayP = Integer.toString(calendar.get(Calendar.DATE));
	//				//				if (dayP.length() == 1) {
	//				//					dayP = "0" + dayP;
	//				//				}
	//				//				String monthP = Integer.toString(calendar.get(Calendar.MONTH)+1);
	//				//				if (monthP.length() == 1) {
	//				//					monthP = "0" + monthP;
	//				//				}
	//				//				if (i == 0) { //Для первой записи так же установить дату, внести в карту для определения элемента
	//				//					date = dayP + "." + monthP + "." + calendar.get(Calendar.YEAR) + ", " + day1;
	//				//					if (!hm.containsKey(day)) {
	//				//						hm.put(day, list.size());
	//				//					}
	//				//				}
	//				
	//				
	//				String discipline = dayList.get(i).DISCIPLINE;
	//				if (!dayList.get(i).SUBGROUP.equals("0")) {
	//					discipline += ", подгруппа " + dayList.get(i).SUBGROUP; 
	//				}
	//				ShModel item = new ShModel(Integer.toString(dayList.get(i).PAIR_NUMBER),
	//						time,
	//						discipline,
	//						teacher,
	//						dayList.get(i).CLASSROOM,
	//						dayList.get(i).DISCIPLINE_TYPE,
	//						date);
	//				if (key.contains("ODD")) {
	//					listOdd.add(item);
	//				} else {
	//					listEven.add(item);
	//				}
	//
	//			}
	//			ShModel nul1 = new ShModel("0","","","","","",date); //заглушка-пустая пара для дня недели
	//			if (key.contains("ODD")) {						
	//				listOdd.add(iOdd * 6, nul1);
	//			} else {
	//				listEven.add(iEven * 6, nul1);
	//			}
	//			
	//			for (int k = 1; k < 6; k++) { //заполнение дня заглушками
	//				ShModel nul2 = new ShModel("","","","","","",""); //заглушка-пустая пара
	//				if (!nums.contains(k)) {
	//					nul2.setN(Integer.toString(k));
	//					if (key.contains("ODD")) {						
	//						listOdd.add(iOdd * 6 + k, nul2);
	//					} else {
	//						listEven.add(iEven * 6 + k, nul2);
	//					}
	//				}
	//			}
	//			if (key.contains("ODD")) {						
	//				iOdd++;
	//			} else {
	//				iEven++;
	//			}
	//
	//		}
	//		//		ArrayList<String> keys = (ArrayList<String>) obj.keys();
	//		//		Log.d("11", keys.toString());
	//		adapterOdd = new CalendarScheduleAdapter(getActivity(), listOdd);
	//		adapterEven = new CalendarScheduleAdapter(getActivity(), listEven);
	//
	//	}


	public class CalendarFragment extends Fragment {

	}

	public class CalendarShortFragment extends Fragment {

		TableRow[] days;
		TextView[][] pairs;

		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			sPref = getActivity().getSharedPreferences("groups", Context.MODE_PRIVATE);
			View view = inflater.inflate(R.layout.calendar_short_table, null);
			String iters[] = {"ODD_MONDAY","ODD_TUESDAY","ODD_WEDNESDAY","ODD_THURSDAY","ODD_FRIDAY","ODD_SATURDAY",
					"EVEN_MONDAY","EVEN_TUESDAY","EVEN_WEDNESDAY","EVEN_THURSDAY","EVEN_FRIDAY","EVEN_SATURDAY"};
			days = new TableRow[]{(TableRow)view.findViewById(R.id.OddMonRow), (TableRow)view.findViewById(R.id.OddTueRow),
					(TableRow)view.findViewById(R.id.OddWedRow), (TableRow)view.findViewById(R.id.OddThuRow),
					(TableRow)view.findViewById(R.id.OddFriRow), (TableRow)view.findViewById(R.id.OddSatRow),
					(TableRow)view.findViewById(R.id.EvenMonRow), (TableRow)view.findViewById(R.id.EvenTueRow),
					(TableRow)view.findViewById(R.id.EvenWedRow), (TableRow)view.findViewById(R.id.EvenThuRow),
					(TableRow)view.findViewById(R.id.EvenFriRow), (TableRow)view.findViewById(R.id.EvenSatRow)};
			pairs = new TextView[][]{{(TextView)view.findViewById(R.id.OddMon1), (TextView)view.findViewById(R.id.OddMon2),
				(TextView)view.findViewById(R.id.OddMon3), (TextView)view.findViewById(R.id.OddMon4), (TextView)view.findViewById(R.id.OddMon5)}, 
				{(TextView)view.findViewById(R.id.OddTue1), (TextView)view.findViewById(R.id.OddTue2),
					(TextView)view.findViewById(R.id.OddTue3), (TextView)view.findViewById(R.id.OddTue4), (TextView)view.findViewById(R.id.OddTue5)},
					{(TextView)view.findViewById(R.id.OddWed1), (TextView)view.findViewById(R.id.OddWed2),
						(TextView)view.findViewById(R.id.OddWed3), (TextView)view.findViewById(R.id.OddWed4), (TextView)view.findViewById(R.id.OddWed5)},
						{(TextView)view.findViewById(R.id.OddThu1), (TextView)view.findViewById(R.id.OddThu2),
							(TextView)view.findViewById(R.id.OddThu3), (TextView)view.findViewById(R.id.OddThu4), (TextView)view.findViewById(R.id.OddThu5)},
							{(TextView)view.findViewById(R.id.OddFri1), (TextView)view.findViewById(R.id.OddFri2),
								(TextView)view.findViewById(R.id.OddFri3), (TextView)view.findViewById(R.id.OddFri4), (TextView)view.findViewById(R.id.OddFri5)},
								{(TextView)view.findViewById(R.id.OddSat1), (TextView)view.findViewById(R.id.OddSat2),
									(TextView)view.findViewById(R.id.OddSat3), (TextView)view.findViewById(R.id.OddSat4), (TextView)view.findViewById(R.id.OddSat5)},
									{(TextView)view.findViewById(R.id.EvenMon1), (TextView)view.findViewById(R.id.EvenMon2),
										(TextView)view.findViewById(R.id.EvenMon3), (TextView)view.findViewById(R.id.EvenMon4), (TextView)view.findViewById(R.id.EvenMon5)}, 
										{(TextView)view.findViewById(R.id.EvenTue1), (TextView)view.findViewById(R.id.EvenTue2),
											(TextView)view.findViewById(R.id.EvenTue3), (TextView)view.findViewById(R.id.EvenTue4), (TextView)view.findViewById(R.id.EvenTue5)},
											{(TextView)view.findViewById(R.id.EvenWed1), (TextView)view.findViewById(R.id.EvenWed2),
												(TextView)view.findViewById(R.id.EvenWed3), (TextView)view.findViewById(R.id.EvenWed4), (TextView)view.findViewById(R.id.EvenWed5)},
												{(TextView)view.findViewById(R.id.EvenThu1), (TextView)view.findViewById(R.id.EvenThu2),
													(TextView)view.findViewById(R.id.EvenThu3), (TextView)view.findViewById(R.id.EvenThu4), (TextView)view.findViewById(R.id.EvenThu5)},
													{(TextView)view.findViewById(R.id.EvenFri1), (TextView)view.findViewById(R.id.EvenFri2),
														(TextView)view.findViewById(R.id.EvenFri3), (TextView)view.findViewById(R.id.EvenFri4), (TextView)view.findViewById(R.id.EvenFri5)},
														{(TextView)view.findViewById(R.id.EvenSat1), (TextView)view.findViewById(R.id.EvenSat2),
															(TextView)view.findViewById(R.id.EvenSat3), (TextView)view.findViewById(R.id.EvenSat4), (TextView)view.findViewById(R.id.EvenSat5)}
			};

			String str = new String(); //Основное расписание
			str = sPref.getString("set", "").isEmpty() ?  new String (sPref.getString(sPref.getString("main_group", "") + "main", "")) : 
				new String (sPref.getString(sPref.getString("set", "") + "main", ""));
			try {
				JSONObject obj = new JSONObject(str);

				String group = sPref.getString("set", "").isEmpty() ?  new String (sPref.getString("main_group", "")) : new String (sPref.getString("set", "")); //определяем, группа ли для забора параметров
				Boolean isGroup; 
				try {
					Integer.parseInt(group.charAt(0) + "");
					isGroup = true; //проверка пройдена - первый символ число, объъект группа
				} catch (NumberFormatException e) {
					isGroup = false; //проверка не пройдена, первый символ не число, объект - преподаватель
				}
				int light[] = {getActivity().getResources().getColor(R.color.light1), getActivity().getResources().getColor(R.color.light2),
						getActivity().getResources().getColor(R.color.light3), getActivity().getResources().getColor(R.color.light4),
						getActivity().getResources().getColor(R.color.light5), getActivity().getResources().getColor(R.color.light6),
						getActivity().getResources().getColor(R.color.light7), getActivity().getResources().getColor(R.color.light8),
						getActivity().getResources().getColor(R.color.light9), getActivity().getResources().getColor(R.color.light10),
						getActivity().getResources().getColor(R.color.light11), getActivity().getResources().getColor(R.color.light12),
						getActivity().getResources().getColor(R.color.light13), getActivity().getResources().getColor(R.color.light14),
						getActivity().getResources().getColor(R.color.light15), getActivity().getResources().getColor(R.color.light16),
						getActivity().getResources().getColor(R.color.light17), getActivity().getResources().getColor(R.color.light18),
						getActivity().getResources().getColor(R.color.light19), getActivity().getResources().getColor(R.color.light20),
						getActivity().getResources().getColor(R.color.light21), getActivity().getResources().getColor(R.color.light22),
						getActivity().getResources().getColor(R.color.light23), getActivity().getResources().getColor(R.color.light24)};
				Iterator<String> iterator = obj.keys();
				int x = 0, y = 0;
				while (iterator.hasNext()) {
					String key = iterator.next();						
					while (!key.equals(iters[x])) {
						days[x].setVisibility(View.GONE);
						x++;
					}

					ArrayList<PAIR> dayList = PAIR.fromJson(obj.getJSONArray(key), isGroup);
					for (int i = 0; i < dayList.size(); i++) {
						switch (getActivity().getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).getString("calendar", ""))
						{ //определение метода раскрашивания
						case "radio0": //по предмету								
							ArrayList<String> namesArray = new ArrayList<String>(sPref.getStringSet("pair_names", new LinkedHashSet<String>()));
							pairs[y][dayList.get(i).PAIR_NUMBER - 1].setBackgroundColor(light[namesArray.indexOf(dayList.get(i).DISCIPLINE)]);
							break;
						case "radio1": //по типу предмета
							ArrayList<String> typeArray = new ArrayList<String>();
							typeArray.add("Лекция");
							typeArray.add("Практика");
							typeArray.add("Лабораторная");
							typeArray.add("КСР");
							if (typeArray.contains(dayList.get(i).DISCIPLINE_TYPE)) {
								pairs[y][dayList.get(i).PAIR_NUMBER - 1].setBackgroundColor(light[typeArray.indexOf(dayList.get(i).DISCIPLINE_TYPE)]);
							} else {
								pairs[y][dayList.get(i).PAIR_NUMBER - 1].setBackgroundColor(light[4]);
							}
							break;
						case "radio2": //по преподавателю
							ArrayList<String> teachersArray = new ArrayList<String>(sPref.getStringSet("pair_teachers", new LinkedHashSet<String>()));
							pairs[y][dayList.get(i).PAIR_NUMBER - 1].setBackgroundColor(light[teachersArray.indexOf(dayList.get(i).NAME)]);
							break;
						default:
							pairs[y][dayList.get(i).PAIR_NUMBER - 1].setBackground(getActivity().getResources().getDrawable(R.drawable.drow));
							break;

						}
						if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && //Если по текущей ориентации в настройках установлено расширенное расписание
								getActivity().getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).getBoolean("full_horisontal", false) == true
								|| getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT &&
								getActivity().getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).getBoolean("full_vertical", false) == true) {
							pairs[y][dayList.get(i).PAIR_NUMBER - 1].setText(dayList.get(i).DISCIPLINE);
							pairs[y][dayList.get(i).PAIR_NUMBER - 1].setTextSize(15);
							pairs[y][dayList.get(i).PAIR_NUMBER - 1].setMaxWidth(getResources().getDisplayMetrics().widthPixels/5);
							pairs[y][dayList.get(i).PAIR_NUMBER - 1].setPadding(5, 0, 5, 0);
							pairs[y][dayList.get(i).PAIR_NUMBER - 1].setGravity(Gravity.CENTER);
							pairs[y][dayList.get(i).PAIR_NUMBER - 1].setMaxWidth(getResources().getDisplayMetrics().heightPixels/20);
						}

					}
					//			ArrayList<Integer> nums = new ArrayList<Integer>();
					//			for (int i = 0; i < dayList.size(); i++) { //Оформление расписания для конкретного дня				
					//				nums.add(dayList.get(i).PAIR_NUMBER);
					//				String teacher = new String();				
					//				teacher = dayList.get(i).NAME;
					//				for (int j = 1; j < dayList.size() - i; j++) { //Цикл для преподавателей: чтобы не дублировались пары
					//					if (dayList.get(i).PAIR_NUMBER == dayList.get(i + j).PAIR_NUMBER && //Если совпадают данные о номерах сравниваемых пар
					//							dayList.get(i).DISCIPLINE.equals(dayList.get(i + j).DISCIPLINE)) { //И их названиях						
					//						teacher += ", " + dayList.get(i + j).NAME; //Дописать группу к прошлой паре
					//					} else {
					//						//Если нет - пара другая, писать отдельно, но с той пары, которая отличается
					//						i += j - 1;
					//						break;
					//					}
					//				}
					//
					//				String time = new String();	
					//				switch (dayList.get(i).PAIR_NUMBER) {
					//				case 1 : time = "8:00 - 9:35"; break;
					//				case 2 : time = "9:45 - 11:20"; break;
					//				case 3 : time = "11:30 - 13:05"; break;
					//				case 4 : time = "13:55 - 15:30"; break;
					//				case 5 : time = "15:40 - 17:15"; break;
					//				}
					//
					//				
					//				//				String dayP = Integer.toString(calendar.get(Calendar.DATE));
					//				//				if (dayP.length() == 1) {
					//				//					dayP = "0" + dayP;
					//				//				}
					//				//				String monthP = Integer.toString(calendar.get(Calendar.MONTH)+1);
					//				//				if (monthP.length() == 1) {
					//				//					monthP = "0" + monthP;
					//				//				}
					//				//				if (i == 0) { //Для первой записи так же установить дату, внести в карту для определения элемента
					//				//					date = dayP + "." + monthP + "." + calendar.get(Calendar.YEAR) + ", " + day1;
					//				//					if (!hm.containsKey(day)) {
					//				//						hm.put(day, list.size());
					//				//					}
					//				//				}
					//				
					//				
					//				String discipline = dayList.get(i).DISCIPLINE;
					//				if (!dayList.get(i).SUBGROUP.equals("0")) {
					//					discipline += ", подгруппа " + dayList.get(i).SUBGROUP; 
					//				}
					//				ShModel item = new ShModel(Integer.toString(dayList.get(i).PAIR_NUMBER),
					//						time,
					//						discipline,
					//						teacher,
					//						dayList.get(i).CLASSROOM,
					//						dayList.get(i).DISCIPLINE_TYPE,
					//						date);
					//				if (key.contains("ODD")) {
					//					listOdd.add(item);
					//				} else {
					//					listEven.add(item);
					//				}
					//
					//			}
					//			ShModel nul1 = new ShModel("0","","","","","",date); //заглушка-пустая пара для дня недели
					//			if (key.contains("ODD")) {						
					//				listOdd.add(iOdd * 6, nul1);
					//			} else {
					//				listEven.add(iEven * 6, nul1);
					//			}
					//			
					//			for (int k = 1; k < 6; k++) { //заполнение дня заглушками
					//				ShModel nul2 = new ShModel("","","","","","",""); //заглушка-пустая пара
					//				if (!nums.contains(k)) {
					//					nul2.setN(Integer.toString(k));
					//					if (key.contains("ODD")) {						
					//						listOdd.add(iOdd * 6 + k, nul2);
					//					} else {
					//						listEven.add(iEven * 6 + k, nul2);
					//					}
					//				}
					//			}
					//			if (key.contains("ODD")) {						
					//				iOdd++;
					//			} else {
					//				iEven++;
					//			}
					//
					x++; y++;
				}

			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			setRetainInstance(true);
			return view;
		}

	}


}
