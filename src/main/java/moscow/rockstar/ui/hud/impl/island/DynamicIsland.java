package moscow.rockstar.ui.hud.impl.island;

import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.hud.impl.island.impl.BlinkStatus;
import moscow.rockstar.ui.hud.impl.island.impl.DefaultStatus;
import moscow.rockstar.ui.hud.impl.island.impl.EventStatus;
import moscow.rockstar.ui.hud.impl.island.impl.MineStatus;
import moscow.rockstar.ui.hud.impl.island.impl.MusicStatus;
import moscow.rockstar.ui.hud.impl.island.impl.NotificationStatus;
import moscow.rockstar.ui.hud.impl.island.impl.PVPStatus;
import moscow.rockstar.ui.hud.impl.island.impl.XrayStatus;
import moscow.rockstar.ui.menu.dropdown.DropDownScreen;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.animation.types.ColorAnimation;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gui.screen.ChatScreen;

public class DynamicIsland extends HudElement implements IMinecraft, IScaledResolution {
   private final SelectSetting statuses = new SelectSetting(this, "hud.dynamic_island.statuses").draggable();
   private final IslandSize size = new IslandSize(48.0F, 15.0F);
   private boolean extended;
   private final Animation extendingAnim = new Animation(200L, 0.0F, Easing.LINEAR);
   private final Animation widthAnim = new Animation(500L, 0.0F, Easing.BAKEK_SIZE);
   private final Animation heightAnim = new Animation(500L, 0.0F, Easing.BAKEK_SIZE);
   private final Animation showPing = new Animation(500L, 0.0F, Easing.BAKEK);
   private final ColorAnimation backgroundColor = new ColorAnimation(300L, new ColorRGBA(0.0F, 0.0F, 0.0F), Easing.FIGMA_EASE_IN_OUT);
   private final ColorAnimation adaptColor = new ColorAnimation(300L, new ColorRGBA(255.0F, 255.0F, 255.0F), Easing.LINEAR);
   private final Timer timer = new Timer();
   private boolean dark;
   private boolean useDark;
   private IslandStatus last;
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (this.active() instanceof MusicStatus && mc.player.age % 2 == 0) {
      }
   };

   public DynamicIsland() {
      super("hud.dynamic_island", "icons/hud/island.png");
      new NotificationStatus(this.statuses);
      new BlinkStatus(this.statuses);
      new PVPStatus(this.statuses);
      new EventStatus(this.statuses);
      new MineStatus(this.statuses);
      new XrayStatus(this.statuses);
      new MusicStatus(this.statuses);
      new DefaultStatus(this.statuses).alwaysEnabled();
      Rockstar.getInstance().getFileManager().readFile("client");
      Rockstar.getInstance().getEventManager().subscribe(this);
   }

   @Override
   protected void renderComponent(UIContext context) {
      this.width = this.size.width;
      this.height = this.size.height;
      this.x = sr.getScaledWidth() / 2.0F - this.width / 2.0F;
      this.y = 7.0F;
      BorderRadius radius = BorderRadius.all(7.0F + 11.0F * this.extendingAnim.getValue());
      String time = TextUtility.getCurrentTime();
      if (this.timer.finished(500L)) {
         float pixelX = mc.getWindow().getWidth() / 2.0F;
         float pixelY = mc.getWindow().getHeight() - (this.y + 5.0F);
         ColorRGBA pixel = ColorRGBA.fromPixel(pixelX, pixelY);
         boolean check = (pixel.getRed() + pixel.getGreen() + pixel.getBlue()) / 3.0F > 70.0F;
         this.useDark = check;
         this.timer.reset();
      }

      this.adaptColor.update(this.useDark ? new ColorRGBA(0.0F, 0.0F, 0.0F) : new ColorRGBA(255.0F, 255.0F, 255.0F));
      this.dark = this.useDark;
      ColorRGBA elmtColor = this.adaptColor.getColor().withAlpha(255.0F * (1.0F - this.extendingAnim.getValue()));
      if (mc.player != null) {
         context.drawText(Fonts.MEDIUM.getFont(7.0F), time, this.x - Fonts.MEDIUM.getFont(9.0F).width(time) - 4.0F, this.y + 5.0F, elmtColor);
         if (!mc.isInSingleplayer() && mc.player.networkHandler.getPlayerListEntry(mc.player.getUuid()) != null) {
            this.showPing.update(GuiUtility.isHovered(this.x + this.width + 4.0F + 4.0F * this.showPing.getValue(), this.y + 5.0F, 12.8F, 7.0, context));
            int ping = mc.player.networkHandler.getPlayerListEntry(mc.player.getUuid()).getLatency();
            int[] pings = new int[]{450, 300, 150, 75};
            context.drawText(
               Fonts.MEDIUM.getFont(7.0F),
               ping + " ms",
               this.x + this.width + 4.0F + 4.0F * this.showPing.getValue(),
               this.y + 5.0F,
               elmtColor.mulAlpha(this.showPing.getValue())
            );

            for (int i = 0; i < 4; i++) {
               context.drawRoundedRect(
                  this.x + this.width + 9.0F + i * 2.7F + 4.0F * this.showPing.getValue(),
                  this.y + 8.0F - i,
                  2.0F,
                  3 + i,
                  BorderRadius.all(0.1F),
                  elmtColor.withAlpha(elmtColor.getAlpha() * 0.2F * (1.0F - this.showPing.getValue()))
               );
            }

            for (int i = 0; i < 4; i++) {
               if (ping < pings[i]) {
                  context.drawRoundedRect(
                     this.x + this.width + 9.0F + i * 2.7F + 4.0F * this.showPing.getValue(),
                     this.y + 8.0F - i,
                     2.0F,
                     3 + i,
                     BorderRadius.all(0.1F),
                     elmtColor.mulAlpha(1.0F - this.showPing.getValue())
                  );
               }
            }
         } else {
            context.drawTexture(Rockstar.id("icons/airplane.png"), this.x + this.width + 8.0F, this.y + 3.5F, 8.0F, 8.0F, elmtColor);
         }
      }

      IslandStatus status = this.active();
      this.backgroundColor.update(status.getColor());
      context.drawSquircle(
         this.x - 1.0F,
         this.y - 1.0F,
         this.width + 2.0F,
         this.height + 2.0F,
         2.0F + 5.0F * this.extendingAnim.getValue(),
         BorderRadius.all(8.0F + 11.0F * this.extendingAnim.getValue()),
         Colors.WHITE.withAlpha(this.adaptColor.getColor().getRed() * 0.1F)
      );
      context.drawBlurredRect(
         this.x, this.y, this.width, this.height, 45.0F, 2.0F + 2.0F * this.extendingAnim.getValue(), radius, ColorRGBA.WHITE.withAlpha(255.0F)
      );
      context.drawSquircle(
         this.x, this.y, this.width, this.height, 2.0F + 5.0F * this.extendingAnim.getValue(), radius, this.backgroundColor.getColor().withAlpha(216.75F)
      );

      for (SelectSetting.Value islandStatus : this.statuses.getValues()) {
         ((IslandStatus)islandStatus).getAnimation().update(status == islandStatus ? 1.0F : 0.0F);
      }

      ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, this.height);
      if (status.getAnimation().getValue() == 1.0F) {
         status.draw(context);
      } else {
         for (SelectSetting.Value islandStatus : this.statuses.getValues()) {
            if (((IslandStatus)islandStatus).getAnimation().getValue() > 0.0F) {
               ((IslandStatus)islandStatus).drawWithAlpha(context);
            }
         }
      }

      ScissorUtility.pop();
      this.widthAnim.update(status.size.width);
      this.heightAnim.update(status.size.height);
      this.size.width = this.widthAnim.getValue();
      this.size.height = this.heightAnim.getValue();
      this.extendingAnim.update(this.extended ? 1.0F : 0.0F);
      if (!(status instanceof ExtandableStatus)) {
         this.extended = false;
      }

      if (!(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof DropDownScreen) && mc.player != null || this.select) {
         this.extended = false;
      }

      if (!this.extended
         && status instanceof ExtandableStatus
         && GuiUtility.isHovered(
            (double)this.x, (double)this.y, (double)this.width, (double)this.height, (double)GuiUtility.getMouse().getX(), (double)GuiUtility.getMouse().getY()
         )) {
         CursorUtility.set(CursorType.HAND);
      }
   }

   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      super.onMouseClicked(mouseX, mouseY, button);
      this.handleClick((float)mouseX, (float)mouseY, button.getButtonIndex());
   }

   public boolean handleClick(float mouseX, float mouseY, int button) {
      float x = sr.getScaledWidth() / 2.0F - this.size.width / 2.0F;
      float y = 7.0F;
      if (this.extended) {
         if (!GuiUtility.isHovered((double)x, (double)y, (double)this.size.width, (double)this.size.height, (double)mouseX, (double)mouseY)) {
            this.extended = false;
         } else {
            this.active().click(mouseX, mouseY, button);
         }

         return true;
      } else if (GuiUtility.isHovered((double)x, (double)y, (double)this.size.width, (double)this.size.height, (double)mouseX, (double)mouseY)
         && this.active() instanceof ExtandableStatus) {
         this.extended = true;
         return true;
      } else {
         return false;
      }
   }

   public IslandStatus active() {
      return this.statuses().getLast();
   }

   public List<IslandStatus> statuses() {
      return this.statuses
         .getValues()
         .stream()
         .filter(islandStatus -> ((IslandStatus)islandStatus).canShow() && islandStatus.isSelected())
         .map(islandStatus -> (IslandStatus)islandStatus)
         .toList()
         .reversed();
   }

   @Generated
   public SelectSetting getStatuses() {
      return this.statuses;
   }

   @Generated
   public IslandSize getSize() {
      return this.size;
   }

   @Generated
   public boolean isExtended() {
      return this.extended;
   }

   @Generated
   public Animation getExtendingAnim() {
      return this.extendingAnim;
   }

   @Generated
   @Override
   public Animation getWidthAnim() {
      return this.widthAnim;
   }

   @Generated
   @Override
   public Animation getHeightAnim() {
      return this.heightAnim;
   }

   @Generated
   public Animation getShowPing() {
      return this.showPing;
   }

   @Generated
   public ColorAnimation getBackgroundColor() {
      return this.backgroundColor;
   }

   @Generated
   public ColorAnimation getAdaptColor() {
      return this.adaptColor;
   }

   @Generated
   public Timer getTimer() {
      return this.timer;
   }

   @Generated
   public boolean isDark() {
      return this.dark;
   }

   @Generated
   public boolean isUseDark() {
      return this.useDark;
   }

   @Generated
   public IslandStatus getLast() {
      return this.last;
   }

   @Generated
   public EventListener<ClientPlayerTickEvent> getOnTick() {
      return this.onTick;
   }
}
