package cultoftheunicorn.marvel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
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

public class Training extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";
    //  rectangle color is set to green
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    public static final int TRAINING= 0;        //  if system is getting trained
    public static final int IDLE= 2;        //  if system is not getting trained


    private int faceState=IDLE;

    private Mat                    mRgba;       //  coloured matrix
    private Mat                    mGray;       //  grayScale matrix
    private File mCascadeFile;          //  Cascade file
    private CascadeClassifier mJavaDetector;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private int mLikely=999;

    String mPath="";        //  path of image

    private CameraView mOpenCvCameraView;      //  OpenCv Camera View object

    String nameOfStudent;
    private ImageView imageView;
    Bitmap mBitmap;
    Handler mHandler;

    StudentRecognizer fr;       //  student's face recognizer
    ToggleButton capture;       //  capture button

    static final long MAXIMG = 10;

    int countImages=0;      //  number of images

    Labels labelsFile;      //  file with names of students

    //  Loading OpenCV
    static {
        OpenCVLoader.initDebug();
        System.loadLibrary("opencv_java");
    }

    //  Constructor for Training Class
    public Training() {
        mDetectorName = new String[2];
        //  initialization
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    //  loading OpenCV
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:   //  if OpenCV loaded successfully
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //  creating an object of StudentRecognizer
                    fr=new StudentRecognizer(mPath);

                    fr.load();  //  loading the StudentRecognizer object

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);       //  creating a file output stream for mCascadeFile
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        //  writing input stream to mCascadeFile
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();
                        //  instantiating CascadeClassifier object of mCascadeFile
                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            //  exception if instantiation failed
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {
                        //  failed to load cascade
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    //  starting view of mOpenCvCameraView
                    mOpenCvCameraView.enableView();

                } break;
                default:
                {
                    //  re-trying
                    super.onManagerConnected(status);
                } break;


            }
        }
    };

    //  onCreate function to set up display and variables
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  setting display view of the activity
        setContentView(R.layout.activity_training);
        //  getting name of the student
        nameOfStudent = getIntent().getStringExtra("name");
        //  image preview
        imageView = (ImageView) findViewById(R.id.imagePreview);
        //  button to capture image
        capture = (ToggleButton) findViewById(R.id.capture);
        capture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                captureOnClick();
            }
        });
        //  Displaying modified Camera view
        mOpenCvCameraView = (CameraView) findViewById(R.id.training_java_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        //  path of storing images
        mPath = Environment.getExternalStorageDirectory()+"/facerecogOCV/";

        Log.e("Path", mPath);
        //  file to store names of students
        labelsFile= new Labels(mPath);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj=="IMG")
                {
                    //  creating a canvas for new image
                    Canvas canvas = new Canvas();
                    //  setting up canvas and Image View
                    canvas.setBitmap(mBitmap);
                    imageView.setImageBitmap(mBitmap);
                    if (countImages>=MAXIMG-1)
                    {
                        //  if number of images are greater than 8 then stop capturing
                        capture.setChecked(false);
                        captureOnClick();
                    }
                }
            }
        };
        //  creating directory for saving images
        boolean success=(new File(mPath)).mkdirs();

        if (!success)
            Log.e("Error","Error creating directory");

    }

    void captureOnClick()
    {   //  System is being trained
        if (capture.isChecked())
            faceState = TRAINING;
        else {
            //  system has captured image
            Toast.makeText(this, "Captured", Toast.LENGTH_SHORT).show();
            countImages=0;
            faceState=IDLE;
            imageView.setImageResource(R.drawable.user_image);
        }
    }

    //  initializing for grayScale and coloured matrix image
    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    //  deallocating mGray and mRgba
    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    //  function to obtain face of student from input frame from camera
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();  //  rgba format of input frame
        mGray = inputFrame.gray();  //  grayscale format of input frame


        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }

        }

        //  rectangles of faces
        MatOfRect faces = new MatOfRect();
        //  if detector type is JAVA_DETECTOR
        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                //  detect faces in mGray and store in faces of sizes mAbsoluteFaceSize
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }

        else {
            //  displaying warning
            Log.e(TAG, "Detection method is not selected!");
        }

        //  creating array of faces
        Rect[] facesArray = faces.toArray();
        //  if single student is detected
        if ((facesArray.length==1)&&(faceState==TRAINING)&&(countImages<MAXIMG)&&(!nameOfStudent.equals("")))
        {

            //  Matrix m
            Mat m;
            //  rectangle of student's face
            Rect r=facesArray[0];

            m=mRgba.submat(r);  //  extracting sub-matrix of student's face from mRgba
            //  creating Bitmap of size same as that of m
            mBitmap = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);

            //  converting matrix to bitmap
            Utils.matToBitmap(m, mBitmap);
            //  creating message for message handler
            Message msg = new Message();
            String textTochange = "IMG";
            msg.obj = textTochange;
            mHandler.sendMessage(msg);
            if (countImages<MAXIMG)
            {   //  add m to face recognizer under name nameOfStudent
                fr.add(m, nameOfStudent);
                countImages++;
            }

        }
        //  applying rectangles on detected faces from facesArray
        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;
    }

    //  on resuming activity
    @Override
    protected void onResume() {
        super.onResume();
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    //  on pausing activity
    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    //  on destroying activity
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }
}
