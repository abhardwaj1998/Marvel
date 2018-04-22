package cultoftheunicorn.marvel;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.cultoftheunicorn.marvel.R;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import database.StudentDatabaseHelper;

//  class to recognize students from CameraView and augment rectangles around faces
public class Recognize extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    private static final String    TAG                 = "Recognize";
    //  by default color of rectangle set to green
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 0, 255, 255);
    public static final int        JAVA_DETECTOR       = 0; //  method of detection
    public static final int        NATIVE_DETECTOR     = 1;

    public static final int SEARCHING= 1;       //  state of activity
    public static final int IDLE= 2;

    private StudentDatabaseHelper studentDatabaseHelper;     //    to perform operations on students database

    private int faceState=IDLE;     //  by default state of face



    private Mat                    mRgba;   //  Rgba Matrix
    private Mat                    mGray;   //  GrayScale Matrix
    private File mCascadeFile;      //  Cascade file
    private CascadeClassifier mJavaDetector;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;       //  stores names of various detectors

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private int mLikely=999;

    String mPath="";        //  location of saved images

    private CameraView mOpenCvCameraView;   //  object of CameraView class

    Bitmap mBitmap;
    Handler mHandler;

    StudentRecognizer fr;       //  StudentRecognizer object
    ToggleButton scan;      //  scan button

    Set<String> uniqueNames = new HashSet<String>();        //  HashSet of unique names of students

    // max number of people to detect in a session
    String[] uniqueNamesArray = new String[10];

    static final long MAXIMG = 10;

    Labels labelsFile;      //  file storing names of students
    static {
        //  loading OpenCV libraries
        OpenCVLoader.initDebug();
        System.loadLibrary("opencv_java");
    }

    //  Loading OpenCV
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    fr=new StudentRecognizer(mPath);        //  instantiating face recognizer

                    fr.load();  //  loading face recognizer

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade.xml");  //  creating cascade file
                        //  instantiate file output stream
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            //  writing bytesRead to outstream
                            os.write(buffer, 0, bytesRead);
                        }
                        //  streaming closed
                        is.close();
                        os.close();
                        //  instantiating cascade classifier
                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            //      Failed to load cascade classifier
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                        //  deleting file cascadeDir
                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    //  enabling CameraView to display
                    mOpenCvCameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;


            }
        }
    };

    //  Constructor
    public Recognize() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    //  views and functions loaded on creation of activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        studentDatabaseHelper = new StudentDatabaseHelper(this);       //  instantiate Student Database Helper

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);        //  setting view of the class

        scan = (ToggleButton) findViewById(R.id.scan);      //  button to scan the camera view
        final TextView results = (TextView) findViewById(R.id.results);     //  store names of students detected
        studentDatabaseHelper = new StudentDatabaseHelper(this);        //  to help in performing operations on Students Database

        //  Camera View of class
        mOpenCvCameraView = (CameraView) findViewById(R.id.training_java_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

        //  location of images of students on device
        mPath = Environment.getExternalStorageDirectory()+"/facerecogOCV/";

        //  file with labels to the images
        labelsFile= new Labels(mPath);

        //  message handler
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String tempName = msg.obj.toString();   //  name received in message
                if (!(tempName.equals("Unknown"))) {    //  student recognized is in database
                    uniqueNames.add(tempName);      //  enter student to set of unique names of students identified
                    uniqueNamesArray = uniqueNames.toArray(new String[uniqueNames.size()]);     //  create array from set values of uniqueNames
                    StringBuilder strBuilder = new StringBuilder();     //  instantiate a string builder
                    //  creating a vertical sequence of names
                    for (int i = 0; i < uniqueNamesArray.length; i++) {
                        strBuilder.append(uniqueNamesArray[i] + "\n");
                    }
                    //  displaying names of students identified
                    String textToDisplay = strBuilder.toString();
                    results.setText(textToDisplay);
                }
            }
        };

        scan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    if(!fr.canPredict()) {
                        //  if face recognizer cannot predict
                        scan.setChecked(false);

                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.SCanntoPredic), Toast.LENGTH_LONG).show();
                        return;
                    }
                    faceState = SEARCHING;
                }
                else {
                    faceState = IDLE;
                }
            }
        });
    }

    //  When Camera View is started
    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();      //  gray scale matrix instantiated
        mRgba = new Mat();      //  rgba matrix instantiated
    }

    //  When Camera View is stopped
    @Override
    public void onCameraViewStopped() {
        mGray.release();    //  gray scale matrix released
        mRgba.release();    //  rgba matrix released
    }


    //  function to augment rectangles on faces in each camera frame
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();      //  rgba matrix of input frame
        mGray = inputFrame.gray();      //  gray scale matrix of input frame

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        // instantiating matrix of rectangles
        MatOfRect faces = new MatOfRect();

        //  detecting faces from mGray
        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }

        else {//    exception
            Log.e(TAG, "Detection method is not selected!");
        }

        //  array of faces detected
        Rect[] facesArray = faces.toArray();

        if ((facesArray.length>0) && (faceState==SEARCHING))
        {
            //  submatrix of 1st face
            Mat m;
            m=mGray.submat(facesArray[0]);
            mBitmap = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);

            //  converting Matrix to Bitmap
            Utils.matToBitmap(m, mBitmap);
            //  creating message
            Message msg = new Message();
            String textTochange = "IMG";
            msg.obj = textTochange;
            //  predicting student's name from matrix
            textTochange = fr.predict(m);
            mLikely=fr.getProb();
            msg = new Message();
            msg.obj = textTochange;
            mHandler.sendMessage(msg);

        }
        //  state of student
        int state;

        //  augmenting rectangle to each face
        for (int i = 0; i < facesArray.length; i++){
            //  submatrix for i'th face in array
            Mat m;
            m=mGray.submat(facesArray[i]);
            mBitmap = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);

            //  converting Matrix to Bitmap
            Utils.matToBitmap(m, mBitmap);
            Message msg = new Message();
            String textTochange = "IMG";
            msg.obj = textTochange;
            //  predicting student's name in matrix
            textTochange = fr.predict(m);
            //  state of student in database
            state = studentDatabaseHelper.findstate(textTochange);
            //  applying different colours depending on state
            if(state<3){            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),new  Scalar(0, 0, 255, 255), 3);
            }
            else if(state<7){
                Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),new  Scalar(255, 0, 0, 255), 3);
            }
            else{
                Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),new  Scalar(0, 255, 0, 255), 3);
            }
        }
        return mRgba;
    }

    //  on resuming the activity
    @Override
    protected void onResume() {
        super.onResume();
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    //  on pausing the activity
    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    //  on destroying the activity
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }
}
