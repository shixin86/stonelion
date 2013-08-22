
package com.xiaomi.stonelion.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectionDemo {
    public static void main(String[] args) {
        testMethod(ShixinDAO.class);
    }

    private static void testMethod(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            System.out.println(method.getGenericReturnType());
            System.out.println(method.getReturnType());
        }
    }

    @DAO
    interface ShixinDAO {
        @SQL("select names from idcards where userId = %id")
        @SQLController(table = "friends", applyAllTables = true)
        <T> List<T> queryIdcards(@SQLParam("id") String id);
    }

    /** Annotations **/

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface DAO {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface SQL {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface SQLController {
        String table() default "";

        boolean applyAllTables() default false;

        boolean useSlave() default false;
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SQLParam {
        String value();
    }
}
