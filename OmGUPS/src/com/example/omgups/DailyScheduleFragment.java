package com.example.omgups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.omgups.Parsers.PAIR;

public class DailyScheduleFragment extends Fragment implements OnClickListener {
	/** 
	 * Класс, отображающий расписание на определенный срок в виде списка предметов с параметрами
	 */
	SharedPreferences sPref;
	ListView lw;
	ScheduleAdapter adapter;
	Button login, settings, update;
	GetListTask glt = null;	
	Map<String, Integer> hm = new HashMap<String, Integer>();
	String[] data;


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		sPref = getActivity().getSharedPreferences("groups", Context.MODE_PRIVATE);
		View view = null;
		boolean data = false;
		if (sPref.contains("main_group") || sPref.contains("set") || sPref.contains("list")) {
			if (sPref.contains(sPref.getString("main_group", "") + "main") || sPref.contains("set")) {
				try {
					view = inflater.inflate(R.layout.daily_schedule_fragment, null); //Если данные существуют, вывести фрагмент с листом
					lw = (ListView)view.findViewById(R.id.schedule);					
					parseGroups(sPref.getString("set", ""), sPref.getString("main_group", ""));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				lw.setAdapter(adapter);
			}
			else {
				Set<String> hs = sPref.getStringSet("list", new HashSet<String>()); //блок для вывода хоть какой-то группы, если нет ни главной, ни следующей активной
				String set[] = hs.toArray(new String [hs.size()]);
				for (int i = 0; i < hs.size(); i++) {
					if (sPref.contains(set[i] + "main")) {
						try {
							view = inflater.inflate(R.layout.daily_schedule_fragment, null); //Если данные существуют, вывести фрагмент с листом
							lw = (ListView)view.findViewById(R.id.schedule);					
							parseGroups("", set[i]);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						lw.setAdapter(adapter);
						data = true;
						break;
					}
				}
				if (!data) {
				view = inflater.inflate(R.layout.null_schedule_fragment, null); //Если данные не существуют, вывести информацию
				login = (Button)view.findViewById(R.id.log_id);
				settings = (Button)view.findViewById(R.id.set_id);
				update = (Button)view.findViewById(R.id.upd_id);
				login.setOnClickListener(this);
				settings.setOnClickListener(this);
				update.setOnClickListener(this);
				}
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

	//	private void focus() {
	//нерабочая наркомания в попытке высчитать нужную позицию
	//		GregorianCalendar calendar = new GregorianCalendar();
	//		String day = new String();
	//		switch (calendar.get(Calendar.WEEK_OF_YEAR) % 2) {
	//		case 0: //Если неделя четная по календарю, она нечетная по расписанию
	//			day = "нечет";
	//			break;
	//		case 1:
	//			day = "четн";
	//		}
	//		int cal = calendar.get(Calendar.DAY_OF_WEEK) - 2;
	//		Log.d("11", "1 " + day + " " + navWeek + " " + navDay + " " + cal);
	//		int position = 0;
	//		if (navWeek == 1 && day.contains("нечет") || navWeek == 2 && day.contains("четн")) { //Если совпадает тип текущей и выбранной недели
	//			if (cal == navDay) { //Если дни так же совпадают, отмотать к первому элементу
	//				Log.d("11", "2 ");
	//				position = 0;
	//				lw.setSelection(0);
	//			}
	//			if (cal < navDay && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) { //Если текущий день раньше выбранного (воскресенье - позже)
	//				//нужный элемент находится в промежутке от 1 до 5, выбранный день в ближайшее время
	//				Log.d("11", "3 ");
	//			}
	//			if (cal > navDay || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) { //Если текущий день позже выбранного
	//				if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
	//					Log.d("11", "4 ");
	//					position += (12 - cal - navDay);
	//				}else {
	//					Log.d("11", "5 ");
	//					position += (12 - cal + navDay); //нужный элемент довольно далеко, через неделю
	//				}
	//			}
	//		} else { //Если не совпадают
	//			if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) { //Если сейчас воскресенье
	//				Log.d("11", "6 " + calendar.get(Calendar.DAY_OF_WEEK));
	//				position += navDay;
	//			} else {
	//				Log.d("11", "7 " + calendar.get(Calendar.DAY_OF_WEEK));
	//				position += (6 + navDay - calendar.get(Calendar.DAY_OF_WEEK));
	//			}			
	//		}
	//		Log.d("11", "8 " + position);

	//		navWeek = -1;
	//		navDay = -1;
	//	}

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
					.replace(R.id.container, new DailyScheduleFragment()).commit();
				}
			}
			break;
		}
	}

	private void parseGroups(String set, String main) throws JSONException {	 //Установка расписания. Если есть элемент set, то его расписание, иначе основное
		//Определение дня для вывода информации
		GregorianCalendar calendar = new GregorianCalendar();
		String day = new String();
		String day1 = new String();
		String str = new String(); //Основное расписание
		str = set.isEmpty() ?  new String (sPref.getString(main + "main", "")) : 
			new String (sPref.getString(set + "main", ""));
		JSONObject obj = new JSONObject(str);
		//		String str1 = new String(); //Модификации
		//		str1 = set.isEmpty() ?  new String (sPref.getString(sPref.getString("main_group", "") + "mod", "")) : 
		//			new String (sPref.getString(sPref.getString("set", "") + "mod", ""));
		//		JSONObject mod = new JSONObject(str1);
		calendar.add(Calendar.DAY_OF_YEAR, -1); //Чтобы заполнение начиналось с "сегодня"
		ArrayList<ShModel> list = new ArrayList<ShModel>(); //Основной лист для записывания данных
		for (int d = 0; d < 30; d++) { //Цикл для 30 дней для отображения расписания
			calendar.add(Calendar.DAY_OF_YEAR, 1); //Заполнение для каждого последующего дня
			switch (calendar.get(Calendar.WEEK_OF_YEAR) % 2) {
			case 0: //Если неделя четная по календарю, она нечетная по расписанию
				day = "ODD_";
				break;
			case 1:
				day = "EVEN_";
			}
			switch (calendar.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SUNDAY: //Если воскресенье - перейти на следующую итерацию цикла
				continue;
			case Calendar.MONDAY:
				day += "MONDAY"; //Для запроса
				day1 = "понедельник"; //Для вывода в поле "дата"
				break; 
			case Calendar.TUESDAY:
				day += "TUESDAY";
				day1 = "вторник";
				break; 
			case Calendar.WEDNESDAY:
				day += "WEDNESDAY";
				day1 = "среда";
				break; 
			case Calendar.THURSDAY:
				day += "THURSDAY";
				day1 = "четверг";
				break; 
			case Calendar.FRIDAY:
				day += "FRIDAY";
				day1 = "пятница";
				break; 
			case Calendar.SATURDAY:
				day += "SATURDAY";
				day1 = "суббота";
				break; 
			}
			//			int number; //Определение подлежащей
			//			if (mod.has(day)) { //Работа по определению небходимости модификации в дне
			//				ArrayList<PAIRMOD> dayPairs = PAIRMOD.fromJson(mod.getJSONArray(day));
			//				for (int m = 0; m < dayPairs.size(); m++) {
			//					if (calendar.after(dayPairs.get(m).begin) && calendar.before(dayPairs.get(m).end)) {
			//						number = dayPairs.get(m).PAIR_NUMBER;
			//					}
			//				}
			//			}
			if (!obj.has(day)) { //Если такого дня в расписании не существует - перейти на следующую итерацию
				continue;
			}
			String group = set.isEmpty() ?  main : set; //определяем, группа ли для забора параметров
			Boolean isGroup; 
			try {
				Integer.parseInt(group.charAt(0) + "");
				isGroup = true; //проверка пройдена - первый символ число, объъект группа
			} catch (NumberFormatException e) {
				isGroup = false; //проверка не пройдена, первый символ не число, объект - преподаватель		    	
			}

			ArrayList<PAIR> dayList = PAIR.fromJson(obj.getJSONArray(day), isGroup);
			// упаковываем данные в понятную для адаптера структуру
			for (int i = 0; i < dayList.size(); ++i) { //Оформление расписания для конкретного дня
				//				if (i > 0)
				//					if (!isGroup && (dayList.get(i).PAIR_NUMBER == Integer.parseInt(list.get(list.size()-1).getN()))) {
				//						continue;
				//					}

				String teacher = new String();				
				teacher = dayList.get(i).NAME;
				int skip = 0;
				for (int j = 1; j < dayList.size() - i; ++j) { //Цикл для преподавателей: чтобы не дублировались пары
					if (dayList.get(i).PAIR_NUMBER == dayList.get(i + j).PAIR_NUMBER && //Если совпадают данные о номерах сравниваемых пар
							dayList.get(i).DISCIPLINE.equals(dayList.get(i + j).DISCIPLINE)) { //И их названиях						
						teacher += ", " + dayList.get(i + j).NAME; //Дописать группу к прошлой паре
						skip++;
					} else {
						break;
					}					
				}

				String time = new String();	
				switch (dayList.get(i).PAIR_NUMBER) {
				case 1 : time = "8:00 - 9:35"; break;
				case 2 : time = "9:45 - 11:20"; break;
				case 3 : time = "11:30 - 13:05"; break;
				case 4 : time = "13:55 - 15:30"; break;
				case 5 : time = "15:40 - 17:15"; break;
				}

				String date = new String();	
				String dayP = Integer.toString(calendar.get(Calendar.DATE));
				if (dayP.length() == 1) {
					dayP = "0" + dayP;
				}
				String monthP = Integer.toString(calendar.get(Calendar.MONTH)+1);
				if (monthP.length() == 1) {
					monthP = "0" + monthP;
				}
				if (i == 0) { //Для первой записи так же установить дату, внести в карту для определения элемента
					date = dayP + "." + monthP + "." + calendar.get(Calendar.YEAR) + ", " + day1;
					if (!hm.containsKey(day)) {
						hm.put(day, list.size());
					}
				}
				String discipline = dayList.get(i).DISCIPLINE;
				if (!dayList.get(i).SUBGROUP.equals("0")) {
					discipline += ", подгруппа " + dayList.get(i).SUBGROUP; 
				}
				ShModel item = new ShModel(Integer.toString(dayList.get(i).PAIR_NUMBER),
						time,
						discipline,
						teacher,
						dayList.get(i).CLASSROOM,
						dayList.get(i).DISCIPLINE_TYPE,
						date);
				list.add(item);
				i+=skip;
			}
		}
		adapter = new ScheduleAdapter(getActivity(), list);
	}





	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.daily_schedule, menu);
		String group = null;
			group = sPref.getString("set",  sPref.getString("main_group", "Группа"));
		if (sPref.contains("list")) {
			Set<String> list = sPref.getStringSet("list", new HashSet<String>());
			data = list.toArray(new String[list.size()]);
			Arrays.sort(data); //Получение списка групп для вывода и сортировка

			SubMenu subMenuGroup = menu.addSubMenu(Menu.NONE, 100, 10, group);
			subMenuGroup.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			for (int i = 0; i < list.size(); i++) {
				subMenuGroup.add(Menu.NONE, 101+i, Menu.NONE, data[i]);
			}

		}		
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String key = null;
		switch (item.getItemId()) {
		case R.id.ds_today:
			lw.setSelection(0);
			break;
		case R.id.mon1:
			key = "ODD_MONDAY";
			break;
		case R.id.tue1:
			key = "ODD_TUESDAY";
			break;
		case R.id.wed1:
			key = "ODD_WEDNESDAY";
			break;
		case R.id.thu1:
			key = "ODD_THURSDAY";
			break;
		case R.id.fri1:
			key = "ODD_FRIDAY";
			break;
		case R.id.sat1:
			key = "ODD_SATURDAY";
			break;
		case R.id.mon2:
			key = "EVEN_MONDAY";
			break;
		case R.id.tue2:
			key = "EVEN_TUESDAY";
			break;
		case R.id.wed2:
			key = "EVEN_WEDNESDAY";
			break;
		case R.id.thu2:
			key = "EVEN_THURSDAY";
			break;
		case R.id.fri2:
			key = "EVEN_FRIDAY";
			break;
		case R.id.sat2:
			key = "EVEN_SATURDAY";
			break;
		case R.id.ds_navigation_1: break;
		case R.id.ds_navigation_2: break;
		case 100: break; //Для вывода подменю
		case 16908332: break; //Для вывода бокового меню
		default:
			Editor ed = sPref.edit();
			ed.putString("set", data[item.getItemId()-101]).apply();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.container, new DailyScheduleFragment()).commit();
			break;
		}
		if (key != null) {
			if (hm.containsKey(key)) {
				lw.setSelection(hm.get(key));
			} else {
				Toast.makeText(getActivity(), "Неучебный день", Toast.LENGTH_SHORT).show();;
			}
		}
		return super.onOptionsItemSelected(item);
	}

}
