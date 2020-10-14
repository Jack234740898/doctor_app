package com.sftelehealth.doctor.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sftelehealth.doctor.data.database.entity.SystemInfo;

@Dao
public interface SystemInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSystemInfo(SystemInfo systemInfo);


    @Query("SELECT * FROM 'systemInfo' WHERE 1 LIMIT 1")
    SystemInfo getSystemInfo();

    @Delete
    void delete(SystemInfo systemInfo);
}
