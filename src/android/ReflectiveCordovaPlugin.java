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
    private Map<String, ActionCommandFactory> factories;

    public enum ExecutionThread {
        MAIN, UI, WORKER
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (factories == null) {
            factories = createCommandFactories();
        }

        ActionCommandFactory factory = factories.get(action);
        if (factory != null) {
            final Object[] methodArgs = getMethodArgs(args, callbackContext);
            // always create a new command object
            // to avoid possible thread conflicts
            final Runnable command = new Runnable() {
                @Override
                public void run() {
                    try {
                        factory.method.invoke(ReflectiveCordovaPlugin.this, methodArgs);
                    } catch (Exception e) {
                        if (e instanceof InvocationTargetException) {
                            e = ((InvocationTargetException)e).getTargetException();
                        }
                        LOG.e(TAG, "Uncaught exception at " + getClass().getSimpleName() + "#" + factory.method.getName(), e);
                        callbackContext.error(e.getMessage());
                    }
                }
            };

            if (factory.thread == ExecutionThread.UI) {
                cordova.getActivity().runOnUiThread(command);
            } else if (factory.thread == ExecutionThread.WORKER) {
                cordova.getThreadPool().execute(command);
            } else {
                command.run();
            }

            return true;
        }

        return false;
    }

    private Map<String, ActionCommandFactory> createCommandFactories() {
        Map<String, ActionCommandFactory> result = new HashMap<String, ActionCommandFactory>();
        for (Method method : this.getClass().getDeclaredMethods()) {
            CordovaMethod cordovaMethod = method.getAnnotation(CordovaMethod.class);
            if (cordovaMethod != null) {
                String methodAction = cordovaMethod.action();
                if (methodAction.isEmpty()) {
                    methodAction = method.getName();
                }
                result.put(methodAction, new ActionCommandFactory(method, cordovaMethod.value()));
                // suppress Java language access checks
                // to improve performance of future calls
                method.setAccessible(true);
            }
        }

        return result;
    }

    private static class ActionCommandFactory {
        private final Method method;
        private final ExecutionThread thread;

        public ActionCommandFactory(Method method, ExecutionThread thread) {
            this.method = method;
            this.thread = thread;
        }
    }

    private static Object[] getMethodArgs(JSONArray args, CallbackContext callbackContext) throws JSONException {
        int len = args.length();
        Object[] methodArgs = new Object[len + 1];
        for (int i = 0; i < len; ++i) {
            Object argValue = args.opt(i);
            if (JSONObject.NULL.equals(argValue)) {
                argValue = null;
            }
            methodArgs[i] = argValue;
        }
        // CallbackContext is always the last one
        methodArgs[len] = callbackContext;

        return methodArgs;
    }
}
