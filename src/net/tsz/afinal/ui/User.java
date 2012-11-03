package net.tsz.afinal.ui;

import java.util.Date;

import net.tsz.afinal.annotation.sqlite.ManyToOne;

public class User {

	private int id;
	private String name;
	private String email;
	private Date registerDate;
	private double ddtest;
	private Float fftest;
	
	@ManyToOne
	private UserType type;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public UserType getType() {
		return type;
	}
	public void setType(UserType type) {
		this.type = type;
	}
	public Date getRegisterDate() {
		return registerDate;
	}
	public void setRegisterDate(Date registerDate) {
		this.registerDate = registerDate;
	}
	
	public double getDdtest() {
		return ddtest;
	}
	public void setDdtest(double ddtest) {
		this.ddtest = ddtest;
		
	}
	public Float getFftest() {
		return fftest;
	}
	public void setFftest(Float fftest) {
		this.fftest = fftest;
	}
	
	
}
