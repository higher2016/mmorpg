package com.kingston.mmorpg.framework.net.command;

import com.kingston.mmorpg.common.util.thread.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class GameExecutor {

	private Logger logger = LoggerFactory.getLogger(GameExecutor.class);

	private static GameExecutor instance;

	private final int CORE_SIZE = Runtime.getRuntime().availableProcessors();
	/** task worker pool */
	private final ExecutorService[] workerPool = new ExecutorService[CORE_SIZE];

	private final AtomicBoolean run = new AtomicBoolean(true);

	private ConcurrentMap<Thread, BaseCommand> currentTasks = new ConcurrentHashMap<>();

	private final long MONITOR_INTERVAL = 5000L;

	private final long MAX_EXEC_TIME = 30000L;

	@PostConstruct
	private void init() {
		for (int i = 0; i < CORE_SIZE; i++) {
			ThreadFactory threadFactory = new NamedThreadFactory("message-task-handler");
			workerPool[i] = Executors.newSingleThreadExecutor(threadFactory);
		}
		instance = this;
		new NamedThreadFactory("message-task-monitor").newThread(new TaskMonitor()).start();
	}

	public static GameExecutor getInstance() {
		return instance;
	}

	/**
	 * @param task
	 */
	public void acceptTask(BaseCommand task) {
		if (task == null) {
			throw new NullPointerException("task is null");
		}
		int distributeKey = task.dispatchMap % CORE_SIZE;
		workerPool[distributeKey].submit(new Runnable() {
			@Override
			public void run() {
				Thread t = Thread.currentThread();
				currentTasks.put(t, task);
				task.run();
				currentTasks.remove(t);
			}
		});
	}

	/**
	 * shut context
	 */
	public void shutDown() {
		run.compareAndSet(true, false);
	}


	class TaskMonitor implements Runnable {

		@Override
		public void run() {
			for (; ;) {
				try {
					Thread.sleep(MONITOR_INTERVAL);
				} catch (InterruptedException e) {
				}

				for (Map.Entry<Thread, BaseCommand> entry: currentTasks.entrySet()) {
					Thread t = entry.getKey();
					BaseCommand task = entry.getValue();
					if (task != null) {
						long now = System.currentTimeMillis();
						if (now - task.getStartTime() > MAX_EXEC_TIME) {
							System.out.println("执行任务超时");
						}
					}
				}
			}
		}
	}

}
