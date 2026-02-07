package moscow.rockstar.mixin.minecraft.client.network;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.game.PickupEvent;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.modules.modules.visuals.XRay;
import moscow.rockstar.utility.game.WorldUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.rotations.Rotation;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler implements IMinecraft {
   @Unique
   private Rotation oldRotation = Rotation.ZERO;

   protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
      super(client, connection, connectionState);
   }

   @Inject(
      method = "onItemPickupAnimation",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getEntityById(I)Lnet/minecraft/entity/Entity;", ordinal = 0)
   )
   private void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet, CallbackInfo info) {
      Entity itemEntity = this.client.world.getEntityById(packet.getEntityId());
      Entity entity = this.client.world.getEntityById(packet.getCollectorEntityId());
      if (itemEntity instanceof ItemEntity && entity == this.client.player) {
         Rockstar.getInstance().getEventManager().triggerEvent(new PickupEvent(((ItemEntity)itemEntity).getStack(), packet.getStackAmount()));
      }
   }

   @Inject(method = "onBlockEntityUpdate", at = @At("TAIL"))
   private void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, CallbackInfo ci) {
      if (mc.world != null) {
         BlockPos pos = packet.getPos();
         BlockEntity blockEntity = mc.world.getBlockEntity(pos);
         if (blockEntity != null && !WorldUtility.blockEntities.contains(blockEntity)) {
            WorldUtility.blockEntities.add(blockEntity);
         }
      }
   }

   @Inject(method = "onChunkData", at = @At("TAIL"))
   private void onChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
      if (ClientPlayNetworkHandlerMixin.mc.world != null) {
         WorldChunk chunk = ClientPlayNetworkHandlerMixin.mc.world.getChunk(packet.getChunkX(), packet.getChunkZ());
         chunk.getBlockEntities().values().forEach(be -> {
            if (!WorldUtility.blockEntities.contains(be)) {
               WorldUtility.blockEntities.add(be);
            }
         });
         XRay xray = Rockstar.getInstance().getModuleManager().getModule(XRay.class);
         MinecraftClient mc = MinecraftClient.getInstance();
         if (xray != null && xray.isEnabled() && mc.world != null) {
            new Thread(() -> xray.scanChunk(chunk)).start();
         }
      }
   }

   @Inject(method = "onGameJoin", at = @At("TAIL"))
   private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
      WorldUtility.blockEntities.clear();
      Rockstar.getInstance().getEventManager().triggerEvent(new WorldChangeEvent());
   }

   @Inject(method = "onPlayerPositionLook", at = @At("HEAD"))
   public void savePlayerRotation(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
      if (mc.player != null) {
         this.oldRotation = new Rotation(mc.player.getYaw(), mc.player.getPitch());
      }
   }

   @Inject(method = "onPlayerPositionLook", at = @At("RETURN"))
   public void modifyPlayerRotation(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
      if (mc.player != null) {
         new Rotation(packet.change().yaw(), packet.change().pitch());
      }
   }
}
