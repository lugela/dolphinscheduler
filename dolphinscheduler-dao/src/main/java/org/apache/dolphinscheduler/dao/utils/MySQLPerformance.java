/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.dao.utils;

import static org.apache.dolphinscheduler.dao.MonitorDBDao.VARIABLE_NAME;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.MonitorRecord;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

/**
 * MySQL performance
 */
@Slf4j
public class MySQLPerformance extends BaseDBPerformance {

    /**
     * get monitor record
     * @param conn connection
     * @return MonitorRecord
     */
    @Override
    public MonitorRecord getMonitorRecord(Connection conn) {
        MonitorRecord monitorRecord = new MonitorRecord();
        monitorRecord.setDate(new Date());
        monitorRecord.setDbType(DbType.MYSQL);
        monitorRecord.setState(Flag.YES);

        try (Statement pstmt = conn.createStatement()) {
            try (ResultSet rs1 = pstmt.executeQuery("show global variables")) {
                while (rs1.next()) {
                    if ("MAX_CONNECTIONS".equalsIgnoreCase(rs1.getString(VARIABLE_NAME))) {
                        monitorRecord.setMaxConnections(Long.parseLong(rs1.getString("value")));
                    }
                }
            }

            try (ResultSet rs2 = pstmt.executeQuery("show global status")) {
                while (rs2.next()) {
                    if ("MAX_USED_CONNECTIONS".equalsIgnoreCase(rs2.getString(VARIABLE_NAME))) {
                        monitorRecord.setMaxUsedConnections(Long.parseLong(rs2.getString("value")));
                    } else if ("THREADS_CONNECTED".equalsIgnoreCase(rs2.getString(VARIABLE_NAME))) {
                        monitorRecord.setThreadsConnections(Long.parseLong(rs2.getString("value")));
                    } else if ("THREADS_RUNNING".equalsIgnoreCase(rs2.getString(VARIABLE_NAME))) {
                        monitorRecord.setThreadsRunningConnections(Long.parseLong(rs2.getString("value")));
                    }
                }
            }
        } catch (Exception e) {
            monitorRecord.setState(Flag.NO);
            log.error("SQLException ", e);
        }
        return monitorRecord;
    }

}
