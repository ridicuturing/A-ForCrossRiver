package astar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class Astar {
	Queue<Node> openList = new PriorityQueue<Node>(); // 优先队列(升序)
    List<Node> closeList = new ArrayList<Node>();
    
    public boolean isInClose(Node node) {
    	for(Node n:closeList) {
    		if(n.person.equal(node.person))
    			return true;
    	}
    	return false;
    }
    
    public boolean isInClose(Person person) {
    	for(Node node :closeList) {
    		if(node.person.equal(person)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public Node findNodeInOpen(Person person){
    	for(Node node :openList) {
    		if(node.person.equal(person))
    			return node;
    	}
    	return null;
    }
    
    public Node findNodeInClose(Person person) {
    	for(Node node :closeList) {
    		if(node.person.equal(person))
    			return node;
    	}
    	return null;
    }
    
	public void start() {
		System.out.println("start");
		openList.clear();
		closeList.clear();
		openList.add(MapInfo.start);
		moveNode();
		System.out.println("over");
		
	}
	
	public void moveNode(){
		while(!openList.isEmpty()) {
			if(isInClose(MapInfo.end)) {
				printPath(findNodeInClose(MapInfo.end.person));
				return;
			}
			Node now = openList.poll();
			closeList.add(now);
			addNextToOpen(now);
		}
		System.out.println("no answer");
	}
	
	public void printPath(Node node) {
		System.out.println("found it");
		for(;node != null;node = node.parent) {
			System.out.print(Arrays.toString(node.person.now) +" " );
			System.out.printf("%-2d",node.G);
			node.showDiffToParent();
		}
	}
	
	public void addNextToOpen(Node node) {
		int[] tmp = node.person.now.clone();
		if(tmp[6] == 1) { //如果船在这边
			tmp[6] = 0;
			for(int s = 0; s < Person.driver ; s++) {
				if(tmp[s] == 0) {//开船的人在对岸，跳过
					continue;
				}
				tmp[s]--;
				for(int n = 0 ; n < Person.type ; n++ ) {
					if(s == n) { // 如果开船的人和被载的人重合，跳过
						addOneToOpen(node,tmp.clone());
						continue;
					}
					if(tmp[n] > 0) {
						tmp[n]--;
						addOneToOpen(node,tmp.clone());
						tmp[n]++;
					}
				}	
				tmp[s]++;
			}
		}else {//船在对岸
			tmp[6] = 1;
			for(int s = 0; s < Person.driver ; s++) {
				if(tmp[s] == 1) {//开船的人在此岸，跳过
					continue;
				}
				tmp[s]++;
				for(int n = 0 ; n < Person.type ; n++ ) {
					if(s == n) { // 如果开船的人和被载的人重合，自己渡河
						//System.out.println("alone back" + Arrays.toString(tmp));
						addOneToOpen(node,tmp.clone());
						continue;
					}
					if(Person.people[n] - tmp[n] > 0) { //Person.people[n] - tmp[n] 为tmp[n]的对岸人数情况
						tmp[n]++;
						addOneToOpen(node,tmp.clone());
						tmp[n]--;
					}
				}	
				tmp[s]--;
			}
		}
	}
	
	public void addOneToOpen(Node current,int[] next) {
		Person person = new Person(next);
		if(person.isLegal() && !isInClose(person)) { //如果节点合法（符合规则） 并且 不在close表中
			int G = current.G + 1;
			Node child = findNodeInOpen(person);
			if(child == null) {
				Node node = new Node(current,next);
				if(node.equal(MapInfo.end)) { //如果要加入的点是终点
					child = new Node(current,MapInfo.end.person.now);
				}else {
					child = new Node(current,next);
				}
				openList.add(child);
			}else if(child.G > G) {
				child.G = G;
				child.parent = current;
			}else {
			}
		}
	}

	public static void main(String[] args) {
		new Astar().start();
		
		
	/*	Astar a = new Astar();
		Node n = new Node(new int[] {1,2,3,4,5,6,7});
		a.printopen();*/
		
		
	}
}



class Person {
	/*
	 提示：玩家的任务是，帮助这些人渡过河，交通工具只有一艘小船。
	 只有爸爸、妈妈以及警察能控制小船，此外，不论成人或是小孩，小船每次最多只能搭乘二人。在渡河期间，玩家还要防止以下三种情况发生：
1	、当警察与犯人分开时，犯人会伤害一家六口。
2	、当爸爸看见妈妈离开女儿时，爸爸便会教训女儿。
3	、当妈妈看见爸爸离开儿子时，妈妈便会教训儿子。 
	 */
	static final int all = 8; //总共8个人
	static final int type = 6; //总共6类人
	static final int driver = 3; //3个人可以开船（数组前三个）
	public static enum human{
		FATHER,MOTHER,POLICE,DAUGHTER,SON,GUILTY,BOAT
	}
	static final int[] people= {1,1,1,2,2,1,1};
	int[] now; //对象当前情况
	
	int thisSide; //没过河的人数
	
	public Person(int[] a) {
		now = a;
		for(int n :now) {
			thisSide += n;
		}
		thisSide -= now[human.BOAT.ordinal()];
	}
	
	public boolean equal(Person person){
		return Arrays.equals(person.now,now);
	}
	
	public boolean hasSomeoneInThisSide(int s) {
		return now[s] > 0;
	}
	
	public boolean hasSomeoneAtThatSide(int s) {
		return people[s] - now[s] > 0;
	}
	
	public boolean isLegal() {
		//条件一：
		if(!hasSomeoneInThisSide(human.POLICE.ordinal()) && hasSomeoneInThisSide(human.GUILTY.ordinal()) && thisSide > now[human.GUILTY.ordinal()] || //河这边没警察，除了犯人，还有其他人
				!hasSomeoneAtThatSide(human.POLICE.ordinal()) && hasSomeoneAtThatSide(human.GUILTY.ordinal())  && Person.all - thisSide > (people[human.GUILTY.ordinal()] - now[human.GUILTY.ordinal()]) )//河那边同上
			return false;
		//条件二：
		if(!hasSomeoneInThisSide(human.MOTHER.ordinal()) && hasSomeoneInThisSide(human.FATHER.ordinal()) && hasSomeoneInThisSide(human.DAUGHTER.ordinal()) ||
				!hasSomeoneAtThatSide(human.MOTHER.ordinal()) && hasSomeoneAtThatSide(human.FATHER.ordinal()) && hasSomeoneAtThatSide(human.DAUGHTER.ordinal()) ) 
			return false;
		//条件三：
		if(!hasSomeoneInThisSide(human.FATHER.ordinal()) && hasSomeoneInThisSide(human.MOTHER.ordinal()) && hasSomeoneInThisSide(human.SON.ordinal()) ||
				!hasSomeoneAtThatSide(human.FATHER.ordinal()) && hasSomeoneAtThatSide(human.MOTHER.ordinal()) && hasSomeoneAtThatSide(human.SON.ordinal()) ) 
			return false;
		return true;
	}
}

class Node implements Comparable<Node> {
	public Person person;
	public Node parent;
	public int G; // G：乘船次数
    public int H; // H：河这边剩余的人数（离目标的距离）
	
	public Node(Person person) {
		this.person = person;
	}
	
	public Node(int[] s) {
		person = new Person(s);
	}
	
	public Node(Person person,Node parent, int g, int h) {
		this.person = person;
		this.parent = parent;
		G = g;
		H = h;
	}
	
	public Node(int[] s,Node parent, int g, int h) {
		this.person = new Person(s);
		this.parent = parent;
		G = g;
		H = h;
	}
	public Node(Node parent,int[] s) {
		this.person = new Person(s);
		this.parent = parent;
		G = parent.G+1;
		H = person.thisSide;
	}

	@Override
	public int compareTo(Node o) { 
		if( G + H > o.G + o.H) 
			return 1;
		else if(  G + H < o.G + o.H )
			return -1;
		return 0;
	}

	public boolean isLegal() {
		return person.isLegal();
	}

	public boolean equal(Node node) {
		return Arrays.equals(person.now,node.person.now);
	}

	public void showDiffToParent() {
		if(parent != null) {
			if(person.now[Person.human.BOAT.ordinal()] == 1) {
				System.out.print(" back ");
			}else {
				System.out.print("  go  ");
			}
			
			for(int n = 0 ; n < Person.type ; n++) {
				if(person.now[n] != parent.person.now[n]) {
					System.out.print(" " + Person.human.values()[n] + " ");
				}
			}
			
		}
		System.out.println();
	}
}

class MapInfo{
	public static Node start = new Node(Person.people.clone()); 
	public static Node end = new Node(new int[] {0,0,0,0,0,0,0});
	public static int[] endList = {0,0,0,0,0,0,0};
}