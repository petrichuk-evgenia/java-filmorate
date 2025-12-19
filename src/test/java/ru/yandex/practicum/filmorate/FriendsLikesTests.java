package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import utils.RestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class FriendsLikesTests extends BaseTest {

    private void fillUsersFilms() {
        for (int i = 1; i <= 20; i++) {
            User user = User.builder()
                    .email(String.format("eva%d@gmail.com", i))
                    .login(String.format("Eva%d", i))
                    .birthday(LocalDate.of(1987, 4, 1))
                    .build();
            RestUtils.post(getUrl("/users"), user, headers);
            Film film = Film.builder()
                    .name(Integer.toString(i))
                    .description(Integer.toString(i))
                    .releaseDate(LocalDate.of(1895, 12, 28))
                    .duration(1)
                    .build();
            RestUtils.post(getUrl("/films"), film, headers);
        }
    }

    @Test
    public void addToFriendsValidTest() {
        fillUsersFilms();
        Response resp = RestUtils.put(getUrl("/users/1/friends/6"), ContentType.JSON, headers);
        Assert.assertEquals(resp.statusCode(), 200, "Статус PUT /users/1/friends/6 должен быть 200");
        Assert.assertTrue(resp.as(Map.class).get("message").equals("Пользователи 1 и 6 теперь друзья"));
    }

    @Test
    public void addLikeValidTest() {
        fillUsersFilms();
        Response resp = RestUtils.put(getUrl("/films/1/like/6"), ContentType.JSON, headers);
        Assert.assertEquals(resp.statusCode(), 200, "Статус PUT /films/1/like/6 должен быть 200");
        Assert.assertTrue(resp.as(Map.class).get("message").equals("Пользователь Eva6 поставил лайк фильму 1"));
    }

    @Test
    public void addToFriendsUserNotFoundTest() {
        fillUsersFilms();
        Response resp1 = RestUtils.put(getUrl("/users/1/friends/21"), ContentType.JSON, headers);
        Assert.assertEquals(resp1.statusCode(), 404, "Статус PUT /users/1/friends/21 должен быть 404");
        Assert.assertTrue(resp1.as(Map.class).get("error").equals("Один из пользователей не найден"));
        Response resp2 = RestUtils.put(getUrl("/users/21/friends/1"), ContentType.JSON, headers);
        Assert.assertEquals(resp2.statusCode(), 404, "Статус PUT /users/21/friends/1 должен быть 404");
        Assert.assertTrue(resp2.as(Map.class).get("error").equals("Один из пользователей не найден"));
    }

    @Test
    public void addLikeNotFoundTest() {
        fillUsersFilms();
        Response resp1 = RestUtils.put(getUrl("/films/1/like/21"), ContentType.JSON, headers);
        Assert.assertEquals(resp1.statusCode(), 404, "Статус PUT /films/1/like/21 должен быть 404");
        Assert.assertTrue(resp1.as(Map.class).get("error").equals("Пользователь с id=21 не найден"));
        Response resp2 = RestUtils.put(getUrl("/films/21/like/1"), ContentType.JSON, headers);
        Assert.assertEquals(resp2.statusCode(), 404, "Статус PUT /films/21/like/1 должен быть 404");
        Assert.assertTrue(resp2.as(Map.class).get("error").equals("Фильм с id=21 не найден"));
    }

    @Test
    public void addToFriendsUserInvalidIdTest() {
        fillUsersFilms();
        Response resp1 = RestUtils.put(getUrl("/users/aaa/friends/1"), ContentType.TEXT, headers);
        Assert.assertEquals(resp1.statusCode(), 500, "Статус PUT /users/aaa/friends/1 должен быть 500");
        Assert.assertTrue(resp1.as(Map.class).get("error").toString().contains("Произошла непредвиденная ошибка"));
        Response resp2 = RestUtils.put(getUrl("/users/1/friends/aaa"), ContentType.TEXT, headers);
        Assert.assertEquals(resp2.statusCode(), 500, "Статус PUT /users/1/friends/aaa должен быть 500");
        Assert.assertTrue(resp2.as(Map.class).get("error").toString().contains("Произошла непредвиденная ошибка"));
    }

    @Test
    public void addLikeInvalidIdTest() {
        fillUsersFilms();
        Response resp1 = RestUtils.put(getUrl("/users/aaa/friends/1"), ContentType.TEXT, headers);
        Assert.assertEquals(resp1.statusCode(), 500, "Статус PUT /users/aaa/friends/1 должен быть 500");
        Assert.assertTrue(resp1.as(Map.class).get("error").toString().contains("Произошла непредвиденная ошибка"));
        Response resp2 = RestUtils.put(getUrl("/users/1/friends/aaa"), ContentType.TEXT, headers);
        Assert.assertEquals(resp2.statusCode(), 500, "Статус PUT /users/1/friends/aaa должен быть 500");
        Assert.assertTrue(resp2.as(Map.class).get("error").toString().contains("Произошла непредвиденная ошибка"));
    }

    @Test
    public void deleteFromFriendsValidTest() {
        fillUsersFilms();
        Response resp = RestUtils.put(getUrl("/users/1/friends/6"), ContentType.JSON, headers);
        Assert.assertEquals(resp.statusCode(), 200, "Статус PUT /users/1/friends/6 должен быть 200");
        Assert.assertTrue(resp.as(Map.class).get("message").equals("Пользователи 1 и 6 теперь друзья"));
        Response resp1 = RestUtils.delete(getUrl("/users/1/friends/6"), ContentType.JSON, headers);
        Assert.assertEquals(resp1.statusCode(), 200, "Статус DELETE /users/1/friends/6 должен быть 200");
        Assert.assertTrue(resp1.as(Map.class).get("message").equals("Пользователи 1 и 6 больше не друзья"));
    }

    @Test
    public void deleteLikeValidTest() {
        fillUsersFilms();
        Response resp = RestUtils.put(getUrl("/films/1/like/6"), ContentType.JSON, headers);
        Assert.assertEquals(resp.statusCode(), 200, "Статус PUT /users/1/friends/6 должен быть 200");
        Assert.assertTrue(resp.as(Map.class).get("message").equals("Пользователь Eva6 поставил лайк фильму 1"));
        Response resp1 = RestUtils.delete(getUrl("/films/1/like/6"), ContentType.JSON, headers);
        Assert.assertEquals(resp1.statusCode(), 200, "Статус DELETE /films/1/like/6 должен быть 200");
        Assert.assertTrue(resp1.as(Map.class).get("message").equals("Пользователь Eva6 убрал лайк у фильма 1"));
    }

    @Test
    public void deleteLikeIdNotFoundTest() {
        fillUsersFilms();
        Response resp1 = RestUtils.delete(getUrl("/films/1/like/21"), ContentType.JSON, headers);
        Assert.assertEquals(resp1.statusCode(), 404, "Статус DELETE /films/1/like/21 должен быть 404");
        Assert.assertTrue(resp1.as(Map.class).get("error").equals("Пользователь с id=21 не найден"));
        Response resp2 = RestUtils.delete(getUrl("/films/21/like/1"), ContentType.JSON, headers);
        Assert.assertEquals(resp2.statusCode(), 404, "Статус DELETE /films/21/like/1 должен быть 404");
        Assert.assertTrue(resp2.as(Map.class).get("error").equals("Фильм с id=21 не найден"));
    }

    @Test
    public void deleteFromFriendsUserNotFoundTest() {
        fillUsersFilms();
        Response resp1 = RestUtils.delete(getUrl("/users/1/friends/21"), ContentType.JSON, headers);
        Assert.assertEquals(resp1.statusCode(), 404, "Статус DELETE /users/1/friends/21 должен быть 404");
        Assert.assertTrue(resp1.as(Map.class).get("error").equals("Один из пользователей не найден"));
        Response resp2 = RestUtils.delete(getUrl("/users/21/friends/1"), ContentType.JSON, headers);
        Assert.assertEquals(resp2.statusCode(), 404, "Статус DELETE /users/21/friends/1 должен быть 404");
        Assert.assertTrue(resp2.as(Map.class).get("error").equals("Один из пользователей не найден"));
    }

    @Test
    public void deleteLikeInvalidIdTest() {
        fillUsersFilms();
        Response resp1 = RestUtils.delete(getUrl("/films/1/like/aaa"), ContentType.JSON, headers);
        Assert.assertEquals(resp1.statusCode(), 500, "Статус DELETE /films/1/like/aaa должен быть 500");
        Assert.assertTrue(resp1.as(Map.class).get("error").toString().contains("Произошла непредвиденная ошибка"));
        Response resp2 = RestUtils.delete(getUrl("/films/aaa/like/1"), ContentType.JSON, headers);
        Assert.assertEquals(resp2.statusCode(), 500, "Статус DELETE /films/aaa/like/1 должен быть 500");
        Assert.assertTrue(resp2.as(Map.class).get("error").toString().contains("Произошла непредвиденная ошибка"));
    }

    @Test
    public void deleteFromFriendsInvalidIdTest() {
        fillUsersFilms();
        Response resp1 = RestUtils.delete(getUrl("/users/aaa/friends/1"), ContentType.JSON, headers);
        Assert.assertEquals(resp1.statusCode(), 500, "Статус DELETE /users/aaa/friends/1 должен быть 500");
        Assert.assertTrue(resp1.as(Map.class).get("error").toString().contains("Произошла непредвиденная ошибка"));
        Response resp2 = RestUtils.delete(getUrl("/users/1/friends/aaa"), ContentType.JSON, headers);
        Assert.assertEquals(resp2.statusCode(), 500, "Статус DELETE /users/1/friends/aaa должен быть 500");
        Assert.assertTrue(resp2.as(Map.class).get("error").toString().contains("Произошла непредвиденная ошибка"));
    }

    @Test
    public void getCommonFriendsList() throws JsonProcessingException {
        fillUsersFilms();
        RestUtils.put(getUrl("/users/1/friends/6"), ContentType.JSON, headers);
        RestUtils.put(getUrl("/users/1/friends/5"), ContentType.JSON, headers);
        RestUtils.put(getUrl("/users/1/friends/2"), ContentType.JSON, headers);
        RestUtils.put(getUrl("/users/2/friends/3"), ContentType.JSON, headers);
        RestUtils.put(getUrl("/users/3/friends/4"), ContentType.JSON, headers);
        RestUtils.put(getUrl("/users/3/friends/5"), ContentType.JSON, headers);
        Response resp = RestUtils.get(getUrl("/users/1/friends/common/3"), ContentType.JSON.toString());
        Assert.assertEquals(resp.statusCode(), 200, "Статус GET /users/1/friends/3 должен быть 200");
        List<User> commonFriends = resp.as(List.class);
        //Response resp1 = RestUtils.get(getUrl("/users/1/friends/3"), ContentType.JSON.toString());
        //Map<String, String> user1 = JsonUtils.convertFromJson(JsonUtils.getDtoAsJsonString(commonFriends.get(0)), Map.class);
        //Map<String, String> user2 = JsonUtils.convertFromJson(JsonUtils.getDtoAsJsonString(commonFriends.get(1)), Map.class);
        Assert.assertEquals(commonFriends.size(), 2);
        //Assert.assertTrue(user1.get("id").equals("2"));
        //Assert.assertTrue(user2.get("id").equals(""));
    }

    @Test
    public void getPopularFilmsTest() {
        fillUsersFilms();
        for (int i = 1; i <= 10; i++) {
            RestUtils.put(getUrl(String.format("/films/10/like/%d", i)), ContentType.JSON, headers);
        }
        for (int i = 1; i <= 5; i++) {
            RestUtils.put(getUrl(String.format("/films/4/like/%d", i)), ContentType.JSON, headers);
        }
        RestUtils.put(getUrl("/films/2/like/1"), ContentType.JSON, headers);
        Response resp = RestUtils.get(getUrl("/films/popular?count=3"), ContentType.JSON.toString());
        Assert.assertEquals(resp.statusCode(), 200, "Статус GET films/popular должен быть 200");
        List<User> ids = resp.as(List.class);
        Assert.assertEquals(ids.size(), 3);
        //Assert.assertTrue(ids.get(0).equals(10));
        //Assert.assertTrue(ids.get(1).equals(4));
        //Assert.assertTrue(ids.get(2).equals(2));
    }

    @Test
    public void getCommonFriendsListUserNotFoundTest() {
        fillUsersFilms();
        Response resp1 = RestUtils.get(getUrl("/users/1/friends/common/21"), ContentType.JSON.toString());
        Assert.assertEquals(resp1.statusCode(), 404, "Статус GET /users/1/friends/common/21 должен быть 404");
        Assert.assertTrue(resp1.as(Map.class).get("error").equals("Один из пользователей не найден"));
        Response resp2 = RestUtils.get(getUrl("/users/21/friends/common/1"), ContentType.JSON.toString());
        Assert.assertEquals(resp2.statusCode(), 404, "Статус GET /users/21/friends/common/1 должен быть 404");
        Assert.assertTrue(resp2.as(Map.class).get("error").equals("Один из пользователей не найден"));
    }

    @Test
    public void getCommonFriendsListInvalidIdTest() {
        fillUsersFilms();
        Response resp1 = RestUtils.get(getUrl("/users/aaa/friends/1"), ContentType.JSON.toString());
        Assert.assertEquals(resp1.statusCode(), 500, "Статус DELETE /users/aaa/friends/1 должен быть 500");
        Assert.assertTrue(resp1.as(Map.class).get("error").toString().contains("Произошла непредвиденная ошибка"));
        Response resp2 = RestUtils.get(getUrl("/users/1/friends/aaa"), ContentType.JSON.toString());
        Assert.assertEquals(resp2.statusCode(), 500, "Статус DELETE /users/1/friends/aaa должен быть 500");
        Assert.assertTrue(resp2.as(Map.class).get("error").toString().contains("Произошла непредвиденная ошибка"));
    }
}
