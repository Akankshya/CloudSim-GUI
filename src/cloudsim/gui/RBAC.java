package cloudsim.gui;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class RBAC {
    public int verify(String uname){
        int flag=0;
        String host = "jdbc:derby://localhost:1527/cloudsimDB";
        String uName = "root";
        String uPass = "root";
        String csp[]={"admin"};
        try {
            Connection con = DriverManager.getConnection(host, uName, uPass);
            Statement st1 = con.createStatement();
            ResultSet rs1 = st1.executeQuery("SELECT * FROM CLOUDUSER WHERE ID='"+uname+"'");
            if(rs1.next()){
                if(rs1.getString("ROLE").equals("CSP"))
                    flag=1;
                else if(rs1.getString("ROLE").equals("USER"))
                    flag=2;
                else
                    flag=3;
            }    
        } catch (SQLException ex) {
            Logger.getLogger(RBAC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return flag;
    }
}
