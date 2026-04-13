# DEVICE SERVER (MongoDB)

## DATA FLOW

1. 설비에서 230 bytes TCP 패킷 수신
2. 패킷 파싱
   - Header: byte[0] ~ byte[9] (ASCII 문자열)
   - ICT_NUMBER: byte[10] ~ byte[19] (ASCII 문자열)
   - Sensor_Data: byte[26] ~ byte[225] (2bytes 단위, 총 100개)
3. MongoDB `TIS_POF_SCADA.POF_Vibration` 컬렉션에 insert

## MongoDB Document 형태

```json
{
  "Header": "TIS_POF_01",
  "ICT_NUMBER": "0000000001",
  "Sensor_Data": [1, 2, 3, 4, 5, ...]
}
```

## 실행

```shell
./gradlew build
nohup java -jar ${생성된 jar file} ${PORT번호(기본값 9600)} &
```
