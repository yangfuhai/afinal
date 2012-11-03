package net.tsz.afinal.ui;

import java.util.List;

import net.tsz.afinal.annotation.sqlite.Id;
import net.tsz.afinal.annotation.sqlite.OneToMany;

public class UserType {
	
	@Id
	private int typeId;
	
	private String typeName;
	
	@OneToMany(manyColumn="type")
	private List<User> allUser;
	
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public List<User> getAllUser() {
		return allUser;
	}
	public void setAllUser(List<User> allUser) {
		this.allUser = allUser;
	}
	
	
	
}
