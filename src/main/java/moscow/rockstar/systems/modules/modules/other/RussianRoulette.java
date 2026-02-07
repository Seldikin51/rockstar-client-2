package moscow.rockstar.systems.modules.modules.other;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@ModuleInfo(name = "Russian Roulette", category = ModuleCategory.OTHER, desc = "Русская рулетка, как повезет :)")
public class RussianRoulette extends BaseModule {
   private final ModeSetting mode = new ModeSetting(this, "Сложность", "Легкая - выключает чит, средняя - врубает спящий режим на пк, очень сложная - ???");
   private final RussianRoulette.RouletteMode easy = new RussianRoulette.RouletteMode(this.mode, "Легкая") {
      @Override
      void execute() {
         IMinecraft.mc.stop();
      }
   };
   private final RussianRoulette.RouletteMode normal = new RussianRoulette.RouletteMode(this.mode, "Средняя") {
      @Override
      void execute() {
         try {
            Runtime.getRuntime().exec(new String[]{"rundll32.exe", "powrprof.dll,SetSuspendState", "0,1,0"});
         } catch (IOException var2) {
         }
      }
   };
   private final RussianRoulette.RouletteMode veryHard = new RussianRoulette.RouletteMode(this.mode, "Очень сложная") {
      @Override
      void execute() {
      }
   };
   private final Random RANDOM = new Random();
   private volatile Identifier qrTexture;
   private final Animation qrAnimation = new Animation(5000L, Easing.CUBIC_IN_OUT);
   private volatile boolean qrRemoving;
   private final EventListener<PreHudRenderEvent> onPreHudRender = event -> {
      if (this.qrTexture != null) {
         if (this.qrAnimation.getValue() == 1.0 && !this.qrRemoving) {
            this.qrRemoving = true;
         }

         this.qrAnimation.update(this.qrRemoving ? 0.0F : 1.0F);
      }
   };

   @Override
   public void onEnable() {
      if (mc.world != null && mc.player != null) {
         int[] drum = new int[6];
         Arrays.setAll(drum, i -> i == 5 ? 1 : 0);
         int randomValue = drum[this.RANDOM.nextInt(drum.length)];
         boolean isWin = randomValue == 0;
         if (isWin) {
            MessageUtility.info(Text.of(this.veryHard.isSelected() ? "Тебе повезло, вот тебе приз" : "Тебе повезло!"));
            if (this.veryHard.isSelected()) {
               this.generateAndShowQR("https://4lapy.ru/journal/info/taksa-osobennosti-porody-kharakter-soderzhanie/");
            }
         } else {
            MessageUtility.info(Text.of(this.veryHard.isSelected() ? "Анлак, вот тебе утешительный приз" : "Анлак"));
            if (this.veryHard.isSelected()) {
               this.generateAndShowQR("https://pornhub.com");
            }
         }

         if (this.easy.isSelected()) {
            this.easy.execute();
         } else if (this.normal.isSelected()) {
            this.normal.execute();
         }

         super.onEnable();
      }
   }

   private void generateAndShowQR(String url) {
      CompletableFuture.runAsync(() -> {
         try {
            BitMatrix matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 300, 300);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);
            NativeImage nativeImage = this.bufferedImageToNativeImage(bufferedImage);
            mc.execute(() -> {
               if (this.qrTexture != null) {
                  mc.getTextureManager().destroyTexture(this.qrTexture);
               }

               Identifier id = Rockstar.id("temp/qr/" + UUID.randomUUID());
               mc.getTextureManager().registerTexture(id, new NativeImageBackedTexture(nativeImage));
               this.qrTexture = id;
               this.qrAnimation.update(1.0F);
               this.qrRemoving = false;
            });
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      });
   }

   private NativeImage bufferedImageToNativeImage(BufferedImage bufferedImage) {
      int width = bufferedImage.getWidth();
      int height = bufferedImage.getHeight();
      NativeImage nativeImage = new NativeImage(width, height, true);

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int argb = bufferedImage.getRGB(x, y);
            nativeImage.setColorArgb(x, y, argb);
         }
      }

      return nativeImage;
   }

   @Generated
   public Identifier getQrTexture() {
      return this.qrTexture;
   }

   @Generated
   public Animation getQrAnimation() {
      return this.qrAnimation;
   }

   @Generated
   public boolean isQrRemoving() {
      return this.qrRemoving;
   }

   abstract class RouletteMode extends ModeSetting.Value {
      public RouletteMode(ModeSetting parent, String name) {
         super(parent, name);
      }

      abstract void execute();
   }
}
