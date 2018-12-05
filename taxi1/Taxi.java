package taxi1;

import java.awt.Point;
import java.util.ArrayList;
import taxi1.nodes.light_state;

public class Taxi extends Thread{
	/*@ OVERVIEW: 出租车，实现出租车的运行接单等状态
	@ 表示对象： pos pos, Taxi_state state, int credit, request req, map map, int id,TaxiGUI gui, iterators iterator;
	@ 抽象函数：AF(c) = (pos, state, credit, req, map, id, gui, iterator)
			pos==c.pos, state==c.state, credit==c.credit, req==c.req, map==c.map, id==c.id, gui==c.gui, iterator==c.iterator
	@ 不变式：c.pos!=null && (c.state==Taxi_state.serving || c.state==Taxi_state.ordered || c.state==Taxi_state.wait || c.state==Taxi_state.stop)
			&& c.credit>0 && (c.req != null && c.req.success) if(c.state==Taxi_state.serving || c.state==Taxi_state.ordered)
			&& c.map != null && 1<=c.id<=100 && c.gui != null && c.iterator != null
	 */
	
	public enum Taxi_state{
		serving, ordered, wait, stop;
	}
	protected pos pos;
	private Taxi_state state;
	protected int credit;
	request req;
	protected int id;
	protected TaxiGUI gui;
	map map;
	iterators iterator;
	//Iterator<String> iterator;
	ArrayList<pos> paths;
	/*public Taxi(int x, int y, map p, int id){
		this.pos=new pos(x, y);
		this.credit=0;
		this.req=null;
		this.map=p;
		this.id=id;
	}*/
	public Taxi(int x, int y, map p, int id, TaxiGUI gui){
		/*@ REQUIRES: 0<=x<=79 && 0<=y<=79 && map != null && 1<=id<=100 && gui !=null
		@ MODIFIES: this.pos, this.credit, this.req, this.map, this.id, this.gui, this.iterator, this.paths;
		@ EFFECTS: 
			this.pos == new pos(x, y);
			this.credit == 0;
			this.req == null;
			this.map == p;
			this.id == id;
			this.gui == gui;
			this.paths == new ArrayList<pos>();
			this.iterator == new iterators();
			设置gui中为普通出租车
		*/
		this.pos=new pos(x, y);
		this.credit=0;
		this.req=null;
		this.map=p;
		this.id=id;
		this.gui=gui;
		this.paths=new ArrayList<pos>();
		this.gui.SetTaxiType(id, 0);
		this.iterator=new iterators();
		//this.iterator= f.iterator();
	}
	
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(pos==null) return false;
		if(map==null) return false;
		if(gui==null) return false;
		if(iterator==null) return false;
		if(credit<0) return false;
		if(id<=0 || id>100) return false;
		if(state!=Taxi_state.serving && state!=Taxi_state.ordered && state!=Taxi_state.wait && state!=Taxi_state.stop) return false;
		if(state==Taxi_state.serving || state==Taxi_state.ordered){
			if(req==null || !req.success) return false;
		}
		return true;
		
	}
	public void Stop(){
		/*@ REQUIRES: 
		@ MODIFIES: this.state;
		@ EFFECTS: 
			出租车在此点停一秒，此时的出租车状态为"stop";
			如果睡中断抛出中断异常InterruptedException，则调用e.printStackTrace()输出相应的异常信息；
		*/
		state=Taxi_state.stop;
		gui.SetTaxiStatus(id, new Point(pos.x,pos.y), 0);
		try {
			sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*public void going(pos now, pos req_start, boolean out){
		//ArrayList<pos> path= map.shortpath(now, to);
		//System.out.println("path;"+path.size());
		ArrayList<pos> path = new ArrayList<pos>();
		element find=req.d[now.x][now.y];
		while(find!=null){
			path.add(find.now.p);
			find=find.parent;
		}
		if(!out){
			for(int i=1; i<path.size(); i++){
				try {
					sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				this.pos.x=path.get(i).x;
				this.pos.y=path.get(i).y;
				gui.SetTaxiStatus(id, new Point(pos.x,pos.y), 3);
			//	System.out.println("order:"+pos.x+","+pos.y);
				
					main.toFile("-->("+pos.x+","+pos.y+")", req.getid());
				
			}
		}else{
			for(int i=path.size()-2; i>=0; i--){
				try {
					sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				this.pos.x=path.get(i).x;
				this.pos.y=path.get(i).y;
				gui.SetTaxiStatus(id, new Point(pos.x,pos.y), 1);
			//	System.out.println("order:"+pos.x+","+pos.y);
				
					main.toFile("-->("+pos.x+","+pos.y+")", req.getid());
				
			}
		}
		
	}*/
	public void Ordered(pos now, pos s, boolean newt){
		/*@ REQUIRES: contains(now).map && contains(s).map;
		@ MODIFIES: this.state, this.map;
		@ EFFECTS: 
			选择最短且流量最小的路径，若newt==true则可以走关闭的道路否则不行，出租车从当前点到请求发出点，此时出租车状态为接单状态；
			在行走过程中，每经过一条边增加一次这条边的流量，若路被删除，则从当前点重新规划路径；
			将走过的路径输出到对应的文件中；
			如果睡中断抛出中断异常InterruptedException，则调用e.printStackTrace()输出相应的异常信息;
		*/
		state=Taxi_state.ordered;
		main.toFile("go serving:\r\n"+"("+pos.x+","+pos.y+")", req.getid());
		pos p0 = new pos(this.pos.x, this.pos.y);
		this.paths.add(p0);
		//System.out.println(this.pos.toString());
		//going(now, s, false);
		//int status;
		//if(newt) status=1;
		//else status=0;
		ArrayList<pos> path= map.shortpath(now, s, newt);
		nodes p = null;
		for(int i=0; i<path.size(); i++){
			int judge = 0;
			nodes t = map.search_topn(path.get(i).x, path.get(i).y);
			for(int j=0; j<t.nodes.size(); j++){
				if(t.nodes.get(j).p.smae(this.pos)){
					judge=1;
					break;
				}
			}
			if((t==null || judge == 0) && !newt){
				path=map.shortpath(now, s, newt);
				i=-1;
				continue;
			}else{
				map.setflow(this.pos, path.get(i));
			}
			nodes n = map.search_topn(this.pos.x, this.pos.y);
			if(n.light && p!=null){
				while((p.p.x==n.p.x && p.p.x==t.p.x && n.green==light_state.UD) || (p.p.y==n.p.y && p.p.y==t.p.y && n.green==light_state.LR) || (n.p.y==p.p.y && n.p.x==p.p.x-1 && n.p.x==t.p.x && n.p.y==t.p.y+1 && n.green==light_state.UD) || (n.p.y==p.p.y && n.p.x==p.p.x+1 && n.p.x==t.p.x && n.p.y==t.p.y-1 && n.green==light_state.UD) || (n.p.x==p.p.x && n.p.y==p.p.y-1 && n.p.y==t.p.y && n.p.x==t.p.x-1 && n.green==light_state.LR) || (n.p.x==p.p.x && n.p.y==p.p.y+1 && n.p.y==t.p.y && n.p.x==t.p.x+1 && n.green==light_state.LR)){
					try {
						sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//System.out.println("1");
				}
			}
			
			try {
				sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			p = map.search_topn(this.pos.x, this.pos.y);
			this.pos.x=path.get(i).x;
			this.pos.y=path.get(i).y;
			gui.SetTaxiStatus(id, new Point(pos.x,pos.y), 3);
		//	System.out.println("order:"+pos.x+","+pos.y);
			
				main.toFile("-->("+pos.x+","+pos.y+")", req.getid());
				pos p1 = new pos(this.pos.x, this.pos.y);
				this.paths.add(p1);
				//System.out.println(this.pos.toString());
			
		}
		main.toFile("\r\n", req.getid());
	}
	public void Serving(request req, boolean newt){
		/*@ REQUIRES: req != null && req.success;
		@ MODIFIES: this.state, this.map;
		@ EFFECTS: 
			选择最短且流量最小的路径，若newt==true则可以走关闭的道路否则不行，出租车从请求发出点到目的地点，此时出租车状态为服务状态；
			在行走过程中，每经过一条边增加一次这条边的流量，若路被删除，则从当前点重新规划路径；
			将走过的路径输出到对应的文件中;
			如果睡中断抛出中断异常InterruptedException，则调用e.printStackTrace()输出相应的异常信息;
		*/
		state=Taxi_state.serving;
		main.toFile("start serving:\r\n"+"("+pos.x+","+pos.y+")", req.getid());
		pos p0 = new pos(this.pos.x, this.pos.y);
		this.paths.add(p0);
		//System.out.println(this.pos.toString());
	//	System.out.println("req:"+req.start_pos.x+","+req.start_pos.y+"  "+req.to_pos.x+","+req.to_pos.y);
		//going(req.to_pos,req.start_pos ,true);
		//int status;
		//if(newt) status=1;
		//else status=0;
		ArrayList<pos> path= map.shortpath(req.start_pos, req.to_pos, newt);
		nodes p = null;
		for(int i=0; i<path.size(); i++){
			int judge = 0;
			nodes t = map.search_topn(path.get(i).x, path.get(i).y);
			for(int j=0; j<t.nodes.size(); j++){
				if(t.nodes.get(j).p.smae(this.pos)){
					judge=1;
					break;
				}
			}
			//System.out.println("order:"+pos.x+","+pos.y);
			if((t==null || judge == 0) && !newt){
				path=map.shortpath(this.pos, req.to_pos, newt);
				//System.out.println("path:"+path.size()+" pos:"+pos.toString());
				/*for(int k=0; k<path.size();k++){
					System.out.println("pos"+k+":"+path.get(k).toString());
				}*/
				i=-1;
				continue;
			}else{
				map.setflow(this.pos, path.get(i));
			}
			
			nodes n = map.search_topn(this.pos.x, this.pos.y);
			
			if(n.light  && p!=null){
				while((p.p.x==n.p.x && p.p.x==t.p.x && n.green==light_state.UD) || (p.p.y==n.p.y && p.p.y==t.p.y && n.green==light_state.LR) || (n.p.y==p.p.y && n.p.x==p.p.x-1 && n.p.x==t.p.x && n.p.y==t.p.y+1 && n.green==light_state.UD) || (n.p.y==p.p.y && n.p.x==p.p.x+1 && n.p.x==t.p.x && n.p.y==t.p.y-1 && n.green==light_state.UD) || (n.p.x==p.p.x && n.p.y==p.p.y-1 && n.p.y==t.p.y && n.p.x==t.p.x-1 && n.green==light_state.LR) || (n.p.x==p.p.x && n.p.y==p.p.y+1 && n.p.y==t.p.y && n.p.x==t.p.x+1 && n.green==light_state.LR)){
					try {
						sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				//	System.out.println("1");
				}
			}
			
			try {
				sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			p = map.search_topn(this.pos.x, this.pos.y);
			this.pos.x=path.get(i).x;
			this.pos.y=path.get(i).y;
			gui.SetTaxiStatus(id, new Point(pos.x,pos.y), 1);
			//System.out.println("order:"+pos.x+","+pos.y);
			
				main.toFile("-->("+pos.x+","+pos.y+")", req.getid());
				pos p1 = new pos(this.pos.x, this.pos.y);
				this.paths.add(p1);
				//System.out.println(this.pos.toString());
			
		}
		main.toFile("\r\n", req.getid());
	}
	public void Wait(){
		/*@ REQUIRES: 
		@ MODIFIES: this.state, this.map;
		@ EFFECTS: 
			随意选择流量小的边走，每走过边设置一次这条边流量，此时出租车处于等待服务状态，没有接到单时处于这个状态20s；
			如果睡中断抛出中断异常InterruptedException，则调用e.printStackTrace()输出相应的异常信息;
		*/
		state=Taxi_state.wait;
		int count=0;
		nodes n=map.search_topn(pos.x, pos.y);
		nodes p = null;
		while(count!=100 && req==null){
			//System.out.println(id+" "+pos.x+" "+pos.y);
			gui.SetTaxiStatus(id, new Point(pos.x,pos.y), 2);
			//System.out.println(id+": ("+pos.x+","+pos.y+") ");
			
			int min=0;
			ArrayList<Integer> m = new ArrayList<Integer>();
			//m.add(min);
			for(int i=1; i<n.nodes.size();i++){
				if( n.flow.get(i)<=n.flow.get(min)){
					/*if(n.flow.get(i)==n.flow.get(min)){
						m.add(i);
					}*/
					min = i;
				}
			}
			for(int i=0; i<n.nodes.size();i++){
				
					if(n.flow.get(i)==n.flow.get(min)){
						m.add(i);
					}
					
				
			}
			//if(n.flow.get(0)==n.flow.get(min)) m.add(0);
			//int random = (int) (Math.random()*10000)%n.nodes.size();
			//System.out.println("size:"+m.size());
			int random = (int) (Math.random()*10000)%m.size();
			//nodes pre = n;
		    n=n.nodes.get(m.get(random));
			map.setflow(this.pos, n.p);
			
			nodes now = map.search_topn(this.pos.x, this.pos.y);
			
			if(now.light && p!=null){
				while((p.p.x==now.p.x && p.p.x==n.p.x && now.green==light_state.UD) || (p.p.y==now.p.y && p.p.y==n.p.y && now.green==light_state.LR) || (now.p.y==p.p.y && now.p.x==p.p.x-1 && now.p.x==n.p.x && now.p.y==n.p.y+1 && now.green==light_state.UD) || (now.p.y==p.p.y && now.p.x==p.p.x+1 && now.p.x==n.p.x && now.p.y==n.p.y-1 && now.green==light_state.UD) || (now.p.x==p.p.x && now.p.y==p.p.y-1 && now.p.y==n.p.y && now.p.x==n.p.x-1 && now.green==light_state.LR) || (now.p.x==p.p.x && now.p.y==p.p.y+1 && now.p.y==n.p.y && now.p.x==n.p.x+1 && now.green==light_state.LR)){
					try {
						sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//System.out.println("1");
				}
			}
			
			try {
				sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			p = map.search_topn(this.pos.x, this.pos.y);
			pos.x=n.p.x;
			pos.y=n.p.y;
			count++;
			
		}
	}
	public void run(){
		/*@ REQUIRES: 
		@ MODIFIES: this.credit, this.req, this.path;
		@ EFFECTS: 
			按照没有接到单时处于服务状态20s停1s，接到单时先去接乘客停1s后去往目的地停1s，完成后信用加3，再次处于未接单状态；
		*/
		while(true){
			if(req==null){
				Wait();
				if(req==null) Stop();
			}
			if(req!=null){
				Ordered(this.pos, req.start_pos,false);
				Stop();
				Serving(req,false);
				Stop();
				this.credit += 3;
				req=null;
				paths.removeAll(paths);
			}
		}
	}
	public  Taxi_state getstate(){
		/*@ REQUIRES: 
		@ MODIFIES: None;
		@ EFFECTS: 
			返回当前出租车所处的状态
		*/
		return state;
	}
	public  pos getpos(){
		/*@ REQUIRES: 
		@ MODIFIES: None;
		@ EFFECTS: 
			返回当前出租车的位置
		*/
		return pos;
	}
	public  int getcredit(){
		/*@ REQUIRES: 
		@ MODIFIES: None;
		@ EFFECTS: 
			返回当前出租车的信用
		*/
		return credit;
	}
	public  int getid(){
		/*@ REQUIRES: 
		@ MODIFIES: None;
		@ EFFECTS: 
			返回出租车的id
		*/
		return id;
	}
	public  void setreq(request req){
		/*@ REQUIRES: req != null && req.success;
		@ MODIFIES: this.req;
		@ EFFECTS: 
			this.req == req;
		*/
		this.req = req;
	}
	public  void setstate(){
		/*@ REQUIRES: 
		@ MODIFIES: this.state;
		@ EFFECTS: 
			更改出租车的状态为接单状态；
		*/
		state=Taxi_state.ordered;
	}
	public  void addcredit(){
		/*@ REQUIRES: 
		@ MODIFIES: this.credit;
		@ EFFECTS: 
			将当前出租车的信用加1；
		*/
		credit++;
	}
	public  String toString(){
		/*@ REQUIRES: 
		@ MODIFIES: None;
		@ EFFECTS: 
			返回当前出租车的id和位置信息以及调用时的系统时间；
		*/
		return System.currentTimeMillis()+": car"+id+" ("+pos.x+","+pos.y+")";
	}
}
