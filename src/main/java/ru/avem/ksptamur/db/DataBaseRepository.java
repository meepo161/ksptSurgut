package ru.avem.ksptamur.db;

import ru.avem.ksptamur.db.model.Account;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.db.model.TestItem;

import java.io.File;

public abstract class DataBaseRepository {
    public static String DATABASE_NAME = "ksptamurDataBase.db";
    protected static String DATABASE_URL = "jdbc:" + "sqlite:" + DATABASE_NAME;

    public static void init(boolean forceInit) {
        if (!new File(DATABASE_NAME).exists() || forceInit) {
            AccountRepository.createTable(Account.class);

            Account ivanov = new Account("ivanov", "1234", "Исполнитель-1", "148", "Иванов И. И.");
            AccountRepository.insertAccount(ivanov);

            Account petrov = new Account("petrov", "1234", "Исполнитель-2", "841", "Петров П. П.");
            AccountRepository.insertAccount(petrov);

            TestItemRepository.createTable(TestItem.class);

            TestItem testItem1 = new TestItem("500В", 500.0, 220.0, 2.0, 3.0, 32.0, 4.0, 60.0, 3000, 1.0);
            TestItemRepository.insertTestItem(testItem1);
            TestItemRepository.insertTestItem(new TestItem("1000В", 1000.0, 220.0, 2.0, 3.0, 32.0, 4.0, 60.0, 3000, 1.0));

            ProtocolRepository.createTable(Protocol.class);

            Protocol protocol = new Protocol("SN1", testItem1, ivanov, petrov, System.currentTimeMillis());
            ProtocolRepository.insertProtocol(protocol);
        }
    }
}
