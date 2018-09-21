package orchi.HHCloud.user.userAvailable;


import orchi.HHCloud.Start;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.userAvailable.Exceptions.DisablingException;
import orchi.HHCloud.user.userAvailable.Exceptions.EnablingException;

public interface UserAvailableProvider {
    static boolean lazy = Start.conf.getBoolean("user.usermanager.provider.user.available.lazy");
    public void init();

    public void disableUser(User user, String reason) throws DisablingException;

    public void enableUser(User user) throws EnablingException;

    public boolean userIsEnable(User user) throws UserException;

    public AvailableDescriptor getDescriptor(User user) throws  UserException;
}
