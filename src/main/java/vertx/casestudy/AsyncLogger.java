package vertx.casestudy;

import com.google.inject.Inject;
import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncLogger {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Vertx vertx;



    @Inject
    public AsyncLogger(Vertx vertx) {
        this.vertx = vertx;
    }



    public void info(String message, Object... params) {
        this.runAsync(
            () -> log.info(message, params)
        );
    }



    public void error(String message, Throwable t) {
        this.runAsync(
            () -> log.error(message, t)
        );
    }



    private void runAsync(Runnable action) {
        this.vertx
            .rxExecuteBlocking(
                promise -> {
                    action.run();
                    promise.complete();
                }
            )
            .subscribe();
    }
}
