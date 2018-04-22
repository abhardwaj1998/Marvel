package cultoftheunicorn.marvel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import database.StudentDatabaseHelper;

import org.opencv.cultoftheunicorn.marvel.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by root on 9/4/18.
 */
public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private StudentDatabaseHelper StudentDatabase;
    //Function to initialize the elements and set up the view whenever this activity is started
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        StudentDatabase = new StudentDatabaseHelper(this);
        //  StudentDatabase is object of class StudentDatabaseHelper which helps in interacting with student database.

        //List of the modules {Student List, Visualization}
        ListView listview = (ListView) findViewById(R.id.listView1);
        listview.setOnItemClickListener(this);

        final Handler handler = new Handler();      //  handler to generate random states.
        Timer    timer = new Timer();       //      timer to count every 2 minutes for updating the states.
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {     //  Asynchronous task running in background
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {
                            StudentDatabase.randomize();    //  randomize() called from StudentDatabase
                        }
                        catch (Exception e) {
                            Log.e("Timer","exception caught");
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 120000);       //  timer scheduled for 2 minutes.

    }

    // Function to listen the click and start appropriate activity
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {

        // Start ViewStudents activity via Intent
        if(position == 0){

            Intent GoToViewStudents = new Intent(getApplicationContext(),ViewStudents.class);
            startActivity(GoToViewStudents);   //  Start activity to Students List

        }
        // Start Recognize activity via Intent
        if(position == 1){
            Intent GoToRecognize = new Intent(getApplicationContext(),Recognize.class);
            startActivity(GoToRecognize);
        }

    }

}