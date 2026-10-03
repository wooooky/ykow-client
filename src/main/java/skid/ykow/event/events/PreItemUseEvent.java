package skid.ykow.event.events;

import skid.ykow.event.CancellableEvent;

public class PreItemUseEvent extends CancellableEvent {
   public int cooldown;

   public PreItemUseEvent(int cooldown) {
      this.cooldown = cooldown;
   }
}
