package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.chunk.WorldChunk;

@ModuleInfo(name = "XRay", category = ModuleCategory.VISUALS, desc = "Подсвечивает определенные руды")
public class XRay extends BaseModule {
   private final Set<BlockPos> cachedBlocks = ConcurrentHashMap.newKeySet();
   private final SelectSetting blocks = new SelectSetting(this, "Руды");
   public final SelectSetting.Value diamondOre = new SelectSetting.Value(this.blocks, "Алмазная руда");
   public final SelectSetting.Value ironOre = new SelectSetting.Value(this.blocks, "Железная руда");
   public final SelectSetting.Value goldOre = new SelectSetting.Value(this.blocks, "Золотая руда");
   public final SelectSetting.Value ancientOre = new SelectSetting.Value(this.blocks, "Обломки");
   public final SelectSetting.Value lapisOre = new SelectSetting.Value(this.blocks, "Лазуритовая руда");
   private int diamonds = 0;
   private int ancient = 0;
   private int gold = 0;
   private int lapis = 0;
   private final EventListener<Render3DEvent> onHudRenderEvent = event -> {
      if (mc.world != null && mc.player != null) {
         MatrixStack matrices = event.getMatrices();
         Camera camera = mc.gameRenderer.getCamera();
         Vec3d cameraPos = camera.getPos();
         matrices.push();
         matrices.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());
         RenderSystem.enableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.lineWidth(10.0F);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         double maxDistSq = 999999.0;
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

         for (BlockPos pos : this.cachedBlocks) {
            if (!(mc.player.squaredDistanceTo(pos.toCenterPos()) > maxDistSq)) {
               Box boundingBox = this.getBoundingBox(pos);
               Block block = mc.world.getBlockState(pos).getBlock();
               Draw3DUtility.renderFilledBox(event.getMatrices(), buffer, boundingBox, this.getBlockColor(block).withAlpha(30.0F));
            }
         }

         BuiltBuffer builtBuffer = buffer.endNullable();
         if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
         }

         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         matrices.pop();
      }
   };
   private final EventListener<WorldChangeEvent> onWorldChange = event -> this.cachedBlocks.clear();

   public void scanChunk(WorldChunk chunk) {
      if (mc.world != null && chunk != null) {
         int chunkX = chunk.getPos().getStartX();
         int chunkZ = chunk.getPos().getStartZ();

         for (int x = 0; x < 16; x++) {
            for (int y = mc.world.getBottomY(); y < mc.world.getTopYInclusive(); y++) {
               for (int z = 0; z < 16; z++) {
                  BlockPos pos = new BlockPos(chunkX + x, y, chunkZ + z);
                  BlockState state = chunk.getBlockState(pos);
                  if (!state.isAir() && this.isBlockEnabled(state.getBlock())) {
                     this.cachedBlocks.add(pos);
                  }
               }
            }
         }
      }
   }

   @Override
   public void onEnable() {
      if (EntityUtility.isInGame()) {
         super.onEnable();
      }
   }

   @Override
   public void onDisable() {
      this.cachedBlocks.clear();
      this.diamonds = 0;
      this.ancient = 0;
      this.gold = 0;
      this.lapis = 0;
      super.onDisable();
   }

   @Override
   public void tick() {
      this.countBlocks();
      this.removeInvalidBlocks();
      super.tick();
   }

   private void removeInvalidBlocks() {
      this.cachedBlocks.removeIf(pos -> !this.isInRenderDistance(pos));
   }

   private void countBlocks() {
      int d = 0;
      int a = 0;
      int g = 0;
      int l = 0;
      synchronized (this.cachedBlocks) {
         for (BlockPos pos : this.cachedBlocks) {
            Block block = mc.world.getBlockState(pos).getBlock();
            if (block == Blocks.DIAMOND_ORE && this.diamondOre.isSelected()) {
               d++;
            } else if (block == Blocks.ANCIENT_DEBRIS) {
               a++;
            } else if (block == Blocks.GOLD_ORE && this.goldOre.isSelected()) {
               g++;
            } else if (block == Blocks.LAPIS_ORE && this.lapisOre.isSelected()) {
               l++;
            }
         }
      }

      this.diamonds = d;
      this.ancient = a;
      this.gold = g;
      this.lapis = l;
   }

   private ColorRGBA getBlockColor(Block block) {
      if (block == Blocks.ANCIENT_DEBRIS) {
         return new ColorRGBA(255.0F, 131.0F, 54.0F);
      } else if (block == Blocks.DIAMOND_ORE) {
         return new ColorRGBA(121.0F, 54.0F, 255.0F);
      } else if (block == Blocks.GOLD_ORE) {
         return new ColorRGBA(255.0F, 215.0F, 0.0F);
      } else {
         return block == Blocks.LAPIS_ORE ? new ColorRGBA(0.0F, 71.0F, 179.0F) : Colors.WHITE;
      }
   }

   private boolean isInRenderDistance(BlockPos pos) {
      return true;
   }

   private Box getBoundingBox(BlockPos blockEntity) {
      BlockState blockState = mc.world.getBlockState(blockEntity);
      VoxelShape shape = blockState.getOutlineShape(mc.world, blockEntity);
      return shape.isEmpty() ? new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(blockEntity) : shape.getBoundingBox().offset(blockEntity);
   }

   public boolean isBlockEnabled(Block block) {
      if (block == Blocks.DIAMOND_ORE && this.diamondOre.isSelected()) {
         return true;
      } else if (block == Blocks.IRON_ORE && this.ironOre.isSelected()) {
         return true;
      } else if (block == Blocks.GOLD_ORE && this.goldOre.isSelected()) {
         return true;
      } else {
         return block == Blocks.LAPIS_ORE && this.lapisOre.isSelected() ? true : block == Blocks.ANCIENT_DEBRIS && this.ancientOre.isSelected();
      }
   }

   @Generated
   public Set<BlockPos> getCachedBlocks() {
      return this.cachedBlocks;
   }

   @Generated
   public int getDiamonds() {
      return this.diamonds;
   }

   @Generated
   public int getAncient() {
      return this.ancient;
   }

   @Generated
   public int getGold() {
      return this.gold;
   }

   @Generated
   public int getLapis() {
      return this.lapis;
   }
}
