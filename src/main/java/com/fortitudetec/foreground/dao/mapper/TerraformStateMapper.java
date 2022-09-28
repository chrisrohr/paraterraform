package com.fortitudetec.foreground.dao.mapper;

import com.fortitudetec.foreground.model.TerraformState;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class TerraformStateMapper implements RowMapper<TerraformState> {

    @Override
    public TerraformState map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TerraformState.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .content(rs.getString("content"))
                .uploadedAt(rs.getObject("uploaded_at", Instant.class))
                .build();
    }
}
