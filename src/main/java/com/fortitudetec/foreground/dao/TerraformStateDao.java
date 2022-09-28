package com.fortitudetec.foreground.dao;

import com.fortitudetec.foreground.dao.mapper.TerraformStateMapper;
import com.fortitudetec.foreground.model.TerraformState;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

@RegisterRowMapper(TerraformStateMapper.class)
public interface TerraformStateDao {

    @SqlQuery("select * from terraform_states")
    List<TerraformState> list();

    @SqlQuery("select * from terraform_states where id = :id")
    TerraformState get(@Bind("id") Long id);

    @SqlUpdate("insert into terraform_states (name, content) values (:name, :content)")
    @GetGeneratedKeys
    Long add(@BindBean TerraformState terraformState);

}
