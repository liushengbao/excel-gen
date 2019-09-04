package org;
import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.constant.GLOBAL;
import org.struct.SheetData;
import org.util.FileUtil;
import org.util.LogUtil;
import org.util.NumberUtil;
import org.util.StringUtil;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * excel表生成 json 和 lua
 *
 * <P>
 *     使用maven 打包
 * </P>
 *
 *
 * /// bat or shell : java -jar tools\excel-gen-1.0-SNAPSHOT.jar D-测试配置.xlsx ..//程序配置文件//
 *
 * @author bao
 */
public class App {

    public static void main(String[] args) {
        String filePath = args[0];
        String outDir = args[1];
        FileUtil.checkCreateDir(outDir);

        String jsonDir = outDir + File.separator + "server-conf" + File.separator;
        FileUtil.checkCreateDir(jsonDir);

        String luaDir = outDir + File.separator + "conf" + File.separator;
        FileUtil.checkCreateDir(luaDir);

        long start = System.currentTimeMillis();
        processExcel(filePath, jsonDir, luaDir);
        LogUtil.info("处理完毕 cost:" + (System.currentTimeMillis() - start));
    }

    private static boolean checkExcelFile(File file) {
        if (file.isFile() && !file.getName().startsWith("~$") && (file.getName().endsWith(".xlsx") || file.getName().endsWith(".xls"))) {
            return true;
        }
        return false;
    }

    private static boolean checkSheetName(String name) {
        if (name.startsWith("#")) {
            return false;
        }
        if (name.startsWith("Sheet")) {
            return false;
        }
        return true;
    }

    private static void processExcel(String filePath, String jsonDir, String luaDir) {
        File file = new File(filePath);
        if (!checkExcelFile(file)) {
            return;
        }
        LogUtil.info("处理 >>>>>> " + file.getName());
        long s1 = System.currentTimeMillis();

        Map<String, SheetData> sheetDataMap = new HashMap<>();
        InputStream is = null;
        Workbook workbook = null;
        try {
            is = new FileInputStream(file);
            workbook = StreamingReader.builder()
                    .rowCacheSize(1000)    // number of rows to keep in memory (defaults to 10)
                    .bufferSize(8096)     // buffer size to use when reading InputStream to file (defaults to 1024)
                    .open(is);            // InputStream or File for XLSX file (required)
            for (Sheet sheet : workbook) {
                if (!checkSheetName(sheet.getSheetName())) {
                    continue;
                }

                SheetData sheetData = null;
                String firstValue = null;
                for (Row r : sheet) {
                    if (r == null || r.getCell(0) == null) {
                        continue;
                    }

                    for (Cell c : r) {
                        String value = c.getStringCellValue().trim();
                        if (r.getRowNum() == 0 && c.getColumnIndex() == 0) { // 第一行第一列
                            firstValue = value;
                            if (firstValue.indexOf("[main]") != -1) {
                                sheetData = new SheetData();
                                sheetData.setMainSheet(true);
                                sheetData.setSheetName(sheet.getSheetName());
                                sheetDataMap.put(sheet.getSheetName(), sheetData);
                            } else if (firstValue.startsWith("[hebing]")) {
                                sheetData = sheetDataMap.values().stream().filter(v -> v.isMainSheet()).findFirst().orElse(null);
                            } else if (firstValue.indexOf("[key]") != -1) { //global配置类型
                                sheetData = new SheetData();
                                sheetData.setGlobalSheet(true);
                                sheetData.setSheetName(sheet.getSheetName());
                                sheetDataMap.put(sheet.getSheetName(), sheetData);
                            } else if (firstValue.indexOf("[not]") != -1) { //某个表的参数对象
                                sheetData = new SheetData();
                                sheetData.setArgSheet(true);
                                sheetData.setSheetName(sheet.getSheetName());
                                sheetDataMap.put(sheet.getSheetName(), sheetData);
                            } else {
                                sheetData = new SheetData();
                                sheetData.setSheetName(sheet.getSheetName());
                                sheetDataMap.put(sheet.getSheetName(), sheetData);
                            }
                        } else if (r.getRowNum() == 1) {
                            if (firstValue == null) {
                                if (sheetData == null) {
                                    sheetData = new SheetData();
                                    sheetData.setSheetName(sheet.getSheetName());
                                    sheetDataMap.put(sheet.getSheetName(), sheetData);
                                }
                            }
                            if (firstValue != null && firstValue.indexOf("[not]") != -1) {
                                if (c.getColumnIndex() == 0) {
                                    String[] split = value.split("!");
                                    sheetData.setArgSheetName(split[1]);
                                    sheetData.setArgName(split[3]);
                                    sheetData.setArgKeyName(split[2]);
                                }
                                sheetData.getColNameMap().put(c.getColumnIndex(), value);
                            } else {
                                // 保存每个列映射字段名
                                sheetData.getColNameMap().put(c.getColumnIndex(), value);
                            }
                        } else {
                            if (r.getRowNum() <= 1) {
                                continue;
                            }
                            if (sheetData.isGlobalSheet()) { //处理gloabl类型
                                if (StringUtil.isEmpty(value)) {//空列不处理
                                    continue;
                                }
                                if (c.getColumnIndex() == 0) {
                                    Map<String, String> dataMap = sheetData.getDataMap().get(GLOBAL.GLOBAL_KEY);
                                    if (dataMap == null) {
                                        dataMap = new HashMap<>();
                                        sheetData.getDataMap().put(GLOBAL.GLOBAL_KEY, dataMap);
                                    }

                                    Cell tempCell = r.getCell(1);
                                    if (tempCell == null || StringUtil.isEmpty(tempCell.getStringCellValue()) || StringUtil.isEmpty(tempCell.getStringCellValue().trim())) {
                                        continue;
                                    }
                                    dataMap.put(value, tempCell.getStringCellValue().trim());
                                }

                            } else { // 普通map型
                                if (c.getColumnIndex() == 0) { //第一列
                                    // 空行不处理
                                    if (StringUtil.isEmpty(value)) {
                                        break;
                                    }

                                    if (sheetData.getDataMap().containsKey(value)) {
                                        LogUtil.error("file:" + file.getName() + " sheet:" + sheet.getSheetName() + " id 重复 >>>>" + value);
                                    }
                                    sheetData.getDataMap().put(value, new HashMap<>());
                                    sheetData.getFirstColValueMap().put(r.getRowNum(), value);
                                }

                                String colName = sheetData.getColNameMap().get(c.getColumnIndex());
                                if (StringUtil.isEmpty(colName) || StringUtil.isEmpty(value) || colName.startsWith("!") || colName.startsWith("not-")) {
                                    continue;
                                }

                                // 拿出第一列的值做为id
                                String firstColValue = sheetData.getFirstColValueMap().get(r.getRowNum());
                                Map<String, String> colValueMap = sheetData.getDataMap().get(firstColValue);
                                colValueMap.put(colName, value);
                                // 不是数字 不是数组
                                if (!NumberUtil.isNumber(value) && (!value.startsWith("[") && !value.endsWith("]"))) {
                                    sheetData.getColStringTypeSet().add(colName);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Collection<SheetData> values = sheetDataMap.values();
        //注入参数
        values.stream().parallel().forEach(sheetData -> {
            if (sheetData.isArgSheet()) {
                SheetData mainSheetData = values.stream().filter(v -> v.isMainSheet()).findFirst().orElse(null);
                if (mainSheetData == null) {
                    LogUtil.error("注入参数错误,缺少主Sheet表, 参数Sheet表:" + sheetData.getSheetName());
                }

                // 把数据放入主sheet ArgDataMap
                sheetData.getDataMap().entrySet().stream().forEach(e -> {
                    Map<String, Map<String, String>> dataMap = mainSheetData.getArgDataMap().get(e.getKey());//id
                    if (dataMap == null) {
                        dataMap = new HashMap<>(sheetData.getDataMap().size());
                        mainSheetData.getArgDataMap().put(e.getKey(), dataMap);
                    }

                    Map<String, String> stringStringMap = dataMap.get(sheetData.getArgName());
                    if (stringStringMap == null) {
                        stringStringMap = new HashMap<>(e.getValue().size());
                        dataMap.put(sheetData.getArgName(), stringStringMap);
                    }

                    stringStringMap.putAll(e.getValue());
                });
            }
        });
        values.stream().parallel().forEach(sheetData -> {
            if (!sheetData.isArgSheet()) {
                String json = toJson(sheetData, null, sheetData);
                String jsonFileName = jsonDir + sheetData.getSheetName() + ".json";
                FileUtil.writeFile(jsonFileName, json);
                LogUtil.info("导出json >>>>" + sheetData.getSheetName() + ".json");
                String lua = toLua(sheetData, null, sheetData);
                String luaFileName = luaDir + sheetData.getSheetName() + ".lua";
                FileUtil.writeFile(luaFileName, lua);
                LogUtil.info("导出lua >>>>" + sheetData.getSheetName() + ".lua");
            }
        });
    }

    private static String toLua(SheetData sheetData, String key, Object object) {
        StringBuilder sb = new StringBuilder();
        if (object instanceof SheetData) {
            sb.append("return {\r\n");
            SheetData castObj = (SheetData) object;
            AtomicInteger count = new AtomicInteger(0);
            if (castObj.isGlobalSheet()) { //单个global对象
                Collection<Map<String, String>> values = castObj.getDataMap().values();
                for (Map<String, String> m : values) {
                    m.entrySet().forEach(e -> {
                        String format = String.format("%s = %s", e.getKey(), toLua(sheetData, e.getKey(), e.getValue()));
                        if (count.incrementAndGet() >= m.size()) {
                            sb.append(format + "\r\n");
                        } else {
                            sb.append(format + ",\r\n");
                        }
                    });
                }
            } else { // 非global类型
                castObj.getDataMap().entrySet().forEach(e -> {
                    sb.append(String.format("  [\"%s\"] = {\r\n", e.getKey()));

                    //args
                    Map<String, Map<String, String>> stringMapMap = sheetData.getArgDataMap().getOrDefault(e.getKey(), new HashMap<>());
                    if (stringMapMap != null && !stringMapMap.isEmpty()) {
                        stringMapMap.entrySet().forEach(ee -> {
                            sb.append(String.format("  %s = {\n%s  },\r\n", ee.getKey(), toLua(sheetData, ee.getKey(), ee.getValue())));
                        });
                        sb.append(toLua(sheetData, e.getKey(), e.getValue()));
                    } else {
                        sb.append(toLua(sheetData, e.getKey(), e.getValue()));
                    }
                    sb.append("  }");
                    if (count.incrementAndGet() >= castObj.getDataMap().size()) {
                        sb.append("\r\n");
                    } else {
                        sb.append(",\r\n");
                    }
                });
            }
            sb.append("}");
        } else if (object instanceof Map) {
            Map<String, String> castObj = (Map<String, String>) object;
            AtomicInteger count = new AtomicInteger(0);
            castObj.entrySet().forEach(e -> {
                String format = String.format("  %s = %s", e.getKey(), toLua(sheetData, e.getKey(), e.getValue()));
                if (count.incrementAndGet() >= castObj.size()) {
                    sb.append(format + "\r\n");
                } else {
                    sb.append(format + ",\r\n");
                }
            });
        } else if (object instanceof String) {
            String castObj = (String) object;
            if (!sheetData.getColStringTypeSet().contains(key) && (NumberUtil.isNumber(castObj))) {
                sb.append(castObj);
            } else if (castObj.startsWith("[") && castObj.endsWith("]")) {
                return castObj.replace("[", "{").replace("]", "}");
            } else {
                sb.append(String.format("\"%s\"", castObj));
            }
        }
        return sb.toString();
    }


    public static String toJson(SheetData sheetData, String key, Object object) {
        StringBuilder sb = new StringBuilder();
        if (object instanceof SheetData) {
            sb.append("{").append("\r\n");
            SheetData castObj = (SheetData) object;
            AtomicInteger count = new AtomicInteger(0);
            if (castObj.isGlobalSheet()) { //单个global对象
                for (Map<String, String> tempMap : castObj.getDataMap().values()) {
                    tempMap.entrySet().stream().sorted((a, b) -> StringUtil.compare(a.getKey(), b.getKey())).forEach(e -> {
                        String format = String.format("\t\"%s\":%s", e.getKey(), toJson(sheetData, e.getKey(), e.getValue()));
                        if (count.incrementAndGet() >= tempMap.size()) {
                            sb.append(format + "\r\n");
                        } else {
                            sb.append(format + ",\r\n");
                        }
                    });
                }
            } else { // 非global类型
                castObj.getDataMap().entrySet().stream().sorted((a, b) -> StringUtil.compare(a.getKey(), b.getKey())).forEach(e -> {
                    sb.append(String.format("\t\"%s\":{\r\n", e.getKey()));
                    //args
                    Map<String, Map<String, String>> stringMapMap = sheetData.getArgDataMap().getOrDefault(e.getKey(), new HashMap<>());
                    if (stringMapMap != null && !stringMapMap.isEmpty()) {
                        stringMapMap.entrySet().forEach(ee -> {
                            sb.append(String.format("\t\t\"%s\":{\n%s\t\t},\r\n", ee.getKey(), toJson(sheetData, ee.getKey(), ee.getValue())));
                        });
                        sb.append("" + toJson(sheetData, e.getKey(), e.getValue()));
                    } else {
                        sb.append("" + toJson(sheetData, e.getKey(), e.getValue()));
                    }
                    sb.append("\t}");
                    if (count.incrementAndGet() >= castObj.getDataMap().size()) {
                        sb.append("\r\n");
                    } else {
                        sb.append(",\r\n");
                    }
                });
            }
            sb.append("}").append("\r\n");

        } else if (object instanceof Map) {
            Map<String, String> castObj = (Map<String, String>) object;
            AtomicInteger count = new AtomicInteger(0);
            castObj.entrySet().stream().sorted((a, b) -> StringUtil.compare(a.getKey(), b.getKey())).forEach(e -> {
                String format = String.format("\t\t\"%s\":%s", e.getKey(), toJson(sheetData, e.getKey(), e.getValue()));
                if (count.incrementAndGet() >= castObj.size()) {
                    sb.append(format + "\r\n");
                } else {
                    sb.append(format + ",\r\n");
                }
            });
        } else if (object instanceof String) {
            String castObj = (String) object;
            if (!sheetData.getColStringTypeSet().contains(key) && (castObj.startsWith("[") || NumberUtil.isNumber(castObj))) {
                sb.append(castObj);
            } else {
                sb.append(String.format("\"%s\"", castObj));
            }
        }
        return sb.toString();
    }


}
