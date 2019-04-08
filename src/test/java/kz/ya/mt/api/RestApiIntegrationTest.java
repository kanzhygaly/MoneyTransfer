/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.ya.mt.api;

import java.math.BigDecimal;
import kz.ya.mt.api.dao.AccountDao;
import kz.ya.mt.api.model.Account;

/**
 *
 * @author yerlana
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
        given().when().get("/").then().statusCode(403);
    }

    @Test
    public void shouldInvokeHealthCheck() {
        given().when().get("/health").then().statusCode(200);
    }

    @Test
    public void shouldCommitTransaction() {
        final Account acc1 = AccountDao.getInstance().create(new BigDecimal(100));
        final Account acc2 = AccountDao.getInstance().create(new BigDecimal(50));

        given().param("fromAccountNo", acc1.getNumber())
                .and().param("toAccountNo", acc2.getNumber())
                .and().param("amount", "10.00")
                .when().post("/transfer")
                .then().statusCode(200);
    }

    @Test
    public void shouldTryToCommitTransactionForInvalidAccounts() {
        given().param("fromAccountNo", "senderNo")
                .and().param("toAccountNo", "receiverNo")
                .and().param("money", "10.00")
                .when().post("/transfer")
                .then().statusCode(404);
    }

    @Test
    public void shouldGetNotFoundStatusForInvalidEndpoint() {
        get("/invalid").then().statusCode(404);
    }
}
