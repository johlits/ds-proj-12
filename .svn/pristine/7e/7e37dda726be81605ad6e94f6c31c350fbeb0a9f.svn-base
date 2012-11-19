package time;
import java.util.*;

/**
 * Class providing functions to create a immutable list.
 *
 * @author <a href="mailto:jnm@doc.ic.ac.uk">Jeff Magee</a>
 */
public class ImmutableList {
  ImmutableList next;
  Object item;

  private ImmutableList(ImmutableList next, Object item) {
    this.next = next; this.item=item;
  }

  public static ImmutableList add(ImmutableList list, Object item) {
    return new ImmutableList(list, item);
  }

  public static ImmutableList remove(ImmutableList list, Object target) {
    if (list == null) return null;
    return list.remove(target);
  }

  private ImmutableList remove(Object target) {
    if (item == target) {
      return next;
    } else {
      ImmutableList new_next = remove(next,target);
      if (new_next == next ) return this;
      return new ImmutableList(new_next,item);
    }
  }

  public static Enumeration elements(ImmutableList list) {
        return new ImmutableListEnumerator(list);
  }
}

final class ImmutableListEnumerator implements Enumeration {

    private ImmutableList current;

    ImmutableListEnumerator(ImmutableList l){current=l;};

    public boolean hasMoreElements() {return current != null;}

    public Object nextElement() {
      if (current!=null) {
        Object o = current.item;
        current = current.next;
        return o;
      }
      throw new NoSuchElementException("ImmutableListEnumerator");
    }
}
