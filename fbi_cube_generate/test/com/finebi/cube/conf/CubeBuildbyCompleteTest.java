package com.finebi.cube.conf;

import com.finebi.cube.conf.relation.BISystemTableRelationManager;
import com.finebi.cube.impl.conf.CubeBuildByPart;
import com.finebi.cube.impl.conf.CubeBuildComplete;
import com.finebi.cube.impl.conf.CubeBuildRealTime;
import com.fr.bi.base.BIUser;
import com.fr.bi.conf.base.cube.BISystemCubeConfManager;
import com.fr.bi.conf.log.BILogManager;
import com.fr.bi.conf.provider.BICubeConfManagerProvider;
import com.fr.bi.conf.provider.BILogManagerProvider;
import com.fr.stable.bridge.StableFactory;
import junit.framework.TestCase;

/**
 * Created by kary on 2016/6/12.
 */
public class CubeBuildbyCompleteTest extends TestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();
//        StableFactory.registerMarkedObject(BISystemPackageConfigurationProvider.XML_TAG, new BISystemTableRelationManager4Test());
        StableFactory.registerMarkedObject(BITableRelationConfigurationProvider.XML_TAG, new BISystemTableRelationManager());
        StableFactory.registerMarkedObject(BICubeConfManagerProvider.XML_TAG, new BISystemCubeConfManager());
        StableFactory.registerMarkedObject(BILogManagerProvider.XML_TAG, new BILogManager());
    }

    public void testSingleTable() {
    }

    public void testSingleTableSource() {
        CubeBuild cubeBuildManager = new CubeBuildRealTime(null,-999);
    }

    public void testIncremental() {
        CubeBuild cubeBuildManager = new CubeBuildByPart(-999,null,null);
        assertTrue(cubeBuildManager.getAllSingleSources().size() == 0);
        assertTrue(cubeBuildManager.getDependTableResource().size() == 0);
        assertTrue(cubeBuildManager.getBiTableSourceRelationPathSet().size() == 0);
        assertTrue(cubeBuildManager.getTableRelationSet().size() == 0);
        assertTrue(!cubeBuildManager.getVersions().isEmpty());
    }

    public void testAll() {
        CubeBuild cubeBuildManager = new CubeBuildComplete(new BIUser(-999));
        assertTrue(cubeBuildManager.getAllSingleSources().size() == 0);
        assertTrue(cubeBuildManager.getDependTableResource().size() == 0);
        assertTrue(cubeBuildManager.getBiTableSourceRelationPathSet().size() == 0);
        assertTrue(cubeBuildManager.getTableRelationSet().size() == 0);
        assertTrue(!cubeBuildManager.getVersions().isEmpty());
    }
}
