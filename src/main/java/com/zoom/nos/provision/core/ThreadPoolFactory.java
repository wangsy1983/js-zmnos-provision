package com.zoom.nos.provision.core;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolFactory {
	private static Logger log = LoggerFactory
			.getLogger(ThreadPoolFactory.class);

	private static Map<String, ThreadPoolExecutor> threadPoolMap = Collections
			.synchronizedMap(new Hashtable<String, ThreadPoolExecutor>());

	/**
	 * 取得TL1 server ThreadPool
	 * 
	 * @param tl1ServerIp
	 * @return
	 */
	public static synchronized ThreadPoolExecutor getThreadPoolExecutor(
			String tl1ServerIp) {
		log.debug("get ThreadPoolExecutor," + tl1ServerIp);
		ThreadPoolExecutor p = threadPoolMap.get(tl1ServerIp);
		if (p != null) {
			return p;
		} else {
			log.debug("Create ThreadPoolExecutor," + tl1ServerIp);
			// 根据tl1ServerIp，取得线程组参数
			int corePoolSize = 1;
			int maxPoolSize = CoreService.ticketControlService.getThreadPoolMaxSize(tl1ServerIp,
					CoreService.areacode);
			long keepAliveTime = 30;
			// DiscardPolicy不能执行的任务将被删除
			ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
					corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
					new SynchronousQueue(),
					new ThreadPoolExecutor.AbortPolicy());

			threadPoolMap.put(tl1ServerIp, threadPool);
			return threadPool;
		}
	}

	/**
	 * 关闭无效的TL1 server ThreadPool
	 */
	public synchronized void shutdownInvalidThreadPool() {

		for (Iterator<Map.Entry<String, ThreadPoolExecutor>> iterator = ThreadPoolFactory.threadPoolMap
				.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, ThreadPoolExecutor> type = iterator.next();
			String serverIp = type.getKey();

			// 检查serverIp是否有效

			// if(){
			// ThreadPoolExecutor pool=type.getValue();
			// try{
			// pool.shutdown();
			// }catch(Exception e){
			// e.printStackTrace();
			// }
			// ThreadPoolFactory.threadPoolMap.remove(serverIp);
			// }

		}

	}
}
