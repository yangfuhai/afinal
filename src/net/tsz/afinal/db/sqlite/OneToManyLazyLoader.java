package net.tsz.afinal.db.sqlite;

import net.tsz.afinal.FinalDb;

import java.util.ArrayList;
import java.util.List;

/**
 * 一对多延迟加载类
 * Created by pwy on 13-7-25.
 */
public class OneToManyLazyLoader<O,M> {
    Object ownerEntity;
    Class<O> ownerClazz;
    Class<M> listItemClazz;
    FinalDb db;
    public OneToManyLazyLoader(Object ownerEntity,Class<O> ownerClazz,Class<M> listItemclazz,FinalDb db){
        this.ownerEntity = ownerEntity;
        this.ownerClazz = ownerClazz;
        this.listItemClazz = listItemclazz;
        this.db = db;
    }
    List<M> entities;

    /**
     * 如果数据未加载，则调用loadOneToMany填充数据
     * @return
     */
    public List<M> getList(){
        if(entities==null){
            this.db.loadOneToMany((O)this.ownerEntity,this.ownerClazz,this.listItemClazz);
        }
        if(entities==null){
            entities =new ArrayList<M>();
        }
        return entities;
    }
    public void setList(List<M> value){
        entities = value;
    }

}
