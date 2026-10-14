package com.zara.price.adapter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerTest {

    private static final String ZARA = "1";
    private static final String PRODUCT = "35455";

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name = "applicationDate {0} applies price list {1}")
    @CsvSource({
            // Test 1: day 14 at 10:00
            "2020-06-14T10:00:00, 1, 2020-06-14T00:00:00, 2020-12-31T23:59:59, 35.50",
            // Test 2: day 14 at 16:00
            "2020-06-14T16:00:00, 2, 2020-06-14T15:00:00, 2020-06-14T18:30:00, 25.45",
            // Test 3: day 14 at 21:00
            "2020-06-14T21:00:00, 1, 2020-06-14T00:00:00, 2020-12-31T23:59:59, 35.50",
            // Test 4: day 15 at 10:00
            "2020-06-15T10:00:00, 3, 2020-06-15T00:00:00, 2020-06-15T11:00:00, 30.50",
            // Test 5: day 16 at 21:00
            "2020-06-16T21:00:00, 4, 2020-06-15T16:00:00, 2020-12-31T23:59:59, 38.95"
    })
    void appliesTheExpectedPrice(String applicationDate, int priceList,
                                 String startDate, String endDate, double price) throws Exception {
        mockMvc.perform(request(ZARA, PRODUCT, applicationDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brandId").value(Long.parseLong(ZARA)))
                .andExpect(jsonPath("$.productId").value(Long.parseLong(PRODUCT)))
                .andExpect(jsonPath("$.priceList").value(priceList))
                .andExpect(jsonPath("$.startDate").value(startDate))
                .andExpect(jsonPath("$.endDate").value(endDate))
                .andExpect(jsonPath("$.price").value(price))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @ParameterizedTest(name = "applicationDate {0} applies price list {1}")
    @CsvSource({
            // the interval start and end are inclusive
            "2020-06-14T14:59:59, 1",
            "2020-06-14T15:00:00, 2",
            "2020-06-14T18:30:00, 2",
            "2020-06-14T18:30:01, 1",
            "2020-06-15T00:00:00, 3",
            "2020-06-15T11:00:00, 3",
            "2020-06-15T11:00:01, 1",
            "2020-06-15T15:59:59, 1",
            "2020-06-15T16:00:00, 4"
    })
    void appliesIntervalBoundariesInclusively(String applicationDate, int priceList) throws Exception {
        mockMvc.perform(request(ZARA, PRODUCT, applicationDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceList").value(priceList));
    }

    @ParameterizedTest(name = "brandId={0}, productId={1}, applicationDate={2} is not found")
    @CsvSource({
            // unknown product
            "1, 999, 2020-06-14T10:00:00",
            // unknown brand
            "2, 35455, 2020-06-14T10:00:00",
            // before the first price starts
            "1, 35455, 2020-06-13T23:59:59",
            // after the last price ends
            "1, 35455, 2021-01-01T00:00:00"
    })
    void returnsNotFoundWhenNoPriceApplies(String brandId, String productId, String applicationDate)
            throws Exception {
        mockMvc.perform(request(brandId, productId, applicationDate))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest(name = "brandId={0}, productId={1}, applicationDate={2} is rejected")
    @CsvSource({
            // missing parameters (empty cell = parameter not sent)
            ", 35455, 2020-06-14T10:00:00",
            "1, , 2020-06-14T10:00:00",
            "1, 35455, ",
            // ids that are not numbers
            "abc, 35455, 2020-06-14T10:00:00",
            "1, 1.5, 2020-06-14T10:00:00",
            // malformed dates
            "1, 35455, 2020-06-14",
            "1, 35455, 14/06/2020 10:00",
            "1, 35455, 2020-06-14T25:00:00"
    })
    void returnsBadRequestWhenParametersAreInvalid(String brandId, String productId, String applicationDate)
            throws Exception {
        mockMvc.perform(request(brandId, productId, applicationDate))
                .andExpect(status().isBadRequest());
    }

    private static MockHttpServletRequestBuilder request(String brandId, String productId,
                                                         String applicationDate) {
        MockHttpServletRequestBuilder request = get("/prices");
        if (brandId != null) {
            request.param("brandId", brandId);
        }
        if (productId != null) {
            request.param("productId", productId);
        }
        if (applicationDate != null) {
            request.param("applicationDate", applicationDate);
        }
        return request;
    }
}
