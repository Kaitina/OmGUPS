package com.example.omgups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.omgups.Parsers.MODIFICATOR;
import com.example.omgups.Parsers.PAIR;

public class DailyScheduleFragment extends DialogFragment
implements OnClickListener, OnScrollListener, SwipeRefreshLayout.OnRefreshListener {
	/** 
	 * �����, ������������ ���������� �� ������������ ���� � ���� ������ ��������� � �����������
	 */
	SharedPreferences sPref;
	ListView lw;
	ScheduleAdapter adapter;
	Button login, settings, update;
	GetListTask glt = null;	
	GetScheduleTask gsht = null;
	String[] data;
	String day1;
	GregorianCalendar calendar;
	String mDate;
	AlertDialog calendarDialog;
	Bundle sIS;
	private View footer;
	LoadMoreTask lmt;
	SwipeRefreshLayout mSwipeRefreshLayout;


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setRetainInstance(true);
		sIS = savedInstanceState;
		adapter = new ScheduleAdapter(new ArrayList<ShModel>());
		sPref = getActivity().getSharedPreferences("groups", Context.MODE_PRIVATE);
		View view = null;
		boolean data = false;
		calendar = new GregorianCalendar();
		if (sPref.contains("main_group") || sPref.contains("set") || sPref.contains("list")) {
			if (sPref.contains(sPref.getString("main_group", "") + "main") || sPref.contains("set")) {
				view = inflater.inflate(R.layout.daily_schedule_fragment, null); //���� ������ ����������, ������� �������� � ������
				lw = (ListView)view.findViewById(R.id.schedule);	
				footer = inflater.inflate(R.layout.listview_footer, null);
				lw.addFooterView(footer);
				lw.setAdapter(adapter);
				lmt = new LoadMoreTask();
				lmt.execute(14);
				lw.setOnScrollListener(this);
				mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
				mSwipeRefreshLayout.setOnRefreshListener(this);
			}
			else {
				Set<String> hs = sPref.getStringSet("list", new HashSet<String>()); //���� ��� ������ ���� �����-�� ������, ���� ��� �� �������, �� ��������� ��������
				String set[] = hs.toArray(new String [hs.size()]);
				for (int i = 0; i < hs.size(); i++) {
					if (sPref.contains(set[i] + "main")) {
						view = inflater.inflate(R.layout.daily_schedule_fragment, null); //���� ������ ����������, ������� �������� � ������
						lw = (ListView)view.findViewById(R.id.schedule);
						footer = inflater.inflate(R.layout.listview_footer, null);
						lw.addFooterView(footer);
						lw.setAdapter(adapter);
						lmt = new LoadMoreTask();
						lmt.execute(14);								
						lw.setOnScrollListener(this);
						SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
						mSwipeRefreshLayout.setOnRefreshListener(this);
						data = true;
						break;
					}
				}
				if (!data) {
					view = inflater.inflate(R.layout.null_schedule_fragment, null); //���� ������ �� ����������, ������� ����������
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
	public void onRefresh() {
		// �������� ���������� ��������
		mSwipeRefreshLayout.setRefreshing(true);
		gsht = new GetScheduleTask(getActivity());
		Set<String> listId = sPref.getStringSet("listId", new HashSet<String>());
		String str[] = new String[listId.size()]; //���� ��� ����������� ������
		int i = 0;
		for(String l : listId) {
			str[i] = l; //��������� ������ ���� ���������� ����� ��� ��������
			i++;

		}
		gsht = new GetScheduleTask(getActivity());
		gsht.execute(str); //��������� ������ �� ��������� ������ ����������
		// ������ ��������
		int result = 0;
		try {
			result = gsht.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		mSwipeRefreshLayout.setRefreshing(false);
		if (result > 0) {
			// ������������ ���� �������� ������
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			lw.setVisibility(View.INVISIBLE); //������ �������� �������� ������ ������. ������ ������ ��������
			ft.replace(R.id.container, new DailyScheduleFragment()).commit();
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setCancelable(true);
		builder.setNegativeButton("������", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss(); // ��������� ���������� ����					
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
					.replace(R.id.container, new DailyScheduleFragment()).commit();
				}
			}
			break;
		}
	}


	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.daily_schedule, menu);
		String group = null;
		group = sPref.getString("set",  sPref.getString("main_group", "������"));
		if (sPref.contains("list")) {
			Set<String> list = sPref.getStringSet("list", new HashSet<String>());
			data = list.toArray(new String[list.size()]);
			Arrays.sort(data); //��������� ������ ����� ��� ������ � ����������

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
		case 100: break; //��� ������ �������
		case 16908332: break; //��� ������ �������� ����
		default:
			Editor ed = sPref.edit();
			ed.putString("set", data[item.getItemId()-101]).apply();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.container, new DailyScheduleFragment()).commit();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {}

	@Override
	public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {
		boolean loadMore = firstVisible + visibleCount >= totalCount;

		if (loadMore && lmt.getStatus() == AsyncTask.Status.FINISHED) {
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			lmt = new LoadMoreTask();
			lmt.execute(totalCount);
		}
	}

	static class ViewHolder {
		//������
		public TextView n;
		public TextView time;
		public TextView name;
		public TextView teacher;
		public TextView auditory;
		public TextView tipe;
		public TextView date;
		public LinearLayout pairContainer;

		//������� �����
		public TextView snum;
		public FrameLayout pair1, pair2, pair3, pair4, pair5;
	}

	public class ScheduleAdapter extends BaseAdapter {
		//������� ��� ���������� ������ ����������

		ArrayList<ShModel> list;
		byte paint = 10;
		ArrayList<String> namesArray;


		ScheduleAdapter(ArrayList<ShModel> list) {
			this.list = list;
			Set<String> name = null;

			switch (
					getActivity().getSharedPreferences("omgupsSettings", Context.MODE_PRIVATE).getString("daily", "")) { //����������� ������ �������������
					case "RadioButton01": //�� ��������
						paint = 0;
						SharedPreferences sPref = getActivity().getSharedPreferences("groups", Context.MODE_PRIVATE);
						name = sPref.getStringSet("pair_names", new LinkedHashSet<String>());
						break;
					case "RadioButton02": //�� ���� ��������
						paint = 1;
						break;
					case "RadioButton03": //�� �������������
						paint = 2;
						sPref = getActivity().getSharedPreferences("groups", Context.MODE_PRIVATE);
						name = sPref.getStringSet("pair_teachers", new LinkedHashSet<String>());
						break;
			}

			if (name != null) {
				namesArray = new ArrayList<String>(name);
			}
		}

		// ���-�� ���������
		@Override
		public int getCount() {
			return list.size();
		}

		// ������� �� �������
		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		// id �� �������
		@Override
		public long getItemId(int position) {
			return position;
		}

		// ����� ������
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			@SuppressWarnings("deprecation")
			int dark[] = {getActivity().getResources().getColor(R.color.dark1), getActivity().getResources().getColor(R.color.dark2),
				getActivity().getResources().getColor(R.color.dark3), getActivity().getResources().getColor(R.color.dark4),
				getActivity().getResources().getColor(R.color.dark5), getActivity().getResources().getColor(R.color.dark6),
				getActivity().getResources().getColor(R.color.dark7), getActivity().getResources().getColor(R.color.dark8),
				getActivity().getResources().getColor(R.color.dark9), getActivity().getResources().getColor(R.color.dark10),
				getActivity().getResources().getColor(R.color.dark11), getActivity().getResources().getColor(R.color.dark12),
				getActivity().getResources().getColor(R.color.dark13), getActivity().getResources().getColor(R.color.dark14),
				getActivity().getResources().getColor(R.color.dark15), getActivity().getResources().getColor(R.color.dark16),
				getActivity().getResources().getColor(R.color.dark17), getActivity().getResources().getColor(R.color.dark18),
				getActivity().getResources().getColor(R.color.dark19), getActivity().getResources().getColor(R.color.dark20),
				getActivity().getResources().getColor(R.color.dark21), getActivity().getResources().getColor(R.color.dark22),
				getActivity().getResources().getColor(R.color.dark23), getActivity().getResources().getColor(R.color.dark24)};
			@SuppressWarnings("deprecation")
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
			int index;

			ViewHolder holder;
			// ������� ������������ ������, ���� �������� �����
			// �������� ������ ���� ������� ������ ��� ���� ������� ���� � ��� ��
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
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


			//������������� ����, ���� ���������
			if (list.get(position).getDate().isEmpty()) {
				holder.date.setVisibility(View.GONE);
			} else {
				holder.date.setVisibility(View.VISIBLE);
				holder.date.setBackgroundColor(getActivity().getResources().getColor(R.color.schedule_dark));
				holder.date.setTextColor(getActivity().getResources().getColor(R.color.schedule_light));
			}
			//������������� ��������� � ����������� �� ��������

			int darkColor = 0, lightColor = 0;
			boolean def = false;		
			switch (paint) {
			case (0): //�� ��������
				index = namesArray.indexOf(list.get(position).getName().replaceFirst(", ��������� [0-9]", ""))%24;
			darkColor = dark[index];
			lightColor = light[index];
			break;

			case (1): //�� ���� ��������
				switch (list.get(position).getTipe()) { //����������� ����
				case "������":
					darkColor = dark[0];
					lightColor = light[0];
					break;
				case "��������":
					darkColor = dark[1];
					lightColor = light[1];
					break;
				case "������������":
					darkColor = dark[2];
					lightColor = light[2];
					break;
				default:
					darkColor = dark[3];
					lightColor = light[3];
					break;				
				}
			break;

			case (2): //�� �������������
				index = namesArray.indexOf(list.get(position).getTeacher())%24;
			if (index >= 0) { //�������, ������ ������ -1
				darkColor = dark[index];
				lightColor = light[index];
			}
			break;

			default:
				holder.n.setBackgroundColor(getActivity().getResources().getColor(R.color.schedule_dark));
				holder.n.setTextColor(getActivity().getResources().getColor(R.color.schedule_light));
				def = true;
				break;
			}
			if (!def) {
				holder.n.setBackgroundColor(darkColor);
				holder.pairContainer.setBackgroundColor(lightColor);
			}
			return rowView;
		}

		public void add(ArrayList<ShModel> data) {
			this.list.addAll(data);			
		}		

	}

	//��������� ��������� ������ ��� ���������� ����� ������
	public class LoadMoreTask extends AsyncTask<Integer, Void, ArrayList<ShModel>> {
		String mDate;			

		@Override
		protected ArrayList<ShModel> doInBackground(Integer... params) {
			int count = params[0]; //������� ���� ���������
			String main = sPref.getString("main_group", ""); //�������� ������
			String set = sPref.getString("set", ""); //�������� ������
			if (main.isEmpty() && set.isEmpty()) { //���� ��� ������ - ������� ����� �� ���������
				Set<String> hs = sPref.getStringSet("list", new HashSet<String>()); //���� ��� ������ ���� �����-�� ������, ���� ��� �� �������, �� ��������� ��������
				String sets[] = hs.toArray(new String [hs.size()]);
				for (int i = 0; i < hs.size(); i++) {
					if (sPref.contains(sets[i] + "main")) {
						set = sets[i];
					}
				}
			}

			String group = set.isEmpty() ?  main : set; //����������, ������ �� ��� ������ ����������
			Boolean isGroup; 
			try {
				Integer.parseInt(group.charAt(0) + "");
				isGroup = true; //�������� �������� - ������ ������ �����, ������� ������
			} catch (NumberFormatException e) {
				isGroup = false; //�������� �� ��������, ������ ������ �� �����, ������ - �������������		    	
			}
			String day = new String();
			String day1 = new String();
			String str = set.isEmpty() ?  new String (sPref.getString(main + "main", "")) : new String (sPref.getString(set + "main", ""));
			JSONObject obj = null;
			try {
				obj = new JSONObject(str);
			} catch (JSONException e1) {
				e1.printStackTrace();
			} //�������� ����������
			String  str1 = set.isEmpty() ?  new String (sPref.getString(main + "mod", "")) : new String (sPref.getString(set + "mod", ""));
			ArrayList<MODIFICATOR> mods = new ArrayList<MODIFICATOR>(); //�����������
			if (!(str1.isEmpty() || str1.equals("null") || str1.equals("NO_UPDATES_AVAILABLE"))) {
				try {
					mods = MODIFICATOR.fromJson(new JSONArray(str1), isGroup);
				} catch (JSONException e) {
					e.printStackTrace();
				} 
			}
			calendar.add(Calendar.DAY_OF_YEAR, -1); //����� ��� ���������� ���� ���� �� ��������
			ArrayList<ShModel> list = new ArrayList<ShModel>(); //�������� ���� ��� ����������� ������

			for (int d = 0; d < count; d++) { //���� ��� count ���� ��� ����������� ����������
				calendar.add(Calendar.DAY_OF_YEAR, 1); //���������� ��� ������� ������������ ���
				mDate = new String();	 //����������� ���� ��� ������ ��� � ��� ��������� �����������
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

				ArrayList<PAIR> dayList = null;
				if (obj.has(day)) { //���� ������ ��� � ���������� �� ���������� - �������� ���� �������
					try {
						dayList = PAIR.fromJson(obj.getJSONArray(day), isGroup);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				ArrayList<PAIR> dayMods = null;
				for (int i = 0; i < mods.size(); i++) { //���� ���� ����������� - ������ ��, ���� ���� ��� ��� � ����������
					if (mods.get(i).DATE.equals(mDate)) {
						dayMods = mods.get(i).MOD;
						continue;
					}
				}
				mDate += ", " + day1; //�������� ���������� ��� ��� ������ ���
				ArrayList<ShModel> modSched = new ArrayList<ShModel>(); //��� �����������
				ArrayList<ShModel> mainSched = new ArrayList<ShModel>(); //��� ���������
				if (dayList != null) {//���� �������� ���������� ����, ��������� �� ���� ����
					mainSched = list(dayList, isGroup);
				}
				if (dayMods != null) {//���� ��� ���������� ����, ��������� �� ���� ����
					modSched = list(dayMods, isGroup);
				}

				int i = 0, j = 0;
				boolean firstPair = true;
				while (i < mainSched.size() && j < modSched.size()) {
					if (Integer.parseInt(mainSched.get(i).getN()) ==  Integer.parseInt(modSched.get(j).getN())) {
						//���� �����, ���� ���� � �������� � � �����������
						if (modSched.get(j).isCancelled()) { //��������, ���� ��������. ����� �� �� ������� � ������ (�� ������, ���� �������� � ������� �� � ����)

							if (!isGroup && mainSched.get(i).getTeacher().length() == modSched.get(j).getTeacher().length() || isGroup) {
								//���� ���������� ��� ������ ��� ���������� ��� �������������, �� ����� ������ ����� ���������, ��� ������. �� ������� ����
								i++; j++;
								continue;
							} else {
								//���� ���������� ���, ������ ������ ���, ���� ��� � ������������ �� ������
								if (firstPair) { //���� ��� ������ ������ ��� ���, �������� � ��� ����
									mainSched.get(i).setDate(mDate);
									firstPair = false;
								}
								ArrayList<String> mainGroups = new ArrayList<String>();
								int simbol = 0;
								for (int len = 0; len < (mainSched.get(i).getTeacher().length() - 4) / 6 + 1; len++) { //������ ������ �������� 4 �������, ��������� �� 6. ������� ������ ����� �� ���������
									mainGroups.add(mainSched.get(i).getTeacher().substring(simbol, simbol + 4));
									simbol += 6;
								}
								simbol = 0;
								for (int len = 0; len < (modSched.get(j).getTeacher().length() - 4) / 6 + 1; len++) { //�������� �� ������ ��, ��� ���� � ��������������
									mainGroups.remove(modSched.get(j).getTeacher().substring(simbol, simbol + 4));
									simbol += 6;
								}
								//�������� ���������� ������ ����� ��������� ����������
								String teacher = new String();
								for (int q = 0; q < mainGroups.size(); q++) { //�� ����, ���� ����� ����� teacher ��������� �������, ��� ��������� � ����� ������� ��������
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
						if (!mainSched.get(i).getName().equals(modSched.get(j).getName())) {//���� �������� �� ���������, ������� ������ �������� ����
							if (firstPair) { //���� ��� ������ ������ ��� ���, �������� � ��� ����
								modSched.get(j).setDate(mDate);
								firstPair = false;
							}
							list.add(mainSched.get(i));
							i++;
						} else {
							// ��������, ��� ������ � �������������. ���������������� ������
							if (!isGroup && mainSched.get(i).getTeacher().length() == modSched.get(j).getTeacher().length() || isGroup) {
								//���� �������. ���� ���������� ��� ������ ��� ���������� ��� �������������, �� ����� ������ ����� ���������, ��� ������. ������� � �������� ���� �� �����������
								if (i == 0 && j == 0) { //���� ��� ������ ������ ��� ���, �������� � ��� ����
									modSched.get(j).setDate(mDate);
								}
								list.add(modSched.get(j));
							} else
							{ //���� ���������� ���, ������ ���, ��� ���� � ������������, ����� �������� ����������� ���, ���� ��� ���
								if (firstPair) { //���� ��� ������ ������ ��� ���, �������� � ��� ����
									modSched.get(j).setDate(mDate);
									firstPair = false;
								}
								list.add(modSched.get(j));
								ArrayList<String> mainGroups = new ArrayList<String>();
								int simbol = 0;
								for (int len = 0; len < (mainSched.get(i).getTeacher().length() - 4) / 6 + 1; len++) { //������ ������ �������� 4 �������, ��������� �� 6. ������� ������ ����� �� ���������
									mainGroups.add(mainSched.get(i).getTeacher().substring(simbol, simbol + 4));
									simbol += 6;
								}
								for (int len = 0; len < (modSched.get(j).getTeacher().length() - 4) / 6 + 1; len++) { //�������� �� ������ ��, ��� ���� � ��������������
									mainGroups.remove(modSched.get(j).getTeacher().substring(simbol, simbol + 4));
									simbol += 6;
								}
								//�������� ���������� ������ ����� ��������� ����������
								String teacher = new String();
								for (int q = 0; q < mainGroups.size(); q++) { //�� ����, ���� ����� ����� teacher ��������� �������, ��� ��������� � ����� ������� ��������
									teacher += mainGroups.get(q);
									if (q != mainGroups.size()-1) {
										teacher += ", ";
									}
								}
								mainSched.get(i).setTeacher(teacher);
								if (!teacher.isEmpty()) { //�� ����, ���� ����� ����� teacher ��������� �������, ��� ��������� � ����� ������� �������� 
									if (firstPair) { //���� ��� ������ ������ ��� ���, �������� � ��� ����
										mainSched.get(i).setDate(mDate);
										firstPair = false;
									}
									list.add(mainSched.get(i));
								}
							}
							//�� ������������ ��� ���� ��������:
							i++; j++;
						}
					} else if (Integer.parseInt(mainSched.get(i).getN()) <  Integer.parseInt(modSched.get(j).getN())) {
						//���� ���� ������ �������, ��������� ������ ��� ����������, ������ � ���������� ������ ��� ���
						if (firstPair) { //���� ��� ������ ������ ��� ���, �������� � ��� ����
							mainSched.get(i).setDate(mDate);
							firstPair = false;
						}
						list.add(mainSched.get(i));
						i++;
					} else if (Integer.parseInt(mainSched.get(i).getN()) >  Integer.parseInt(modSched.get(j).getN())) {
						//���� ���� ������ �������, ��������� ������ ��� ����������, ������ � ���������� ������ ��� ���
						if (firstPair) { //���� ��� ������ ������ ��� ���, �������� � ��� ����
							modSched.get(j).setDate(mDate);
							firstPair = false;
						}
						list.add(modSched.get(j));
						j++;
					}
				}
				//����������, ����� �� ������� ����������. ������ ������ �� ����
				for (; i < mainSched.size(); i++) {
					if (firstPair) { //���� ��� ������ ������ ��� ���, �������� � ��� ����
						mainSched.get(i).setDate(mDate);
						firstPair = false;
					}
					list.add(mainSched.get(i));
				}
				for (; j < modSched.size(); j++) {
					if (firstPair) { //���� ��� ������ ������ ��� ���, �������� � ��� ����
						modSched.get(j).setDate(mDate);
						firstPair = false;
					}
					list.add(modSched.get(j));
				}
			}

			return list;
		}		

		public ArrayList<ShModel> list(ArrayList<PAIR> dayList, boolean isGroup) { //����� �� ����������� ���. ���������� ��� ������� ��� �������� ��� ���������� � �����������, �������� 2 ������
			ArrayList<ShModel> list = new ArrayList<ShModel>(); //�������� ���� ��� ����������� ������
			for (int i = 0; i < dayList.size(); ++i) { //���������� ���������� ��� ����������� ���
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
						date,
						dayList.get(i).IS_CANCELED);
				list.add(item);
				i+=skip;
			}
			return list;		
		}


		@Override
		protected void onPostExecute(ArrayList<ShModel> data) {
			if (data.isEmpty()) {
				Toast.makeText(getActivity(), "������ �����������", Toast.LENGTH_SHORT).show();
				return;
			}
			adapter.add(data);
			adapter.notifyDataSetChanged();
			int index = lw.getFirstVisiblePosition();
			int top = (lw.getChildAt(0) == null) ? 0 : lw.getChildAt(0).getTop();
			lw.setSelectionFromTop(index, top);
		}
	}

}
