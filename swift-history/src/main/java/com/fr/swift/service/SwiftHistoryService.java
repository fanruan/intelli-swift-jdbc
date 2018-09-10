package com.fr.swift.service;

import com.fr.swift.annotation.SwiftService;
import com.fr.swift.config.entity.SwiftTablePathEntity;
import com.fr.swift.config.service.SwiftCubePathService;
import com.fr.swift.config.service.SwiftMetaDataService;
import com.fr.swift.config.service.SwiftSegmentService;
import com.fr.swift.config.service.SwiftTablePathService;
import com.fr.swift.config.service.impl.SwiftSegmentServiceProvider;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.cube.io.Types;
import com.fr.swift.db.Where;
import com.fr.swift.exception.SwiftServiceException;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.query.info.bean.query.QueryInfoBean;
import com.fr.swift.query.query.QueryBeanFactory;
import com.fr.swift.query.session.factory.SessionFactory;
import com.fr.swift.repository.SwiftRepository;
import com.fr.swift.repository.manager.SwiftRepositoryManager;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.segment.SwiftSegmentManager;
import com.fr.swift.segment.operator.delete.WhereDeleter;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftResultSet;
import com.fr.swift.task.service.ServiceTaskExecutor;
import com.fr.swift.task.service.ServiceTaskType;
import com.fr.swift.task.service.SwiftServiceCallable;
import com.fr.swift.util.FileUtil;
import com.fr.swift.util.concurrent.PoolThreadFactory;
import com.fr.swift.util.concurrent.SwiftExecutors;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @author pony
 * @date 2017/10/10
 */
@SwiftService(name = "history")
public class SwiftHistoryService extends AbstractSwiftService implements HistoryService, Serializable {
    private static final long serialVersionUID = -6013675740141588108L;

    private transient SwiftSegmentManager segmentManager;

    private transient ServiceTaskExecutor taskExecutor;

    private transient SwiftCubePathService pathService;

    private transient SwiftMetaDataService metaDataService;

    private transient SwiftTablePathService tablePathService;

    private transient QueryBeanFactory queryBeanFactory;

    private transient SwiftSegmentService segmentService;

    private transient ExecutorService loadDataService;


    private SwiftHistoryService() {
    }

    @Override
    public boolean start() throws SwiftServiceException {
        super.start();
        segmentManager = SwiftContext.get().getBean("localSegmentProvider", SwiftSegmentManager.class);
        taskExecutor = SwiftContext.get().getBean(ServiceTaskExecutor.class);
        pathService = SwiftContext.get().getBean(SwiftCubePathService.class);
        metaDataService = SwiftContext.get().getBean(SwiftMetaDataService.class);
        tablePathService = SwiftContext.get().getBean(SwiftTablePathService.class);
        queryBeanFactory = SwiftContext.get().getBean(QueryBeanFactory.class);
        segmentService = SwiftContext.get().getBean(SwiftSegmentServiceProvider.class);
        loadDataService = SwiftExecutors.newSingleThreadExecutor(new PoolThreadFactory(SwiftHistoryService.class));
        checkSegmentExists();
        return true;
    }

    private void checkSegmentExists() {
        SwiftRepository repository = SwiftRepositoryManager.getManager().currentRepo();
        Map<String, List<SegmentKey>> map = segmentService.getOwnSegments();
        for (Map.Entry<String, List<SegmentKey>> entry : map.entrySet()) {
            String table = entry.getKey();
            List<SegmentKey> value = entry.getValue();
            List<SegmentKey> notExists = new ArrayList<SegmentKey>();
            final Map<String, Set<String>> needDownload = new HashMap<String, Set<String>>();
            for (SegmentKey segmentKey : value) {
                if (segmentKey.getStoreType() == Types.StoreType.FINE_IO) {
                    if (!segmentManager.getSegment(segmentKey).isReadable()) {
                        String remotePath = String.format("%s/%s", segmentKey.getSwiftSchema().getDir(), segmentKey.getUri().getPath());
                        if (repository.exists(remotePath)) {
                            if (null == needDownload.get(table)) {
                                needDownload.put(table, new HashSet<String>());
                            }
                            needDownload.get(table).add(remotePath);
                        } else {
                            notExists.add(segmentKey);
                        }
                    }
                }
            }
            if (!needDownload.isEmpty()) {
                loadDataService.submit(new Runnable() {
                    @Override
                    public void run() {
                        load(needDownload, false);
                    }
                });
            }
            segmentService.removeSegments(notExists);
        }
    }

    @Override
    public boolean shutdown() throws SwiftServiceException {
        super.shutdown();
        segmentManager = null;
        taskExecutor = null;
        pathService = null;
        metaDataService = null;
        tablePathService = null;
        queryBeanFactory = null;
        loadDataService.shutdown();
        loadDataService = null;
        segmentService = null;
        return true;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.HISTORY;
    }

    @Override
    public void load(Map<String, Set<String>> remoteUris, boolean replace) {
        String path = pathService.getSwiftPath();
        SwiftRepository repository = SwiftRepositoryManager.getManager().currentRepo();
        if (null != remoteUris && !remoteUris.isEmpty()) {
            for (String sourceKey : remoteUris.keySet()) {
                Set<String> sets = remoteUris.get(sourceKey);
                if (!sets.isEmpty()) {
                    SwiftMetaData metaData = metaDataService.getMetaDataByKey(sourceKey);
                    int tmp = 0;
                    SwiftTablePathEntity entity = tablePathService.get(sourceKey);
                    if (null == entity) {
                        entity = new SwiftTablePathEntity(sourceKey, tmp);
                        tablePathService.saveOrUpdate(entity);
                        replace = true;
                    } else {
                        tmp = entity.getTablePath() == null ? -1 : entity.getTablePath();
                        if (replace) {
                            tmp += 1;
                            entity.setTmpDir(tmp);
                            tablePathService.saveOrUpdate(entity);
                        }
                    }

                    boolean downloadSuccess = true;
                    for (String uri : sets) {
                        String cubePath = String.format("%s/%s/%d/%s", path, metaData.getSwiftDatabase().getDir(), tmp, uri);
                        String remotePath = String.format("%s/%s", metaData.getSwiftDatabase().getDir(), uri);
                        try {
                            repository.copyFromRemote(remotePath, cubePath);
                        } catch (Exception e) {
                            downloadSuccess = false;
                            SwiftLoggers.getLogger().error("Download " + sourceKey + " with error! ", e);
                            break;
                        }
                    }

                    if (replace && downloadSuccess) {
                        entity = tablePathService.get(sourceKey);
                        int current = entity.getTablePath() == null ? -1 : entity.getTablePath();
                        entity.setLastPath(current);
                        entity.setTablePath(tmp);
                        tablePathService.saveOrUpdate(entity);
                        String cubePath = String.format("%s/%s/%d/%s", path, metaData.getSwiftDatabase().getDir(), current, sourceKey);
                        FileUtil.delete(cubePath);
                        new File(cubePath).getParentFile().delete();
                    }
                    if (downloadSuccess) {
                        SwiftLoggers.getLogger().info("Download " + sourceKey + " successful");
                    }
                }
            }
        } else {
            SwiftLoggers.getLogger().warn("Receive an empty URI set. Skip loading.");
        }
    }

    @Override
    public SwiftResultSet query(final String queryDescription) throws Exception {
        try {
            final QueryInfoBean bean = queryBeanFactory.create(queryDescription);
            SessionFactory factory = SwiftContext.get().getBean(SessionFactory.class);
            return factory.openSession(bean.getQueryId()).executeQuery(bean);
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public boolean delete(final SourceKey sourceKey, final Where where) throws Exception {
        taskExecutor.submit(new SwiftServiceCallable(sourceKey, ServiceTaskType.DELETE) {
            @Override
            public void doJob() throws Exception {
                List<Segment> segments = segmentManager.getSegment(sourceKey);
                for (Segment segment : segments) {
                    WhereDeleter whereDeleter = (WhereDeleter) SwiftContext.get().getBean("decrementer", sourceKey, segment);
                    whereDeleter.delete(where);
                }
            }
        });
        return true;
    }
}
