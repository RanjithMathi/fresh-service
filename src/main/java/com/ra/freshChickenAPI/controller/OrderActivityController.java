package com.ra.freshChickenAPI.controller;

import com.ra.freshChickenAPI.dto.OrderActivityMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class OrderActivityController {

    private final SimpMessagingTemplate messagingTemplate;

    public OrderActivityController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/order-activity")
    @SendTo("/topic/order-activity")
    public OrderActivityMessage handleOrderActivity(OrderActivityMessage message) {
        return message;
    }

    public void sendOrderActivityNotification(OrderActivityMessage message) {
        messagingTemplate.convertAndSend("/topic/order-activity", message);
    }
}