package cloudsim.gui;
import java.io.File;
import org.cloudbus.cloudsim.*;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ComplianceGraph {
    static double maxTransferTime;
    static Vector v=new Vector(3,1);
    public ArrayList<String> Datacenter_Country= new ArrayList<String>();
    static String host = "jdbc:derby://localhost:1527/cloudsimDB";
    static String uName = "root";
    static String uPass = "root";
    static List<String> clist;
    static int flag=0;
    String dc1,dc2;
    int index=0;
    CloudDriver obj = new CloudDriver();
    public void instantiate()
    {
        try
        {
            Connection con = DriverManager.getConnection(host, uName, uPass);
            Statement stmt = con.createStatement();
            String SQL = "SELECT * FROM TOKEN_COMPLIANTS";
            ResultSet rs = stmt.executeQuery(SQL);
            while(rs.next())
            {
                LinkedList l=new LinkedList();
                l.add(rs.getString("TOKEN"));
                String[] countryList = rs.getString("LINK").trim().split("\\s*,\\s*");
                for(int i=0;i<countryList.length;i++)
                  l.add(countryList[i]);
                v.addElement(l);
            }
            con.close();
        }
        catch(Exception ex){
          ex.printStackTrace();
        }
    }
    public double remove(String filename,java.io.File openFile)
    {
        double result=0.0;
        if(flag==1){
        String host = "jdbc:derby://localhost:1527/cloudsimDB";
        String uName = "root";
        String uPass = "root";
        String country="",replicate="";
        double time1=0.0,time2=0.0;
        try
        {
            Connection con = DriverManager.getConnection(host, uName, uPass);
            Statement stmt = con.createStatement();
            String SQL = "SELECT * FROM CLOUDFILE where FILENAME='"+filename+"'";
            ResultSet rs = stmt.executeQuery(SQL);
            if(rs.next())
            {
                country=rs.getString("COUNTRY");
                replicate=rs.getString("REPLICATE");
            }
            con.close();
        }
        catch(Exception ex){
          ex.printStackTrace();
        }
        
        Datacenter_Country.add(country);
        Datacenter_Country.add(replicate);
        Vector v = obj.getVector();
            try {
                String d= search(v,1);
                System.out.println(d);
                obj.setVal(1);
                time1=obj.services(2,filename,openFile,null,Datacenter_Country,v,d);
                d=search(v,2);
                //System.out.println(d);
                obj.setVal(2);
                time2=obj.services(2,filename,openFile,null,Datacenter_Country,v,d);
                result=(time1+time2);
            } catch (ParameterException ex) {
                Logger.getLogger(ComplianceGraph.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            System.out.println("CLOUDSIM CANNOT REMEMBER PREVIOUS TRANSACTION");
        }
        return result;
    }

    public double transfer(String filename,String maincountry, String replicountry, String newMain, String newRepli,java.io.File openFile, String path)
    {
        double result=0.0,time1=0.0,time2=0.0;
        if(flag==1){
        Datacenter_Country.add(maincountry);
        Datacenter_Country.add(replicountry);
        Vector v = obj.getVector();
        ArrayList<String> DC= new ArrayList<String>(Datacenter_Country);     
            try {
                String d=search(v,1);
                obj.setVal(1);
                time1=obj.services(3, filename, openFile,null,Datacenter_Country,v,d);
                d=search(v,2);
                obj.setVal(2);
                time2=obj.services(3, filename, openFile,null,Datacenter_Country,v,d);
            } catch (ParameterException ex) {
                Logger.getLogger(ComplianceGraph.class.getName()).log(Level.SEVERE, null, ex);
            }
            Datacenter_Country.add(newMain);
            Datacenter_Country.add(newRepli);
            Vector v1 = obj.getVector();    
            try {
                String d=search(v1,1);
                obj.setVal(1);
                time1+=obj.services(4,filename,openFile,path,Datacenter_Country,v1,d);
                d=search(v1,2);
                obj.setVal(2);
                time2+=obj.services(4,filename,openFile,path,Datacenter_Country,v1,d);
            } catch (ParameterException ex) {
                Logger.getLogger(ComplianceGraph.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            System.out.println("CLOUDSIM CANNOT REMEMBER PREVIOUS TRANSACTION");
        }
        System.out.println(time1);
        System.out.println(time2);
        return time1+time2;
    }
    
    public double store(String country,String file,java.io.File fileObj,String path)
    {
        String host = "jdbc:derby://localhost:1527/cloudsimDB";
        String uName = "root";
        String uPass = "root";
        String token=" ";
        int flags=0;
        String s1;
        double time1=0.0,time2=0.0;
        String country1="",country2;
        maxTransferTime=0.0;
        Enumeration vEnum2=v.elements();
        LinkedList temp1=new LinkedList();
        try
        {
            Connection con = DriverManager.getConnection(host, uName, uPass);
            Statement stmt = con.createStatement();
            String SQL = "SELECT * FROM TOKEN_CNTRY NATURAL JOIN TOKEN_COMPLIANTS WHERE COUNTRY='"+country+"'";
            ResultSet rs = stmt.executeQuery(SQL);
            if(rs.next())
            {
                token=rs.getString("TOKEN");
            }
            while(vEnum2.hasMoreElements())
            {
                temp1=(LinkedList) vEnum2.nextElement();
                s1=temp1.getFirst().toString();
                if(s1.equals(token))
                {
                    flags=1;
                    break;
                }
            }
                int size;
                size=temp1.size();
                Random r=new Random();
                int country_1=r.nextInt(size);
                int country_2=r.nextInt(size);
                while(country_1==country_2||(country_1==0)||(country_2==0)){
                    if(country_1==0)
                        country_1=r.nextInt(size);
                    if(country_2==0)
                        country_2=r.nextInt(size);
                    country_2=r.nextInt(size);
                }
                
                String ctry1,ctry2;
                ctry1=temp1.get(country_1).toString();
                ctry2=temp1.get(country_2).toString();
                 SQL = "UPDATE CLOUDFILE SET COUNTRY='"+ctry1+"',REPLICATE = '"+ctry2+"' WHERE COUNTRY='" + country + "'";
                    Statement stmt1 = con.createStatement();
                    stmt1.executeUpdate(SQL);
                    Datacenter_Country.add(ctry1);
                    Datacenter_Country.add(ctry2);
                    System.out.println(Datacenter_Country.get(0)+" "+Datacenter_Country.get(1));
                    Vector v=obj.create_datacenter();
                    String d=search(v,1);
                    //System.out.println(d);
                    obj.setVal(1);
                    time1=obj.services(1,file,fileObj,path,Datacenter_Country,v,d);
                    d=search(v,2);
                    //System.out.println(d);
                    obj.setVal(2);
                    time2=obj.services(1,file,fileObj,path,Datacenter_Country,v,d);
                    flag=1;
              maxTransferTime=time1+time2;
        }catch(Exception ex){
          ex.printStackTrace();
        }
        return maxTransferTime;
    }
    public String search(Vector v1,int iter)
    {
        String s1="";
        String d="";
        int i=0;
        Enumeration vEnum2=v1.elements();
        LinkedList temp1=new LinkedList();
        while(vEnum2.hasMoreElements())
        {
                if(iter==1)
                    index++;
                else if(index>0){
                    index--;
                    continue;
                }
                temp1=(LinkedList) vEnum2.nextElement();
                d=(String) temp1.getFirst();
                for(String temp:Datacenter_Country)
                {
                    if(d.compareTo(temp)==0)
                    {
                        Datacenter_Country.remove(d);
                        return d;
                    }
                }
        }
        return d;
    }
    
}
