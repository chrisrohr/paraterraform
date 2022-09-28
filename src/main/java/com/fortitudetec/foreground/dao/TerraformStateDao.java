package com.fortitudetec.foreground.dao;

import com.fortitudetec.foreground.dao.mapper.TerraformStateMapper;
import com.fortitudetec.foreground.model.TerraformState;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

@RegisterRowMapper(TerraformStateMapper.class)
public interface TerraformStateDao {

    @SqlQuery("select * from terraform_states")
    List<TerraformState> list();
}
