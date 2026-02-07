package moscow.rockstar.systems.file;

import java.io.File;
import lombok.Generated;
import moscow.rockstar.systems.file.api.FileInfo;

public abstract class ClientFile {
   public final FileInfo infoAnnotation = this.getClass().getAnnotation(FileInfo.class);
   public final File file = new File(FileManager.DIRECTORY, this.infoAnnotation.name() + "." + this.infoAnnotation.fileType());

   public abstract void write();

   public abstract void read();

   @Generated
   public FileInfo getInfoAnnotation() {
      return this.infoAnnotation;
   }

   @Generated
   public File getFile() {
      return this.file;
   }
}
