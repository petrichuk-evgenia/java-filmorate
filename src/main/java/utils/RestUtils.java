package utils;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpHeaders.CONNECTION;
import static org.apache.http.protocol.HTTP.CONN_KEEP_ALIVE;

/**
 * Класс, содержащий базовые методы для отправки rest запросов
 */
public class RestUtils {

    /**
     * Отправка get запроса с параметрами без указания спецификации запросов
     *
     * @param url     - url
     * @param cookie  - cookie
     * @param headers - заголовки
     * @param params  - параметры
     * @return Response - ответ запроса
     */
    public static Response get(final String url, final Cookies cookie, final Map<String, String> headers, final Map<String, String> params) {
        final Map<String, String> queryParameters = Optional.ofNullable(params).orElseGet(HashMap::new);
        final Cookies cookies = Optional.ofNullable(cookie).orElseGet(Cookies::new);
        final Map<String, String> headersMap = getHeaders(headers);
        return given()
                .headers(headersMap)
                .cookies(cookies)
                .params(queryParameters)
                .log().all()
                .get(url)
                .then()
                .log().all()
                .extract().response();
    }

    /**
     * Отправка get запроса с параметрами и указанием спецификации запросов
     *
     * @param url                  - url
     * @param cookie               - cookies
     * @param headers              - заголовки
     * @param params               - параметры
     * @param requestSpecification - спецификация запросов
     * @return Response - ответ запроса
     */
    public static Response getWithSpec(final String url, final Cookies cookie, final Map<String, String> headers,
                                       final Map<String, String> params, RequestSpecification requestSpecification) {

        final Map<String, String> queryParameters = Optional.ofNullable(params).orElseGet(HashMap::new);
        final Cookies cookies = Optional.ofNullable(cookie).orElseGet(Cookies::new);
        final Map<String, String> headersMap = getHeaders(headers);

        return given()
                .spec(requestSpecification)
                .headers(headersMap)
                .params(queryParameters)
                .cookies(cookies)
                .log().all()
                .get(url)
                .then()
                .log().all()
                .extract().response();
    }

    /**
     * Отправка get запроса на url со спецификацией, содержащей заголовок ContentType
     *
     * @param url         - url
     * @param contentType - contentType в формате строки
     * @return Response - ответ запроса
     */
    public static Response get(final String url, String contentType) {
        return getWithSpec(url, null, null, null, buildReqSpecificationWithContentType(contentType));
    }

    /**
     * Отправка post запроса с body
     *
     * @param url     - url
     * @param body    - тело запроса
     * @param headers - заголовки
     * @return Response - ответ запроса
     */
    public static Response post(String url, Object body, Map<String, String> headers) {
        Map<String, String> headersMap = getHeaders(headers);
        return given()
                .headers(headersMap)
                .body(body)
                .log().all()
                .post(url)
                .then()
                .log().all()
                .extract().response();
    }

    /**
     * Отправка put запроса с body
     *
     * @param url     - url
     * @param body    - тело запроса
     * @param headers - заголовки
     * @return Response - ответ запроса
     */
    public static Response put(String url, Object body, Map<String, String> headers) {
        Map<String, String> headersMap = getHeaders(headers);
        return given()
                .headers(headersMap)
                .body(body)
                .log().all()
                .put(url)
                .then()
                .log().all()
                .extract().response();
    }

    /**
     * Отправка put запроса без body
     *
     * @param url     - url
     * @param headers - заголовки
     * @return Response - ответ запроса
     */
    public static Response put(String url, ContentType contentType, Map<String, String> headers) {
        Map<String, String> headersMap = getHeaders(headers);
        return given()
                .headers(headersMap)
                .contentType(contentType)
                .log().all()
                .put(url)
                .then()
                .log().all()
                .extract().response();
    }

    /**
     * Получение заголовков запроса.
     * К заголовкам ("Connection":"Keep-Alive") добавляются переданные в параметрах заголовки
     *
     * @param headers - добавляемые заголовки
     * @return Map<String, String> заголовки запросов
     */
    public static Map<String, String> getHeaders(Map<String, String> headers) {
        final Map<String, String> headersMap = new HashMap<>();
        headersMap.put(CONNECTION, CONN_KEEP_ALIVE);
        if (headers == null) return headersMap;
        headersMap.putAll(headers);
        return headersMap;
    }

    /**
     * Метод для создания RequestSpecification
     * Сейчас метод задает только заголовок Content-Type, при необходимости
     * команды могут расширить функционал метода
     *
     * @param contentType - contentType
     * @return RequestSpecification спецификация запроса
     */
    public static RequestSpecification buildReqSpecificationWithContentType(String contentType) {
        return new RequestSpecBuilder()
                .addHeader("Content-Type", contentType)
                .build();
    }

    public static void setRestAssuredConfigHttp() {
        RestAssured.config = RestAssuredConfig.newConfig().httpClient(HttpClientConfig.httpClientConfig());
    }

    public static void setRestAssuredConfigHttps() {
        RestAssured.useRelaxedHTTPSValidation();
    }

    /**
     * Отправка delete запроса
     *
     * @param url - url
     * @return Response - ответ запроса
     */
    public static Response delete(String url, ContentType contentType, Map<String, String> headers) {
        return given()
                .headers(headers)
                .contentType(contentType)
                .log().all()
                .delete(url)
                .then()
                .log().all()
                .extract().response();
    }
}
