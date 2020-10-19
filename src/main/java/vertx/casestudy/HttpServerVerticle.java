package vertx.casestudy;

import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;

public class HttpServerVerticle extends AbstractVerticle {

    @Override
    public Completable rxStart() {
        return Completable.complete();
    }
}
