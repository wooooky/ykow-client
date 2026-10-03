package skid.ykow.event.events;

import skid.ykow.event.CancellableEvent;

public class PostItemUseEvent extends CancellableEvent {
   public int cooldown;

   public PostItemUseEvent(int cooldown) {
      this.cooldown = cooldown;
   }
}
