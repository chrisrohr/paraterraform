package org.paraterraform.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.jdbc.KiwiJdbc;
import org.paraterraform.model.TerraformState;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TerraformStateMapper implements RowMapper<TerraformState> {

    @Override
    public TerraformState map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TerraformState.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .uploadedAt(KiwiJdbc.instantFromTimestamp(rs, "uploaded_at"))
                .build();
    }
}
