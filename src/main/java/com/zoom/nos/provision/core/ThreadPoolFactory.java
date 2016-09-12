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
	 * ȡ��TL1 server ThreadPool
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
			// ����tl1ServerIp��ȡ���߳������
			int corePoolSize = 1;
			int maxPoolSize = CoreService.ticketControlService.getThreadPoolMaxSize(tl1ServerIp,
					CoreService.areacode);
			long keepAliveTime = 30;
			// DiscardPolicy����ִ�е����񽫱�ɾ��
			ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
					corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
					new SynchronousQueue(),
					new ThreadPoolExecutor.AbortPolicy());

			threadPoolMap.put(tl1ServerIp, threadPool);
			return threadPool;
		}
	}

	/**
	 * �ر���Ч��TL1 server ThreadPool
	 */
	public synchronized void shutdownInvalidThreadPool() {

		for (Iterator<Map.Entry<String, ThreadPoolExecutor>> iterator = ThreadPoolFactory.threadPoolMap
				.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, ThreadPoolExecutor> type = iterator.next();
			String serverIp = type.getKey();

			// ���serverIp�Ƿ���Ч

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
