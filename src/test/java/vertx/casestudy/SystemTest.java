package vertx.casestudy;

import com.google.inject.Guice;
import io.netty.handler.codec.http.HttpHeaderNames;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import vertx.casestudy.data.DataVerticle;
import vertx.casestudy.http.HttpServerVerticle;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(VertxExtension.class)
public class SystemTest {

    private RequestSpecification requestSpecification;

    private final JsonObject user = new JsonObject()
        .put("email", "test@test.de")
        .put("password", "secret");



    @BeforeEach
    void prepare(Vertx vertx, VertxTestContext ctx) {
        this.requestSpecification = new RequestSpecBuilder()
            .addFilters(Arrays.asList(new ResponseLoggingFilter(), new RequestLoggingFilter()))
            .setBasePath("http:/localhost:8080")
            .setBasePath("/api/v1")
            .build();

        final var injector = Guice.createInjector(new Module(vertx));

        final var pgPool = injector.getInstance(PgPool.class);

        pgPool.query("TRUNCATE headline RESTART IDENTITY; TRUNCATE \"user\" RESTART IDENTITY;")
              .rxExecute()
              .flatMap(
                  rowSet -> pgPool.preparedQuery("INSERT INTO \"user\" (email, password) VALUES ($1, $2)")
                                  .rxExecute(Tuple.of(user.getString("email"), user.getString("password")))
              )
              .flatMap(ros -> vertx.rxDeployVerticle(injector.getInstance(HttpServerVerticle.class)))
              .flatMap(did -> vertx.rxDeployVerticle(injector.getInstance(DataVerticle.class)))
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
            .header(HttpHeaderNames.AUTHORIZATION.toString(), "Bearer " + getJwt())
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



    @Test
    void login() {
        final var jwt = getJwt();

        final var decoder = Base64.getDecoder();

        final String[] parts = jwt.split("\\.");
        final var header = new JsonObject(new String(decoder.decode(parts[0])));
        final var payload = new JsonObject(new String(decoder.decode(parts[1])));

        assertThat(header.getString("typ")).isEqualTo("JWT");
        assertThat(payload.getString("sub")).isEqualTo("1");
    }



    private String getJwt() {
        return given(this.requestSpecification)
            .contentType(ContentType.JSON)
            .body(user.encode())
            .post("/login")
            .then()
            .assertThat()
            .statusCode(200)
            .contentType("application/jwt")
            .extract()
            .body()
            .asString();
    }
}
