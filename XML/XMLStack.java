package XML;

/** This class is used by the XMLLoader and the XMLSaver to remember where
  * the parser is saving/loading in the XML tree. XMLStack is an extension
  * of the normal java.util.Stack.
 */

public class XMLStack extends java.util.Stack { 

    String branchName;

  /** Make a new XMLStack */
  public XMLStack () { 
      super();
      updateBranchName();
  }
  
  /** Standard stack push method */
  public Object push (Object x) { 
      Object result=super.push(x);
      updateBranchName();
      return result;
  }
  
  /** Standard stack pop method */
  public Object pop () { 
      Object result=super.pop();
      updateBranchName();
      return result;
  }
  
  /** @return the complete dot separated branchname
   */
  public String getBranchName () { 
      return branchName;
  }
  
  /** Internal method to update the branch name if the stack has changed
   */
  protected void updateBranchName () { 
      branchName=super.toString();
      branchName=branchName.substring(1,branchName.length()-1);
      //branchName=StringUtils.replace(branchName,", ",".");
  }
    
}
