package com.atomicnorth.hrm.util.commonClass;

import com.atomicnorth.hrm.tenant.helper.SessionHolder;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;

@Component
public class CommonFeatures {
    /*
        @Autowired
        private JdbcTemplate jdbcTemplate;

        //commonFeatures.callGenerateCodeProcedure(typeId, moduleId, functionId, "LT", "M");
        //
        public String callGenerateCodeProcedure(int moduleId, int functionId, String content, String contentType) {
            try {
                UserLoginDetail user= SessionHolder.getUserLoginDetail();
                String schemaName = "tenant_1"; // Replace with the actual schema name
                String procedureName = schemaName + ".generate_final_code";

                String generatedCode = jdbcTemplate.queryForObject(
                        "CALL " + procedureName + "(?, ?, ?, ?)",
                        new Object[]{moduleId, functionId, content, contentType},
                        new int[]{Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.VARCHAR},
                        String.class);
                return generatedCode;
            } catch (Exception e) {
                // Log the exception for debugging purposes
                e.printStackTrace();
                // Return an error message or handle the exception according to your application's logic
                return "Error occurred while generating code: " + e.getMessage();
            }
        }*/
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //commonFeatures.callGenerateCodeProcedure(typeId, moduleId, functionId, "LT", "M");

    public String callGenerateCodeProcedure(int typeId, int moduleId, int functionId, String content, String contentType) {
        try {
            UserLoginDetail user = SessionHolder.getUserLoginDetail();

            String tenantId = user.getSubject();
            String schemaName = "tenant_1";
            System.out.println(schemaName);
            String procedureName = schemaName + ".generate_final_code";

            String generatedCode = jdbcTemplate.queryForObject(
                    "CALL " + procedureName + "(?,?, ?, ?, ?)",
                    new Object[]{typeId, moduleId, functionId, content, contentType},
                    new int[]{Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.VARCHAR},
                    String.class);
            return generatedCode;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while generating code: " + e.getMessage();
        }
    }
}
