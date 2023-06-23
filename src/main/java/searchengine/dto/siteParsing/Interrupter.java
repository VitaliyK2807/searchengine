package searchengine.dto.siteParsing;

public class Interrupter implements Runnable{
    private Thread thread;

    public Interrupter (Thread thread) {
        this.thread = thread;
    }

    @Override
    public void run() {
        thread.interrupt();
    }
}
