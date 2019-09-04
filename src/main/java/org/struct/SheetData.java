package org.struct;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Author: super bao
 * @Date: 2019/8/30 20:54
 */
public class SheetData {

    private String sheetName;
    private boolean mainSheet;
    private boolean globalSheet;
    private boolean argSheet;
    private String argSheetName;
    private String argName;
    private String argKeyName;

    /** string类型**/
    private Set<String> colStringTypeSet = new HashSet<>();

    private Map<Integer, String> colNameMap = new HashMap<>();
    /** 第一列的名字 **/
    private Map<Integer, String> firstColValueMap = new HashMap<>();
    /** 普通数据Map **/
    private Map<String, Map<String,String>> dataMap = new HashMap<>();
    /** 参数数据map (参数名字-(id-(字段名-字段值))**/
    private Map<String, Map<String, Map<String,String>>> argDataMap = new HashMap<>();



    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public Map<Integer, String> getColNameMap() {
        return colNameMap;
    }

    public void setColNameMap(Map<Integer, String> colNameMap) {
        this.colNameMap = colNameMap;
    }

    public Map<String, Map<String, String>> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Map<String, String>> dataMap) {
        this.dataMap = dataMap;
    }

    public boolean isMainSheet() {
        return mainSheet;
    }

    public void setMainSheet(boolean mainSheet) {
        this.mainSheet = mainSheet;
    }

    public boolean isGlobalSheet() {
        return globalSheet;
    }

    public void setGlobalSheet(boolean globalSheet) {
        this.globalSheet = globalSheet;
    }

    public Map<Integer, String> getFirstColValueMap() {
        return firstColValueMap;
    }

    public void setFirstColValueMap(Map<Integer, String> firstColValueMap) {
        this.firstColValueMap = firstColValueMap;
    }

    public Map<String, Map<String, Map<String, String>>> getArgDataMap() {
        return argDataMap;
    }

    public void setArgDataMap(Map<String, Map<String, Map<String, String>>> argDataMap) {
        this.argDataMap = argDataMap;
    }

    public boolean isArgSheet() {
        return argSheet;
    }

    public void setArgSheet(boolean argSheet) {
        this.argSheet = argSheet;
    }

    public String getArgSheetName() {
        return argSheetName;
    }

    public void setArgSheetName(String argSheetName) {
        this.argSheetName = argSheetName;
    }

    public String getArgName() {
        return argName;
    }

    public void setArgName(String argName) {
        this.argName = argName;
    }

    public String getArgKeyName() {
        return argKeyName;
    }

    public void setArgKeyName(String argKeyName) {
        this.argKeyName = argKeyName;
    }

    public Set<String> getColStringTypeSet() {
        return colStringTypeSet;
    }

    public void setColStringTypeSet(Set<String> colStringTypeSet) {
        this.colStringTypeSet = colStringTypeSet;
    }
}
