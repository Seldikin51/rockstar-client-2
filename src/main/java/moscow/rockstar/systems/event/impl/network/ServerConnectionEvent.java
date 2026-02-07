package moscow.rockstar.systems.event.impl.network;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class ServerConnectionEvent extends Event {
   private final ServerAddress address;
   private final ServerInfo info;
   private final CookieStorage cookieStorage;

   @Generated
   public ServerAddress getAddress() {
      return this.address;
   }

   @Generated
   public ServerInfo getInfo() {
      return this.info;
   }

   @Generated
   public CookieStorage getCookieStorage() {
      return this.cookieStorage;
   }

   @Generated
   public ServerConnectionEvent(ServerAddress address, ServerInfo info, CookieStorage cookieStorage) {
      this.address = address;
      this.info = info;
      this.cookieStorage = cookieStorage;
   }
}
