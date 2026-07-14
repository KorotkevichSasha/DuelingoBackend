package by.gsu.duelingobackend.util;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

public class PostgreSqlStringArrayType implements UserType<String[]> {

    @Override
    public int getSqlType() {
        return Types.ARRAY;
    }

    @Override
    public Class<String[]> returnedClass() {
        return String[].class;
    }

    @Override
    public boolean equals(String[] strings, String[] j1) {
        return Arrays.deepEquals(strings, j1);
    }

    @Override
    public int hashCode(String[] strings) {
        return Arrays.hashCode(strings);
    }

    @Override
    public String[] nullSafeGet(ResultSet resultSet, int i, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws SQLException {
        Array array = resultSet.getArray(i);
        return array != null ? (String[]) array.getArray() : null;
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, String[] strings, int i, SharedSessionContractImplementor sharedSessionContractImplementor) throws SQLException {
        if (preparedStatement != null) {
            if (strings != null) {
                Array array = sharedSessionContractImplementor.getJdbcConnectionAccess().obtainConnection()
                        .createArrayOf("text", strings);
                preparedStatement.setArray(i, array);
            } else {
                preparedStatement.setNull(i, Types.ARRAY);
            }
        }
    }

    @Override
    public String[] deepCopy(String[] strings) {
        return Arrays.copyOf(strings, strings.length);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(String[] strings) {
        return strings;
    }

    @Override
    public String[] assemble(Serializable serializable, Object o) {
        return new String[0];
    }
}
