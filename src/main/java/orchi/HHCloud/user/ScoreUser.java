package orchi.HHCloud.user;

public class ScoreUser extends DataUser {
    private Float score;


    public User bind(String id, String username, String email, boolean emailVerified, String password, String firstName, String lastName, Long createAt,Float score) {
        this.score = score;
        return super.bind(id, username, email, emailVerified, password, firstName, lastName, createAt);

    }

    public Float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }


    @Override
    public String toString() {
        return "ScoreUser{" +
                "score=" + score +
                ", id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
