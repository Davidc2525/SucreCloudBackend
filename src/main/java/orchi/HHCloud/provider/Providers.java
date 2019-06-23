package orchi.HHCloud.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class Providers {
    private static Logger log = LoggerFactory.getLogger(Providers.class);

    private static Map<Class, Object> providers = new HashMap<Class, Object>();

    public static void scan() throws InvocationTargetException, IllegalAccessException {
        log.debug("Iniciando escaneo de *managers*");
        for (Class clazz : new ScanPackage("orchi", Arrays.asList("orchi.HHCloud.HHCloudAdmin")).getList()) {
            ProviderManager pm = (ProviderManager) clazz.getAnnotation(ProviderManager.class);
            if (pm != null) {
                log.debug("Manager found: " + clazz.getName());

                Method methodGetInstance = null;
                List<Method> providers = new LinkedList<>();

                for (Method method : clazz.getDeclaredMethods()) {
                    ProviderManagerInstance getInstance = method.getDeclaredAnnotation(ProviderManagerInstance.class);
                    if (getInstance != null) {
                        log.debug("     |- getInstance: " + method.getName());
                        methodGetInstance = method;

                    }

                    GetProvider getProvider = method.getDeclaredAnnotation(GetProvider.class);
                    if (getProvider != null) {
                        log.debug("     |- GetProvider: " + method.getName() + " -> " + method.getReturnType().getName());
                        providers.add(method);
                    }

                }

                if (methodGetInstance != null) {
                    Object ins = methodGetInstance.invoke(null, null);
                    log.debug("     |- ins: " + ins);
                    for (Method provider : providers) {
                        Providers.extractInterfaces(provider.invoke(ins));
                    }
                }

            }

        }
    }

    public static void extractInterfaces(Object obj) {
        Class<?>[] interfaces = obj.getClass().getInterfaces();
        for (Class<?> infc : interfaces) {
            providers.put(infc, obj);
            extractInterfaces(infc, obj);
        }
    }

    public static void extractInterfaces(Class<?> intfc, Object obj) {
        Class<?>[] interfaces = intfc.getInterfaces();
        for (Class<?> infc : interfaces) {
            providers.put(infc, obj);
            extractInterfaces(infc, obj);
        }
    }

    public static Map<Class, Object> getProviders() {
        return providers;
    }

    public static <T> T get(Class<T> intfc) throws ProviderNotFoundException {
        T provider = (T) providers.get(intfc);
        if (provider == null) {
            throw new ProviderNotFoundException(String.format("No se encontro proveedor: %s", intfc.getName()));
        }
        return provider;
    }
}
