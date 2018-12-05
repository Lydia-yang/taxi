package taxi1;

import java.awt.Point;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class in extends Thread{
	/*@ OVERVIEW: 处理控制台输入
	@ 表示对象：requestlist queue, map map, TaxiGUI gui;
	@ 抽象函数：AF(c) = (queue, map, gui)
			queue==c.queue, map==c.map, gui==c.gui
	@ 不变式：c.queue != null && c.map != null && c.gui != null
	 */
	
	private requestlist queue;
	private map map;
	private TaxiGUI gui;
	public in(requestlist queue, map map, TaxiGUI gui){
	/*@ REQUIRES: queue != null && m != null && gui != null
	@ MODIFIES: this.queue, this.map, this.gui;
	@ EFFECTS: 
		this.queue == queue;
		this.map == m;
		this.gui == gui;
	*/
		this.queue = queue;
		this.map = map;
		this.gui = gui;
	}
	
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(queue==null || map==null || gui==null) return false;
		return true;
	}
	
	/*public in(requestlist queue, map m){
		this.queue = queue;
		this.map = m;
	}*/
	
	public void run(){
	/*@ REQUIRES: 
	@ MODIFIES: this.map, this.queue, this.gui;
	@ EFFECTS: 
		从控制台读入字符串，分解字符串，如果是符合增减道路传入地图中操作，如果是请求创建请求对象，将创建成功的请求加入请求队列，并在gui上显示此请求;
	*/
		Scanner in = new Scanner(System.in);
		while(true){
			int count = 0;
			String s = in.nextLine();
			//System.out.println(s);
			String str =s.replace(" ", "");
			String [] strs1=str.split("[;]");
			long time = System.currentTimeMillis();
			for(int i=0;i<strs1.length;i++){
				//System.out.println(s);
				strs1[i]=strs1[i].replace(" ", "");
				String pEx ="\\[(ADD|DEL),\\([+]{0,1}\\d{1,2},[+]{0,1}\\d{1,2}\\),\\([+]{0,1}\\d{1,2},[+]{0,1}\\d{1,2}\\)\\]";
				Pattern pattern = Pattern.compile(pEx);
				Matcher matcher = pattern.matcher(strs1[i]);
				if(matcher.matches()){
					if(count == 5) continue;
					if(map.road(strs1[i])) count++;
					continue;
				}
				request rq = new request(strs1[i], map, time);
				if(!rq.success){
					continue;
				}
				gui.RequestTaxi(new Point(rq.start_pos.x,rq.start_pos.y), new Point(rq.to_pos.x,rq.to_pos.y));
				queue.addreq(rq);
				//System.out.println("to:"+rq.to_pos.x+","+rq.to_pos.y);
				
			}
		}
	}

}

