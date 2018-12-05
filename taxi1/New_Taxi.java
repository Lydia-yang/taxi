package taxi1;

import java.util.Iterator;
import java.util.Vector;

public class New_Taxi extends Taxi{
	/*@ OVERVIEW: 可追踪出租车，实现出租车的运行接单等状态，可以走关闭道路，可以用迭代器访问历史载客情况
	@ 表示对象： pos pos, Taxi_state state, int credit, request req, map map, int id,TaxiGUI gui, iterators iterator;
	@ 抽象函数：AF(c) = (pos, state, credit, req, map, id, gui, iterator)
			pos==c.pos, state==c.state, credit==c.credit, req==c.req, map==c.map, id==c.id, gui==c.gui, iterator==c.iterator
	@ 不变式：super.repOK()==true && c.count >=0
	 */
	
	int count;
	
	public New_Taxi(int x, int y, taxi1.map p, int id, TaxiGUI gui) {
		/*@ REQUIRES: 0<=x<=79 && 0<=y<=79 && map != null && 1<=id<=100 && gui !=null
		@ MODIFIES: this.pos, this.credit, this.req, this.map, this.id, this.gui, this.iterator, this.paths， this.count;
		@ EFFECTS: 
			this.pos == new pos(x, y);
			this.credit == 0;
			this.req == null;
			this.map == p;
			this.id == id;
			this.gui == gui;
			this.paths == new ArrayList<pos>();
			this.iterator == new iterators();
			this.count == 0;
			设置gui中为特殊出租车
		*/
		super(x, y, p, id, gui);
		super.gui.SetTaxiType(id, 1);
		count =0;
	}
	
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(super.repOK()==false) return false;
		if(count<0) return false;
		return true;
	}
	
	public void Serving(request req, boolean newt){
		/*@ REQUIRES: req != null && req.success;
		@ MODIFIES: this.state, this.map, this.count, this.iterator;
		@ EFFECTS: 
			选择最短且流量最小的路径，若newt==true则可以走关闭的道路否则不行，出租车从请求发出点到目的地点，此时出租车状态为服务状态；
			在行走过程中，每经过一条边增加一次这条边的流量，若路被删除，则从当前点重新规划路径；
			将走过的路径输出到对应的文件中,并将完成请求的信息加入到迭代器中;
			如果睡中断抛出中断异常InterruptedException，则调用e.printStackTrace()输出相应的异常信息;
		*/
		super.Serving(req, newt);
		count ++;
		String s = new String();
		for(int i=0;i<paths.size();i++){
			//System.out.println(paths.get(i).toString());
			s += paths.get(i).toString();
		}
		String information="car"+id+" req"+count+": [CR,"+req.start_pos.toString()+","+req.to_pos.toString()+"]\n"+s+"\n";
		iterator.add(information);
		//System.out.println(information);
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
				Ordered(super.pos, req.start_pos,true);
				Stop();
				Serving(req,true);
				Stop();
				super.credit += 3;
				req=null;
				paths.removeAll(paths);
			}
		}
	}


}

class iterators implements Iterator<String>{
	/*@ OVERVIEW: 双向迭代器
	@ 表示对象：Vector<String> inlist， int i=0;
	@ 抽象函数：AF(c) = (inlist, i) inlist==c.inlist, i==c.i
	@ 不变式：i>=0 && inlist!=null
	 */
	Vector<String> inlist;
	 private int i=0;
	
	public iterators(){
		/*@ REQUIRES: 
		@ MODIFIES: this.inlist;
		@ EFFECTS: 
			this.inlist == new Vector<String>();
		*/
		this.inlist=new Vector<String>();
	}
	
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(i<0) return false;
		if(inlist==null) return false;
		return true;
	}
	
	public void add(String f){
		/*@ REQUIRES: 
		@ MODIFIES: this.inlist
		@ EFFECTS: 
			将传入的字符串加入到inlist中
		*/
		inlist.add(f);
	}
	
	
	public boolean hasNext(){
		/*@ REQUIRES: 
		@ MODIFIES: None;
		@ EFFECTS: 
			判断下一个元素是否存在；
		*/
	
		  if(i<inlist.size()){
			  return true;
		  } else{
			  return false;
		  }
	 }
	
	public String next(){
		/*@ REQUIRES: 
		@ MODIFIES: this;
		@ EFFECTS: 
			获得inlist中的下一个元素；
		*/
	
		return inlist.get(i++);
	
	}

    public String previous(){
    	/*@ REQUIRES: 
		@ MODIFIES: this;
		@ EFFECTS: 
			获得inlist中的前一个元素；
		*/
    	return inlist.get(--i);
    }
	    
    public boolean hasPrevious(){
    	/*@ REQUIRES: 
		@ MODIFIES: None;
		@ EFFECTS: 
			判断前一个元素是否存在；
		*/
    	if(i>0){
			  return true;
		  } else{
			  return false;
		  }
    }
		
}


	

