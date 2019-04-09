package kz.ya.mt.api;

import io.restassured.RestAssured;
import java.math.BigDecimal;
import kz.ya.mt.api.dao.AccountDao;
import kz.ya.mt.api.model.Account;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author yerlan.akhmetov
 */
public class RestApiIntegrationTest {

    @BeforeClass
    public static void setUp() {
        configureHost();
        configurePort();
        configureBasePath();
        startServer();
    }

    private static void configureHost() {
        String baseHost = System.getProperty("server.host");
        if (baseHost == null) {
            baseHost = "http://localhost";
        }
        RestAssured.baseURI = baseHost;
    }

    private static void configurePort() {
        String port = System.getProperty("server.port");
        if (port == null) {
            RestAssured.port = Integer.parseInt("8000");
        } else {
            RestAssured.port = Integer.parseInt(port);
        }
    }

    private static void configureBasePath() {
        String basePath = System.getProperty("server.base");
        if (basePath == null) {
            basePath = "/";
        }
        RestAssured.basePath = basePath;
    }

    private static void startServer() {
        Application.main(new String[]{});
    }

    @Test
    public void shouldRespondWithForbiddenStatus() {
        RestAssured.given().when().get("/").then().statusCode(403);
    }

    @Test
    public void shouldInvokeHealthCheck() {
        RestAssured.given().when().get("/health").then().statusCode(200);
    }
    
    @Test
    public void shouldGetNotFoundStatusForInvalidEndpoint() {
        RestAssured.get("/invalid").then().statusCode(404);
    }

    @Test
    public void shouldTransfer() {
        final Account acc1 = AccountDao.getInstance().create(new BigDecimal(100));
        final Account acc2 = AccountDao.getInstance().create(new BigDecimal(50));

        RestAssured.given()
                .formParam("fromAccountNo", acc1.getNumber())
                .and().formParam("toAccountNo", acc2.getNumber())
                .and().formParam("amount", "10.00")
                .when().post("/transfer")
                .then().statusCode(200);
    }

    @Test
    public void shouldTryToTransferForInvalidAccounts() {
        RestAssured.given()
                .formParam("fromAccountNo", "senderNo")
                .and().formParam("toAccountNo", "receiverNo")
                .and().formParam("amount", "10.00")
                .when().post("/transfer")
                .then().statusCode(404);
    }
    
    @Test
    public void shouldFailToTransferFromZeroBalanceAccount() {
        final Account acc1 = AccountDao.getInstance().create(new BigDecimal(0));
        final Account acc2 = AccountDao.getInstance().create(new BigDecimal(100));
        
        RestAssured.given()
                .formParam("fromAccountNo", acc1.getNumber())
                .formParam("toAccountNo", acc2.getNumber())
                .and().formParam("amount", "60")
                .when().post("/transfer")
                .then().statusCode(400);
    }
    
    @Test
    public void shouldFailToTransferToTheSameAccount() {
        final Account acc = AccountDao.getInstance().create(new BigDecimal(100));
        
        RestAssured.given()
                .formParam("fromAccountNo", acc.getNumber())
                .formParam("toAccountNo", acc.getNumber())
                .and().formParam("amount", "65")
                .when().post("/transfer")
                .then().statusCode(406);
    }
    
    @Test
    public void shouldGetBadRequestStatusForNullSender() {
        RestAssured.given()
                .formParam("toAccountNo", "receiverNo")
                .and().formParam("amount", "10.00")
                .when().post("/transfer")
                .then().statusCode(400);
    }
    
    @Test
    public void shouldGetBadRequestStatusForNullReceiver() {
        final Account acc = AccountDao.getInstance().create(new BigDecimal(20));
        
        RestAssured.given()
                .formParam("fromAccountNo", acc.getNumber())
                .and().formParam("amount", "10.00")
                .when().post("/transfer")
                .then().statusCode(400);
    }
    
    @Test
    public void shouldGetBadRequestStatusForNullAmount() {
        final Account acc1 = AccountDao.getInstance().create(new BigDecimal(200));
        final Account acc2 = AccountDao.getInstance().create(new BigDecimal(0));
        
        RestAssured.given()
                .formParam("fromAccountNo", acc1.getNumber())
                .formParam("toAccountNo", acc2.getNumber())
                .when().post("/transfer")
                .then().statusCode(400);
    }
    
    @Test
    public void shouldGetNotAcceptableStatusForNegativeAmount() {
        final Account acc1 = AccountDao.getInstance().create(new BigDecimal(200));
        final Account acc2 = AccountDao.getInstance().create(new BigDecimal(0));
        
        RestAssured.given()
                .formParam("fromAccountNo", acc1.getNumber())
                .formParam("toAccountNo", acc2.getNumber())
                .and().formParam("amount", "-10")
                .when().post("/transfer")
                .then().statusCode(406);
    }
    
    @Test
    public void shouldGetNotAcceptableStatusForZeroAmount() {
        final Account acc1 = AccountDao.getInstance().create(new BigDecimal(200));
        final Account acc2 = AccountDao.getInstance().create(new BigDecimal(0));
        
        RestAssured.given()
                .formParam("fromAccountNo", acc1.getNumber())
                .formParam("toAccountNo", acc2.getNumber())
                .and().formParam("amount", "0")
                .when().post("/transfer")
                .then().statusCode(406);
    }
}
