package XML;

import java.io.*;

class XMLFileWriter extends XMLWriter {  


    /** Make a new XMLFileWriter
     * @param file The file to save the data to
     * @throws IOException If the file cannot be opened because of an IO error
     */
    protected XMLFileWriter(File file) throws IOException { 	
	super(new FileOutputStream(file));
    }
}
