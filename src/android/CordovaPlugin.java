package by.chemerisuk.cordova.support;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;


public class CordovaPlugin extends org.apache.cordova.CordovaPlugin {
    private Map<String, Method> methodsMap;

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (methodsMap == null) {
            methodsMap = new HashMap<String, Method>();
            for (Method method : this.getClass().getDeclaredMethods()) {
                CordovaMethod cordovaMethod = method.getAnnotation(CordovaMethod.class);
                if (cordovaMethod != null) {
                    String methodAction = cordovaMethod.action();
                    if (methodAction.isEmpty()) {
                        methodAction = method.getName();
                    }
                    methodsMap.put(methodAction, method);

                    try {
                        // improve performance for future invokations:
                        // suppress Java language access checks
                        method.setAccessible(true);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        Method method = methodsMap.get(action);
        if (method != null) {
            try {
                method.invoke(this, args, callbackContext);
                return true;
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
