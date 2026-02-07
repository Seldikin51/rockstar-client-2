package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.Removals;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.ui.hud.impl.island.impl.PVPStatus;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public class BossBarHudMixin implements IMinecraft {
   @Shadow
   @Final
   private Map<UUID, ClientBossBar> bossBars;
   @Unique
   private static final Pattern PVP_TIME_PATTERN = Pattern.compile("(\\d+)\\s*[сc][еe][кk](?=$|\\s|\\p{Punct})", 66);
   private static final String FILTERED_TEXT = "둅ꈣꈃ둄ꈣꈅ";
   private final Map<UUID, String> lastProcessedNames = new HashMap<>();

   @Inject(method = "render", at = @At("HEAD"))
   private void onRenderHead(DrawContext context, CallbackInfo ci) {
      int ctTimer = 0;

      for (ClientBossBar bossBar : this.bossBars.values()) {
         if (bossBar.getName() != null) {
            String name = bossBar.getName().getString().toLowerCase();
            if (name.contains("бой") || name.contains("pvp")) {
               Matcher matcher = PVP_TIME_PATTERN.matcher(bossBar.getName().getString());
               if (matcher.find()) {
                  ctTimer = Integer.parseInt(matcher.group(1));
               }
               break;
            }
         }
      }

      ServerUtility.setHasCT(ctTimer > 0);
      ServerUtility.setCtTime(ctTimer);
      Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
      if (!removals.isEnabled() || !removals.getBossBar().isSelected()) {
         if (Rockstar.getInstance().getHud().getIsland().isShowing()
            && !this.bossBars.isEmpty()
            && (!removals.isEnabled() || !removals.getBossBar().isSelected())
            && !ServerUtility.isCM()) {
            DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
            boolean islandShowingPvp = island.isShowing() && island.statuses().stream().anyMatch(status -> status instanceof PVPStatus);
            if (removals.isEnabled() && removals.getBossBar().isSelected() || ServerUtility.hasCT && islandShowingPvp) {
               return;
            }

            context.getMatrices().push();
            context.getMatrices().translate(0.0F, Rockstar.getInstance().getHud().getIsland().getSize().height + 7.0F, 0.0F);
         }
      }
   }

   @Inject(method = "render", at = @At("HEAD"), cancellable = true)
   private void render(CallbackInfo ci) {
      Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
      DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
      boolean islandShowingPvp = island.isShowing() && island.statuses().stream().anyMatch(status -> status instanceof PVPStatus);
      if (removals.isEnabled() && removals.getBossBar().isSelected() || ServerUtility.hasCT && islandShowingPvp) {
         ci.cancel();
      }
   }

   @Inject(method = "render", at = @At("RETURN"))
   private void onRenderReturn(DrawContext context, CallbackInfo ci) {
      int j = 19 * this.bossBars.size();
      Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
      if (!removals.isEnabled() || !removals.getBossBar().isSelected()) {
         if (Rockstar.getInstance().getHud().getIsland().isShowing()
            && !this.bossBars.isEmpty()
            && (!removals.isEnabled() || !removals.getBossBar().isSelected())
            && !ServerUtility.isCM()) {
            context.getMatrices().pop();
         }
      }
   }
}
