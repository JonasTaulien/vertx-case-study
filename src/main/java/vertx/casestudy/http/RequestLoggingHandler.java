package vertx.casestudy.http;

import com.google.inject.Inject;
import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.RoutingContext;
import vertx.casestudy.AsyncLogger;

public class RequestLoggingHandler implements Handler<RoutingContext> {

    private final AsyncLogger asyncLogger;



    @Inject
    public RequestLoggingHandler(AsyncLogger asyncLogger) {
        this.asyncLogger = asyncLogger;
    }



    @Override
    public void handle(RoutingContext ctx) {
        this.asyncLogger.info(
            "New Request {} {}",
            ctx.request().method(),
            ctx.request().path()
        );

        ctx.next();
    }
}
