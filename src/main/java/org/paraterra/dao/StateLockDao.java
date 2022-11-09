package org.paraterra.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.paraterra.dao.mapper.StateLockMapper;
import org.paraterra.model.StateLock;

import java.util.Optional;

@RegisterRowMapper(StateLockMapper.class)
public interface StateLockDao {

    @SqlQuery("select * from state_locks where state_name = :name")
    Optional<StateLock> findLockByStateName(@Bind("name") String name);

    @SqlUpdate("insert into state_locks (id, state_name, operation, info, locked_by, version, created_at, path) " +
            "values (:id, :stateName, :operation, :info, :lockedBy, :version, :createdAt, :path)")
    void insertLock(@BindBean StateLock lock);

    @SqlUpdate("delete from state_locks where state_name = :name")
    int deleteLock(@Bind("name") String name);
}
