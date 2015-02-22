package com.example.omgups;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
/**
 * ����� ��� ��������� ����� ����������
 * ���������� �� ������� ������� ����������,
 * ��� ������ ����� ������ � ���������
 */
public class GetScheduleTask extends AsyncTask<String, Void, Integer> {
	Context context;
	SharedPreferences sPref;
	final int TIMEOUT_MILLISEC = 5000;
	Date lastModifiedDate;
	static InputStream is = null;
	String takenJson = "";
	int count = 0; //��� ����������� ���������

	public GetScheduleTask(Context context) {
		super();
		this.context = context;
	}


	@Override
	protected Integer doInBackground(String... params) {
		//���������� false, ���� ��������� ���
			sPref = context.getSharedPreferences("groups", Context.MODE_PRIVATE);
			Editor ed = sPref.edit();
//			String dateStr = new String(sPref.getString("DATE", ""));
			String dateStr = "";
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			Date date = null;
			try {
				date = (Date)formatter.parse(dateStr); //���������� ����
			} catch (ParseException e1) {}
			HttpParams httpParams = new BasicHttpParams(); //��������� ��������� �����������
			HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
			HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC); //��������� ��������
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);

			//����� ����, �����������, ���� ���������� (����� ������ �������)
			String parameters = new String("?");
			for (String p : params) { //��������� ������ ���������� ����� �����������
				if (p.charAt(0) == 'g') {
					parameters += "id_user=" + p.replace("g", "") + "&";
				}
				if (p.charAt(0) == 't') {
					parameters += "id_user=" + p.replace("t", "") + "&";
				}
			}
			String uri = context.getResources().getString(R.string.uri);
			String url = uri + "getSchedule" + parameters.replaceFirst("&$", ""); //���� �� �������, ����� �������� ���������
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader("If-Modified-Since", dateStr);
			HttpResponse httpResponse = null;
			try {
				httpResponse = httpClient.execute(httpGet); //�������� ������
				String lastModified = httpResponse.getFirstHeader("Last-modified").toString().replaceFirst("Last-Modified: ", ""); //������� ����� ���������� ���������
								try {
									lastModifiedDate = (Date)formatter.parse(lastModified);
								} catch (ParseException e) {} //���� � �������
//								if (!dateStr.equals("")) {
//									Log.d("11", lastModifiedDate.toString());
//									if (lastModifiedDate.getTime() == (date.getTime())) {
//										//���� ���� ���������, ��������� �� ���������
//										return 0;
//									}
//								}
								ed.putString("DATE",lastModified).apply(); //���� �� ���������, ������� ����� ����
				HttpEntity httpEntity = httpResponse.getEntity(); //������� ������
				is = httpEntity.getContent();
				BufferedReader reader = null;
				reader = new BufferedReader(new InputStreamReader(is, "CP-1251"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				takenJson = sb.toString(); //�������� ������ � ������������ ���
				if (takenJson.isEmpty()) { //���� ����� ���� �������� ��� ����� ������	
					return -1;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return -1;
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}

			try {
				globalParse();
			} catch (JSONException e) {
				e.printStackTrace();
				return -1;
			}
			return count;
	}

	@Override
	protected void onPostExecute(Integer data) { //��������������, ���� ������ ������
		if (data == -1)
			Toast.makeText(context, "�� ������� �������� ������" + '\n'
					+ "��������, ���������� ������", Toast.LENGTH_LONG).show();
	}


	private void globalParse() throws JSONException { //��������� ���������� �� ������� ������������
		boolean mainMain = false, mainAdd = false, modMain = false, modAdd = false; //������ - ��� �����������, ������ - ��� ������
		sPref = context.getSharedPreferences("groups", Context.MODE_PRIVATE);
		Editor ed = sPref.edit();
		JSONArray array = null;
		array = new JSONArray(takenJson);
		String groupName = new String();
		LinkedHashSet<String> newNames = new LinkedHashSet<String>(); //������ ���
		LinkedHashSet<String> newTeachers = new LinkedHashSet<String>(); //������ ���
		String[] days = {"ODD_MONDAY","ODD_TUESDAY","ODD_WEDNESDAY","ODD_THURSDAY","ODD_FRIDAY","ODD_SATURDAY",
				"EVEN_MONDAY","EVEN_TUESDAY","EVEN_WEDNESDAY","EVEN_THURSDAY","EVEN_FRIDAY","EVEN_SATURDAY"};
		for (int i = 0; i < array.length(); i++) //�������� ��� �������������� ��������
		{
			groupName = array.getJSONObject(i).get("SCHEDULE_NAME").toString();		
			if (!array.getJSONObject(i).get("SCHEDULE_MAIN").toString().equals("NO_UPDATES_AVAILABLE")) {
				ed.putString(groupName + "main",array.getJSONObject(i).getString("SCHEDULE_MAIN")).apply();
				//���� ���� ��������� � �������� ����������, �������� ���
				if (groupName.equals(sPref.getString("main_group", ""))) {
					mainMain = true; //��� ����������. ����������� �������� ��������� ��� ������� ������
				}
				if (!groupName.equals(sPref.getString("main_group", ""))) {
					mainAdd = true; //��� ����������. ����������� �������� ��������� ��� �������������� �����
				}
				
				//����, ����������� ������ ��� ��� �������� �������� � ��������������
				for (int j = 0; j < days.length; j++) {
					if (array.getJSONObject(i).getJSONObject("SCHEDULE_MAIN").has(days[j])) {
					JSONArray arr = array.getJSONObject(i).getJSONObject("SCHEDULE_MAIN").getJSONArray(days[j]);
					for (int k = 0; k < arr.length(); k++) {
						try {
							Integer.parseInt(groupName.charAt(0) + "");
							newTeachers.add(arr.getJSONObject(k).getString("TEACHER_NAME")); //�������� �������� - ������ ������ �����, ������� ������
					    } catch (NumberFormatException e) {
					    	newTeachers.add(arr.getJSONObject(k).getString("GROUP_NAME")); //�������� �� ��������, ������ ������ �� �����, ������ - �������������
					    }
						newNames.add(arr.getJSONObject(k).getString("DISCIPLINE"));
					}
				}
				}
				
			}
			if (!array.getJSONObject(i).get("SCHEDULE_MOD").toString().equals("NO_UPDATES_AVAILABLE")) {
				ed.putString(groupName + "mod",array.getJSONObject(i).getString("SCHEDULE_MOD")).apply();
				//���� ���� ��������� � ������������, ���������� ��
				if (groupName.equals(sPref.getString("main_group", ""))) {
					modMain = true; //��� ����������. ����������� ���. ��������� ��� ������� ������
				}
				if (!groupName.equals(sPref.getString("main_group", ""))) {
					modAdd = true; //��� ����������. ����������� ���. ��������� ��� �������������� �����
				}
			}
		}
		if (mainMain) {
			count +=1000;
		}
		if (mainAdd) {
			count +=100;
		}
		if (modMain) {
			count +=10;
		}
		if (modAdd) {
			count +=1;
		}
		compare ("pair_names", newNames);
		compare ("pair_teachers", newTeachers);
	}
	
	private void compare(String pref, LinkedHashSet<String> newNames) { //���� ��������� ������ ����������� ��� � ������� ������� ���
		Editor ed = sPref.edit();
		if (sPref.contains(pref)) {
			Set<String> oldNames = sPref.getStringSet(pref, new LinkedHashSet<String>());
			String[] oldN = {};
			oldN = oldNames.toArray(new String[oldNames.size()]);			
			ArrayList<Integer> num = new ArrayList<Integer>();
			for (int i = 0; i < oldNames.size(); i++) { //������� ��� ���������� ��������
				if (!newNames.contains(oldN[i]) || oldN[i].equals("")) {
					oldN[i] = "";
					num.add(i);
				} else { //� ��� ������������� �� ������ ������
					newNames.remove(oldN[i]);
				}
			}
			String[] newN = {};
			newN = newNames.toArray(new String[newNames.size()]);
			int i = 0;
			for (; i < newNames.size(); i++) {
				if (i > num.size()) { //��� ���������� ������ ��������� ����� ��������� ����
					continue;
				} else {
					oldN[num.get(i)] = newN[i];
				}
			}
			LinkedHashSet<String> pairNames = new LinkedHashSet<String>();
			for (int j =0; j < oldN.length; j++) {
				pairNames.add(oldN[j]);
			}
			if (i > num.size()) {//����, ������������� ��� ����������
				for (; i < num.size(); i++) {
				pairNames.add(newN[i]);
				}
			}
			//������ ��� ������ � ��������
			ed.putStringSet(pref, pairNames).apply();;
		} else {//���� �� ������������, ������ ��������� ��� ����
			ed.putStringSet(pref, newNames).apply();;
		}
	}
}