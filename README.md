# cordova-support-android-plugin
The plugin introduces new base class for Android Cordova plugins called `ReflectiveCordovaPlugin` that extends `CordovaPlugin` and allows to reduce amount of boilerplate code. Please read links below to understand all new features:
* [Default implementation of `execute`](https://github.com/chemerisuk/cordova-support-android-plugin/wiki/Default-implementation-of-execute)
* [Argument binding](https://github.com/chemerisuk/cordova-support-android-plugin/wiki/Argument-binding)
* [Asynchronous execution](https://github.com/chemerisuk/cordova-support-android-plugin/wiki/Asynchronous-execution)

## ProGuard notes
If you obfuscate your app with ProGuard then `proguard-rules.pro` usually contains rules:

```
-keep class org.apache.cordova.* { *; }
-keep class org.apache.cordova.engine.* { *; }
-keep public class * extends org.apache.cordova.CordovaPlugin
```

Because `ReflectiveCordovaPlugin` uses method names to invoke appropriate action you should keep any methods marked with `@CordovaMethod`:

```
-keepclassmembers class ** {
  @by.chemerisuk.cordova.support.CordovaMethod *;
}
```
