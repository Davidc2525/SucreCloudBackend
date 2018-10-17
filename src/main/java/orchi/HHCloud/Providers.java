package orchi.HHCloud;

import java.util.HashMap;
import java.util.Map;

public abstract class Providers {


    private static Map<Class, Object> providers = new HashMap<Class, Object>();


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

    public static Object getProvider(Class<?> intfc) throws ProviderNotFoundException {
        Object provider = providers.get(intfc);
        if(provider==null){
            throw new ProviderNotFoundException(String.format("No se encontro proveedor: %s",intfc.getName()));
        }
        return provider;
    }
}
