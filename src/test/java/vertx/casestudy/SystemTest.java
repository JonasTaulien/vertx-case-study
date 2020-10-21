package vertx.casestudy;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class SystemTest {

    @BeforeEach
    void prepare(Vertx vertx, VertxTestContext ctx) {
        vertx.deployVerticle(
            new MyFirstVerticle(asyncLogger),
            ctx.succeeding(id -> ctx.completeNow())
        );
    }



    @Test
    void test(VertxTestContext ctx) {
        assertThat(true).isTrue();
        ctx.completeNow();
    }
}
