package com.example.restfulbooker;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Tag("api")
class BookingApiTest {
    private static final Map<String, Object> BOOKING = Map.of(
            "firstname", "Morgan",
            "lastname", "Tester",
            "totalprice", 275,
            "depositpaid", true,
            "bookingdates", Map.of(
                    "checkin", "2027-02-10",
                    "checkout", "2027-02-15"
            ),
            "additionalneeds", "Breakfast"
    );

    @BeforeAll
    static void configureRestAssured() {
        RestAssured.baseURI = System.getProperty("apiBaseUrl", "https://restful-booker.herokuapp.com");
    }

    @Test
    void createdBookingCanBeRetrievedWithItsOriginalData() {
        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(BOOKING)
                .when()
                .post("/booking")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        int bookingId = createResponse.jsonPath().getInt("bookingid");
        assertThat("Created booking ID must be positive", bookingId, org.hamcrest.Matchers.greaterThan(0));

        Response retrievedBooking = given()
                // Restful Booker compares this header literally; don't send a comma-separated Accept value.
                .header("Accept", "application/json")
                .when()
                .get("/booking/{id}", bookingId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        assertThat(retrievedBooking.jsonPath().getString("firstname"), equalTo(BOOKING.get("firstname")));
        assertThat(retrievedBooking.jsonPath().getString("lastname"), equalTo(BOOKING.get("lastname")));
        assertThat(retrievedBooking.jsonPath().getInt("totalprice"), equalTo(BOOKING.get("totalprice")));
        assertThat(retrievedBooking.jsonPath().getBoolean("depositpaid"), equalTo(BOOKING.get("depositpaid")));
        assertThat(retrievedBooking.jsonPath().getString("bookingdates.checkin"),
                equalTo("2027-02-10"));
        assertThat(retrievedBooking.jsonPath().getString("bookingdates.checkout"),
                equalTo("2027-02-15"));
        assertThat(retrievedBooking.jsonPath().getString("additionalneeds"),
                equalTo(BOOKING.get("additionalneeds")));
    }

    @Test
    void requestingAnUnknownBookingReturnsNotFound() {
        given()
                .header("Accept", "application/json")
                .when()
                .get("/booking/{id}", 0)
                .then()
                .statusCode(404);
    }
}
