package XML;

public class XMLAttribute { 
    protected String name,value;

    /** Make a new XMlAttribute. Encapsulate control characters from the
     * parameters so they don't confuse our XML parser.
     * @param name The name of the attribute
     * @param value The value of the attribute
     */
    public XMLAttribute (String name,String value) { 
	this.name = name;
	this.value = value;
    }
    
    public XMLAttribute (String name,int value) { 
	this.name = name;
	this.value = value+"";
    }

    public XMLAttribute (String name,float value) { 
	this.name = name;
	this.value = value+"";
    }

    public XMLAttribute (String name,double value) { 
	this.name = name;
	this.value = value+"";
    }

    public XMLAttribute (String name,boolean value) { 
	this.name = name;
	if (value) this.value = "1";
	else this.value = "0";
    }

    /** @return The name of this attribute*/
    public String getName () { 
	return name;
    }
  
    /** @return The value of this attribute */
    public String getValue () { 
	return value;
    }

    public int getIntValue () { 
	return Integer.parseInt(value);
    }

    public String getStringValue () { 
	return value;
    }

    public float getFloatValue () { 
	return Float.valueOf(value).floatValue();
    }

    public double getDoubleValue () { 
	return Double.valueOf(value).doubleValue();
    }

    public boolean getBooleanValue () { 
	if (Integer.parseInt(value) == 1) return true;
	else return false;
    }

    /** @return A string with name="value". With eventual encapsulation.
     */
    public String toString () { 
	return name+'='+'\"'+value+'\"';
    }
}






