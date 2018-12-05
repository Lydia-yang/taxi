package taxi1;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class request{
	/*@ OVERVIEW:请客请求，包括时间，起始点，目的地等信息
	@ 表示对象：pos start_pos, pos to_pos,long time,private map p,boolean success, int id;
	@ 抽象函数：AF(c) = (start_pos, to_pos, time, p, success, id);
			start_pos==c.start_pos, to_pos==c.to_pos, time==c.time, p==c.p, success==c.success, id==c.id
	@ 不变式：(c.start_pos != null && c.to_pos != null && c.id >0) if c.success 
			&& c.p != null 
	 */
	
	pos start_pos;
	pos to_pos;
	//element[][] d=new element[80][80];
	long time;
	private map p;
	boolean success;
	private int id;
	public request(String s, map p, long time){
		/*@ REQUIRES: p != null
		@ MODIFIES: this.start_pos, this.to_pos, this.time, this.p, this.success;
		@ EFFECTS: 
			this.time == time;
			this.p == p;
			分解输入字符串，读取出请求的初始位置和目的地，放入this.start_pos和this.to_pos；
			若请求不符合格式或是出发地和目的地一样则控制台输出"wrong input", 创建不成功；
			符合请求条件创建成功this.success为true，不符合条件this.success为false；
		*/
		this.success = false;
		this.time = time;
		this.p = p;
		String str =s.replace(" ", "");
		String pEx ="\\[CR,\\([+]{0,1}\\d{1,2},[+]{0,1}\\d{1,2}\\),\\([+]{0,1}\\d{1,2},[+]{0,1}\\d{1,2}\\)\\]";
		Pattern pattern = Pattern.compile(pEx);
		Matcher matcher = pattern.matcher(str);
		if(!matcher.matches()){
			System.out.println("wrong input"+s);
		}else{
			String strs[] = str.split("[(),\\[\\]]");
			/*for(int i=0;i<strs.length;i++){
				System.out.println(i+" :"+strs[i]);
			}*/
			int x1 = Integer.parseInt(strs[3]);
			int y1 = Integer.parseInt(strs[4]);
			int x2 = Integer.parseInt(strs[7]);
			int y2 = Integer.parseInt(strs[8]);
			if(this.p.search_topn(x1, y1)==null || this.p.search_topn(x2, y2)==null || (x1==x2 && y1==y2)){
				System.out.println("wrong input");
			}else{
				start_pos = new pos(x1, y1);
				to_pos	= new pos(x2, y2);	
				//d=p.allpath(start_pos);
				this.success = true;
			}
		}
	}
	
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(this.p==null ) return false;
		if(this.success){
			if(this.start_pos==null || this.to_pos==null) return false;
			if(this.id<0) return false;
		}
		return true;
	}
	
	public boolean same(request r){
		/*@ REQUIRES: r！=null && r.success
		@ MODIFIES: None;
		@ EFFECTS: 
			判断是否是相同请求，即发出时间，出发地和目的地是否相同，是返回true，不是返回false；
		*/
		if(r.time==this.time && r.start_pos.smae(this.start_pos) && r.to_pos.smae(this.to_pos)){
			return true;
		}
		return false;
	}
	public void setid(int i){
		/*@ REQUIRES: i>0
		@ MODIFIES: this.id;
		@ EFFECTS: 
			设置请求的id；
		*/
		id = i;
	}
	public int getid(){
		/*@ REQUIRES: 
		@ MODIFIES: None;
		@ EFFECTS: 
			返回请求的id；
		*/
		return id;
	}
}
public class requestlist {
	/*@ OVERVIEW: 请求队列，保证线程安全
	@ 表示对象: ArrayList<request> rqlist, int total, int counter;
	@ 抽象函数：AF(c) =(rqlist, total, counter) ;
			rqlist==c.rqlist, total==c.total, counter==c.counter
	@ 不变式：c.rqlist != null && (\all int i; 0<=i<c.rqlist.size; c.rqlist[i]!=null && c.rqlist[i].success==true)
	 		&& c.total >= 0 && c.total > c.counter >= -1
	 */
	
	private ArrayList<request> rqlist;
	private int total;
	private int counter;
	private boolean full;
	public requestlist(){
		/*@ REQUIRES: 
		@ MODIFIES: this.counter, this.total, this.full, this.rqlist;
		@ EFFECTS: 
			this.counter == -1;
			this.total == 0;
			this.full == false;
			this.rqlist == new ArrayList<request>();
		*/
		this.counter=-1;
		this.total = 0;
		this.full = false;
		this.rqlist = new ArrayList<request>();
	}
	
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(rqlist==null) return false;
		for(int i=0; i<rqlist.size(); i++){
			if(rqlist.get(i)==null || !rqlist.get(i).success) return false;
		}
		if(total<0) return false;
		if(counter<-1 || counter>=total) return false;
		return true;
	}
	
	public synchronized void addreq(request rq){
		/*@ REQUIRES: rq!=null && rq.success;
		@ MODIFIES: this.rqlist, this.total, this.full;
		@ EFFECTS: 将指令rq加入队列rqlist中，将full改成true，唤醒等待的线程，若已存在则控制台输出报错；
		@ THREAD_REQUIRES:
		@ THREAD_EFFECTS: \locked()
		*/
		boolean issame = false;
		for(int i=0; i<total; i++){
			if(rq.same(rqlist.get(i))){
				System.out.println("wrong input");
				issame = true;
			}
		}
		if(!issame&&rq.success){
			rqlist.add(rq);
			total++;
		//	System.out.println("total:"+total+" "+rq.start_pos.x+","+rq.start_pos.y+" "+rq.to_pos.x+","+rq.to_pos.y);
			rq.setid(total);
			full=true;
			notifyAll();
		}
		
	}
	public synchronized requestlist getreq(){
		/*@ REQUIRES: 
		@ MODIFIES: this.full, this.counter;
		@ EFFECTS: 
			将新加入的所有指令取出，若队列为空则等待；
			如果等待中断抛出中断异常InterruptedException，则调用e.printStackTrace()输出相应的异常信息
		@ THREAD_REQUIRES:
		@ THREAD_EFFECTS: \locked();
		*/
		while(full==false){
			try{
				wait();
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		full=false;
			requestlist rq=new requestlist();
			while(counter<total-1){
				counter ++;
				rq.add(rqlist.get(counter));
			}
			notifyAll();
			return rq;
	}
	public int gettotal(){
		/*@ REQUIRES: 
		@ MODIFIES: None;
		@ EFFECTS: 
			返回目前为止请求队列里的所有请求个数
		*/
		return total;
	}
	public request search(int i){
		/*@ REQUIRES: i>=0 && i <= this.total
		@ MODIFIES: None;
		@ EFFECTS: 
			返回请求队列中的第i个请求；
		*/
		return rqlist.get(i);
	}
	public void add(request rq){
		/*@ REQUIRES: rq != null && rq.success
		@ MODIFIES: this.rqlist, this.total;
		@ EFFECTS: 
			将指令rq加入到请求队列rqlist中
		*/
		rqlist.add(rq);
		total++;
	}
/*	public void remove(request q){
		rqlist.remove(q);
		total --;
	}*/
	
}
