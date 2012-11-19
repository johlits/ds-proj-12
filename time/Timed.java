package time;

/**
 * Interface that timed object must implement
 *
 * @author <a href="mailto:jnm@doc.ic.ac.uk">Jeff Magee</a>
 */
public interface Timed {
  void pretick() throws TimeStop;
  void tick();
}
