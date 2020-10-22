package vertx.casestudy;

import com.google.inject.Guice;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import vertx.casestudy.http.HttpServerVerticle;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(VertxExtension.class)
public class HttpServerIntegrationTest {

    private static final String TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE2MDMzNzUxODIsImV4cCI6MTkxODczNTE4Miwic3ViIjoiMSJ9.n2BO_gbGNtNO0v-Cp9ddsr91XWgaZ-nl_xrWi3ZDk2_lfMYT4ty8gf40Y0Om2CgMWzb1qnIQjUCx3xtxvqX5LG43_aIdr1erTvWM2_tx9HADDJ3_-fx3GO6OfQ0o3b6SvG8c2BXSS9FkLkyW7xfVPiha29wNc4Z3PSVzfXkghd5UOPJZK9wlbrNGBxzLedBAnMCTeex4MagpDKe-CV-ddbUuo2vO0OynPzd4hbh9O6jGrxzflcYUaJuEsVQfCMHomK-wd2humlSrkAy4vUoIsaXn0Yy6VyTtx7SlTDAm2trINvrmM9PIF9inPtXpBxhTHMXVTMxp2SEhK02QLiznIw";

    private RequestSpecification requestSpecification;



    @BeforeEach
    void prepare(Vertx vertx, VertxTestContext ctx) {
        this.requestSpecification = new RequestSpecBuilder()
            .addFilters(asList(
                new ResponseLoggingFilter(),
                new RequestLoggingFilter()
            ))
            .setBaseUri("http://localhost:8080")
            .setBasePath("/api/v1")
            .build();

        final var injector = Guice.createInjector(new Module(vertx));

        vertx.rxDeployVerticle(injector.getInstance(HttpServerVerticle.class))
             .subscribe(
                 did -> ctx.completeNow(),
                 ctx::failNow
             );
    }



    @Test
    void getHeadlines(Vertx vertx) {
        final var allHeadlines = new JsonArray()
            .add(new JsonObject()
                     .put("author", "Max Mustermann")
                     .put("source", "sz.de")
                     .put("title", "Trump verliert US Wahl")
                     .put("description", "Die Republikaner weinen, die Welt lacht")
                     .put("publishedAt", OffsetDateTime.of(2020, 11, 10, 8, 20, 0, 0, ZoneOffset.UTC).toString())
            );

        vertx.eventBus()
            .<JsonObject>consumer("headline.getAll", msg -> {
                assertThat(msg.body()).isEqualTo(new JsonObject());
                msg.reply(allHeadlines);
            });

        given(this.requestSpecification)
            .get("/headlines")
            .then()
            .assertThat()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(equalTo(allHeadlines.encode()));
    }



    @Test
    void createHeadline(Vertx vertx) {
        final var headline = new JsonObject()
            .put("author", "Max Mustermann")
            .put("source", "sz.de")
            .put("title", "Trump verliert US Wahl")
            .put("description", "Die Republikaner weinen, die Welt lacht")
            .put("publishedAt", OffsetDateTime.of(2020, 11, 10, 8, 20, 0, 0, ZoneOffset.UTC).toString());

        vertx.eventBus()
             .<JsonObject>consumer("headline.create", msg -> {
                 assertThat(msg.body()).isEqualTo(headline);
                 msg.reply(msg.body().put("id", 1));
             });

        final var body = given(this.requestSpecification)
            .body(headline.encode())
            .header("Authorization", "Bearer " + TOKEN)
            .post("/headlines")
            .then()
            .assertThat()
            .statusCode(201)
            .contentType("application/json")
            .extract()
            .body()
            .asString();

        final var bodyJson = new JsonObject(body);

        assertThat(bodyJson).isEqualTo(headline.put("id", 1));
    }



    @Test
    void myTest(Vertx vertx, VertxTestContext ctx) {
        vertx
            .rxExecuteBlocking(
                promise -> {
                    assertThat(true).isTrue();
                    promise.complete();
                }
            )
            .subscribe(
                res -> ctx.completeNow(),
                ctx::failNow,
                ctx::completeNow
            );
    }
}
