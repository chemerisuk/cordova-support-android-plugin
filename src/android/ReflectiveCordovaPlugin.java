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
                    Class[] paramTypes = method.getParameterTypes();
                    if (!CallbackContext.class.equals(paramTypes[paramTypes.length - 1])) {
                        LOG.e(TAG, "Method with @CordovaMethod must have CallbackContext as the last parameter");
                    } else {
                        String methodAction = cordovaMethod.action();
                        if (methodAction.isEmpty()) {
                            methodAction = method.getName();
                        }
                        methodsMap.put(methodAction, new CordovaMethodCommand(
                            this, method, cordovaMethod));
                        // suppress Java language access checks
                        // to improve performance of future calls
                        method.setAccessible(true);
                    }
                }
            }
        }

        CordovaMethodCommand command = methodsMap.get(action);
        if (command != null) {
            command.init(args, callbackContext);
            if (command.ui) {
                cordova.getActivity().runOnUiThread(command);
            } else if (command.async) {
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
        private final boolean ui;
        private Object[] methodArgs;
        private CallbackContext callback;

        public CordovaMethodCommand(CordovaPlugin plugin, Method method, CordovaMethod cordovaMethod) {
            this.plugin = plugin;
            this.method = method;
            this.async = cordovaMethod.async();
            this.ui = cordovaMethod.ui();
        }

        public void init(JSONArray args, CallbackContext callbackContext) throws JSONException {
            int len = args.length();
            this.methodArgs = new Object[len + 1];
            for (int i = 0; i < len; ++i) {
                Object argValue = args.opt(i);
                if (JSONObject.NULL.equals(argValue)) {
                    argValue = null;
                }
                this.methodArgs[i] = argValue;
            }
            // CallbackContext is always the last one
            this.methodArgs[len] = callbackContext;
            this.callback = callbackContext;
        }

        @Override
        public void run() {
            try {
                this.method.invoke(this.plugin, this.methodArgs);
            } catch (InvocationTargetException e) {
                LOG.e(TAG, "Invocation exception from plugin", e.getTargetException());
                this.callback.error(e.getTargetException().getMessage());
            } catch (Exception e) {
                LOG.e(TAG, "Uncaught exception from plugin", e);
                this.callback.error(e.getMessage());
            } finally {
                this.methodArgs = null;
                this.callback = null;
            }
        }
    }
}
