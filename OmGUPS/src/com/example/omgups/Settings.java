package com.example.omgups;

import android.R.color;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
/**�������� - ���� � ���������� ��������
 */
public class Settings extends Fragment{

	String[] items;
	String item_list;
	FragmentTransaction ft;
	FrameLayout cont;
	SharedPreferences sPref;
	Object item;
	int lastPosition;
	AsyncTask<MenuItem, Void, Boolean> glt;
	ListView settints;
	Boolean reload = false;
	MenuItem refreshItem;
	Fragment groups;


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		setRetainInstance(true); //������ �� ������������ �������
		// ����������� ���������
		View view = inflater.inflate(R.layout.fragment_settings, null);	
		items = getResources().getStringArray(R.array.settings_array);
		settints = (ListView)view.findViewById(R.id.settingsView);
		if (getResources().getConfiguration().orientation ==
				Configuration.ORIENTATION_LANDSCAPE) {
			cont = (FrameLayout)view.findViewById(R.id.frgmCont);
			if (cont != null && !reload) {
				ft = getFragmentManager().beginTransaction();
				ft.add(R.id.frgmCont, new SecondFragment()).commit();
				reload = true;
			}
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.settings_item, items);
		settints.setAdapter(adapter);

		groups = new MainGroupFragment();
		//��� ���� �� ����� ����
		settints.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0: //������� ������� - ����� �����.					
					sPref = getActivity().getSharedPreferences("item_list", Context.MODE_PRIVATE);
					if(!sPref.contains("DEPARTMENTS")) { //���� �� ���������� ���� ����� (���� �� ���������� ����)
						refreshItem.setActionView(R.layout.actionbar_progress);
						refreshItem.setVisible(true);
						//��������� task � ���������
						if (isNetworkConnected(getActivity())) {
							glt = new GetListTask(getActivity()); //������������ ��� ��������� �������
							glt.execute(refreshItem);
							// ����� �������� ������ ����������: ������������� UI, ��������� ������ �������, ������� ������. ������ �������� � ����, ����� ���������� ����� ������ �� ������
							//							try {
							//								if (glt.get(7, TimeUnit.SECONDS)) {}
							//							} catch (Exception e) {
							//								e.printStackTrace();
							//							}
						}
						else {
							Toast.makeText(getActivity(), "�� ������� �������� ������" + '\n'
									+ "��������� ���������� � ����������", Toast.LENGTH_LONG).show();
						}
					}
					if (sPref.contains("DEPARTMENTS")) { //���� ��������� ������� ��� ���� ����������
						settints.setBackgroundColor(Color.WHITE); //����������� ���������� �������
						settints.getChildAt(lastPosition).setBackgroundColor(color.white);
						lastPosition = position;
						if (getResources().getConfiguration().orientation ==
								Configuration.ORIENTATION_LANDSCAPE) {
							settints.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.schedule_light));
						}
						//��������� ����������/�������� ��� ������ ������
						if (getResources().getConfiguration().orientation ==
								Configuration.ORIENTATION_PORTRAIT) {
							Intent intent = new Intent(getActivity(), MainGroup.class);
							startActivity(intent);		
						}
						else {
							ft = getFragmentManager().beginTransaction();
							ft.replace(R.id.frgmCont, groups).commit();
						}
					}
					break;
				case 1: //������� 1 - ???
					settints.setBackgroundColor(Color.WHITE); //����������� ���������� �������
					settints.getChildAt(lastPosition).setBackgroundColor(color.white);
					lastPosition = position;
					if (getResources().getConfiguration().orientation ==
							Configuration.ORIENTATION_LANDSCAPE) {
						settints.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.schedule_light));
					}
					break;
				case 2: //������� 2 - ��������� �������������
					settints.setBackgroundColor(Color.WHITE); //����������� ���������� �������
					settints.getChildAt(lastPosition).setBackgroundColor(color.white);
					lastPosition = position;
					if (getResources().getConfiguration().orientation ==
							Configuration.ORIENTATION_LANDSCAPE) {
						settints.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.schedule_light));
					}
					if (getResources().getConfiguration().orientation ==
							Configuration.ORIENTATION_PORTRAIT) {
						Intent intent = new Intent(getActivity(), Timing.class);
						startActivity(intent);		
					}
					else {
						ft = getFragmentManager().beginTransaction();
						ft.replace(R.id.frgmCont, new TimingFragment()).commit();
					}
					break;
				case 3: //������� 3 - ��������� ������������ (��� ����������)
					settints.setBackgroundColor(Color.WHITE); //����������� ���������� �������
					settints.getChildAt(lastPosition).setBackgroundColor(color.white);
					lastPosition = position;
					if (getResources().getConfiguration().orientation ==
							Configuration.ORIENTATION_LANDSCAPE) {
						settints.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.schedule_light));
					}
					if (getResources().getConfiguration().orientation ==
							Configuration.ORIENTATION_PORTRAIT) {
						Intent intent = new Intent(getActivity(), Vitalization.class);
						startActivity(intent);		
					}
					else {
						ft = getFragmentManager().beginTransaction();
						ft.replace(R.id.frgmCont, new VitalizationFragment()).commit();
					}
				default:
					break;
				}
			}
		});
		return view;
	}

	public boolean isNetworkConnected(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.settings, menu);
		refreshItem = (MenuItem) menu.findItem(R.id.download_pb);
		refreshItem.setActionView(R.layout.actionbar_progress);
		refreshItem.setVisible(false);
		super.onCreateOptionsMenu(menu, inflater);
	}



	protected String pullList () {
		try {	
			//������� ������ �� xml
			sPref = getActivity().getSharedPreferences("item_list", Context.MODE_PRIVATE);
			if(sPref.contains("list")) { //���� ������ ������ ����������

				item_list = sPref.getString("list", "");
			}
			else {
				return "";
			}
			return item_list;
		} catch (Exception e) {
			//���� �� ���������
			return "";
		}
	}	

	@Override
	public void onDestroy() {
		if (glt != null)
			glt.cancel(false);
		ft = getFragmentManager().beginTransaction(); //�������� ��������� �� �������� �����, ���� ��� �������. ������� actionBar �� ��� ���������, ��������� ���������� ������
		ft.remove(groups).commit();
		super.onDestroy();
	}
}