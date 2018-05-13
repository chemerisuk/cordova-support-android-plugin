package by.chemerisuk.cordova.support;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONObject;
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
                    String methodAction = cordovaMethod.action();
                    if (methodAction.isEmpty()) {
                        methodAction = method.getName();
                    }
                    methodsMap.put(methodAction, method);

                    try {
                        // suppress Java language access checks
                        // to improve performance of future calls
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
                method.invoke(this, collectArgs(method, args, callbackContext));
                return true;
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    protected Object[] collectArgs(Method method, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Class<?>[] argTypes = method.getParameterTypes();
        Object[] methodArgs = new Object[argTypes.length];

        for (int i = 0; i < argTypes.length; ++i) {
            Class<?> argType = argTypes[i];
            if (CallbackContext.class.equals(argType)) {
                methodArgs[i] = callbackContext;
            } else if (JSONArray.class.equals(argType)) {
                methodArgs[i] = args.optJSONArray(i);
            } else if (JSONObject.class.equals(argType)) {
                methodArgs[i] = args.optJSONObject(i);
            } else {
                methodArgs[i] = args.get(i);
            }
        }

        return methodArgs;
    }
}
