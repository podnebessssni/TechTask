package com.example.saucedemo;

import com.codeborne.selenide.Configuration;
import org.openqa.selenium.chrome.ChromeOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("ui")
class SauceDemoTest {
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";
    private final Pages.Login loginPage = new Pages.Login();
    private final Pages.Inventory inventoryPage = new Pages.Inventory();

    @BeforeAll
    static void configureSelenide() {
        Configuration.baseUrl = System.getProperty("baseUrl", "https://www.saucedemo.com");
        Configuration.browser = "chrome";
        Configuration.headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        Configuration.timeout = 10_000;
        Configuration.browserSize = "1440x1000";

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        java.nio.file.Path chromium = java.nio.file.Path.of("/usr/bin/chromium");
        if (java.nio.file.Files.isExecutable(chromium)) {
            chromeOptions.setBinary(chromium.toString());
        }
        Configuration.browserCapabilities = chromeOptions;

        String remote = System.getProperty("selenide.remote", System.getenv("SELENIDE_REMOTE"));
        if (remote != null && !remote.isBlank()) {
            Configuration.remote = remote;
        }
    }

    @AfterEach
    void closeBrowser() {
        closeWebDriver();
    }

    @Test
    void userCanLogInAndSeeProducts() {
        loginPage.open();
        loginPage.login(USERNAME, PASSWORD);

        inventoryPage.shouldBeLoaded();
    }

    @Test
    void userCanSortProductsAndAddOneToCart() {
        loginPage.open();
        loginPage.login(USERNAME, PASSWORD);
        inventoryPage.shouldBeLoaded();

        inventoryPage.sortByNameDescending();
        List<String> actualNames = inventoryPage.productNames();
        List<String> expectedNames = new ArrayList<>(actualNames);
        expectedNames.sort(Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER));
        assertEquals(expectedNames, actualNames, "Products should be sorted from Z to A");

        inventoryPage.addBackpackToCart();
        inventoryPage.openCart();
        new Pages.Cart().shouldContain("Sauce Labs Backpack");
    }

    @ParameterizedTest
    @CsvSource({
            "standard_user,wrong_password",
            "unknown_user,secret_sauce"
    })
    void invalidCredentialsShowAnError(String username, String password) {
        loginPage.open();
        loginPage.login(username, password);

        loginPage.shouldShowCredentialError();
    }
}
