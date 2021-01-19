package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.Assertions;
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

    private Ticket ticket;

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    void processExitingVehicleTest(){
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    void processIncomingVehicleWithDiscount() {
        // Bouchon pour attraper la sauvegarde en base de données d'un ticket et stocker ce ticket dans le test
        when(ticketDAO.saveTicket(any(Ticket.class)))
                .then(mockData->getTicketFromMockData(mockData))
                .thenReturn(true);
        // PRE CONDITION(S)
        // TODO simuler la présence d'au moins 1 ticket dans la BDD
        when(ticketDAO.countByVehicleRegNumber()).thenReturn(1);
        // TEST
        parkingService.processIncomingVehicle();
        // POST CONDITION(S)
        Assertions.assertEquals(5, this.ticket.getDiscount());
    }

    @Test
    void processIncomingVehicleWithoutDiscount() {
        // Bouchon pour attraper la sauvegarde en base de données d'un ticket et stocker ce ticket dans le test
        when(ticketDAO.saveTicket(any(Ticket.class)))
                .then(mockData->getTicketFromMockData(mockData))
                .thenReturn(true);
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
