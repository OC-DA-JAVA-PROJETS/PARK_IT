package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @Mock
    private static ParkingSpotDAO parkingSpotDAO;

    @Mock
    private static TicketDAO ticketDAO;

    private static DataBasePrepareService dataBasePrepareService;

    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() {
        try {
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            dataBasePrepareService.clearDataBaseEntries();
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up tests");
        }
    }

    @Test
    void processExitingVehicleTest() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    void processIncomingVehicleWithDiscount() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class)))
                .then(mockData->getTicketFromMockData(mockData))
                .thenReturn(true);
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(2);
        // PRE CONDITION(S)
        when(ticketDAO.countByVehicleRegNumber()).thenReturn(1);
        // TEST
        parkingService.processIncomingVehicle();
        // POST CONDITION(S)
        Assertions.assertEquals(5, this.ticket.getDiscount());
    }

    @Test
    void processIncomingVehicleWithoutDiscount() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class)))
                .then(mockData->getTicketFromMockData(mockData))
                .thenReturn(true);
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(2);
        // PRE CONDITION(S)
        when(ticketDAO.countByVehicleRegNumber()).thenReturn(0);
        // TEST
        parkingService.processIncomingVehicle();
        // POST CONDITION(S)
        Assertions.assertEquals(0, this.ticket.getDiscount());
    }

    private Object getTicketFromMockData(InvocationOnMock mockData) {
        this.ticket = mockData.getArgument(0);
        return null;
    }

}
