package orchi.HHCloud.user;

import orchi.HHCloud.Start;
import orchi.HHCloud.user.avatar.AvatarProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AvatarUser extends ScoreUser {

    private static AvatarProvider ap = Start.getUserManager().getUserProvider().getAvatarProvider();
    private static List<String> bounds = Arrays.asList("50x50", "100x100", "290x290");
    public List<String> getAvatars() {
        return avatars;
    }

    public void setAvatars(List<String> avatars) {
        this.avatars = avatars;
    }

    public void addAvatar(String path){
        this.avatars.add(path);
    }

    private List<String> avatars = new ArrayList<>();

    public static AvatarUser build(User user) {
        AvatarUser au = new AvatarUser();
        au.setId(user.getId());
        au.setUsername(user.getUsername());
        au.setPassword(user.getPassword());

        if (user instanceof DataUser) {
            DataUser du = (DataUser) user;
            au.setFirstName(du.getFirstName());
            au.setLastName(du.getLastName());
            au.setEmail(du.getEmail());
            au.setCreateAt(du.getCreateAt());
            au.setEmailVerified(du.isEmailVerified());
            au.setGender(du.getGender());
        }

        if (user instanceof ScoreUser) {
            ScoreUser du = (ScoreUser) user;
            au.setFirstName(du.getFirstName());
            au.setLastName(du.getLastName());
            au.setEmail(du.getEmail());
            au.setCreateAt(du.getCreateAt());
            au.setEmailVerified(du.isEmailVerified());
            au.setGender(du.getGender());
            au.setScore(du.getScore());
        }

        try {
            if (/*ap.isSet(au)*/true) {
                String protocol = Start.conf.getString("api.protocol");
                String host = Start.conf.getString("api.host");
                String port = Start.conf.getString("api.port");

                bounds.forEach((String bound) -> {
                    au.addAvatar(String.format("%s://%s:%s/api/avatar?id=%s&size=%s", protocol, host, port, au.getId(),bound));
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return au;
    }
}
