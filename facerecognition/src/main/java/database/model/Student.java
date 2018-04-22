package database.model;

/**
 * Created by root on 18/3/18.
 */

//  Class for student row in database

public class Student {
    public static final String TABLE_NAME = "students";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ROLLNUMBER = "rollnumber";
    public static final String COLUMN_STATE = "state";
    //  for each student we have 3 columns(attributes) : id, name, rollnumber
    private int id;
    private String name;
    private long rollnumber;
    private int state;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME + " TEXT,"
                    + COLUMN_ROLLNUMBER + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + COLUMN_STATE + " INTEGER"
                    + ")";

    public Student() {
    }
    // Setting details of a student
    public Student(int id, String name, long rollnumber, int state) {
        this.id = id;
        this.name = name;
        this.rollnumber = rollnumber;
        this.state=state;
    }

    //  return id of student
    public int getId() {
        return id;
    }
    //  return name of student
    public String getName() {
        return name;
    }
    //set name of student
    public void setName(String name) {
        this.name = name;
    }
    // return roll number
    public long getRollnumber() {
        return rollnumber;
    }
    // setting id of student
    public void setId(int id) {
        this.id = id;
    }
    //  setting roll number of student
    public void setRollnumber(long rollnumber) {
        this.rollnumber = rollnumber;
    }

    public void setState(int state){this.state=state;}

    public int getState() {return state;}
}