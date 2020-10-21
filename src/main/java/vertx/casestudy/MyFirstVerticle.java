package vertx.casestudy;

import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Promise;
import io.vertx.reactivex.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyFirstVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AsyncLogger asyncLogger;



    @Inject
    public MyFirstVerticle(AsyncLogger asyncLogger) {
        this.asyncLogger = asyncLogger;
    }



    @Override
    public Completable rxStart() {
        this.asyncLogger.info("Successfully started my first verticle");

        return Completable.complete();
    }
}
