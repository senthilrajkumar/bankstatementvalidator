package com.nl.rabobank.bankstatementvalidator.util;

import com.nl.rabobank.bankstatementvalidator.constant.ApplicationConstant;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public final class CommonUtil {

    // predicate to filter the duplicates by the given key extractor.
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> uniqueMap = new ConcurrentHashMap<>();
        return t -> uniqueMap.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static boolean hasCSVFormat(MultipartFile file) {
        return ApplicationConstant.CSV_TYPE.equals(file.getContentType());
    }

    public static boolean hasXMLFormat(MultipartFile file) {
        return ApplicationConstant.TEXT_XML_TYPE.equals(file.getContentType())
                || ApplicationConstant.APP_XML_TYPE.equals(file.getContentType());
    }

}
