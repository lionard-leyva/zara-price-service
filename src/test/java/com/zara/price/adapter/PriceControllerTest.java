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

@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerTest {

    private static final long ZARA = 1;
    private static final long PRODUCT = 35455;

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
        mockMvc.perform(get("/prices")
                        .param("brandId", String.valueOf(ZARA))
                        .param("productId", String.valueOf(PRODUCT))
                        .param("applicationDate", applicationDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brandId").value(ZARA))
                .andExpect(jsonPath("$.productId").value(PRODUCT))
                .andExpect(jsonPath("$.priceList").value(priceList))
                .andExpect(jsonPath("$.startDate").value(startDate))
                .andExpect(jsonPath("$.endDate").value(endDate))
                .andExpect(jsonPath("$.price").value(price))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }
}
