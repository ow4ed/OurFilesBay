package client.models.coordination;

import java.util.ArrayDeque;
import java.util.Queue;

/*
 * Used in Client
 * 
 *
 * In this package methods can throw exceptions
 * 
 */

public class FileBloksQueue <E>{

	private Queue<E> queue;
	private boolean allBlocksAreAvailable = false;

	public FileBloksQueue() {
		this.queue = new ArrayDeque<>();
	}
	
	public synchronized int getSize() {
		return queue.size();
	}
	
	public void add(E e) {//only 1 Thread operating here
		queue.add(e);
	}

	public synchronized E take() {// removes the head of queue
		E e = queue.poll();// remove method retrieves and removes
		return e;
	}
	
	
	//why synchronized ???
	public synchronized void waitBlocks() throws InterruptedException {//only 1 Thread operating here
		while(!allBlocksAreAvailable) {
			wait();
		}
	}
	
	public void resetWaitBlocks() {
		allBlocksAreAvailable = false;
	}
	
	public synchronized void Done() {//only 1 Thread operating here
		allBlocksAreAvailable = true;
		notifyAll();//more threads can be sleeping
	}

	public void clear() {
		queue.clear();		
	}

}

