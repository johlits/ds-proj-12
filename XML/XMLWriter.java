package XML;

import java.io.*;

class XMLWriter extends PrintWriter { 	

    OutputStream stream;

    public XMLWriter (OutputStream stream) { 
	super(stream);
    }

    /** Fill a string with several occurences of a char
     * @param c The char to fill the string with
     * @param times The length of the string
     * @return A string which consists of "times" times char c 
     */
    public static String repeat(char c,int times) { 
	String result="";
	while (times > 0 ) { 
	    if ( (times % 2)==0 && result.length()>0) { 
		result+=result;
		times/=2;
	    } else { 
		result+=c;
		times--;
	    }
	}
	return result;
    }
	
    /** Write the open tag of a XMLElement 
     * @param element The XMLElement
     * @param indent The number of spaces to indent the tag
     */
    public void writeOpenTag (XMLElement element,int indent) {
	println(repeat(' ',indent)+element.getOpenTag());
    }
	
    /** Write the open tag of a XMLElement 
     * @param element The XMLElement
     */
    public void writeOpenTag (XMLElement element) {	
	writeOpenTag(element,0);
    }

    /** Write the close tag of a XMLElement 
     * @param element The XMLElement
     * @param indent The number of spaces to indent the tag
     */
    public void writeCloseTag (XMLElement element,int indent) { 	
	println(repeat(' ',indent)+element.getCloseTag());
    }
    
  /** Write the close tag of a XMLElement 
   * @param element The XMLElement
   */
    public void writeCloseTag (XMLElement element) {	
	writeCloseTag(element,0);
    }
}





