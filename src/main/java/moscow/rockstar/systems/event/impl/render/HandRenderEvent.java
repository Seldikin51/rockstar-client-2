package moscow.rockstar.systems.event.impl.render;

import lombok.Generated;
import moscow.rockstar.systems.event.EventCancellable;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

public class HandRenderEvent extends EventCancellable {
   private final Arm arm;
   private final float swingProgress;
   private final ItemStack itemStack;
   private final float equipProgress;
   private final MatrixStack matrices;

   @Generated
   public Arm getArm() {
      return this.arm;
   }

   @Generated
   public float getSwingProgress() {
      return this.swingProgress;
   }

   @Generated
   public ItemStack getItemStack() {
      return this.itemStack;
   }

   @Generated
   public float getEquipProgress() {
      return this.equipProgress;
   }

   @Generated
   public MatrixStack getMatrices() {
      return this.matrices;
   }

   @Generated
   public HandRenderEvent(Arm arm, float swingProgress, ItemStack itemStack, float equipProgress, MatrixStack matrices) {
      this.arm = arm;
      this.swingProgress = swingProgress;
      this.itemStack = itemStack;
      this.equipProgress = equipProgress;
      this.matrices = matrices;
   }
}
