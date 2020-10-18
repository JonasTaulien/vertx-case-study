package vertx.casestudy;

import com.google.inject.Inject;
import io.vertx.core.DeploymentOptions;
import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaseStudy {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Vertx vertx;

    private final HttpServerVerticle httpServerVerticle;



    @Inject
    public CaseStudy(Vertx vertx, HttpServerVerticle httpServerVerticle) {
        this.vertx = vertx;
        this.httpServerVerticle = httpServerVerticle;
    }



    public void start() {
        vertx.rxDeployVerticle(httpServerVerticle, new DeploymentOptions())
             .subscribe(
                 id -> log.info("Successfully started server"),
                 err -> log.info("Failed to start server")
             );
    }
}
