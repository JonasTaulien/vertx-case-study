package vertx.casestudy;

import com.google.inject.Guice;
import io.vertx.reactivex.core.Vertx;

public class Main {

    public static void main(String[] args) {
        final var vertx = Vertx.vertx();

        final var injector = Guice.createInjector(new GuiceModule(vertx));

        final var caseStudy = injector.getInstance(CaseStudy.class);

        caseStudy.start();
    }
}
