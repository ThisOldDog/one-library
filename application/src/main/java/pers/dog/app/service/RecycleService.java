package pers.dog.app.service;

import java.util.List;

import pers.dog.domain.entity.Project;
import pers.dog.domain.entity.Recycle;

/**
 * @author 废柴 2023/9/25 15:42
 */
public interface RecycleService {
    List<Recycle> list(String keyword);

    Project recover(Recycle recycle);

    void delete(Recycle recycle);
}
