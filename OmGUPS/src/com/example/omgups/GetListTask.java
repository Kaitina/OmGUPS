package com.example.omgups;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;


import org.json.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.widget.Toast;
/**����� ��� ���������� �������� � ��������� ������.
 * �������� Json'� �� �������
 */
public class GetListTask extends AsyncTask<Void, Void, Boolean> {
	static Context context;
	SharedPreferences sPref;
	String item_list = null;
	String takenJson = "";
	final int TIMEOUT_MILLISEC = 5000;
	static InputStream is = null;
	private static final String LOG_TAG = "myLogs";
	Date lastModifiedDate;
	boolean current = false;

	public GetListTask(Context context) {
		super();
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		//���������� false, ���� ��������� ���
			sPref = context.getSharedPreferences("item_list", Context.MODE_PRIVATE);
			Editor ed = sPref.edit();
//			String dateStr = new String(sPref.getString("DATE", ""));
			String dateStr = "";
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			Date date = null;
			try {
				date = (Date)formatter.parse(dateStr); //���������� ����
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			HttpParams httpParams = new BasicHttpParams(); //��������� ��������� �����������
			HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
			HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC); //��������� ��������
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
			String uri = context.getResources().getString(R.string.uri);
			String url = uri + "getLists";
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader("If-Modified-Since", dateStr);
			HttpResponse httpResponse = null;
			try {
				httpResponse = httpClient.execute(httpGet); //�������� ������
				String lastModified = httpResponse.getFirstHeader("Last-modified").toString().replaceFirst("Last-Modified: ", ""); //������� ����� ���������� ���������
				lastModifiedDate = (Date)formatter.parse(lastModified); //���� � �������
//				if (!dateStr.equals("")) {
//					if (lastModifiedDate.getTime() == (date.getTime())) {
//						current = true;
//						//���� ���� ���������, ��������� �� ���������
//						return false;
//					}
//				}
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
					return false;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			try {
				globalParse();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			return true;		
	}

	@Override
	protected void onPostExecute(Boolean data) { //��������������, ���� ������ ������
		if (!data && !current)
			Toast.makeText(context, "�� ������� �������� ������" + '\n'
					+ "��������, ������ ����������", Toast.LENGTH_LONG).show();
	}

//	static public boolean isNetworkConnected() {
//		Log.d(LOG_TAG, "11");
//		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
//		Log.d(LOG_TAG, "12");
//		NetworkInfo ni = cm.getActiveNetworkInfo();
//		Log.d(LOG_TAG, "13");
//		if (ni == null) {
//			// There are no active networks.
//			return false;
//		} else
//			return true;
//	}

	private void globalParse() throws JSONException { //��������� �������� �� ������� ������������
		JSONObject obj = null;
		obj = new JSONObject(takenJson);
		sPref = context.getSharedPreferences("item_list", Context.MODE_PRIVATE);
		Editor ed = sPref.edit();
		
		if (!obj.get("FACULTIES").toString().equals("NO_UPDATE_AVAILABLE"))
			ed.putString("FACULTIES",obj.getString("FACULTIES"));
		if (!obj.get("DEPARTMENTS").toString().equals("NO_UPDATE_AVAILABLE"))
			ed.putString("DEPARTMENTS",obj.getString("DEPARTMENTS"));
		if (!obj.get("GROUPS").toString().equals("NO_UPDATE_AVAILABLE"))
			ed.putString("GROUPS",obj.getString("GROUPS"));
		if (!obj.get("TEACHERS").toString().equals("NO_UPDATE_AVAILABLE"))
			ed.putString("TEACHERS",obj.getString("TEACHERS"));
		ed.apply();
	}
}