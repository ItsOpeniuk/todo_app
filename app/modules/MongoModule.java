package modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;

public class MongoModule extends AbstractModule {

    @Override
    protected void configure() {
        // No explicit bindings are required here
    }

    @Provides
    @Singleton
    public MongoClient provideMongoClient(Config config) {
        String uri = config.getString("mongodb.uri");
        return MongoClients.create(uri);
    }

    @Provides
    @Singleton
    public MongoDatabase provideMongoDatabase(MongoClient mongoClient, Config config) {
        String dbName = config.getString("mongodb.database");
        return mongoClient.getDatabase(dbName);
    }
}


