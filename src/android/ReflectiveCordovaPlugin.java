package by.chemerisuk.cordova.support;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.HashMap;


public class ReflectiveCordovaPlugin extends CordovaPlugin {
    private static String TAG = "ReflectiveCordovaPlugin";
    private Map<String, CordovaMethodCommand> methodsMap;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (methodsMap == null) {
            methodsMap = new HashMap<String, CordovaMethodCommand>();
            for (Method method : this.getClass().getDeclaredMethods()) {
                CordovaMethod cordovaMethod = method.getAnnotation(CordovaMethod.class);
                if (cordovaMethod != null) {
                    String methodAction = cordovaMethod.action();
                    if (methodAction.isEmpty()) {
                        methodAction = method.getName();
                    }
                    methodsMap.put(methodAction, new CordovaMethodCommand(
                        this, method, cordovaMethod.async()));
                    // suppress Java language access checks
                    // to improve performance of future calls
                    method.setAccessible(true);
                }
            }
        }

        CordovaMethodCommand command = methodsMap.get(action);
        if (command != null) {
            command.init(args, callbackContext);
            if (command.isAsync()) {
                cordova.getThreadPool().execute(command);
            } else {
                command.run();
            }
            return true;
        }

        return false;
    }

    private static class CordovaMethodCommand implements Runnable {
        private final CordovaPlugin plugin;
        private final Method method;
        private final boolean async;
        private final Class<?>[] argTypes;
        private Object[] methodArgs;
        private CallbackContext callback;

        public CordovaMethodCommand(CordovaPlugin plugin, Method method, boolean async) {
            this.plugin = plugin;
            this.method = method;
            this.async = async;
            this.argTypes = method.getParameterTypes();
        }

        public void init(JSONArray args, CallbackContext callbackContext) throws JSONException {
            this.methodArgs = new Object[this.argTypes.length];
            this.callback = callbackContext;

            for (int i = 0; i < this.argTypes.length; ++i) {
                Class<?> argType = this.argTypes[i];
                if (CallbackContext.class.equals(argType)) {
                    this.methodArgs[i] = callbackContext;
                } else if (JSONArray.class.equals(argType)) {
                    this.methodArgs[i] = args.optJSONArray(i);
                } else if (JSONObject.class.equals(argType)) {
                    this.methodArgs[i] = args.optJSONObject(i);
                } else {
                    this.methodArgs[i] = args.get(i);
                }
            }
        }

        @Override
        public void run() {
            try {
                this.method.invoke(this.plugin, this.methodArgs);
            } catch (InvocationTargetException e) {
                LOG.e(TAG, "Reflection exception from plugin", e.getTargetException());
                this.callback.error(e.getTargetException().getMessage());
            } catch (Exception e) {
                LOG.e(TAG, "Uncaught exception from plugin", e);
                this.callback.error(e.getMessage());
            } finally {
                this.methodArgs = null;
                this.callback = null;
            }
        }

        public boolean isAsync() {
            return this.async;
        }
    }
}
