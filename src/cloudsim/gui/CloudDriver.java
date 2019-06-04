/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudsim.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ParameterException;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class CloudDriver {
    static double capacity=5000000;
    int num_user = 1; 
    int flag;
    long maxTransferRate = 133;
    File readFile = null;
    static List<Vm> vmlist;
    static String host = "jdbc:derby://localhost:1527/cloudsimDB";
    static String uName = "root";
    static String uPass = "root";
    String fname="";
    Vector v=new Vector(3,1);
    ArrayList<String> dcNameNew;
    String dc1,dc2;
    int index=0;
    public int val=0;
    public void setVal(int v){
        val=v;
    }
    public Vector getVector(){
        return v;
    }
    public Vector create_datacenter() throws ParameterException
    {
            String[] Datacenter={"NorthCentralUS","SouthCentralUS","NorthEurope","WestEurope","CentralIndia","SouthIndia","WestIndia","EastAsia"};
            for(int i=0;i<Datacenter.length;i++)
            {
                LinkedList l=new LinkedList();
                HarddriveStorage hdd1 = new HarddriveStorage(capacity);
                LinkedList<Storage> hdList1 = new LinkedList<Storage>();
                hdList1.add(hdd1);
                l.add(Datacenter[i]);
                l.add(hdList1);
                v.add(l);
            }
            return v;
    }
    public static double getseek(double fileObj)
    {
        double result =0;
        if(fileObj>0 && capacity!=0)
        {
            result+=(int) fileObj/capacity;
        }
        return result;
            
    }
    private double getTransferTime(double fileObj) 
    {
	double result = 0;
	if (fileObj > 0 && capacity != 0) {
		result = (fileObj * maxTransferRate) / capacity;
	}
        return result;
    }
    public double services(int service,String filename,java.io.File fileObj,String path,ArrayList<String>dcName,Vector v,String dc) throws ParameterException
   {
       double result=0.0,seektime=0.0,transfertime=0.0,net=0.0;
       dcNameNew = new ArrayList<String>(dcName);
        try {
			
			int num_user = 1;  
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; 
			CloudSim.init(num_user, calendar, trace_flag);
                        LinkedList<Storage> hdList1 = new LinkedList<Storage>(search(v,dcNameNew));
                        //LinkedList<Storage> hdList2 = new LinkedList<Storage>(search(v));
			@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter(dc,hdList1);
			//@SuppressWarnings("unused")
			//Datacenter datacenter1 = createDatacenter(dc2,hdList2);
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();
			vmlist = createVM(brokerId,1); 
			cloudletList = createCloudlet(brokerId,1);
			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);
                        HarddriveStorage hdd = new HarddriveStorage(capacity);
                        switch(service){
                        case 1:{
                            File file1 = new File(filename, (int) fileObj.length());
                            hdd=cloudletList.get(0).addRequiredFile(filename,(HarddriveStorage)hdList1.get(0),file1,fileObj,path,dc);
                            v=putHdd(hdd, v);
                            transfertime=getTransferTime(fileObj.length());
                            result=result+transfertime;
                            break;
                        }
                        case 2:{
                           hdd=cloudletList.get(0).deleteRequiredFile(filename,(HarddriveStorage) search(v,dcNameNew).get(0));
                           v=putHdd(hdd, v);
                           seektime=getseek(fileObj.length());
                           //System.out.println(seektime);
                           result=result+seektime;
                           break;
                        }
                        case 3:{
                           hdd=cloudletList.get(0).transferCleanup(filename,dc,(HarddriveStorage) search(v,dcNameNew).get(0));
                           v=putHdd(hdd, v);
                           seektime=getseek(fileObj.length());
                           result=result+seektime;
                           break;
                        }
                        case 4:{
                            File file1 = new File(filename, (int) fileObj.length());
                            hdd=cloudletList.get(0).transferWrite(filename,dc,(HarddriveStorage)hdList1.get(0),file1);
                            v=putHdd(hdd, v);
                            transfertime=getTransferTime(fileObj.length());
                            result=result+transfertime;
                            break;
                        }
       }
			CloudSim.startSimulation();
			List<Cloudlet> newList = broker.getCloudletReceivedList();
                        if(val==1)
                        {
                            result+=(cloudletList.get(0).getFinishTime()/100);
                        }
                        System.out.println(filename+" processed at "+dc+" in "+result+" seconds");
                            net = cloudletList.get(0).duration/100000000 + result;
                            System.out.println("TOTAL TIME:  "+net+" seconds");
			printCloudletList(newList);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
        return net;
   }
    public Vector putHdd(HarddriveStorage hdd,Vector v){
        String s1="";
        String d="";
        LinkedList<Storage> hd = new LinkedList<Storage>();
        Enumeration vEnum2=v.elements();
        LinkedList temp1=new LinkedList();
        while(vEnum2.hasMoreElements())
            {
                temp1=(LinkedList) vEnum2.nextElement();
                d=(String) temp1.getFirst();
                hd =(LinkedList<Storage>) temp1.getLast();
                for(String temp:dcNameNew)
                {
                    if(d.compareTo(temp)==0)
                    {
                        hd.remove(0);
                        hd.add(hdd);
                        return v;
                    }
                }
            }
        return v;
    }
    public LinkedList<Storage> search(Vector v1,ArrayList<String> dcNameNew)
    {
        String s1="";
        String d="";
        LinkedList<Storage> hd = new LinkedList<Storage>();
        Enumeration vEnum2=v1.elements();
        LinkedList temp1=new LinkedList();
        while(vEnum2.hasMoreElements())
            {
                temp1=(LinkedList) vEnum2.nextElement();
                d=(String) temp1.getFirst();
                hd =(LinkedList<Storage>) temp1.getLast();
                for(String temp:dcNameNew)
                {
                    if(d.compareTo(temp)==0)
                    {
                       dcNameNew.remove(temp);
                       return hd;
                    }
                }
            }
        return hd;
    }
    private static List<Cloudlet> cloudletList;


	private static List<Vm> createVM(int userId, int vms) {

		//Creates a container to store VMs. This list is passed to the broker later
		LinkedList<Vm> list = new LinkedList<Vm>();

		//VM Parameters
		long size = 10000; //image size (MB)
		int ram = 512; //vm memory (MB)
		int mips = 1000;
		long bw = 1000;
		int pesNumber = 1; //number of cpus
		String vmm = "Xen"; //VMM name

		//create VMs
		Vm[] vm = new Vm[vms];

		for(int i=0;i<vms;i++){
			vm[i] = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			//for creating a VM with a space shared scheduling policy for cloudlets:
			//vm[i] = Vm(i, userId, mips, pesNumber, ram, bw, size, priority, vmm, new CloudletSchedulerSpaceShared());

			list.add(vm[i]);
		}

		return list;
	}


	private static List<Cloudlet> createCloudlet(int userId, int cloudlets){
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		//cloudlet parameters
		long length = 1000;
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet[] cloudlet = new Cloudlet[cloudlets];

		for(int i=0;i<cloudlets;i++){
			cloudlet[i] = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}

    private static Datacenter createDatacenter(String name,LinkedList<Storage> storageList){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		//    our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 1000;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

		//4. Create Hosts with its id and list of PEs and add them to the list of machines
		int hostId=0;
		int ram = 2048; //host memory (MB)
		long storage = 1000000; //host storage
		int bw = 10000;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList,
    				new VmSchedulerTimeShared(peList)
    			)
    		); // This is our first machine

		//create another machine in the Data center
		List<Pe> peList2 = new ArrayList<Pe>();

		peList2.add(new Pe(0, new PeProvisionerSimple(mips)));

		hostId++;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList2,
    				new VmSchedulerTimeShared(peList2)
    			)
    		); // This is our second machine



		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.001;	// the cost of using storage in this resource
		double costPerBw = 0.0;			// the cost of using bw in this resource
		//LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static DatacenterBroker createBroker(){

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;
                
		String indent = "    ";
		System.out.println();
		System.out.println("========== OUTPUT ==========");
		System.out.println("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time"+indent+"Files");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			System.out.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				System.out.print("SUCCESS");

				System.out.println(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime())
                                                +indent+indent
						+cloudlet.getRequiredFiles());
			}
		}
	}
    
}
