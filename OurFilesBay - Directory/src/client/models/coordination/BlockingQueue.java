/*
 * Used in ThreadPool
 * 
 */
package directory.models;

import java.util.ArrayDeque;
import java.util.Queue;

public class BlockingQueue<E> {

	private Queue<E> queue;

	public BlockingQueue() {
		this.queue = new ArrayDeque<>();
	}

	public synchronized void offer(E e) throws InterruptedException {
		
		queue.add(e);
		notify();
	}

	public synchronized E take() throws InterruptedException {
		
		while (queue.isEmpty()) {
			wait();
		}
		// removes and retrieves the head of queue
		E e = queue.remove();
		
		return e;
	}

}