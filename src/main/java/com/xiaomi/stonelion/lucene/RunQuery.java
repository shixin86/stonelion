
package com.xiaomi.stonelion.lucene;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
// 这个annotation可以在运行时保留在JVM中，可以用反射得到
public @interface RunQuery {
}
