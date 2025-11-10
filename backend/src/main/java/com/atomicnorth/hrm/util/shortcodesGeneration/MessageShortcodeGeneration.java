package com.atomicnorth.hrm.util.shortcodesGeneration;

import com.atomicnorth.hrm.configuration.multitenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

@Component
public class MessageShortcodeGeneration {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String callMessageGenerateCodeProcedure(String prefix, String messageCode, String type, int moduleId) {
        try {
            String schemaName = TenantContextHolder.getTenant();
            //String procedureName = schemaName + ".GenerateMessageShortcode"; // Replace with the actual procedure name
            String procedureName = schemaName + ".GenerateLookupTypeShortcode"; // Replace with the actual procedure name

            // Get the database connection
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            CallableStatement callableStatement = connection.prepareCall("{CALL " + procedureName + "(?, ?, ?, ?, ?)}");

            // Set input parameters
            callableStatement.setString(1, prefix);
            callableStatement.setString(2, messageCode);
            callableStatement.setString(3, type);
            callableStatement.setInt(4, moduleId);

            // Register output parameter
            callableStatement.registerOutParameter(5, Types.VARCHAR);

            // Execute the stored procedure
            callableStatement.execute();

            // Get the result from the output parameter
            String generatedCode = callableStatement.getString(5);

            // Close resources
            callableStatement.close();
            connection.close();

            return generatedCode;
        } catch (Exception e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            // Return an error message or handle the exception according to your application's logic
            return "Error occurred while generating code: " + e.getMessage();
        }
    }
}
