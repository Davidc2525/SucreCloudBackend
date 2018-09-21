package orchi.HHCloud.store;

import orchi.HHCloud.Start;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.User;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class ContextStore {
    public static Path rootPath = Paths.get(Start.conf.getString("store.contextstore.root.path.store.fs"));
    public static String filesStorePath = Start.conf.getString("store.contextstore.root.path.store.fs.user.files.paths");


    public static void createUserContext(User user) {
        try {
            Start.getStoreManager().getStoreProvider().createStoreContextToUser(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static java.nio.file.Path toUserContext(User user, String path) {
        Path rootNormalized = Paths.get(rootPath.normalize().toString(), user.getId(), filesStorePath).normalize();
        Path pathNormalized = Paths.get(path).normalize();
        return Paths.get(
                rootNormalized.toString(),
                Paths.get("/", pathNormalized.toString()).normalize().toString()
        ).normalize();
    }

    public static java.nio.file.Path toUserContext(String idUser, String path) {
        DataUser u = new DataUser();
        u.setId(idUser);
        return toUserContext(u, path);
    }

}
