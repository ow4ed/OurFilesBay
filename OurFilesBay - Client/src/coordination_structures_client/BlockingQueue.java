package coordination_structures_client;

import java.util.ArrayDeque;
import java.util.Queue;

public class BlockingQueue<E> {
	/*
	 * Used in ThreadPool 
	 * 
	 *
	 * In this package methods can throw exceptions
	 * 
	 */

	private Queue<E> queue;

	public BlockingQueue() {
		this.queue = new ArrayDeque<>();
	}
	
	public synchronized void offer(E e) throws InterruptedException {//multiple threads access this method
		queue.add(e);
		notifyAll();//only threads from threadPool will be in wait
		//check if is needed to notify All
	}

	public synchronized E take() throws InterruptedException {// removes the head of queue
		while (queue.isEmpty()) {
			wait();
		}
		E e = queue.remove();// remove method retrieves and removes

		return e;
	}

}
