package se.bjornblomqvist;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class Require {
    public static void require(String aString) {
        try {
            addURL((URLClassLoader) Jas.class.getClassLoader(), new File(aString).toURI().toURL().toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addURL(URLClassLoader urlClassLoader, String url) {
//        System.out.println(url);
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{ URL.class });
            method.setAccessible(true);
            method.invoke(urlClassLoader, new Object[]{new URL(url)});
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Error, could not add URL to system classloader", t);
        }
    }
}
