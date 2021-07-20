package client.models.coordination;

public class ThreadPool {

	// BlockingQueue of classes that implements the Runnable interface
	private BlockingQueue<Runnable> tasksQueue;
	private Worker[] workers;

	public ThreadPool(int n) {
		
		tasksQueue = new BlockingQueue<Runnable>();
		workers = new Worker[n];
		
		for (int i = 0; i < n; i++) {
			Worker worker= new Worker();
			workers[i] = worker;
			
			Thread workerThread = new Thread(worker);
			workerThread.start();
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
	
	public void shutDownPool() {
		for(Worker worker: workers) {
			worker.shutDown();
		}
	}

	private class Worker implements Runnable {

		private boolean work;

		public Worker() {
			this.work = true;
		}
		
		@Override
		public void run() {
			
			try {
				while (work) {

					// task is given only when tasksQueue is not empty,
					// otherwise worker is put into wait()
					// basicaly the workers only work if there is work to do,and they are notified
					// when work is available
					Runnable task = tasksQueue.take();
					task.run();

				}
				System.out.println("Worker has been Stoped");
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void shutDown() {
			this.work = false;
		}
	}
	
}
