package orchi.HHCloud.user.avatar;

import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.avatar.Exceptions.DeleteAvatarException;
import orchi.HHCloud.user.avatar.Exceptions.GetAvatarException;
import orchi.HHCloud.user.avatar.Exceptions.SetAvatarException;

import java.io.InputStream;

public interface AvatarProvider {

    Avatar set(User user, InputStream image) throws SetAvatarException;

    Avatar get(User user) throws  GetAvatarException;

    Avatar get(User user, Bound size) throws GetAvatarException;

    void delete(User user) throws DeleteAvatarException;

    Boolean isSet(User user) throws Exception;

    public AvatarDescriptor getADescriptor(User user) throws Exception;
}
