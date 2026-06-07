package org.egc.core.motifs;
import java.io.*;
import java.util.*;

import org.egc.core.data.motifdb.MarkovBackgroundModel;
import org.egc.core.data.motifdb.WeightMatrix;


public class FreqMatrixImport {

	private static MarkovBackgroundModel back = null;
	private static int[] indices = { 'A', 'C', 'G', 'T' };
	private static int MAX_MOTIF_LEN = 200;
	private static float SCALE_FACTOR = (float) 0.1;

    public void setBackground(MarkovBackgroundModel b){back=b;}

    //Unlike the WeigthMatrixImport version, this one actually loads more than one matrix!
    public static LinkedList<WeightMatrix> readTransfacMatrices(String wmfile, String version) throws IOException {
        LinkedList<WeightMatrix> matrices = new LinkedList<WeightMatrix>();
        BufferedReader br = new BufferedReader(new FileReader(new File(wmfile)));
        String line;
        WeightMatrix matrix = null;
        int motifCount=0;
        Vector<float[][]> arrays = new Vector<float[][]>();
        Vector<Integer> arrayLens = new Vector<Integer>();
        Vector<String> names = new Vector<String>();
        Vector<String> versions=new Vector<String>();

        //Read in Transfac format first
        boolean nameLoaded=false;
        int matLen=0;
        while((line = br.readLine()) != null) {
            line = line.trim();
            if(line.length() > 0) {
            	String[] pieces = line.split("\\s+");
                if(pieces[0].equals("DE")){
                	names.add(pieces[1]);
                	if(pieces.length>=3){
                		String v_string =pieces[2];
                		if(pieces.length>=4){for(int v=3; v<pieces.length; v++){v_string =v_string+","+pieces[v];}}

                		if(version!=null){versions.add(new String(v_string+","+version));}
                		else{versions.add(v_string);}
                	}else{
                		versions.add(version);
                	}
                    nameLoaded=true;
                    arrays.add(new float[MAX_MOTIF_LEN][4]);
                    matLen=0;
                }else if(pieces[0].equals("XX")){
                	arrayLens.add(matLen);
                	motifCount++;
                }else if(nameLoaded && (pieces.length==5 || pieces.length==6)){
                	//Load the matrix
                	for(int i = 1; i <=4 ; i++) {
                        arrays.get(motifCount)[matLen][i-1] = Float.parseFloat(pieces[i]);
                    }
                    matLen++;
                }
            }
        }
        for(int m = 0; m<motifCount; m++){
        	//Make a new WeightMatrix
            matrix = new WeightMatrix(arrayLens.get(m));
            matrix.name=names.get(m);
            matrix.version=versions.get(m);

            //Convert the freq matrix to a weight matrix
            for(int i = 0; i < arrayLens.get(m); i++) {
            	float ttl=0;
            	for(int j=0; j<4; j++){ttl += arrays.get(m)[i][j];}
            	float currScale = SCALE_FACTOR*ttl;
        		for(int j = 0; j < 4; j++) {
        			matrix.matrix[i][indices[j]] = (float)(Math.log((((arrays.get(m)[i][j] + (currScale*back.getMarkovProb(j, 1)))/(ttl+currScale))/back.getMarkovProb(j, 1)))/Math.log(2));
                }
            }
            matrices.add(matrix);
           System.err.println("Added \"" + matrix.name + "\"\t"+matrix.version);
	   System.err.println("Max score = "+matrix.getMaxScore());
           System.err.println(WeightMatrix.printMatrix(matrix));
        }

        br.close();
        return matrices;
    }
    public static LinkedList<WeightMatrix> readTransfacMatrices(String wmfile, String version, float pseudocountTotal){
    	SCALE_FACTOR = pseudocountTotal;
    	LinkedList<WeightMatrix> matrices = new LinkedList<WeightMatrix>();
    	try {
			matrices = readTransfacMatrices(wmfile, version);
		} catch (IOException e) {
			e.printStackTrace();
		}return(matrices);
    }
    public static LinkedList<WeightMatrix> readTransfacMatrices(String wmfile){
    	LinkedList<WeightMatrix> matrices = new LinkedList<WeightMatrix>();
    	try {
			matrices = readTransfacMatrices(wmfile, "");
		} catch (IOException e) {
			e.printStackTrace();
		}return(matrices);
    }

}
