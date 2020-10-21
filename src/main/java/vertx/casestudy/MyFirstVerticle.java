package vertx.casestudy;

import com.google.inject.Inject;
import io.reactivex.Completable;
import io.vertx.core.Promise;
import io.vertx.reactivex.core.AbstractVerticle;

public class MyFirstVerticle extends AbstractVerticle {

    private final AsyncLogger asyncLogger;



    @Inject
    public MyFirstVerticle(AsyncLogger asyncLogger) {
        this.asyncLogger = asyncLogger;
    }



    @Override
    public Completable rxStart() {
        this.asyncLogger.info("Succesfully started my first verticle");

        return Completable.complete();
    }
}
