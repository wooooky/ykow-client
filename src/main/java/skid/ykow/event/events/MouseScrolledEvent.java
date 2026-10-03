package skid.ykow.event.events;

import skid.ykow.event.CancellableEvent;

public class MouseScrolledEvent extends CancellableEvent {
   public double amount;

   public MouseScrolledEvent(double amount) {
      this.amount = amount;
   }
}
