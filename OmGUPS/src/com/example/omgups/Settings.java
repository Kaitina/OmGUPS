package com.example.omgups;

import android.R.color;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
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
	AsyncTask<Void, Void, Boolean> glt;
	ListView settints;
	Boolean reload = false;


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
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

		
		//��� ���� �� ����� ����
		settints.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0: //������� ������� - ����� �����.
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
					}
					if (sPref.contains("DEPARTMENTS")) { //���� ��������� ������� ��� ���� ����������
						settints.setBackgroundColor(Color.WHITE); //����������� ���������� �������
						settints.getChildAt(lastPosition).setBackgroundColor(color.white);
						lastPosition = position;
						if (getResources().getConfiguration().orientation ==
								Configuration.ORIENTATION_LANDSCAPE) {
							settints.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.list_select));
						}
						//��������� ����������/�������� ��� ������ ������
						if (getResources().getConfiguration().orientation ==
								Configuration.ORIENTATION_PORTRAIT) {
							Intent intent = new Intent(getActivity(), MainGroup.class);
							startActivity(intent);		
						}
						else {
							ft = getFragmentManager().beginTransaction();
							ft.replace(R.id.frgmCont, new MainGroupFragment()).commit();
						}
					}
					break;

//				case 1: //������� 1 - �������������� ������.
//					sPref = getActivity().getSharedPreferences("item_list", Context.MODE_PRIVATE);
//					if(!sPref.contains("DEPARTMENTS")) { //���� �� ���������� ���� ����� (���� �� ���������� ����)
//						//��������� task � ���������
//						if (SideBar.isNetworkConnected(getActivity())) {
//							glt = new GetListTask(getActivity()); //������������ ��� ��������� �������
//							glt.execute();
//						}
//						else {
//							Toast.makeText(getActivity(), "�� ������� �������� ������" + '\n'
//									+ "��������� ���������� � ����������", Toast.LENGTH_LONG).show();
//						}
//					}
//					if (sPref.contains("DEPARTMENTS")) { //���� ��������� ������� ��� ���� ����������
//						settints.setBackgroundColor(Color.WHITE); //����������� ���������� �������
//						settints.getChildAt(lastPosition).setBackgroundColor(color.white);
//						lastPosition = position;
//						if (getResources().getConfiguration().orientation ==
//								Configuration.ORIENTATION_LANDSCAPE) {
//							settints.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.list_select));
//						}
//						//��������� ����������/�������� ��� ������ �����
//						if (getResources().getConfiguration().orientation ==
//								Configuration.ORIENTATION_PORTRAIT) {
//							Intent intent = new Intent(getActivity(), AdditionalGroup.class);
//							startActivity(intent);		
//						}
//						else {
//							ft = getFragmentManager().beginTransaction();
//							ft.replace(R.id.frgmCont, new AdditionalGroupFragment()).commit();
//						}
//					}	
//					break;
				case 1: //������� 1 - ???
					settints.setBackgroundColor(Color.WHITE); //����������� ���������� �������
					settints.getChildAt(lastPosition).setBackgroundColor(color.white);
					lastPosition = position;
					if (getResources().getConfiguration().orientation ==
							Configuration.ORIENTATION_LANDSCAPE) {
						settints.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.list_select));
					}
					break;
				case 2: //������� 2 - ��������� �������������
					settints.setBackgroundColor(Color.WHITE); //����������� ���������� �������
					settints.getChildAt(lastPosition).setBackgroundColor(color.white);
					lastPosition = position;
					if (getResources().getConfiguration().orientation ==
							Configuration.ORIENTATION_LANDSCAPE) {
						settints.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.list_select));
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
						settints.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.list_select));
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


}


