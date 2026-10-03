package skid.ykow.event.events;

import skid.ykow.event.CancellableEvent;
import net.minecraft.client.gui.screen.Screen;

public class SetScreenEvent extends CancellableEvent {
   public Screen screen;

   public SetScreenEvent(Screen screen) {
      this.screen = screen;
   }
}
