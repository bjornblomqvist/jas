package se.bjornblomqvist;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class Jas {
    public static void main(String...args) throws Exception {
        if (args.length == 0) {
            System.out.println("");
            System.out.println("    You must supplie a jas script path!");
            System.out.println("");
            System.out.println("    jas path/to/script.jas");
            System.out.println("");
            System.exit(1);
        }

        String scriptFileContent = readFile(args[0]);

        scriptFileContent = cleanupBashStyleComments(scriptFileContent);

        String classSource = wrapInClass(scriptFileContent);

        Class<?> scriptClass = MyCompiler.compile(classSource);

        run(scriptClass);
    }

    private static String cleanupBashStyleComments(String scriptFileContent) {
        List<String> rows = asList(scriptFileContent.split("\n"))
                .stream()
                .map(row -> row.startsWith("#") ? "//" + row.substring(1) : row)
                .collect(toList());

        return String.join("\n", rows);
    }

    private static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), Charset.forName("UTF8"));
    }

    static int jasClassCounter = 0;

    private static String wrapInClass(String scriptFileContent) {
        scriptFileContent = indent(scriptFileContent.trim(), "        ");

        StringBuilder classSource = new StringBuilder();

        classSource.append("package se.blomqvist.Jas.runtime_classes;\n");
        classSource.append("public class JASClass_" + (jasClassCounter++) + " {\n");
        classSource.append("    public static void run() {\n");
        classSource.append(scriptFileContent + "\n");
        classSource.append("    }\n");
        classSource.append("}\n");

        return classSource.toString();
    }

    private static String indent(String linesToIndent, String indentWith) {
        return indentWith + String.join("\n" + indentWith , linesToIndent.split("\n"));
    }

    private static void run(Class<?> clazz) throws InvocationTargetException, IllegalAccessException {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals("run")) {
                method.invoke(clazz);
            }
        }
    }
}
