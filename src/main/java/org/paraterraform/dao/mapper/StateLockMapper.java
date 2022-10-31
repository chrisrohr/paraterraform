package org.paraterraform.dao.mapper;

import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.paraterraform.model.StateLock;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StateLockMapper implements RowMapper<StateLock> {

    @Override
    public StateLock map(ResultSet rs, StatementContext ctx) throws SQLException {
        return StateLock.builder()
                .id(rs.getString("id"))
                .stateName(rs.getString("state_name"))
                .info(rs.getString("info"))
                .operation(rs.getString("operation"))
                .lockedBy(rs.getString("locked_by"))
                .version(rs.getString("version"))
                .path(rs.getString("path"))
                .createdAt(instantFromTimestamp(rs, "created_at"))
                .build();
    }
}
