package moscow.rockstar.systems.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.systems.file.impl.ClientDataFile;
import net.minecraft.client.MinecraftClient;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.Initialization;

public class FileManager {
   public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   public static final File DIRECTORY = new File(MinecraftClient.getInstance().runDirectory, "Rockstar");
   public static final String DEFAULT_FILE_TYPE = "rock";
   private final List<ClientFile> clientFiles = new ArrayList<>();

   public FileManager() {
      try {
         if (!DIRECTORY.exists()) {
            Files.createDirectories(Path.of(DIRECTORY.toURI()));
         }
      } catch (IOException var2) {
         System.err.println("Error creating directory: " + var2.getMessage());
      }
   }

   @Initialization
   public void registerClientFiles() {
      this.clientFiles.add(new ClientDataFile());
   }

   public ClientFile getClientFile(String clientFileName) {
      return this.clientFiles.stream().filter(clientFile -> clientFile.getInfoAnnotation().name().equalsIgnoreCase(clientFileName)).findFirst().orElse(null);
   }

   public void readFile(ClientFile clientFile) {
      try {
         if (clientFile.getFile().exists()) {
            clientFile.read();
         }
      } catch (Exception var3) {
         System.err.println("Error reading file: " + var3.getMessage());
      }
   }

   public void readFile(String clientFileName) {
      ClientFile clientFile = this.getClientFile(clientFileName);
      if (clientFile != null) {
         this.readFile(clientFile);
      }
   }

   public void writeFile(ClientFile clientFile) {
      try {
         if (!clientFile.getFile().exists()) {
            clientFile.getFile().createNewFile();
         }

         clientFile.write();
      } catch (IOException var3) {
         System.err.println("Error saving file: " + var3.getMessage());
      }
   }

   public void writeFile(String clientFileName) {
      ClientFile clientFile = this.getClientFile(clientFileName);
      if (clientFile != null) {
         clientFile.write();
      }
   }

   @Compile
   @Initialization
   public void loadClientFiles() {
      for (ClientFile file : this.clientFiles) {
         this.readFile(file);
      }
   }

   public void saveClientFiles() {
      for (ClientFile file : this.clientFiles) {
         this.writeFile(file);
      }
   }

   @Generated
   public List<ClientFile> getClientFiles() {
      return this.clientFiles;
   }
}
