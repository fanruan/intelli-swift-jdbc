package com.fr.swift.segment.column.impl.base;

import com.fr.swift.cube.io.BuildConf;
import com.fr.swift.cube.io.Types.DataType;
import com.fr.swift.cube.io.Types.IoType;
import com.fr.swift.cube.io.input.DoubleReader;
import com.fr.swift.cube.io.location.IResourceLocation;
import com.fr.swift.cube.io.output.DoubleWriter;

/**
 * @author anchore
 * @date 2017/11/9
 */
public class DoubleDetailColumn extends BaseDetailColumn<Double> {
    private DoubleWriter detailWriter;
    private DoubleReader detailReader;

    public DoubleDetailColumn(IResourceLocation parent) {
        super(parent);
    }

    @Override
    public double getDouble(int pos) {
        initDetailReader();
        return detailReader.get(pos);
    }

    @Override
    public void put(int pos, Double val) {
        initDetailWriter();
        detailWriter.put(pos, val);
    }

    @Override
    public Double get(int pos) {
        return getDouble(pos);
    }

    @Override
    void initDetailWriter() {
        if (detailWriter == null) {
            detailWriter = DISCOVERY.getWriter(location, new BuildConf(IoType.WRITE, DataType.DOUBLE));
        }
    }

    @Override
    void initDetailReader() {
        if (detailReader == null) {
            detailReader = DISCOVERY.getReader(location, new BuildConf(IoType.READ, DataType.DOUBLE));
        }
    }

    @Override
    public void flush() {
        if (detailWriter != null) {
            detailWriter.flush();
        }
    }

    @Override
    public void release() {
        if (detailWriter != null) {
            detailWriter.release();
            detailWriter = null;
        }
        if (detailReader != null) {
            detailReader.release();
            detailReader = null;
        }
    }
}