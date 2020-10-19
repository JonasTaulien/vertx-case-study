package vertx.casestudy;

import io.vertx.core.Promise;
import io.vertx.reactivex.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyFirstVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(this.getClass());



    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        log.info("Starting my first verticle");

        startPromise.complete();
    }
}
