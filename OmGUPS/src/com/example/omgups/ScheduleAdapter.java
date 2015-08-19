package com.example.omgups;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ScheduleAdapter extends BaseAdapter {

	Context context;
	ArrayList<ShModel> list;
	byte paint = 10;
	ArrayList<String> namesArray;


	ScheduleAdapter(Context context, ArrayList<ShModel> list) {
		this.context = context;
		this.list = list;
		Set<String> name = null;

		switch (
			context.getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).getString("daily", "")) { //определение метода раскрашивания
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
		//список
		public TextView n;
		public TextView time;
		public TextView name;
		public TextView teacher;
		public TextView auditory;
		public TextView tipe;
		public TextView date;
		public LinearLayout pairContainer;

		//краткая сетка
		public TextView snum;
		public FrameLayout pair1, pair2, pair3, pair4, pair5;
	}

	// пункт списка
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
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
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.schedule_line, null, true);
				holder = new ViewHolder();
				holder.n = (TextView) rowView.findViewById(R.id.pairNumber);
				holder.time = (TextView) rowView.findViewById(R.id.pairTime);
				holder.name = (TextView) rowView.findViewById(R.id.pairName);
				holder.teacher = (TextView) rowView.findViewById(R.id.teacher);
				holder.auditory = (TextView) rowView.findViewById(R.id.auditory);
				holder.tipe = (TextView) rowView.findViewById(R.id.pairType);
				holder.date = (TextView) rowView.findViewById(R.id.date);
				holder.pairContainer = (LinearLayout) rowView.findViewById(R.id.pairContainer);
				rowView.setTag(holder);
			} else {
				holder = (ViewHolder) rowView.getTag();
			}

			holder.n.setText(list.get(position).getN());
			holder.time.setText(list.get(position).getTime());
			holder.name.setText(list.get(position).getName());
			holder.teacher.setText(list.get(position).getTeacher());
			holder.auditory.setText(list.get(position).getAuditory());
			holder.tipe.setText(list.get(position).getTipe());
			holder.date.setText(list.get(position).getDate());


			//раскрашивание даты, цвет определен
			if (list.get(position).getDate().isEmpty()) {
				holder.date.setVisibility(View.GONE);
			} else {
				holder.date.setVisibility(View.VISIBLE);
				holder.date.setBackgroundColor(context.getResources().getColor(R.color.schedule_dark));
				holder.date.setTextColor(context.getResources().getColor(R.color.schedule_light));
			}
			//раскрашивание элементов в зависимости от настроек

			int darkColor = 0, lightColor = 0;
			boolean def = false;		
			switch (paint) {
			case (0): //по предмету
				index = namesArray.indexOf(list.get(position).getName().replaceFirst(", подгруппа [0-9]", ""))%24;
			darkColor = dark[index];
			lightColor = light[index];
			break;

			case (1): //по типу предмета
				switch (list.get(position).getTipe()) { //определение типа
				case "Лекция":
					darkColor = dark[0];
					lightColor = light[0];
					break;
				case "Практика":
					darkColor = dark[1];
					lightColor = light[1];
					break;
				case "Лабораторная":
					darkColor = dark[2];
					lightColor = light[2];
					break;
				default:
					darkColor = dark[3];
					lightColor = light[3];
					break;				
				}
			break;

			case (2): //по преподавателю
				index = namesArray.indexOf(list.get(position).getTeacher())%24;
			if (index >= 0) { //костыль, бывает выдает -1
				darkColor = dark[index];
				lightColor = light[index];
			}
			break;

			default:
				holder.n.setBackgroundColor(context.getResources().getColor(R.color.schedule_dark));
				holder.n.setTextColor(context.getResources().getColor(R.color.schedule_light));
				def = true;
				break;
			}
			if (!def) {
				holder.n.setBackgroundColor(darkColor);
				holder.pairContainer.setBackgroundColor(lightColor);
			}
		return rowView;
	}
}
