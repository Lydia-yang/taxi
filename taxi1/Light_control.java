package taxi1;

import java.awt.Point;

import taxi1.nodes.light_state;

public class Light_control extends Thread{
	/*@ OVERVIEW: 控制红路灯间隔时间变换
	@ 表示对象：nodes n, TaxiGUI gui;
	@ 抽象函数：AF(c) = (n, gui)
			n==c.n, gui==c.gui;
	@ 不变式：c.gui!=null && c.n != null && c.n.light == true && 200 <= c.n.time <=500 && (c.n.green==light_state.LR || c.n.green==light_state.UD)
	 */
	
	nodes n;
	TaxiGUI gui;
	/*public Light_control(nodes n){
		this.n = n;
	}*/
	public Light_control(nodes n, TaxiGUI gui){
		/*@ REQUIRES: n != null && gui != null && n.light == true && 200 <= n.time <=500 && (n.green==light_state.LR || n.green==light_state.UD)
		@ MODIFIES: this.n, this.gui;
		@ EFFECTS: 
			this.n == n;
			this.gui == gui;
		*/
		this.n = n;
		this.gui = gui;
		
	}
	
	public boolean repOK(){
		//@EFFECTS: \result==invariant(this);
		if(n==null || gui==null) return false;
		if(!n.light || n.time<200 || n.time>500) return false;
		if(n.green != light_state.LR && n.green != light_state.UD) return false;
		return true;
	}
	
	public void run(){
		/*@ REQUIRES: 
		@ MODIFIES: this.n, this.gui;
		@ EFFECTS: 
			间隔this.n.time将n.green变换为另一状态并在gui上改变状态
			如果睡中断抛出中断异常InterruptedException，则调用e.printStackTrace()输出相应的异常信息；
		*/
		
		//System.out.println("T: "+n.time);
		while(true){
			
			try {
				sleep(n.time);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(n.green==light_state.LR){
				n.green = light_state.UD;
				//System.out.println("p:"+n.p.toString()+" UD");
				gui.SetLightStatus(new Point(n.p.x, n.p.y), 2);
			}
			else {
				n.green = light_state.LR;
				//System.out.println("p:"+n.p.toString()+" LR");
				gui.SetLightStatus(new Point(n.p.x, n.p.y), 1);
			}
		}
		
	}
}
