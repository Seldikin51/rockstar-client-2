package moscow.rockstar.ui.hud;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.ChatRenderEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.window.ChatClickEvent;
import moscow.rockstar.systems.event.impl.window.ChatKeyPressEvent;
import moscow.rockstar.systems.event.impl.window.ChatReleaseEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.modules.visuals.Nametags;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.ui.components.animated.AnimatedText;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.ui.hud.impl.Effects;
import moscow.rockstar.ui.hud.impl.KeyBinds;
import moscow.rockstar.ui.hud.impl.TargetHud;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.ui.hud.inline.impl.PlayerElement;
import moscow.rockstar.ui.hud.inline.impl.WorldElement;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import ru.kotopushka.compiler.sdk.annotations.CompileBytecode;

public class Hud implements IMinecraft, IScaledResolution {
   private final List<HudElement> elements = new ArrayList<>();
   private final List<Popup> popups = new ArrayList<>();
   public DynamicIsland island;
   private final HudHistoryManager historyManager = new HudHistoryManager();
   private final Grid grid = new Grid();
   private String desc = "";
   private AnimatedText descText;
   private final Timer timer = new Timer();
   private final EventListener<HudRenderEvent> onHud = event -> {
      UIContext context = UIContext.of(
         event.getContext(),
         mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getX(),
         mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getY(),
         MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false)
      );
      if (this.descText == null) {
         this.descText = new AnimatedText(Fonts.REGULAR.getFont(10.0F), 10.0F, 300L, Easing.BAKEK).centered();
      }

      this.desc = "";
      this.grid.draw(context);
      this.grid.update();

      for (HudElement element : this.elements) {
         element.render(context);
         if (element.getSelecting().getValue() >= 0.0F) {
            float anim = element.getAnimation().getValue() * element.getVisible().getValue();
            float scale = 0.5F + anim * 0.5F - 0.05F * element.getSelecting().getValue();
            element.getLoadingAnim().setDuration(1500L);
            element.getLoadingAnim().update(1.0F);
            if (element.getLoadingAnim().getValue() == 1.0F) {
               element.getLoadingAnim().setValue(0.0F);
            }

            RenderUtility.scale(context.getMatrices(), element.getX() + element.getWidth() / 2.0F, element.getY() + element.getHeight() / 2.0F, scale);
            context.drawLoadingRect(
               element.getX(),
               element.getY(),
               element.getWidth(),
               element instanceof HudList ? Math.max(20.0F, element.getHeight()) : element.getHeight(),
               element.getLoadingAnim().getValue() * 2.2F - 0.5F,
               BorderRadius.all(element instanceof DynamicIsland ? 7.0F : 6.0F),
               ColorRGBA.WHITE.withAlpha(100.0F * element.getSelecting().getValue())
            );
            RenderUtility.end(context.getMatrices());
         }
      }

      this.descText.pos(sr.getScaledWidth() / 2.0F, 30.0F);
      if (!this.desc.contains(".description")) {
         this.descText.update(this.desc);
         this.descText.render(context);
      }

      for (Popup popup : this.popups) {
         if (!(mc.currentScreen instanceof ChatScreen)) {
            popup.setShowing(false);
         }
      }

      if (!(mc.currentScreen instanceof ChatScreen)) {
         CursorUtility.set(CursorType.DEFAULT);
      }

      this.popups.removeIf(popupx -> popupx.getAnimation().getValue() == 0.0F && !popupx.isShowing());
   };
   private final EventListener<ChatRenderEvent> onPostHud = event -> {
      UIContext context = UIContext.of(
         event.getContext(),
         mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getX(),
         mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getY(),
         MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false)
      );
      context.getMatrices().push();
      context.getMatrices().translate(0.0F, 0.0F, 2000.0F);

      for (Popup popup : this.popups) {
         if (popup.getY() + popup.getHeight() > sr.getScaledHeight()) {
            popup.setY(sr.getScaledHeight() - 10.0F - popup.getHeight());
         }

         popup.render(context);
      }

      context.getMatrices().pop();
   };
   private final EventListener<ChatKeyPressEvent> onKeyPress = event -> {
      int modifiers = event.getModifiers();
      int keyCode = event.getKeyCode();
      if (keyCode == 90 && (modifiers & 2) != 0) {
         Rockstar.getInstance().getHud().getHistoryManager().undo();
      } else if (keyCode == 89 && (modifiers & 2) != 0) {
         Rockstar.getInstance().getHud().getHistoryManager().redo();
      }
   };
   private final EventListener<ChatClickEvent> onClick = event -> {
      for (Entity entity : Rockstar.getInstance().getModuleManager().getModule(Nametags.class).getEntityList()) {
         Vec3d pos = Utils.getInterpolatedPos(entity, 1.0F).add(0.0, entity.getBoundingBox().getLengthY() + 0.5, 0.0);
         Vec2f screenPos = Utils.worldToScreen(pos);
         if (screenPos != null && entity.getType() == EntityType.PLAYER && this.handleClick(event, entity, screenPos)) {
            return;
         }
      }

      for (Popup popup : this.popups) {
         popup.onMouseClicked(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
         if (popup.isHovered(event.getX(), event.getY())) {
            return;
         }

         popup.setShowing(false);
      }

      for (HudElement element : this.elements) {
         element.onMouseClicked(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
         if (element.isHovered(event.getX(), event.getY()) && element.isShowing() || element.isDragging()) {
            return;
         }
      }

      if (event.getButton() == 1 && !this.disabledElements().isEmpty()) {
         Popup popup = new Popup(event.getX(), event.getY(), 90.0F, 6.0F).title(Localizator.translate("whatadd")).separator();

         for (HudElement elementx : this.disabledElements()) {
            popup.button(Localizator.translate(elementx.getName()), elementx.getIcon(), popup1 -> {
               elementx.pos(event.getX(), event.getY());
               elementx.setShowing(true);
               popup1.setShowing(false);
               Rockstar.getInstance().getFileManager().writeFile("client");
            });
         }

         this.popups.add(popup);
      } else if (event.getButton() == 1 && this.disabledElements().isEmpty() && this.timer.finished(600L)) {
         Rockstar.getInstance()
            .getNotificationManager()
            .addNotificationOther(NotificationType.ERROR, "Элементов нет", "Элементы закончились, добавлять больше нечего");
         this.timer.reset();
      }
   };
   private final EventListener<ChatReleaseEvent> onRelease = event -> {
      for (Popup popup : this.popups) {
         popup.onMouseReleased(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
         if (popup.isHovered(event.getX(), event.getY())) {
            return;
         }
      }

      for (HudElement element : this.elements) {
         element.onMouseReleased(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
      }
   };

   @CompileBytecode
   private void initialize() {
      Rockstar.getInstance().getEventManager().subscribe(this);
      this.elements.addAll(List.of(new Effects(), new KeyBinds(), new TargetHud(), this.island = new DynamicIsland(), new WorldElement(), new PlayerElement()));
   }

   public Hud() {
      this.initialize();
   }

   public List<HudElement> enabledElements() {
      return this.elements.stream().filter(HudElement::isShowing).toList();
   }

   public List<HudElement> disabledElements() {
      return this.elements.stream().filter(element -> !element.isShowing()).toList();
   }

   public <T extends HudElement> T getElementByName(String name) {
      return (T)this.elements.stream().filter(element -> element.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
   }

   private boolean handleClick(ChatClickEvent event, Entity entity, Vec2f screenPos) {
      float distance = entity.distanceTo(mc.player);
      float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
      Text displayName = Nametags.displayName(entity);
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
      return GuiUtility.isHovered(
         (double)scaledRectX, (double)scaledRectY, (double)scaledRectWidth, (double)scaledRectHeight, (double)event.getX(), (double)event.getY()
      );
   }

   @Generated
   public List<HudElement> getElements() {
      return this.elements;
   }

   @Generated
   public List<Popup> getPopups() {
      return this.popups;
   }

   @Generated
   public DynamicIsland getIsland() {
      return this.island;
   }

   @Generated
   public HudHistoryManager getHistoryManager() {
      return this.historyManager;
   }

   @Generated
   public Grid getGrid() {
      return this.grid;
   }

   @Generated
   public String getDesc() {
      return this.desc;
   }

   @Generated
   public AnimatedText getDescText() {
      return this.descText;
   }

   @Generated
   public Timer getTimer() {
      return this.timer;
   }

   @Generated
   public EventListener<HudRenderEvent> getOnHud() {
      return this.onHud;
   }

   @Generated
   public EventListener<ChatRenderEvent> getOnPostHud() {
      return this.onPostHud;
   }

   @Generated
   public EventListener<ChatKeyPressEvent> getOnKeyPress() {
      return this.onKeyPress;
   }

   @Generated
   public EventListener<ChatClickEvent> getOnClick() {
      return this.onClick;
   }

   @Generated
   public EventListener<ChatReleaseEvent> getOnRelease() {
      return this.onRelease;
   }

   @Generated
   public void setDesc(String desc) {
      this.desc = desc;
   }
}
