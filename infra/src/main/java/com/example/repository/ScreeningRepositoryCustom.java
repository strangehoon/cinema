package com.example.repository;

import com.example.entity.Screening;
import com.example.enums.Genre;

import java.util.List;

public interface ScreeningRepositoryCustom {
    List<Screening> searchScreenings(int theaterId, String title, Genre genre);
}
