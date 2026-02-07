package moscow.rockstar.utility.game;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Generated;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class ItemUtility implements IMinecraft {
   public static List<ItemStack> getItemsInShulker(ItemStack s) {
      List<ItemStack> items = new ArrayList<>();
      ContainerComponent container = (ContainerComponent)s.get(DataComponentTypes.CONTAINER);
      if (container == null) {
         BundleContentsComponent container1 = (BundleContentsComponent)s.get(DataComponentTypes.BUNDLE_CONTENTS);
         if (container1 == null) {
            return items;
         } else {
            for (ItemStack stack : container1.iterate()) {
               items.add(stack);
            }

            return items;
         }
      } else {
         for (ItemStack stack : container.iterateNonEmpty()) {
            items.add(stack);
         }

         return items;
      }
   }

   public static NbtCompound getNBT(ItemStack stack) {
      WrapperLookup registries = mc.world.getRegistryManager();
      if (stack.toNbtAllowEmpty(registries) instanceof NbtCompound compound && compound.contains("components", 10)) {
         NbtCompound components = compound.getCompound("components");
         if (components.contains("minecraft:custom_data", 10)) {
            return components.getCompound("minecraft:custom_data");
         }
      }

      return null;
   }

   public static boolean checkDonItem(ItemStack itemStack, String startWith) {
      NbtCompound customData = getNBT(itemStack);
      if (customData == null) {
         return false;
      } else if (customData.contains("don-item")) {
         String donItemName = customData.getString("don-item");
         return donItemName.contains(startWith);
      } else {
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

   public static boolean isDonItem(ItemStack itemStack) {
      NbtCompound customData = getNBT(itemStack);
      return customData == null ? false : customData.contains("don-item");
   }

   public static String donNBT(ItemStack itemStack) {
      NbtCompound customData = getNBT(itemStack);
      if (customData == null) {
         return "";
      } else {
         NbtCompound sphereEffect = customData.getCompound("sphereEffect");
         if (customData.contains("don-item")) {
            return customData.getString("don-item");
         } else if (customData.contains("spooky-item")) {
            return customData.getString("spooky-item");
         } else if (!ServerUtility.is("holyworld")
            || !customData.contains("sphereEffect", 10)
            || itemStack.getItem() != Items.TOTEM_OF_UNDYING
            || !sphereEffect.contains("rank")) {
            return "";
         } else {
            return sphereEffect.getString("rank").equals("ETERNITY") ? sphereEffect.getString("name") : sphereEffect.getString("rank");
         }
      }
   }

   public static DonateItem getDonateItem(ItemStack stack) {
      for (DonateItem item : DonateItem.values()) {
         for (String key : item.getNbt()) {
            if (donNBT(stack).equals(key)) {
               return item;
            }
         }
      }

      return null;
   }

   public static int totemFactor(ItemStack stack) {
      if (!stack.hasEnchantments()) {
         return -1;
      } else {
         for (DonateItem item : DonateItem.values()) {
            for (String key : item.getNbt()) {
               if (donNBT(stack).equals(key)) {
                  return 12 - item.getTotem();
               }
            }
         }

         return 0;
      }
   }

   public static int bestFactor(ItemStack stack) {
      if (!stack.hasEnchantments() && !isDonItem(stack)) {
         return 17;
      } else {
         for (DonateItem item : DonateItem.values()) {
            for (String key : item.getNbt()) {
               if (donNBT(stack).equals(key)) {
                  return 15 - item.getFactor();
               }
            }
         }

         return 16;
      }
   }

   @Generated
   private ItemUtility() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
