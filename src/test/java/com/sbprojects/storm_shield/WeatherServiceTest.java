package com.sbprojects.storm_shield;

import com.sbprojects.storm_shield.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class WeatherServiceTest {

    private WeatherService weatherService;

    //dependencies
    @Mock private RestClient restClient;
    @Mock private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock private RestClient.RequestHeadersSpec requestHeadersSpec;
    @Mock private  RestClient.ResponseSpec responseSpec;


    @BeforeEach //run setup before every test method
    public void setUp() {
        MockitoAnnotations.openMocks(this); //Initializes Mockito annotations

        //Mock the RestClient builder to return a mock restClient
        RestClient.Builder builder = mock(RestClient.Builder.class);

        //Fox: Handle fluent chain by returning the builder mock itself
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);


        weatherService = new WeatherService(builder, "https://fake-url.com");
        ReflectionTestUtils.setField(weatherService, "apiKey", "fake_api_key");
    }

    @Test
    public void testGetWindSpeed_Success(){
        //1. Arrange: Create a mock response map that looks like OpenWeatherMap's JSON
        Map<String, Object> mockResponse = new HashMap<>();
        Map<String, Object> mockWindData = new HashMap<>();
        mockWindData.put("speed", 15.5);
        mockResponse.put("wind", mockWindData);
        /* mockResponse map:
            {
              "wind": {
                "speed": 15.5
              }
            }
         */

        //Mock the entire fluent HTTP call chain structure
        when(restClient.get()).thenReturn(requestHeadersUriSpec); //returns mocked object
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Map.class)).thenReturn(mockResponse); //Actual data

        //2. Act: call the method
        double windSpeed = weatherService.getWindSpeed("Memphis");

        //3. Assert
        assertEquals(15.5, windSpeed, 0.001);
    }

}
