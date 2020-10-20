package vertx.casestudy;

import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class HeadlineFetcherVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final WebClient newsApiClient;

    private final ConfigRetriever configRetriever;



    @Inject
    public HeadlineFetcherVerticle(WebClient newsApiClient, ConfigRetriever configRetriever) {
        this.newsApiClient = newsApiClient;
        this.configRetriever = configRetriever;
    }



    @Override
    public Completable rxStart() {
        this.vertx.setPeriodic(TimeUnit.MINUTES.toMillis(1), id -> fetchAndCreateHeadlines());

        return Completable.complete();
    }



    private void fetchAndCreateHeadlines() {
        this.fetchHeadlines()
            .forEach(this::sendHeadline);
    }



    private void sendHeadline(JsonObject headline) {
        this.vertx.eventBus().send("headline.create", headline);
    }



    private Observable<JsonObject> fetchHeadlines() {
        return this.configRetriever
                   .rxGetConfig()
                   .flatMap(
                       config -> {
                           final var newsApiConfig = config.getJsonObject("newsapi");
                           final var category = newsApiConfig.getString("category");

                           log.info("Fetching headlines in category {}", category);

                           return this.newsApiClient
                                      .get("/v2/top-headlines")
                                      .as(BodyCodec.jsonObject())
                                      .putHeader("X-Api-Key", newsApiConfig.getString("apiKey"))
                                      .addQueryParam("country", newsApiConfig.getString("country"))
                                      .addQueryParam("category", category)
                                      .addQueryParam(
                                          "pageSize",
                                          newsApiConfig.getInteger("numberOfHeadlines").toString()
                                      )
                                      .rxSend();
                       }
                   )
                   .flatMapObservable(response -> Observable.fromIterable(response.body().getJsonArray("articles")))
                   .map(article -> (JsonObject) article)
                   .map(
                       article -> new JsonObject()
                                      .put("source", article.getJsonObject("source").getString("name"))
                                      .put("author", article.getString("author"))
                                      .put("title", article.getString("title"))
                                      .put("description", article.getString("description"))
                                      .put("publishedAt", article.getString("publishedAt"))
                   );
    }
}
