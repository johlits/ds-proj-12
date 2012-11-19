package XML;

import java.io.*;

/** A utility class to read XML from an InputStream */
abstract class XMLReader extends InputStreamReader { 
    protected final int bufferSize=100;
    protected int bufferBegin,bufferEnd;
    protected boolean atEOF;
    protected char[] buffer;
    
    /** Make a new XMLReader
     * @param stream The InputStream on which this XMLReader is based
     *
     */
    protected XMLReader (InputStream stream) throws IOException { 
	super(stream);
	atEOF=false;
	bufferEnd=0;
	bufferBegin=0;
	buffer=new char[bufferSize];
    }

    /** Read next XML tag
     * @return The tag which is read
     * @throws IOException If a new tag cannot be read because of an IO error
     * @throws EOFException If a new tag cannot be read because we have
     *         arrived at the end of the underlying file.
     */
    public String readNextTag() throws IOException,EOFException { 
	ignoreUntilChar('<',false);
	String result=readUntilChar ('>',true);
	return result;
    }

    /** Move the buffer to the first occurence of a certain char.
     * @param c The specific char
     * @param including True if the buffer should be moved to one position 
     * after the char, false if it should be positioned at the char itself.
     * @throws IOException If a new tag cannot be read because of an IO error
     */
    protected void ignoreUntilChar (char c,boolean including) throws IOException { 
	try { 
	    while (! atEOF ) { 
		fillBuffer();
		// Check the first char in the buffer
		if (bufferBegin<bufferEnd && buffer[bufferBegin]==c) { 
		    if (including) bufferBegin++;
		    return;
		}
		for ( ; bufferBegin< bufferEnd; bufferBegin++) { 
		    if (buffer[bufferBegin]==c) { 
			if (including) bufferBegin++;
			return;  
		    }
		}
	    }
	} catch (EOFException e) { // EOFExceptions are OK. 
	}
    }

    /** Read a string until we encounter a specific character.
     * @param c The specific char
     * @param including True if the char should be included in the string,
     *        false if it should be discarded
     * @return The result	
    * @throws IOException If a new tag cannot be read because of an IO error
    * @throws EOFException If a new tag cannot be read because we have
    *         arrived at the end of the underlying file.
    */
    protected String readUntilChar (char c,boolean including) throws EOFException,IOException { 
	String result="";
	int t;
	while (true) { 
	    fillBuffer();
	    for (t=bufferBegin;t<bufferEnd;t++) { 
		if (c==buffer[t]) { 
		    int endPos=t-bufferBegin;
		    if (including) endPos++;
		    result+=(new String(buffer,bufferBegin,endPos));
		    bufferBegin=t; 
		    return result;
		}
	    }
	    result+=(new String(buffer,bufferBegin,bufferEnd-bufferBegin));
	    bufferBegin=bufferEnd;
	    // This statement is superfluous, just to make really sure the
	    // loop is eventually terminated if the char is never encountered.
	    if (atEOF) throw new EOFException ();
	}
    }

    /** Read a line 
     * @return A string which contains the next line of the file
     * @throws IOException If a new tag cannot be read because of an IO error
     * @throws EOFException If a new tag cannot be read because we have
     *         arrived at the end of the underlying file.
     */
    protected String readln () throws IOException,EOFException { 
	String result=readUntilChar ('\n',false);
	bufferBegin++;
	return result;
    }
    
    /* @return A boolean which indicates if the buffer is empty */
    protected boolean bufferIsEmpty () { 
	return bufferBegin==bufferEnd;
    }

    /** Fill the internal buffer of this Reader
     * @throws IOException If the reader cannot read because of an IO error
     * @throws EOFException If the buffer can't be filled because we
     *         have arrived at the end of the underlying file.
     */
    protected void fillBuffer () throws EOFException,IOException { 
	if (bufferIsEmpty()) { 
	    bufferBegin=0;
	    bufferEnd=super.read(buffer,bufferBegin,bufferSize);
	    if (bufferEnd==-1) { 
		bufferEnd=0;
		atEOF=true;
		throw new EOFException ();
	    }
	}
    }
}









