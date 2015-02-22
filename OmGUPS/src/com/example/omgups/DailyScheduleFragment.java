package com.example.omgups;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.omgups.Parsers.PAIR;

public class DailyScheduleFragment extends Fragment implements OnClickListener {
	/** 
	 * �����, ������������ ���������� �� ������������ ���� � ���� ������ ��������� � �����������
	 */
	SharedPreferences sPref;
	ListView lw;
	GridView gw;
	ScheduleAdapter adapter;
	Button login, settings, update;
	GetListTask glt = null;
	String key;
	Map<String, Integer> hm = new HashMap<String, Integer>();


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		sPref = getActivity().getSharedPreferences("groups", Context.MODE_PRIVATE);
		View view = null;
		if (sPref.contains("main_group") || sPref.contains("set")) {
			if (sPref.contains(sPref.getString("main_group", "") + "main") || sPref.contains("set")) {
				try {
					view = inflater.inflate(R.layout.daily_schedule_fragment, null); //���� ������ ����������, ������� �������� � ������
					gw = (GridView)view.findViewById(R.id.daysView);
					gw.setColumnWidth(30);
					ArrayAdapter<String> ad = new ArrayAdapter<String>(getActivity(), R.layout.day, R.id.tv, getResources().getStringArray(R.array.days));
					gw.setAdapter(ad);

					lw = (ListView)view.findViewById(R.id.schedule);					
					parseGroups(sPref.getString("set", ""), sPref.getString("main_group", ""));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				lw.setAdapter(adapter);
				gw.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent,
							View view, int position, long id) {
						switch (position) { //������������ ����� ��� ��������� �������
						case 0:
							key += "MONDAY"; break;
						case 1:
							key += "TUESDAY"; break;
						case 2:
							key += "WEDNESDAY"; break;
						case 3:
							key += "THURSDAY"; break;
						case 4:
							key += "FRIDAY"; break;
						case 5:
							key += "SATURDAY"; break;
						}
						if (hm.containsKey(key)) {
							final Integer k = hm.get(key);
							gw.post(new Runnable() {
								@Override
								public void run() {								
									lw.setSelection(k);
								}
							});
						} else {
							Toast.makeText(getActivity(), "��������� ����", Toast.LENGTH_SHORT).show();;
						}
						gw.setVisibility(View.GONE);
						key = "";
					}
				});

			}
			else {
				view = inflater.inflate(R.layout.null_schedule_fragment, null); //���� ������ �� ����������, ������� ����������
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

	//	private void focus() {
	//��������� ���������� � ������� ��������� ������ �������
	//		GregorianCalendar calendar = new GregorianCalendar();
	//		String day = new String();
	//		switch (calendar.get(Calendar.WEEK_OF_YEAR) % 2) {
	//		case 0: //���� ������ ������ �� ���������, ��� �������� �� ����������
	//			day = "�����";
	//			break;
	//		case 1:
	//			day = "����";
	//		}
	//		int cal = calendar.get(Calendar.DAY_OF_WEEK) - 2;
	//		Log.d("11", "1 " + day + " " + navWeek + " " + navDay + " " + cal);
	//		int position = 0;
	//		if (navWeek == 1 && day.contains("�����") || navWeek == 2 && day.contains("����")) { //���� ��������� ��� ������� � ��������� ������
	//			if (cal == navDay) { //���� ��� ��� �� ���������, �������� � ������� ��������
	//				Log.d("11", "2 ");
	//				position = 0;
	//				lw.setSelection(0);
	//			}
	//			if (cal < navDay && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) { //���� ������� ���� ������ ���������� (����������� - �����)
	//				//������ ������� ��������� � ���������� �� 1 �� 5, ��������� ���� � ��������� �����
	//				Log.d("11", "3 ");
	//			}
	//			if (cal > navDay || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) { //���� ������� ���� ����� ����������
	//				if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
	//					Log.d("11", "4 ");
	//					position += (12 - cal - navDay);
	//				}else {
	//					Log.d("11", "5 ");
	//					position += (12 - cal + navDay); //������ ������� �������� ������, ����� ������
	//				}
	//			}
	//		} else { //���� �� ���������
	//			if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) { //���� ������ �����������
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
			Toast.makeText(getActivity(), "� ��� �����������", Toast.LENGTH_SHORT).show();
			// ��� ������ ���� �����������... �����-������
			// ����� ���������� �����������
			break;
		case R.id.set_id:
			// ������� � ���� ���������
			sPref = getActivity().getSharedPreferences("item_list", Context.MODE_PRIVATE);
			if(!sPref.contains("DEPARTMENTS")) { //���� �� ���������� ���� ����� (���� �� ���������� ����)
				//��������� task � ���������
				if (SideBar.isNetworkConnected(getActivity())) {
					glt = new GetListTask(getActivity()); //������������ ��� ��������� �������
					glt.execute();
				}
				else {
					Toast.makeText(getActivity(), "�� ������� �������� ������" + '\n'
							+ "��������� ���������� � ����������", Toast.LENGTH_LONG).show();
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
			// �������� ����
			sPref = getActivity().getSharedPreferences("groups", Context.MODE_PRIVATE);
			if (sPref.contains("main_group")) {
				if (sPref.contains(sPref.getString("main_group", "") + "main")) {					
					android.app.FragmentManager fragmentManager = getFragmentManager();		
					fragmentManager.beginTransaction()
					.replace(R.id.content_frame, new DailyScheduleFragment()).commit();
				}
			}
			break;
		}
	}

	private void parseGroups(String set, String main) throws JSONException {	 //��������� ����������. ���� ���� ������� set, �� ��� ����������, ����� ��������
		//����������� ��� ��� ������ ����������
		GregorianCalendar calendar = new GregorianCalendar();
		String day = new String();
		String day1 = new String();
		String str = new String(); //�������� ����������
		str = set.isEmpty() ?  new String (sPref.getString(sPref.getString("main_group", "") + "main", "")) : 
			new String (sPref.getString(sPref.getString("set", "") + "main", ""));
		JSONObject obj = new JSONObject(str);
		//		String str1 = new String(); //�����������
		//		str1 = set.isEmpty() ?  new String (sPref.getString(sPref.getString("main_group", "") + "mod", "")) : 
		//			new String (sPref.getString(sPref.getString("set", "") + "mod", ""));
		//		JSONObject mod = new JSONObject(str1);
		calendar.add(Calendar.DAY_OF_YEAR, -1); //����� ���������� ���������� � "�������"
		ArrayList<ShModel> list = new ArrayList<ShModel>(); //�������� ���� ��� ����������� ������
		for (int d = 0; d < 30; d++) { //���� ��� 30 ���� ��� ����������� ����������
			calendar.add(Calendar.DAY_OF_YEAR, 1); //���������� ��� ������� ������������ ���
			switch (calendar.get(Calendar.WEEK_OF_YEAR) % 2) {
			case 0: //���� ������ ������ �� ���������, ��� �������� �� ����������
				day = "ODD_";
				break;
			case 1:
				day = "EVEN_";
			}
			switch (calendar.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SUNDAY: //���� ����������� - ������� �� ��������� �������� �����
				continue;
			case Calendar.MONDAY:
				day += "MONDAY"; //��� �������
				day1 = "�����������"; //��� ������ � ���� "����"
				break; 
			case Calendar.TUESDAY:
				day += "TUESDAY";
				day1 = "�������";
				break; 
			case Calendar.WEDNESDAY:
				day += "WEDNESDAY";
				day1 = "�����";
				break; 
			case Calendar.THURSDAY:
				day += "THURSDAY";
				day1 = "�������";
				break; 
			case Calendar.FRIDAY:
				day += "FRIDAY";
				day1 = "�������";
				break; 
			case Calendar.SATURDAY:
				day += "SATURDAY";
				day1 = "�������";
				break; 
			}
			//			int number; //����������� ����������
			//			if (mod.has(day)) { //������ �� ����������� ������������ ����������� � ���
			//				ArrayList<PAIRMOD> dayPairs = PAIRMOD.fromJson(mod.getJSONArray(day));
			//				for (int m = 0; m < dayPairs.size(); m++) {
			//					if (calendar.after(dayPairs.get(m).begin) && calendar.before(dayPairs.get(m).end)) {
			//						number = dayPairs.get(m).PAIR_NUMBER;
			//					}
			//				}
			//			}
			if (!obj.has(day)) { //���� ������ ��� � ���������� �� ���������� - ������� �� ��������� ��������
				continue;
			}
			String group = set.isEmpty() ?  new String (sPref.getString("main_group", "")) : new String (sPref.getString("set", "")); //����������, ������ �� ��� ������ ����������
			Boolean isGroup; 
			try {
				Integer.parseInt(group.charAt(0) + "");
				isGroup = true; //�������� �������� - ������ ������ �����, ������� ������
			} catch (NumberFormatException e) {
				isGroup = false; //�������� �� ��������, ������ ������ �� �����, ������ - �������������		    	
			}

			ArrayList<PAIR> dayList = PAIR.fromJson(obj.getJSONArray(day), isGroup);
			// ����������� ������ � �������� ��� �������� ���������
			for (int i = 0; i < dayList.size(); ++i) { //���������� ���������� ��� ����������� ���
//				if (i > 0)
//					if (!isGroup && (dayList.get(i).PAIR_NUMBER == Integer.parseInt(list.get(list.size()-1).getN()))) {
//						continue;
//					}

				String teacher = new String();				
				teacher = dayList.get(i).NAME;
				int skip = 0;
				for (int j = 1; j < dayList.size() - i; ++j) { //���� ��� ��������������: ����� �� ������������� ����
					if (dayList.get(i).PAIR_NUMBER == dayList.get(i + j).PAIR_NUMBER && //���� ��������� ������ � ������� ������������ ���
							dayList.get(i).DISCIPLINE.equals(dayList.get(i + j).DISCIPLINE)) { //� �� ���������						
						teacher += ", " + dayList.get(i + j).NAME; //�������� ������ � ������� ����
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
				if (i == 0) { //��� ������ ������ ��� �� ���������� ����, ������ � ����� ��� ����������� ��������
					date = dayP + "." + monthP + "." + calendar.get(Calendar.YEAR) + ", " + day1;
					if (!hm.containsKey(day)) {
						hm.put(day, list.size());
					}
				}
				String discipline = dayList.get(i).DISCIPLINE;
				if (!dayList.get(i).SUBGROUP.equals("0")) {
					discipline += ", ��������� " + dayList.get(i).SUBGROUP; 
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





	@SuppressWarnings("deprecation")
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.daily_schedule, menu);

		ActionBar bar = getActivity().getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		//		AttributeSet attr = 
		//		TextView view = new TextView(getActivity(),)
		if (sPref.contains("list")) {
			Set<String> list = sPref.getStringSet("list", new HashSet<String>());
			final String[] data = list.toArray(new String[list.size()]);
			final String[] dataa = new String[list.size() + 1];
			dataa[0] = "����������: " + (sPref.getString("set", "").isEmpty() ? sPref.getString("main_group", "") : sPref.getString("set", ""));
			for (int i = 0; i < list.size(); i++) {
				dataa[i+1] = data[i];
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
					R.layout.drawer_list_item, R.id.label, dataa);
			adapter.setDropDownViewResource(R.layout.drawer_list_item);
			bar.setListNavigationCallbacks(adapter, new OnNavigationListener() {

				@Override
				public boolean onNavigationItemSelected(int position, long id) {
					if (!dataa[position].contains("����������")) {
						Editor ed = sPref.edit();
						ed.putString("set", dataa[position]).apply();
						FragmentTransaction ft = getFragmentManager().beginTransaction();
						ft.replace(R.id.content_frame, new DailyScheduleFragment()).commit();
					}
					return true;
				}
			});
		}
		bar.setSelectedNavigationItem(-1);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.ds_today:
			lw.setSelection(0);
			break;
		case R.id.ds_navigation_1:
			if (gw.getVisibility() == View.VISIBLE) {
				gw.setVisibility(View.GONE);
				key = "";
			} else {
				gw.setVisibility(View.VISIBLE);
				key = "ODD_";
			}
			break;
		case R.id.ds_navigation_2:
			if (gw.getVisibility() == View.VISIBLE) {
				gw.setVisibility(View.GONE);
				key = "";
			} else {
				gw.setVisibility(View.VISIBLE);
				key = "EVEN_";
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}