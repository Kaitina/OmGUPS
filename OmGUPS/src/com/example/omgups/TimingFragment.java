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
	 * �������� ��� ������ � ����������� �������������
	 * ����������� � �������������� ����������
	 * ��������� ���������� Timing
	 */
	TimePicker tp;
	ListView clickBox;
	SharedPreferences sPref;
	private static final String LOG_TAG  = "myLogs";
	Context context;


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setRetainInstance(true);  //�������� �������� ��������� ������ � ����������
		View view = inflater.inflate(R.layout.timing, container, false);
		context = getActivity();

		tp = (TimePicker)view.findViewById(R.id.timePicker);        
		tp.setIs24HourView(true); // ������ 24 ����		
		clickBox = (ListView)view.findViewById(R.id.clickBox);
		clickBox.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				context, R.array.clickBox,
				android.R.layout.simple_list_item_single_choice);
		clickBox.setAdapter(adapter); // ������� �������, ��������� ������ �� ����� ��������
		clickBox.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {  //��� ������� �� ��������� ������ ����� ������� ���������
				case 0:  //���� ������� "��� �������"
					tp.setVisibility(View.INVISIBLE);
					break;
				case 1:  //���� ������� "���������"
					tp.setVisibility(View.VISIBLE);
					break;
				case 2:  //���� ������� "��� � ������"
					tp.setVisibility(View.VISIBLE);
					break;
				case 3:  //���� ������� "��� � �����"
					tp.setVisibility(View.VISIBLE);
					break;
				case 4:  //���� ������� "25 �������"
					tp.setVisibility(View.VISIBLE);
					break;
				case 5:  //���� ������� "�������"
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
	public void onResume() {  //��� ������\�������������� ������ �������� ����������� ��������������
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
				clickBox.setItemChecked(position, true); //�������� ����������� ����� ��������
			}
			tp.setCurrentHour(new Integer(sPref.getInt("timing_h", 0)));
			tp.setCurrentMinute(new Integer(sPref.getInt("timing_m", 0)));
		}
		super.onResume();
	}

	@Override
	public void onPause() {  //��� ����� ��������� ��������� ���������
		super.onPause();
		sPref = context.getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE);
		Editor ed = sPref.edit();
		//��������� �����
		int h = tp.getCurrentHour();
		int m = tp.getCurrentMinute();
		int check = clickBox.getCheckedItemPosition(); //���������� ������� �������
		switch (check) { //� ����������� �� ����� ������� � ���� ������ ����
		case 0:  //���� ������� "��� �������"
			ed.putString("timing_date", "ever");
			ed.putInt("timing_h",h);
			ed.putInt("timing_m",m);
			ed.apply();
			break;
		case 1:  //���� ������� "���������"
			ed.putString("timing_date","daily");
			ed.putInt("timing_h",h);
			ed.putInt("timing_m",m);
			ed.apply();
			break;
		case 2:  //���� ������� "��� � ������"
			ed.putString("timing_date","monthly");
			ed.putInt("timing_h",h);
			ed.putInt("timing_m",m);
			ed.apply();
			break;
		case 4:  //���� ������� "25 �������"
			ed.putString("timing_date","inAugust");
			ed.putInt("timing_h",h);
			ed.putInt("timing_m",m);
			ed.apply();
			break;
		case 5:  //���� ������� "�������"
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

