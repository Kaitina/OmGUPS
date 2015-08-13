package com.example.omgups;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.widget.Toast;
/** ласс дл€ выполнени€ действий в отдельном потоке.
 * ѕрин€тие Json'а со списком
 */
public class GetListTask extends AsyncTask<MenuItem, Void, Boolean> {
	Context context;
	SharedPreferences sPref;
	String item_list = null;
	String takenJson = "";
	final int TIMEOUT_MILLISEC = 5000;
	static InputStream is = null;
	Date lastModifiedDate;
	boolean current = false;
	MenuItem item;

	public GetListTask(Context context) {
		super();
		this.context = context;		
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

	}

	@Override
	protected Boolean doInBackground(MenuItem... params) {
		if (params.length != 0) {
			item = params[0];
		}
		//¬озвращать false, если изменений нет
		sPref = context.getSharedPreferences("item_list", Context.MODE_PRIVATE);
		Editor ed = sPref.edit();
		String dateStr = new String(sPref.getString("DATE", ""));
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault());
		Date date = null;
		try {
			date = (Date)formatter.parse(dateStr); //хран€ща€с€ дата
		} catch (ParseException e1) {
			e1.printStackTrace();
			dateStr = "20000101000000";
			try {
				date = (Date)formatter.parse(dateStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		String uri = context.getResources().getString(R.string.uri);
		URL url;
		try {
			url = new URL(uri + "getLists" + "?last_refresh_dt=" + dateStr);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(TIMEOUT_MILLISEC);
			InputStream inputStream = urlConnection.getInputStream();
			StringBuffer buffer = new StringBuffer();
			String lastModified = urlConnection.getHeaderField("Last-modified");
			lastModifiedDate = (Date)formatter.parse(lastModified); //ƒата с сервера
			if (!dateStr.equals("")) {
				if (lastModifiedDate.getTime() == (date.getTime())) {
					current = true;
					//≈сли даты совпадают, изменений не требуетс€
					return false;
				}
			}
			ed.putString("DATE",lastModified).apply(); //≈сли не совпадают, занести новую дату

			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "CP-1251")); 
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			} 
			takenJson = buffer.toString();
			urlConnection.disconnect();
			if (takenJson.isEmpty()) { //≈сли после всех операций все равно пустой	
				return false;
			}
			globalParse();
		} catch (Exception e){
			return false;
		}

		return true;
	}

	@Override
	protected void onPostExecute(Boolean data) { //ѕредупреждение, если список пустой
		if (!data && !current) {
			if (item != null) {
				item.setVisible(false);
			}
			Toast.makeText(context, "Ќе удалось получить списки" + '\n'
					+ "¬озможно, сервер недоступен", Toast.LENGTH_LONG).show();
		} else {
			if (item != null) {
				item.setActionView(R.layout.actionbar_finish); //¬ случае успешной загрузки показать галочку на месте progressbar, через секунду скрыть
				new CountDownTimer(1000, 1000) {
					public void onTick(long millisUntilFinished) {}
					public void onFinish() { 
						item.setVisible(false);
					}
				}.start();
			}
		}
	}

	private void globalParse() throws JSONException { //разбиение порт€нки на крупные составл€ющие
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