package database;

/**
 * Created by root on 9/4/18.
 */

// Importing required libraries
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// importing Student class
import database.model.Student;

import static android.os.SystemClock.elapsedRealtime;
import static database.model.Student.COLUMN_ROLLNUMBER;
import static database.model.Student.TABLE_NAME;

// StudentDatabaseHelper helps to Create, Read, Update, and Delete entries from database
public class StudentDatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "students_db";

    // initialization
    public StudentDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create students table
        db.execSQL(Student.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public long insertStudent(String name, long rollnumber) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` will be inserted automatically.
        // no need to add it

        values.put(Student.COLUMN_NAME, name);
        values.put(Student.COLUMN_ROLLNUMBER, rollnumber);
        values.put(Student.COLUMN_STATE, 0);

        long id;
        String query = "SELECT + " + COLUMN_ROLLNUMBER +" FROM "+ TABLE_NAME +" WHERE "+ COLUMN_ROLLNUMBER + " =?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(rollnumber)});
        if (!(cursor.getCount() > 0))
        {
            // insert row
            id = db.insert(TABLE_NAME, null, values);

            // close db connection
            db.close();
        }
        else
            id = -1;


        // return newly inserted row id
        return id;
    }


    public Student getStudent(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{Student.COLUMN_ID, Student.COLUMN_NAME, Student.COLUMN_ROLLNUMBER, Student.COLUMN_STATE },
                Student.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null && cursor.getCount()>0) {
            cursor.moveToFirst();

            // prepare student object

            Student student = new Student(

                    cursor.getInt(cursor.getColumnIndex(Student.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(Student.COLUMN_NAME)),
                    cursor.getLong(cursor.getColumnIndex(Student.COLUMN_ROLLNUMBER)), //0);
                    cursor.getInt(cursor.getColumnIndex(Student.COLUMN_STATE)));

            cursor.close();

            return student;
        }

        return null;
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " +
                Student.COLUMN_ROLLNUMBER + " DESC";
        // Select All Query
       // String selectQuery = "SELECT  " + Student.COLUMN_ID + ", " + Student.COLUMN_NAME + ", " + Student.COLUMN_ROLLNUMBER + ", " + Student.COLUMN_STATE + " FROM " + Student.TABLE_NAME + ";" ;
//        + " ORDER BY " +
//                Student.COLUMN_ROLLNUMBER + " DESC" + Student.COLUMN_STATE + " state";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Student student = new Student();
                student.setId(cursor.getInt(cursor.getColumnIndex(Student.COLUMN_ID)));
                student.setName(cursor.getString(cursor.getColumnIndex(Student.COLUMN_NAME)));
                student.setRollnumber(cursor.getLong(cursor.getColumnIndex(Student.COLUMN_ROLLNUMBER)));
                student.setState(cursor.getInt(cursor.getColumnIndex(Student.COLUMN_STATE)));
                students.add(student);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return students list
        return students;
    }

    public List<Student> getGoodStudents() {
        List<Student> students = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " +
                Student.COLUMN_ROLLNUMBER + " DESC";
        // Select All Query
        // String selectQuery = "SELECT  " + Student.COLUMN_ID + ", " + Student.COLUMN_NAME + ", " + Student.COLUMN_ROLLNUMBER + ", " + Student.COLUMN_STATE + " FROM " + Student.TABLE_NAME + ";" ;
//        + " ORDER BY " +
//                Student.COLUMN_ROLLNUMBER + " DESC" + Student.COLUMN_STATE + " state";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if(cursor.getColumnIndex(Student.COLUMN_STATE) > 4) {
                    Student student = new Student();
                    student.setId(cursor.getInt(cursor.getColumnIndex(Student.COLUMN_ID)));
                    student.setName(cursor.getString(cursor.getColumnIndex(Student.COLUMN_NAME)));
                    student.setRollnumber(cursor.getLong(cursor.getColumnIndex(Student.COLUMN_ROLLNUMBER)));
                    student.setState(cursor.getInt(cursor.getColumnIndex(Student.COLUMN_STATE)));
                    students.add(student);
                }
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return students list
        return students;
    }

    public List<Student> getBadStudents() {
        List<Student> students = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " +
                Student.COLUMN_ROLLNUMBER + " DESC";
        // Select All Query
        // String selectQuery = "SELECT  " + Student.COLUMN_ID + ", " + Student.COLUMN_NAME + ", " + Student.COLUMN_ROLLNUMBER + ", " + Student.COLUMN_STATE + " FROM " + Student.TABLE_NAME + ";" ;
//        + " ORDER BY " +
//                Student.COLUMN_ROLLNUMBER + " DESC" + Student.COLUMN_STATE + " state";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if(cursor.getColumnIndex(Student.COLUMN_STATE) <= 4) {
                    Student student = new Student();
                    student.setId(cursor.getInt(cursor.getColumnIndex(Student.COLUMN_ID)));
                    student.setName(cursor.getString(cursor.getColumnIndex(Student.COLUMN_NAME)));
                    student.setRollnumber(cursor.getLong(cursor.getColumnIndex(Student.COLUMN_ROLLNUMBER)));
                    student.setState(cursor.getInt(cursor.getColumnIndex(Student.COLUMN_STATE)));
                    students.add(student);
                }
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return students list
        return students;
    }

    public int getStudentsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count of students
        return count;
    }

    // updating student
    public int editStudent(Student student) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Student.COLUMN_NAME, student.getName());
        values.put(Student.COLUMN_ROLLNUMBER, student.getRollnumber());

        if(student.getName().toString()=="" || String.valueOf(student.getRollnumber())==""){
            return 0;
        }
            // updating row
            return db.update(TABLE_NAME, values, Student.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(student.getId())});


    }

    //  deleting student
    public void deleteName(Student student) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, Student.COLUMN_ID + " = ?",
                new String[]{String.valueOf(student.getId())});
        db.close();
    }

    public void randomize(){

        SQLiteDatabase db = this.getWritableDatabase();
        for(int i = 1; i<= getStudentsCount(); i++){
            ContentValues cv = new ContentValues();
            Random rand = new Random(elapsedRealtime());
            int k = rand.nextInt(10);
            cv.put(Student.COLUMN_STATE,k);
            db.update(TABLE_NAME, cv, Student.COLUMN_ID + "= ?", new String[] {String.valueOf(i)});
        }
        db.close();
    }

    public int findstate(String s){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{Student.COLUMN_ID, Student.COLUMN_NAME, Student.COLUMN_ROLLNUMBER, Student.COLUMN_STATE },
                Student.COLUMN_NAME + "=?",
                new String[]{s}, null, null, null, null);
        if (cursor != null && cursor.getCount()>0) {
            cursor.moveToFirst();

                   return cursor.getInt(cursor.getColumnIndex(Student.COLUMN_STATE));

        }
        return 0;
    }


}
