package utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

/**
 * Методы для работы с json-файлами.
 */
@Slf4j
public class JsonUtils {
    private static final ObjectMapper objectMapper = setMapper();
    private static Gson gson = new Gson();

    private static ObjectMapper setMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    /**
     * Формирует json-строку из любого объекта и возвращает её
     * Пример использования в тесте checkGetDtoAsJsonStringTest() класса ExampleUtilsTest
     *
     * @param dto объект для преобразования
     * @return {@link String} в json формате
     */
    public static String getDtoAsJsonString(Object dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.info("Ошибка преобразования JSON ", e);
            throw new RuntimeException("Не удалось преобразовать json в String");
        }
    }

    public static <T> T convertFromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }
}
