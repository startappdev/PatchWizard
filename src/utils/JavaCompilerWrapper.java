package utils;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class JavaCompilerWrapper {

    public static void compile(Collection<File> sourceFiles, String compiledFilesOutputDir) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(sourceFiles);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(new File(compiledFilesOutputDir)));
        if (!compiler.getTask(null, fileManager, null, null, null, compilationUnits1).call()) {
            throw new IllegalStateException("Error on compiling API");
        }
        fileManager.close();
    }
}
