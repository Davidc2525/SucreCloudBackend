package orchi.HHCloud.quota;

public class Quota {

    private Long _quota = 0L;

    public Quota() {
    }

    public Quota(Long q) {
        _quota = q;
    }

    public Long getQuota() {
        return _quota;
    }

    public void setQuota(Long _quota) {
        this._quota = _quota;
    }
}
