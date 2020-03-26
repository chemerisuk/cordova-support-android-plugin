package by.chemerisuk.cordova.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin.ExecutionThread;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CordovaMethod {
    ExecutionThread value() default ExecutionThread.MAIN;
    String action() default "";
}
