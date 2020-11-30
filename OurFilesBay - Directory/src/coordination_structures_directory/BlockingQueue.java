package coordination_structures_directory;

import java.util.ArrayDeque;
import java.util.Queue;

public class BlockingQueue<E> {// designed to be accessed by multiple Threads
	/*
	 * Used in ThreadPool
	 * 
	 * In this package methods can throw exceptions
	 * 
	 */

	private Queue<E> queue;

	public BlockingQueue() {
		this.queue = new ArrayDeque<>();
	}

	public synchronized void offer(E e) throws InterruptedException {
		queue.add(e);
		notify();//only threads from threadPool will be in wait
	}

	public synchronized E take() throws InterruptedException {// removes the head of queue
		while (queue.isEmpty()) {
			wait();
		}
		E e = queue.remove();// remove method retrieves and removes

		return e;
	}

}