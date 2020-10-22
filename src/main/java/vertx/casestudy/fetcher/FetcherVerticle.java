package vertx.casestudy.fetcher;

import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import vertx.casestudy.AsyncLogger;

public class FetcherVerticle extends AbstractVerticle {

    private final AsyncLogger asyncLogger;



    @Inject
    public FetcherVerticle(AsyncLogger asyncLogger) {
        this.asyncLogger = asyncLogger;
    }



    @Override
    public Completable rxStart() {
        final var webClient = WebClient.create(
            vertx,
            new WebClientOptions()
                .setDefaultHost("newsapi.org")
                .setDefaultPort(80)
        );

        this.vertx.setPeriodic(5000, id -> fetchAndCreateHeadlines(webClient));

        return Completable.complete();
    }



    private void fetchAndCreateHeadlines(WebClient webClient) {
        webClient.get("/v2/top-headlines")
                 .as(BodyCodec.jsonObject())
                 .putHeader("X-Api-Key", "c0e401d649db42da8b2e7d13bd62f141")
                 .addQueryParam("country", "de")
                 .addQueryParam("category", "science")
                 .addQueryParam("pageSize", String.valueOf(10))
                 .rxSend()
                 .map(response -> response.body().getJsonArray("articles"))
                 .flatMapObservable(Observable::fromIterable)
                 .map(article -> (JsonObject) article)
                 .map(
                     article -> new JsonObject()
                         .put("source", article.getJsonObject("source").getString("name"))
                         .put("author", article.getString("author"))
                         .put("title", article.getString("title"))
                         .put("description", article.getString("description"))
                         .put("publishedAt", article.getString("publishedAt"))
                 )
                 .subscribe(
                     headline -> vertx.eventBus().send("headline.create", headline),
                     error -> this.asyncLogger.error("Failed to send headlines", error),
                     () -> this.asyncLogger.info("Send headlines")
                 );
    }
}
