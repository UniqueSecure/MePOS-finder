package uniquesecure.com.meposfinder.persistence;


public interface MePOSFinderCallback {
    void onMePOSFinderStarted ();

    void onMePOSFinderProgress(int progress);

    void onMePOSFound(MePOSDevice device);

    void onMePOSFinderCompleted();

    void onMePOSFinderError(Exception e);
}
