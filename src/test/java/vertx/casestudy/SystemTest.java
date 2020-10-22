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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(VertxExtension.class)
public class SystemTest {

    private static final JsonObject USER = new JsonObject().put("email", "test@test.de").put("password", "secret");

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

        final var pgClient = injector.getInstance(PgPool.class);

        pgClient.query("TRUNCATE headline RESTART IDENTITY")
                .rxExecute()
                .flatMap(
                    rs -> pgClient.preparedQuery("INSERT INTO \"user\" (email, password) VALUES ($1, $2)")
                                  .rxExecute(Tuple.of(USER.getString("email"), USER.getString("password")))
                )
                .flatMap(rs -> vertx.rxDeployVerticle(injector.getInstance(HttpServerVerticle.class)))
                .subscribe(
                    did -> ctx.completeNow(),
                    ctx::failNow
                );
    }



    @Test
    void getHeadlinesIsEmpty() {
        given(this.requestSpecification)
            .get("/headlines")
            .then()
            .assertThat()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(equalTo(new JsonArray().encode()));
    }



    @Test
    void createHeadline() {
        final var headline = new JsonObject()
            .put("author", "Max Mustermann")
            .put("source", "sz.de")
            .put("title", "Trump verliert US Wahl")
            .put("description", "Die Republikaner weinen, die Welt lacht")
            .put("publishedAt", OffsetDateTime.of(2020, 11, 10, 8, 20, 0, 0, ZoneOffset.UTC).toString());

        final var token = given(this.requestSpecification)
            .contentType(ContentType.JSON)
            .body(USER.encode())
            .post("/login")
            .then()
            .assertThat()
            .statusCode(200)
            .contentType("application/jwt")
            .extract()
            .body()
            .asString();

        final var body = given(this.requestSpecification)
            .body(headline.encode())
            .header("Authorization", "Bearer " + token)
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
