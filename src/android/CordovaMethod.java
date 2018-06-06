package by.chemerisuk.cordova.support;

import android.support.annotation.Keep;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Keep
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CordovaMethod {
    public String action() default "";
}
