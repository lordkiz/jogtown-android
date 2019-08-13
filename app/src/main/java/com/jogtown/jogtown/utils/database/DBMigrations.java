package com.jogtown.jogtown.utils.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DBMigrations {

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS jogs " +
                    "(user_id TEXT, " +
                    "distance INTEGER NOT NULL DEFAULT 0, " +
                    "duration INGTEGER NOT NULL DEFAULT 0, " +
                    "calories REAL NOT NULL DEFAULT 0.0, " +
                    "average_speed REAL NOT NULL DEFAULT 0.0, " +
                    "average_pace INTEGER NOT NULL DEFAULT 0, " +
                    "start_latitude REAL NOT NULL DEFAULT 0.0, " +
                    "end_latitude REAL NOT NULL DEFAULT 0.0, " +
                    "start_longitude REAL NOT NULL DEFAULT 0.0, " +
                    "end_longitude REAL NOT NULL DEFAULT 0.0, " +
                    "paces TEXT NOT NULL DEFAULT '[]', " +
                    "speeds TEXT NOT NULL DEFAULT '[]', " +
                    "coordinates TEXT NOT NULL DEFAULT '[]', " +
                    "hydration REAL NOT NULL DEFAULT 0.0, " +
                    "max_speed REAL NOT NULL DEFAULT 0.0, " +
                    "max_pace INTEGER NOT NULL DEFAULT 0, " +
                    "min_altitude REAL NOT NULL DEFAULT 0.0, " +
                    "max_altitude REAL NOT NULL DEFAULT 0.0, " +
                    "total_ascent INTEGER NOT NULL DEFAULT 0, " +
                    "total_descent INTEGER NOT NULL DEFAULT 0)");
        }
    };

}
