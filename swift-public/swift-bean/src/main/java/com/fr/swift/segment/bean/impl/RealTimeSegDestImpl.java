package com.fr.swift.segment.bean.impl;

import com.fr.stable.AssistUtils;
import com.fr.swift.query.Queryable;
import com.fr.swift.segment.SegmentDestination;

/**
 * @author yee
 * @date 2018/9/5
 */
public class RealTimeSegDestImpl extends SegmentDestinationImpl {
    private static final long serialVersionUID = -5969030726680132148L;

    public RealTimeSegDestImpl() {
    }

    public RealTimeSegDestImpl(String clusterId, String segmentId, int order, Class<? extends Queryable> serviceClass, String methodName) {
        super(clusterId, segmentId, order, serviceClass, methodName);
    }

    public RealTimeSegDestImpl(SegmentDestination destination) {
        super(destination);
    }

    public RealTimeSegDestImpl(String segmentId, int order) {
        super(segmentId, order);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RealTimeSegDestImpl
                && AssistUtils.equals(this.clusterId, ((RealTimeSegDestImpl) o).clusterId)
                && AssistUtils.equals(this.serviceClass, ((RealTimeSegDestImpl) o).serviceClass)
                && AssistUtils.equals(this.methodName, ((RealTimeSegDestImpl) o).methodName);
    }

    @Override
    public int hashCode() {
        return AssistUtils.hashCode(this.clusterId, this.serviceClass, this.methodName);
    }

    @Override
    public SegmentDestination copy() {
        return new RealTimeSegDestImpl(this);
    }
}
