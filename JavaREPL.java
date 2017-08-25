/**
 * @author Carlos Aguilera
 * @apiNote 1/28/2016
 * @version 2.0.0
 */

import com.sun.source.util.JavacTask;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JavaREPL {
    private static final File tempDir = new File(System.getProperty("java.io.tmpdir"));
    private static int fileNumber = 0;
    private static URLClassLoader loader;

    public static void main(String[] args) throws IOException {
        exec(new InputStreamReader(System.in));
    }

    /**
     * @throws IOException
     **/
    public static void exec(Reader r) throws IOException {
        BufferedReader stdin = new BufferedReader(r);
        NestedReader reader = new NestedReader(stdin);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        URI temp = tempDir.toURI();
        loader = new URLClassLoader(new URL[]{temp.toURL()});
        while (true) {
            System.out.print("> ");
            String java = reader.getNestedString();
            if (java == null || java.isEmpty()) {
                break;
            }
            java = changePrint(java);
            execute(compiler, java);
        }
    }

    /**
     * @param compiler
     * @param java
     **/
    public static void execute(JavaCompiler compiler, String java) throws IOException {
        boolean child = isChild();
        Path fileToCompile = writeFile(java, tempDir, true, child);
        if (!isDeclaration(fileToCompile.toString())) {
            fileNumber--;
            fileToCompile = writeFile(java, tempDir, false, child);
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(diagnostics, null, null);
        fileManager.flush();
        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(Collections.singleton(fileToCompile.toFile()));
        Iterable<String> compileOptions =
                Arrays.asList("-d", tempDir.toString(), "-cp",
                        tempDir.toString() + System.getProperty("path.separator")
                                + System.getProperty("java.class.path"));

        JavacTask task =
                (JavacTask) compiler.getTask(null, fileManager, diagnostics, compileOptions, null, compilationUnits);
        boolean success = task.call();
        if (success) {
            Class c = load(fileToCompile.getFileName().toString());
            invoke(c);
        } else {
            fileNumber--;
            List<Diagnostic<? extends JavaFileObject>> list = diagnostics.getDiagnostics();
            for (int i = 0; i < list.size(); i++) {
                Diagnostic d = list.get(i);
                System.err.println("line " + d.getLineNumber() + ": " + d.getMessage(new Locale("English")));
            }
        }
    }

    /**
     * @param fileName - the newly written java file that has been compiled.
     * @return Class - a Class object that has been loaded by the classloader.
     */
    public static Class load(String fileName) {
        Class c = null;
        try {
            int index = fileName.indexOf(".");
            c = loader.loadClass(fileName.substring(0, index));
        } catch (ClassNotFoundException e) {
            System.err.println("Class Not Found!");
        }
        return c;
    }

    /**
     * @param c - the class we want to invoke.
     **/
    public static void invoke(Class c) {
        Method m;
        try {
            m = c.getDeclaredMethod("exec");
            m.invoke(null, null);
        } catch (NoSuchMethodException sm) {
            System.err.println("No Such Method Exception!");
        } catch (InvocationTargetException e) {
            System.err.println("Invocation Target Exception!");
        } catch (IllegalAccessException e) {
            System.err.println("Illegal Access Exception!");
        }
    }

    public static Path writeFile(String java, File directory, boolean declaration, boolean child) throws IOException {
        String fileName = "Interp_" + fileNumber;
        int prevFile = fileNumber - 1;
        Path p = Paths.get(directory.toString() + File.separator + fileName + ".java");
        Files.deleteIfExists(p);
        OutputStream out = new BufferedOutputStream(Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
        PrintWriter writer = new PrintWriter(out);
        StringBuilder parent = new StringBuilder();
        writer.append("import java.io.*;\n" +
                "import java.util.*;\n\n\n" +
                "public class ");
        if (child) {
            parent.append("extends " + "Interp_" + prevFile);
        }
        if (declaration) {
            writer.format("%s %s { \n\t" + "public static %s\n\t" +
                    "public static void exec(){\n\t%s\n\t}\n}\n", fileName, parent.toString(), java, "");
        } else {
            writer.format("%s %s {\n\t" +
                    "public static void exec() {\n\t" +
                    "%s\n\t}\n}\n", fileName, parent.toString(), java);
        }
        fileNumber++;
        writer.flush();
        writer.close();
        return p;
    }

    /**
     * @param file the being parsed for errors.
     * @return if the file is just a declaration or not
     */
    public static boolean isDeclaration(String file) throws IOException {
        File tempFile = new File(file);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(diagnostics, null, null);

        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(Collections.singleton(tempFile));

        Iterable<String> compileOptions =
                Arrays.asList("-d", tempDir.toString(), "-cp",
                        tempDir.toString() + System.getProperty("path.separator") + System.getProperty("java.class.path"));

        JavacTask task =
                (JavacTask) compiler.getTask(null, fileManager, diagnostics, compileOptions, null, compilationUnits);

        task.parse();

        List<Diagnostic<? extends JavaFileObject>> list = diagnostics.getDiagnostics();
        return list.size() == 0;
    }

    /**
     * @return if the file is the first file or the children
     */
    public static boolean isChild() {
        return fileNumber > 0;
    }


    /**
     * @param code - the java code to clean.
     * @return code - the changed java code.
     **/
    public static String changePrint(String code) {
        String newCode;
        String codeToChange;
        Pattern regex = Pattern.compile("(?ims)^print\\s*.+;");
        Matcher m = regex.matcher(code);
        while (m.find()) {
            codeToChange = m.group();
            newCode = codeToChange.replaceAll("print", "System.out.println(");
            newCode = newCode.replaceAll(";", ");");
            code = code.replace(codeToChange, newCode);
        }
        return code;
    }
}

