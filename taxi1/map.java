package taxi1;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class pos{
	/*@ OVERVIEW:位置信息
	@ 表示对象：int x, int y;
	@ 抽象函数：AF(c) = (x, y), x==c.x, y==c.y
	@ 不变式： 0 <= c.x <=79 && 0 <= c.y <= 79
	 */
	int x;
	int y;
	
	public pos(int x, int y){
		/*@ REQUIRES: 0 <= x <=79 && 0 <= y <= 79
		@ MODIFIES: this.x, this.y;
		@ EFFECTS: 
			this.x == x;
			this.y == y;
		*/
		this.x=x;
		this.y=y;
	}
	
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(this.x<0 || this.x>79 || this.y<0 || this.y>79) return false;
		return true;
	}
	
	public boolean smae(pos p){
		/*@ REQUIRES: p != null
		@ MODIFIES: None;
		@ EFFECTS: 
			比较这个点与传入的点坐标x,y是否相同，相同返回true，不相同返回false
		*/
		if(this.x==p.x && this.y==p.y){
			return true;
		}
		return false;
	}
	public String toString(){
		/*@ REQUIRES: 
		@ MODIFIES: None;
		@ EFFECTS: 
			返回该点的坐标信息
		*/
		return "("+x+","+y+")";
	}
}
class nodes{
	/*@ OVERVIEW:地图节点信息用来构成邻接表的点
	@ 表示对象：light_state green, boolean light, int time, pos p, ArrayList<nodes> nodes, ArrayList<Integer> flow;
	@ 抽象函数：AF(c) = (light, green, time, p, nodes, flow),
	 		light == c.light, green == c.green, time == c.time, p == c.p, nodes == c.nodes, flow == c.flow
	@ 不变式：c.p !=null && 0<=c.p.x<=79 && 0<=c.p.y<=79
			&& c.nodes != null && (\all int i; 0<=i<c.nodes.size; c.nodes[i]!=null)
			&& c.flow != null && (\all int i; 0<=i<c.flow.size; c.flow[i]>=0)
			&& 200<=c.time<=500 if c.light==true 
			&& (c.green==light_state.LR || c.green==light_state.UD) if c.light==true
	 */
	
	public enum light_state{
		UD, LR;
	}
	light_state green;
	boolean light;
	int time;
	pos p;
	ArrayList<nodes> nodes;
	ArrayList<Integer> flow;
	public nodes(int x, int y){
		/*@ REQUIRES: 0<=x<=79 && 0<=y<=79
		@ MODIFIES: this.p, this.nodes, this.flow, this.light, this.green, this.time;
		@ EFFECTS: 
			this.p == new pos(x, y);
			this.nodes == new ArrayList<nodes>();
			this.flow == new ArrayList<Integer>();
			this.light == false;
			this.green == null;
			this.time == 0;
		*/
		p=new pos(x, y);
		nodes=new ArrayList<nodes>();
		flow=new ArrayList<Integer>();
		this.light = false;
		green = null;
		time = 0;
	}
	
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(p==null) return false;
		if(p.x > 79 || p.x <0) return false;
		if(p.y > 79 || p.y <0) return false;
		if(nodes==null||flow==null) return false;
		for(int i=0; i<nodes.size();i++){
			if(nodes.get(i)==null) return false;
		}
		for(int i=0; i<flow.size(); i++){
			if(flow.get(i)<0) return false;
		}
		if(light){
			if(time<200 || time >500) return false;
			if((green!=light_state.LR && green!=light_state.UD)) return false;
		}
		return true;
	}
	
	public void addnode(nodes n){
		/*@ REQUIRES: n != null;
		@ MODIFIES: this.nodes, this.flow;
		@ EFFECTS: 
			增加一个邻接的点并初始化这条边的流量;
		*/
		nodes.add(n);
		flow.add(0);
	}
	public void del(nodes n){
		/*@ REQUIRES: contains(n).nodes
		@ MODIFIES: this.nodes, this.flow;
		@ EFFECTS: 
			删除对应的邻接点n及其流量;
		*/
		flow.remove(nodes.indexOf(n));
		nodes.remove(n);
		
	}
	public void setlight(){
		/*@ REQUIRES: 
		@ MODIFIES: this.light, this.green, this.time;
		@ EFFECTS: 
			设置该点的红路灯，东西方向为绿，间隔时间为50-100的一个数
		*/
		light = true;
		green = light_state.LR;
		time = (int) ((Math.random()*100)%301 + 200);
		//time = 10000;
	}
}
public class map {
	/*@ OVERVIEW:用来存放地图的信息，每个节点边流量红绿灯等
	@ 表示对象：ArrayList<nodes> topnode, TaxiGUI gui;
	@ 抽象函数：AF(c) = (topnode, gui), topnode==c.topnode, gui==c.gui
	@ 不变式：c.gui != null && c.topnode != null && (\all int i; 0<=i<c.topnode.size; c.topnode[i]!=null)
	 */
	
	ArrayList<nodes> topnode;
	TaxiGUI gui;
	map p;
	public map(File f, TaxiGUI gui){
		/*@ REQUIRES: f!=null && f.exit && gui != null
		@ MODIFIES: this.gui, this.topnode;
		@ EFFECTS: 
			this.gui == gui;
			地图格式正确，构造邻接链表，所有点存放在topnode中；
			当地图格式不满足80*80，或者出现非法字符或边界点连接出界等非法情况控制台输出"INVAILD INPUT"报错；
			文件读取异常时抛出异常IOEception,调用 e.printStackTrace()打印相应异常信息；
		*/
		this.gui = gui;
		topnode=new ArrayList<nodes>();
		 BufferedReader reader = null;
		    try {
	            reader = new BufferedReader(new FileReader(f));
	            String tempString = null;
	            int line = 0;
	            while ((tempString = reader.readLine()) != null) {
	               if(line>80){
	            	   System.out.println("INVAILD INPUT");
	            	   System.exit(0);
	               }
	               if(!add(tempString, line)){
	            	   System.out.println("INVAILD INPUT");
	            	   System.exit(0);
	               }
	               line ++;
	            }
	            if(line!=80){
	            	System.out.println("INVAILD INPUT");
	                System.exit(0);
	            }
	            reader.close();
	            System.out.println("node:"+this.topnode.size());
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                }
	            }
	        }
		    
	}
	public void setp(File f,TaxiGUI gui){
		/*@ REQUIRES:  f!=null && f.exit && gui != null
		@ MODIFIES: this.p;
		@ EFFECTS: 
			备份一份当前地图
		*/
		p=new map(f,gui);
	}
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(gui==null || this.topnode==null) return false;
		for(int i=0;i<this.topnode.size();i++){
			if(this.topnode.get(i)==null) return false;
		}
		return true;
	}
	
	nodes search_topn(int x, int y){
		/*@ REQUIRES: 
		@ MODIFIES: None;
		@ EFFECTS: 
			寻找地图上的节点，找到返回该节点，没找到返回null
		*/
		nodes n = null;
		for(int i=0;i<topnode.size();i++){
			if(topnode.get(i).p.x==x && topnode.get(i).p.y==y){
				n = topnode.get(i);
				break;
			}
		}
		return n;
	}
	boolean add(String s, int line){
		/*@ REQUIRES: s != null && 0<= line <=79
		@ MODIFIES: this.topnode;
		@ EFFECTS: 
			将传入的字符串遍历，并存放该行相应的节点，构造邻接链表，成功返回true;
			若出现非法字符或是边界点出现邻接越界则返回false;
		*/
		s = s.replace(" ", "");
		s = s.replace("\t", "");
		if(s.length()!=80) return false;
		for(int j=0;j<s.length();j++){
			char c = s.charAt(j);
			if(c!='0' && c!='1' && c!='2' && c!='3'){
				return false;
			}
			if(line==79 && (c=='2' || c=='3')) return false;
			if(line==79 && j==79 && c!='0') return false;
			if(j==79 && (c=='1' || c=='3')) return false;
			nodes n = search_topn(line, j);
			if(n==null){
				n = new nodes(line, j);
				topnode.add(n);
			}
			if(c=='0'){
				
			}else if(c=='1'){
				nodes down = search_topn(line, j+1);
				if(down==null){
					down = new nodes(line, j+1);
					topnode.add(down);
				}
				n.addnode(down);
				down.addnode(n);
			}else if(c=='2'){
				nodes right = search_topn(line+1, j);
				if(right==null){
					right=new nodes(line+1, j);
					topnode.add(right);
				}
				n.addnode(right);
				right.addnode(n);
			}else{
				nodes down = search_topn(line, j+1);
				if(down==null){
					down = new nodes(line, j+1);
					topnode.add(down);
				}
				n.addnode(down);
				down.addnode(n);
				nodes right = search_topn(line+1, j);
				if(right==null){
					right=new nodes(line+1, j);
					topnode.add(right);
				}
				n.addnode(right);
				right.addnode(n);
			}
			if(n.nodes.size()==0) topnode.remove(n);
		}
		return true;
	}
	
	 ArrayList<pos> shortpath(pos s, pos end, boolean status){
		 /*@ REQUIRES: contains(s).map && contains(end).map && (status==1 || status==0) && this.p!=null;
		@ MODIFIES: None;
		@ EFFECTS: 
			计算两点间最短且流量最小的路径，若status==true则可以走关闭的边，否则不行，将路径结果存放在shortpath中，并返回，若起始点s和终点end一样，则返回空；
		*/
		ArrayList<pos> path = new ArrayList<pos>();
		ArrayList<pos> shortpath = new ArrayList<pos>();
		if(s.smae(end)) return shortpath;
		if(status){
			 return p.shortpath(s, end, false);
		}
		//int visit[] = new int[topnode.size()];
		nodes begin = search_topn(s.x, s.y);
		element b = new element(null, begin,0);
		//DFS(begin, path, shortpath, end, visit);
		ArrayList<element> q = new ArrayList<element>();
		ArrayList<element> visit = new ArrayList<element>();
		q.add(b);
		element find = null;
		//System.out.println("s:"+s.toString()+"e:"+end.toString());
		while(!q.isEmpty()){
			element top = q.get(0);
			q.remove(0);
			//System.out.println("s:"+top.now.p.toString());
			if(top.now.p.smae(end)) {
				find = top;
				break;
			}
			visit.add(top);
			for(int j=0; j<top.now.nodes.size(); j++){
				int judge = 0;
				for(int k=0; k<visit.size();k++){
					if(visit.get(k).now.p.smae(top.now.nodes.get(j).p)){
						judge=1;
						break;
					}
				}
				for(int k=0; k<q.size(); k++){
					if(q.get(k).now.p.smae(top.now.nodes.get(j).p)){
						element newe = new element(top, top.now.nodes.get(j),top.path+1);
						if(newe.path<=top.path && newe.parent.now.flow.get(newe.parent.now.nodes.indexOf(newe.now)) <= q.get(k).parent.now.flow.get(q.get(k).parent.now.nodes.indexOf(q.get(k).now))){
							q.remove(k);
							q.add(newe);
						}
						judge=1;
						break;
					}
				}
				if(judge==0){
					element new_element = new element(top, top.now.nodes.get(j),top.path+1);
					q.add(new_element);
					//System.out.println("s:"+new_element.now.p.toString());
				}
			}
		}
		while(find.parent!=null){
			path.add(find.now.p);
			find = find.parent;
		}
		for(int i=path.size()-1;i>=0;i--){
			shortpath.add(path.get(i));
			//System.out.println(path.get(i).x+","+path.get(i).y);
		}
		return shortpath;
	}
	/*element[][] allpath(pos s){
		//int count=0;
		element [][]d = new element[80][80];
		ArrayList<element> q = new ArrayList<element>();
		ArrayList<element> visit = new ArrayList<element>();
		nodes begin = search_topn(s.x, s.y);
		element b = new element(null, begin,0);
		q.add(b);
		while(!q.isEmpty()){
			//System.out.println(visit.size());
			element top = q.get(0);
			q.remove(0);
			visit.add(top);
			//System.out.println(top.now.p.x+","+top.now.p.y);
			d[top.now.p.x][top.now.p.y]=top;
			for(int j=0; j<top.now.nodes.size(); j++){
				int judge = 0;
				for(int k=0; k<visit.size();k++){
					if(visit.get(k).now.p.smae(top.now.nodes.get(j).p)){
						judge=1;
						break;
					}
				}
			
				for(int k=0; k<q.size(); k++){
					if(q.get(k).now.p.smae(top.now.nodes.get(j).p)){
						judge=1;
						break;
					}
				}
				if(judge==0){
					//count++;
					//System.out.println(count);
					element new_element = new element(top, top.now.nodes.get(j),top.path+1);
					q.add(new_element);
					
				}
			}
		}
		return d;
	}*/
	public void setflow(pos s, pos e){
		/*@ REQUIRES: contains(s).map && contains(e).map;
		@ MODIFIES: flow;
		@ EFFECTS: 
			增加一次点s和e之间边的流量，若两点之间无边则不改变；
		*/
		nodes S = search_topn(s.x, s.y);
		nodes E = search_topn(e.x, e.y);
		for(int i=0; i<S.nodes.size(); i++){
			if(S.nodes.get(i).p.smae(E.p)){
				S.flow.set(i, S.flow.get(i)+1);
				break;
			}
		}
		for(int i=0; i<E.nodes.size(); i++){
			if(E.nodes.get(i).p.smae(S.p)){
				E.flow.set(i, E.flow.get(i)+1);
				break;
			}
		}
	}
	public void clean(){
		/*@ REQUIRES: 
		@ MODIFIES: flow;
		@ EFFECTS: 
			将地图中所有边的流量清零；
		*/
		for(int i=0; i<topnode.size();i++){
			nodes n = topnode.get(i);
			for(int j=0; j<n.flow.size();j++){
			//	System.out.println("flow:"+n.flow.get(j));
				n.flow.set(j, 0);
			}
		}
	}
	public boolean road(String s){
		/*@ REQUIRES: s != null && this.p!=null;
		@ MODIFIES: this.topnode, this.p;
		@ EFFECTS: 
			若传入的字符串是符合要求的增减边要求，则实现相应的增减边，返回true；
			若传入的点不存在或是两点相同，或者要增加的边已存在删减的边不存在，或者删减时有车在道路上,或者增加的边不是相邻的两个点，控制台输出"wrong input", 返回false；
		*/
		String strs[] = s.split("[(),\\[\\]]");
		int x1 = Integer.parseInt(strs[3]);
		int y1 = Integer.parseInt(strs[4]);
		int x2 = Integer.parseInt(strs[7]);
		int y2 = Integer.parseInt(strs[8]);
		nodes n1 = search_topn(x1, y1);
		nodes n2 = search_topn(x2, y2);
		if(n1==null || n2==null || (x1==x2 && y1==y2)){
			System.out.println("wrong input:"+s);
			return false;
		}
		
		if(strs[1].equals("ADD")){
			for(int i=0;i<n1.nodes.size();i++){
				if(n2.p.smae(n1.nodes.get(i).p)){
					System.out.println("wrong input:"+s);
					return false;
				}
			}
			if(!((x1==x2-1 && y1==y2) || (x1==x2+1 && y1==y2) || (x1==x2 && y1==y2+1) || (x1==x2 && y1==y2-1)) ){
				System.out.println("wrong input:"+s);
				return false;
			}
			n1.addnode(n2);
			n2.addnode(n1);
		//	p.road(s);
			gui.SetRoadStatus(new Point(n1.p.x, n1.p.y), new Point(n2.p.x, n2.p.y), 1);
		}else{
			int judge=0;
			for(int i=0;i<n1.nodes.size();i++){
				if(n2.p.smae(n1.nodes.get(i).p) && n1.flow.get(i)==0){
					judge=1;
					break;
				}
			}
			if(judge==1){
				n1.del(n2);
				n2.del(n1);
				
				gui.SetRoadStatus(new Point(n1.p.x, n1.p.y), new Point(n2.p.x, n2.p.y), 0);
			}else{
				System.out.println("wrong input:"+s);
				return false;
			}
		}
		return true;
	}
	public boolean setlight(File f){
		/*@ REQUIRES: f!=null && f.exit
		@ MODIFIES:  this.topnode;
		@ EFFECTS: 
			this.gui == gui;
			地图格式正确，设置红绿灯；
			当地图格式不满足80*80，或者出现非法字符等非法情况控制台输出"INVAILD INPUT"报错；
			文件读取异常时抛出异常IOEception,调用 e.printStackTrace()打印相应异常信息；
		*/
		BufferedReader reader = null;
	    try {
            reader = new BufferedReader(new FileReader(f));
            String tempString = null;
            int line = 0;
            while ((tempString = reader.readLine()) != null) {
            	if(line>80){
	            	   System.out.println("INVAILD INPUT");
	            	   System.exit(0);
	            	   return false;
	               }
	               if(!addlight(tempString, line)){
	            	   System.out.println("INVAILD INPUT");
	            	   System.exit(0);
	            	   return false;
	               }
	               line ++;
            }
            if(line!=80){
            	System.out.println("INVAILD INPUT");
                System.exit(0);
                return false;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
	    return true;
	}
	public boolean addlight(String s, int line){
		/*@ REQUIRES: s != null && 0<= line <=79
		@ MODIFIES: this.topnode;
		@ EFFECTS: 
			将传入的字符串遍历，并将满足条件的节点设置红绿灯，不满足控制台输出报错并忽略，成功返回true;
			若出现非法字符或是出现多于80的字符则返回false;
		*/
		s = s.replace(" ", "");
		s = s.replace("\t", "");
		if(s.length()!=80) return false;
		for(int j=0;j<s.length();j++){
			char c = s.charAt(j);
			if(c!='0' && c!='1'){
				return false;
				
			}
			if(c=='1'){
				nodes n = this.search_topn(line, j);
				if(n.nodes.size()!=3 && n.nodes.size()!=4) {
					//return false;
					System.out.println("INVAILD INPUT");
					continue;
				}
				n.setlight();
				if(n.light) gui.SetLightStatus(new Point(n.p.x, n.p.y), 1);
				Light_control control = new Light_control(n, gui);
				control.start();
			}
		}
		return true;
			
	}
	
}
class element{
	/*@ OVERVIEW:在遍历时用来存放上个节点及路径长短的元素
	@ 表示对象：element parent, nodes now, int path;
	@ 抽象函数：AF(c) = (parent, now, path)
	 		parent==c.parent now==c.now path==c.path
	@ 不变式：c.parent != null && c.now != null && c.path >= 0
	 */
	
	element parent;
	nodes now;
	int path;
	public element(element parent, nodes now, int path){
		/*@ REQUIRES: parent != null && now != null && path >= 0
		@ MODIFIES: this.parent, this.now, this.path;
		@ EFFECTS: 
			this.parent == parent;
			this.now == now;
			this.path == path;
		*/
		this.parent = parent;
		this.now = now;
		this.path = path;
	}
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(parent==null) return false;
		if(now==null) return false;
		if(path<0) return false;
		return true;
	}
}