package com.example.omgups;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.example.omgups.Parsers.DEPARTMENTS;
import com.example.omgups.Parsers.FACULTIES;
import com.example.omgups.Parsers.GROUPS;
import com.example.omgups.Parsers.TEACHERS;

@SuppressWarnings("deprecation")
public class MainGroup extends ActionBarActivity  {
	/** 
	 * ����� ��� ������ �������� ������ � ������������ ����������
	 * ������� 2 ��������� �����, ����� ������� ���� �������, ���� ������
	 * ����� ������, � onDestroy(), ���������� ������ �� ������
	 * ���������� ������ ������� �������� ������ �� �����
	 * �������� ��� �������������� ���������� - MainGroupFragment
	 */

	ExpandableListView list;
	ExpListAdapter adapter;
	SharedPreferences sPref;
	MenuItem refreshItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_group);	    

		list = (ExpandableListView)findViewById(R.id.list);
		sPref = getSharedPreferences("item_list", Context.MODE_PRIVATE);
		parse();

	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.groups, menu);
 
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("�����");
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				MainGroup.this.adapter.getFilter().filter(query);
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				MainGroup.this.adapter.getFilter().filter(newText);
				return false;
			}
		});
		refreshItem = (MenuItem) menu.findItem(R.id.download_pb);
		refreshItem.setActionView(R.layout.actionbar_progress);
		refreshItem.setVisible(false);
        return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.save:
        	save(); break;
        case R.id.help:
        	//�������������
        	break;
        }
        return super.onOptionsItemSelected(item);
    }
 
	public void save() { //���������� ������
		if (!adapter.groups().isEmpty()) {
			sPref = getSharedPreferences("groups", Context.MODE_PRIVATE);
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
			ed.remove("list"); //������� ���������� � ������ �������
			ed.putStringSet("list", list).apply();; //������� ������ ���� �������� ����� � xml
			ed.remove("listId");
			ed.putStringSet("listId", listId).apply();;
			AsyncTask<String, Void, Integer> gsht;
			gsht = new GetScheduleTask(getApplicationContext());
			if (SideBar.isNetworkConnected(getApplicationContext())) {
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
				Toast.makeText(getApplicationContext(), "�� ������� �������� ������" + '\n'
						+ "�������� ���������� � ����������", Toast.LENGTH_LONG).show();
			}			
		}
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
		adapter = new ExpListAdapter(getApplicationContext(), parents, models);
		list.setAdapter(adapter); //��� ���� �� ����� ����

	}

	private Model get(String name, String id) {
		return new Model(name, id);
	}

	@Override
	protected void onDestroy() {
		save();
		super.onDestroy();
	}


}

