package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.MpaDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDao mpaDao;

    private List<Mpa> allMpaCache;
    private Map<Long, Mpa> mpaByIdCache;

    public List<Mpa> getAllMpa() {
        if (allMpaCache == null) {
            allMpaCache = mpaDao.getAllMpa();
            mpaByIdCache = new HashMap<>();
            for (Mpa mpa : allMpaCache) {
                mpaByIdCache.put(mpa.getId(), mpa);
            }
        }
        return allMpaCache;
    }

    @Transactional
    public Mpa getMpaById(Long id) {
        if (mpaByIdCache != null) {
            Mpa mpa = mpaByIdCache.get(id);
            if (mpa != null) {
                log.debug("Рейтинг MPA с ID {} найден в кэше", id);
                return mpa;
            }
        }

        Mpa mpa = mpaDao.getMpaById(id)
                .orElseThrow(() -> {
                    log.error("Рейтинг MPA с ID {} не найден", id);
                    return new IdNotFoundException("Рейтинг MPA с ID " + id + " не найден");
                });

        if (mpaByIdCache != null) {
            mpaByIdCache.put(mpa.getId(), mpa);
        }

        return mpa;
    }

    public Map<Long, Mpa> getAllMpaMap() {
        getAllMpa();
        return mpaByIdCache;
    }
}