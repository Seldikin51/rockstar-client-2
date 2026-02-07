package moscow.rockstar.ui.hud.impl.island.impl;

import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.impl.win.WindowsMediaSession;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.window.MouseScrollEvent;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.ui.hud.impl.island.ExtandableStatus;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.obj.Rect;
import moscow.rockstar.utility.sounds.MusicTracker;
import net.minecraft.util.Identifier;

public class MusicStatus extends ExtandableStatus implements IMinecraft {
   private final Animation[] waveAnims = new Animation[4];
   private final Animation pausingAnim = new Animation(300L, 0.0F, Easing.BAKEK_SIZE);
   private final Animation hoverPrevious = new Animation(300L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
   private final Animation hoverPause = new Animation(300L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
   private final Animation hoverNext = new Animation(300L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
   private final Animation hoverLyrics = new Animation(300L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
   private boolean showLyrics = false;
   private int lyricsOffset = 0;
   private final EventListener<MouseScrollEvent> onMouseScroll = event -> {
      if (this.showLyrics) {
         DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
         if (island.active() == this && island.isExtended()) {
            if (event.getVerticalAmount() < 0.0) {
               this.lyricsOffset++;
            } else {
               if (!(event.getVerticalAmount() > 0.0)) {
                  return;
               }

               this.lyricsOffset--;
            }

            String[] lines = Rockstar.getInstance().getMusicTracker().getLyrics().split("\\n");
            int maxOffset = Math.max(0, lines.length - 6);
            this.lyricsOffset = Math.min(Math.max(0, this.lyricsOffset), maxOffset);
         }
      }
   };

   public MusicStatus(SelectSetting setting) {
      super(setting, "music");

      for (int i = 0; i < this.waveAnims.length; i++) {
         this.waveAnims[i] = new Animation(400L, 0.0F, Easing.LINEAR);
      }

      Rockstar.getInstance().getEventManager().subscribe(this);
   }

   @Override
   public void draw(CustomDrawContext context) {
      DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
      float x = sr.getScaledWidth() / 2.0F - island.getSize().width / 2.0F;
      float y = 7.0F;
      MusicTracker tracker = Rockstar.getInstance().getMusicTracker();
      ColorRGBA textColor = Colors.getTextColor();
      if (tracker.haveActiveSession() && tracker.getSession() != null) {
         MediaInfo media = tracker.getSession().getMedia();
         float expWidth = 164.0F;
         float expHeight = this.showLyrics ? 125.0F : 80.0F;
         float maxWidth = 100.0F;
         float defaultWidth = 32.0F + Fonts.MEDIUM.getFont(7.0F).width(media.getTitle());
         float width = this.size.width = island.isExtended() ? expWidth : Math.min(defaultWidth, maxWidth);
         float height = this.size.height = island.isExtended() ? expHeight : 15.0F;
         float extending = island.getExtendingAnim().getValue();
         float imageMargin = 4.0F + 6.0F * extending;
         float imageSize = 7.0F + 19.0F * extending;
         Identifier trackImage = tracker.getImage() != null ? tracker.getImage() : Rockstar.id("icons/music/no_image.png");
         float imageY = y + (island.isExtended() ? imageMargin : GuiUtility.getMiddleOfBox(imageSize, island.getSize().height));
         context.drawRoundedTexture(
            trackImage, x + imageMargin - 10.0F + 10.0F * this.animation.getValue(), imageY, imageSize, imageSize, BorderRadius.all(1.0F + 5.0F * extending)
         );
         context.drawFadeoutText(
            Fonts.MEDIUM.getFont(7.0F),
            tracker.getSession().getMedia().getTitle(),
            x - 5.0F + 10.0F * this.animation.getValue() + 10.0F * this.animation.getValue() + 29.0F * extending,
            y + 5.0F + 11.0F * extending,
            textColor,
            0.3F,
            0.7F,
            island.isExtended() ? expWidth - 30.0F : maxWidth + 5.0F
         );
         if (extending != 0.0F && tracker.getSession() != null) {
            context.drawFadeoutText(
               Fonts.REGULAR.getFont(7.0F),
               tracker.getSession().getMedia().getArtist(),
               x + 20.0F + 24.0F * extending,
               y + 5.0F + 19.0F * extending,
               textColor.withAlpha(178.5F * extending),
               0.3F,
               0.7F,
               island.isExtended() ? expWidth - 30.0F : maxWidth + 5.0F
            );
            context.drawText(
               Fonts.REGULAR.getFont(5.0F),
               formatTime(media.getPosition()),
               sr.getScaledWidth() / 2.0F - expWidth / 2.0F + 11.0F * extending,
               y + 43.0F * extending,
               textColor.withAlpha(255.0F)
            );
            context.drawText(
               Fonts.REGULAR.getFont(5.0F),
               formatTime(media.getDuration()),
               sr.getScaledWidth() / 2.0F + expWidth / 2.0F - (9.5F + Fonts.REGULAR.getFont(5.0F).width(formatTime(media.getDuration())) * extending),
               y + 43.0F * extending,
               textColor.withAlpha(255.0F)
            );
            float barWidth = 116.0F;
            float barX = sr.getScaledWidth() / 2.0F - barWidth / 2.0F;
            context.drawRoundedRect(
               barX, y + expHeight - (this.showLyrics ? 45 : 0) - 36.5F * extending, barWidth, 3.0F, BorderRadius.all(0.5F), textColor.withAlpha(63.75F)
            );
            float progressWidth = barWidth * Math.min(1.0F, (float)media.getPosition() / (float)media.getDuration());
            context.drawRoundedRect(
               barX, y + expHeight - (this.showLyrics ? 45 : 0) - 36.5F * extending, progressWidth, 3.0F, BorderRadius.all(0.5F), textColor.withAlpha(150.0F)
            );
            this.pausingAnim.setDuration(600L);
            this.pausingAnim.update(media.isPlaying() ? 1.0F : 0.0F);
            if (extending > 0.7F) {
               float controlY = y + expHeight - 25.0F * extending;
               double mouseX = GuiUtility.getMouse().getX();
               double mouseY = GuiUtility.getMouse().getY();
               Rect previous = new Rect(sr.getScaledWidth() / 2.0F - 40.0F, controlY, 16.0F, 16.0F);
               Rect pause = new Rect(sr.getScaledWidth() / 2.0F - 8.0F, controlY, 16.0F, 16.0F);
               Rect next = new Rect(sr.getScaledWidth() / 2.0F + 24.0F, controlY, 16.0F, 16.0F);
               if (previous.hovered(mouseX, mouseY) || pause.hovered(mouseX, mouseY) || next.hovered(mouseX, mouseY)) {
                  CursorUtility.set(CursorType.HAND);
               }

               this.hoverPrevious.update(previous.hovered(mouseX, mouseY));
               this.hoverPause.update(pause.hovered(mouseX, mouseY));
               this.hoverNext.update(next.hovered(mouseX, mouseY));
               context.drawTexture(Rockstar.id("icons/music/previous.png"), previous, textColor.withAlpha(255.0F - 100.0F * this.hoverPrevious.getValue()));
               float anim = this.pausingAnim.getValue();
               float centerX = pause.getX() + pause.getWidth() / 2.0F;
               float centerY = pause.getY() + pause.getHeight() / 2.0F;
               RenderUtility.rotate(context.getMatrices(), centerX, centerY, 90.0F * anim);
               RenderUtility.scale(context.getMatrices(), centerX, centerY, 1.0F - anim);
               context.drawTexture(
                  Rockstar.id("icons/music/play.png"), pause, textColor.withAlpha(255.0F * (1.0F - anim) - 100.0F * this.hoverPause.getValue())
               );
               RenderUtility.end(context.getMatrices());
               RenderUtility.end(context.getMatrices());
               RenderUtility.rotate(context.getMatrices(), centerX, centerY, -90.0F + 90.0F * anim);
               RenderUtility.scale(context.getMatrices(), centerX, centerY, anim);
               context.drawTexture(Rockstar.id("icons/music/pause.png"), pause, textColor.withAlpha(255.0F * anim - 100.0F * this.hoverPause.getValue()));
               RenderUtility.end(context.getMatrices());
               RenderUtility.end(context.getMatrices());
               context.drawTexture(Rockstar.id("icons/music/next.png"), next, textColor.withAlpha(255.0F - 100.0F * this.hoverNext.getValue()));
            }

            String owner = null;
            if (tracker.getSession() == null) {
               return;
            }

            if (tracker.getSession().getOwner().toLowerCase().contains("yandex") || tracker.getSession().getOwner().toLowerCase().contains("яндекс")) {
               owner = "yandex_music";
            } else if (tracker.getSession().getOwner().toLowerCase().contains("edge")) {
               owner = "edge";
            } else if (tracker.getSession().getOwner().toLowerCase().contains("spotify")) {
               owner = "spotify";
            }

            if (owner != null) {
               context.drawTexture(Rockstar.id("icons/media/" + owner + ".png"), x + expWidth - 22.0F, y + expHeight - 21.0F, 8.0F, 8.0F, ColorRGBA.WHITE);
            }

            switch (WindowsMediaSession.getCycle()) {
               case 0:
                  context.drawTexture(Rockstar.id("icons/music/repeat.png"), x + 14.0F, y + expHeight - 21.0F, 8.0F, 8.0F, textColor.withAlpha(150.0F));
                  break;
               case 1:
                  context.drawTexture(Rockstar.id("icons/music/repeat.png"), x + 14.0F, y + expHeight - 21.0F, 8.0F, 8.0F, textColor);
                  break;
               case 2:
                  context.drawTexture(Rockstar.id("icons/music/repeat1.png"), x + 14.0F, y + expHeight - 21.0F, 8.0F, 8.0F, textColor);
            }

            if (tracker.getLyrics().isEmpty()) {
               this.showLyrics = false;
            }

            Rect lyricsRect = new Rect(x + 14.0F, y + expHeight - 21.0F, 8.0F, 8.0F);
            if (!tracker.getLyrics().isEmpty() && tracker.getLyrics().split("butors\\n\\n").length > 1) {
               if (lyricsRect.hovered(GuiUtility.getMouse().getX(), GuiUtility.getMouse().getY())) {
                  CursorUtility.set(CursorType.HAND);
               }

               this.hoverLyrics.update(lyricsRect.hovered(GuiUtility.getMouse().getX(), GuiUtility.getMouse().getY()));
               context.drawTexture(Rockstar.id("icons/music/text.png"), lyricsRect, textColor.withAlpha(255.0F - 100.0F * this.hoverLyrics.getValue()));
            }

            if (this.showLyrics && tracker.getLyrics().split("butors\\n\\n").length > 1) {
               String[] lines = tracker.getLyrics().split("butors\\n\\n")[1].split("\\n");
               int maxLines = Math.min(6, lines.length);
               if (this.lyricsOffset > lines.length - maxLines) {
                  this.lyricsOffset = Math.max(lines.length - maxLines, 0);
               }

               for (int i = 0; i < maxLines && i + this.lyricsOffset < lines.length; i++) {
                  context.drawFadeoutText(
                     Fonts.REGULAR.getFont(6.0F),
                     lines[i + this.lyricsOffset],
                     x + 10.0F,
                     y + 55.0F + i * 7,
                     textColor.withAlpha(255.0F * extending),
                     0.91F,
                     1.0F,
                     expWidth - 20.0F
                  );
               }
            }
         }

         for (int i = 0; i < this.waveAnims.length; i++) {
            float phase = (float)media.getPosition() * 8.0F + i * 0.7F;
            float size = media.isPlaying() ? (float)(2.0 + Math.abs(MathUtility.sin(phase)) * 8.0) : 3.0F;
            this.waveAnims[i].update(size);
            this.waveAnims[i].setDuration(1000L);
            context.drawRoundedRect(
               x
                  + MathUtility.interpolate(Math.min(defaultWidth, maxWidth), expWidth - 10.0F, extending)
                  - 2.0F
                  - 10.0F * this.animation.getValue()
                  + i * (2.0F + extending),
               y + MathUtility.interpolate(4.25, 14.0, extending) + (7.0F - this.waveAnims[i].getValue()) / 2.0F,
               1.0F + extending,
               this.waveAnims[i].getValue(),
               BorderRadius.all(0.5F),
               tracker.getMediaColor()
            );
         }
      }
   }

   @Override
   public void click(float mouseX, float mouseY, int button) {
      DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
      float x = sr.getScaledWidth() / 2.0F - island.getSize().width / 2.0F;
      float y = 7.0F;
      float width = this.size.width;
      float height = this.size.height;
      MusicTracker tracker = Rockstar.getInstance().getMusicTracker();
      if (tracker.haveActiveSession()) {
         if (GuiUtility.isHovered((double)(x + width / 2.0F - 40.0F), (double)(y + height - 9.0F - 16.0F), 16.0, 16.0, (double)mouseX, (double)mouseY)) {
            tracker.getSession().previous();
         }

         if (GuiUtility.isHovered((double)(x + width / 2.0F - 8.0F), (double)(y + height - 9.0F - 16.0F), 16.0, 16.0, (double)mouseX, (double)mouseY)) {
            tracker.getSession().playPause();
         }

         if (GuiUtility.isHovered((double)(x + width / 2.0F + 24.0F), (double)(y + height - 9.0F - 16.0F), 16.0, 16.0, (double)mouseX, (double)mouseY)) {
            tracker.getSession().next();
         }

         if (GuiUtility.isHovered((double)(x + 14.0F), (double)(y + height - 21.0F), 8.0, 8.0, (double)mouseX, (double)mouseY)) {
            tracker.getSession().swapCycle();
            WindowsMediaSession.setCycle(tracker.getSession().getCycleType());
         }

         Rect lyricsRect = new Rect(x + 14.0F, y + height - 21.0F, 8.0F, 8.0F);
         if (!tracker.getLyrics().isEmpty()
            && GuiUtility.isHovered(
               (double)lyricsRect.getX(),
               (double)lyricsRect.getY(),
               (double)lyricsRect.getWidth(),
               (double)lyricsRect.getHeight(),
               (double)mouseX,
               (double)mouseY
            )
            && tracker.getLyrics().split("butors\\n\\n").length > 1) {
            this.showLyrics = !this.showLyrics;
            if (this.showLyrics) {
               this.lyricsOffset = 0;
            }
         }
      }
   }

   @Override
   public boolean canShow() {
      return Rockstar.getInstance().getMusicTracker().getSession() != null
         && Rockstar.getInstance().getMusicTracker().haveActiveSession()
         && !Rockstar.getInstance().getMusicTracker().getSession().getOwner().toLowerCase().contains("gram");
   }

   public static String formatTime(long totalSeconds) {
      long minutes = totalSeconds / 60L;
      long seconds = totalSeconds % 60L;
      return String.format("%d:%02d", minutes, seconds);
   }

   @Override
   public ColorRGBA getColor() {
      return super.getColor().mix(Rockstar.getInstance().getMusicTracker().getMediaColor(), 0.2F);
   }
}
