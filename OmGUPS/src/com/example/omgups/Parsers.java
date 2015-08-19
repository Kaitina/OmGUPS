package com.example.omgups;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Parsers {
	public static class DEPARTMENTS { //Классы для парсинга: кафедры

		public final int ID_DEPARTMENT;
		public final String FACULTY_NAME;
		public final String DEPARTMENT_NAME;		

		public DEPARTMENTS(int ID_DEPARTMENT, String FACULTY_NAME, String DEPARTMENT_NAME) {
			this.ID_DEPARTMENT = ID_DEPARTMENT;
			this.DEPARTMENT_NAME = DEPARTMENT_NAME;
			this.FACULTY_NAME = FACULTY_NAME;
		}

		public static DEPARTMENTS fromJson(final JSONObject object) {
			final int ID_DEPARTMENT= object.optInt("ID_DEPARTMENT", 0);			
			final String DEPARTMENT_NAME = object.optString("DEPARTMENT_NAME", "");
			final String FACULTY_NAME = object.optString("FACULTY_NAME", "");
			return new DEPARTMENTS(ID_DEPARTMENT,FACULTY_NAME,DEPARTMENT_NAME);
		}

		public static ArrayList<DEPARTMENTS> fromJson(final JSONArray array) {
			final ArrayList<DEPARTMENTS> DEPARTMENTS = new ArrayList<DEPARTMENTS>();
			for (int index = 0; index < array.length(); ++index) {
				try {
					final DEPARTMENTS DEPARTMENT = fromJson(array.getJSONObject(index));
					if (null != DEPARTMENT) DEPARTMENTS.add(DEPARTMENT);
				} catch (final JSONException ignored) {
				}
			}
			return DEPARTMENTS;
		}
	}

	public static class FACULTIES { //Классы для парсинга: факультеты

		public final int ID_FACULTY;
		public final String FACULTY_NAME;

		public FACULTIES(int ID_FACULTY, String FACULTY_NAME) {
			this.ID_FACULTY = ID_FACULTY;
			this.FACULTY_NAME = FACULTY_NAME;
		}

		public static FACULTIES fromJson(final JSONObject object) {
			final int ID_FACULTY= object.optInt("ID_FACULTY", 0);
			final String FACULTY_NAME = object.optString("FACULTY_NAME", "");
			return new FACULTIES(ID_FACULTY,FACULTY_NAME);
		}

		public static ArrayList<FACULTIES> fromJson(final JSONArray array) {
			final ArrayList<FACULTIES> FACULTIES = new ArrayList<FACULTIES>();
			for (int index = 0; index < array.length(); ++index) {
				try {
					final FACULTIES FACULTET = fromJson(array.getJSONObject(index));
					if (null != FACULTET) FACULTIES.add(FACULTET);
				} catch (final JSONException ignored) {
				}
			}
			return FACULTIES;
		}
	}

	public static class TEACHERS { //Классы для парсинга: преподы

		public final int ID_TEACHER;
		public final String FACULTY_NAME;
		public final String DEPARTMENT_NAME;
		public final String TEACHER_NAME;


		public TEACHERS(int ID_TEACHER, String FACULTY_NAME, String DEPARTMENT_NAME, String TEACHER_NAME) {
			this.ID_TEACHER = ID_TEACHER;
			this.DEPARTMENT_NAME = DEPARTMENT_NAME;
			this.FACULTY_NAME = FACULTY_NAME;
			this.TEACHER_NAME = TEACHER_NAME;
		}

		public static TEACHERS fromJson(final JSONObject object) {
			final int ID_TEACHER= object.optInt("ID_TEACHER", 0);			
			final String DEPARTMENT_NAME = object.optString("DEPARTMENT_NAME", "");
			final String FACULTY_NAME = object.optString("FACULTY_NAME", "");
			final String TEACHER_NAME = object.optString("TEACHER_NAME", "");
			return new TEACHERS(ID_TEACHER, FACULTY_NAME, DEPARTMENT_NAME, TEACHER_NAME);
		}

		public static  ArrayList<TEACHERS> fromJson(final JSONArray array) {
			final ArrayList<TEACHERS> TEACHERS = new ArrayList<TEACHERS>();
			for (int index = 0; index < array.length(); ++index) {
				try {
					final TEACHERS TEACHER = fromJson(array.getJSONObject(index));
					if (null != TEACHER) TEACHERS.add(TEACHER);
				} catch (final JSONException ignored) {
				}
			}
			return TEACHERS;
		}
	}

	public static class GROUPS { //Классы для парсинга: группы

		public final int ID_GROUP;
		public final String FACULTY_NAME;
		public final String GROUP_NAME;


		public GROUPS(int ID_GROUP, String FACULTY_NAME, String GROUP_NAME) {
			this.ID_GROUP = ID_GROUP;
			this.FACULTY_NAME = FACULTY_NAME;
			this.GROUP_NAME = GROUP_NAME;
		}

		public static GROUPS fromJson(final JSONObject object) {
			final int ID_GROUP= object.optInt("ID_GROUP", 0);
			final String FACULTY_NAME = object.optString("FACULTY_NAME", "");
			final String GROUP_NAME = object.optString("GROUP_NAME", "");
			return new GROUPS(ID_GROUP, FACULTY_NAME, GROUP_NAME);
		}

		public static ArrayList<GROUPS> fromJson(final JSONArray array) {
			final ArrayList<GROUPS> GROUPS = new ArrayList<GROUPS>();
			for (int index = 0; index < array.length(); ++index) {
				try {
					final GROUPS GROUP = fromJson(array.getJSONObject(index));
					if (null != GROUP) GROUPS.add(GROUP);
				} catch (final JSONException ignored) {
				}
			}
			return GROUPS;
		}
	}

		public static class MODIFICATOR {
			public final String DATE;
			public final ArrayList<PAIR> MOD;

			public MODIFICATOR(String DATE, JSONArray MODS, boolean isGroup) {
				this.DATE = DATE;
				this.MOD = PAIR.fromJson(MODS, isGroup);			
			}

			public static MODIFICATOR fromJson(final JSONObject object, boolean isGroup) {
				String date = object.optString("DATE", "");
				final String DATE = date.substring(8, 10) + "." + date.substring(5, 7) + "." + date.substring(0, 4);
				JSONArray MODS = null;
				try {
					MODS = object.getJSONArray("DAILY_SCHEDULE");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return new MODIFICATOR(DATE,MODS,isGroup);
			}			

			public static ArrayList<MODIFICATOR> fromJson(final JSONArray array, boolean isGroup) {
				final ArrayList<MODIFICATOR> MODIFICATOR = new ArrayList<MODIFICATOR>();
				for (int index = 0; index < array.length(); ++index) {
					try {
						final MODIFICATOR mod = fromJson(array.getJSONObject(index), isGroup);
						if (null != mod) MODIFICATOR.add(mod);
					} catch (final JSONException ignored) {
					}
				}
				return MODIFICATOR;
			}


		}



		public static class PAIR { //Классы для парсинга: пара, модификации

			public final int PAIR_NUMBER;
			public final String DISCIPLINE;
			public final String DISCIPLINE_TYPE;	
			public final String NAME;
			public final String CLASSROOM;
			public final String SUBGROUP;
			public final boolean IS_CANCELED;

			public PAIR(int PAIR_NUMBER, String DISCIPLINE, String DISCIPLINE_TYPE, String NAME, String CLASSROOM, String SUBGROUP, boolean IS_CANCELED) {
				this.PAIR_NUMBER = PAIR_NUMBER;
				this.DISCIPLINE = DISCIPLINE;
				this.DISCIPLINE_TYPE = DISCIPLINE_TYPE;
				this.NAME = NAME;
				this.CLASSROOM = CLASSROOM;
				this.SUBGROUP = SUBGROUP;
				this.IS_CANCELED = IS_CANCELED;
			}
			
			public static PAIR fromJson(final JSONObject object, boolean isGroup) throws ParseException {
				String NAME = ""; //Хранится информация о том, с кем проходит занятие
				if (isGroup) { //Если истина, пришла группа
					NAME = object.optString("TEACHER_NAME", "");
				} else  { 
					NAME = object.optString("GROUP_NAME", "");
				}
				final int PAIR_NUMBER= object.optInt("PAIR_NUMBER", 0);			
				final String DISCIPLINE = object.optString("DISCIPLINE", "");
				final String DISCIPLINE_TYPE = object.optString("DISCIPLINE_TYPE", "");
				final String CLASSROOM = object.optString("CLASSROOM", "");
				final String SUBGROUP = object.optString("SUBGROUP_NUMBER", "");
				final boolean IS_CANCELED = object.optBoolean("IS_CANCELED", false);
				return new PAIR(PAIR_NUMBER,DISCIPLINE,DISCIPLINE_TYPE,NAME,CLASSROOM,SUBGROUP,IS_CANCELED);
			}

			public static ArrayList<PAIR> fromJson(final JSONArray array, boolean isGroup) {
				final ArrayList<PAIR> PAIR = new ArrayList<PAIR>();
				for (int index = 0; index < array.length(); ++index) {
					try {
						final PAIR pair = fromJson(array.getJSONObject(index), isGroup);
						if (null != pair) PAIR.add(pair);
					} catch (final JSONException | ParseException ignored) {
					}
				}
				return PAIR;
			}
		}
	}
