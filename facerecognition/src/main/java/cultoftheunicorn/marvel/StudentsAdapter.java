package cultoftheunicorn.marvel;

/**
 * Created by root on 9/4/18.
 */

// importing libraries used

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.opencv.cultoftheunicorn.marvel.R;

import java.util.List;


import database.model.Student;     //  importing note class

//  Class to set up notes adapter

public class StudentsAdapter extends RecyclerView.Adapter<StudentsAdapter.MyViewHolder> {

    private Context context;
    private List<Student> studentsList;

    //  Defining elements of custom row of recycler view

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;       // name of student
        public TextView dot;        //  bullet for each list item
        public TextView rollnumber;     // rollnumber of student
        public TextView state;      //  state of student

        public MyViewHolder(View view) {
            super(view);
            //  importing views from XML file
            name = (android.widget.TextView)view.findViewById(R.id.student);
            dot = (android.widget.TextView)view.findViewById(R.id.dot);
            rollnumber = (android.widget.TextView)view.findViewById(R.id.timestamp);
            state = (android.widget.TextView)view.findViewById(R.id.state);
        }
    }

    // Initiating StudentsAdapter

    public StudentsAdapter(Context context, List<Student> notesList) {
        this.context = context;
        this.studentsList = notesList;
    }

    // Setting up MyView Holder upon loading

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    //  Applying values to Row elements
    @Override
    public void onBindViewHolder(MyViewHolder StudentHolder, int position) {
        //  obtaining student at given position in list
        Student student = studentsList.get(position);

        //  displaying name
        StudentHolder.name.setText(student.getName());

        // Displaying dot from HTML character code
        StudentHolder.dot.setText(Html.fromHtml("&#8226;"));

        // displaying rollnumber
        StudentHolder.rollnumber.setText(String.valueOf(student.getRollnumber()));

        // displaying state
        StudentHolder.state.setText(String.valueOf(student.getState()));
    }

    //  return total number of students
    @Override
    public int getItemCount() {return studentsList.size();
    }


}
