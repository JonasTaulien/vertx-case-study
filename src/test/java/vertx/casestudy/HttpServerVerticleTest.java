package vertx.casestudy;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@ExtendWith(VertxExtension.class)
public class HttpServerVerticleTest {

    private static final RequestSpecification requestSpecification
        = new RequestSpecBuilder()
              .addFilters(asList(new ResponseLoggingFilter(), new RequestLoggingFilter()))
              .setBaseUri("http://localhost:8080/")
              .setBasePath("")
              .build();

    private static final List<JsonObject> headlines = asList(
        new JsonObject()
            .put("author", "Max Mustermann")
            .put("source", "sz.de")
            .put("title", "Trump verliert US Wahl")
            .put("description", "Die Republikaner weinen, die Welt lacht")
            .put("publishedAt", OffsetDateTime.of(2020, 11, 10, 8, 20, 0, 0, ZoneOffset.UTC).toString()),

        new JsonObject()
            .put("author", "Mia Mustermann")
            .put("source", "bild.de")
            .put("title", "Corona ist vorbei")
            .put("description", "Wir haben es geschafft!")
            .put("publishedAt", OffsetDateTime.of(2020, 12, 10, 1, 14, 0, 0, ZoneOffset.UTC).toString())
    );



    @BeforeEach
    void prepare(Vertx vertx, VertxTestContext ctx) {
        final var pgPool = PgPool.pool(
            vertx,
            new PgConnectOptions()
                .setHost("localhost")
                .setDatabase("case-study")
                .setUser("example")
                .setPassword("example"),
            new PoolOptions()
        );

        pgPool.preparedQuery("TRUNCATE headline RESTART IDENTITY;")
              .execute(ctx.succeeding(
                  ar -> vertx.deployVerticle(
                      new HttpServerVerticle(vertx),
                      ctx.succeeding(id -> ctx.completeNow())
                  )
              ));
    }



    @Test
    void headlinesInitiallyEmpty() {
        given(HttpServerVerticleTest.requestSpecification)
            .get("/headlines")
            .then()
            .assertThat()
            .statusCode(200)
            .contentType("application/json")
            .body(equalTo(new JsonArray().encode()));
    }



    @Test
    void creatingHeadline() {
        final var headline = HttpServerVerticleTest.headlines.get(0);

        final var body = given(HttpServerVerticleTest.requestSpecification)
                             .contentType(ContentType.JSON)
                             .body(headline.encode())
                             .post("/headline")
                             .then()
                             .assertThat()
                             .statusCode(201)
                             .contentType("application/json")
                             .extract()
                             .body();

        final var bodyAsJson = new JsonObject(body.asString());

        assertThat(bodyAsJson).isEqualTo(headline.copy().put("id", 1));
    }



    @Test
    void getHeadlines() {
        HttpServerVerticleTest.headlines.forEach(
            headline -> given(HttpServerVerticleTest.requestSpecification)
                            .contentType(ContentType.JSON)
                            .body(headline.encode())
                            .post("/headline")
                            .then()
                            .assertThat()
                            .statusCode(201)
        );

        final var body = given(HttpServerVerticleTest.requestSpecification)
                             .get("/headlines")
                             .then()
                             .assertThat()
                             .statusCode(200)
                             .contentType("application/json")
                             .extract()
                             .body();

        final var bodyAsJson = new JsonArray(body.asString());

        assertThat(bodyAsJson.size()).isEqualTo(HttpServerVerticleTest.headlines.size());
    }



    @Test
    void getOneHeadline() {
        final var headline = HttpServerVerticleTest.headlines.get(0);

        final var idOfCreatedHeadline
            = given(HttpServerVerticleTest.requestSpecification)
                  .contentType(ContentType.JSON)
                  .body(headline.encode())
                  .post("/headline")
                  .then()
                  .assertThat()
                  .statusCode(201)
                  .extract()
                  .body()
                  .jsonPath()
                  .getInt("id");

        final var body = given(HttpServerVerticleTest.requestSpecification)
                             .get("/headline/" + idOfCreatedHeadline)
                             .then()
                             .assertThat()
                             .statusCode(200)
                             .contentType("application/json")
                             .extract()
                             .body();

        final var bodyAsJson = new JsonObject(body.asString());

        assertThat(bodyAsJson).isEqualTo(headline.copy().put("id", 1));
    }

    @Test
    void cors(){
        given(HttpServerVerticleTest.requestSpecification)
            .header("Origin", "localhost")
            .options("/headlines")
            .then()
            .assertThat()
            .statusCode(405)
            .header("Access-Control-Allow-Origin", equalTo("*"));
    }
}
