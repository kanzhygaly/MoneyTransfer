package kz.ya.mt.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.ForbiddenResponse;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.json.JavalinJson;
import kz.ya.mt.api.controller.TransferController;
import kz.ya.mt.api.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final int PORT = 8000;

    public static void main(String[] args) {
        final TransferController transferController = new TransferController();

        final Gson gson = new GsonBuilder().create();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);

        Javalin app = Javalin.create()
                .port(PORT)
                .start();

        app.get("/", ctx -> {
            throw new ForbiddenResponse();
        });

        app.get("/health", ctx ->
                ctx.status(200) // OK
        );

        app.routes(() -> {
            ApiBuilder.path("/transaction", () -> {
                ApiBuilder.post(transferController::process);
            });
        });

        app.exception(Exception.class, (exception, ctx) -> {
            ctx.status(500); // Internal Server Error
            LOGGER.error("Error occurred: ", exception);
        });

        app.exception(RuntimeException.class, (exception, ctx) -> {
            if (exception instanceof AccountNotFoundException) {
                ctx.status(404); // NOT FOUND
            } else if (exception instanceof NotEnoughFundsException) {
                ctx.status(400); // BAD REQUEST
            } else if (exception instanceof TransferNegativeAmountException ||
                    exception instanceof TransferZeroAmountException ||
                    exception instanceof TransferToTheSameAccountException) {
                ctx.status(406); // Not Acceptable
            }
            LOGGER.error("Exception: ", exception);
        });
    }
}
