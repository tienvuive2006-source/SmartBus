package com.smartbus.booking.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private String bookingId;
    private int rating;
    private String comment;
}
