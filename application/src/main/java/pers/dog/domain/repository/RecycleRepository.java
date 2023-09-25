package pers.dog.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pers.dog.domain.entity.Recycle;

/**
 * @author 废柴 2023/9/22 15:08
 */
@Repository
public interface RecycleRepository extends JpaRepository<Recycle, Long>, JpaSpecificationExecutor<Recycle> {

    List<Recycle> findByProjectNameLikeIgnoreCaseOrLocationLikeIgnoreCase(String projectName, String location);
}
