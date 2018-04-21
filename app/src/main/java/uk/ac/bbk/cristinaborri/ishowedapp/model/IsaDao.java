package uk.ac.bbk.cristinaborri.ishowedapp.model;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public abstract class IsaDao {
    private static final String LOGTAG = "ISA_DATABASE";
    SQLiteOpenHelper dbHandler;
    SQLiteDatabase database;

    public void open(){
        Log.i(LOGTAG,"Database Opened");
        database = dbHandler.getWritableDatabase();
    }

    public void close(){
        Log.i(LOGTAG, "Database Closed");
        dbHandler.close();
    }
}
