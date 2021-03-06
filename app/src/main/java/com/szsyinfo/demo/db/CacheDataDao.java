package com.szsyinfo.demo.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "tb_cache_data".
*/
public class CacheDataDao extends AbstractDao<CacheData, Long> {

    public static final String TABLENAME = "tb_cache_data";

    /**
     * Properties of entity CacheData.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Key = new Property(1, String.class, "key", false, "KEY");
        public final static Property Value = new Property(2, String.class, "value", false, "VALUE");
        public final static Property Channel_name = new Property(3, String.class, "channel_name", false, "CHANNEL_NAME");
        public final static Property Add_time = new Property(4, String.class, "add_time", false, "ADD_TIME");
        public final static Property Update_time = new Property(5, String.class, "update_time", false, "UPDATE_TIME");
    }


    public CacheDataDao(DaoConfig config) {
        super(config);
    }
    
    public CacheDataDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"tb_cache_data\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"KEY\" TEXT," + // 1: key
                "\"VALUE\" TEXT," + // 2: value
                "\"CHANNEL_NAME\" TEXT," + // 3: channel_name
                "\"ADD_TIME\" TEXT," + // 4: add_time
                "\"UPDATE_TIME\" TEXT);"); // 5: update_time
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"tb_cache_data\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, CacheData entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String key = entity.getKey();
        if (key != null) {
            stmt.bindString(2, key);
        }
 
        String value = entity.getValue();
        if (value != null) {
            stmt.bindString(3, value);
        }
 
        String channel_name = entity.getChannel_name();
        if (channel_name != null) {
            stmt.bindString(4, channel_name);
        }
 
        String add_time = entity.getAdd_time();
        if (add_time != null) {
            stmt.bindString(5, add_time);
        }
 
        String update_time = entity.getUpdate_time();
        if (update_time != null) {
            stmt.bindString(6, update_time);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, CacheData entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String key = entity.getKey();
        if (key != null) {
            stmt.bindString(2, key);
        }
 
        String value = entity.getValue();
        if (value != null) {
            stmt.bindString(3, value);
        }
 
        String channel_name = entity.getChannel_name();
        if (channel_name != null) {
            stmt.bindString(4, channel_name);
        }
 
        String add_time = entity.getAdd_time();
        if (add_time != null) {
            stmt.bindString(5, add_time);
        }
 
        String update_time = entity.getUpdate_time();
        if (update_time != null) {
            stmt.bindString(6, update_time);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public CacheData readEntity(Cursor cursor, int offset) {
        CacheData entity = new CacheData( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // key
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // value
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // channel_name
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // add_time
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5) // update_time
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, CacheData entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setKey(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setValue(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setChannel_name(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setAdd_time(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setUpdate_time(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(CacheData entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(CacheData entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(CacheData entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
