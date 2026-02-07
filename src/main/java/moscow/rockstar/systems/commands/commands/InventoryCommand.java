package moscow.rockstar.systems.commands.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class InventoryCommand implements IMinecraft {
   private final Map<String, Map<Integer, Integer>> inventories = new HashMap<>();

   @Compile
   public Command command() {
      return CommandBuilder.begin("inv")
         .aliases("inventory", "slot", "инвентарь")
         .desc("commands.inventory.description")
         .param("action", p -> p.literal("save", "create", "add", "сохранить", "load", "use", "загрузить"))
         .param(
            "name",
            p -> p.optional()
               .validator(text -> (ValidationResult)(text.length() < 2 ? ValidationResult.error("commands.prefix.invalid_length") : ValidationResult.ok(text)))
         )
         .handler(this::handle)
         .build();
   }

   @Compile
   private void handle(CommandContext ctx) {
      String action = (String)ctx.arguments().get(0);
      String name = (String)ctx.arguments().get(1);
      if (action.equals("save") || action.equals("create") || action.equals("add") || action.equals("сохранить")) {
         this.saveInventory(name);
         MessageUtility.info(Text.of(Localizator.translate("commands.inventory.saved", name)));
      } else if (!action.equals("load") && !action.equals("use") && !action.equals("загрузить")) {
         MessageUtility.error(Text.of(Localizator.translate("commands.inventory.invalid_action")));
      } else {
         this.loadInventory(name);
      }
   }

   @Compile
   private void saveInventory(String name) {
      if (mc.player != null) {
         Map<Integer, Integer> inventory = new HashMap<>();

         for (int i = 0; i <= 45; i++) {
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (!stack.isEmpty()) {
               inventory.put(i, Item.getRawId(stack.getItem()));
            }
         }

         this.inventories.put(name, inventory);
      }
   }

   private void loadInventory(String name) {
      if (!this.inventories.containsKey(name)) {
         MessageUtility.error(Text.of(Localizator.translate("commands.inventory.not_found", name)));
      } else {
         Map<Integer, Integer> savedInventory = this.inventories.get(name);
         boolean anyItemRendered = false;

         for (Entry<Integer, Integer> entry : savedInventory.entrySet()) {
            int slotIndex = entry.getKey();
            Item item = Item.byRawId(entry.getValue());
            ItemStack ghostStack = new ItemStack(item);
            ghostStack.setCount(1);
            mc.player.currentScreenHandler.getSlot(slotIndex).setStack(ghostStack);
            anyItemRendered = true;
         }

         if (anyItemRendered) {
            MessageUtility.info(Text.of(Localizator.translate("commands.inventory.loaded")));
         } else {
            MessageUtility.error(Text.of(Localizator.translate("commands.inventory.empty")));
         }
      }
   }

   @Compile
   public JsonObject save() {
      JsonObject jsonObject = new JsonObject();

      for (Entry<String, Map<Integer, Integer>> entry : this.inventories.entrySet()) {
         JsonObject innerJson = new JsonObject();

         for (Entry<Integer, Integer> innerEntry : entry.getValue().entrySet()) {
            innerJson.addProperty(innerEntry.getKey().toString(), innerEntry.getValue());
         }

         jsonObject.add(entry.getKey(), innerJson);
      }

      return jsonObject;
   }

   @Compile
   public void load(JsonElement jsonElement) {
      this.inventories.clear();
      JsonObject jsonObject = jsonElement.getAsJsonObject();

      for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
         JsonObject innerJson = entry.getValue().getAsJsonObject();
         Map<Integer, Integer> innerMap = new HashMap<>();

         for (Entry<String, JsonElement> innerEntry : innerJson.entrySet()) {
            Integer intKey = Integer.valueOf(innerEntry.getKey());
            Integer value = innerEntry.getValue().getAsInt();
            innerMap.put(intKey, value);
         }

         this.inventories.put(entry.getKey(), innerMap);
      }
   }
}
