package com.example.omgups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.ListView;
import android.widget.Toast;

import com.example.omgups.Parsers.MODIFICATOR;
import com.example.omgups.Parsers.PAIR;

public class DailyScheduleFragment extends DialogFragment implements OnClickListener {
	/** 
	 * Класс, отображающий расписание на определенный срок в виде списка предметов с параметрами
	 */
	SharedPreferences sPref;
	ListView lw;
	ScheduleAdapter adapter;
	Button login, settings, update;
	GetListTask glt = null;	
	String[] data;
	String day1;
	GregorianCalendar calendar;
	String mDate;
	AlertDialog calendarDialog;
	Bundle sIS;


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		sIS = savedInstanceState;
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

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setCancelable(true);
		builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss(); // Отпускает диалоговое окно					
			}
		});
		final View dialog = getActivity().getLayoutInflater()
				.inflate(R.layout.calendar_dialog, null);
		AcademicalCalendar calendar = (AcademicalCalendar) dialog.findViewById(R.id.calendar);
		calendar.setOnDateChangeListener(new OnDateChangeListener() {

			@Override
			public void onSelectedDayChange(CalendarView view, int year,
					int month, int dayOfMonth) {
				int mYear = year;
				int mMonth = month;
				int mDay = dayOfMonth;
				String selectedDate = new StringBuilder().append(mMonth + 1)
						.append("-").append(mDay).append("-").append(mYear)
						.append(" ").toString();
				Toast.makeText(getActivity(), selectedDate, Toast.LENGTH_LONG).show();
				calendarDialog.dismiss();
			}
		});
		calendarDialog = builder.setView(dialog).create();
		return calendarDialog;
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
					.replace(R.id.container, new DailyScheduleFragment()).commit();
				}
			}
			break;
		}
	}


	private void parseGroups(String set, String main) throws JSONException {	 //Установка расписания. Если есть элемент set, то его расписание, иначе основное
		//Определение дня для вывода информации
		calendar = new GregorianCalendar();
		String group = set.isEmpty() ?  main : set; //определяем, группа ли для забора параметров
		Boolean isGroup; 
		try {
			Integer.parseInt(group.charAt(0) + "");
			isGroup = true; //проверка пройдена - первый символ число, объъект группа
		} catch (NumberFormatException e) {
			isGroup = false; //проверка не пройдена, первый символ не число, объект - преподаватель		    	
		}
		String day = new String();
		day1 = new String();
		String str = set.isEmpty() ?  new String (sPref.getString(main + "main", "")) : new String (sPref.getString(set + "main", ""));
		JSONObject obj = new JSONObject(str); //Основное расписание
		String  str1 = set.isEmpty() ?  new String (sPref.getString(main + "mod", "")) : new String (sPref.getString(set + "mod", ""));
		ArrayList<MODIFICATOR> mods = new ArrayList<MODIFICATOR>(); //Модификации
		if (!(str1.isEmpty() || str1.equals("null") || str1.equals("NO_UPDATES_AVAILABLE"))) {
			mods = MODIFICATOR.fromJson(new JSONArray(str1), isGroup); 
		}
		calendar.add(Calendar.DAY_OF_YEAR, -1); //Чтобы заполнение начиналось с "сегодня"
		ArrayList<ShModel> list = new ArrayList<ShModel>(); //Основной лист для записывания данных
		for (int d = 0; d < 30; d++) { //Цикл для 30 дней для отображения расписания
			calendar.add(Calendar.DAY_OF_YEAR, 1); //Заполнение для каждого последующего дня
			mDate = new String();	 //Определение даты для записи дня и для выделения модификаций
			String dayP = Integer.toString(calendar.get(Calendar.DATE));
			if (dayP.length() == 1) {
				dayP = "0" + dayP;
			}
			String monthP = Integer.toString(calendar.get(Calendar.MONTH)+1);
			if (monthP.length() == 1) {
				monthP = "0" + monthP;
			}
			mDate = dayP + "." + monthP + "." + calendar.get(Calendar.YEAR);
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

			ArrayList<PAIR> dayList = null;
			if (obj.has(day)) { //Если такого дня в расписании не существует - оставить поле нулевым
				dayList = PAIR.fromJson(obj.getJSONArray(day), isGroup);
			}
			ArrayList<PAIR> dayMods = null;
			for (int i = 0; i < mods.size(); i++) { //Если есть модификации - внести их, даже если нет дня в расписании
				if (mods.get(i).DATE.equals(mDate)) {
					dayMods = mods.get(i).MOD;
					continue;
				}
			}
			mDate += ", " + day1; //Дописать содержимое дня для вывода его
			ArrayList<ShModel> modSched = new ArrayList<ShModel>(); //Для модификаций
			ArrayList<ShModel> mainSched = new ArrayList<ShModel>(); //Для основного
			if (dayList != null) {//Если основное расписание есть, составить по нему лист
				mainSched = list(dayList, isGroup);
			}
			if (dayMods != null) {//Если доп расписание есть, составить по нему лист
				modSched = list(dayMods, isGroup);
			}

			int i = 0, j = 0;
			boolean firstPair = true;
			while (i < mainSched.size() && j < modSched.size()) {
				if (Integer.parseInt(mainSched.get(i).getN()) ==  Integer.parseInt(modSched.get(j).getN())) {
					//Если равны, есть пара в основном и в модификации
					if (modSched.get(j).isCancelled()) { //Возможно, пара отменена. Тогда ее не вносить в список (но внести, если отменена у препода не у всех)

						if (!isGroup && mainSched.get(i).getTeacher().length() == modSched.get(j).getTeacher().length() || isGroup) {
							//Если расписание для группы ИЛИ расписание для преподавателя, но длина списка групп совпадает, все хорошо. Не вносить пару
							i++; j++;
							continue;
						} else {
							//Если совпадения нет, внести только тех, кого нет в модификациях по отмене
							if (firstPair) { //Если это первая запись для дня, добавить в нее дату
								mainSched.get(i).setDate(mDate);
								firstPair = false;
							}
							ArrayList<String> mainGroups = new ArrayList<String>();
							int simbol = 0;
							for (int len = 0; len < (mainSched.get(i).getTeacher().length() - 4) / 6 + 1; len++) { //Первая группа занимает 4 символа, остальные по 6. Создать список групп из основного
								mainGroups.add(mainSched.get(i).getTeacher().substring(simbol, simbol + 4));
								simbol += 6;
							}
							simbol = 0;
							for (int len = 0; len < (modSched.get(j).getTeacher().length() - 4) / 6 + 1; len++) { //Выкинуть из списка то, что есть в дополнительном
								mainGroups.remove(modSched.get(j).getTeacher().substring(simbol, simbol + 4));
								simbol += 6;
							}
							//Заменить содержимое списка групп основного расписания
							String teacher = new String();
							for (int q = 0; q < mainGroups.size(); q++) { //По идее, если после цикла teacher останется нулевым, все совпадает и можно убирать основные
								teacher += mainGroups.get(q);
								if (q != mainGroups.size()-1) {
									teacher += ", ";
								}
							}
							mainSched.get(i).setTeacher(teacher);
							list.add(mainSched.get(i));
							i++; j++;
						}
					}
					if (!mainSched.get(i).getName().equals(modSched.get(j).getName())) {//Если названия не совпадают, занести только основную пару
						if (i == 0 && j == 0) { //Если это первая запись для дня, добавить в нее дату
							modSched.get(j).setDate(mDate);
						}
						list.add(mainSched.get(i));
						i++;
					} else {
						// Возможно, это лекция у преподавателя. Проанализировать список
						if (!isGroup && mainSched.get(i).getTeacher().length() == modSched.get(j).getTeacher().length() || isGroup) {
							//Пока костыль. Если расписание для группы ИЛИ расписание для преподавателя, но длина списка групп совпадает, все хорошо. Вносить в основной лист из модификации
							if (i == 0 && j == 0) { //Если это первая запись для дня, добавить в нее дату
								modSched.get(j).setDate(mDate);
							}
							list.add(modSched.get(j));
						} else
						{ //Если совпадения нет, внести тех, что есть в модификациях, затем основным расписанием тех, кого там нет
							if (firstPair) { //Если это первая запись для дня, добавить в нее дату
								modSched.get(j).setDate(mDate);
								firstPair = false;
							}
							list.add(modSched.get(j));
							ArrayList<String> mainGroups = new ArrayList<String>();
							int simbol = 0;
							for (int len = 0; len < (mainSched.get(i).getTeacher().length() - 4) / 6 + 1; len++) { //Первая группа занимает 4 символа, остальные по 6. Создать список групп из основного
								mainGroups.add(mainSched.get(i).getTeacher().substring(simbol, simbol + 4));
								simbol += 6;
							}
							for (int len = 0; len < (modSched.get(j).getTeacher().length() - 4) / 6 + 1; len++) { //Выкинуть из списка то, что есть в дополнительном
								mainGroups.remove(modSched.get(j).getTeacher().substring(simbol, simbol + 4));
								simbol += 6;
							}
							//Заменить содержимое списка групп основного расписания
							String teacher = new String();
							for (int q = 0; q < mainGroups.size(); q++) { //По идее, если после цикла teacher останется нулевым, все совпадает и можно убирать основные
								teacher += mainGroups.get(q);
								if (q != mainGroups.size()-1) {
									teacher += ", ";
								}
							}
							mainSched.get(i).setTeacher(teacher);
							if (!teacher.isEmpty()) { //По идее, если после цикла teacher останется нулевым, все совпадает и можно убирать основные 
								if (firstPair) { //Если это первая запись для дня, добавить в нее дату
									mainSched.get(i).setDate(mDate);
									firstPair = false;
								}
								list.add(mainSched.get(i));
							}
						}
						//Не обрабатывать эти пары повторно:
						i++; j++;
					}
				} else if (Integer.parseInt(mainSched.get(i).getN()) <  Integer.parseInt(modSched.get(j).getN())) {
					//Если одно меньше другого, увеличить только его переменную, внести в расписание только его код
					if (firstPair) { //Если это первая запись для дня, добавить в нее дату
						mainSched.get(i).setDate(mDate);
						firstPair = false;
					}
					list.add(mainSched.get(i));
					i++;
				} else if (Integer.parseInt(mainSched.get(i).getN()) >  Integer.parseInt(modSched.get(j).getN())) {
					//Если одно меньше другого, увеличить только его переменную, внести в расписание только его код
					if (firstPair) { //Если это первая запись для дня, добавить в нее дату
						modSched.get(j).setDate(mDate);
						firstPair = false;
					}
					list.add(modSched.get(j));
					j++;
				}
			}
			//Определить, какой из списков недовнесен. Внести данные из него
			for (; i < mainSched.size(); i++) {
				if (firstPair) { //Если это первая запись для дня, добавить в нее дату
					mainSched.get(i).setDate(mDate);
					firstPair = false;
				}
				list.add(mainSched.get(i));
			}
			for (; j < modSched.size(); j++) {
				if (firstPair) { //Если это первая запись для дня, добавить в нее дату
					modSched.get(j).setDate(mDate);
					firstPair = false;
				}
				list.add(modSched.get(j));
			}
		}
		adapter = new ScheduleAdapter(getActivity(), list);
	}


	public ArrayList<ShModel> list(ArrayList<PAIR> dayList, boolean isGroup) { //Чтобы не дублировать код. Вызывается для каждого дня отдельно для расписания и модификаций, собирает 2 списка
		ArrayList<ShModel> list = new ArrayList<ShModel>(); //Основной лист для записывания данных
		for (int i = 0; i < dayList.size(); ++i) { //Оформление расписания для конкретного дня
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
					date,
					dayList.get(i).IS_CANCELED);
			list.add(item);
			i+=skip;
		}
		return list;		
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
		switch (item.getItemId()) {
		case R.id.navigation:
			onCreateDialog(sIS);
			calendarDialog.show();
			break;
		case 100: break; //Для вывода подменю
		case 16908332: break; //Для вывода бокового меню
		default:
			Editor ed = sPref.edit();
			ed.putString("set", data[item.getItemId()-101]).apply();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.container, new DailyScheduleFragment()).commit();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
