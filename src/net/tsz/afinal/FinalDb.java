/**
 * Copyright (c) 2012-2013, Michael Yang 杨福海 (www.yangfuhai.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tsz.afinal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.tsz.afinal.db.sqlite.CursorUtils;
import net.tsz.afinal.db.sqlite.DbModel;
import net.tsz.afinal.db.sqlite.SqlBuilder;
import net.tsz.afinal.db.table.ManyToOne;
import net.tsz.afinal.db.table.OneToMany;
import net.tsz.afinal.db.table.TableInfo;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FinalDb {
	
	private static HashMap<String, FinalDb> daoMap = new HashMap<String, FinalDb>();
	
	private SQLiteDatabase db;
	private DaoConfig config;
	
	private FinalDb(DaoConfig config){
		if(config == null)
			throw new RuntimeException("daoConfig is null");
		if(config.getContext() == null)
			throw new RuntimeException("android context is null");
		this.db = new SqliteDbHelper(config.getContext(), config.getDbName(), config.getDbVersion()).getWritableDatabase();
		this.config = config;
	}
	
	
	private synchronized static FinalDb getInstance(DaoConfig daoConfig) {
		FinalDb dao = daoMap.get(daoConfig.getDbName());
		if(dao == null){
			dao = new FinalDb(daoConfig);
			daoMap.put(daoConfig.getDbName(), dao);
		}
		return dao;
	}
	
	
	public static FinalDb create(Context context){
		DaoConfig config = new DaoConfig();
		config.setContext(context);
		
		return getInstance(config);
		
	}
	
	public static FinalDb create(Context context,boolean isDebug){
		DaoConfig config = new DaoConfig();
		config.setContext(context);
		config.setDebug(isDebug);
		return getInstance(config);
		
	}
	
	public static FinalDb create(Context context,String dbName){
		DaoConfig config = new DaoConfig();
		config.setContext(context);
		config.setDbName(dbName);
		
		return getInstance(config);
	}
	
	public static FinalDb create(Context context,String dbName,boolean isDebug){
		DaoConfig config = new DaoConfig();
		config.setContext(context);
		config.setDbName(dbName);
		config.setDebug(isDebug);
		return getInstance(config);
	}
	
	
	public static FinalDb createSqliteDao(DaoConfig daoConfig){
		return getInstance(daoConfig);
	}
	

	public void save(Object entity){
		checkTableExist(entity.getClass());
		String saveSQL = SqlBuilder.getInsertSQL(entity);
		debugSql(saveSQL);
		db.execSQL(saveSQL);
	}
	
	
	public void update(Object entity){
		checkTableExist(entity.getClass());
		String saveSQL = SqlBuilder.getUpdateSQL(entity);
		debugSql(saveSQL);
		db.execSQL(saveSQL);
	}
	
	
	public void update(Object entity,String ...strWhere){
		checkTableExist(entity.getClass());
		String saveSQL = SqlBuilder.getUpdateSQL(entity, strWhere);
		debugSql(saveSQL);
		db.execSQL(saveSQL);
	}
	
	public void deleteById(Object entity) {
		checkTableExist(entity.getClass());
		String sql = SqlBuilder.getDeleteSQL(entity);
		debugSql(sql);
		db.execSQL(sql);
	}
	
	public void deleteById(Class<?> clazz , Object id) {
		checkTableExist(clazz);
		String sql = SqlBuilder.getDeleteSQL(clazz,id);
		debugSql(sql);
		db.execSQL(sql);
	}
	
	public void deleteByWhere(Class<?> clazz , String ...strWhere ) {
		checkTableExist(clazz);
		String sql = SqlBuilder.getDeleteSQL(clazz, strWhere);
		debugSql(sql);
		db.execSQL(sql);
	}
	
	public <T> T findById(Object id ,Class<T> clazz){
		checkTableExist(clazz);
		String sql = SqlBuilder.getSelectSQL(clazz, id);
		debugSql(sql);
		Cursor cursor = db.rawQuery(sql, null);
		try {
			if(cursor.moveToNext()){
				return CursorUtils.getEntity(cursor, clazz);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			cursor.close();
		}
		return null;
	}
	
	
	public <T> T findObjectWihtManyToOneById(Object id ,Class<T> clazz){
		checkTableExist(clazz);
		String sql = SqlBuilder.getSelectSQL(clazz, id);
		debugSql(sql);
		DbModel dbModel = findDbModelBySQL(sql);
		if(dbModel!=null){
			T entity = CursorUtils.dbModel2Entity(dbModel, clazz);
			if(entity!=null){
				try {
					Collection<ManyToOne> manys = TableInfo.get(clazz).manyToOneMap.values();
					for(ManyToOne many : manys){
						Object obj = dbModel.get(many.getColumn());
						if(obj!=null){
							@SuppressWarnings("unchecked")
							T manyEntity = (T) findById(Integer.valueOf(obj.toString()), many.getDataType());
							if(manyEntity!=null){
								many.setValue(entity, manyEntity);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return entity;
		}
		
		return null;
	}
	
	
	public <T> T findObjectWihtManyToOneById(Object id ,Class<T> clazz,Class<?> ... findClass){
		if(findClass!=null && findClass.length>0){
			checkTableExist(clazz);
			String sql = SqlBuilder.getSelectSQL(clazz, id);
			debugSql(sql);
			DbModel dbModel = findDbModelBySQL(sql);
			if(dbModel!=null){
				T entity = CursorUtils.dbModel2Entity(dbModel, clazz);
				if(entity!=null){
					try {
						Collection<ManyToOne> manys = TableInfo.get(clazz).manyToOneMap.values();
						for(ManyToOne many : manys){
							boolean isFind = false;
							for(Class<?> mClass : findClass){
								if(many.getManyClass()==mClass){
									isFind = true;
									break;
								}
							}
							
							if(isFind){
								@SuppressWarnings("unchecked")
								T manyEntity = (T) findById(dbModel.get(many.getColumn()), many.getDataType());
								if(manyEntity!=null){
									many.setValue(entity, manyEntity);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return entity;
			}
			return null;
		}else{
			return findObjectWihtManyToOneById(id, clazz);
		}
	}
	
	
	
	public <T> T findObjectWihtOneToManyById(Object id ,Class<T> clazz){
		checkTableExist(clazz);
		String sql = SqlBuilder.getSelectSQL(clazz, id);
		debugSql(sql);
		DbModel dbModel = findDbModelBySQL(sql);
		if(dbModel!=null){
			T entity = CursorUtils.dbModel2Entity(dbModel, clazz);
			if(entity!=null){
				try {
					Collection<OneToMany> ones = TableInfo.get(clazz).oneToManyMap.values();
					for(OneToMany one : ones){
						List<?> list = findListByWhere(one.getOneClass(), one.getColumn()+"="+id);
						if(list!=null){
							one.setValue(entity, list);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return entity;
		}
		
		return null;
	}
	
	
	public <T> T findObjectWihtOneToManyById(Object id ,Class<T> clazz,Class<?> ... findClass){
		checkTableExist(clazz);
		String sql = SqlBuilder.getSelectSQL(clazz, id);
		debugSql(sql);
		DbModel dbModel = findDbModelBySQL(sql);
		if(dbModel!=null){
			T entity = CursorUtils.dbModel2Entity(dbModel, clazz);
			if(entity!=null){
				try {
					Collection<OneToMany> ones = TableInfo.get(clazz).oneToManyMap.values();
					for(OneToMany one : ones){
						boolean isFind = false;
						for(Class<?> mClass : findClass){
							if(one.getOneClass().equals(mClass.getName())){
								isFind = true;
								break;
							}
						}
						
						if(isFind){
							List<?> list = findListByWhere(one.getOneClass(), one.getColumn()+"="+id);
							if(list!=null){
								one.setValue(entity, list);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return entity;
		}
		
		return null;
	}
	
	
	public <T> List<T> findAll(Class<T> clazz){
		checkTableExist(clazz);
		return findList(clazz,SqlBuilder.getSelectSQL(clazz));
	}
	
	public <T> List<T> findAll(Class<T> clazz,String orderBy){
		checkTableExist(clazz);
		return findList(clazz,SqlBuilder.getSelectSQL(clazz)+" ORDER BY '"+orderBy+"' DESC");
	}
	
	public <T> List<T> findList(Class<T> clazz,String strSQL){
		checkTableExist(clazz);
		debugSql(strSQL);
		Cursor cursor = db.rawQuery(strSQL, null);
		try {
			List<T> list = new ArrayList<T>();
			while(cursor.moveToNext()){
				T t = CursorUtils.getEntity(cursor, clazz);
				list.add(t);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(cursor!=null)
				cursor.close();
			cursor=null;
		}
		return null;
	}
	
	
	
	
	public <T> List<T> findListByWhere(Class<T> clazz,String ... strWhere){
		checkTableExist(clazz);
		String strSQL = SqlBuilder.getSelectSQL(clazz,strWhere);
		debugSql(strSQL);
		Cursor cursor = db.rawQuery(strSQL, null);
		try {
			List<T> list = new ArrayList<T>();
			while(cursor.moveToNext()){
				T t = CursorUtils.getEntity(cursor, clazz);
				if(t!=null ) list.add(t);
				
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(cursor!=null)
				cursor.close();
			cursor=null;
		}
		return null;
	}
	
	
	public DbModel findDbModelBySQL(String strSQL){
		debugSql(strSQL);
		Cursor cursor = db.rawQuery(strSQL,null);
		try {
			if(cursor.moveToNext()){
				return CursorUtils.getDbModel(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			cursor.close();
		}
		return null;
	}
	
	
	
	private void checkTableExist(Class<?> clazz){
		if(!tableIsExist(TableInfo.get(clazz))){
			String sql = SqlBuilder.getCreatTableSQL(clazz);
			debugSql(sql);
			db.execSQL(sql);
		}
	}
	
	
	private boolean tableIsExist(TableInfo table){
		if(table.isCheckDatabese())
			return true;
		
        Cursor cursor = null;
        try {
                String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='"+table.getTableName()+"' ";
                debugSql(sql);
                cursor = db.rawQuery(sql, null);
                if(cursor!=null && cursor.moveToNext()){
                        int count = cursor.getInt(0);
                        if(count>0){
                        	table.setCheckDatabese(true);
                            return true;
                        }
                }
                
        } catch (Exception e) {
                e.printStackTrace();
        }finally{
        	if(cursor!=null)
        		cursor.close();
        	cursor=null;
        }
        
        return false;
	}
	
	
	private void debugSql(String sql){
		if(config!=null && config.isDebug())
			android.util.Log.d("Debug SQL", ">>>>>>  "+sql);
	}
	
	
	
	
	
	
	
	public static class DaoConfig{
		private Context context = null;//android上下文
		private String dbName = "afinal.db";//数据库名字
		private int dbVersion = 1;//数据库版本
		private boolean debug = true;
		
		public Context getContext() {
			return context;
		}
		public void setContext(Context context) {
			this.context = context;
		}
		public String getDbName() {
			return dbName;
		}
		public void setDbName(String dbName) {
			this.dbName = dbName;
		}
		public int getDbVersion() {
			return dbVersion;
		}
		public void setDbVersion(int dbVersion) {
			this.dbVersion = dbVersion;
		}
		public boolean isDebug() {
			return debug;
		}
		public void setDebug(boolean debug) {
			this.debug = debug;
		}
		
		
	}
	
	
	class SqliteDbHelper extends SQLiteOpenHelper {
		
		public SqliteDbHelper(Context context, String name,int version) {
			super(context, name, null, version);
		}

		public void onCreate(SQLiteDatabase db) {
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}

	}
	

}
