package moscow.rockstar.systems.file.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FileInfo {
   String name();

   String fileType() default "rock";
}
