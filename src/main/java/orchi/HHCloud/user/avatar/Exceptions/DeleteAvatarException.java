package orchi.HHCloud.user.avatar.Exceptions;

import orchi.HHCloud.user.Exceptions.UserException;

public class DeleteAvatarException extends AvatarException {
    public DeleteAvatarException(Exception e) {
        super(e);
    }

    public DeleteAvatarException(String e) {
        super(e);
    }
}
