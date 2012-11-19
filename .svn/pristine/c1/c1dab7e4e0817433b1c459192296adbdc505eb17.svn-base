package XML;

import java.io.*;
import java.util.*;

/** This class can restore the state of the program from an XML file. It also
  * provides methods which XMLSerializable objects can use to retrieve
  * their child objects.
 */
public class XMLLoader { 
    protected XMLReader reader;
    protected XMLElement currentElement;
    protected XMLElement nextElement;
    
    /** Make a new XMLLoader which loads from a file
     * @param file The file to load from
     * @throws IOException If the XMLLoader cannot read from the XML file 
     * because of an IO error.
     */     
    public XMLLoader (InputStream stream) throws IOException {
	reader=new XMLFileReader(stream);
	currentElement = getNextElement(); 
	nextElement = getNextElement();
    }
   
    public void loadObject(XMLSerializable object) {
	String fullName = object.getXMLName();

	XMLElement element = getElement(fullName);
	object.loadSelf(element);
	object.loadChilds(this);
    }

    public void close () {	
	try {
	    reader.close();
	} catch (IOException e) {	
	    System.out.println ("Warning : could not close XMLLoader : "+e);
	}
    }

    public String getNextElementName() {
	if (nextElement == null) return "END OF XML FILE";
	else return nextElement.name;
    }

    public void moveOnOne() {
	currentElement = nextElement;
	nextElement = getNextElement();
    }

    private XMLElement getElement(String fullName) {
	moveOnOne();
	while (!currentElement.name.equals(fullName)) moveOnOne();
	return currentElement;
    }

    private XMLElement getNextElement() {
	String tag= getNextTag();
	while (tag.startsWith ("</")) {
	    if (tag.equals("</xml>")) return null;
	    tag = getNextTag();
	}
	return parse(tag);
    }

    private String getNextTag() {
	try {
	    String tag=reader.readNextTag();
	    return tag;
	} catch (IOException e) { // EOFExceptions are OK. 
	    System.out.println ("IOException e, End Of File?");
	    return null;
	}
    }

    /** Parse a new XML element from a string which contains a XML tag
     * @param string The string to parse
     */
    public XMLElement parse(String string) { 
	StringTokenizer t,a;
	String original=new String(string),attName,attValue; 
	if ( string.startsWith ("</")) { 
	    System.out.println("XMLElement.parse cannot handle close tags: "+original);
	}
	if (! (string.startsWith("<") && string.endsWith (">"))) {
	    System.out.println("Invalid XML tag (no < or > ) :"+original);
	}

	string=string.substring(1,string.length()-1);
	t=new StringTokenizer(string," ");

	if (! t.hasMoreTokens() ) { 
	    System.out.println("XML tag without valid name "+original);
	}

	XMLElement result=new XMLElement(t.nextToken());
    
	while (t.hasMoreTokens()) { 
	    a=new StringTokenizer(t.nextToken(),"=");
	    if (a.countTokens()!=2) {
		System.out.println("Wrong attribute in XML tag :"+original);
	    }
	    attName=a.nextToken();
	    attValue=a.nextToken();
	    if (! (attValue.startsWith("\"") && attValue.endsWith ("\""))) { 
		System.out.println("Wrong attribute value in XML tag (no quotes?) :"+attValue+"/"+original);
	    }
	    result.addAttribute(new XMLAttribute(attName,attValue.substring(1,attValue.length()-1)));
	}
	return result;	      
    }
}



    /*
    private boolean isOpeningTag(String tag, String fullName) {
	System.out.print("in getOpeningTag, tag= " +tag + 
			    ", fullName= "+fullName);
	if (tag.length() < fullName.length()+2) {
	    System.out.println(" returning false");
	    return false;
	} else {
	    return tag.substring(1,fullName.length()+1).equals(fullName);
	}
    }

    private boolean isClosingTag(String tag, String fullName) {
	System.out.println ("in getClosingTag, tag= " +tag + 
			    "fullName= "+fullName);
	if (tag.length() < fullName.length()+3) return false;
	return (tag.substring(2,fullName.length()+2) == fullName); 
    }
    */




    /*
      System.out.println ("In getElement currentElement= "+
      currentElement.name);
      
      if (nextElement == null) {
      System.out.println ("nextElement == null");
      } else {
      System.out.println ("In getElement nextElement= "+
      nextElement.name);
      }    
    */














