package vertx.casestudy;

import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class HeadlineFetcherVerticleUnitTest {

    @Test
    void fetch(Vertx vertx, VertxTestContext ctx) {
        final var serverCheck = ctx.checkpoint();
        final var deploymentCheck = ctx.checkpoint();
        final var requestCheck = ctx.checkpoint();
        final var eventBusCheck = ctx.checkpoint();

        // Set up config
        final var testConfig = new JsonObject().put(
            "newsapi",
            new JsonObject()
                .put("fetchIntervalInSeconds", 1)
                .put("apiKey", "test-api-key")
                .put("country", "test-country")
                .put("category", "test-category")
                .put("numberOfHeadlines", 1)
        );

        final var configRetriever = ConfigRetriever.create(
            vertx,
            new ConfigRetrieverOptions()
                .addStore(
                    new ConfigStoreOptions()
                        .setType("json")
                        .setConfig(testConfig)
                )
                .setScanPeriod(2000)
        );

        final var serverPort = 19822;

        // Set up client
        final var webClient = WebClient.create(
            vertx,
            new WebClientOptions().setDefaultHost("localhost")
                                  .setDefaultPort(serverPort)
        );

        final var headlineSource = "test-name";
        final var headlineAuthor = "test-author";
        final var headlineTitle = "test-title";
        final var headlineDescription = "test-description";
        final var headlinePublishedAt = OffsetDateTime.of(2020, 12, 10, 1, 14, 0, 0, ZoneOffset.UTC).toString();

        final var headlines = new JsonObject().put(
            "articles", new JsonArray().add(
                new JsonObject()
                    .put("source", new JsonObject().put("name", headlineSource))
                    .put("author", headlineAuthor)
                    .put("title", headlineTitle)
                    .put("description", headlineDescription)
                    .put("publishedAt", headlinePublishedAt)
            )
        );

        // Set up server
        vertx.createHttpServer()
             .requestHandler(
                 request -> {
                     assertThat(request.method()).isEqualTo(HttpMethod.GET);
                     // TODO: More assertions
                     request.response().end(headlines.encode());

                     requestCheck.flag();
                 }
             )
             .rxListen(serverPort)
             .subscribe(
                 server -> serverCheck.flag(),
                 ctx::failNow
             );

        // Register event bus
        vertx.eventBus()
            .<JsonObject>consumer("headline.create", message -> {
                assertThat(message.body()).isEqualTo(
                    new JsonObject()
                        .put("source", headlineSource)
                        .put("author", headlineAuthor)
                        .put("title", headlineTitle)
                        .put("description", headlineDescription)
                        .put("publishedAt", headlinePublishedAt)
                );
                eventBusCheck.flag();
            });

        // Deploy fetcher
        vertx.rxDeployVerticle(new HeadlineFetcherVerticle(webClient, configRetriever))
             .subscribe(
                 id -> deploymentCheck.flag(),
                 ctx::failNow
             );
    }
}
