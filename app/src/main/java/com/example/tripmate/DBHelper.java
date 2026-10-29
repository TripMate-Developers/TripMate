package com.example.tripmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "tripmate.db";
    public static final int DB_VERSION = 1;

    public DBHelper(Context c) {
        super(c, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, password TEXT)");
        db.execSQL("CREATE TABLE trips(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user TEXT, name TEXT, destination TEXT, start_date TEXT, " +
                "end_date TEXT, budget REAL, people INTEGER)");
        db.execSQL("CREATE TABLE activities(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trip_id INTEGER, day INTEGER, title TEXT, done INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE expenses(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trip_id INTEGER, title TEXT, amount REAL)");
        db.execSQL("CREATE TABLE reminders(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trip_id INTEGER, title TEXT, date TEXT, time TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS trips");
        db.execSQL("DROP TABLE IF EXISTS activities");
        db.execSQL("DROP TABLE IF EXISTS expenses");
        db.execSQL("DROP TABLE IF EXISTS reminders");
        onCreate(db);
    }

    // ================= USERS =================
    public boolean registerUser(String username, String password) {
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password);
        return getWritableDatabase().insert("users", null, cv) != -1;
    }

    public boolean checkUser(String username, String password) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM users WHERE username=? AND password=?",
                new String[]{username, password});
        boolean ok = c.getCount() > 0;
        c.close();
        return ok;
    }

    // ================= TRIPS =================
    public long addTrip(String user, String name, String destination,
                        String start, String end, double budget, int people) {
        ContentValues cv = new ContentValues();
        cv.put("user", user);
        cv.put("name", name);
        cv.put("destination", destination);
        cv.put("start_date", start);
        cv.put("end_date", end);
        cv.put("budget", budget);
        cv.put("people", people);
        return getWritableDatabase().insert("trips", null, cv);
    }

    public Cursor getTrips(String user) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM trips WHERE user=? ORDER BY id DESC",
                new String[]{user});
    }

    public Cursor getTrip(long tripId) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM trips WHERE id=?",
                new String[]{String.valueOf(tripId)});
    }

    public boolean updateTrip(long id, String name, String destination,
                              String start, String end, double budget, int people) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("destination", destination);
        cv.put("start_date", start);
        cv.put("end_date", end);
        cv.put("budget", budget);
        cv.put("people", people);
        return getWritableDatabase().update("trips", cv, "id=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public void deleteTrip(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] arg = {String.valueOf(id)};
        db.delete("activities", "trip_id=?", arg);
        db.delete("expenses", "trip_id=?", arg);
        db.delete("reminders", "trip_id=?", arg);
        db.delete("trips", "id=?", arg);
    }

    // ================= ITINERARY ACTIVITIES =================
    public long addActivity(long tripId, int day, String title) {
        ContentValues cv = new ContentValues();
        cv.put("trip_id", tripId);
        cv.put("day", day);
        cv.put("title", title);
        cv.put("done", 0);
        return getWritableDatabase().insert("activities", null, cv);
    }

    public Cursor getActivities(long tripId) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM activities WHERE trip_id=? ORDER BY day, id",
                new String[]{String.valueOf(tripId)});
    }

    public void setActivityDone(long id, boolean done) {
        ContentValues cv = new ContentValues();
        cv.put("done", done ? 1 : 0);
        getWritableDatabase().update("activities", cv, "id=?",
                new String[]{String.valueOf(id)});
    }

    public void deleteActivity(long id) {
        getWritableDatabase().delete("activities", "id=?",
                new String[]{String.valueOf(id)});
    }

    // ================= EXPENSES =================
    public long addExpense(long tripId, String title, double amount) {
        ContentValues cv = new ContentValues();
        cv.put("trip_id", tripId);
        cv.put("title", title);
        cv.put("amount", amount);
        return getWritableDatabase().insert("expenses", null, cv);
    }

    public Cursor getExpenses(long tripId) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM expenses WHERE trip_id=? ORDER BY id DESC",
                new String[]{String.valueOf(tripId)});
    }

    public double getTotalExpense(long tripId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT SUM(amount) FROM expenses WHERE trip_id=?",
                new String[]{String.valueOf(tripId)});
        double total = 0;
        if (c.moveToFirst()) total = c.getDouble(0);
        c.close();
        return total;
    }

    public void deleteExpense(long id) {
        getWritableDatabase().delete("expenses", "id=?",
                new String[]{String.valueOf(id)});
    }

    // ================= REMINDERS =================
    public long addReminder(long tripId, String title, String date, String time) {
        ContentValues cv = new ContentValues();
        cv.put("trip_id", tripId);
        cv.put("title", title);
        cv.put("date", date);
        cv.put("time", time);
        return getWritableDatabase().insert("reminders", null, cv);
    }

    public Cursor getReminders(long tripId) {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM reminders WHERE trip_id=? ORDER BY date, time",
                new String[]{String.valueOf(tripId)});
    }

    public void deleteReminder(long id) {
        getWritableDatabase().delete("reminders", "id=?",
                new String[]{String.valueOf(id)});
    }
}