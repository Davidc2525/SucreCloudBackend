package orchi.HHCloud;

import orchi.HHCloud.cipher.CipherProvider;
import orchi.HHCloud.provider.Providers;
import orchi.HHCloud.user.UserProvider;
import orchi.HHCloud.user.search.SearchUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aqui se hacen pruebas.
 */
public class Main {
    private static Logger log = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) throws Exception {

        System.out.println("HHCloud pruebas!");

        Providers.scan();


        UserProvider c = Providers.get(UserProvider.class);
        System.err.println(c.getUserByEmail("david@gmail.com"));

        System.err.println(Providers.get(SearchUserProvider.class).search("David"));
        System.err.println(Providers.get(CipherProvider.class).encrypt("Hola loca"));

        if (true) {
            System.exit(0);
        }

    }


}
