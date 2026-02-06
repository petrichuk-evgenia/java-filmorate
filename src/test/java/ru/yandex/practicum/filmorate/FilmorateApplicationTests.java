package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.GenreDao;
import ru.yandex.practicum.filmorate.storage.dao.MpaDao;
import ru.yandex.practicum.filmorate.storage.impl.FilmDbStorageImpl;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorageImpl;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        UserDbStorageImpl.class,
        FilmDbStorageImpl.class,
        GenreDao.class,
        MpaDao.class
})
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmorateApplicationTests {
    private final UserDbStorageImpl userStorage;

    @Test
    void testCreateAndFindUser() {
        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.createUser(user);

        Optional<User> foundUser = userStorage.getUserById(createdUser.getId());

        assertThat(foundUser)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getId()).isEqualTo(createdUser.getId());
                    assertThat(u.getEmail()).isEqualTo("test@mail.com");
                    assertThat(u.getLogin()).isEqualTo("testuser");
                });
    }

    @Test
    void testUpdateUser() {
        User user = User.builder()
                .email("old@mail.com")
                .login("olduser")
                .name("Old Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.createUser(user);
        createdUser.setEmail("new@mail.com");
        createdUser.setName("New Name");

        User updatedUser = userStorage.updateUser(createdUser);

        assertThat(updatedUser.getEmail()).isEqualTo("new@mail.com");
        assertThat(updatedUser.getName()).isEqualTo("New Name");
    }

    @Test
    void testGetAllUsers() {
        User user1 = User.builder()
                .email("user1@mail.com")
                .login("user1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@mail.com")
                .login("user2")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();

        userStorage.createUser(user1);
        userStorage.createUser(user2);

        assertThat(userStorage.getAllUsers()).hasSize(2);
    }

}