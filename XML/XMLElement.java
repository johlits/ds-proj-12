package XML;

import java.util.*;

// TODO:  - Write JUnit test

public class XMLElement {

    protected String name;
    protected Vector attributes;
  
    /** Make a new XMLElement
     * @param name The name of the new element
     */
    public XMLElement (String name) { 
	this.name=name;
	attributes=new Vector();
    }

    /** Add a new attribute to the attribute list
     * @param attribute The new attribute
     */
    public void addAttribute (XMLAttribute attribute) { 
	attributes.add(attribute);
    }

    protected int getAttributeIndex (String name) { 
	for (int t=0;t<attributes.size();t++) { 
	    if (name.equals(((XMLAttribute)(attributes.elementAt(t))).name))
		return t;
	}
	System.out.println("Could not find XML attribute named "+name);   
	return -1;
    }

    public XMLAttribute getAttribute (String name) { 
	return (XMLAttribute)(attributes.elementAt(getAttributeIndex(name)));
    }

    /** @return The attribute list in array form
     */
    public XMLAttribute[] getAttributesArray () { 
	XMLAttribute[] result=new XMLAttribute [attributes.size()];
	return (XMLAttribute[])(attributes.toArray(result));
    }

    /** @return a string which represents the XML tag which opens this
     *         element in the XML file
     */
    public String getOpenTag () { 
	String result="<"+name;
	XMLAttribute[] attlist=getAttributesArray();
	for (int t=0;t<attlist.length;t++)
	    result+=" "+attlist[t].toString();
	result+=">";
	return result;
    }  

    /** @return Returns a string which represents the XML tag which closes this
     *         element in the XML file.
     */
    public String getCloseTag () { 
	return "</"+name+">";
    }  
}







