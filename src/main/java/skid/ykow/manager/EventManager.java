package skid.ykow.manager;

import skid.ykow.Ykow;
import skid.ykow.event.CancellableEvent;
import skid.ykow.event.Event;
import skid.ykow.event.EventListener;
import skid.ykow.event.Listener;
import skid.ykow.module.Module;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventManager {
   private final Map<Class<?>, List<Listener>> EVENTS = new HashMap<>();

   public void register(Object o) {
      Method[] declaredMethods = o.getClass().getDeclaredMethods();

      for (Method method : declaredMethods) {
         if (method.isAnnotationPresent(EventListener.class) && method.getParameterCount() == 1 && Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
            this.addListener(o, method, method.getAnnotation(EventListener.class));
         }
      }
   }

   private void addListener(Object o, Method method, EventListener eventListener) {
      Class<?> key = method.getParameterTypes()[0];
      method.setAccessible(true);
      this.EVENTS.computeIfAbsent(key, p0 -> new CopyOnWriteArrayList<>()).add(new Listener(o, method, eventListener.priority()));
      this.EVENTS.get(key).sort(Comparator.comparingInt(listener -> listener.getPriority().getValue()));
   }

   public void unregister(Object object) {
      for (List<Listener> listeners : this.EVENTS.values()) {
         listeners.removeIf(listener -> listener.getInstance() == object);
      }
   }

   public void clear() {
      this.EVENTS.clear();
   }

   public void keyCodec(Event event) {
      List<Listener> listeners = this.EVENTS.get(event.getClass());
      if (listeners != null) {
         for (Listener listener : listeners) {
            try {
               Object holder = listener.getInstance();
               if ((!(holder instanceof Module) || ((Module)holder).isEnabled()) && (!event.isCancelled() || event instanceof CancellableEvent)) {
                  listener.invoke(event);
               }
            } catch (Throwable var6) {
               System.err
                  .println(
                     "Error dispatching event "
                        + event.getClass().getSimpleName()
                        + " to "
                        + (listener.getInstance() != null ? listener.getInstance().getClass().getSimpleName() : "unknown")
                  );
               var6.printStackTrace(System.err);
            }
         }
      }
   }

   public static void elementCodec(Event event) {
      if (Ykow.INSTANCE != null && Ykow.INSTANCE.getEventBus() != null) {
         Ykow.INSTANCE.getEventBus().keyCodec(event);
      }
   }
}
