package run.mone.m78.service.dao;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.MappedJdbcTypes;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * @author wmin
 * @date 2024/9/20
 */
import com.google.gson.Gson;
import run.mone.m78.api.bo.flow.SyncFlowStatus;

import java.util.Map;

@MappedTypes(Map.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class InputMapTypeHandler extends BaseTypeHandler<Map<String, SyncFlowStatus.SyncNodeInput>> {

    private static final Gson gson = new Gson();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, SyncFlowStatus.SyncNodeInput> parameter, JdbcType jdbcType) throws SQLException {
        String json = gson.toJson(parameter);
        ps.setString(i, json);
    }

    @Override
    public Map<String, SyncFlowStatus.SyncNodeInput> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return json == null ? null : gson.fromJson(json, getType());
    }

    @Override
    public Map<String, SyncFlowStatus.SyncNodeInput> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return json == null ? null : gson.fromJson(json, getType());
    }

    @Override
    public Map<String, SyncFlowStatus.SyncNodeInput> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return json == null ? null : gson.fromJson(json, getType());
    }

    private Type getType() {
        return new TypeToken<Map<String, SyncFlowStatus.SyncNodeInput>>() {}.getType();
    }
}
