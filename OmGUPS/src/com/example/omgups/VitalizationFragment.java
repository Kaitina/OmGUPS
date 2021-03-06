package com.example.omgups;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class VitalizationFragment extends Fragment{

	SharedPreferences sPref;
	CheckBox vertical, horisontal;
	RadioGroup calendar, daily;
	RadioButton radio0, radio1, radio2, radio3, RadioButton01, RadioButton02, RadioButton03, RadioButton04;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setRetainInstance(true);  //�������� �������� ��������� ������ � ����������
		View view = inflater.inflate(R.layout.vitalization, container, false);
		vertical = (CheckBox)view.findViewById(R.id.full_vertical);
		horisontal = (CheckBox)view.findViewById(R.id.full_horisontal);
		calendar = (RadioGroup)view.findViewById(R.id.radioGroupCalendar);
		daily = (RadioGroup)view.findViewById(R.id.RadioGroupDaily);
		radio0 = (RadioButton)view.findViewById(R.id.radio0);
		radio1 = (RadioButton)view.findViewById(R.id.radio1);
		radio2 = (RadioButton)view.findViewById(R.id.radio2);
		radio3 = (RadioButton)view.findViewById(R.id.radio3);
		RadioButton01 = (RadioButton)view.findViewById(R.id.RadioButton01);
		RadioButton02 = (RadioButton)view.findViewById(R.id.RadioButton02);
		RadioButton03 = (RadioButton)view.findViewById(R.id.RadioButton03);
		RadioButton04 = (RadioButton)view.findViewById(R.id.RadioButton04);

		sPref = getActivity().getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE);
		vertical.setChecked(sPref.getBoolean("full_vertical", false));
		horisontal.setChecked(sPref.getBoolean("full_horisontal", false));
		switch (sPref.getString("calendar", "")) {
		case "radio0":
			radio0.setChecked(true); break;
		case "radio1":
			radio1.setChecked(true); break;
		case "radio2":
			radio2.setChecked(true); break;
		case "radio3":
			radio3.setChecked(true); break;
		default: 
			radio3.setChecked(true); break;
		}
		switch (sPref.getString("daily", "")) {
		case "RadioButton01":
			RadioButton01.setChecked(true); break;
		case "RadioButton02":
			RadioButton02.setChecked(true); break;
		case "RadioButton03":
			RadioButton03.setChecked(true); break;
		case "RadioButton04":
			RadioButton04.setChecked(true); break;
		default: 
			RadioButton04.setChecked(true); break;
		}
		return view;
	}


	@Override
	public void onDestroy() {
		Editor ed = sPref.edit();
		ed.putBoolean("full_vertical", vertical.isChecked());
		ed.putBoolean("full_horisontal", horisontal.isChecked());
		switch (calendar.getCheckedRadioButtonId()) {
		case R.id.radio0:
			ed.putString("calendar", "radio0"); break;
		case R.id.radio1:
			ed.putString("calendar", "radio1"); break;
		case R.id.radio2:
			ed.putString("calendar", "radio2"); break;
		case R.id.radio3:
			ed.putString("calendar", "radio3"); break;
		}
		switch (daily.getCheckedRadioButtonId()) {
		case R.id.RadioButton01:
			ed.putString("daily", "RadioButton01"); break;
		case R.id.RadioButton02:
			ed.putString("daily", "RadioButton02"); break;
		case R.id.RadioButton03:
			ed.putString("daily", "RadioButton03"); break;
		case R.id.RadioButton04:
			ed.putString("daily", "RadioButton04"); break;
		}
		ed.apply();
		super.onDestroy();
	}

}
