package vertx.casestudy;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class FetcherVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(this.getClass());



    @Override
    public Completable rxStart() {
        vertx.setPeriodic(
            5000,
            t -> this.fetchHeadlines()
                     .flatMapObservable(Observable::fromIterable)
                     .map(headline -> {
                         vertx.eventBus().send("headline.create", headline);
                         return headline;
                     })
                     .toList()
                     .subscribe(
                         hl -> log.info("Created all headlines"),
                         error -> log.error("Failed to create all headlines", error)
                     )
        );

        return Completable.complete();
    }



    public Single<JsonArray> fetchHeadlines() {
        return Single.just(
            new JsonArray()
                .add(
                    new JsonObject()
                        .put("author", "Mia Mustermann")
                        .put("source", "bild.de")
                        .put("title", "Corona ist vorbei")
                        .put("description", "Wir haben es geschafft!")
                        .put("publishedAt", OffsetDateTime.of(2020, 12, 10, 1, 14, 0, 0, ZoneOffset.UTC).toString())
                )
        );
    }
}