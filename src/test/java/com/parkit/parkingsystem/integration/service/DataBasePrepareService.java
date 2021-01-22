package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.NoSuchElementException;

public class DataBasePrepareService {

    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public void clearDataBaseEntries(){
        Connection connection = null;
        try{
            connection = dataBaseTestConfig.getConnection();

            //set parking entries to available
            connection.prepareStatement("update parking set available = true").execute();

            //clear ticket entries;
            connection.prepareStatement("truncate table ticket").execute();

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }

    public boolean ticketExistsForVehicleRegNumber(String vehicleRegNumber) {
        try (Connection connection = dataBaseTestConfig.getConnection()) {
            final PreparedStatement ps = connection.prepareStatement(
                    "SELECT COUNT(*) as quantity FROM ticket WHERE ticket.VEHICLE_REG_NUMBER = ?"
            );
            ps.setString(1, vehicleRegNumber);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("quantity") > 0;
            } else {
                throw new NoSuchElementException("Empty ResultSet");
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean slotAvailable() {
        //TODO
        return true;
    }
}
