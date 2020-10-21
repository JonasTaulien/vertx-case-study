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
        this.vertx
            .rxExecuteBlocking(
                promise -> {
                    log.info(message, params);
                    promise.complete();
                }
            )
            .subscribe();
    }
}
