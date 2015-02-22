package com.example.omgups;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CalendarScheduleAdapter extends BaseAdapter {

	Context context;
	ArrayList<ShModel> list;
	byte paint = 10;
	ArrayList<String> namesArray;
	Boolean isDetail = false;

	CalendarScheduleAdapter(Context context, ArrayList<ShModel> list) {
		this.context = context;
		this.list = list;
		Set<String> name = null;



		//определение метода заполнения календаря
		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && //Если по текущей ориентации в настройках установлено расширенное расписание
				context.getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).getBoolean("full_horisontal", false) == true
				|| context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT &&
				context.getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).getBoolean("full_vertical", false) == true) {
			isDetail = true;
		}

		switch (context.getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).getString("calendar", ""))
		{ //определение метода раскрашивания
		case "RadioButton01": //по предмету
			paint = 0;
			SharedPreferences sPref = context.getSharedPreferences("groups", Context.MODE_PRIVATE);
			name = sPref.getStringSet("pair_names", new LinkedHashSet<String>());
			break;
		case "RadioButton02": //по типу предмета
			paint = 1;
			break;
		case "RadioButton03": //по преподавателю
			paint = 2;
			sPref = context.getSharedPreferences("groups", Context.MODE_PRIVATE);
			name = sPref.getStringSet("pair_teachers", new LinkedHashSet<String>());
			break;
		}

		if (name != null) {
			namesArray = new ArrayList<String>(name);
		}
	}

	// кол-во элементов
	@Override
	public int getCount() {
		return list.size();
	}

	// элемент по позиции
	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	// id по позиции
	@Override
	public long getItemId(int position) {
		return position;
	}


	static class ViewHolder {
		//краткая сетка
		public TextView cell;
	}

	// пункт списка
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d("11", "position: " + Integer.toString(position) + ", n: " + list.get(position).getN() + ", name: " + list.get(position).getName());
		int dark[] = {context.getResources().getColor(R.color.dark1), context.getResources().getColor(R.color.dark2),
				context.getResources().getColor(R.color.dark3), context.getResources().getColor(R.color.dark4),
				context.getResources().getColor(R.color.dark5), context.getResources().getColor(R.color.dark6),
				context.getResources().getColor(R.color.dark7), context.getResources().getColor(R.color.dark8),
				context.getResources().getColor(R.color.dark9), context.getResources().getColor(R.color.dark10),
				context.getResources().getColor(R.color.dark11), context.getResources().getColor(R.color.dark12),
				context.getResources().getColor(R.color.dark13), context.getResources().getColor(R.color.dark14),
				context.getResources().getColor(R.color.dark15), context.getResources().getColor(R.color.dark16),
				context.getResources().getColor(R.color.dark17), context.getResources().getColor(R.color.dark18),
				context.getResources().getColor(R.color.dark19), context.getResources().getColor(R.color.dark20),
				context.getResources().getColor(R.color.dark21), context.getResources().getColor(R.color.dark22),
				context.getResources().getColor(R.color.dark23), context.getResources().getColor(R.color.dark24)};
		int light[] = {context.getResources().getColor(R.color.light1), context.getResources().getColor(R.color.light2),
				context.getResources().getColor(R.color.light3), context.getResources().getColor(R.color.light4),
				context.getResources().getColor(R.color.light5), context.getResources().getColor(R.color.light6),
				context.getResources().getColor(R.color.light7), context.getResources().getColor(R.color.light8),
				context.getResources().getColor(R.color.light9), context.getResources().getColor(R.color.light10),
				context.getResources().getColor(R.color.light11), context.getResources().getColor(R.color.light12),
				context.getResources().getColor(R.color.light13), context.getResources().getColor(R.color.light14),
				context.getResources().getColor(R.color.light15), context.getResources().getColor(R.color.light16),
				context.getResources().getColor(R.color.light17), context.getResources().getColor(R.color.light18),
				context.getResources().getColor(R.color.light19), context.getResources().getColor(R.color.light20),
				context.getResources().getColor(R.color.light21), context.getResources().getColor(R.color.light22),
				context.getResources().getColor(R.color.light23), context.getResources().getColor(R.color.light24)};
		int index;

		ViewHolder holder;
		// Очищает сущетсвующий шаблон, если параметр задан
		// Работает только если базовый шаблон для всех классов один и тот же
		View rowView = convertView;
		//Оформление сетки календаря
		if (isDetail) { //Оформление подробного расписания
			Log.d("11", "1");

		} else { //краткое расписания: вместо данных окрашенные квадраты
			if (rowView == null) {
				Log.d("11", "2");
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.short_calendar_line, null, true);
				holder = new ViewHolder();
				holder.cell = (TextView) rowView.findViewById(R.id.cell);		
				rowView.setTag(holder);
			} else {
				Log.d("11", "3");
				holder = (ViewHolder) rowView.getTag();
			}
			int color = 0;
			Log.d("11", "4");
			if (position%6 == 0) {
				Log.d("11", "4.1");
				holder.cell.setText(list.get(position).getDate());
				holder.cell.setWidth(20);
			}
			if (!list.get(position).getName().equals("")) {
				Log.d("11", "5");
				Boolean def = false;
				switch (paint) {
				case (0): //по предмету
					Log.d("11", "6");
					index = namesArray.indexOf(list.get(position).getName().replaceFirst(", подгруппа [0-9]", ""))%24;
				color = dark[index];
				break;
				case (1): //по типу предмета
					Log.d("11", "7");
					switch (list.get(position).getTipe()) { //определение типа
					case "Лекция":
						color = dark[0];
						break;
					case "Практика":
						color = dark[1];
						break;
					case "Лабораторная":
						color = dark[2];
						break;
					default:
						color = dark[3];
						break;				
					}
				break;
				case (2): //по преподавателю
					Log.d("11", "8");
					index = namesArray.indexOf(list.get(position).getTeacher());
				if (index >= 0) { //костыль, бывает выдает -1
					color = dark[index];
				}
				break;
				default:
					Log.d("11", "9");
//					color = dark[11];
					def = true;
					break;
				}
				
				Log.d("11", Integer.toString(color));
				if (!def) {
					Log.d("11", "10");
				holder.cell.setBackgroundColor(color);
				} else {
					Log.d("11", "11");
					holder.cell.setBackground(context.getResources().getDrawable(R.drawable.drow));
//					holder.cell.setBackgroundColor(R.drawable.drow);
				}
				
			}
		}

		return rowView;
	}



}
