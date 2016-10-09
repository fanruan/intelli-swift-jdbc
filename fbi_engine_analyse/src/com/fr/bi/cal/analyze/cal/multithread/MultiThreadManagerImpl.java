package com.fr.bi.cal.analyze.cal.multithread;import com.fr.bi.manager.MultiThreadManager;import com.fr.bi.manager.PerformancePlugManager;import com.finebi.cube.common.log.BILoggerFactory;import java.util.concurrent.ExecutorService;import java.util.concurrent.Executors;import java.util.concurrent.TimeUnit;/** * Created by Hiram on 2015/5/14. */public class MultiThreadManagerImpl implements MultiThreadManager {    private static MultiThreadManagerImpl ourInstance = new MultiThreadManagerImpl();    private static ThreadLocal<ExecutorService> executorServiceThreadLocal = new ThreadLocal<ExecutorService>();    private MultiThreadManagerImpl() {    }    public static boolean isMultiCall() {        return PerformancePlugManager.getInstance().isUseMultiThreadCal();    }    public static MultiThreadManagerImpl getInstance() {        return ourInstance;    }    /**     * 刷新当前线程的ExecutorService,一般web容器都会有线程池，用ThreadLocal之前要先清掉     */    @Override    public void refreshExecutorService() {        if (!isMultiCall()) {            return;        }        executorServiceThreadLocal.set(createNewExecutorServer());    }    @Override    public ExecutorService getExecutorService() {        ExecutorService executorService = executorServiceThreadLocal.get();        if (executorService == null) {            executorService = createNewExecutorServer();            executorServiceThreadLocal.set(executorService);        }        return executorService;    }    @Override    public ExecutorService createNewExecutorServer() {        return Executors.newFixedThreadPool(8);    }    @Override    public void awaitExecutor() {        if (!isMultiCall()) {            return;        }        getExecutorService().shutdown();        try {            getExecutorService().awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);            refreshExecutorService();        } catch (InterruptedException e) {                    BILoggerFactory.getLogger().error(e.getMessage(), e);        }    }}