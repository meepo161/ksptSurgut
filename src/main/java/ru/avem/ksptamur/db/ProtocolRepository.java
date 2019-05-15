package ru.avem.ksptamur.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import ru.avem.ksptamur.db.model.Protocol;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ProtocolRepository extends ru.avem.ksptamur.db.DataBaseRepository {
    public static void createTable(Class dataClass) {
        try (ConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL)) {
            TableUtils.dropTable(connectionSource, dataClass, true);
            TableUtils.createTableIfNotExists(connectionSource, dataClass);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertProtocol(Protocol protocol) {
        sendAction((protocolDao) -> protocolDao.create(protocol));
    }

    public static void updateProtocol(Protocol protocol) {
        sendAction((protocolDao) -> protocolDao.update(protocol));
    }

    public static void deleteProtocol(Protocol protocol) {
        sendAction((protocolDao) -> protocolDao.delete(protocol));
    }

    public static List<Protocol> getAllProtocols() {
        final List[] protocols = {null};
        sendAction((protocolDao) -> protocols[0] = protocolDao.queryForAll());
        return (List<Protocol>) protocols[0];
    }

    public static Protocol getProtocol(long id) {
        final Protocol[] protocol = {null};
        sendAction((protocolDao) -> protocol[0] = protocolDao.queryForId(id));
        return protocol[0];
    }

    @FunctionalInterface
    private interface Actionable {
        void onAction(Dao<Protocol, Long> protocolDao) throws SQLException;
    }

    private static void sendAction(Actionable actionable) {
        try (ConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL)) {
            Dao<Protocol, Long> protocolDao =
                    DaoManager.createDao(connectionSource, Protocol.class);

            actionable.onAction(protocolDao);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
