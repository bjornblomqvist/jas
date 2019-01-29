package se.bjornblomqvist;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static jarinstaller.Api.*;
import static jarinstaller.Api.isInstalled;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class Jas {
    public static void main(String...args) throws Exception {

        long startTime = System.currentTimeMillis();

        String helpMessage = "\n" +
                             "usage: jas [--help] " + (isInstalled() ? "" : "[--install] ") + "./script.jas\n" +
                             "\n" +
                             "jas is used to run script files with java code.\n" +
                             "\n" +
                             (isInstalled()  ? "" : "   --install    install jas (uses jarInstaller)\n") +
                             (!isInstalled() ? "" : "   --uninstall  uninstall jas (uses jarInstaller)\n") +
                             "   --help       show help\n" +
                             "\n" +
                             "   Example usage:\n" +
                             "\n" +
                             "   jas ./script.jas\n";

        if (args.length == 0) {
            System.out.println(helpMessage);
            return;
        }

        if (args[0].equals("--help")) {
            System.out.println(helpMessage);
            return;
        }

        if (!isInstalled()) {
            if (args[0].equals("--install")) {
                install();
                return;
            }
        } else {
            if (args[0].equals("--uninstall")) {
                unInstall();
                return;
            }
        }

        System.out.println("jasinfo: " + (System.currentTimeMillis() - startTime));

        String scriptFileContent = readFile(args[0]);

        List<String> requireStatements = getRequireStatments(scriptFileContent);
        List<String> importStatements = getImportStatements(scriptFileContent);

        scriptFileContent = cleanupBashStyleComments(scriptFileContent);
        scriptFileContent = cleanupImportStatements(scriptFileContent);

        String classSource = wrapInClass(scriptFileContent, importStatements);

        List<File> jarFiles = getJarFilesFor(requireStatements);

        addDefaultJarFiles(jarFiles);

        System.out.println("jasinfo: " + (System.currentTimeMillis() - startTime));
        Class<?> scriptClass = null;
        for (int i = 10; i > 0; i--) {
            MyCompiler myCompiler = new MyCompiler(jarFiles);
            scriptClass = myCompiler.compile(classSource);
        }

        System.out.println("jasinfo: " + (System.currentTimeMillis() - startTime));
        run(scriptClass, args);

        System.out.println("jasinfo: " + (System.currentTimeMillis() - startTime));
    }

    private static void addDefaultJarFiles(List<File> jarFiles) {
        String[] paths = System.getProperties().get("java.class.path").toString().split(":");
        for (String path : paths) {
            jarFiles.add(new File(path).getAbsoluteFile());
        }
    }

    private static List<File> getJarFilesFor(List<String> requireStatements) {
        List<File> jarFiles = new ArrayList<>();
        for (String requireStatment : requireStatements) {
            String cleaned = requireStatment.trim().substring(9).replace("\");", "");
            jarFiles.add(new File(cleaned).getAbsoluteFile());
        }

        return jarFiles;
    }

    private static List<String> getRequireStatments(String scriptFileContent) {
        return asList(scriptFileContent.split("\n"))
                .stream()
                .filter(row -> row.trim().startsWith("require("))
                .collect(toList());
    }

    private static List<String> getImportStatements(String scriptFileContent) {
        return asList(scriptFileContent.split("\n"))
                .stream()
                .filter(row -> row.trim().startsWith("import "))
                .collect(toList());
    }

    private static String cleanupImportStatements(String scriptFileContent) {
        List<String> rows = asList(scriptFileContent.split("\n"))
                .stream()
                .map(row -> row.trim().startsWith("import ") ? "// " + row : row)
                .collect(toList());

        return String.join("\n", rows);
    }

    private static String cleanupBashStyleComments(String scriptFileContent) {
        List<String> rows = asList(scriptFileContent.split("\n"))
                .stream()
                .map(row -> row.startsWith("#") ? "// " + row : row)
                .collect(toList());

        return String.join("\n", rows);
    }

    private static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), Charset.forName("UTF8"));
    }

    static int jasClassCounter = 0;

    private static String wrapInClass(String scriptFileContent, List<String> importStatements) {
        scriptFileContent = indent(scriptFileContent.trim(), "        ");

        StringBuilder classSource = new StringBuilder();

        classSource.append("package se.blomqvist.Jas.runtime_classes;\n");
        for (String importStatement : importStatements) {
            classSource.append(importStatement + "\n");
        }
        classSource.append("public class JASClass_" + (jasClassCounter++) + " {\n");
        classSource.append("    public static void run(String[] args) {\n");
        classSource.append(scriptFileContent + "\n");
        classSource.append("    }\n");
        classSource.append("}\n");

        return classSource.toString();
    }

    private static String indent(String linesToIndent, String indentWith) {
        return indentWith + String.join("\n" + indentWith , linesToIndent.split("\n"));
    }

    private static void run(Class<?> clazz, String[] args) throws InvocationTargetException, IllegalAccessException {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals("run")) {
                method.invoke(clazz, (Object) args);
            }
        }
    }
}
