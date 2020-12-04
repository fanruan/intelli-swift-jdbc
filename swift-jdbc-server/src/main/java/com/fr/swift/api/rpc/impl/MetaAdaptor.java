package com.fr.swift.api.rpc.impl;

import com.fr.swift.config.entity.SwiftMetaDataEntity;
import com.fr.swift.db.SwiftDatabase;
import com.fr.swift.source.SwiftMetaData;

/**
 * @Author: lucifer
 * @Description:
 * @Date: Created in 2020/11/20
 */
public class MetaAdaptor {
    // FIXME: 2020/11/19 临时先处理下，后面考虑改掉
    public static SwiftMetaData toCubeMeta(SwiftMetaData swiftMetaData) {
        return new SwiftMetaDataEntity.Builder(swiftMetaData).setSwiftSchema(SwiftDatabase.CUBE).build();
    }
}
