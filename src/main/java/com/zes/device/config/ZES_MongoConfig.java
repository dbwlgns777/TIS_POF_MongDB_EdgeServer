package com.zes.device.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class ZES_MongoConfig
{
    private static final String ZES_gv_uri = "mongodb://localhost:27017";
    public static final String ZES_gv_databaseName = "TIS_POF_SCADA";
    public static final String ZES_gv_vibrationCollection = "POF_Vibration";
    public static final String ZES_gv_strainGaugeCollection = "POF_StrainGauge";
    public static final String ZES_gv_temperatureCollection = "POF_Temperature";

    private static final MongoClient ZES_gv_mongoClient = MongoClients.create(ZES_gv_uri);
    private static final MongoDatabase ZES_gv_database = ZES_gv_mongoClient.getDatabase(ZES_gv_databaseName);

    private ZES_MongoConfig() {}

    public static MongoCollection<Document> ZES_getCollection(String collectionName)
    {
        return ZES_gv_database.getCollection(collectionName);
    }
}
