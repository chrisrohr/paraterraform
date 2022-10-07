package com.fortitudetec.foreground.dao;

import com.fortitudetec.foreground.dao.mapper.SlimTerraformMapper;
import com.fortitudetec.foreground.dao.mapper.TerraformStateMapper;
import com.fortitudetec.foreground.model.TerraformState;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

@RegisterRowMapper(TerraformStateMapper.class)
public interface TerraformStateDao {

    @SqlQuery("select id, name, uploaded_at from terraform_states")
    List<TerraformState> list();

    @RegisterRowMapper(SlimTerraformMapper.class)
    @SqlQuery("SELECT MAX(uploaded_at) as uploaded_at, name " +
            "from terraform_states group by name;")
    List<TerraformState> listMostRecentOfEachFile();

    @SqlQuery("select id, name, uploaded_at from terraform_states where id = :id")
    Optional<TerraformState> findById(@Bind("id") Long id);

    @SqlQuery("select content from terraform_states where id = :id")
    Optional<String> findContentById(@Bind("id") Long id);

    @SqlUpdate("insert into terraform_states (name, content) values (:name, :content)")
    @GetGeneratedKeys
    long insert(@BindBean TerraformState terraformState);

    @SqlUpdate("delete from terraform_states where id = :id")
    int deleteById(@Bind("id") Long id);
}
