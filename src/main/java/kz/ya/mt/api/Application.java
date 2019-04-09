package kz.ya.mt.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.ForbiddenResponse;
import io.javalin.Javalin;
import io.javalin.JavalinEvent;
import io.javalin.json.JavalinJson;
import io.javalin.validation.JavalinValidation;
import java.math.BigDecimal;
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

        final Javalin app = Javalin.create()
                .event(JavalinEvent.SERVER_STARTED, () -> LOGGER.info("Server is started..."))
                .event(JavalinEvent.SERVER_START_FAILED, () -> LOGGER.error("Server start was failed!"))
                .requestLogger((context, executionTimeMs)
                        -> LOGGER.info("{} ms\t {}\t {} {}",
                        executionTimeMs,
                        context.req.getMethod(),
                        context.req.getRequestURI(),
                        context.req.getParameterMap().toString().replaceAll("^.|.$", "")
                ))
                .start(PORT);
        
        // Validation
        // Register a custom converter
        JavalinValidation.register(BigDecimal.class, v -> new BigDecimal(v));

        // Request Handlers
        app.get("/", ctx -> {
            throw new ForbiddenResponse();
        });
        app.get("/health", ctx
                -> ctx.status(200) // OK
        );
        app.post("/transfer", ctx -> transferController.process(ctx));

        // Exception Handlers
        app.exception(Exception.class, (ex, ctx) -> {
            LOGGER.error("Error occurred: ", ex.getMessage());
            ctx.status(500); // SERVER INTERNAL ERROR
        });
        app.exception(AccountNotFoundException.class, (ex, ctx) -> {
            LOGGER.error("Exception: ", ex.getMessage());
            ctx.status(404); // NOT FOUND
        });
        app.exception(NotEnoughFundsException.class, (ex, ctx) -> {
            LOGGER.error("Exception: ", ex.getMessage());
            ctx.status(400); // BAD REQUEST
        });
        app.exception(TransferNegativeAmountException.class, (ex, ctx) -> {
            LOGGER.error("Exception: ", ex.getMessage());
            ctx.status(406); // NOT ACCEPTABLE
        });
        app.exception(TransferZeroAmountException.class, (ex, ctx) -> {
            LOGGER.error("Exception: ", ex.getMessage());
            ctx.status(406); // NOT ACCEPTABLE
        });
        app.exception(TransferToTheSameAccountException.class, (ex, ctx) -> {
            LOGGER.error("Exception: ", ex.getMessage());
            ctx.status(406); // NOT ACCEPTABLE
        });
    }
}
