package com.example.saucedemo;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

final class Pages {
    private Pages() {}

    static final class Login {
        private final SelenideElement username = $byTestId("username");
        private final SelenideElement password = $byTestId("password");
        private final SelenideElement submit = $byTestId("login-button");
        private final SelenideElement error = $byTestId("error");

        void open() {
            com.codeborne.selenide.Selenide.open("/");
        }

        void login(String user, String pass) {
            username.setValue(user);
            password.setValue(pass);
            submit.click();
        }

        void shouldShowCredentialError() {
            error.shouldBe(visible)
                    .shouldHave(com.codeborne.selenide.Condition.text(
                            "Username and password do not match any user in this service"));
        }
    }

    static final class Inventory {
        private final SelenideElement title = $("[data-test='title']");
        private final ElementsCollection products = $$("[data-test='inventory-item']");
        private final SelenideElement sort = $byTestId("product-sort-container");
        private final SelenideElement cartLink = $("[data-test='shopping-cart-link']");

        void shouldBeLoaded() {
            title.shouldBe(visible).shouldHave(com.codeborne.selenide.Condition.text("Products"));
            products.shouldHave(com.codeborne.selenide.CollectionCondition.sizeGreaterThan(0));
        }

        java.util.List<String> productNames() {
            return $$("[data-test='inventory-item-name']").texts();
        }

        void sortByNameDescending() {
            sort.selectOption("Name (Z to A)");
        }

        void addBackpackToCart() {
            $byTestId("add-to-cart-sauce-labs-backpack").click();
        }

        void openCart() {
            cartLink.click();
        }
    }

    static final class Cart {
        private final ElementsCollection items = $$("[data-test='inventory-item']");

        void shouldContain(String productName) {
            items.findBy(com.codeborne.selenide.Condition.text(productName)).shouldBe(visible);
        }
    }

    private static SelenideElement $byTestId(String id) {
        return $("[data-test='" + id + "']");
    }
}
