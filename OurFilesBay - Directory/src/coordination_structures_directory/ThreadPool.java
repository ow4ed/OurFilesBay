package coordination_structures_directory;

public class ThreadPool {
	/*
	 * In this package methods can throw exceptions
	 * 
	 */
	
	private BlockingQueue<Runnable> tasksQueue;//BlockingQueue of classes that implements the Runnable interface 
	private Worker[] workers;
	
	public ThreadPool(int n){
		workers = new Worker[n];
		tasksQueue = new BlockingQueue<Runnable>();// basically i only can get tasks if they are any, otherwise thread will wait
		
		for(int i=0;i<n;i++){
			Worker w = new Worker();
			workers[i] = w;
			
			Thread thread = new Thread(w);
			thread.start();		
		}
	}
	
	public void submit(Runnable task){//only question needed to be answered what task do i submit to the pool ?
		try {
			tasksQueue.offer(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private class Worker implements Runnable{
		@Override
		public void run() {
			while(!Thread.currentThread().isInterrupted()){
				try {
					Runnable task =tasksQueue.take();//task is given only when tasksQueue is not empty, if not worker is put into wait()
					task.run();//basicaly the workers only work if there is work to do,and they are notified when work is available
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
