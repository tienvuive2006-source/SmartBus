package com.smartbus.booking.service;

/**
 * Chính sách tích điểm dùng chung cho mua, hủy và đổi vé.
 */
public final class LoyaltyPointPolicy {

    public static final int VND_PER_POINT = 5_000;

    private LoyaltyPointPolicy() {
    }

    public static int pointsFor(double paidAmount) {
        if (paidAmount <= 0) return 0;
        return (int) Math.floor(paidAmount / VND_PER_POINT);
    }
}
