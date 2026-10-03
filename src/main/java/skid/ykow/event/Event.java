package skid.ykow.event;

public interface Event {
   default boolean isCancelled() {
      return false;
   }
}
