package user;


public class ThreadPool {
	
	private BlockingQueue<Runnable> tasks;
	private Worker[] workers;
	
	public ThreadPool(int n){
		//n = numero de threads worker
		workers=new Worker[n];
		tasks=new BlockingQueue<Runnable>();
		for(int i=0;i<n;i++){
			Worker w =new Worker();
			workers[i]=w;
			w.start();		
		}
	}
	
	public void submit(Runnable task){
		try {
			tasks.offer(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private class Worker extends Thread{
		
		@Override
		public void run() {
			while(!interrupted()){
				try {
					Runnable r =tasks.poll();
					r.run();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
