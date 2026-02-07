package moscow.rockstar.mixin.minecraft.client.gui.screen;

import moscow.rockstar.Rockstar;
import moscow.rockstar.mixin.accessors.ScreenAccessor;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.group.impl.ArmorSlotsGroup;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends RecipeBookScreen<PlayerScreenHandler> implements IMinecraft {
   public InventoryScreenMixin(PlayerScreenHandler handler, RecipeBookWidget<?> recipeBook, PlayerInventory inventory, Text title) {
      super(handler, recipeBook, inventory, title);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void dropButton(CallbackInfo ci) {
      if (!Rockstar.INSTANCE.isPanic()) {
         ButtonWidget widget = ButtonWidget.builder(Text.of(Localizator.translate("inventory.button.drop_all")), b -> this.dropAll())
            .dimensions(this.x + this.backgroundWidth / 2 - 40, this.y - 20, 80, 18)
            .build();
         ((ScreenAccessor)this).invokeAddDrawableChild(widget);
      }
   }

   @Unique
   private void dropAll() {
      SlotGroup<ItemSlot> slots = SlotGroups.inventory().and(SlotGroups.hotbar()).and(SlotGroups.offhand()).and(new ArmorSlotsGroup());

      for (ItemSlot slot : slots.getSlots()) {
         if (!slot.isEmpty()) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot.getIdForServer(), 1, SlotActionType.THROW, mc.player);
         }
      }
   }
}
