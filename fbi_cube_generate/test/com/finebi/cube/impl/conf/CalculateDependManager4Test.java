package com.finebi.cube.impl.conf;

import com.fr.bi.stable.data.source.CubeTableSource;

import java.util.Set;

/**
 * Created by kary on 2016/6/13.
 */
public class CalculateDependManager4Test extends CalculateDependManager {

    @Override
    public void setOriginal(Set<CubeTableSource> cubeTableSources) {
        analysisTableSources=cubeTableSources;
    }
}
