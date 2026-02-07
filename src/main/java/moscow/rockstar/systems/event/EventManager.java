package moscow.rockstar.systems.event;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import moscow.rockstar.Rockstar;

public class EventManager {
   private final ConcurrentHashMap<Type, CopyOnWriteArrayList<EventListener<?>>> listenerMap = new ConcurrentHashMap<>();
   private final Map<Class<?>, Field[]> declaredFieldsCache = new HashMap<>();
   private final Comparator<EventListener<?>> priorityOrder = Comparator.<EventListener<?>>comparingInt(listener -> listener.getPriority()).reversed();
   private final BiConsumer<List<EventListener<?>>, Comparator<EventListener<?>>> sortCallback = List::sort;
   private final Consumer<Throwable> errorHandler = Throwable::printStackTrace;

   public void subscribe(Object subscriber) {
      this.modifyEventListenerState(subscriber, (type, listener) -> {
         this.listenerMap.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
         this.sortCallback.accept(this.listenerMap.get(type), this.priorityOrder);
      });
   }

   public void unsubscribe(Object subscriber) {
      this.modifyEventListenerState(subscriber, (type, listener) -> {
         CopyOnWriteArrayList<EventListener<?>> listeners = this.listenerMap.get(type);
         if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
               this.listenerMap.remove(type);
            }
         }
      });
   }

   public <T extends Event> void triggerEvent(T event) {
      Type eventType = event.getClass();
      List<EventListener<?>> listeners = this.listenerMap.get(eventType);
      if (listeners != null && !Rockstar.INSTANCE.isPanic()) {
         for (EventListener<?> listener : listeners) {
            try {
               ((EventListener<T>)listener).onEvent(event);
            } catch (Throwable var7) {
               this.errorHandler.accept(var7);
            }
         }
      }
   }

   private void modifyEventListenerState(Object o, BiConsumer<Type, EventListener<?>> action) {
      for (Field field : this.getCachedDeclaredFields(o.getClass())) {
         if (field.getType() == EventListener.class) {
            EventListener<?> eventListener = this.getEventListener(o, field);
            if (eventListener != null) {
               Type eventType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
               action.accept(eventType, eventListener);
            }
         }
      }
   }

   private Field[] getCachedDeclaredFields(Class<?> clazz) {
      return this.declaredFieldsCache.computeIfAbsent(clazz, Class::getDeclaredFields);
   }

   private EventListener<?> getEventListener(Object o, Field field) {
      boolean accessible = field.canAccess(o);
      field.setAccessible(true);

      Object var5;
      try {
         return (EventListener<?>)field.get(o);
      } catch (IllegalAccessException var9) {
         this.errorHandler.accept(var9);
         var5 = null;
      } finally {
         field.setAccessible(accessible);
      }

      return (EventListener<?>)var5;
   }
}
