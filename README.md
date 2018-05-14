# cordova-support-android-plugin
More convenient base CordovaPlugin class

## Receipts
Let's look at the simple plugin below

```java
import org.apache.cordova.CordovaPlugin;
...

public class MyPlugin extends CordovaPlugin {
    private static final String METHOD_1 = "method1";
    private static final String METHOD_2 = "method2";
 
    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (METHOD_1.equals(action)) {
            method1(callbackContext);
        } else if (METHOD_2.equals(action)) {
            method2(callbackContext);
        /// more methods...
        } else {
            return false;
        }
        return true;
    }

    protected void method1(CallbackContext callbackContext) {
        // method1 implementation
    }

    protected void method2(CallbackContext callbackContext) {
        // method2 implementation
    }
    ...
}
```

### Default implementation of `execute`
In order to use the plugin extend your plugin class from `ReflectiveCordovaPlugin`. Then mark action methods with annotation `@CordovaMethod`. Make sure that name of such method matches parameter `action` passed to `execute`

```java
import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin;
...

public class MyPlugin extends ReflectiveCordovaPlugin {
    @CordovaMethod
    protected void method1(CallbackContext callbackContext) {
        // method1 implementation
    }
    
    @CordovaMethod
    protected void method2(CallbackContext callbackContext) {
        // method2 implementation
    }
    ...
}
```

### Argument binding
Often you need to send parameters from JavaScript into Java.
```java
public class MyPlugin extends CordovaPlugin {
    private static final String METHOD_1 = "method1";
    private static final String METHOD_2 = "method2";
 
    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (METHOD_1.equals(action)) {
            method1(args.getBoolean(0), callbackContext);
        } else if (METHOD_2.equals(action)) {
            method2(args.getString(0), args.getBoolean(1), callbackContext);
        /// more methods...
        } else {
            return false;
        }
        return true;
    }

    protected void method1(boolean flag, CallbackContext callbackContext) {
        // method1 implementation
    }

    protected void method2(String id, boolean flag, CallbackContext callbackContext) {
        // method2 implementation
    }
    ...
}
```

`ReflectiveCordovaPlugin` does argument parsing for you, no extra code required:

```java
public class MyPlugin extends ReflectiveCordovaPlugin {
    @CordovaMethod
    protected void method1(boolean flag, CallbackContext callbackContext) {
        // method1 implementation
    }
    
    @CordovaMethod
    protected void method2(String id, boolean flag, CallbackContext callbackContext) {
        // method2 implementation
    }
    ...
}
```

