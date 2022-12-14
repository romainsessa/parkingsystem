package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    // CHANGE - New unit test
    @Test
    public void testProcessIncomingVehicle() {
        try {
            //getNextParkingNumberIfAvailable
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
            //getVehichleRegNumber
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            when(ticketDAO.getTicket(anyString())).thenReturn(null);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.processIncomingVehicle();

            verify(inputReaderUtil, Mockito.times(1)).readSelection();
            verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
            verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
            verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }

    }

    // CHANGE - Update this test
    @Test
    public void processExitingVehicleTest(){
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.processExitingVehicle();

            verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
            verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
            verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
            verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    // CHANGE - New unit tests
    
    @Test
    public void processExitingVehicleTestUnableUpdate(){
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            parkingService.processExitingVehicle();

            verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
            verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
            verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
            verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        try {
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

            ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();
            assertEquals(ParkingType.CAR, spot.getParkingType());
            assertEquals(1,spot.getId());
            assertEquals(true, spot.isAvailable());

            verify(inputReaderUtil, Mockito.times(1)).readSelection();
            verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        try {
            when(inputReaderUtil.readSelection()).thenReturn(2);
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

            ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();
            assertNull(spot);

            verify(inputReaderUtil, Mockito.times(1)).readSelection();
            verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        try {
            when(inputReaderUtil.readSelection()).thenReturn(3);
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

            ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();
            assertNull(spot);

            verify(inputReaderUtil, Mockito.times(1)).readSelection();
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

}
