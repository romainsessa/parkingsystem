package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    // CHANGE - Update existing integration tests

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertEquals("ABCDEF",ticket.getVehicleRegNumber());

        int slot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertEquals(2, slot);
    }

    @Test
    public void testParkingLotExit() throws InterruptedException {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle();
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        Date expectedDate = new Date();
        expectedDate.setTime(ticket.getInTime().getTime() + 1000);
        Thread.sleep(1000); // to simulate 1 second on the parking
        parkingService.processExitingVehicle();

        ticket = ticketDAO.getTicket("ABCDEF");
        assertEquals(0, ticket.getPrice());

        validateTime(expectedDate, ticket.getOutTime());
    }

    // CHANGE - Add new integration tests

    @Test
    public void testParkingLotExitRecurringUser() throws InterruptedException {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        Thread.sleep(1000);
        parkingService.processExitingVehicle();
        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        Date expectedDate = new Date();
        expectedDate.setTime(ticket.getInTime().getTime());

        Date inTime = new Date();
        inTime.setTime(ticket.getInTime().getTime() - (  45 * 60 * 1000));
        ticket.setInTime(inTime); // update ticket to be 45min in past
        ticketDAO.updateTicket(ticket);

        parkingService.processExitingVehicle();

        ticket = ticketDAO.getTicket("ABCDEF");
        assertEquals(0.75 * Fare.CAR_RATE_PER_HOUR * 0.95, ticket.getPrice());

        validateTime(expectedDate, ticket.getOutTime());
    }

    private void validateTime(Date expected, Date provided) {
        Calendar expectedCalendar = GregorianCalendar.getInstance();
        expectedCalendar.setTime(expected);

        Calendar obtainedCalendar = GregorianCalendar.getInstance();
        obtainedCalendar.setTime(provided);

        assertEquals(expectedCalendar.get(Calendar.MONTH), obtainedCalendar.get(Calendar.MONTH));
        assertEquals(expectedCalendar.get(Calendar.DAY_OF_MONTH), obtainedCalendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(expectedCalendar.get(Calendar.HOUR), obtainedCalendar.get(Calendar.HOUR));
        assertEquals(expectedCalendar.get(Calendar.MINUTE), obtainedCalendar.get(Calendar.MINUTE));
    }

}
