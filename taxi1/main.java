package taxi1;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;

class mapInfo{
	int[][] map=new int[80][80];
	public void readmap(String path){//读入地图信息
		//Requires:String类型的地图路径,System.in
		//Modifies:System.out,map[][]
		//Effects:从文件中读入地图信息，储存在map[][]中
		Scanner scan=null;
		File file=new File(path);
		if(file.exists()==false){
			System.out.println("地图文件不存在,程序退出");
			System.exit(1);
			return;
		}
		try {
			scan = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			
		}
		for(int i=0;i<80;i++){////////////////////////////
			String[] strArray = null;
			try{
				strArray=scan.nextLine().split("");
			}catch(Exception e){
				System.out.println("地图文件信息有误，程序退出");
				System.exit(1);
			}
			for(int j=0;j<80;j++){////////////////////////////////
				try{
					this.map[i][j]=Integer.parseInt(strArray[j]);
				}catch(Exception e){
					System.out.println("地图文件信息有误，程序退出");
					System.exit(1);
				}
			}
		}
		scan.close();
	}
}
public class main {
	/*@ OVERVIEW:实现对其他类的对象创建，实现出租车系统
	@ 表示对象：None
	@ 抽象函数：None
	@ 不变式：None
	 */
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		return true;
	}
	public static void toFile(String str, int id){
		/*@ REQUIRES: id>0 && str != null;
		@ MODIFIES: 对应文件;
		@ EFFECTS: 
			通过id寻找相应请求的文件，若文件不存在新建一个文件，并写入字符串str;
			输入输出流出现异常时抛出异常IOException，控制台输出该异常信息；
		*/
		Charset charset = Charset.forName("US-ASCII");
		try{
			String s = "result"+id+".txt";
			FileOutputStream out = new FileOutputStream(s, true); 
			out.write(str.getBytes(charset)); 
			out.close();    
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
	}
	public static void main (String[] args){
		/*@ REQUIRES: 
		@ MODIFIES: None;
		@ EFFECTS: 
			创建此程序所需的线程以及数据处理对象，其中若本程序地图设置的路径不存在则控制台输出报错并退出，并实现每200ms刷新一次流量
		*/
		TaxiGUI gui=new TaxiGUI();
		mapInfo mi=new mapInfo();
		mi.readmap("C:/Users/Desktop/Map.txt");//在这里设置gui地图文件路径
		gui.LoadMap(mi.map, 80);
		Taxi taxi[] = new Taxi[100];
		File f = new File("C:/Users/Desktop/map.txt");//在这里设置本程序的地图文件路径
		File f2 = new File("C:/Users/Desktop/a.txt");//在这里设置红绿灯的文件
		if(!f.exists()&&!f.isFile()&&!f2.exists()&&!f2.isFile()){
			System.out.println("NO file");
			System.exit(0);
		}
		map map = new map(f, gui);
		map.setlight(f2);
		map.setp(f, gui);
		taxi=init_taxi( map,  gui);
		requestlist list = new requestlist();
		in in = new in(list, map, gui);
		system s = new system(list, taxi, map);
		for(int i=0; i<100; i++){// car total>>>>>>>>>>>>>>>>>>
			//the pos
			//int x= (int) (Math.random()*10000)%map.topnode.size();
			//taxi[i] = new New_Taxi(map.topnode.get(x).p.x,map.topnode.get(x).p.y, map, i+1, gui);
			taxi[i].start();
			//gui.SetTaxiStatus(i, new Point(map.topnode.get(x).p.x,map.topnode.get(x).p.y), 2);
		}
		in.start();
		s.start();
		while(true){
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			map.clean();
			
		}
	}
	public static Taxi[] init_taxi(map map, TaxiGUI gui){
		/*@ REQUIRES: map!=null && gui!=null
		@ MODIFIES: None;
		@ EFFECTS: 
			实现100辆出租车的初始化，其中30辆可追踪出租车，70辆普通出租车
		*/
		Taxi taxi[] = new Taxi[100];
		//可以将下面的代码注释掉换上你的代码
		for(int i=0; i<30; i++){
			int x= (int) (Math.random()*10000)%80;
			int y= (int) (Math.random()*10000)%80;
			taxi[i] = new New_Taxi(x,y, map, i+1, gui);
			
		}
		for(int i=30;i<100;i++){
			int x= (int) (Math.random()*10000)%80;
			int y= (int) (Math.random()*10000)%80;
			taxi[i] = new Taxi(x,y, map, i+1, gui);
		}
		//end
		return taxi;
	}

}
