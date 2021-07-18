package directory.models;

public class ThreadPool {

	// BlockingQueue of classes that implements the Runnable interface
	private BlockingQueue<Runnable> tasksQueue;

	public ThreadPool(int n) {
		tasksQueue = new BlockingQueue<Runnable>();
		
		for (int i = 0; i < n; i++) {
			Worker worker= new Worker();
			worker.run();
		}
	}

	public void submit(Runnable task) {
		try {
			// submit task to be executed
			tasksQueue.offer(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private class Worker implements Runnable {
		
		@Override
		public void run() {
			while (true) {
				
				try {
					// task is given only when tasksQueue is not empty, 
					// otherwise  worker is put into wait()
					// basicaly the workers only work if there is work to do,and they are notified
					// when work is available
					Runnable task = tasksQueue.take();
					task.run();
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
