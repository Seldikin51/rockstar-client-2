package moscow.rockstar.utility.render.penis;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

public class PenisMeta {
   private final int width;
   private final int height;
   private final int fps;
   private final boolean loop;
   private final List<String> frameFiles;

   public static PenisMeta fromJson(String jsonString) {
      JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
      int width = json.get("width").getAsInt();
      int height = json.get("height").getAsInt();
      int fps = json.get("fps").getAsInt();
      boolean loop = json.get("loop_mode").getAsString().equals("loop");
      List<String> frameFiles = new ArrayList<>();
      JsonArray frames = json.getAsJsonArray("frames");

      for (int i = 0; i < frames.size(); i++) {
         JsonObject frame = frames.get(i).getAsJsonObject();
         frameFiles.add(frame.get("file").getAsString());
      }

      return new PenisMeta(width, height, fps, loop, frameFiles);
   }

   public long getFrameDuration() {
      return 1000L / this.fps;
   }

   public int getFrameCount() {
      return this.frameFiles.size();
   }

   @Generated
   public int getWidth() {
      return this.width;
   }

   @Generated
   public int getHeight() {
      return this.height;
   }

   @Generated
   public int getFps() {
      return this.fps;
   }

   @Generated
   public boolean isLoop() {
      return this.loop;
   }

   @Generated
   public List<String> getFrameFiles() {
      return this.frameFiles;
   }

   @Generated
   public PenisMeta(int width, int height, int fps, boolean loop, List<String> frameFiles) {
      this.width = width;
      this.height = height;
      this.fps = fps;
      this.loop = loop;
      this.frameFiles = frameFiles;
   }
}
