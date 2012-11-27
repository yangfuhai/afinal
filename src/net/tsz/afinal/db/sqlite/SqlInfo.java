package net.tsz.afinal.db.sqlite;

import java.text.SimpleDateFormat;
import java.util.LinkedList;

public class SqlInfo {
	
	private String sql;
	private LinkedList<Object> bindArgs;
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	
	public LinkedList<Object> getBindArgs() {
		return bindArgs;
	}
	public void setBindArgs(LinkedList<Object> bindArgs) {
		this.bindArgs = bindArgs;
	}
	
	public Object[] getBindArgsAsArray() {
		if(bindArgs!=null)
			return bindArgs.toArray();
		return null;
	}
	
	public String[] getBindArgsAsStringArray() {
		if(bindArgs!=null){
			String[] strings = new String[bindArgs.size()];
			for(int i = 0;i<bindArgs.size();i++){
				strings[i]=bindArgs.get(i).toString();
			}
			return strings;
		}
		return null;
	}
	
	public void addValue(Object obj){
		if(bindArgs == null)
			bindArgs = new LinkedList<Object>();
		
		if(obj instanceof java.util.Date || obj instanceof java.sql.Date){
			obj = format.format(obj);
		}
		
		bindArgs.add(obj);
	}

}
