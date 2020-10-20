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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(VertxExtension.class)
public class SystemTest {

    private RequestSpecification requestSpecification;



    @BeforeEach
    void prepare(Vertx vertx, VertxTestContext ctx) {
        this.requestSpecification = new RequestSpecBuilder()
            .addFilters(Arrays.asList(new ResponseLoggingFilter(), new RequestLoggingFilter()))
            .setBasePath("http:/localhost:8080")
            .setBasePath("/api/v1")
            .build();

        final var injector = Guice.createInjector(new Module(vertx));

        final var pgPool = injector.getInstance(PgPool.class);

        pgPool.query("TRUNCATE headline RESTART IDENTITY")
              .rxExecute()
              .flatMap(ros -> vertx.rxDeployVerticle(injector.getInstance(HttpServerVerticle.class)))
              .subscribe(
                  deploymentId -> ctx.completeNow(),
                  ctx::failNow
              );
    }



    @Test
    void headlinesInitiallyEmpty() {
        given(requestSpecification)
            .get("/headlines")
            .then()
            .assertThat()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(equalTo(new JsonArray().encode()));
    }



    @Test
    void getHeadlines() {
        final var headline = new JsonObject()
            .put("author", "Max Mustermann")
            .put("source", "sz.de")
            .put("title", "Trump verliert US Wahl")
            .put("description", "Eine Beschreibung")
            .put("publishedAt", OffsetDateTime.of(2020, 11, 10, 8, 20, 0, 0, ZoneOffset.UTC).toString());

        given(requestSpecification)
            .body(headline.encode())
            .post("/headlines")
            .then()
            .assertThat()
            .statusCode(201);

        final var bodyAsString = given(requestSpecification)
            .get("/headlines")
            .then()
            .assertThat()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .body()
            .asString();

        final var body = new JsonArray(bodyAsString);

        assertThat(body).isEqualTo(new JsonArray().add(headline.put("id", 1)));
    }
}
