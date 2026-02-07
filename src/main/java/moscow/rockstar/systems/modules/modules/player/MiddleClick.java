package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventIntegration;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.friends.FriendManager;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

@ModuleInfo(name = "Middle Click", category = ModuleCategory.PLAYER, desc = "Выполняет действие при нажатии на колесико мыши")
public class MiddleClick extends BaseModule {
   private final SelectSetting actions = new SelectSetting(this, "Действие").min(1);
   private final SelectSetting.Value clickPearl = new SelectSetting.Value(this.actions, "Бросать жемчуг").select();
   private final SelectSetting.Value clickFriend = new SelectSetting.Value(this.actions, "Добавлять друзей");
   private final BindSetting clickFriendKey = new BindSetting(this, "Клавиша друзей", () -> !this.clickFriend.isSelected());
   private final BindSetting clickPearlKey = new BindSetting(this, "Клавиша перла", () -> !this.clickPearl.isSelected());
   private final EventListener<KeyPressEvent> onKeyPressEvent = event -> this.handleKey(event.getKey(), event.getAction());
   private final EventListener<MouseEvent> onMouseEvent = event -> this.handleKey(event.getButton(), event.getAction());

   private void handleKey(int key, int action) {
      if (mc.currentScreen == null && action == 1) {
         if (this.clickFriend.isSelected() && this.clickFriendKey.isKey(key) && mc.targetedEntity instanceof PlayerEntity) {
            String nick = mc.targetedEntity.getName().getString();
            FriendManager friend = Rockstar.getInstance().getFriendManager();
            if (friend.isFriend(nick)) {
               friend.remove(nick);
            } else {
               friend.add(nick);
            }
         }

         if (this.clickPearl.isSelected() && this.clickPearlKey.isKey(key)) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.ENDER_PEARL);
         }
      }
   }
}
