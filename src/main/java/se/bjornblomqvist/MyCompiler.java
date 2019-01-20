package se.bjornblomqvist;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.Map;
import javax.tools.*;

public class MyCompiler {
    static JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

    public static Class<?> compile(String sourceCodeInText) throws Exception {
        int startIndex = sourceCodeInText.indexOf("public class ");
        int endIndex = sourceCodeInText.indexOf(" ", startIndex + 13);
        String className = sourceCodeInText.substring(startIndex + 13, endIndex);

        int packageStart = sourceCodeInText.indexOf("package ");
        int packageEnd = sourceCodeInText.indexOf(";", packageStart + 8);
        String packageName = sourceCodeInText.substring(packageStart + 8, packageEnd);

        return compile(packageName + "." + className, sourceCodeInText);
    }

    public static Class<?> compile(String className, String sourceCodeInText) throws Exception {
        DynamicClassLoader cl = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
        SourceCode sourceCode = new SourceCode(className, sourceCodeInText);
        CompiledCode compiledCode = new CompiledCode(className);
        ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), compiledCode, cl);
        javac.getTask(null, fileManager, null, null, null, asList(sourceCode)).call();
        return cl.loadClass(className);
    }

    public static class SourceCode extends SimpleJavaFileObject {
        private String contents = null;

        public SourceCode(String className, String contents) throws Exception {
            super(URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE);
            this.contents = contents;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return contents;
        }
    }

    public static class CompiledCode extends SimpleJavaFileObject {
        private ByteArrayOutputStream baos = new ByteArrayOutputStream();

        public CompiledCode(String className) throws Exception {
            super(new URI(className), JavaFileObject.Kind.CLASS);
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return baos;
        }

        public byte[] getByteCode() {
            return baos.toByteArray();
        }
    }

    public static class DynamicClassLoader extends ClassLoader {

        private Map<String, CompiledCode> customCompiledCode = new HashMap<>();

        public DynamicClassLoader(ClassLoader parent) {
            super(parent);
        }

        public void setCode(CompiledCode cc) {
            customCompiledCode.put(cc.getName(), cc);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            CompiledCode cc = customCompiledCode.get(name);
            if (cc == null) {
                return super.findClass(name);
            }
            byte[] byteCode = cc.getByteCode();
            return defineClass(name, byteCode, 0, byteCode.length);
        }
    }

    public static class ExtendedStandardJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

        private CompiledCode compiledCode;
        private DynamicClassLoader cl;

        /**
         * Creates a new instance of ForwardingJavaFileManager.
         *
         * @param fileManager delegate to this file manager
         * @param cl
         */
        protected ExtendedStandardJavaFileManager(JavaFileManager fileManager, CompiledCode compiledCode, DynamicClassLoader cl) {
            super(fileManager);
            this.compiledCode = compiledCode;
            this.cl = cl;
            this.cl.setCode(compiledCode);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            return compiledCode;
        }

        @Override
        public ClassLoader getClassLoader(JavaFileManager.Location location) {
            return cl;
        }
    }
}