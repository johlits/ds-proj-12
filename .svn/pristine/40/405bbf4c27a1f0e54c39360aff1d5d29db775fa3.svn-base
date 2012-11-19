package XML;

import java.io.*;
import java.util.*;

public class XMLSaver {

    protected XMLWriter writer;
    protected XMLStack stack;
  
    public XMLSaver (File file) throws IOException {
	writer=new XMLFileWriter(file);
	stack=new XMLStack();
	writer.println("<?xml version=\"1.0\"?>");
    }

    public void saveObject (XMLSerializable object) {
	String fullName = object.getXMLName();
	XMLElement element = object.saveSelf();

	stack.push(fullName);
	writer.writeOpenTag(element,indent());
	object.saveChilds(this);
	writer.writeCloseTag(element,indent());
	stack.pop();
    }

    public void close () {   
	writer.println("</xml>");
	writer.flush();
	writer.close();
	writer=null;
    }

    protected int indent () { return stack.size(); }
}
    
