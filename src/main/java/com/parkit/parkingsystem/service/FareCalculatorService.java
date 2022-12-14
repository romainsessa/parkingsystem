package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    // CHANGE - For 30min free and discount features
    public void calculateFare(Ticket ticket, boolean discount) {
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();
        double durationMinute = (outTime - inTime) / 1000 / 60;

        double basedPrice, price;
        if(durationMinute < 30) {
            basedPrice = 0;
        } else {
            basedPrice = durationMinute / 60;
        }

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                price = basedPrice * Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                price = basedPrice * Fare.BIKE_RATE_PER_HOUR;
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
        if(discount) {
            price *= 0.95;
        }
        ticket.setPrice(price);
    }

    // CHANGE - to compliance with existing tests
    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}