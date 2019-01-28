package com.myspringboot.commonspringboot.view;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页 JSON 视图
 *
 * @author yzy
 */
public class PagingJsonView<T> extends JsonView<Map<String, Object>> {

    /**
     * 创建一个分页视图 数据Map
     *
     * @param dataList 数据列表
     * @param total    总数
     * @param pageNo   页码， 大于1
     * @param pageSize 每页展示数量， 大于1
     * @param <T>      模型类型
     * @return 返回一个DataMap, 包含属性 dataList, pageNo, pageSize, total
     */
    public static <T> Map<String, Object> createPagingDataMap(List<T> dataList, int total, int pageNo, int pageSize) throws Exception {
        Map<String, Object> dataMap = new HashMap<>(4);
        dataList = null == dataList ? new ArrayList<T>() : dataList;

        if (pageNo < 1 || pageSize < 1) {
            throw new Exception("分页参数不正确，要求 pageNo > 0 并且 pageSize > 0");
        }

        dataMap.put("dataList", dataList);
        dataMap.put("pageNo", pageNo);
        dataMap.put("pageSize", pageSize);
        dataMap.put("total", total);

        return dataMap;
    }

    public PagingJsonView(List<T> dataList, int total, int pageNo, int pageSize) throws Exception {
        this.setData(createPagingDataMap(dataList, total, pageNo, pageSize));
    }

}
