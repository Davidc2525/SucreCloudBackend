package orchi.HHCloud;

public class ProviderNotFoundException extends RuntimeException {
    public ProviderNotFoundException(){
        super();
    }

    public ProviderNotFoundException(String msg) {
        super(msg);
    }

    public ProviderNotFoundException(Exception e) {
        super(e);
    }
}

