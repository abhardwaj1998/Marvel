package cultoftheunicorn.marvel;

import static  com.googlecode.javacv.cpp.opencv_highgui.*;
import static  com.googlecode.javacv.cpp.opencv_core.*;

import static  com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.MatVector;

import android.graphics.Bitmap;
import android.util.Log;

//	Describing class StudentRecognizer
public  class StudentRecognizer {

	FaceRecognizer faceRecognizer;      //  defining faceRecognizer variable
	String mPath;       //  Path of stored images in device
	int count=0;        //  number of images stored
	Labels labelsFile;      //  Applying names to the files stored

	static  final int WIDTH= 128;       //     width of BitMap
	static  final int HEIGHT= 128;;     //      height of BitMap
	private int mProb=999;      //  maximum probability

    //  Constructing StudentRecognizer
	StudentRecognizer(String path) {
		faceRecognizer =  com.googlecode.javacv.cpp.opencv_contrib.createLBPHFaceRecognizer(2,8,8,8,200);
		mPath=path;
		labelsFile= new Labels(mPath);

	}

	//  adding new BitMap Image
	void add(Mat m, String description) {
	    //  Creating BitMap for conversion
		Bitmap bmp= Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);

		//  Converting matrix format to BitMap
		Utils.matToBitmap(m,bmp);

		//  Scaling of Bitmap
		bmp= Bitmap.createScaledBitmap(bmp, WIDTH, HEIGHT, false);

		//  Saving BitMap
		FileOutputStream f;
		try {
		    //  initializing FileOutputStream
			f = new FileOutputStream(mPath+description+"-"+count+".jpg",true);
			count++;
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, f);   //  compressing BitMap
			f.close();

		} catch (Exception e) {
		    //  exception
			Log.e("error",e.getCause()+" "+e.getMessage());
			e.printStackTrace();

		}
	}

	//  function to train OpenCV
	public boolean train() {

		File root = new File(mPath);        //  initializing file with name root

        //  filter out files with ".jpg" extension
		FilenameFilter pngFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jpg");

			};
		};

		File[] imageFiles = root.listFiles(pngFilter);  //  getting all images in array imageFiles

		MatVector images = new MatVector(imageFiles.length);    //  MatVector of size equal to total number of images.

		int[] labels = new int[imageFiles.length];  //  array with names of imageFiles

		int counter = 0;
		int label;

		IplImage img;
		IplImage grayImg;

		int i1=mPath.length();

        //  running for loop on all files
		for (File image : imageFiles) {
			String p = image.getAbsolutePath();     //  path of image
			img = cvLoadImage(p);       //  loading image in openCV

            //  exception
			if (img==null)
				Log.e("Error","Error cVLoadImage");
			Log.i("image",p);

			int i2=p.lastIndexOf("-");
			int i3=p.lastIndexOf(".");
			int icount=Integer.parseInt(p.substring(i2+1,i3));      //  image count
			if (count<icount) count++;      //increasing count

			String description=p.substring(i1,i2);      //  name of student in image

            //  if student is not present in labelsFile
			if (labelsFile.get(description)<0)
				labelsFile.add(description, labelsFile.max()+1);

			label = labelsFile.get(description);    //  using student from labelsFile

            //  creating grayscale image from img.
			grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1);

			//  converting img from grayscale to BGR2GRAY color scheme
			cvCvtColor(img, grayImg, CV_BGR2GRAY);

			//  storing grayImg in images
			images.put(counter, grayImg);

			//  adding label to labels array
			labels[counter] = label;

			//  increasing counter
			counter++;
		}

		if (counter>0)
			if (labelsFile.max()>1)
				faceRecognizer.train(images, labels);   //  recursive call for other images of student
		labelsFile.Save();  //  saving labels file
		return true;
	}

	//  whether student can be predicted from existing images or not
	public boolean canPredict()
	{
		if (labelsFile.max()>1)
			return true;
		else
			return false;

	}

	//  predicting student from his matrix image
	public String predict(Mat m) {

	    //  if cannot predict student
		if (!canPredict())
			return "";


		int n[] = new int[1];
		double p[] = new double[1];     //  probability array
		IplImage ipl = MatToIplImage(m,WIDTH, HEIGHT);      //  converting matrix image to Ipl Image

        //  Calling predict() function to predict student from ipl and putting results in arrays n and p
		faceRecognizer.predict(ipl, n, p);

		if (n[0]!=-1)
			mProb=(int)p[0];        //  probability
		else
			mProb=-1;       //  probability

		if (n[0] != -1)
			return labelsFile.get(n[0]);        //  name of recognized student
		else
			return "Unknown";
	}

    //  function to covert Matrix image to Ipl image of given width and height

	IplImage MatToIplImage(Mat m,int width,int heigth)
	{
        //  creating bitmap of size of given matrix
		Bitmap bmp=Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);

        //  converting matrix to bitmap
		Utils.matToBitmap(m, bmp);

		//  convert bitmap to Ipl and return the result
		return BitmapToIplImage(bmp,width, heigth);

	}

	//  Converting BitMap image to Ipl image of given width and height
	IplImage BitmapToIplImage(Bitmap bmp, int width, int height) {

		if ((width != -1) || (height != -1)) {
		    //  creating scaled BitMap if width or height = -1
			Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, width, height, false);
			bmp = bmp2;
		}

		//  creating Ipl image of height and width same as of BitMap
		IplImage image = IplImage.create(bmp.getWidth(), bmp.getHeight(),
				IPL_DEPTH_8U, 4);

		//  converting BitMap to Ipl image
		bmp.copyPixelsToBuffer(image.getByteBuffer());

		//  Obtaining GrayScale image of Ipl image of same width and height
		IplImage grayImg = IplImage.create(image.width(), image.height(),
				IPL_DEPTH_8U, 1);

		//  creating GrayScale image
		cvCvtColor(image, grayImg, opencv_imgproc.CV_BGR2GRAY);

		return grayImg;
	}


    //  saving given BitMap image to specified path
	protected void SaveBmp(Bitmap bmp,String path)
	{
	    //  file output stream for saving bitmap
		FileOutputStream file;
		try {
			file = new FileOutputStream(path , true);   //  initializing file at given path

			bmp.compress(Bitmap.CompressFormat.JPEG,100,file);  //  saving BitMap
			file.close();
		}
		//  error
		catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("",e.getMessage()+e.getCause());
			e.printStackTrace();
		}

	}

    //  on loading
	public void load() {
		train();

	}

	//  function to get probability
	public int getProb() {
		// TODO Auto-generated method stub
		return mProb;
	}


}
