package yeamy.restlite.annotation;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;

class SourceManifest {

    public static void create(ProcessingEnvironment env, String mainClass) throws IOException {
        FileObject f = env.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/MANIFEST.MF");
        try (OutputStream os = f.openOutputStream()) {
            os.write("Manifest-Version: 1.0\nMain-Class: ".getBytes());
            os.write(mainClass.getBytes());
//            Implementation-Version: 1.0.0
            os.flush();
        }
    }
}
