package moscow.rockstar.utility.game.countermine;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.rotations.Rotation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EquipmentSlot.Type;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class CMUtility implements IMinecraft {
   public static Text removeTextFromComponent(Text original, String textToRemove) {
      return original instanceof MutableText mutableText
         ? removeTextFromMutableText(mutableText.copy(), textToRemove)
         : removeTextFromMutableText(original.copy(), textToRemove);
   }

   private static MutableText removeTextFromMutableText(MutableText text, String textToRemove) {
      if (text instanceof PlainTextContent literalContent) {
         String content = literalContent.string();
         if (content.contains(textToRemove)) {
            String cleanedContent = content.replace(textToRemove, "");
            MutableText newText = Text.literal(cleanedContent);
            newText.setStyle(text.getStyle());

            for (Text sibling : text.getSiblings()) {
               newText.append(removeTextFromComponent(sibling, textToRemove));
            }

            return newText;
         }
      }

      MutableText result = Text.empty().setStyle(text.getStyle());
      result.append(text.copy());
      List<Text> cleanedSiblings = new ArrayList<>();

      for (Text sibling : text.getSiblings()) {
         Text cleanedSibling = removeTextFromComponent(sibling, textToRemove);
         if (!cleanedSibling.getString().isEmpty()) {
            cleanedSiblings.add(cleanedSibling);
         }
      }

      MutableText finalText = Text.empty().setStyle(text.getStyle());
      String mainContent = getMainContent(text);
      if (!mainContent.isEmpty()) {
         mainContent = mainContent.replace(textToRemove, "");
         if (!mainContent.isEmpty()) {
            finalText.append(Text.literal(mainContent).setStyle(text.getStyle()));
         }
      }

      for (Text siblingx : cleanedSiblings) {
         finalText.append(siblingx);
      }

      return finalText;
   }

   private static String getMainContent(Text text) {
      return text instanceof PlainTextContent literalContent ? literalContent.string() : "";
   }

   public static void removeAllArmor() {
      for (Entity entity : mc.world.getPlayers()) {
         if (entity instanceof PlayerEntity livingEntity) {
            removeArmorFromEntity(livingEntity);
         }
      }
   }

   private static void removeArmorFromPlayer(PlayerEntity player) {
      for (EquipmentSlot slot : EquipmentSlot.values()) {
         if (slot.getType() == Type.HUMANOID_ARMOR || slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            ItemStack currentItem = player.getEquippedStack(slot);
            if (!currentItem.isEmpty()) {
               player.getInventory().insertStack(currentItem.copy());
               player.equipStack(slot, ItemStack.EMPTY);
            }
         }
      }
   }

   private static void removeArmorFromEntity(PlayerEntity entity) {
      for (EquipmentSlot slot : EquipmentSlot.values()) {
         if (slot.getType() == Type.HUMANOID_ARMOR) {
            ItemStack currentArmor = entity.getEquippedStack(slot);
            if (!currentArmor.isEmpty()) {
               entity.getInventory().insertStack(currentArmor.copy());
               entity.equipStack(slot, ItemStack.EMPTY);
            }
         }
      }
   }

   public static boolean isPlayerOnline(String playerName) {
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

   public static Rotation calculateRotation(Vec3d targetPos) {
      Vec3d eyes = mc.player.getEyePos();
      double dx = targetPos.x - eyes.x;
      double dy = targetPos.y - eyes.y;
      double dz = targetPos.z - eyes.z;
      double dist = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
      return new Rotation(yaw, pitch);
   }

   @Generated
   private CMUtility() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
