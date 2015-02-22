package com.example.omgups;

public class ShModel {

	private String n;
	private String time;
	private String name;
	private String teacher;
	private String auditory;
	private String tipe;
	private String date;


	public ShModel(String n, String time, String name, String teacher, String auditory, String tipe, String date) {
		this.n = n;
		this.time = time;
		this.name = name;
		this.teacher = teacher;
		this.auditory = auditory;
		this.tipe = tipe;
		this.name = name;
		this.date = date;
	}

	public String getN() {
		return n;
	}
	public String getTime() {
		return time;
	}
	public String getName() {
		return name;
	}
	public String getTeacher() {
		return teacher;
	}
	public String getAuditory() {
		return auditory;
	}
	public String getTipe() {
		return tipe;
	}
	public String getDate() {
		return date;
	}
	
	public void setN(String n) {
		this.n = n;
	}

}

