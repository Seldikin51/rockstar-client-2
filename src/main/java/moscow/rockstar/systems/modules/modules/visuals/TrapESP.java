package moscow.rockstar.systems.modules.modules.visuals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.block.TripwireBlock;
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Direction.Type;

@ModuleInfo(name = "Trap ESP", category = ModuleCategory.VISUALS)
public class TrapESP extends BaseModule {
   private volatile List<TrapESP.Trap> traps = Collections.emptyList();
   private final Timer updateTimer = new Timer();
   private final Deque<BlockPos> scanQueue = new ConcurrentLinkedDeque<>();
   private final Set<BlockPos> enqueuedColumns = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private final Map<Long, TrapESP.Trap> detectedTraps = new ConcurrentHashMap<>();
   private static final long SCAN_INTERVAL_MS = 5000L;
   private static final int SCAN_RADIUS = 64;
   private static final int COLUMNS_PER_TICK = 96;
   private static final int MAX_TRAP_DEPTH = 24;
   private static final int MIN_TRAP_DEPTH = 5;
   private final Block[] REGION_BLOCKS = new Block[]{
      Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.EMERALD_BLOCK, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE
   };
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (mc.player != null && mc.world != null) {
         if (this.updateTimer.finished(5000L) && this.scanQueue.isEmpty()) {
            this.enqueueFullScan();
            this.updateTimer.reset();
         }

         int processed = 0;
         long timeBudgetNs = 2000000L;
         long startNs = System.nanoTime();

         BlockPos columnBase;
         while (processed < 96 && (columnBase = this.scanQueue.poll()) != null) {
            this.enqueuedColumns.remove(columnBase);
            processed++;
            TrapESP.Trap t = this.scanColumnForTrap(columnBase);
            long key = packXZ(columnBase.getX(), columnBase.getZ());
            if (t != null) {
               this.detectedTraps.put(key, t);
            } else {
               this.detectedTraps.remove(key);
            }

            if (System.nanoTime() - startNs > timeBudgetNs) {
               break;
            }
         }

         this.traps = new ArrayList<>(this.detectedTraps.values());
      }
   };
   private final EventListener<ReceivePacketEvent> onPacket = event -> {
      if (mc.player != null && mc.world != null) {
         try {
            Packet<?> packet = event.getPacket();
            if (packet instanceof ChunkDataS2CPacket wrapper) {
               this.enqueueChunk(new ChunkPos(wrapper.getChunkX(), wrapper.getChunkZ()));
            }

            if (packet instanceof ChunkDeltaUpdateS2CPacket wrapper) {
               wrapper.visitUpdates((posx, state) -> this.enqueueColumn(posx.getX(), posx.getZ()));
            }

            if (packet instanceof BlockUpdateS2CPacket wrapper) {
               BlockPos pos = wrapper.getPos();
               this.enqueueColumn(pos.getX(), pos.getZ());
            }
         } catch (Throwable var5) {
         }
      }
   };
   private final EventListener<WorldChangeEvent> onWorldChange = event -> this.reset();
   private final EventListener<PreHudRenderEvent> onHud = ev -> {
      CustomDrawContext ctx = ev.getContext();
      MatrixStack ms = ctx.getMatrices();
      Font font = Fonts.MEDIUM.getFont(9.0F);
      int fontHeight = (int)font.height();

      for (TrapESP.Trap trap : this.traps) {
         Vec2f screen = Utils.worldToScreen(trap.pos.toCenterPos().add(0.0, 0.5, 0.0));
         if (screen != null) {
            String l1 = Localizator.translate("modules.trap_esp.label");
            String l2 = Localizator.translate("modules.trap_esp.depth", trap.depth);
            String l3 = trap.hasPrivate ? Localizator.translate("modules.trap_esp.private") : Localizator.translate("modules.trap_esp.no_private");
            int iconSize = 9;
            int textPadding = 6;
            int totalWidth = (int)(font.width(l2) + textPadding);
            float titleWidth = font.width(l1) + 6.0F + iconSize + 2.0F;
            float miniHeight = fontHeight * 2;
            float height = miniHeight * 3.0F;
            ctx.pushMatrix();
            ms.translate(screen.x, screen.y - height, 0.0F);
            ctx.drawRect(-titleWidth / 2.0F, 0.0F, titleWidth, miniHeight, ColorRGBA.BLACK.withAlpha(150.0F));
            ctx.drawText(font, l1, -titleWidth / 2.0F + iconSize + 5.0F, 3.0F, Colors.WHITE);
            ctx.drawTexture(Rockstar.id("icons/trap.png"), -titleWidth / 2.0F + 2.0F, fontHeight - iconSize / 2.0F, iconSize, iconSize, Colors.WHITE);
            ctx.drawRect(-totalWidth / 2.0F, miniHeight, totalWidth, miniHeight, ColorRGBA.BLACK.withAlpha(150.0F));
            ctx.drawText(font, l2, -totalWidth / 2.0F + 2.0F, miniHeight + 3.0F, Colors.WHITE.withAlpha(200.0F));
            ctx.drawRect(-(font.width(l3) + 6.0F) / 2.0F, miniHeight * 2.0F, font.width(l3) + 6.0F, miniHeight, ColorRGBA.BLACK.withAlpha(150.0F));
            ctx.drawText(font, l3, -font.width(l3) / 2.0F, miniHeight * 2.0F + 3.0F, Colors.WHITE.withAlpha(200.0F));
            ctx.popMatrix();
         }
      }
   };

   @Override
   public void onDisable() {
      this.reset();
      super.onDisable();
   }

   @Override
   public void onEnable() {
      this.reset();
      this.enqueueFullScan();
      super.onEnable();
   }

   public void reset() {
      this.scanQueue.clear();
      this.enqueuedColumns.clear();
      this.detectedTraps.clear();
      this.traps = Collections.emptyList();
      this.updateTimer.reset();
   }

   private void enqueueFullScan() {
      if (mc.player != null) {
         BlockPos playerPos = mc.player.getBlockPos();
         List<BlockPos> cols = new ArrayList<>(16641);

         for (int dx = -64; dx <= 64; dx++) {
            for (int dz = -64; dz <= 64; dz++) {
               BlockPos col = new BlockPos(playerPos.getX() + dx, playerPos.getY(), playerPos.getZ() + dz);
               if (this.enqueuedColumns.add(col)) {
                  cols.add(col);
               }
            }
         }

         cols.sort(Comparator.comparingDouble(pos -> pos.getSquaredDistance(playerPos)));
         this.scanQueue.addAll(cols);
      }
   }

   private void enqueueChunk(ChunkPos chunkPos) {
      if (mc.player != null) {
         int baseY = mc.player.getBlockPos().getY();
         int startX = chunkPos.getStartX();
         int startZ = chunkPos.getStartZ();

         for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
               BlockPos col = new BlockPos(x, baseY, z);
               if (this.enqueuedColumns.add(col)) {
                  this.scanQueue.add(col);
               }
            }
         }
      }
   }

   private void enqueueColumn(int x, int z) {
      if (mc.player != null) {
         int baseY = mc.player.getBlockPos().getY();
         BlockPos pos = new BlockPos(x, baseY, z);
         if (this.enqueuedColumns.add(pos)) {
            this.scanQueue.add(pos);
         }
      }
   }

   private TrapESP.Trap scanColumnForTrap(BlockPos columnBase) {
      if (mc.player != null && mc.world != null) {
         int baseY = columnBase.getY();

         for (int dy = 20; dy >= -20; dy--) {
            BlockPos start = new BlockPos(columnBase.getX(), baseY + dy, columnBase.getZ());
            int depth = 0;
            boolean inShaft = false;

            for (int i = 0; i < 24; i++) {
               BlockPos pos = start.down(i);
               BlockState state = mc.world.getBlockState(pos);
               if (state.isAir() && mc.world.getFluidState(pos).isEmpty()) {
                  int walls = 0;

                  for (Direction dir : Type.HORIZONTAL) {
                     BlockPos side = pos.offset(dir);
                     BlockState sideState = mc.world.getBlockState(side);
                     if (!sideState.isAir() && !sideState.getCollisionShape(mc.world, side).isEmpty()) {
                        walls++;
                     }
                  }

                  if (walls < 4) {
                     if (inShaft) {
                        break;
                     }
                  } else {
                     inShaft = true;
                     depth++;
                  }
               } else if (inShaft) {
                  break;
               }
            }

            if (depth >= 5) {
               BlockPos bottom = start.down(depth);
               BlockState bottomState = mc.world.getBlockState(bottom);
               if (!bottomState.isAir() && !bottomState.getCollisionShape(mc.world, bottom).isEmpty()) {
                  boolean flagged = this.hasNearbyIndicatorsOrValuables(start, 6);
                  return new TrapESP.Trap(start, depth, flagged);
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean hasNearbyIndicatorsOrValuables(BlockPos center, int radius) {
      if (mc.player != null && mc.world != null) {
         BlockPos min = center.add(-radius, -radius, -radius);
         BlockPos max = center.add(radius, radius, radius);

         for (BlockPos pos : BlockPos.iterate(min, max)) {
            BlockState state = mc.world.getBlockState(pos);
            if (!state.isAir()) {
               if (state.getBlock() instanceof TntBlock
                  || state.getBlock() instanceof PressurePlateBlock
                  || state.getBlock() instanceof TripwireBlock
                  || state.getBlock() instanceof TripwireHookBlock
                  || state.getBlock() instanceof HopperBlock
                  || state.getBlock() instanceof ChestBlock) {
                  return true;
               }

               Block block = state.getBlock();

               for (Block region : this.REGION_BLOCKS) {
                  if (block == region) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static long packXZ(int x, int z) {
      return (long)x << 32 ^ z & 4294967295L;
   }

   static class Trap {
      BlockPos pos;
      int depth;
      boolean hasPrivate;

      @Generated
      public Trap(BlockPos pos, int depth, boolean hasPrivate) {
         this.pos = pos;
         this.depth = depth;
         this.hasPrivate = hasPrivate;
      }
   }
}
