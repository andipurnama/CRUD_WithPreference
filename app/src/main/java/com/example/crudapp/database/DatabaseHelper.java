package com.example.crudapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    // versi Database
    private static final int DATABASE_VERSION = 1;
    // nama Database
    private static final String DATABASE_NAME = "berita_db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // membuat tabel
    @Override
    public void onCreate(SQLiteDatabase db) {
        // membuat note tabel
        db.execSQL(Berita.CREATE_TABLE);

        // cobain ini dari tugas sebelah dudududududu biar keliatan diaksi apa sih ya
        String sql = "create table tbl_user (id integer promary key, nama text null, " + "username text null, password text null);";
        Log.d("Data", "onCreate : "+sql);
        db.execSQL(sql);

        sql = "INSERT INTO tbl_user (nama, username, password) " + "VALUES ('Andi Purnama', 'andi', '1234');";
        db.execSQL(sql);
    }



    // Mengupgrade database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // hapus tabel lama jika ada
        db.execSQL("DROP TABLE IF EXISTS " + Berita.TABLE_NAME);

        // membuat tabel lagi
        onCreate(db);
    }

    public long insertBerita(String judul, String isi) {
        // get write database as we want to write data cenah
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // id dan stamp akan dimasukkan otomatis cenah
        // jadi teu kudu diasupkeun
        values.put(Berita.COLUMN_TITLE, judul);
        values.put(Berita.COLUMN_CONTENT, isi);

        //memasukkan row
        long id = db.insert(Berita.TABLE_NAME, null, values);

        //close db connection
        db.close();

        //return row id baru
        return id;
    }

    public Berita getBerita(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Berita.TABLE_NAME,
                new String[]{Berita.COLUMN_ID, Berita.COLUMN_TITLE, Berita.COLUMN_CONTENT, Berita.COLUMN_TIMESTAMP},
                Berita.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // mempersiapkan note object
        Berita berita = new Berita(
                cursor.getInt(cursor.getColumnIndex(Berita.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Berita.COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndex(Berita.COLUMN_CONTENT)),
                cursor.getString(cursor.getColumnIndex(Berita.COLUMN_TIMESTAMP))
        );

        cursor.close();
        return berita;
    }

    public List <Berita> getAllBerita() {
        List<Berita> listBerita = new ArrayList<>();

        // memilih semua query
        String selectQuery = "SELECT * FROM " + Berita.TABLE_NAME + " ORDER BY " + Berita.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Berita berita = new Berita();
                berita.setId(cursor.getInt(cursor.getColumnIndex(Berita.COLUMN_ID)));
                berita.setJudul(cursor.getString(cursor.getColumnIndex(Berita.COLUMN_TITLE)));
                berita.setIsi(cursor.getString(cursor.getColumnIndex(Berita.COLUMN_CONTENT)));
                berita.setTimestamp(cursor.getString(cursor.getColumnIndex(Berita.COLUMN_TIMESTAMP)));

                listBerita.add(berita);
            } while (cursor.moveToNext());
        }

        db.close();

        return listBerita;
    }

    public int getBeritaCount() {
        String countQuery = "SELECT * FROM " + Berita.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    // update berita
    public int updateBerita(Berita berita) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Berita.COLUMN_TITLE, berita.getJudul());
        values.put(Berita.COLUMN_CONTENT, berita.getIsi());

        // updating row
        return db.update(Berita.TABLE_NAME, values, Berita.COLUMN_ID + " = ?",
                new String[]{String.valueOf(berita.getId())});
    }

    //delete berita
    public void deleteBerita(Berita berita) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Berita.TABLE_NAME, Berita.COLUMN_ID + " = ?",
                new String[]{String.valueOf(berita.getId())});
        db.close();
    }
}
//woilaah malam minggu iniiiii
