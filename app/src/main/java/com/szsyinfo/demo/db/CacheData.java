package com.szsyinfo.demo.db;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "tb_cache_data")
public class CacheData {

    @Id(autoincrement = true)
    private Long id;
    private String key;
    private String value;
    private String channel_name;
    private String add_time;
    private String update_time;
    @Generated(hash = 1330912334)
    public CacheData(Long id, String key, String value, String channel_name,
            String add_time, String update_time) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.channel_name = channel_name;
        this.add_time = add_time;
        this.update_time = update_time;
    }
    @Generated(hash = 1582791643)
    public CacheData() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getKey() {
        return this.key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getValue() {
        return this.value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public String getChannel_name() {
        return this.channel_name;
    }
    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }
    public String getAdd_time() {
        return this.add_time;
    }
    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }
    public String getUpdate_time() {
        return this.update_time;
    }
    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

}
