package cultoftheunicorn.marvel;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.opencv.cultoftheunicorn.marvel.R;

import java.util.ArrayList;
import java.util.List;

import database.StudentDatabaseHelper;
import database.model.Student;
import utils.MyDividerItemDecoration;

/**
 * Created by root on 22/4/18.
 */

public class GoodStudents extends AppCompatActivity {
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

        setContentView(R.layout.view_good_students);


        coordinatorLayout = (android.support.design.widget.CoordinatorLayout) findViewById(R.id.coordinator_layout);
        recyclerView = (android.support.v7.widget.RecyclerView) findViewById(R.id.recycler_view);
        noStudentsView = (android.widget.TextView) findViewById(R.id.empty_notes_view);      //  display if students list is epmty

        db = new StudentDatabaseHelper(this);  //  database of Students

        studentsList.addAll(db.getGoodStudents());     //  list of all students

        mAdapter = new StudentsAdapter(this, studentsList);       //  Adapter for students list
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);      //  displaying elements of list

        toggleEmptyNotes();

    }

    private void toggleEmptyNotes() {
        // you can check studentsList.size() > 0

        if (db.getStudentsCount() > 0) {
            noStudentsView.setVisibility(View.GONE);
        } else {
            noStudentsView.setVisibility(View.VISIBLE);
        }
    }
}
