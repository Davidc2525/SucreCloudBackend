package orchi.HHCloud.user.avatar.Exceptions;

import java.io.IOException;

public class GetAvatarException extends Throwable {
    public GetAvatarException(String e) {
        super(e);
    }
    public GetAvatarException(Exception e) {
        super(e);
    }
}
