package cultoftheunicorn.marvel;

/**
 * Created by root on 9/4/18.
 */

// importing required libraries

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.cultoftheunicorn.marvel.R;

import java.util.ArrayList;
import java.util.List;

// importing other classes used
import database.StudentDatabaseHelper;
import database.model.Student;
import utils.MyDividerItemDecoration;
import utils.RecycleTouchListener;

//  class to display Students List

public class ViewStudents extends AppCompatActivity {

    private StudentsAdapter mAdapter;      // Adapter for Students List
    private List<Student> studentsList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noStudentsView;

    private StudentDatabaseHelper db;      //  database of Students

    //  Setting up view of activity and initializing variables
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_students_main);


        coordinatorLayout =(android.support.design.widget.CoordinatorLayout) findViewById(R.id.coordinator_layout);
        recyclerView =(android.support.v7.widget.RecyclerView) findViewById(R.id.recycler_view);
        noStudentsView = (android.widget.TextView)findViewById(R.id.empty_notes_view);      //  display if students list is epmty

        db = new StudentDatabaseHelper(this);  //  database of Students

        studentsList.addAll(db.getAllStudents());     //  list of all students

        //  button to add a new student
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStudentDialog(false, null, -1);
            }
        });

        mAdapter = new StudentsAdapter(this, studentsList);       //  Adapter for students list
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);      //  displaying elements of list

        toggleEmptyNotes();

        //  on touching a student
        recyclerView.addOnItemTouchListener(new RecycleTouchListener(this,
                recyclerView, new RecycleTouchListener.ClickListener() {
            @Override   //  if clicked
            public void onClick(View view, final int position) {
            }

            @Override   // if long pressed
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));

    }

    //  fucntion to enter a student
    private void createStudent(String name, Long rollnumber) {
        Student student;
        // inserting student in db and getting
        // newly inserted note id
        long id = db.insertStudent(name,rollnumber);

        if(id==-1){
            Toast.makeText(getApplicationContext(), "Roll Number already exists!",
                    Toast.LENGTH_LONG).show();
            return ;
        }
        // get the newly inserted student from db
        student = db.getStudent(id);

        if (student != null) {
            // adding new student to array list at 0 position
            studentsList.add(0, student);

            // refreshing the list
            mAdapter.notifyDataSetChanged();

            toggleEmptyNotes();
            Toast.makeText(getApplicationContext(), "Student added successfuly!",
                    Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "Error while adding student",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Updating student in db and updating
     * item in the list by its position
     */
    private void editStudent(String name, Long rollnumber, int position) {
        Student student = studentsList.get(position);
        // updating student name and rollnumber
        student.setName(name);
        student.setRollnumber(rollnumber);

        // updating student in db
        if(db.editStudent(student)>0) {

            // refreshing the list
            studentsList.set(position, student);
            mAdapter.notifyItemChanged(position);

            toggleEmptyNotes();
            Toast.makeText(getApplicationContext(), "Edited student successfully!",
                    Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "Error while editing student",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Deleting student from SQLite and removing the
     * item from the list by its position
     */
    private void deleteStudent(int position) {
        // deleting the student from db
        db.deleteName(studentsList.get(position));

        // removing the note from the list
        studentsList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyNotes();
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 1
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showStudentDialog(true, studentsList.get(position), position);
                } else {
                    deleteStudent(position);
                    Toast.makeText(getApplicationContext(), "Deleted successfully!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.show();
    }

    /**
     * Shows alert dialog with EditText options to enter / edit
     * a student's entry.
     * when shouldUpdate=true, it automatically displays old student's info and changes the
     * button nameOfStudent to UPDATE
     */

    private void showStudentDialog(final boolean shouldUpdate, final Student student, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());

        //  view for edit student

        View view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(ViewStudents.this);
        alertDialogBuilderUserInput.setView(view);

        // Initializing EditText variables for storing name and rollnumber

        final EditText inputName = (android.widget.EditText)view.findViewById(R.id.name);
        final EditText inputRollnumber = (android.widget.EditText)view.findViewById(R.id.rollnumber);

        TextView dialogTitle = (android.widget.TextView)view.findViewById(R.id.dialog_title);

        //  Setting up title of dialog box
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_note_title) : getString(R.string.lbl_edit_note_title));

        // Updation
        if (shouldUpdate && student != null) {
            inputName.setText(student.getName());
            inputRollnumber.setText(Long.toString(student.getRollnumber()));

        }

        // Building alert Dialog box
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setNeutralButton("Image", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {

                    }
                })
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                // if user presses cancel
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });



        //  creating alertDialog to showing box
        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast message when no nameOfStudent is entered
                if (TextUtils.isEmpty(inputName.getText().toString()) || TextUtils.isEmpty(inputRollnumber.getText().toString())) {
                    Toast.makeText(ViewStudents.this, "Enter student!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }


                // check if user updating student
                if (shouldUpdate && student != null) {
                    // update student by it's id
                    editStudent(inputName.getText().toString(),Long.parseLong(inputRollnumber.getText().toString()), position);
                } else {
                    // create new student
                    createStudent(inputName.getText().toString(),Long.parseLong(inputRollnumber.getText().toString()));
                }
            }
        });

        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!inputName.getText().toString().equals("")) {
                    Intent intent = new Intent(ViewStudents.this, Training.class);
                    intent.putExtra("name", inputName.getText().toString().trim());
                    startActivity(intent);
                }
                else {

                    Toast.makeText(ViewStudents.this, "Please enter the name", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Toggling list and empty notes view
     */
    private void toggleEmptyNotes() {
        // you can check studentsList.size() > 0

        if (db.getStudentsCount() > 0) {
            noStudentsView.setVisibility(View.GONE);
        } else {
            noStudentsView.setVisibility(View.VISIBLE);
        }
    }
}

