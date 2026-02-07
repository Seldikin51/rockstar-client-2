package moscow.rockstar.systems.modules.modules.visuals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.ChatRenderEvent;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.event.impl.window.ChatClickEvent;
import moscow.rockstar.systems.friends.FriendManager;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.other.NameProtect;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.target.TargetManager;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.ItemUtility;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.RectBatching;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Name Tags", category = ModuleCategory.VISUALS, enabledByDefault = true, desc = "Теги, отображающие информацию о сущностях")
public class Nametags extends BaseModule {
   private final List<Entity> entityList = new ArrayList<>();
   private final BooleanSetting armor = new BooleanSetting(this, "modules.settings.name_tags.armor");
   private final BooleanSetting offFriends = new BooleanSetting(this, "modules.settings.name_tags.offFriends");
   private final BooleanSetting items = new BooleanSetting(this, "modules.settings.name_tags.items");
   private final BooleanSetting backItems = new BooleanSetting(this, "modules.settings.name_tags.background", () -> !this.items.isEnabled());
   private final EventListener<PreHudRenderEvent> onHudRenderEvent = event -> {
      MatrixStack matrices = event.getContext().getMatrices();
      float tickDelta = event.getTickDelta();
      Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
      this.entityList.clear();

      for (Entity entity : mc.world.getEntities()) {
         if (entity != mc.player
            && (entity.getType() == EntityType.PLAYER || entity.getType() == EntityType.ITEM)
            && !(
               entity instanceof PlayerEntity player
                  && Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString())
                  && this.offFriends.isEnabled()
            )) {
            this.entityList.add(entity);
         }
      }

      List<List<ItemEntity>> itemGroups = new LinkedList<>();
      Set<ItemEntity> processedItems = new HashSet<>();

      for (Entity e : this.entityList) {
         if (e instanceof ItemEntity item && !processedItems.contains(item)) {
            List<ItemEntity> group = new LinkedList<>();

            for (Entity other : this.entityList) {
               if (other instanceof ItemEntity otherItem && !processedItems.contains(otherItem) && item.squaredDistanceTo(otherItem) < 1.0) {
                  group.add(otherItem);
                  processedItems.add(otherItem);
               }
            }

            itemGroups.add(group);
         }
      }

      Batching rect = new RectBatching(VertexFormats.POSITION_COLOR, event.getContext().getMatrices());
      this.drawBack(event, itemGroups, tickDelta);
      rect.draw();

      for (Entity entityx : this.entityList) {
         Vec3d pos = Utils.getInterpolatedPos(entityx, tickDelta).add(0.0, entityx.getBoundingBox().getLengthY() + 0.5, 0.0);
         Vec2f screenPos = Utils.worldToScreen(pos);
         if (screenPos != null && entityx.getType() == EntityType.PLAYER && this.armor.isEnabled()) {
            this.renderArmorPlayer(event, matrices, (PlayerEntity)entityx, screenPos);
         }
      }

      for (Entity entityxx : this.entityList) {
         Vec3d pos = Utils.getInterpolatedPos(entityxx, tickDelta).add(0.0, entityxx.getBoundingBox().getLengthY() + 0.5, 0.0);
         Vec2f screenPos = Utils.worldToScreen(pos);
         if (screenPos != null && entityxx.getType() == EntityType.ITEM) {
            this.renderShulkerDisplay(event, matrices, (ItemEntity)entityxx, screenPos);
         }
      }

      DiffuseLighting.disableGuiDepthLighting();
      event.getContext().draw();
      FontBatching fontBatching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);

      for (Entity entityxxx : this.entityList) {
         Vec3d pos = Utils.getInterpolatedPos(entityxxx, tickDelta).add(0.0, entityxxx.getBoundingBox().getLengthY() + 0.5, 0.0);
         Vec2f screenPos = Utils.worldToScreen(pos);
         if (screenPos != null && entityxxx.getType() == EntityType.PLAYER) {
            this.renderNametagPlayer(event, matrices, entityxxx, screenPos);
         }
      }

      for (List<ItemEntity> group : itemGroups) {
         ItemEntity first = group.getFirst();
         Vec3d pos = Utils.getInterpolatedPos(first, tickDelta).add(0.0, first.getBoundingBox().getLengthY() + 0.5, 0.0);
         Vec2f screenPos = Utils.worldToScreen(pos);
         if (screenPos != null && this.items.isEnabled()) {
            if (group.size() > 1) {
               this.renderItemsText(event, matrices, group, screenPos);
            } else {
               this.renderItemText(event, matrices, first, screenPos);
            }
         }
      }

      for (Entity entityxxxx : this.entityList) {
         Vec3d pos = Utils.getInterpolatedPos(entityxxxx, tickDelta).add(0.0, entityxxxx.getBoundingBox().getLengthY() + 0.5, 0.0);
         Vec2f screenPos = Utils.worldToScreen(pos);
         if (screenPos != null && entityxxxx.getType() == EntityType.ITEM) {
            this.renderShulkerText(event, matrices, (ItemEntity)entityxxxx, screenPos);
         }
      }

      fontBatching.draw();
      if (!(mc.currentScreen instanceof ChatScreen)) {
         this.active = null;
      }
   };
   private Popup active;
   private final EventListener<ChatRenderEvent> onRender = event -> {
      UIContext context = UIContext.of(
         event.getContext(),
         mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getX(),
         mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getY(),
         MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false)
      );
      if (this.active != null) {
         this.active.render(context);
      }
   };
   private final EventListener<ChatClickEvent> onClick = event -> {
      if (this.active != null) {
         this.active.onMouseClicked(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
         if (this.active.isHovered(event.getX(), event.getY())) {
            return;
         }

         this.active.setShowing(false);
      }

      for (Entity entity : this.entityList) {
         Vec3d pos = Utils.getInterpolatedPos(entity, 1.0F).add(0.0, entity.getBoundingBox().getLengthY() + 0.5, 0.0);
         Vec2f screenPos = Utils.worldToScreen(pos);
         if (screenPos != null && entity.getType() == EntityType.PLAYER) {
            this.handleClick(event, entity, screenPos);
         }
      }
   };

   private void drawBack(PreHudRenderEvent event, List<List<ItemEntity>> itemGroups, float tickDelta) {
      MatrixStack matrices = event.getContext().getMatrices();

      for (Entity entity : this.entityList) {
         Vec3d pos = Utils.getInterpolatedPos(entity, tickDelta).add(0.0, entity.getBoundingBox().getLengthY() + 0.5, 0.0);
         Vec2f screenPos = Utils.worldToScreen(pos);
         if (screenPos != null && entity.getType() == EntityType.PLAYER) {
            this.renderBack(event, matrices, entity, screenPos);
         }
      }

      for (List<ItemEntity> group : itemGroups) {
         ItemEntity first = group.getFirst();
         Vec3d pos = Utils.getInterpolatedPos(first, tickDelta).add(0.0, first.getBoundingBox().getLengthY() + 0.5, 0.0);
         Vec2f screenPos = Utils.worldToScreen(pos);
         if (screenPos != null && this.backItems.isEnabled() && this.items.isEnabled()) {
            if (group.size() > 1) {
               this.renderItemsBack(event, matrices, group, screenPos);
            } else {
               this.renderItemBack(event, matrices, first, screenPos);
            }
         }
      }

      for (Entity entityx : this.entityList) {
         Vec3d pos = Utils.getInterpolatedPos(entityx, tickDelta).add(0.0, entityx.getBoundingBox().getLengthY() + 0.5, 0.0);
         Vec2f screenPos = Utils.worldToScreen(pos);
         if (screenPos != null && entityx.getType() == EntityType.ITEM) {
            this.renderShulkerBack(event, matrices, (ItemEntity)entityx, screenPos);
         }
      }
   }

   private void renderItemBack(PreHudRenderEvent event, MatrixStack matrices, ItemEntity entity, Vec2f screenPos) {
      float distance = entity.distanceTo(mc.player);
      float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
      matrices.push();
      matrices.translate(screenPos.x, screenPos.y, 0.0F);
      matrices.scale(scale, scale, 1.0F);
      String text = entity.getStack().getName().getString() + " " + entity.getStack().getCount() + "x";
      int textWidth = (int)Fonts.MEDIUM.getFont(11.0F).width(text);
      int x = -textWidth / 2;
      int y = 5;
      event.getContext().drawRect(x - 3, y - 3, textWidth + 6, Fonts.MEDIUM.getFont(11.0F).height() + 6.0F, new ColorRGBA(0.0F, 0.0F, 0.0F, 100.0F));
      matrices.pop();
   }

   private void renderItemText(PreHudRenderEvent event, MatrixStack matrices, ItemEntity entity, Vec2f screenPos) {
      float distance = entity.distanceTo(mc.player);
      float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
      matrices.push();
      matrices.translate(screenPos.x, screenPos.y, 0.0F);
      matrices.scale(scale, scale, 1.0F);
      Text text = entity.getStack().getName().copy().append(" " + entity.getStack().getCount() + "x");
      int textWidth = (int)Fonts.MEDIUM.getFont(11.0F).width(text);
      int x = -textWidth / 2;
      int y = 5;
      event.getContext().drawText(Fonts.MEDIUM.getFont(11.0F), text, x, y);
      matrices.pop();
   }

   private void renderItemsBack(PreHudRenderEvent event, MatrixStack matrices, List<ItemEntity> items, Vec2f screenPos) {
      if (!items.isEmpty()) {
         float distance = items.getFirst().distanceTo(mc.player);
         float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
         matrices.push();
         matrices.translate(screenPos.x, screenPos.y, 0.0F);
         matrices.scale(scale, scale, 1.0F);
         int maxWidth = 0;
         int textHeight = (int)Fonts.MEDIUM.getFont(11.0F).height();

         for (ItemEntity item : items) {
            String text = item.getStack().getName().getString() + " " + item.getStack().getCount() + "x";
            int w = (int)Fonts.MEDIUM.getFont(11.0F).width(text);
            if (w > maxWidth) {
               maxWidth = w;
            }
         }

         int boxWidth = maxWidth + 6;
         int boxHeight = items.size() * textHeight + (items.size() - 1) * 2 + 6;
         event.getContext().drawRect(-maxWidth / 2.0F - 3.0F, 2.0F, boxWidth, boxHeight, new ColorRGBA(0.0F, 0.0F, 0.0F, 100.0F));
         matrices.pop();
      }
   }

   private void renderItemsText(PreHudRenderEvent event, MatrixStack matrices, List<ItemEntity> items, Vec2f screenPos) {
      if (!items.isEmpty()) {
         float distance = items.getFirst().distanceTo(mc.player);
         float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
         matrices.push();
         matrices.translate(screenPos.x, screenPos.y, 0.0F);
         matrices.scale(scale, scale, 1.0F);
         int textHeight = (int)Fonts.MEDIUM.getFont(11.0F).height();
         int maxWidth = 0;
         List<Text> lines = new LinkedList<>();

         for (ItemEntity item : items) {
            Text text = item.getStack().getName().copy().append(" " + item.getStack().getCount() + "x");
            lines.add(text);
            int w = (int)Fonts.MEDIUM.getFont(11.0F).width(text);
            if (w > maxWidth) {
               maxWidth = w;
            }
         }

         int startX = -maxWidth / 2;

         for (int i = 0; i < lines.size(); i++) {
            Text line = lines.get(i);
            int lineWidth = (int)Fonts.MEDIUM.getFont(11.0F).width(line);
            int x = startX + (maxWidth - lineWidth) / 2;
            int y = 5 + i * (textHeight + 2);
            event.getContext().drawText(Fonts.MEDIUM.getFont(11.0F), Text.of(line), x, y);
         }

         matrices.pop();
      }
   }

   private void renderBack(PreHudRenderEvent event, MatrixStack matrices, Entity entity, Vec2f screenPos) {
      float distance = entity.distanceTo(mc.player);
      float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
      if (entity instanceof PlayerEntity player) {
         matrices.push();
         matrices.translate(screenPos.x, screenPos.y, 0.0F);
         matrices.scale(scale, scale, 1.0F);
         Text displayName = entity.getDisplayName().copy().append(" ").append("[" + (int)EntityUtility.getHealth(player) + "]");
         int textWidth = (int)Fonts.MEDIUM.getFont(11.0F).width(displayName);
         int x = -textWidth / 2;
         int y = 5;
         event.getContext()
            .drawRect(
               x - 3,
               y - 3,
               textWidth + 5,
               Fonts.MEDIUM.getFont(11.0F).height() + 6.0F,
               Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString())
                  ? new ColorRGBA(0.0F, 125.0F, 0.0F, 100.0F)
                  : new ColorRGBA(0.0F, 0.0F, 0.0F, 100.0F)
            );
         matrices.pop();
      }
   }

   private void renderNametagPlayer(PreHudRenderEvent event, MatrixStack matrices, Entity entity, Vec2f screenPos) {
      float distance = entity.distanceTo(mc.player);
      float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
      if (entity instanceof PlayerEntity player) {
         matrices.push();
         matrices.translate(screenPos.x, screenPos.y, 0.0F);
         matrices.scale(scale, scale, 1.0F);
         Text displayName = entity.getDisplayName()
            .copy()
            .append(" ")
            .append(Text.of("[" + (int)EntityUtility.getHealth(player) + "]").copy().withColor(-2142128));
         int textWidth = (int)Fonts.MEDIUM.getFont(11.0F).width(displayName);
         int x = -textWidth / 2;
         int y = 5;
         event.getContext().drawText(Fonts.MEDIUM.getFont(11.0F), displayName, x - 1, y);
         matrices.pop();
      }
   }

   private void renderArmorPlayer(PreHudRenderEvent event, MatrixStack matrices, PlayerEntity entity, Vec2f screenPos) {
      float distance = entity.distanceTo(mc.player);
      float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
      matrices.push();
      matrices.translate(screenPos.x, screenPos.y, 0.0F);
      matrices.scale(scale, scale, 1.0F);
      List<ItemStack> items = new LinkedList<>();
      items.add((ItemStack)entity.getInventory().armor.get(3));
      items.add((ItemStack)entity.getInventory().armor.get(2));
      items.add((ItemStack)entity.getInventory().armor.get(1));
      items.add((ItemStack)entity.getInventory().armor.get(0));
      items.add(entity.getMainHandStack());
      items.add(entity.getOffHandStack());
      items.removeIf(ItemStack::isEmpty);
      int count = items.size();
      if (count > 0) {
         float totalWidth = (count - 1) * 18.0F + 16.0F;
         float startX = -totalWidth / 2.0F;

         for (int i = 0; i < count; i++) {
            ItemStack item = items.get(i);
            int x = (int)(startX + i * 18);
            event.getContext().drawBatchItem(item, x, -14);
         }
      }

      matrices.pop();
   }

   private void renderShulkerBack(PreHudRenderEvent event, MatrixStack matrices, ItemEntity entity, Vec2f screenPos) {
      List<ItemStack> items = ItemUtility.getItemsInShulker(entity.getStack());
      if (!items.isEmpty()) {
         float distance = entity.distanceTo(mc.player);
         float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
         matrices.push();
         matrices.translate(screenPos.x, screenPos.y, 0.0F);
         matrices.scale(scale, scale, 1.0F);
         int columns = Math.min(items.size(), 9);
         int rows = (int)Math.ceil(items.size() / 9.0F);
         int boxWidth = columns * 18 + 4;
         int boxHeight = rows * 18 + 4;
         event.getContext().drawRect(-boxWidth / 2, -boxHeight / 2, boxWidth, boxHeight, new ColorRGBA(0.0F, 0.0F, 0.0F, 150.0F));
         matrices.pop();
      }
   }

   private void renderShulkerDisplay(PreHudRenderEvent event, MatrixStack matrices, ItemEntity entity, Vec2f screenPos) {
      List<ItemStack> items = ItemUtility.getItemsInShulker(entity.getStack());
      if (!items.isEmpty()) {
         float distance = entity.distanceTo(mc.player);
         float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
         matrices.push();
         matrices.translate(screenPos.x, screenPos.y, 0.0F);
         matrices.scale(scale, scale, 1.0F);
         int columns = Math.min(items.size(), 9);
         int rows = (int)Math.ceil(items.size() / 9.0F);
         int boxWidth = columns * 18 + 4;
         int boxHeight = rows * 18 + 4;

         for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            int x = i % 9 * 18 - boxWidth / 2 + 3;
            int y = i / 9 * 18 - boxHeight / 2 + 3;
            event.getContext().drawBatchItem(item, x, y);
         }

         matrices.pop();
      }
   }

   private void renderShulkerText(PreHudRenderEvent event, MatrixStack matrices, ItemEntity entity, Vec2f screenPos) {
      List<ItemStack> items = ItemUtility.getItemsInShulker(entity.getStack());
      if (!items.isEmpty()) {
         float distance = entity.distanceTo(mc.player);
         float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
         matrices.push();
         matrices.translate(screenPos.x, screenPos.y, 0.0F);
         matrices.scale(scale, scale, 1.0F);
         int columns = Math.min(items.size(), 9);
         int rows = (int)Math.ceil(items.size() / 9.0F);
         int boxWidth = columns * 18 + 4;
         int boxHeight = rows * 18 + 4;
         event.getContext()
            .drawText(Fonts.MEDIUM.getFont(11.0F), entity.getDisplayName().getString(), -boxWidth / 2 + 0.5F, -boxHeight / 2 - 11, ColorRGBA.WHITE);

         for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            int x = i % 9 * 18 - boxWidth / 2 + 3;
            int y = i / 9 * 18 - boxHeight / 2 + 3;
            if (item.getCount() > 1) {
               String count = String.valueOf(item.getCount());
               event.getContext().drawText(Fonts.MEDIUM.getFont(11.0F), count, x + 16 - Fonts.MEDIUM.getFont(11.0F).width(count), y + 9, ColorRGBA.WHITE);
            }
         }

         matrices.pop();
      }
   }

   public static Text displayName(Entity entity) {
      if (entity.getDisplayName() == null) {
         return Text.empty();
      } else {
         NameProtect nameProtectModule = Rockstar.getInstance().getModuleManager().getModule(NameProtect.class);
         String displayName = nameProtectModule.isEnabled()
            ? nameProtectModule.patchName(entity.getDisplayName().getString())
            : entity.getDisplayName().getString();
         MutableText text = Text.of(displayName).copy();
         if (entity instanceof LivingEntity living) {
            int health = (int)(entity instanceof PlayerEntity player ? EntityUtility.getHealth(player) : living.getHealth());
            if (!text.getString().endsWith(" ")) {
               text.append(" ");
            }

            return text.append(Text.of("[" + (health == 1000 ? "?" : health) + "]").copy().withColor(-2142128));
         } else {
            return text;
         }
      }
   }

   private void handleClick(ChatClickEvent event, Entity entity, Vec2f screenPos) {
      float distance = entity.distanceTo(mc.player);
      float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
      Text displayName = displayName(entity);
      float textWidth = Fonts.MEDIUM.getFont(11.0F).width(displayName);
      float textHeight = Fonts.MEDIUM.getFont(11.0F).height();
      float rectWidth = textWidth + 5.0F;
      float rectHeight = textHeight + 6.0F;
      float rectOffsetX = -textWidth / 2.0F - 3.0F;
      float rectOffsetY = 2.0F;
      float scaledRectWidth = rectWidth * scale;
      float scaledRectHeight = rectHeight * scale;
      float scaledRectX = screenPos.x + rectOffsetX * scale;
      float scaledRectY = screenPos.y + rectOffsetY * scale;
      if (GuiUtility.isHovered(
         (double)scaledRectX, (double)scaledRectY, (double)scaledRectWidth, (double)scaledRectHeight, (double)event.getX(), (double)event.getY()
      )) {
         FriendManager friendManager = Rockstar.getInstance().getFriendManager();
         TargetManager targetManager = Rockstar.getInstance().getTargetManager();
         String name = entity.getName().getString();
         this.active = new Popup(event.getX(), event.getY(), 100.0F, 6.0F)
            .title(name)
            .separator()
            .checkbox(Localizator.translate("friend"), friendManager.isFriend(name), toggled -> {
               if (toggled) {
                  friendManager.add(name);
               } else {
                  friendManager.remove(name);
               }
            })
            .checkbox(Localizator.translate("enemy"), targetManager.isTarget(name), toggled -> {
               if (toggled) {
                  targetManager.addTarget(name);
               } else {
                  targetManager.removeTarget(name);
               }
            })
            .button(Localizator.translate("copy"), "icons/hud/copy.png", popup -> {
               TextUtility.copyText(name);
               popup.setShowing(false);
            });
      }
   }

   @Override
   public void onDisable() {
      this.entityList.clear();
   }

   @Generated
   public List<Entity> getEntityList() {
      return this.entityList;
   }

   @Generated
   public BooleanSetting getArmor() {
      return this.armor;
   }

   @Generated
   public BooleanSetting getOffFriends() {
      return this.offFriends;
   }

   @Generated
   public BooleanSetting getItems() {
      return this.items;
   }

   @Generated
   public BooleanSetting getBackItems() {
      return this.backItems;
   }

   @Generated
   public EventListener<PreHudRenderEvent> getOnHudRenderEvent() {
      return this.onHudRenderEvent;
   }

   @Generated
   public Popup getActive() {
      return this.active;
   }

   @Generated
   public EventListener<ChatRenderEvent> getOnRender() {
      return this.onRender;
   }

   @Generated
   public EventListener<ChatClickEvent> getOnClick() {
      return this.onClick;
   }
}
