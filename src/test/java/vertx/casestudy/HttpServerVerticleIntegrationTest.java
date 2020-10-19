package vertx.casestudy;

import com.google.inject.Guice;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class HttpServerVerticleIntegrationTest {

    private EventBus eventBus;

    private RequestSpecification requestSpecification;



    @BeforeEach
    void prepare(Vertx vertx, VertxTestContext ctx) {
        final var injector = Guice.createInjector(new GuiceModule(vertx));

        final var config = injector.getInstance(JsonObject.class);

        this.eventBus = vertx.eventBus();

        this.requestSpecification = new RequestSpecBuilder()
                                        .addFilters(asList(
                                            new ResponseLoggingFilter(),
                                            new RequestLoggingFilter()
                                        ))
                                        .setBaseUri("http://localhost:" + config.getInteger("port"))
                                        .setBasePath("")
                                        .build();

        vertx.rxDeployVerticle(injector.getInstance(HttpServerVerticle.class))
             .subscribe(
                 id -> ctx.completeNow(),
                 ctx::failNow
             );
    }



    @Test
    void getHeadlines(VertxTestContext ctx) {
        final var sendsMessageCheck = ctx.checkpoint();
        final var responseCheck = ctx.checkpoint();

        final var testHeadlines = new JsonArray().add(new JsonObject().put("test", "test"));

        this.eventBus.consumer("headline.getAll", message -> {
            sendsMessageCheck.flag();
            message.reply(new JsonObject().put("result", testHeadlines));
        });

        final var body = given(this.requestSpecification)
                             .get("/headlines")
                             .then()
                             .assertThat()
                             .statusCode(200)
                             .contentType("application/json")
                             .extract()
                             .body()
                             .asString();

        final var bodyAsJson = new JsonArray(body);

        assertThat(bodyAsJson).isEqualTo(testHeadlines);
        responseCheck.flag();
    }
}
