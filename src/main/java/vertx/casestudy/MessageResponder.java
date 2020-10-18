package vertx.casestudy;

import io.reactivex.functions.Consumer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageResponder {

    private final Logger log = LoggerFactory.getLogger(this.getClass());



    public Consumer<Throwable> replyWithError(Message<JsonObject> message) {
        return error -> replyWithError(message, error);
    }



    public Consumer<Throwable> replyWithError(Message<JsonObject> message, String type) {
        return error -> replyWithError(message, type, error);
    }



    public void replyWithError(Message<JsonObject> message, Throwable error) {
        this.replyWithError(message, "UNKNOWN", error);
    }



    public void replyWithError(Message<JsonObject> message, String type, Throwable error) {
        log.error("Error while handling message", error);

        message.reply(
            new JsonObject().put("error", new JsonObject()
                                              .put("message", error.getMessage())
                                              .put("type", type)),
            new DeliveryOptions().addHeader("FAILED", "FAILED")
        );
    }
}
