/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package orchi.HHCloud.auth;

import orchi.HHCloud.Start;
import orchi.HHCloud.provider.GetProvider;
import orchi.HHCloud.provider.ProviderManager;
import orchi.HHCloud.provider.ProviderManagerInstance;

@ProviderManager
public class AuthManager {
    private static String nameProvider = Start.conf.getString("auth.manager.provider");
    private static AuthManager instance;
    private AuthProvider provider;

    public AuthManager(){
        try {
            Class<? extends AuthProvider> classProvider = (Class<? extends AuthProvider>) Class.forName(nameProvider);

            provider = classProvider.newInstance();
            provider.init();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @ProviderManagerInstance
    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    @GetProvider
    public AuthProvider getProvider() {
        return provider;
    }
}
