package org.egc.core.data.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * This class will hold IO operations for various types of variables: arrays, matrices, lists etc
 * @author geopapa
 *
 */
public class IOUtil {

	private static final Logger logger = LogManager.getLogger(IOUtil.class);

	/**********************************
	 **       I OPERATIONS           **
	 **********************************/

  	 /////////////////////////
	 //   READ FROM FILE   //
    ////////////////////////

	public static String[] readFile2Array(String f) {
		return readFile2Array(f, "\t");
	}

	public static String[] readFile2Array(String f, String delim) {
		String[][] a = readFile(f, delim, -1);
		return a[0];
	}

	public static String[][] readFile(String f, int num_cols) {
		return readFile(f, "\t", num_cols);
	}

	public static String[][] readFile(String f, String delim) {
		return readFile(f, delim, 0);
	}

	/**
	 *
	 * @param f file from where the array will be read
	 * @param delim delimiter used
	 * @param num_cols number of columns of the array
	 * @return
	 */
	public static String[][] readFile(String f, String delim, int num_cols) {
		String[][] a = new String[0][];
		try { a = readStream(new FileInputStream(new File(f)), delim, num_cols); }
		catch (FileNotFoundException e) { logger.error("File not found: {}", f, e); }
		return a;
	}


	 ///////////////////////////
	 //   READ FROM STREAM   //
    //////////////////////////

	public static String[] readStream2Array(InputStream os) {
		return readStream2Array(os, "\t");
	}

	public static String[] readStream2Array(InputStream os, String delim) {
		String[][] a = readStream(os, delim, -1);
		return a[0];
	}

	public static String[][] readStream(InputStream os, int num_cols) {
		return readStream(os, "\t", num_cols);
	}

	public static String[][] readStream(InputStream os, String delim) {
		return readStream(os, delim, 0);
	}

	/**
	 *
	 * @param os The output stream where the array will be stored
	 * @param a array to be stored
	 * @param delim delimiter used
	 * @param num_cols number of array elements in each line (row) of the stream
	 */
	public static String[][] readStream(InputStream os, String delim, int num_cols) {
		String[][] a = new String[0][];
		ArrayList<String[]> al = new ArrayList<String[]>();
		BufferedReader br = null;

		try {

			InputStreamReader osr = new InputStreamReader(os);
			br = new BufferedReader(osr);

			if( num_cols > 0 ) {
				String str;
				int count = 0;
				String[] leftover = new String[0];
				while((str = br.readLine()) != null) {
					String[] tokens = str.split(delim);
					String[] els = concat_strarrays(leftover, tokens);
					count = els.length;

					int quot = count/num_cols;

					int ind = 0;
					if(quot != 0) {
						for(int i = 0; i < quot; i++) {
							String[] curr_els = new String[num_cols];
							for(int j = 0; j < num_cols; j++) { curr_els[j] = els[ind++]; }
							al.add(curr_els);
						}
						count %= num_cols;
					}

					leftover = new String[count];
					for(int k = 0; k < count; k++)
						leftover[k] = els[ind++];
				}
				al.add(leftover);
			}

			else if( num_cols == 0 ) {
				String str;
				while((str = br.readLine()) != null) {
					String[] tokens = str.split(delim);
					al.add(tokens);
				}
			}

			else if( num_cols == -1 ) {
				ArrayList<String> temp_list = new ArrayList<String>();
				String str;
				while((str = br.readLine()) != null) {
					String[] tokens = str.split(delim);
					for(int i = 0; i < tokens.length; i++) { temp_list.add(tokens[i]); }
				}
				al.add(temp_list.toArray(new String[0]));
			}
			else { throw new IllegalArgumentException("Invalid value for num_cols. The valid values are -1 (one row), 0 (as they are on the file), k (columns in each row)."); }

			a = new String[al.size()][];
			for(int i = 0; i < a.length; i++) { a[i] = al.get(i); }
		}
		catch (IOException e) { logger.error("Error reading stream", e); }
		finally {
			try { br.close(); }
			catch (IOException e) { logger.error("Error closing stream", e); }
		}

		return a;
	}//end of readStream method


	private static String[] concat_strarrays(String[] ar1, String[] ar2) {
		String[] ar = new String[ar1.length+ar2.length];
		int count = 0;
		for(int i = 0; i < ar1.length; i++) { ar[count++] = ar1[i]; }
		for(int i = 0; i < ar2.length; i++) { ar[count++] = ar2[i]; }
		return ar;
	}

}
