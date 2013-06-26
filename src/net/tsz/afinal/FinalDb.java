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
import net.tsz.afinal.db.sqlite.SqlInfo;
import net.tsz.afinal.db.table.KeyValue;
import net.tsz.afinal.db.table.ManyToOne;
import net.tsz.afinal.db.table.OneToMany;
import net.tsz.afinal.db.table.TableInfo;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FinalDb {
	
	private static final String TAG = "FinalDb";
	
	private static HashMap<String, FinalDb> daoMap = new HashMap<String, FinalDb>();
	
	private SQLiteDatabase db;
	private DaoConfig config;
	
	private FinalDb(DaoConfig config){
		if(config == null)
			throw new RuntimeException("daoConfig is null");
		if(config.getContext() == null)
			throw new RuntimeException("android context is null");
		this.db = new SqliteDbHelper(config.getContext().getApplicationContext(), config.getDbName(), config.getDbVersion(),config.getDbUpdateListener()).getWritableDatabase();
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
	
	/**
	 * 创建FinalDb
	 * @param context
	 */
	public static FinalDb create(Context context){
		DaoConfig config = new DaoConfig();
		config.setContext(context);
		
		return getInstance(config);
		
	}
	
	/**
	 * 创建FinalDb
	 * @param context
	 * @param isDebug 是否是debug模式（debug模式进行数据库操作的时候将会打印sql语句）
	 */
	public static FinalDb create(Context context,boolean isDebug){
		DaoConfig config = new DaoConfig();
		config.setContext(context);
		config.setDebug(isDebug);
		return getInstance(config);
		
	}
	
	/**
	 * 创建FinalDb
	 * @param context
	 * @param dbName 数据库名称
	 */
	public static FinalDb create(Context context,String dbName){
		DaoConfig config = new DaoConfig();
		config.setContext(context);
		config.setDbName(dbName);
		
		return getInstance(config);
	}
	
	/**
	 * 创建 FinalDb
	 * @param context
	 * @param dbName 数据库名称
	 * @param isDebug 是否为debug模式（debug模式进行数据库操作的时候将会打印sql语句）
	 */
	public static FinalDb create(Context context,String dbName,boolean isDebug){
		DaoConfig config = new DaoConfig();
		config.setContext(context);
		config.setDbName(dbName);
		config.setDebug(isDebug);
		return getInstance(config);
	}
	
	/**
	 * 创建 FinalDb
	 * @param context 上下文
	 * @param dbName 数据库名字
	 * @param isDebug 是否是调试模式：调试模式会log出sql信息
	 * @param dbVersion 数据库版本信息
	 * @param dbUpdateListener 数据库升级监听器：如果监听器为null，升级的时候将会清空所所有的数据
	 * @return
	 */
	public static FinalDb create(Context context,String dbName,boolean isDebug,int dbVersion,DbUpdateListener dbUpdateListener){
		DaoConfig config = new DaoConfig();
		config.setContext(context);
		config.setDbName(dbName);
		config.setDebug(isDebug);
		config.setDbVersion(dbVersion);
		config.setDbUpdateListener(dbUpdateListener);
		return getInstance(config);
	}
	
	/**
	 * 创建FinalDb
	 * @param daoConfig
	 * @return
	 */
	public static FinalDb create(DaoConfig daoConfig){
		return getInstance(daoConfig);
	}
	

	
	/**
	 * 保存数据库，速度要比save快
	 * @param entity
	 */
	public void save(Object entity){
		checkTableExist(entity.getClass());
		exeSqlInfo(SqlBuilder.buildInsertSql(entity));
	}
	
	
	/**
	 * 保存数据到数据库<br />
	 * <b>注意：</b><br />
	 * 保存成功后，entity的主键将被赋值（或更新）为数据库的主键， 只针对自增长的id有效
	 * @param entity 要保存的数据
	 * @return  ture： 保存成功    false:保存失败
	 */
	public boolean saveBindId(Object entity){
		checkTableExist(entity.getClass());
		List<KeyValue> entityKvList = SqlBuilder.getSaveKeyValueListByEntity(entity);
		if(entityKvList != null && entityKvList.size() > 0){
			TableInfo tf = TableInfo.get(entity.getClass());
			ContentValues cv = new ContentValues();
			insertContentValues(entityKvList,cv);
			Long id = db.insert(tf.getTableName(), null, cv);
			if(id == -1)
				return false;
			tf.getId().setValue(entity, id);
			return true;
		}
		return false;
	}
	
	/**
	 * 把List<KeyValue>数据存储到ContentValues
	 * @param list
	 * @param cv
	 */
	private void insertContentValues(List<KeyValue> list , ContentValues cv){
		if(list!=null && cv!=null){
			for(KeyValue kv : list){
				cv.put(kv.getKey(), kv.getValue().toString());
			}
		}else{
			Log.w(TAG, "insertContentValues: List<KeyValue> is empty or ContentValues is empty!");
		}
		
	}
	
	/**
	 * 更新数据 （主键ID必须不能为空）
	 * @param entity
	 */
	public void update(Object entity){
		checkTableExist(entity.getClass());
		exeSqlInfo(SqlBuilder.getUpdateSqlAsSqlInfo(entity));
	}
	
	/**
	 * 根据条件更新数据
	 * @param entity
	 * @param strWhere 条件为空的时候，将会更新所有的数据
	 */
	public void update(Object entity,String strWhere){
		checkTableExist(entity.getClass());
		exeSqlInfo(SqlBuilder.getUpdateSqlAsSqlInfo(entity, strWhere));
	}
	
	/**
	 * 删除数据
	 * @param entity  entity的主键不能为空
	 */
	public void delete(Object entity) {
		checkTableExist(entity.getClass());
		exeSqlInfo(SqlBuilder.buildDeleteSql(entity));
	}
	
	/**
	 * 根据主键删除数据
	 * @param clazz 要删除的实体类
	 * @param id 主键值
	 */
	public void deleteById(Class<?> clazz , Object id) {
		checkTableExist(clazz);
		exeSqlInfo(SqlBuilder.buildDeleteSql(clazz, id));
	}
	
	/**
	 * 根据条件删除数据
	 * @param clazz
	 * @param strWhere 条件为空的时候 将会删除所有的数据
	 */
	public void deleteByWhere(Class<?> clazz , String strWhere ) {
		checkTableExist(clazz);
		String sql = SqlBuilder.buildDeleteSql(clazz, strWhere);
		debugSql(sql);
		db.execSQL(sql);
	}
	
	
	private void exeSqlInfo(SqlInfo sqlInfo){
		if(sqlInfo!=null){
			debugSql(sqlInfo.getSql());
			db.execSQL(sqlInfo.getSql(),sqlInfo.getBindArgsAsArray());
		}else{
			Log.e(TAG, "sava error:sqlInfo is null");
		}
	}
	
	/**
	 * 根据主键查找数据（默认不查询多对一或者一对多的关联数据）
	 * @param id
	 * @param clazz
	 */
	public <T> T findById(Object id ,Class<T> clazz){
		checkTableExist(clazz);
		SqlInfo sqlInfo = SqlBuilder.getSelectSqlAsSqlInfo(clazz, id);
		if(sqlInfo!=null){
			debugSql(sqlInfo.getSql());
			Cursor cursor = db.rawQuery(sqlInfo.getSql(), sqlInfo.getBindArgsAsStringArray());
			try {
				if(cursor.moveToNext()){
					return CursorUtils.getEntity(cursor, clazz);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				cursor.close();
			}
		}
		return null;
	}
	
	/**
	 * 根据主键查找，同时查找“多对一”的数据（如果有多个“多对一”属性，则查找所有的“多对一”属性）
	 * @param id
	 * @param clazz
	 */
	public <T> T findWithManyToOneById(Object id ,Class<T> clazz){
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
	
	/**
	 * 根据条件查找，同时查找“多对一”的数据（只查找findClass中的类的数据）
	 * @param id
	 * @param clazz
	 * @param findClass 要查找的类
	 */
	public <T> T findWithManyToOneById(Object id ,Class<T> clazz,Class<?> ... findClass){
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
	}
	
	
	/**
	 * 根据主键查找，同时查找“一对多”的数据（如果有多个“一对多”属性，则查找所有的一对多”属性）
	 * @param id
	 * @param clazz
	 */
	public <T> T findWithOneToManyById(Object id ,Class<T> clazz){
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
						List<?> list = findAllByWhere(one.getOneClass(), one.getColumn()+"="+id);
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
	
	/**
	 * 根据主键查找，同时查找“一对多”的数据（只查找findClass中的“一对多”）
	 * @param id
	 * @param clazz
	 * @param findClass
	 */
	public <T> T findWithOneToManyById(Object id ,Class<T> clazz,Class<?> ... findClass){
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
							List<?> list = findAllByWhere(one.getOneClass(), one.getColumn()+"="+id);
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
	
	/**
	 * 查找所有的数据
	 * @param clazz
	 */
	public <T> List<T> findAll(Class<T> clazz){
		checkTableExist(clazz);
		return findAllBySql(clazz,SqlBuilder.getSelectSQL(clazz));
	}
	
	/**
	 * 查找所有数据
	 * @param clazz
	 * @param orderBy 排序的字段
	 */
	public <T> List<T> findAll(Class<T> clazz,String orderBy){
		checkTableExist(clazz);
		return findAllBySql(clazz,SqlBuilder.getSelectSQL(clazz)+" ORDER BY "+orderBy+" DESC");
	}
	
	/**
	 * 根据条件查找所有数据
	 * @param clazz
	 * @param strWhere 条件为空的时候查找所有数据
	 */
	public <T> List<T> findAllByWhere(Class<T> clazz,String strWhere){
		checkTableExist(clazz);
		return findAllBySql(clazz,SqlBuilder.getSelectSQLByWhere(clazz,strWhere));
	}
	
	/**
	 * 根据条件查找所有数据
	 * @param clazz
	 * @param strWhere 条件为空的时候查找所有数据
	 * @param orderBy 排序字段
	 */
	public <T> List<T> findAllByWhere(Class<T> clazz,String strWhere,String orderBy){
		checkTableExist(clazz);
		return findAllBySql(clazz,SqlBuilder.getSelectSQLByWhere(clazz,strWhere)+" ORDER BY "+orderBy+" DESC");
	}
	
	/**
	 * 根据条件查找所有数据
	 * @param clazz
	 * @param strSQL
	 */
	private <T> List<T> findAllBySql(Class<T> clazz,String strSQL){
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
	
	

	/**
	 * 根据sql语句查找数据，这个一般用于数据统计
	 * @param strSQL
	 */
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
	
	public List<DbModel> findDbModelListBySQL(String strSQL){
		debugSql(strSQL);
		Cursor cursor = db.rawQuery(strSQL,null);
		List<DbModel> dbModelList = new ArrayList<DbModel>();
		try {
			while(cursor.moveToNext()){
				dbModelList.add(CursorUtils.getDbModel(cursor));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			cursor.close();
		}
		return dbModelList;
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
		private DbUpdateListener dbUpdateListener;
		
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
		public DbUpdateListener getDbUpdateListener() {
			return dbUpdateListener;
		}
		public void setDbUpdateListener(DbUpdateListener dbUpdateListener) {
			this.dbUpdateListener = dbUpdateListener;
		}
		
	}
	
	
	class SqliteDbHelper extends SQLiteOpenHelper {
		
		private DbUpdateListener mDbUpdateListener;
		public SqliteDbHelper(Context context, String name,int version, DbUpdateListener dbUpdateListener) {
			super(context, name, null, version);
			this.mDbUpdateListener = dbUpdateListener;
		}

		public void onCreate(SQLiteDatabase db) {
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(mDbUpdateListener!=null){
				mDbUpdateListener.onUpgrade(db, oldVersion, newVersion);
			}else{ //清空所有的数据信息
				Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type ='table'", null);
				if(cursor!=null){
					while(cursor.moveToNext()){
						db.execSQL("DROP TABLE "+cursor.getString(0));
					}
				}
				if(cursor!=null){
					cursor.close();
					cursor=null;
				}
			}
		}

	}
	
	public interface DbUpdateListener{
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
	}

}
