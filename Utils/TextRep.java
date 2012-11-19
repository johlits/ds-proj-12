package Utils;

import java.io.*;
import java.util.*;

/**
 * A class to open a file, copy it to another name and replace strings in the 
 * copy with other strings.
 */
public class TextRep {
    //args[0] = source file.
    //args[1] = sink file.
    //args[i] = search string.
    //args[i+1] = replace String.
    public TextRep (String[] args) {
	
	try {
	    // make 32k buffer for output
	    StringBuffer strOutput = new StringBuffer(32768);

	    // read input file into a byte array
	    byte[] pInput = ReadFile(args[0]);
	    //WriteFile(args[1],pInput);
	    String strInput = new String(pInput);
	    				
	    
	    int nCount = args.length-2;
	    
	    if(nCount==0||nCount%2!=0) {
		System.out.println("Argument strings should be in pairs");
		return;
	    }
			
	    for(int i=2;i<args.length;i+=2) {
		
		// string to search in the input
		String strSearch = args[i];
		
		// string used to replace the previous one
		String strReplace = args[i+1];
		
		// check if words are empty
		if(strSearch.equals("")||strReplace.equals("")) {
		    System.out.println("Cannot process empty words");
		    return;
		}
		
		// replace each instance of strSearch with strReplace
		System.out.println("Replacing \""+strSearch+"\" with \""+strReplace+"\"");
		int nPos = 0;
		while(true) {
		    int nIndex = strInput.indexOf(strSearch,nPos);
		    // if strSearch can no longer be found, then copy the rest of the input
		    if(nIndex<0) {
			strOutput.append(strInput.substring(nPos));
			break;
		    }
		    // otherwise, replace it with strReplace and continue
		    else {
			strOutput.append(strInput.substring(nPos,nIndex));
			strOutput.append(strReplace);
			nPos = nIndex+strSearch.length();
		    }
		}
		// continue to process the next pair of string tokens
		strInput = strOutput.toString();
		strOutput = new StringBuffer(32768);
	    }
	
	    // write the output string to file
	    WriteFile(args[1],strInput.getBytes());
	} catch(Exception e) {
	    System.out.println(e.getMessage());
	}
    }
    
    // helper function to read a file into a byte array
    static public final byte[] ReadFile(String strFile) throws IOException {
	int nSize = 32768;
	// open the input file stream
	BufferedInputStream inStream = 
	    new BufferedInputStream(new FileInputStream(strFile),nSize);
	byte[] pBuffer = new byte[nSize];
	int nPos = 0;
	// read bytes into a buffer
	nPos += inStream.read(pBuffer,nPos,nSize-nPos);
	// while the buffer is filled, double the buffer size and read more
	while(nPos==nSize) {
	    byte[] pTemp = pBuffer;
	    nSize *= 2;
	    pBuffer = new byte[nSize];
	    System.arraycopy(pTemp,0,pBuffer,0,nPos);
	    nPos += inStream.read(pBuffer,nPos,nSize-nPos);
	}
	// close the input stream
	inStream.close();
	if(nPos==0) {
	    return "".getBytes();
	}
	// return data read into the buffer as a byte array
	byte[] pData = new byte[nPos];
	System.arraycopy(pBuffer,0,pData,0,nPos);
	return pData;
    }
    
    // helper function to write a byte array into a file
    static public final void WriteFile(String strFile, byte[] pData) throws IOException {
	BufferedOutputStream outStream = 
	    new BufferedOutputStream(new FileOutputStream(strFile),32768);
	if(pData.length>0) outStream.write(pData,0,pData.length);
	outStream.close();
    }
}

