package com.example.omgups;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
/**
 * Класс для получения листа расписаний
 * Вызывается из сервиса полного обновления,
 * при выборе новой группы в настройке
 */
public class GetScheduleTask extends AsyncTask<String, Void, Integer> {
	Context context;
	SharedPreferences sPref;
	final int TIMEOUT_MILLISEC = 5000;
	Date lastModifiedDate;
	static InputStream is = null;
	String takenJson = "";
	int count = 0; //Для определения изменений

	public GetScheduleTask(Context context) {
		super();
		this.context = context;
	}


	@Override
	protected Integer doInBackground(String... params) {
		//Возвращать 0, если изменений нет		
		sPref = context.getSharedPreferences("groups", Context.MODE_PRIVATE);
		Editor ed = sPref.edit();
		String dateStr = new String(sPref.getString("DATE", ""));
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault());
		Date date = null;
		try {
			date = (Date)formatter.parse(dateStr); //хранящаяся дата
		} catch (ParseException e1) {
			e1.printStackTrace();
			dateStr = "20000101000000";
			try {
				date = (Date)formatter.parse(dateStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		//Кусок кода, формирующий, куда отправлять (какие данные просить)
		String parameters = new String("?");
		for (String p : params) { //Заполнить строку параметров всеми переданными
			if (p.charAt(0) == 'g') {
				parameters += "id_user=" + p.replace("g", "") + "&";
			}
			if (p.charAt(0) == 't') {
				parameters += "id_user=" + p.replace("t", "") + "&";
			}
		}
		String uri = context.getResources().getString(R.string.uri) + "getSchedule" + parameters.replaceFirst("&$", ""); //Если не единица, нужно добавить параметры
		URL url;
		try {
			url = new URL(uri + "&last_refresh_dt=" + dateStr);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(TIMEOUT_MILLISEC);
			InputStream inputStream = urlConnection.getInputStream();
			StringBuffer buffer = new StringBuffer();
			String lastModified = urlConnection.getHeaderField("Last-modified");
				lastModifiedDate = (Date)formatter.parse(lastModified); //Дата с сервера
				if (!dateStr.equals("")) {
					if (lastModifiedDate.getTime() == (date.getTime())) {
						//Если даты совпадают, изменений не требуется
						return 0;
					}
				}
				ed.putString("DATE",lastModified).apply(); //Если не совпадают, занести новую дату
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "CP-1251")); 
				String line;
				while ((line = reader.readLine()) != null) {
					buffer.append(line);
				} 
				takenJson = buffer.toString();
				urlConnection.disconnect();
				if (takenJson.isEmpty()) { //Если после всех операций все равно пустой	
					return -1;
				}
				globalParse();
			} catch (Exception e){
				return -1;
			}

			return count;
		}
		
		
		
//		sPref = context.getSharedPreferences("groups", Context.MODE_PRIVATE);
//		Editor ed = sPref.edit();
//		String dateStr = new String(sPref.getString("DATE", ""));
//		//			String dateStr = "";
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//		//			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
//		Date date = null;
//		try {
//			date = (Date)formatter.parse(dateStr); //хранящаяся дата
//		} catch (ParseException e1) {
//			e1.printStackTrace();
//			dateStr = "20000101000000";
//			try {
//				date = (Date)formatter.parse(dateStr);
//			} catch (ParseException e) {
//				e.printStackTrace();
//			}
//		}
//		HttpParams httpParams = new BasicHttpParams(); //Настроить параметры подключения
//		HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
//		HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC); //Установка ожидания
//		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
//
//		//Кусок кода, формирующий, куда отправлять (какие данные просить)
//		String parameters = new String("?");
//		for (String p : params) { //Заполнить строку параметров всеми переданными
//			if (p.charAt(0) == 'g') {
//				parameters += "id_user=" + p.replace("g", "") + "&";
//			}
//			if (p.charAt(0) == 't') {
//				parameters += "id_user=" + p.replace("t", "") + "&";
//			}
//		}
//		String uri = context.getResources().getString(R.string.uri);
//		String url = uri + "getSchedule" + parameters.replaceFirst("&$", ""); //Если не единица, нужно добавить параметры
//		url += "&last_refresh_dt=" + dateStr;
//		HttpGet httpGet = new HttpGet(url);
//		//			httpGet.setHeader("If-Modified-Since", dateStr);
//		HttpResponse httpResponse = null;
//		try {
//			httpResponse = httpClient.execute(httpGet); //Получить данные
//			String lastModified = httpResponse.getFirstHeader("Last-modified").toString().replaceFirst("Last-Modified: ", ""); //Считать время последнего изменения
//			try {
//				lastModifiedDate = (Date)formatter.parse(lastModified);
//			} catch (ParseException e) {} //Дата с сервера
//			if (!dateStr.equals("")) {
//				if (lastModifiedDate.getTime() == (date.getTime())) {
//					//Если даты совпадают, изменений не требуется
//					return 0;
//				}
//			}
//			ed.putString("DATE",lastModified).apply(); //Если не совпадают, занести новую дату
//			HttpEntity httpEntity = httpResponse.getEntity(); //Считать данные
//			is = httpEntity.getContent();
//			BufferedReader reader = null;
//			reader = new BufferedReader(new InputStreamReader(is, "CP-1251"), 8);
//			StringBuilder sb = new StringBuilder();
//			String line = null;
//			while ((line = reader.readLine()) != null) {
//				sb.append(line + "\n");
//			}
//			is.close();
//			takenJson = sb.toString(); //Положить данные в удобоваримый вид
//			if (takenJson.isEmpty()) { //Если после всех операций все равно пустой	
//				return -1;
//			}
//		} catch (ClientProtocolException e) {
//			e.printStackTrace();
//			return -1;
//		} catch (IOException e) {
//			e.printStackTrace();
//			return -1;
//		}
//
//		try {
//			globalParse();
//		} catch (JSONException e) {
//			e.printStackTrace();
//			return -1;
//		}
//		return count;
//	}

	@Override
	protected void onPostExecute(Integer data) { //Предупреждение, если список пустой
		if (data == -1)
			Toast.makeText(context, "Не удалось получить списки" + '\n'
					+ "Возможно, недоступен сервер", Toast.LENGTH_LONG).show();
	}


	private void globalParse() throws JSONException { //разбиение расписаний на крупные составляющие
		boolean mainMain = false, mainAdd = false, modMain = false, modAdd = false; //Первое - тип модификации, второе - тип группы
		sPref = context.getSharedPreferences("groups", Context.MODE_PRIVATE);
		Editor ed = sPref.edit();
		JSONArray array = null;
		array = new JSONArray(takenJson);
		String groupName = new String();
		LinkedHashSet<String> newNames = new LinkedHashSet<String>(); //списки пар
		LinkedHashSet<String> newTeachers = new LinkedHashSet<String>(); //списки пар
		String[] days = {"ODD_MONDAY","ODD_TUESDAY","ODD_WEDNESDAY","ODD_THURSDAY","ODD_FRIDAY","ODD_SATURDAY",
				"EVEN_MONDAY","EVEN_TUESDAY","EVEN_WEDNESDAY","EVEN_THURSDAY","EVEN_FRIDAY","EVEN_SATURDAY"};
		for (int i = 0; i < array.length(); i++) //Записать все новополученные элементы
		{
			groupName = array.getJSONObject(i).get("SCHEDULE_NAME").toString();		
			if (!array.getJSONObject(i).get("SCHEDULE_MAIN").toString().equals("NO_UPDATES_AVAILABLE")) {
				ed.putString(groupName + "main",array.getJSONObject(i).getString("SCHEDULE_MAIN")).apply();
				//Если есть изменения в основном расписании, заменить его
				if (groupName.equals(sPref.getString("main_group", ""))) {
					mainMain = true; //Для оповещений. определение ключевых изменений для главной группы
				}
				if (!groupName.equals(sPref.getString("main_group", ""))) {
					mainAdd = true; //Для оповещений. определение ключевых изменений для дополнительных групп
				}

				//Блок, заполняющий списки пар для цветовой разметки и преподавателей
				for (int j = 0; j < days.length; j++) {
					if (array.getJSONObject(i).getJSONObject("SCHEDULE_MAIN").has(days[j])) {
						JSONArray arr = array.getJSONObject(i).getJSONObject("SCHEDULE_MAIN").getJSONArray(days[j]);
						for (int k = 0; k < arr.length(); k++) {
							try {
								Integer.parseInt(groupName.charAt(0) + "");
								newTeachers.add(arr.getJSONObject(k).getString("TEACHER_NAME")); //проверка пройдена - первый символ число, объъект группа
							} catch (NumberFormatException e) {
								newTeachers.add(arr.getJSONObject(k).getString("GROUP_NAME")); //проверка не пройдена, первый символ не число, объект - преподаватель
							}
							newNames.add(arr.getJSONObject(k).getString("DISCIPLINE"));
						}
					}
				}

			}
			if (!array.getJSONObject(i).get("SCHEDULE_MOD").toString().equals("NO_UPDATES_AVAILABLE")) {
				ed.putString(groupName + "mod",array.getJSONObject(i).getString("SCHEDULE_MOD")).apply();
				//Если есть изменения в модификациях, переписать их
				if (groupName.equals(sPref.getString("main_group", ""))) {
					modMain = true; //Для оповещений. определение доп. изменений для главной группы
				}
				if (!groupName.equals(sPref.getString("main_group", ""))) {
					modAdd = true; //Для оповещений. определение доп. изменений для дополнительных групп
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

	private void compare(String pref, LinkedHashSet<String> newNames) { //Блок сравнения списка загруженных пар с прошлым списком пар
		Editor ed = sPref.edit();
		if (sPref.contains(pref)) { //При существовании подобного списка необходимо заменить старые элементы на новые
			Set<String> oldNames = sPref.getStringSet(pref, new LinkedHashSet<String>());
			String[] oldN = {};
			oldN = oldNames.toArray(new String[oldNames.size()]);
			ArrayList<Integer> num = new ArrayList<Integer>();
			for (int i = 0; i < oldNames.size(); i++) { //удалить все устаревшие элементы
				if (!newNames.contains(oldN[i])) {
					oldNames.remove(oldN[i]);
					num.add(i);
				} else { //и все дублирующиеся из нового списка
					newNames.remove(oldN[i]);
				}
			}
			oldN = oldNames.toArray(new String[oldNames.size()]);
			String[] newN = newNames.toArray(new String[newNames.size()]); //Создание списков старых и новых расписаний для поэлементного внесения
			LinkedHashSet<String> pairNames = new LinkedHashSet<String>();
			int oldIteration = 0, newIteration = 0;
			for (int i = 0; i < oldNames.size() + newNames.size(); i++) { //Создание нового актуального списка
				if (num.contains(i)) { //Если в старом списке дыра
					if (newIteration < newN.length) { //Заполнить ее новым значением, если оно есть
						pairNames.add(newN[newIteration]);
						newIteration++;
					}
				} else { //Если есть значение, внести его
					if (oldIteration < oldN.length) { //Если, конечно, значения остались
						pairNames.add(oldN[oldIteration]);
						oldIteration++;
					} else { //Если не остались - дозаполнить новыми
						pairNames.add(newN[newIteration]);
						newIteration++;
					}
				}
			}
			//Списки пар готовы к погрузке
			ed.putStringSet(pref, pairNames).apply();

		} else {//Если не существовало, просто загрузить что есть
			ed.putStringSet(pref, newNames).apply();;
		}

		
	}
}
