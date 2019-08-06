package com.jogtown.jogtown.utils;


import com.jogtown.jogtown.DAO.JogDAO;
import com.jogtown.jogtown.models.Jog;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Jog.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract JogDAO getJogDAO();
}
