package com.example.omgups;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.example.omgups.Parsers.DEPARTMENTS;
import com.example.omgups.Parsers.FACULTIES;
import com.example.omgups.Parsers.GROUPS;
import com.example.omgups.Parsers.TEACHERS;

public class MainGroupFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
	/** 
	 * ����� ��� ������ �������� ������ � �������������� ����������
	 * ������� 2 ��������� �����, ����� ������� ���� �������, ���� ������
	 * ����� ������, � onDestroy(), ���������� ������ �� ������
	 * ���������� ������ ������� �������� ������ �� �����
	 * �������� ��� ������������ ���������� - MainGroup
	 */

	ExpandableListView list;
	SharedPreferences sPref;
	ExpListAdapter adapter;
	MenuItem refreshItem;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setRetainInstance(true);
		setHasOptionsMenu(true);
		View view = inflater.inflate(R.layout.main_group, container, false);
		list = (ExpandableListView)view.findViewById(R.id.list);

		sPref = getActivity().getSharedPreferences("item_list", Context.MODE_PRIVATE);
		mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
		parse();

		return view;
	}

	@Override
	public void onRefresh() {
		mSwipeRefreshLayout.setRefreshing(true);
		GetListTask glt = new GetListTask(getActivity());
		glt.execute(); //��������� ������ �� ��������� ������ ����������
		// ������ ��������
		boolean result = false;
		try {
			result = glt.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		mSwipeRefreshLayout.setRefreshing(false);
		if (result) {
			//������ ��������� ������
			parse();
		}
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.groups, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		searchView.setQueryHint("�����");
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				adapter.getFilter().filter(query);
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				adapter.getFilter().filter(newText);
				return false;
			}
		});
		refreshItem = (MenuItem) menu.findItem(R.id.download_pb);
		refreshItem.setActionView(R.layout.actionbar_progress);
		refreshItem.setVisible(false);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save:
			save();
			break;
		case R.id.help:
			//�������������
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	private void parse() {
		int faculcySize = 0;
		int departmentSize = 0;
		ArrayList<String> parents = new ArrayList<String>();
		try {  //��������� ������ �����������
			JSONArray arr = new JSONArray(sPref.getString("FACULTIES", ""));
			ArrayList<FACULTIES> FAC = FACULTIES.fromJson(arr);
			faculcySize = FAC.size();
			for (int i = 0; i < faculcySize; i++) {
				parents.add(FAC.get(i).FACULTY_NAME);
			} //� ������
			arr = new JSONArray(sPref.getString("DEPARTMENTS", ""));
			ArrayList<DEPARTMENTS> DEP = DEPARTMENTS.fromJson(arr);
			departmentSize = DEP.size();
			for (int i = 0; i < departmentSize; i++) {
				parents.add(DEP.get(i).DEPARTMENT_NAME);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//������� ���� � ��������
		ArrayList<ArrayList<Model>> models = new ArrayList<ArrayList<Model>>();
		String faculcyName = "";
		String departmentName = "";
		JSONArray arr;
		try {
			arr = new JSONArray(sPref.getString("GROUPS", ""));
			ArrayList<GROUPS> GROUP = GROUPS.fromJson(arr); //�������� ������ �����
			int groupsize = GROUP.size(); //������ ������ ������ �����
			int j = 0;

			for (int i = 0; i < faculcySize; i++) { //��� ������� ����������
				ArrayList<Model> list = new ArrayList<Model>();
				for (; j<groupsize; j++) { //�� ������ �����, ���������� � ������
					if (j == 0) { //� ������ ��� �� �����, �������� ������� � ������
						faculcyName = GROUP.get(j).FACULTY_NAME;
					}
					if (faculcyName.equals(GROUP.get(j).FACULTY_NAME)) {//���� �������� ���������� ��������� � ����������
						list.add(get(GROUP.get(j).GROUP_NAME, "g"+Integer.toString(GROUP.get(j).ID_GROUP)));
					}					
					else { //���� �� ���������, ���� ���������� �� ��������� ���������
						faculcyName = GROUP.get(j).FACULTY_NAME; //���������� �������� ���������� ����������
						models.add(list);
						break; //������� �� ����������� �����
					}
					if (i == faculcySize-1 && j==groupsize-1) {//�� ��������� ��������. ������� ��������� ������ � ������
						models.add(list);
					}
				}
			}
			arr = new JSONArray(sPref.getString("TEACHERS", ""));
			ArrayList<TEACHERS> TEACHER = TEACHERS.fromJson(arr); //�������� ������ �����
			groupsize = TEACHER.size(); //������ ������ ������ �����
			j = 0;
			for (int i = 0; i < departmentSize; i++) { //��� ������� ����������
				ArrayList<Model> list = new ArrayList<Model>();
				for (; j<groupsize; j++) { //�� ������ �����, ���������� � ������
					if (j == 0) { //� ������ ��� �� �����, �������� ������� � ������
						departmentName = TEACHER.get(j).DEPARTMENT_NAME;
					}
					if (departmentName.equals(TEACHER.get(j).DEPARTMENT_NAME)) {//���� �������� ���������� ��������� � ����������
						list.add(get(TEACHER.get(j).TEACHER_NAME, "g"+Integer.toString(TEACHER.get(j).ID_TEACHER)));
					}					
					else { //���� �� ���������, ���� ���������� �� ��������� ���������
						departmentName = TEACHER.get(j).DEPARTMENT_NAME; //���������� �������� ���������� ����������
						models.add(list);
						break; //������� �� ����������� �����
					}
					if (i == departmentSize-1 && j==groupsize-1) {//�� ��������� ��������. ������� ��������� ������ � ������
						models.add(list);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		adapter = new ExpListAdapter(getActivity().getApplicationContext(), parents, models);
		list.setAdapter(adapter); //��� ���� �� ����� ����
	}

	private Model get(String name, String id) {
		return new Model(name, id);
	}

	public void save() {
		if (!adapter.groups().isEmpty()) {
			sPref = getActivity().getSharedPreferences("groups", Context.MODE_PRIVATE);
			Editor ed = sPref.edit();

			if (!(adapter.mainGroup() == null)) {
				ed.putString("main_group",adapter.mainGroup().getName()).apply(); //�������� �������� ����� �������� ������
				ed.putString("main_group_id",adapter.mainGroup().getId()).apply();
			}	
			Set<String> list = new HashSet<String>(); //������ �����
			Set<String> listId = new HashSet<String>();
			String ids[] = new String[adapter.groups().size()];
			int j = 0;
			for (int i = 0; i < adapter.groups().size(); i++, j++) {
				list.add(adapter.groups().get(i).getName());
				listId.add(adapter.groups().get(i).getId());
				ids[j] = adapter.groups().get(i).getId();
			}
			if (!sPref.getStringSet("list", new HashSet<String>()).equals(list)) {
				ed.putString("DATE", "20000101000000"); //�������: ��� ������������ ������ ����� �������, ��� ������� ����� ������. ��� ��������� ���������� �������� ����
			}
			ed.remove("list"); //������� ���������� � ������ �������
			ed.putStringSet("list", list).apply();; //������� ������ ���� �������� ����� � xml
			ed.remove("listId");
			ed.putStringSet("listId", listId).apply();;
			AsyncTask<String, Void, Integer> gsht;
			gsht = new GetScheduleTask(getActivity());
			if (SideBar.isNetworkConnected(getActivity())) {
				refreshItem.setActionView(R.layout.actionbar_progress);
				refreshItem.setVisible(true);
				gsht.execute(ids); //��������� ������ �� ��������� ������ ����������
				try {
					if (gsht.get() == -1) {
						refreshItem.setVisible(false);
					} else {		
						refreshItem.setActionView(R.layout.actionbar_finish); //� ������ �������� �������� �������� ������� �� ����� progressbar, ����� ������� ������
						new CountDownTimer(500, 500) {
							public void onTick(long millisUntilFinished) {}
							public void onFinish() { 
								refreshItem.setVisible(false);
							}
						}.start();
					}
				} catch (Exception e) {	} 
			}
			else {
				Toast.makeText(getActivity(), "�� ������� �������� ������" + '\n'
						+ "�������� ���������� � ����������", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}