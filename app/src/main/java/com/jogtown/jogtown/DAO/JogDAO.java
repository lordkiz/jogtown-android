package com.jogtown.jogtown.DAO;

import com.jogtown.jogtown.models.Jog;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface JogDAO {
    @Insert
    void insertJog (Jog jog);

    @Delete
    void deleteJog(Jog jog);

    @Query("SELECT * FROM jogs")
    List<Jog> getAllJogs();
}
