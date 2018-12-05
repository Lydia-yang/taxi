package taxi1;

import java.util.ArrayList;

import taxi1.Taxi.Taxi_state;

public class system extends Thread {
	/*@ OVERVIEW: 出租车管理系统，对于每个请求开设3秒的抢单窗口，并通知出租车抢单，关闭窗口后处理请求
	@ 表示对象：requestlist queue, Taxi taxi[], map map;
	@ 抽象函数：AF(c)=(queue, taxi, map)
			queue==c.queue, taxi==c.taxi, map==c.map
	@ 不变式：c.queue != null 
			&& c.taxi !=null && c.taxi == taxi[100] && (\all int i; 0<=i<100; c.taxi[i] != null)
			&& c.map !=null
	 */
	
	private requestlist queue;
	private Taxi taxi[];
	map map;
	public system(requestlist rq, Taxi taxi[], map m){
		/*@ REQUIRES: ：queue != null && taxi == taxi[100] && (\all int i; 0<=i<100; taxi[i] != null) && map !=null
		@ MODIFIES: this.queue, this.taxi, this.map;
		@ EFFECTS: 
			this.queue == rq;
			this.taxi == taxi;
			this.map == m;
		*/
		this.queue = rq;
		this.taxi = taxi;
		this.map = m;
	}
	
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(queue==null || taxi==null || map==null) return false;
		for(int i=0; i<100; i++){
			if(taxi[i]==null) return false;
		}
		return true;
	}
	
	public ArrayList<Taxi> gettaxi(String s){
		/*@ REQUIRES: s!=null;
		@ MODIFIES: None;
		@ EFFECTS: 
			通过输入的字符串，查找满足这种状态的出租车，将所有满足这种状态的出租车对象返回;
		 	如果字符串不是"serving","ordered","wait"和"stop"中的一个，则控制台输出错误提示并返回空;
		 	如果睡中断抛出中断异常InterruptedException，则调用e.printStackTrace()输出相应的异常信息；
		*/
		try {
			sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<Taxi> cars = new ArrayList<Taxi>();
		if(s.equals("serving")||s.equals("ordered")||s.equals("wait")||s.equals("stop")){
			for(int i=0; i<100; i++){
				//System.out.println(taxi[i].getid());
				if(taxi[i].getstate().name().equals(s)) cars.add(taxi[i]);
			}
			
		}else{
			System.out.println("input wrong state");
		}
		return cars;
	}
	public void run(){
		/*@ REQUIRES: 
		@ MODIFIES: None;
		@ EFFECTS: 
			设立指令处理线程，并不断将指令从请求队列中取出，之后开设相应的抢单窗口线程，并将该处理指令信息输入到对应的文件中；
		*/
		dealorder deal = new dealorder( taxi, map);
		 deal.start();
		requestlist rqlist = new requestlist();
		while(true){
			 rqlist = queue.getreq();
			 for(int i=0; i<rqlist.gettotal(); i++){
				 request req = rqlist.search(i);
				 main.toFile("request"+req.getid()+":[CR,"+req.start_pos.toString()+","+req.to_pos.toString()+"]\r\n", req.getid());
				 window w = new window(req, taxi, deal);
				 w.start();
			 }
			
		}
	}

}

class window extends Thread {
	/*@ OVERVIEW: 3秒的抢单窗口，通知出租车抢单，关闭后交与处理
	@ 表示对象： int total, request req, int carid[], Taxi taxi[],dealorder deal;
	@ 抽象函数：AF(c) = (req, taxi, carid, total, deal)
			req==c.req, taxi==c.taxi, carid==c.carid, total==c.total, deal==c.deal
	@ 不变式：c.req!=null && c.req.success
			&& c.taxi!=null && (\all int i; 0<=i<100; c.taxi[i] != null) 
			&& c.carid != null && c.total>=0 && (\all int i; 0<=i<total; 1<=c.carid[i]<=100)
			&& c.deal!=null
	 */
	
	private int total;
	private request req;
	private int carid[];
	private Taxi taxi[];
	private dealorder deal;
	public window(request req, Taxi taxi[], dealorder d){
		/*@ REQUIRES: req != null && req.success && && taxi!=null && (\all int i; 0<=i<100; taxi[i] != null) && d !=null
		@ MODIFIES: this.req, this.taxi, this.carid, this.total, this.deal;
		@ EFFECTS: 
			this.req == req;
			this.taxi == taxi;
			this.carid == new int[100];
			this.total == 0;
			this.deal == d;
		*/
		this.req=req;
		this.taxi=taxi;
		this.carid=new int[100];
		this.total=0;
		this.deal=d;
	}
	
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(req==null || !req.success || taxi==null || carid==null || deal==null){
			return false;
		}
		if(total<0) return false;
		for(int i=0; i<total; i++){
			if(carid[i]<1 || carid[i]>100) return false;
		}
		for(int i=0; i<100; i++){
			if(taxi[i]==null) return false;
		}
		return true;
	}
	
	public void run(){
		/*@ REQUIRES: 
		@ MODIFIES: this.carid, this.total, this.taxi;
		@ EFFECTS: 
			扫描3秒内将在请求发出地4*4范围内处于等待服务的出租车加入到抢单的队列中并将相应的出租车信用加1，将结果加入到线程deal的队列中等待处理；
			隔一定时间将所有请求4*4范围内的出租车信息输出到对应的文件中；
			如果睡中断抛出中断异常InterruptedException，则调用e.printStackTrace()输出相应的异常信息；
		*/
		int count=0;
		main.toFile("car in 4*4:\r\n", req.getid());
		while(true){
			
			for(int i=0; i<100; i++){//car total>>>>>>>>>>>>>>>>>>>>>>>>>>>>
				Taxi_state state = taxi[i].getstate();
				pos taxi_pos = taxi[i].getpos();
				//System.out.println("req:"+req.start_pos.x+","+req.start_pos.y);
				if( (taxi_pos.x <= (req.start_pos.x+2) && taxi_pos.x >= (req.start_pos.x-2)) && (taxi_pos.y <= (req.start_pos.y+2) && taxi_pos.y >= (req.start_pos.y-2))){
					if(state.name().equals("wait")){
						int judge = 0;
						for(int j=0;j<total; j++){
							if(carid[j]==i+1){
								judge = 1;
								break;
							}
						}
						if(judge==0) {
							taxi[i].addcredit();
							carid[total++] = i+1;
						}
					}
				//	System.out.println("car"+taxi[i].getid()+":("+taxi_pos.x+","+taxi_pos.y+")");
					main.toFile(taxi[i].getid()+":"+state.name()+","+taxi[i].getcredit()+"\r\n", req.getid());
				}
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count++;
			if(count == 15) break;
			
		}
		choosecar c = new choosecar(carid, total, req);
		deal.addorder(c);
		
	}
}
class choosecar{
	/*@ OVERVIEW: 存放窗口关闭后请求对应的抢单车的信息
	@ 表示对象：int carid[], int total, request req;
	@ 抽象函数：AF(c) = (carid, total, req)
			carid==c.carid, total==c.total, req==c.req
	@ 不变式：c.req != null && c.req.success 
			&& c.total>=0 && c.carid != null && (\all int i; 0<=i<c.total; 1<=c.carid[i]<=100)  
	 */
	
	int carid[];
	int total;
	request req;
	public choosecar(int carid[], int total, request req){
		/*@ REQUIRES: req != null && req.success && total>=0 && carid != null && (\all int i; 0<=i<total; 1<=carid[i]<=100)
		@ MODIFIES: this.carid, this.total, this.req;
		@ EFFECTS: 
			this.carid == carid;
			this.total == total;
			this.req == req;
		*/
		this.carid=carid;
		this.total=total;
		this.req=req;
	}
	
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(req==null || !req.success || carid==null) return false;
		if(total<0) return false;
		for(int i=0; i<total; i++){
			if(carid[i]>100||carid[i]<0) return false;
		}
		return true;
	}
}
class dealorder extends Thread{
	/*@ OVERVIEW: 处理窗口关闭后指令，在抢单出租车中选择信用高的路径最短的出租车接单。保证线程安全
	@ 表示对象：Taxi taxi[], ArrayList<choosecar> choose_car,map map;
	@ 抽象函数：AF(c)=(taxi, choose_car, map)
			taxi==c.taxi, choose_car==c.choose_car, map==c.map
	@ 不变式：c.map!=null && c.taxi!=null  && (\all int i; 0<=i<100; c.taxi[i] != null)
			&& c.choose_car != null && (\all int i; 0<=i<c.choose_car.size; c.choose_car[i]!=null)
	 */
	
	private Taxi taxi[];
	private ArrayList<choosecar> choose_car;
	private boolean full;
	map map;

	
	public dealorder(Taxi taxi[], map m){
		/*@ REQUIRES: map!=null && taxi!=null  && (\all int i; 0<=i<100; taxi[i] != null)
		@ MODIFIES: this.taxi, this.choose_car, this.full, this.map;
		@ EFFECTS: 
			this.taxi == taxi;
			this.choose_car == new ArrayList<choosecar>();
			this.full == false;
			this.map == m;
		*/
		this.taxi=taxi;
		this.choose_car=new ArrayList<choosecar>();
		this.full=false;
		this.map = m;
	}
	
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(taxi==null || choose_car==null || map==null) return false;
		for(int i=0; i<this.choose_car.size();i++){
			if(this.choose_car.get(i)==null) return false;
		}
		for(int i=0; i<100; i++){
			if(taxi[i]==null) return false;
		}
		return true;
	}
	
	public  void choose(int carid[], int total, request req){
		/*@ REQUIRES: req != null && req.success && total >= 0 && (\all int i; 0 <= i <= total;1<= carid[i] <= 100); 
		@ MODIFIES: this.taxi;
		@ EFFECTS: 
			遍历传入的抢单的汽车队列carid，处于等待服务状态中优先选择信用度最高的再选择离请求发出点最近的出租车，最后将这条指令req分配给选择的出租车并及时更改出租车状态为接单;
			将能服务的出租车id以及最后选择的出租车id输出到文件中，如果没有能够可以选择的出租车输出"no car can serve";
		*/
		int car=0;
		int maxc=0;
		int shortpath=0;
	    main.toFile("\r\ncars that can server the guest\r\n", req.getid());
		for(int i=0; i<total; i++){
			Taxi_state state = taxi[carid[i]-1].getstate();
			pos taxi_pos = taxi[carid[i]-1].getpos();
			int credit = taxi[carid[i]-1].getcredit();
			//int path = req.d[taxi_pos.x][taxi_pos.y].path;
			int path = map.shortpath(req.start_pos, taxi_pos,false).size();
			main.toFile(carid[i]+" ", req.getid());
			if(state.name().equals("wait")){
				if(car==0){
					car = carid[i];
					maxc = credit;
					shortpath=path;
				}else{
					if(maxc<credit){
						car = carid[i];
						maxc = credit;
						shortpath = path;
					}
					if(maxc==credit){
						if(shortpath>path){
							car = carid[i];
							maxc = credit;
							shortpath = path;
						}
					}
				}
			}
			//System.out.println("car"+carid[i]+":("+taxi_pos.x+","+taxi_pos.y+")");
		}
		if(car!=0){
			taxi[car-1].setreq(req);
			taxi[car-1].setstate();
			main.toFile("\r\nchoose car:"+car+"\r\n", req.getid());
		}else{
			main.toFile("\r\nno car can serve\r\n", req.getid());
			System.out.println("no car can serve");
		}
	}
	public synchronized void addorder(choosecar c){
		/*@ REQUIRES: c != null;
		@ MODIFIES: this.full, this.choose_car;
		@ EFFECTS: 
			将新的要处理的订单加入到队列choose_car中，并改变full的值唤醒等待的线程；
		@ THREAD_REQUIRES:
		@ THREAD_EFFECTS:\locked();
		*/
		choose_car.add(c);
		full=true;
		notifyAll();
	}
	public synchronized choosecar get(){
		/*@ REQUIRES: 
		@ MODIFIES: this.full, this.choose_car;
		@ EFFECTS: 
			若需要处理队列choose_car中不为空，则从队列头部取出一个需处理的抢单并从队列中移除该需处理的抢单，返回该取出的需处理的抢单；
			当需要处理队列choose_car中为空，改变this.full的值，处于等待；
			当等待中断抛出中断异常InterruptedException，则调用e.printStackTrace()输出相应的异常信息；
		@ THREAD_REQUIRES:
		@ THREAD_EFFECTS:\locked();
		*/
		if(choose_car.size()==0) full=false;
		while(full==false){
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		choosecar c=choose_car.get(0);
		choose_car.remove(c);
		return c;
	}
	public void run(){
		/*@ REQUIRES: 
		@ MODIFIES: None;
		@ EFFECTS: 
			不断的取出需处理的抢单，调用choose方法处理该抢单，处理完毕后控制台输出处理完的请求的信息；
		*/
		while(true){
			choosecar c = get();
			//ArrayList<Integer> array=shortpath(c.carid, c.total, c.req);
			choose(c.carid, c.total, c.req);
			System.out.println("req"+c.req.getid()+":"+c.req.start_pos.x+","+c.req.start_pos.y+" "+c.req.to_pos.x+","+c.req.to_pos.y);
		}
	}
}
