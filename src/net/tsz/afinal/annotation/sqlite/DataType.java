package net.tsz.afinal.annotation.sqlite;

/**
 * 数据类型
 * 
 * @author leaf
 * 
 */
public enum DataType {
	INTEGER(" INTEGER "), DOUBLE(" DOUBLE "), TEXT(" TEXT ");
	private String type;

	DataType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return this.type;
	}
}
