package moscow.rockstar.ui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class TargetHud extends HudElement {
   private final BooleanSetting rayTrace = new BooleanSetting(this, "hud.targethud.look");
   private final ModeSetting armor = new ModeSetting(this, "hud.targethud.armor");
   private final ModeSetting.Value armorNone = new ModeSetting.Value(this.armor, "hud.targethud.armor.none");
   private final ModeSetting.Value armorNumber = new ModeSetting.Value(this.armor, "hud.targethud.armor.number").select();
   private final ModeSetting.Value armorIcon = new ModeSetting.Value(this.armor, "hud.targethud.armor.icon");
   private final Animation content = new Animation(300L, 0.0F, Easing.BAKEK_SIZE);
   private final Animation health = new Animation(300L, 0.0F, Easing.BAKEK);
   private final Animation golden = new Animation(300L, 0.0F, Easing.BAKEK);
   private final Animation number = new Animation(300L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
   private final Animation itemsX = new Animation(300L, 0.0F, Easing.BAKEK);
   private final Animation copy = new Animation(300L, 0.0F, Easing.BAKEK);
   private final Animation success = new Animation(500L, 0.0F, Easing.BAKEK_SIZE);
   private final Animation eatingPulse = new Animation(150L, 0.0F, Easing.BAKEK);
   private final Animation pulseIntensity = new Animation(50L, 0.0F, Easing.SINE_IN_OUT);
   private final Animation[] items = new Animation[4];
   private LivingEntity target;
   private final Timer copyTimer = new Timer();
   private boolean copied;

   public TargetHud() {
      super("hud.targethud", "icons/hud/target.png");

      for (int i = 0; i < this.items.length; i++) {
         this.items[i] = new Animation(300L, 0.0F, Easing.BAKEK);
      }
   }

   @Override
   public void update(UIContext context) {
      super.update(context);
      this.width = 103.0F;
      this.height = 31.0F;
   }

   @Override
   protected void renderComponent(UIContext context) {
      LivingEntity target = this.getTarget();
      if (target != null) {
         this.target = target;
      }

      if (this.target != null) {
         Font regular7 = Fonts.REGULAR.getFont(7.0F);
         Font semibold6 = Fonts.SEMIBOLD.getFont(6.0F);
         boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
         ColorRGBA bgColor = Colors.getBackgroundColor().withAlpha(255.0F * (dark ? 0.8F - 0.6F * Interface.glass() : 0.7F));
         boolean hover = GuiUtility.isHovered(this.x + 30.0F, this.y + 3.0F + 6.0F * this.content.getValue(), 60.0, 6.0, context);
         if (!hover || this.copyTimer.finished(1000L)) {
            this.copied = false;
         }

         boolean isEating = this.target.isUsingItem() && this.target.getActiveItem().contains(DataComponentTypes.FOOD);
         this.eatingPulse.update(isEating);
         if (isEating) {
            float pulse = (float)Math.sin(System.currentTimeMillis() / 100.0) * 0.5F + 0.5F;
            this.pulseIntensity.setValue(pulse);
         }

         this.copy.update(hover);
         this.success.update(this.copied);
         this.content.update(this.animation.getValue() * this.visible.getValue() >= 1.0F);
         this.health
            .update((this.target instanceof PlayerEntity player ? EntityUtility.getHealth(player) : this.target.getHealth()) / this.target.getMaxHealth());
         this.golden.update(this.target.getAbsorptionAmount() / 20.0F);
         float healthNum = this.target instanceof PlayerEntity playerx ? EntityUtility.getHealth(playerx) : this.target.getHealth();
         this.number.update(healthNum);
         if (this.animation.getValue() != 0.0F) {
            if (!this.armorNone.isSelected()) {
               float prev = RenderSystem.getShaderColor()[3];
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               context.drawItem(Items.DIAMOND_CHESTPLATE, -992.0F, 994.0F, 1.0F);
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, prev);
               float animOff = 0.0F;
               int i = 0;
               ItemStack[] handItems = new ItemStack[]{this.target.getMainHandStack(), this.target.getOffHandStack()};

               for (ItemStack itemStack : this.target.getArmorItems()) {
                  if (!itemStack.isEmpty()) {
                     animOff += (this.armorIcon.isSelected() ? 11.0F : 5.0F + semibold6.width(this.calculateDurabilityPercent(itemStack))) + 2.0F;
                  }
               }

               float itemSize = 11.0F;
               if (this.armorIcon.isSelected()) {
                  for (ItemStack handItem : handItems) {
                     if (!handItem.isEmpty()) {
                        animOff += 13.0F;
                     }
                  }
               }

               this.itemsX.update(animOff - 2.0F);
               float xOffset = -this.itemsX.getValue() / 2.0F;

               for (ItemStack itemStackx : this.target.getArmorItems()) {
                  this.items[i].update(!itemStackx.isEmpty());
                  float anim = this.content.getValue() * this.items[i].getValue();
                  String percent = this.calculateDurabilityPercent(itemStackx);
                  boolean isIcon = this.armorIcon.isSelected();
                  float panelWidth = isIcon ? 11.0F : 5.0F + semibold6.width(percent);
                  float panelHeight = isIcon ? 11.0F : 9.0F;
                  float panelX = this.x + this.width / 2.0F + xOffset;
                  float panelY = this.y + this.height - 4.0F + 6.0F * anim;
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, prev * anim);
                  context.drawBlurredRect(
                     panelX, panelY, panelWidth, panelHeight, 5.0F, BorderRadius.all(1.5F), ColorRGBA.WHITE.withAlpha(255.0F * this.animation.getValue())
                  );
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, prev);
                  context.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, BorderRadius.all(1.5F), bgColor.withAlpha(bgColor.getAlpha() * anim));
                  ScissorUtility.push(context.getMatrices(), panelX, panelY, panelWidth, panelHeight);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, prev * anim * 0.5F);
                  if (this.armorNumber.isSelected()) {
                     context.drawItem(itemStackx, panelX - 11.0F + panelWidth / 2.0F + 2.0F, panelY - 4.0F, 1.0F);
                  } else {
                     context.drawItem(itemStackx, panelX - 11.0F + panelWidth / 2.0F + 5.5F, panelY, 0.7F);
                  }

                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, prev);
                  ScissorUtility.pop();
                  if (this.armorNumber.isSelected()) {
                     context.drawText(semibold6, percent, panelX + 3.0F, panelY + 2.5F, Colors.getTextColor().withAlpha(255.0F * anim));
                  }

                  xOffset += (panelWidth + 2.0F) * anim;
                  i++;
               }

               float handWidth = Arrays.stream(handItems).mapToInt(item -> item.isEmpty() ? 0 : (int)(itemSize + 2.0F)).sum() - 2;
               float handX = this.armorNumber.isSelected() ? -handWidth / 2.0F : xOffset;

               for (ItemStack handItemx : handItems) {
                  if (!handItemx.isEmpty()) {
                     float handItemX = this.x + this.width / 2.0F + handX;
                     float handItemY = this.y + this.height - 4.0F + 6.0F * this.content.getValue() + (this.armorNumber.isSelected() ? 12 : 0);
                     float alpha = this.content.getValue()
                        * (isEating && this.target.getActiveItem() == handItemx ? 0.5F + 0.7F * this.pulseIntensity.getValue() : 1.0F);
                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, prev * alpha);
                     context.drawBlurredRect(
                        handItemX, handItemY, itemSize, itemSize, 5.0F, BorderRadius.all(1.5F), ColorRGBA.WHITE.withAlpha(255.0F * this.animation.getValue())
                     );
                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, prev);
                     context.drawRoundedRect(handItemX, handItemY, itemSize, itemSize, BorderRadius.all(1.5F), bgColor.withAlpha(bgColor.getAlpha() * alpha));
                     ScissorUtility.push(context.getMatrices(), handItemX, handItemY, itemSize, itemSize);
                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, prev * alpha);
                     context.drawItem(handItemx, handItemX - 11.0F + itemSize / 2.0F + 5.5F, handItemY, 0.7F);
                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, prev);
                     ScissorUtility.pop();
                     handX += itemSize + 2.0F;
                     if (this.armorIcon.isSelected()) {
                        xOffset += (itemSize + 2.0F) * this.content.getValue();
                     }
                  }
               }
            }

            context.drawShadow(
               this.x - 5.0F,
               this.y - 5.0F,
               this.width + 10.0F,
               this.height + 10.0F,
               15.0F,
               BorderRadius.all(6.0F),
               ColorRGBA.BLACK.withAlpha(63.75F * this.dragAnim.getValue())
            );
            if (Interface.showMinimalizm()) {
               context.drawBlurredRect(
                  this.x,
                  this.y,
                  this.width,
                  this.height,
                  45.0F,
                  7.0F,
                  BorderRadius.all(6.0F),
                  ColorRGBA.WHITE.withAlpha(255.0F * this.animation.getValue() * Interface.minimalizm())
               );
            }

            if (Interface.showGlass()) {
               context.drawLiquidGlass(
                  this.x,
                  this.y,
                  this.width,
                  this.height,
                  7.0F,
                  0.08F - 0.07F * this.dragAnim.getValue(),
                  BorderRadius.all(6.0F),
                  ColorRGBA.WHITE.withAlpha(255.0F * this.animation.getValue() * Interface.glass())
               );
            }

            context.drawSquircle(this.x, this.y, this.width, this.height, 7.0F, BorderRadius.all(6.0F), bgColor);
            float alpha = 255.0F * this.content.getValue();
            ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, this.height);
            if (this.target instanceof AbstractClientPlayerEntity playerxx) {
               context.drawHead(playerxx, this.x + 6.0F * this.content.getValue(), this.y + 6.0F, 19.0F, BorderRadius.all(3.0F), Colors.WHITE.withAlpha(alpha));
            } else {
               context.drawRoundedTexture(
                  Rockstar.id(
                     Interface.glassSelected()
                        ? "icons/hud/whoglass.png"
                        : (Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK ? "icons/hud/whodark.png" : "icons/hud/who.png")
                  ),
                  this.x + 6.0F * this.content.getValue(),
                  this.y + 6.0F,
                  19.0F,
                  19.0F,
                  BorderRadius.all(3.0F),
                  Colors.WHITE.withAlpha(alpha)
               );
            }

            String numberText = healthNum == 1000.0F ? "?" : TextUtility.formatNumber(this.number.getValue()).replace(",", ".");
            context.drawFadeoutText(
               regular7,
               this.target.getName().getString(),
               this.x + 30.0F + 8.0F * this.copy.getValue(),
               this.y + 3.0F + 6.0F * this.content.getValue(),
               Colors.getTextColor().withAlpha(alpha),
               0.7F,
               1.0F,
               this.width - 40.0F - 8.0F * this.copy.getValue() - semibold6.width(numberText)
            );
            RenderUtility.rotate(
               context.getMatrices(),
               this.x + 28.0F + 5.0F * this.copy.getValue(),
               this.y + 6.0F + 6.0F * this.content.getValue(),
               90.0F * this.success.getValue()
            );
            context.drawTexture(
               Rockstar.id("icons/hud/copy.png"),
               this.x + 25.0F + 5.0F * this.copy.getValue(),
               this.y + 3.0F + 6.0F * this.content.getValue(),
               6.0F,
               6.0F,
               Colors.getTextColor().withAlpha(alpha * this.copy.getValue() * (1.0F - this.success.getValue()))
            );
            RenderUtility.end(context.getMatrices());
            RenderUtility.rotate(
               context.getMatrices(),
               this.x + 28.0F + 5.0F * this.copy.getValue(),
               this.y + 6.0F + 6.0F * this.content.getValue(),
               -90.0F + 90.0F * this.success.getValue()
            );
            context.drawTexture(
               Rockstar.id("icons/check.png"),
               this.x + 25.0F + 5.0F * this.copy.getValue(),
               this.y + 3.0F + 6.0F * this.content.getValue(),
               6.0F,
               6.0F,
               Colors.GREEN.withAlpha(alpha * this.copy.getValue() * this.success.getValue())
            );
            RenderUtility.end(context.getMatrices());
            context.drawRightText(
               semibold6, numberText, this.x + this.width - 7.0F, this.y + 4.0F + 6.0F * this.content.getValue(), Colors.ACCENT.withAlpha(alpha)
            );
            context.drawRoundedRect(
               this.x + 30.0F,
               this.y + this.height - 6.0F - 6.0F * this.content.getValue(),
               65.0F,
               3.0F,
               BorderRadius.all(0.7F),
               Colors.getAdditionalColor().withAlpha(alpha * (1.0F - 0.7F * Interface.glass()))
            );
            context.drawRoundedRect(
               this.x + 30.0F,
               this.y + this.height - 6.0F - 6.0F * this.content.getValue(),
               65.0F * Math.clamp(this.health.getValue(), 0.0F, 1.0F),
               3.0F,
               BorderRadius.all(0.7F),
               Colors.ACCENT.withAlpha(alpha)
            );
            context.drawRoundedRect(
               this.x + 95.0F - 65.0F * Math.clamp(this.golden.getValue(), 0.0F, 1.0F),
               this.y + this.height - 6.0F - 6.0F * this.content.getValue(),
               65.0F * Math.clamp(this.golden.getValue(), 0.0F, 1.0F),
               3.0F,
               BorderRadius.all(0.7F),
               new ColorRGBA(255.0F, 220.0F, 81.0F, alpha)
            );
            ScissorUtility.pop();
         }
      }
   }

   private String calculateDurabilityPercent(ItemStack itemStack) {
      if (!itemStack.isEmpty() && itemStack.isDamageable()) {
         int maxDurability = itemStack.getMaxDamage();
         int currentDamage = itemStack.getDamage();
         if (currentDamage >= maxDurability) {
            return "0%";
         } else {
            double durabilityPercent = 100.0 - (double)currentDamage / maxDurability * 100.0;
            return String.format("%.0f%%", durabilityPercent);
         }
      } else {
         return "100%";
      }
   }

   private LivingEntity getTarget() {
      LivingEntity mainTarget = Rockstar.getInstance().getTargetManager().getCurrentTarget() instanceof LivingEntity target2 ? target2 : null;
      if (mainTarget != null) {
         return mainTarget;
      } else if (this.rayTrace.isEnabled() && mc.targetedEntity instanceof LivingEntity livingEntity) {
         return livingEntity;
      } else {
         return mc.currentScreen instanceof ChatScreen ? mc.player : null;
      }
   }

   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      if (GuiUtility.isHovered((double)(this.x + 30.0F), (double)(this.y + 3.0F + 6.0F * this.content.getValue()), 60.0, 6.0, mouseX, mouseY)) {
         TextUtility.copyText(mc.player.getName().getString());
         this.copyTimer.reset();
         this.copied = true;
      } else {
         super.onMouseClicked(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean show() {
      return this.getTarget() != null;
   }
}
