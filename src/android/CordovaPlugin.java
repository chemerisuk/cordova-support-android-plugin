package by.chemerisuk.cordova.support;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;


public class CordovaPlugin extends org.apache.cordova.CordovaPlugin {
    private Map<String, Method> methodsMap;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (methodsMap == null) {
            methodsMap = new HashMap<String, Method>();
            for (Method method : this.getClass().getDeclaredMethods()) {
                CordovaMethod cordovaMethod = method.getAnnotation(CordovaMethod.class);
                if (cordovaMethod != null) {
                    try {
                        method.setAccessible(true);
                        if (cordovaMethod.action().isEmpty()) {
                            methodsMap.put(method.getName(), method);
                        } else {
                            methodsMap.put(cordovaMethod.action(), method);
                        }
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
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }
}
