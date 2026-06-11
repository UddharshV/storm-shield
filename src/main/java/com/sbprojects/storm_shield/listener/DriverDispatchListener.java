package com.sbprojects.storm_shield.listener;

import com.sbprojects.storm_shield.event.SevereWeatherEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DriverDispatchListener {
    @EventListener //Spring routes a copy of the event object here concurrently
    public void handleSevereWeather(SevereWeatherEvent event){
        System.out.println("[PLAYBOOK ACTION] DriverDispatchListener preparing emergency logistics warning payloads...");

        //Constructing a sanitized enterprise warning template payload
        String alertPayload = String.format(
                "\n__________________________________________________________\n" +
                        "[OUTBOUND DISPATCH TRANSMISSION]\n" +
                        "TARGET OPERATIONS REGION: %s Hub\n" +
                        "CRITICAL HAZARD PARAMETER: Wind speeds reached %.1f mph\n" +
                        "OPERATIONAL PLAYBOOK COMMAND: Hold line haul trailers at nearest safe terminal. Do not dispatch into storm zone.\n" +
                        "\n__________________________________________________________\n",
                event.getCity().toUpperCase(),
                event.getWindSpeed()
        );

        //FUTURE SCOPE: To extend this as a production system, this payload would be handed to a message queue (Kafka/RaabitMQ) or SMS gateway
        System.out.println(alertPayload);
    }
}
