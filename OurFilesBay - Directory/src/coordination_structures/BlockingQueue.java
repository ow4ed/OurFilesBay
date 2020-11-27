package coordination_structures;

import java.util.ArrayDeque;
import java.util.Queue;

public class BlockingQueue<E> {//supposedly it is designed to be accessed by multiple Threads
	
	/*
	 * Used in ThreadPool
	 * 
	 * In this package methods can throw exceptions
	 * 
	 */
	
	private Queue<E> queue;// head, tail; FIFO, LIFO
	private int size;
	
	public BlockingQueue(){
		this.size = 0;
		this.queue= new ArrayDeque<>();//initial Queue size = 16 (default)
	}
	
	/*
	 * this constructor is never used in this project
	 * 
	public BlockingQueue(int size){
		if( size > 0 ){
			this.size=size;
			this.queue= new ArrayDeque<>(size);//initial Queue size = size
		}
		else
			throw new IllegalArgumentException("Blocking Queue size must be greater than 0");//object isn't created ?
	}
	*/
	
	public synchronized void offer(E e) throws InterruptedException{
		while(queue.size()>=size && size>0){//if queue is full (in case queue has a limit), the thread invoking this method will wait
			wait();//inportant to use notify
		}
		queue.add(e);
		notify();//important to use wait
		//in my thread pool i want to notify one of the workers that queue is not empty, not All
	}
	
	public synchronized E take() throws InterruptedException{//removes the head of queue
		while(queue.isEmpty()){//if queue is empty, the thread invoking this method will wait
			wait();
		}
		E e = queue.remove();//remove method retrieves and removes
		//notifyAll(); 
		//in my thread pool i don't have to notify anyone after i take an element 
		return e;
	}
	
	public synchronized int size(){
		return queue.size();
	}
	/*
	 * this method is never used in this project 
	 * 
	public synchronized void clear(){
		queue.clear();
	}
	*/
}