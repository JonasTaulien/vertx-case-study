package vertx.casestudy;

import com.google.inject.Guice;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
            .addFilters(asList(
                new ResponseLoggingFilter(),
                new RequestLoggingFilter()
            ))
            .setBaseUri("http://localhost:8080")
            .setBasePath("/api/v1")
            .build();

        final var injector = Guice.createInjector(new Module(vertx));

        vertx.rxDeployVerticle(injector.getInstance(MyFirstVerticle.class))
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
