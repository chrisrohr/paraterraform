package org.paraterra.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.paraterra.dao.mapper.SlimTerraformMapper;
import org.paraterra.dao.mapper.TerraformStateMapper;
import org.paraterra.model.TerraformState;

import java.util.List;
import java.util.Optional;

@RegisterRowMapper(TerraformStateMapper.class)
public interface TerraformStateDao {

    @SqlQuery("select id, name, uploaded_at, updated_by from terraform_states order by uploaded_at desc")
    List<TerraformState> find();

    @RegisterRowMapper(SlimTerraformMapper.class)
    @SqlQuery("select max(uploaded_at) as uploaded_at, name from terraform_states group by name order by uploaded_at desc")
    List<TerraformState> findLatestStates();

    @SqlQuery("select content from terraform_states where name = :name order by uploaded_at desc limit 1")
    Optional<String> findLatestStateContentByName(@Bind("name") String name);

    @SqlQuery("select id, name, uploaded_at, updated_by from terraform_states where name = :name order by uploaded_at desc")
    List<TerraformState> findStateHistoryByName(@Bind("name") String name);

    @SqlQuery("select id, name, uploaded_at, updated_by from terraform_states where id = :id")
    Optional<TerraformState> findById(@Bind("id") Long id);

    @SqlQuery("select content from terraform_states where id = :id")
    Optional<String> findContentById(@Bind("id") Long id);

    @SqlUpdate("insert into terraform_states (name, content, updated_by) values (:name, :content, :updatedBy)")
    @GetGeneratedKeys
    long insert(@BindBean TerraformState terraformState);

    @SqlUpdate("delete from terraform_states where id = :id")
    int deleteById(@Bind("id") Long id);

    @SqlUpdate("delete from terraform_states where name = :name")
    int deleteByName(@Bind("name") String name);
}
