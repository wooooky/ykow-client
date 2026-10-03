package skid.ykow.event;

public abstract class CancellableEvent implements Event {
   private boolean cancelled = false;

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   public void cancel() {
      this.cancelled = true;
   }
}
