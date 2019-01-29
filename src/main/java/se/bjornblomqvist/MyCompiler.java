package se.bjornblomqvist;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import static java.util.Arrays.asList;

import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.*;

public class MyCompiler {

    JavaCompiler javac;
    StandardJavaFileManager javacFileManager;

    public MyCompiler(List<File> jarFiles) throws IOException {

//        System.out.println(jarFiles);

        javac = ToolProvider.getSystemJavaCompiler();
        javacFileManager = javac.getStandardFileManager(null, null, null);
        javacFileManager.setLocation(StandardLocation.CLASS_PATH, jarFiles);
    }

    public Class<?> compile(String className, String sourceCodeInText) throws Exception {
        DynamicClassLoader cl = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
        SourceCode sourceCode = new SourceCode(className, sourceCodeInText);
        CompiledCode compiledCode = new CompiledCode(className);

        ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javacFileManager, compiledCode, cl);
        javac.getTask(null, fileManager, null, null, null, asList(sourceCode)).call();
        return cl.loadClass(className);
    }

    public Class<?> compile(String sourceCodeInText) throws Exception {
        try {
            int startIndex = sourceCodeInText.indexOf("public class ");
            int endIndex = sourceCodeInText.indexOf(" ", startIndex + 13);
            String className = sourceCodeInText.substring(startIndex + 13, endIndex);

            int packageStart = sourceCodeInText.indexOf("package ");
            int packageEnd = sourceCodeInText.indexOf(";", packageStart + 8);
            String packageName = sourceCodeInText.substring(packageStart + 8, packageEnd);

            return compile(packageName + "." + className, sourceCodeInText);
        } catch (java.lang.ClassFormatError error) {
            System.err.println("");
            System.err.println("Error in generated jas class!");
            System.err.println("");
            System.err.println(sourceCodeInText);
            System.err.println("");
            System.exit(2);
            return null;
        }
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
//            System.out.println("getJavaFileForOutput(" + location + ", " + className + ", " + kind + ", " + sibling + ")");
            return compiledCode;
        }

        @Override
        public ClassLoader getClassLoader(JavaFileManager.Location location) {
//            System.out.println("getClassLoader(" + location + ")");
//            System.out.println(cl);
//            System.out.println(cl.getParent());
//            System.out.println(asList(((URLClassLoader)cl.getParent()).getURLs()));
            return cl;
        }

        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds,
                                             boolean recurse) throws IOException {

            //System.out.print("list(" + location + ", " + packageName + ", " + kinds + ", " + recurse + ") -> [");
            Iterable<JavaFileObject> iterable = super.list(location, packageName, kinds, recurse);

//            if (iterable.iterator().hasNext()) {
//                System.out.println("");
//                for (JavaFileObject javaFileObject : iterable) {
//                    System.out.println("    " + javaFileObject);
//                }
//
//                System.out.println("]");
//            } else {
//                System.out.println("]");
//            }

            return iterable;
        }

        @Override
        public String inferBinaryName(Location location, JavaFileObject file) {
            //System.out.println("inferBinaryName(" + location + ", " + file + ")");
            return super.inferBinaryName(location, file);
        }

        @Override
        public boolean isSameFile(FileObject a, FileObject b) {
            //System.out.println("isSameFile(" + a + ", " + b + ")");
            return super.isSameFile(a, b);
        }

        @Override
        public boolean handleOption(String current, Iterator<String> remaining) {
            //System.out.println("handleOption(" + current + ", " + remaining + ")");
            return super.handleOption(current, remaining);
        }

        @Override
        public boolean hasLocation(Location location) {
            //System.out.println("hasLocation(" + location + ")");
            return super.hasLocation(location);
        }

        @Override
        public int isSupportedOption(String option) {
            //System.out.println("isSupportedOption(" + option + ")");
            return super.isSupportedOption(option);
        }

        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws
                                                                                                                 IOException {
            //System.out.println("getJavaFileForInput(" + location + ", " + className + ", " + kind + ")");
            return super.getJavaFileForInput(location, className, kind);
        }

        @Override
        public FileObject getFileForInput(Location location, String packageName, String relativeName) throws
                                                                                                      IOException {
            //System.out.println("getFileForInput(" + location + ", " + packageName + ", " + relativeName + ")");
            return super.getFileForInput(location, packageName, relativeName);
        }

        @Override
        public FileObject getFileForOutput(Location location, String packageName, String relativeName,
                                           FileObject sibling) throws IOException {
            //System.out.println("getFileForOutput(" + location + ", " + packageName + ", " + relativeName + ", " + sibling + ")");
            return super.getFileForOutput(location, packageName, relativeName, sibling);
        }
    }
}