package cultoftheunicorn.marvel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;


import android.util.Log;


public class Labels {

	String mPath;	//	location of file in device
	//	constructor
	class label {
		public label(String s, int n) {
			thelabel=s;
			num=n;
		}

		int num;
		//  name of file
		String thelabel;
	}

	//  array of labels
	ArrayList<label> thelist=new ArrayList<label>();

	public Labels(String Path)
	{
		mPath=Path;
	}   //  setting path for label

	public void add(String s,int n)
	{
		thelist.add( new label(s,n));
	}   //  adding an entry in label

    //  function to return label at position i in thelist
	public String get(int i) {
		Iterator<label> Ilabel = thelist.iterator();
		while (Ilabel.hasNext()) {
			label l = Ilabel.next();        //  moving forward
			if (l.num==i)       //  comparing two positions
				return l.thelabel;
		}
		return "";
	}

    //  function to return position of string s in thelist
	public int get(String s) {
		Iterator<label> Ilabel = thelist.iterator();
		while (Ilabel.hasNext()) {
			label l = Ilabel.next();        //  moving forward
			if (l.thelabel.equalsIgnoreCase(s))     //  comparing two strings
				return l.num;
		}
		return -1;
	}

	//  function to save labels to text file
	public void Save() {
		try {
		    //  creating file f at specified path
			File f=new File (mPath+"faces.txt");
			f.createNewFile();
			//  writing labels in file
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			Iterator<label> Ilabel = thelist.iterator();    //  iterator for thelist
			while (Ilabel.hasNext()) {
				label l = Ilabel.next();
				bw.write(l.thelabel+","+l.num);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
		    //  error exception
			Log.e("error",e.getMessage()+" "+e.getCause());
			e.printStackTrace();
		}
	}

	//  function to read text file containing labels to add in thelist
	public void Read() {
		try {
            //  creating input file stream
			FileInputStream fstream = new FileInputStream(
					mPath+"faces.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));

			String strLine;     //  a line in input stream
			thelist= new ArrayList<label>();
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				StringTokenizer tokens=new StringTokenizer(strLine,",");    //  Tokenize strLine
				String s=tokens.nextToken();
				String sn=tokens.nextToken();

				thelist.add(new label(s,Integer.parseInt(sn)));     //  adding string to thelist
			}
			br.close();
			fstream.close();
		} catch (IOException e) {
		    //  exception
			e.printStackTrace();
		}
	}

	//  return maximum number of imager any label has
	public int max() {
		int m=0;
		Iterator<label> Ilabel = thelist.iterator();
		while (Ilabel.hasNext()) {
			label l = Ilabel.next();
			if (l.num>m) m=l.num;
		}
		return m;
	}

}
