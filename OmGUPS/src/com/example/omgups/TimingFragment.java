package com.example.omgups;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemClickListener;

public class TimingFragment extends Fragment{
	/** 
	 * Фрагмент для работы с настройками синхронизации
	 * Открывается в горизонтальной ориентации
	 * Дублирует активность Timing
	 */
	TimePicker tp;
	ListView clickBox;
	SharedPreferences sPref;
	private static final String LOG_TAG  = "myLogs";
	Context context;


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setRetainInstance(true);  //Создание основных элементов вместе с фрагментом
		View view = inflater.inflate(R.layout.timing, container, false);
		context = getActivity();

		tp = (TimePicker)view.findViewById(R.id.timePicker);        
		tp.setIs24HourView(true); // формат 24 часа		
		clickBox = (ListView)view.findViewById(R.id.clickBox);
		clickBox.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				context, R.array.clickBox,
				android.R.layout.simple_list_item_single_choice);
		clickBox.setAdapter(adapter); // Создаем адаптер, используя массив из файла ресурсов
		clickBox.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {  //При нажатии на некоторые строки выбор времени пропадает
				case 0:  //Если выбрано "при запуске"
					tp.setVisibility(View.INVISIBLE);
					break;
				case 1:  //Если выбрано "ежедневно"
					tp.setVisibility(View.VISIBLE);
					break;
				case 2:  //Если выбрано "раз в неделю"
					tp.setVisibility(View.VISIBLE);
					break;
				case 3:  //Если выбрано "раз в месяц"
					tp.setVisibility(View.VISIBLE);
					break;
				case 4:  //Если выбрано "25 августа"
					tp.setVisibility(View.VISIBLE);
					break;
				case 5:  //Если выбрано "никогда"
					tp.setVisibility(View.INVISIBLE);
					break;
				default:
					break;
				}
			}
		});
		return view;
	}



	@Override
	public void onResume() {  //При начале\восстановлении работы выделить сохраненные характеристики
		sPref = context.getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE);
		if(sPref.contains("timing_date")) {
			int position = -1;
			String pos = sPref.getString("timing_date", "");
			switch (pos) {
			case "ever":
				tp.setVisibility(View.INVISIBLE);
				position = 0;
				break;
			case "daily":
				position = 1;
				break;
			case "weekly":
				position = 2;
				break;
			case "monthly":
				position = 3;
				break;
			case "inAugust":
				position = 4;
				break;
			case "nothing":
				tp.setVisibility(View.INVISIBLE);
				position = 5;
				break;
			default:
				break;
			}
			if (position != -1) {
				clickBox.setItemChecked(position, true); //Выделяем сохраненное ранее значение
			}
			tp.setCurrentHour(new Integer(sPref.getInt("timing_h", 0)));
			tp.setCurrentMinute(new Integer(sPref.getInt("timing_m", 0)));
		}
		super.onResume();
	}

	@Override
	public void onPause() {  //При паузе сохранить выбранные настройки
		super.onPause();
		sPref = context.getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE);
		Editor ed = sPref.edit();
		//Считываем время
		int h = tp.getCurrentHour();
		int m = tp.getCurrentMinute();
		int check = clickBox.getCheckedItemPosition(); //Определяем нажатый элемент
		switch (check) { //В зависимости от этого заносим в файл разные даты
		case 0:  //Если выбрано "при запуске"
			ed.putString("timing_date", "ever");
			ed.putInt("timing_h",h);
			ed.putInt("timing_m",m);
			ed.apply();
			break;
		case 1:  //Если выбрано "ежедневно"
			ed.putString("timing_date","daily");
			ed.putInt("timing_h",h);
			ed.putInt("timing_m",m);
			ed.apply();
			break;
		case 2:  //Если выбрано "раз в неделю"
			ed.putString("timing_date","monthly");
			ed.putInt("timing_h",h);
			ed.putInt("timing_m",m);
			ed.apply();
			break;
		case 4:  //Если выбрано "25 августа"
			ed.putString("timing_date","inAugust");
			ed.putInt("timing_h",h);
			ed.putInt("timing_m",m);
			ed.apply();
			break;
		case 5:  //Если выбрано "никогда"
			ed.putString("timing_date","nothing");
			ed.putInt("timing_h",h);
			ed.putInt("timing_m",m);
			ed.apply();
			break;
		default:
			break;
		}
	}

	@Override
	public void onDestroy() {
		context.stopService(new Intent(context, UpdateService.class));
		context.startService(new Intent(context, UpdateService.class));
		super.onDestroy();
	}

}

