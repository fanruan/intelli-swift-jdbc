/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fr.swift.jdbc.druid.sql.ast.statement;

import com.fr.swift.jdbc.druid.sql.SQLUtils;
import com.fr.swift.jdbc.druid.sql.ast.SQLExpr;
import com.fr.swift.jdbc.druid.sql.ast.SQLHint;
import com.fr.swift.jdbc.druid.sql.ast.SQLObjectImpl;
import com.fr.swift.jdbc.druid.util.FnvHash;

import java.util.ArrayList;
import java.util.List;

public abstract class SQLTableSourceImpl extends SQLObjectImpl implements SQLTableSource {
    protected String alias;
    protected List<SQLHint> hints;
    protected SQLExpr flashback;
    protected long aliasHashCod64;

    public SQLTableSourceImpl() {

    }

    public SQLTableSourceImpl(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
        this.aliasHashCod64 = 0L;
    }

    public int getHintsSize() {
        if (hints == null) {
            return 0;
        }

        return hints.size();
    }

    public List<SQLHint> getHints() {
        if (hints == null) {
            hints = new ArrayList<SQLHint>(2);
        }
        return hints;
    }

    public void setHints(List<SQLHint> hints) {
        this.hints = hints;
    }

    public SQLTableSource clone() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    public String computeAlias() {
        return alias;
    }

    public SQLExpr getFlashback() {
        return flashback;
    }

    public void setFlashback(SQLExpr flashback) {
        if (flashback != null) {
            flashback.setParent(this);
        }
        this.flashback = flashback;
    }

    public boolean containsAlias(String alias) {
        return SQLUtils.nameEquals(this.alias, alias);

    }

    public long aliasHashCode64() {
        if (aliasHashCod64 == 0
                && alias != null) {
            aliasHashCod64 = FnvHash.hashCode64(alias);
        }
        return aliasHashCod64;
    }

    public SQLColumnDefinition findColumn(String columnName) {
        if (columnName == null) {
            return null;
        }

        long hash = FnvHash.hashCode64(alias);
        return findColumn(hash);
    }

    public SQLColumnDefinition findColumn(long columnNameHash) {
        return null;
    }

    public SQLTableSource findTableSourceWithColumn(String columnName) {
        if (columnName == null) {
            return null;
        }

        long hash = FnvHash.hashCode64(alias);
        return findTableSourceWithColumn(hash);
    }

    public SQLTableSource findTableSourceWithColumn(long columnNameHash) {
        return null;
    }

    public SQLTableSource findTableSource(String alias) {
        long hash = FnvHash.hashCode64(alias);
        return findTableSource(hash);
    }

    public SQLTableSource findTableSource(long alias_hash) {
        long hash = this.aliasHashCode64();
        if (hash != 0 && hash == alias_hash) {
            return this;
        }
        return null;
    }
}
