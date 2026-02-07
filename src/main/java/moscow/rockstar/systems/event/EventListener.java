package moscow.rockstar.systems.event;

public interface EventListener<T extends Event> {
   void onEvent(T var1);

   default int getPriority() {
      return 0;
   }
}
