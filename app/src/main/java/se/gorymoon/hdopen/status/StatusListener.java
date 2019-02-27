package se.gorymoon.hdopen.status;

public interface StatusListener {
    void accept(Status status, String update);
}
