/**
 * RestrictedNames.java
 */
package orchi.HHCloud.store;

import java.util.ArrayList;
import java.util.List;

/**
 * @author david 14 ago. 2018
 */
public abstract class RestrictedNames {
    private static List<String> restrictedList = new ArrayList<>();

    public static void registerName(String name) {
        if (!isRestricted(name)) {
            restrictedList.add(name);
        }
    }

    public static void UnRegisterName(String name) {
        if (!isRestricted(name)) {
            int index = -1;
            index = restrictedList.indexOf(name);
            if (index != -1) {
                restrictedList.remove(index);
            }
        }
    }

    public static boolean isRestricted(String name) {
        boolean isRestrictedName = false;

        isRestrictedName = restrictedList.contains(name);

        return isRestrictedName;
    }
}
