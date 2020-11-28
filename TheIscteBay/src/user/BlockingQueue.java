package user;

import java.util.ArrayDeque;
import java.util.Queue;

public class BlockingQueue<T> {
	
	private Queue<T> queue;
	private int size;
	private boolean recebeuArgumentos;
	
	public BlockingQueue(int size){
		if( size >0 ){
			this.size=size;
			queue= new ArrayDeque<>(size);
			recebeuArgumentos=true;
		}
		else
			throw new IllegalArgumentException("O tamanho tem de ser maior do que 0");
	}
	
	public BlockingQueue(){
		queue= new ArrayDeque<>();
		recebeuArgumentos=false;
	}
	
	public synchronized void offer(T e) throws InterruptedException{//se a fila estiver cheia espera-se
		while(queue.size()>=size && recebeuArgumentos){
			wait();
		}
		queue.add(e);
		notifyAll();
	}
	
	public synchronized T poll() throws InterruptedException{// quando quero tirar o primeiro da fila se a fila estiver vazia espera-se
		while(queue.isEmpty()){
			wait();
		}
		T t = queue.remove();
		notifyAll();
		return t;
	}
	
	public synchronized int size(){
		return queue.size();
	}
	
	public synchronized void clear(){
		queue.clear();
	}
	
}