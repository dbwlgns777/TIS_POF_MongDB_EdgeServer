package com.zes.device.models;

import com.zes.device.config.ZES_MongoConfig;
import org.bson.Document;

import static com.zes.device.ZES_TimeUtil.convertTimestampToDateFormat;

public abstract class ZES_TypeMongoDB extends ZES_Type
{
    public ZES_TypeMongoDB(long timestamp, byte[] bytes, String ictNumber)
    {
        super(timestamp, bytes, ictNumber);
    }

    @Override
    protected void ZES_parseData(ZES_Data data)
    {
        switch (data.ZES_gv_dataType)
        {
            case "long":
                data.setValue(ZES_getLong(ZES_gv_bytes, data.ZES_gv_offset, data.ZES_gv_size));
                break;
            case "double":
                data.setValue(ZES_getDouble(ZES_gv_bytes, data.ZES_gv_offset, data.ZES_gv_delimit_size));
                break;
            case "time":
                data.setValue(ZES_getTime(ZES_gv_bytes, data.ZES_gv_offset));
                break;
        }
    }

    protected void ZES_insertMongoData(ZES_Data[] dataMap, String collectionName)
    {
        Document document = new Document();
        for (int i = 0; i < 10 && i < dataMap.length; i++)
        {
            String key = i == 0 ? "data" : "data" + (i + 1);
            document.append(key, String.valueOf(dataMap[i].ZES_gv_value));
        }
        document.append("timestamp", convertTimestampToDateFormat(ZES_gv_timestamp, "yyyy-MM-dd HH:mm:ss.SSS"));
        ZES_MongoConfig.ZES_getCollection(collectionName).insertOne(document);
    }
}
