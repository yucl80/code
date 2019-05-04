package com.yucl.learn;

import com.mysql.cj.jdbc.ConnectionGroupManager;
import com.mysql.cj.jdbc.ha.ReplicationConnectionGroup;
import com.mysql.cj.jdbc.ha.ReplicationConnectionGroupManager;
import com.mysql.cj.jdbc.jmx.LoadBalanceConnectionGroupManager;
import com.mysql.cj.jdbc.jmx.ReplicationGroupManager;
import com.mysql.cj.jdbc.jmx.ReplicationGroupManagerMBean;
import com.sun.jmx.mbeanserver.JmxMBeanServer;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.sql.*;

public class ReplicationGroupManager {
    static ReplicationGroupManager replicationGroupManager = new ReplicationGroupManager();

    private static String URL = "jdbc:mysql:replication://" +
            "address=(type=master)(host=192.168.142.166)(port=3306),address=(type=slave)(host=192.168.142.167)(port=3306),address=(type=slave)(host=192.168.142.168)(port=3306)/test?" +
            "replicationConnectionGroup=aa&allowMasterDownConnections=true&replicationEnableJMX=true&useSSL=false&connectTimeout=1";

    public static void main(String[] args) throws Exception {

        replicationGroupManager.registerJmx();
        for (int i = 0; i < 10000; i++) {
            try (Connection conn = getNewConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement s = conn.prepareStatement("insert into test.t2(b) values(?)")) {
                    s.setString(1, String.valueOf(i));
                    s.executeUpdate();
                    conn.commit();
                    System.out.println("commit");
                }
            } catch (SQLException e) {
                System.out.println(e.getErrorCode());
                autoPromoteSlaveToMaster();
                e.printStackTrace();
            }
            Thread.sleep(1000);

            //if(i==1){
            // System.out.println("groups:"+replicationGroupManager.getRegisteredConnectionGroups());
            //replicationGroupManager.promoteSlaveToMaster("aa","192.168.142.167:3306");
            // replicationGroupManager.removeMasterHost("aa","192.168.142.166:3306");

            // System.out.println("groups:"+loadBalanceConnectionGroupManager.getRegisteredConnectionGroups());
            //ConnectionGroupManager.removeHost("aa", "192.168.142.166:3306");
            //loadBalanceConnectionGroupManager.
            //ReplicationConnectionGroup xx = ReplicationConnectionGroupManager.getConnectionGroup("aa");
            //xx.promoteSlaveToMaster("192.168.142.167:3306");
            // ReplicationConnectionGroupManager.removeMasterHost("aa","192.168.142.166:3306");
            // ReplicationConnectionGroupManager.promoteSlaveToMaster("aa","192.168.142.167:3306");
            // }
        }
    }

    static void autoPromoteSlaveToMaster() throws SQLException {


        String group = replicationGroupManager.getRegisteredConnectionGroups().split(",")[0];
        String hosts = replicationGroupManager.getMasterHostsList(group);
        System.out.println(hosts);
        replicationGroupManager.removeMasterHost(group, hosts);
        try (Connection conn = getNewConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement s = conn.prepareStatement("SELECT * FROM performance_schema.replication_group_members WHERE MEMBER_ID = (SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME= 'group_replication_primary_member')")) {
                try (ResultSet rst = s.executeQuery()) {
                    if (rst.next()) {
                        String memberHost = rst.getString("MEMBER_HOST");
                        String memberPort = rst.getString("MEMBER_PORT");
                        System.out.println("member:" + memberHost + ":" + memberPort);
                        replicationGroupManager.promoteSlaveToMaster(group, memberHost + ":" + memberPort);
                    }
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    static Connection getNewConnection() throws SQLException, ClassNotFoundException {
        // Class.forName("com.mysql.jdbc.Driver");
        //Class.forName("com.mysql.jdbc.ReplicationDriver");
        return DriverManager.getConnection(URL, "root", "yCl2017$");
    }


}
