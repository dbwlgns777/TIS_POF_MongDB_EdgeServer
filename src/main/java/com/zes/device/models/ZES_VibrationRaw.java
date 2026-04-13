package com.zes.device.models;

import com.zes.device.config.ZES_MongoConfig;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ZES_VibrationRaw extends ZES_TypeMongoDB
{
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int HEADER_START = 0;
    private static final int HEADER_END = 4;
    private static final int FLAG_START = 5;
    private static final int FLAG_END = 8;
    private static final int CHANNEL_KEY_INDEX = 9;
    private static final int COLLECTION_CYCLE_START = 10;
    private static final int COLLECTION_CYCLE_END = 11;
    private static final int COLLECTION_TYPE_INDEX = 12;
    private static final int ICT_START = 13;
    private static final int ICT_END = 19;
    private static final int SENSOR_START = 28;
    private static final int SENSOR_END = 227;

    public ZES_VibrationRaw(long timestamp, byte[] bytes, String ictNumber)
    {
        super(timestamp, bytes, ictNumber);
    }

    @Override
    public ZES_TypeMongoDB ZES_saveRealTime()
    {
        try
        {
            String header = ZES_extractAscii(HEADER_START, HEADER_END);
            long flag = ZES_extractUnsignedLong(FLAG_START, FLAG_END);
            int channel = ZES_gv_bytes[CHANNEL_KEY_INDEX] & 0xFF;
            int collectionCycle = (int) ZES_extractUnsignedLong(COLLECTION_CYCLE_START, COLLECTION_CYCLE_END);
            int sensorCode = ZES_gv_bytes[COLLECTION_TYPE_INDEX] & 0xFF;
            String ictNumber = ZES_extractAscii(ICT_START, ICT_END);
            List<Integer> sensorData = ZES_extractSensorData();
            String collectionName = ZES_extractCollectionName();

            System.out.println("header: " + header);
            System.out.println("flag: " + flag);
            System.out.println("channel: " + channel);
            System.out.println("sensor_code: " + sensorCode);
            System.out.println("ict_number: " + ictNumber);
            int printCount = Math.min(99, sensorData.size());
            StringBuilder sensorDataLog = new StringBuilder();
            for (int i = 0; i < printCount; i++)
            {
                if (i > 0)
                {
                    sensorDataLog.append(", ");
                }
                sensorDataLog.append(sensorData.get(i));
            }
            System.out.println("sensor_data[1.." + printCount + "]: " + sensorDataLog);

            Document document = new Document();
            document.append("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
            document.append("header", header);
            document.append("flag", flag);
            document.append("channel_key", channel);
            document.append("collection_cycle", collectionCycle);
            document.append("ict_number", ictNumber);
            document.append("sensor_data", sensorData);
            ZES_MongoConfig.ZES_getCollection(collectionName).insertOne(document);
        }
        catch (Exception e)
        {
            ZES_handleException(e);
        }
        finally
        {
            return this;
        }
    }

    private String ZES_extractAscii(int start, int end)
    {
        byte[] value = new byte[end - start + 1];
        System.arraycopy(ZES_gv_bytes, start, value, 0, value.length);
        return new String(value);
    }

    private List<Integer> ZES_extractSensorData()
    {
        List<Integer> sensorData = new ArrayList<>();
        for (int i = SENSOR_START; i <= SENSOR_END; i += 2)
        {
            int high = ZES_gv_bytes[i] & 0xFF;
            int low = ZES_gv_bytes[i + 1] & 0xFF;
            sensorData.add((high << 8) | low);
        }
        return sensorData;
    }

    private long ZES_extractUnsignedLong(int start, int end)
    {
        long value = 0;
        for (int i = start; i <= end; i++)
        {
            value = (value << 8) | (ZES_gv_bytes[i] & 0xFFL);
        }
        return value;
    }

    private String ZES_extractCollectionName()
    {
        int collectionType = ZES_gv_bytes[COLLECTION_TYPE_INDEX] & 0xFF;
        if (collectionType == 1)
        {
            return ZES_MongoConfig.ZES_gv_vibrationCollection;
        }
        if (collectionType == 2)
        {
            return ZES_MongoConfig.ZES_gv_strainGaugeCollection;
        }

        return ZES_MongoConfig.ZES_gv_vibrationCollection;
    }
}
