package moscow.rockstar.systems.modules.modules.other;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.BufferedReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.target.TargetComparators;
import moscow.rockstar.systems.target.TargetSettings;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EquipmentSlot.Type;
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

@ModuleInfo(name = "Counter Mine", category = ModuleCategory.OTHER, desc = "Автоматически убивает криперов на фармилке")
public class CounterMineOld extends BaseModule {
   private final BooleanSetting moderDetect = new BooleanSetting(this, "ModerDetect");
   private final BooleanSetting wallHack = new BooleanSetting(this, "WallHack");
   private final BooleanSetting noF5 = new BooleanSetting(this, "No F5");
   private final BooleanSetting noSmoke = new BooleanSetting(this, "NoSmoke");
   private final BooleanSetting aim = new BooleanSetting(this, "Aim");
   private final BooleanSetting silent = new BooleanSetting(this, "Silent", () -> !this.aim.isEnabled());
   private final BooleanSetting autoShoot = new BooleanSetting(this, "AutoShoot", () -> !this.aim.isEnabled());
   private final BooleanSetting antiAim = new BooleanSetting(this, "AntAim");
   private final BooleanSetting fakeLag = new BooleanSetting(this, "FakeLag");
   private final Timer shootingTimer = new Timer();
   private final Timer moderTimer = new Timer();
   boolean waitRelease;
   boolean stopping;
   private final Timer movementPauseTimer = new Timer();
   private boolean temporaryStopping = false;
   private boolean hasShotDuringPause = false;
   private final List<CounterMineOld.Head> heads = new ArrayList<>();
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (this.moderDetect.isEnabled() && this.moderTimer.finished(5000L)) {
         if (this.isPlayerOnline("johnebik")) {
            MessageUtility.info(Text.of("johnebik"));
            mc.player.networkHandler.sendChatCommand("hub");
            this.moderTimer.reset();
         }

         if (this.isPlayerOnline("sherlock")) {
            MessageUtility.info(Text.of("sherlock"));
            mc.player.networkHandler.sendChatCommand("hub");
            this.moderTimer.reset();
         }
      }

      for (Entity entity : mc.world.getEntities()) {
         Box boundingBox = entity.getBoundingBox();
         if (entity instanceof ItemDisplayEntity itemDisplay && mc.player.distanceTo(entity) < 3.0F) {
            String modelId = getModelIdFromNbt(itemDisplay.getItemStack(), mc.player.getRegistryManager());
            if (modelId != null) {
               String modelJson = findHashedModel(modelId);
               if (modelJson != null && mc.player.age % 200 == 0) {
                  System.out.println(modelJson);
               }
            }
         }

         if (boundingBox.maxX == boundingBox.minX && !(mc.player.distanceTo(entity) < 2.0F) && entity.getName().getString().contains("предмета")) {
            boolean hologramNearby = isHologramNearby(entity, mc.world, 2.5);
            if (entity instanceof ItemDisplayEntity itemDisplayx) {
               String modelId = getModelIdFromNbt(itemDisplayx.getItemStack(), mc.player.getRegistryManager());
               if (modelId != null) {
                  String modelJson = findHashedModel(modelId);
                  if (modelJson != null) {
                     if (modelJson.contains("\"textures\":{\"particle\":\"item/") && modelJson.contains("\",\"skin\":\"item/")) {
                        entity.setGlowing(true);
                     }

                     boolean sex = false;
                     if (modelJson.contains("{\"elements\":[{\"from\":[7.765625,8.0,7.765625]")) {
                        boolean nearGround = isEntityNearGround(itemDisplayx, mc.world, 1.0);
                        CounterMineOld.Head inList = null;

                        for (CounterMineOld.Head head : this.heads) {
                           if (head.entity == entity) {
                              inList = head;
                           }
                        }

                        if (!nearGround) {
                           if (inList != null) {
                              inList.poses.get(0).cords = entity.getPos();
                           } else {
                              CounterMineOld.Head addHead = new CounterMineOld.Head(entity, hologramNearby);
                              addHead.poses.add(new CounterMineOld.Position(entity.getPos()));
                              this.heads.add(addHead);
                           }
                        } else if (inList != null) {
                           this.heads.remove(inList);
                        }
                     }
                  }
               }
            }
         }
      }

      for (CounterMineOld.Head headx : new ArrayList<>(this.heads)) {
         if (!mc.world.hasEntity(headx.entity)) {
            this.heads.remove(headx);
         }

         for (CounterMineOld.Position var25 : new ArrayList<>(headx.poses)) {
            ;
         }
      }

      this.stopping = false;
      if (this.waitRelease) {
         mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, BlockPos.ORIGIN, Direction.DOWN));
         this.waitRelease = false;
      }

      if (this.antiAim.isEnabled()) {
         int age = mc.player.age;
         float yaw = mc.player.getYaw() - 90.0F - (age % 5 == 0 ? 0 : (age % 5 == 1 ? 180 : 90));
         float pitch = 90.0F;
         mc.player
            .networkHandler
            .sendPacket(new Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), yaw, pitch, mc.player.isOnGround(), mc.player.horizontalCollision));
         if (this.aim.isEnabled() && this.autoShoot.isEnabled()) {
            Rockstar.getInstance()
               .getRotationHandler()
               .rotate(new Rotation(yaw, pitch), MoveCorrection.NONE, 180.0F, 180.0F, 180.0F, RotationPriority.TO_TARGET);
         }
      }

      if (mc.player != null && mc.world != null && this.aim.isEnabled()) {
         TargetSettings settings = new TargetSettings.Builder().targetPlayers(true).requiredRange(200.0F).sortBy(TargetComparators.FOV).build();
         Rockstar.getInstance().getTargetManager().update(settings);
         Entity targetEntity = Rockstar.getInstance().getTargetManager().getCurrentTarget();
         if (targetEntity != null) {
            boolean notShoot = true;
            if (!MathUtility.canShoot(targetEntity.getPos())) {
               this.shootingTimer.reset();
            } else {
               Rotation toTarget = this.calculateRotation(targetEntity.getPos());
               float yaw = toTarget.getYaw();
               float pitch = toTarget.getPitch();
               notShoot = false;
               if (this.silent.isEnabled()) {
                  if (!this.autoShoot.isEnabled()) {
                     Rockstar.getInstance().getRotationHandler().rotate(toTarget, MoveCorrection.NONE, 180.0F, 180.0F, 180.0F, RotationPriority.TO_TARGET);
                  }
               } else {
                  mc.player.setYaw(yaw);
                  mc.player.setPitch(pitch);
                  mc.player.setHeadYaw(yaw);
               }

               if (this.autoShoot.isEnabled()) {
                  Rockstar.getInstance().getRotationHandler().rotate(toTarget, MoveCorrection.NONE, 180.0F, 180.0F, 180.0F, RotationPriority.TO_TARGET);
                  if (!this.temporaryStopping) {
                     this.temporaryStopping = true;
                     this.hasShotDuringPause = false;
                     this.movementPauseTimer.reset();
                     EntityUtility.setSpeed(0.0);
                  }

                  if (this.temporaryStopping) {
                     if (this.shootingTimer.finished(70L) && !this.hasShotDuringPause) {
                        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, BlockPos.ORIGIN, Direction.DOWN));
                        this.waitRelease = true;
                        this.hasShotDuringPause = true;
                        this.shootingTimer.reset();
                     }

                     if (this.movementPauseTimer.finished(200L) && this.hasShotDuringPause) {
                        this.temporaryStopping = false;
                        this.stopping = false;
                     }

                     this.stopping = this.temporaryStopping;
                  }
               }
            }
         }
      }
   };
   private final EventListener<InputEvent> onMove = event -> {
      if (this.stopping) {
         event.setForward(0.0F);
         event.setStrafe(0.0F);
      }
   };
   private final EventListener<Render3DEvent> on3DRender = event -> {
      if (this.noF5.isEnabled()) {
         this.removeAllArmor();
      }

      if (mc.player.age % 200 == 0) {
         System.out.println("==============================================================");
      }

      BossBarHud boss = mc.inGameHud.getBossBarHud();
      boolean bebra = mc.options.getPerspective() != Perspective.FIRST_PERSON;
      if (boss != null && bebra) {
         Class bossbarklass = BossBarHud.class;

         try {
            Field field = bossbarklass.getField("bossBars");
            Map<UUID, ClientBossBar> bossBars = (Map<UUID, ClientBossBar>)field.get(boss);

            for (UUID uuid : bossBars.keySet()) {
               ClientBossBar clientBossBar = bossBars.get(uuid);
               List<Text> siblings = clientBossBar.getName().getSiblings();
               MutableText newText = Text.literal("");
               AtomicInteger i = new AtomicInteger();
               siblings.stream().allMatch(text -> {
                  if (!text.getString().contains("둅ꈣꈃ둄ꈣꈅ")) {
                     newText.append(text);
                  }

                  i.getAndIncrement();
                  return true;
               });
               clientBossBar.setName(newText);
            }
         } catch (Exception var13) {
         }
      }

      if (mc.world != null && mc.player != null && this.wallHack.isEnabled()) {
         MatrixStack matrices = event.getMatrices();
         Camera camera = mc.gameRenderer.getCamera();
         Vec3d cameraPos = camera.getPos();
         RenderSystem.enableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         BufferBuilder linesBuffer = RenderSystem.renderThreadTesselator().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

         for (CounterMineOld.Head head : this.heads) {
            for (CounterMineOld.Position pos : head.poses) {
               Draw3DUtility.renderOutlinedBox(
                  matrices,
                  linesBuffer,
                  new Box(pos.cords.add(-0.05F, -0.05F, -0.05F), pos.cords.add(0.05F, 0.05F, 0.05F))
                     .offset(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()),
                  (head.isFriend ? ColorRGBA.GREEN : ColorRGBA.RED).withAlpha(100.0F)
               );
            }
         }

         BuiltBuffer builtLinesBuffer = linesBuffer.endNullable();
         if (builtLinesBuffer != null) {
            BufferRenderer.drawWithGlobalProgram(builtLinesBuffer);
         }

         RenderSystem.defaultBlendFunc();
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
      }
   };
   private final List<Packet<?>> packets = new ArrayList<>();
   private final Timer timer = new Timer();
   private Vec3d lastPos;
   private boolean replaying;
   private final EventListener<SendPacketEvent> sendListener = this::savePacket;
   private final EventListener<Render3DEvent> event3d = e -> {
      if (mc.options.getPerspective() != Perspective.FIRST_PERSON && this.fakeLag.isEnabled()) {
         MatrixStack ms = e.getMatrices();
         BufferBuilder quadsBuffer = RenderSystem.renderThreadTesselator().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
         Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
         ms.push();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         Draw3DUtility.renderOutlinedBox(
            ms,
            quadsBuffer,
            mc.player.getBoundingBox().offset(this.lastPos.subtract(mc.player.getPos())).offset(-cameraPos.x, -cameraPos.y, -cameraPos.z),
            ColorRGBA.WHITE.withAlpha(180.0F)
         );
         BuiltBuffer buildQuadsBuffer = quadsBuffer.endNullable();
         if (buildQuadsBuffer != null) {
            BufferRenderer.drawWithGlobalProgram(buildQuadsBuffer);
         }

         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         ms.pop();
      }
   };
   private final EventListener<WorldChangeEvent> world = e -> this.stop();

   public void removeAllArmor() {
      for (EquipmentSlot slot : EquipmentSlot.values()) {
         if (slot.getType() == Type.HUMANOID_ARMOR) {
            ItemStack currentArmor = mc.player.getEquippedStack(slot);
            if (!currentArmor.isEmpty()) {
               mc.player.getInventory().insertStack(currentArmor.copy());
               mc.player.equipStack(slot, ItemStack.EMPTY);
            }
         }
      }
   }

   private Rotation calculateRotation(Vec3d targetPos) {
      Vec3d eyes = mc.player.getCameraPosVec(1.0F);
      double dx = targetPos.x - eyes.x;
      double dy = targetPos.y - eyes.y;
      double dz = targetPos.z - eyes.z;
      double dist = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
      return new Rotation(yaw, pitch);
   }

   private Vec3d getAimPosition(Entity target) {
      Vec3d pos = target.getPos();
      return pos.add(0.0, 0.1F, 0.0);
   }

   public static boolean shouldHideEntity(ItemDisplayEntity entity) {
      String modelId = getModelIdFromNbt(entity.getItemStack(), MinecraftClient.getInstance().player.getRegistryManager());
      CounterMineOld mod = Rockstar.getInstance().getModuleManager().getModule(CounterMineOld.class);
      if (modelId == null) {
         return false;
      } else {
         String modelJson = findHashedModel(modelId);
         return modelJson != null
            && (
               modelJson.contains("smoke_sprite_transparent") && mod.noSmoke.isEnabled()
                  || modelJson.contains(",\"textures\":{\"arms\":\"") && mod.noF5.isEnabled() && mc.options.getPerspective() != Perspective.FIRST_PERSON
                  || modelJson.contains("\"textures\":{\"particle\":\"item/")
                     && modelJson.contains("\",\"skin\":\"item/")
                     && mod.noF5.isEnabled()
                     && mc.options.getPerspective() != Perspective.FIRST_PERSON
            );
      }
   }

   public void savePacket(SendPacketEvent e) {
      if (!this.replaying && EntityUtility.isInGame() && this.fakeLag.isEnabled()) {
         this.packets.add(e.getPacket());
         e.cancel();
         if (this.timer.finished(600L) || !this.packets.stream().filter(packet -> packet instanceof PlayerActionC2SPacket).toList().isEmpty()) {
            this.stop();
            this.start();
            this.timer.reset();
         }
      }
   }

   public void start() {
      if (mc.player != null) {
         this.packets.clear();
         this.lastPos = mc.player.getPos();
         this.timer.reset();
         this.replaying = false;
      }
   }

   public void stop() {
      if (mc.player != null) {
         this.replaying = true;

         for (Packet<?> p : this.packets) {
            mc.player.networkHandler.sendPacket(p);
         }

         this.replaying = false;
         this.packets.clear();
         this.lastPos = null;
      }
   }

   @Override
   public void onDisable() {
      this.stop();
      Rockstar.getInstance().getTargetManager().reset();
   }

   public boolean isPlayerOnline(String playerName) {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
      if (networkHandler == null) {
         return false;
      } else {
         for (PlayerListEntry entry : networkHandler.getPlayerList()) {
            if (entry.getProfile().getName().equals(playerName)) {
               return true;
            }
         }

         return false;
      }
   }

   public static String findHashedModel(String hashedId) {
      try {
         ResourceManager resourceManager = mc.getResourceManager();
         Identifier modelPath = Identifier.of("minecraft", "models/item/" + hashedId.replace("minecraft:", "") + ".json");
         Optional<Resource> resource = resourceManager.getResource(modelPath);
         if (resource.isPresent()) {
            String var5;
            try (BufferedReader reader = resource.get().getReader()) {
               var5 = reader.lines().collect(Collectors.joining("\n"));
            }

            return var5;
         } else {
            return null;
         }
      } catch (Exception var9) {
         System.err.println("Ошибка при получении серверной модели: " + var9.getMessage());
         return null;
      }
   }

   public static String getModelIdFromNbt(ItemStack itemStack, WrapperLookup registryManager) {
      if (itemStack.toNbtAllowEmpty(registryManager) instanceof NbtCompound compound && compound.contains("components", 10)) {
         NbtCompound components = compound.getCompound("components");
         if (components.contains("minecraft:item_model", 8)) {
            return components.getString("minecraft:item_model");
         }
      }

      return null;
   }

   public static boolean isHologramNearby(Entity entity, ClientWorld world, double searchRadius) {
      for (Entity nearbyEntity : world.getEntities()) {
         if (nearbyEntity instanceof TextDisplayEntity textDisplay
            && textDisplay.getText() != null
            && !textDisplay.getText().getString().isEmpty()
            && entity.distanceTo(textDisplay) < searchRadius) {
            return true;
         }
      }

      return false;
   }

   public static boolean isEntityNearGround(Entity entity, World world, double maxDistance) {
      BlockPos entityPos = entity.getBlockPos();
      double entityY = entity.getY();

      for (int y = entityPos.getY(); y >= entityPos.getY() - 3 && y >= world.getBottomY(); y--) {
         BlockPos checkPos = new BlockPos(entityPos.getX(), y, entityPos.getZ());
         BlockState blockState = world.getBlockState(checkPos);
         VoxelShape collisionShape = blockState.getCollisionShape(world, checkPos);
         if (!collisionShape.isEmpty()) {
            double blockTopY = checkPos.getY() + collisionShape.getMax(Axis.Y);
            double distance = entityY - blockTopY;
            if (distance <= maxDistance && distance >= 0.0) {
               return true;
            }
            break;
         }
      }

      return false;
   }

   static class Head {
      Entity entity;
      boolean isFriend;
      final List<CounterMineOld.Position> poses = new ArrayList<>();

      @Generated
      public Head(Entity entity, boolean isFriend) {
         this.entity = entity;
         this.isFriend = isFriend;
      }
   }

   static class Position {
      Vec3d cords;
      final Timer age = new Timer();

      @Generated
      public Position(Vec3d cords) {
         this.cords = cords;
      }
   }
}
