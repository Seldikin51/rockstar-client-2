package moscow.rockstar.systems.airdrop;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.config.ConfigFile;
import moscow.rockstar.systems.config.ConfigManager;
import moscow.rockstar.utility.interfaces.IMinecraft;

public class ConfigUploadServer extends NanoHTTPD implements IMinecraft {
   private final File directory = new File(mc.runDirectory, "Rockstar" + File.separator + "configs");
   private String name;
   private boolean render;

   public ConfigUploadServer() throws IOException {
      super(5656);
      if (!this.directory.exists() && !this.directory.mkdirs()) {
         throw new IOException("Не удалось создать папку для конфигов: " + this.directory);
      } else {
         this.start(5000, false);
         System.out.println("Сервер запущен на порту 5656, конфиги в " + this.directory.getAbsolutePath());
      }
   }

   public Response serve(IHTTPSession session) {
      if (Method.POST.equals(session.getMethod())) {
         try {
            Map<String, String> files = new HashMap<>();
            session.parseBody(files);
            String tmpPath = files.get("file");
            if (tmpPath != null) {
               String originalName = (String)session.getParms().get("file");
               if (originalName == null || originalName.isEmpty()) {
                  originalName = "client.rock";
               }

               this.name = originalName;
               this.render = true;
               File dest = new File(this.directory, originalName);
               Files.copy(Path.of(tmpPath), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
               System.out.println("Конфиг получен, ожидаем появления " + dest.getName());
               long start = System.currentTimeMillis();

               while (!dest.exists() && System.currentTimeMillis() - start < 5000L) {
                  try {
                     Thread.sleep(50L);
                  } catch (InterruptedException var10) {
                     break;
                  }
               }

               if (!dest.exists()) {
                  return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "Файл не появился в папке после копирования");
               }

               System.out.println("Файл подтверждён, лоадим конфиг " + dest.getName());
               ConfigManager mgr = Rockstar.getInstance().getConfigManager();
               ConfigFile cfg = mgr.getConfig(originalName);
               if (cfg == null) {
                  cfg = new ConfigFile(originalName);
                  mgr.getConfigFiles().add(cfg);
               }

               cfg.load();
               return newFixedLengthResponse("Конфиг успешно загружен и применён");
            }
         } catch (Exception var11) {
            return newFixedLengthResponse("Ошибка загрузки: " + var11.getMessage());
         }
      }

      this.render = false;
      String html = "<!DOCTYPE html> <html lang=\"ru\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>Загрузка конфига</title>\n  <style>\n    body {\n      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n      display: flex;\n      flex-direction: column;\n      align-items: center;\n      justify-content: center;\n      height: 100vh;\n      margin: 0;\n      padding: 20px;\n      background: #f2f2f2;\n    }\n\n    h1 {\n      font-size: 1.8em;\n      margin-bottom: 1em;\n      text-align: center;\n    }\n\n    form {\n      display: flex;\n      flex-direction: column;\n      gap: 15px;\n      width: 100%;\n      max-width: 400px;\n      background: #fff;\n      padding: 20px;\n      border-radius: 12px;\n      box-shadow: 0 4px 10px rgba(0,0,0,0.1);\n    }\n\n    input[type=\"file\"] {\n      font-size: 1.1em;\n    }\n\n    input[type=\"submit\"] {\n      padding: 12px;\n      font-size: 1.2em;\n      border: none;\n      border-radius: 8px;\n      background: #007aff;\n      color: white;\n      cursor: pointer;\n      transition: background 0.3s ease;\n    }\n\n    input[type=\"submit\"]:hover {\n      background: #005fcc;\n    }\n\n    @media (max-width: 400px) {\n      h1 {\n        font-size: 1.4em;\n      }\n      input[type=\"submit\"] {\n        font-size: 1em;\n        padding: 10px;\n      }\n    }\n  </style>\n</head>\n<body>\n  <h1>Загрузить конфиг Rockstar</h1>\n  <form method=\"POST\" enctype=\"multipart/form-data\">\n    <input type=\"file\" name=\"file\" accept=\".rock\" required />\n    <input type=\"submit\" value=\"Отправить в клиент\" />\n  </form>\n</body> </html>";
      return newFixedLengthResponse(Status.OK, "text/html", html);
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public boolean isRender() {
      return this.render;
   }
}
